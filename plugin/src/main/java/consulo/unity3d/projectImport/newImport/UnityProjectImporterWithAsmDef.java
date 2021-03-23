/*
 * Copyright 2013-2021 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.unity3d.projectImport.newImport;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.AccessRule;
import consulo.csharp.lang.CSharpFileType;
import consulo.json.JsonFileType;
import consulo.json.jom.JomElement;
import consulo.json.jom.JomFileElement;
import consulo.json.jom.JomManager;
import consulo.unity3d.asmdef.AsmDefElement;
import consulo.unity3d.asmdef.AsmDefFileDescriptor;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.UnityProjectImportContext;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author VISTALL
 * @since 23/03/2021
 */
public class UnityProjectImporterWithAsmDef
{
	public static final String EDITOR_DIRECTORY = "Editor";
	// not ignored case
	public static final String EDITOR_PLATFORM = "Editor";

	@Nonnull
	public static List<Module> importOrUpdate(@Nonnull Project project,
											  @Nullable Sdk unitySdk,
											  @Nullable ModifiableModuleModel originalModel,
											  @Nonnull ProgressIndicator progressIndicator,
											  @Nullable Collection<String> defines)
	{
		boolean fromProjectStructure = originalModel != null;

		VirtualFile baseDir = project.getBaseDir();
		assert baseDir != null;

		UnityProjectImportContext context = UnityProjectImportContext.load(project, defines, baseDir, progressIndicator, unitySdk);

		ModifiableModuleModel newModel = fromProjectStructure ? originalModel : AccessRule.read(() -> ModuleManager.getInstance(project).getModifiableModel());
		assert newModel != null;

		List<Module> modules = new ArrayList<>();

		ContainerUtil.addIfNotNull(modules, Unity3dProjectImporter.createRootModule(project, newModel, unitySdk, progressIndicator, context));
		progressIndicator.setFraction(0.1);

		VirtualFile assetsDir = baseDir.findChild(Unity3dProjectImporter.ASSETS_DIRECTORY);
		if(assetsDir == null)
		{
			progressIndicator.setFraction(1);
			progressIndicator.setText(null);

			if(!fromProjectStructure)
			{
				WriteAction.runAndWait(newModel::commit);
			}

			return modules;
		}

		Map<String, UnityAssemblyContext> asmdefs = new TreeMap<>();
		// default defines
		asmdefs.put("Assembly-CSharp-firstpass", new UnityAssemblyContext("Assembly-CSharp-firstpass", null, null));
		asmdefs.put("Assembly-CSharp", new UnityAssemblyContext("Assembly-CSharp", null, null));
		asmdefs.put("Assembly-CSharp-Editor", new UnityAssemblyContext("Assembly-CSharp-Editor", null, null));

		JomManager jomManager = JomManager.getInstance(project);
		PsiManager psiManager = PsiManager.getInstance(project);

		VfsUtil.visitChildrenRecursively(assetsDir, new VirtualFileVisitor()
		{
			@Override
			@RequiredReadAction
			public boolean visitFile(@Nonnull VirtualFile file)
			{
				if(FileTypeManager.getInstance().isFileIgnored(file))
				{
					return false;
				}

				if(file.getFileType() == JsonFileType.INSTANCE && AsmDefFileDescriptor.EXTENSION.equals(file.getExtension()))
				{
					ReadAction.run(() -> {
						PsiFile maybeJsonFile = psiManager.findFile(file);
						if(maybeJsonFile != null)
						{
							JomFileElement<JomElement> fileElement = jomManager.getFileElement(maybeJsonFile);
							if(fileElement != null && fileElement.getRootElement() instanceof AsmDefElement def)
							{
								String name = def.getName();
								if(!StringUtil.isEmptyOrSpaces(name))
								{
									asmdefs.put(name, new UnityAssemblyContext(name, file, def));
								}
							}
						}
					});
				}
				return super.visitFile(file);
			}
		});

		// set of registered file urls
		Set<String> registeredFiles = new HashSet<>();
		// todo

		for(UnityAssemblyContext assemblyContext : asmdefs.values())
		{
			VirtualFile asmdefFile = assemblyContext.getAsmdefFile();
			if(asmdefFile == null)
			{
				continue;
			}

			//boolean isAllowEditorDir = assemblyContext.getAsmDefElement().getIncludePlatforms().contains(EDITOR_PLATFORM);

			VfsUtil.visitChildrenRecursively(asmdefFile.getParent(), new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@Nonnull VirtualFile file)
				{
					if(FileTypeManager.getInstance().isFileIgnored(file))
					{
						return false;
					}

					// editor dir stopper
					if(EDITOR_DIRECTORY.equalsIgnoreCase(file.getName()))
					{
						return false;
					}

					if(file.getFileType() == CSharpFileType.INSTANCE)
					{
						assemblyContext.addSourceFile(file);

						registeredFiles.add(file.getUrl());
					}

					return super.visitFile(file);
				}
			});
		}

		progressIndicator.setFraction(1);
		progressIndicator.setText(null);

		if(!fromProjectStructure)
		{
			WriteAction.runAndWait(newModel::commit);
		}
		return modules;
	}
}
