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

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.roots.impl.ExcludedContentFolderTypeProvider;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
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

		modules.add(createRootModule(project, newModel));

		modules.add(createAssemblyCSharpModuleFirstPass(project, newModel));

		modules.add(createAssemblyCSharpModule(project, newModel));

		modules.add(createAssemblyCSharpModuleEditor(project, newModel));

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

	private static Module createAssemblyCSharpModule(Project project, ModifiableModuleModel newModel)
	{
		Module assemblyCSharpModule = newModel.newModule("Assembly-CSharp", project.getBasePath() + "/Assets");

		val modifiableModel = ModuleRootManager.getInstance(assemblyCSharpModule).getModifiableModel();
		modifiableModel.addContentEntry(project.getBaseDir().getUrl()  + "/Assets");
		modifiableModel.addInvalidModuleEntry("Assembly-CSharp-firstpass");

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				modifiableModel.commit();
			}
		}.execute();
		return assemblyCSharpModule;
	}

	private static Module createAssemblyCSharpModuleFirstPass(Project project, ModifiableModuleModel newModel)
	{
		Module assemblyCSharpModule = newModel.newModule("Assembly-CSharp-firstpass", project.getBasePath() + "/Assets/Standard Assets");

		val modifiableModel = ModuleRootManager.getInstance(assemblyCSharpModule).getModifiableModel();
		for(String path : new String[]{"Standard Assets", "Pro Standard Assets", "Plugins"})
		{
			modifiableModel.addContentEntry(project.getBaseDir().getUrl()  + "/Assets/" + path);
		}

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				modifiableModel.commit();
			}
		}.execute();
		return assemblyCSharpModule;
	}

	private static Module createAssemblyCSharpModuleEditor(final Project project, ModifiableModuleModel newModel)
	{
		Module assemblyCSharpModule = newModel.newModule("Assembly-CSharp-Editor", project.getBasePath() + "/Assets/Standard Assets/Editor");

		val modifiableModel = ModuleRootManager.getInstance(assemblyCSharpModule).getModifiableModel();
		for(String path : new String[]{"Standard Assets/Editor", "Pro Standard Assets/Editor", "Plugins/Editor"})
		{
			modifiableModel.addContentEntry(project.getBaseDir().getUrl()  + "/Assets/" + path);
		}

		VirtualFile baseDir = project.getBaseDir();

		VirtualFile assetsDir = baseDir.findFileByRelativePath("Assets");
		if(assetsDir != null)
		{
			VfsUtil.visitChildrenRecursively(assetsDir, new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@NotNull VirtualFile file)
				{
					if(file.isDirectory() && "Editor".equals(file.getName()))
					{
						modifiableModel.addContentEntry(file.getUrl());
					}
					return true;
				}
			});
		}

		modifiableModel.addInvalidModuleEntry("Assembly-CSharp-firstpass");
		modifiableModel.addInvalidModuleEntry("Assembly-CSharp-Editor");

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				modifiableModel.commit();
			}
		}.execute();
		return assemblyCSharpModule;
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
