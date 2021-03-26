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
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import consulo.application.AccessRule;
import consulo.csharp.lang.CSharpFileType;
import consulo.roots.impl.ProductionContentFolderTypeProvider;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.UnityProjectImportContext;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharp;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharpEditor;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharpFirstPass;
import consulo.unity3d.projectImport.newImport.standardImporter.StandardModuleImporter;

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

	private static StandardModuleImporter[] ourStandardModuleImporters = {
			new AssemblyCSharpFirstPass(),
			new AssemblyCSharpEditor(),
			new AssemblyCSharp(),
	};

	@Nonnull
	public static List<Module> importOrUpdate(@Nonnull Project project,
											  @Nullable Sdk unitySdk,
											  @Nullable ModifiableModuleModel originalModel,
											  @Nonnull ProgressIndicator progressIndicator,
											  @Nullable Collection<String> defines)
	{
		boolean fromProjectStructure = originalModel != null;

		progressIndicator.setIndeterminate(true);

		VirtualFile baseDir = project.getBaseDir();
		assert baseDir != null;

		UnityProjectImportContext context = UnityProjectImportContext.load(project, defines, baseDir, progressIndicator, unitySdk);

		ModifiableModuleModel newModulesModel = fromProjectStructure ? originalModel : AccessRule.read(() -> ModuleManager.getInstance(project).getModifiableModel());
		assert newModulesModel != null;

		List<Module> modules = new ArrayList<>();

		modules.add(Unity3dProjectImporter.createRootModule(project, newModulesModel, unitySdk, progressIndicator, context));

		VirtualFile assetsDir = baseDir.findChild(Unity3dProjectImporter.ASSETS_DIRECTORY);
		if(assetsDir == null)
		{
			progressIndicator.setText(null);

			if(!fromProjectStructure)
			{
				WriteAction.runAndWait(newModulesModel::commit);
			}

			return modules;
		}

		Map<String, UnityAssemblyContext> asmdefs = new TreeMap<>();
		// standard modules
		for(StandardModuleImporter importer : ourStandardModuleImporters)
		{
			asmdefs.put(importer.getName(), new UnityAssemblyContext(importer.getName(), null, null));
		}

		VfsUtil.visitChildrenRecursively(assetsDir, new AssemblyFileVisitor(project, asmdefs));

		VirtualFile packagesDir = baseDir.findChild(Unity3dProjectImporter.PACKAGES_DIRECTORY);
		if(packagesDir != null && packagesDir.isDirectory())
		{
			for(VirtualFile packageDirectory : packagesDir.getChildren())
			{
				if(packageDirectory.isDirectory() && packageDirectory.findChild("package.json") != null)
				{
					VfsUtil.visitChildrenRecursively(packageDirectory, new AssemblyFileVisitor(project, asmdefs));
				}
			}
		}

		// set of registered file urls
		Set<String> registeredFiles = new HashSet<>();

		// analyze first of all assemby defs
		for(UnityAssemblyContext assemblyContext : asmdefs.values())
		{
			VirtualFile asmdefFile = assemblyContext.getAsmdefFile();
			if(asmdefFile == null)
			{
				continue;
			}

			progressIndicator.setText("Analyzing " + assemblyContext.getName());

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

		for(StandardModuleImporter importer : ourStandardModuleImporters)
		{
			UnityAssemblyContext assemblyContext = asmdefs.get(importer.getName());

			assert assemblyContext != null;

			importer.analyzeSourceFiles(project, assemblyContext, registeredFiles);
		}

		// first asmdefs
		for(UnityAssemblyContext assemblyContext : asmdefs.values())
		{
			VirtualFile asmdefFile = assemblyContext.getAsmdefFile();
			if(asmdefFile == null)
			{
				continue;
			}

			Module assembyModule = newModulesModel.findModuleByName(assemblyContext.getName());
			if(assembyModule != null)
			{
				newModulesModel.disposeModule(assembyModule);
			}

			assembyModule = newModulesModel.newModule(assemblyContext.getName(), asmdefFile.getParent().getPath());
			modules.add(assembyModule);
			final Module finalAssembyModule = assembyModule;

			ModifiableRootModel rootModel = ReadAction.compute(() -> ModuleRootManager.getInstance(finalAssembyModule).getModifiableModel());

			rootModel.addContentEntry(asmdefFile.getParent()).addFolder(asmdefFile.getParent(), ProductionContentFolderTypeProvider.getInstance());

			WriteAction.runAndWait(rootModel::commit);
		}

		for(StandardModuleImporter importer : ourStandardModuleImporters)
		{
			UnityAssemblyContext assemblyContext = asmdefs.get(importer.getName());

			assert assemblyContext != null;

			Module assembyModule = newModulesModel.findModuleByName(assemblyContext.getName());
			if(assembyModule != null)
			{
				newModulesModel.disposeModule(assembyModule);
			}

			assembyModule = newModulesModel.newModule(assemblyContext.getName(), null);
			modules.add(assembyModule);
			final Module finalAssembyModule = assembyModule;

			ModifiableRootModel rootModel = ReadAction.compute(() -> ModuleRootManager.getInstance(finalAssembyModule).getModifiableModel());

			for(VirtualFile file : assemblyContext.getSourceFiles())
			{
				rootModel.addSingleContentEntry(file);
			}

			WriteAction.runAndWait(rootModel::commit);
		}



		// todo


		progressIndicator.setIndeterminate(false);
		progressIndicator.setFraction(1);
		progressIndicator.setText(null);

		if(!fromProjectStructure)
		{
			WriteAction.runAndWait(newModulesModel::commit);
		}
		return modules;
	}
}
