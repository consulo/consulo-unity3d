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

package consulo.unity3d.projectImport;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.StaticGetter;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
import consulo.annotations.RequiredReadAction;
import consulo.unity3d.Unity3dIcons;

/**
 * @author VISTALL
 * @since 29.12.14
 */
public class Unity3dProjectImportBuilder extends ProjectImportBuilder
{
	private Sdk myUnitySdk;

	public void setUnitySdk(Sdk unitySdk)
	{
		myUnitySdk = unitySdk;
	}

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
	@RequiredReadAction
	public List<Module> commit(final Project project, @Nullable ModifiableModuleModel originalModel, ModulesProvider modulesProvider, ModifiableArtifactModel artifactModel)
	{
		Sdk unitySdk = myUnitySdk;
		myUnitySdk = null; // drop link to sdk

		boolean fromProjectStructure = originalModel != null;

		final ModifiableModuleModel newModel = fromProjectStructure ? originalModel : ModuleManager.getInstance(project).getModifiableModel();

		Module rootModule = newModel.newModule(project.getName(), project.getBasePath());

		if(!fromProjectStructure)
		{
			new WriteAction<Object>()
			{
				@Override
				protected void run(Result<Object> result) throws Throwable
				{
					newModel.commit();
				}
			}.execute();
		}

		project.putUserData(Unity3dProjectUtil.NEWLY_IMPORTED_PROJECT_SDK, new StaticGetter<Sdk>(unitySdk));
		return Arrays.asList(rootModule);
	}
}
