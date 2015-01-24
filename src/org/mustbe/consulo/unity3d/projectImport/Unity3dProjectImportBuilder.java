/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.unity3d.projectImport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.module.roots.DotNetLibraryOrderEntryImpl;
import org.mustbe.consulo.roots.impl.ExcludedContentFolderTypeProvider;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import org.mustbe.consulo.unity3d.bundle.UnityDefineByVersion;
import org.mustbe.consulo.unity3d.csharp.module.extension.Unity3dCSharpMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dTarget;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.ModuleRootLayerImpl;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 29.12.14
 */
public class Unity3dProjectImportBuilder extends ProjectImportBuilder
{
	@NotNull
	@Override
	public String getName()
	{
		return "Unity3D";
	}

	@Override
	public Icon getIcon()
	{
		return Unity3dIcons.Unity3d;
	}

	@Override
	public List getList()
	{
		return null;
	}

	@Override
	public boolean isMarked(Object element)
	{
		return false;
	}

	@Override
	public void setList(List list) throws ConfigurationException
	{

	}

	@Override
	public void setOpenProjectSettingsAfter(boolean on)
	{

	}

	@Nullable
	@Override
	public List<Module> commit(Project project,
			ModifiableModuleModel originalModel,
			ModulesProvider modulesProvider,
			ModifiableArtifactModel artifactModel)
	{
		val fromProjectStructure = originalModel != null;

		val newModel = fromProjectStructure ? originalModel : ModuleManager.getInstance(project).getModifiableModel();

		List<Module> modules = new ArrayList<Module>(5);

		Sdk unitySdkType = SdkTable.getInstance().findMostRecentSdkOfType(Unity3dBundleType.getInstance());

		ContainerUtil.addIfNotNull(modules, createRootModule(project, newModel));

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleEditor(project, newModel, unitySdkType));

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleFirstPass(project, newModel, unitySdkType));

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModule(project, newModel, unitySdkType));

		//TODO [VISTALL] Assembly-UnityScript-firstpass??

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				if(!fromProjectStructure)
				{
					newModel.commit();
				}
			}
		}.execute();
		return modules;
	}

	private static Module createAssemblyCSharpModule(Project project, ModifiableModuleModel newModel, final Sdk unityBundle)
	{
		return createAndSetupModule("Assembly-CSharp", project, newModel, new String[]{"Assets"}, unityBundle, new Consumer<ModuleRootLayerImpl>()
		{
			@Override
			public void consume(ModuleRootLayerImpl layer)
			{
				layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
			}
		});
	}

	private static Module createAssemblyCSharpModuleFirstPass(Project project, ModifiableModuleModel newModel, final Sdk unityBundle)
	{
		String[] paths = new String[]{
				"Assets/Standard Assets",
				"Assets/Pro Standard Assets",
				"Assets/Plugins"
		};

		return createAndSetupModule("Assembly-CSharp-firstpass", project, newModel, paths, unityBundle, new Consumer<ModuleRootLayerImpl>()
		{
			@Override
			public void consume(ModuleRootLayerImpl layer)
			{
			}
		});
	}

	private static Module createAssemblyCSharpModuleEditor(final Project project, ModifiableModuleModel newModel, final Sdk unityBundle)
	{
		val paths = new ArrayList<String>();
		paths.add("Assets/Standard Assets/Editor");
		paths.add("Assets/Pro Standard Assets/Editor");
		paths.add("Assets/Plugins/Editor");

		val baseDir = project.getBaseDir();

		val assetsDir = baseDir.findFileByRelativePath("Assets");
		if(assetsDir != null)
		{
			VfsUtil.visitChildrenRecursively(assetsDir, new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@NotNull VirtualFile file)
				{
					if(file.isDirectory() && "Editor".equals(file.getName()))
					{
						paths.add(VfsUtil.getRelativePath(file, baseDir, '/'));
					}
					return true;
				}
			});
		}

		return createAndSetupModule("Assembly-CSharp-Editor", project, newModel, ArrayUtil.toStringArray(paths), unityBundle, new Consumer<ModuleRootLayerImpl>()

		{
			@Override
			public void consume(ModuleRootLayerImpl layer)
			{
				layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
				layer.addInvalidModuleEntry("Assembly-CSharp");

				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor.Graphs"));
			}
		});
	}

	private static Module createAndSetupModule(String moduleName,
			Project project,
			ModifiableModuleModel modifiableModuleModels,
			String[] paths, Sdk unitySdk, Consumer<ModuleRootLayerImpl> setupConsumer)
	{
		for(int i = 0; i < paths.length; i++)
		{
			paths[i] = project.getBasePath() + "/" + paths[i];
		}

		VirtualFile targetDir = null;
		for(String path : paths)
		{
			VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
			if(virtualFile != null)
			{
				targetDir = virtualFile;
				break;
			}
		}

		if(targetDir == null)
		{
			File file = new File(project.getBasePath(), paths[0]);

			FileUtil.createDirectory(file);
			targetDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
		}

		if(targetDir == null)
		{
			return null;
		}

		Module module = modifiableModuleModels.newModule(moduleName, targetDir.getPath());
		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		val modifiableModel = moduleRootManager.getModifiableModel();

		modifiableModel.removeLayer("Default", false);

		for(Unity3dTarget unity3dTarget : Unity3dTarget.values())
		{
			ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableModel.addLayer(unity3dTarget.getPresentation(), null,
					getDefaultTarget() == unity3dTarget);

			for(String path : paths)
			{
				modifiableModel.addContentEntry(VirtualFileManager.constructUrl(StandardFileSystems.FILE_PROTOCOL, path));
			}

			setupConsumer.consume(layer);

			Unity3dMutableModuleExtension ext = layer.getExtensionWithoutCheck(Unity3dMutableModuleExtension.class);
			assert ext != null;

			ext.getInheritableSdk().set(null, unitySdk);
			ext.setEnabled(true);
			ext.setBuildTarget(unity3dTarget);
			ext.getVariables().add(unity3dTarget.getDefineName());

			if(unitySdk != null)
			{
				UnityDefineByVersion unityDefineByVersion = UnityDefineByVersion.find(unitySdk.getVersionString());
				if(unityDefineByVersion != UnityDefineByVersion.UNKNOWN)
				{
					ext.getVariables().add(unityDefineByVersion.name());
				}
			}

			layer.getExtensionWithoutCheck(Unity3dCSharpMutableModuleExtension.class).setEnabled(true);

			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "mscorlib"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Core"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml.Linq"));
		}

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				modifiableModel.commit();
			}
		}.execute();
		return module;
	}

	private static Unity3dTarget getDefaultTarget()
	{
		if(SystemInfo.isWindows)
		{
			return Unity3dTarget.Windows;
		}
		else if(SystemInfo.isLinux)
		{
			return Unity3dTarget.LinuxUniversal;
		}
		else if(SystemInfo.isMac)
		{
			return Unity3dTarget.OSXUniversal;
		}
		throw new IllegalArgumentException(SystemInfo.OS_NAME);
	}

	private static Module createRootModule(Project project, ModifiableModuleModel newModel)
	{
		Module rootModule = newModel.newModule(project.getName(), project.getBasePath());

		String projectUrl = project.getBaseDir().getUrl();

		val rootModifiableModel = ModuleRootManager.getInstance(rootModule).getModifiableModel();
		ContentEntry contentEntry = rootModifiableModel.addContentEntry(projectUrl);

		// exclude temp dirs
		contentEntry.addFolder(projectUrl + "/" + Project.DIRECTORY_STORE_FOLDER, ExcludedContentFolderTypeProvider.getInstance());
		contentEntry.addFolder(projectUrl + "/Library", ExcludedContentFolderTypeProvider.getInstance());
		contentEntry.addFolder(projectUrl + "/Temp", ExcludedContentFolderTypeProvider.getInstance());
		contentEntry.addFolder(projectUrl + "/test_Data", ExcludedContentFolderTypeProvider.getInstance());

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				rootModifiableModel.commit();
			}
		}.execute();
		return rootModule;
	}
}
