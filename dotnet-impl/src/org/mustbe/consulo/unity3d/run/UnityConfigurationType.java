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

package org.mustbe.consulo.unity3d.run;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.module.extension.ModuleExtensionHelper;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import lombok.val;

/**
 * @author VISTALL
 * @since 17.11.2014
 */
public class UnityConfigurationType extends ConfigurationTypeBase
{
	public static UnityConfigurationType getInstance()
	{
		return CONFIGURATION_TYPE_EP.findExtension(UnityConfigurationType.class);
	}

	public UnityConfigurationType()
	{
		super("#UnityConfigurationType", "Unity", "", Unity3dIcons.Unity3d);

		addFactory(new ConfigurationFactoryEx(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new UnityConfiguration("Unnamed", new RunConfigurationModule(project), this);
			}

			@Override
			public void onNewConfigurationCreated(@NotNull RunConfiguration configuration)
			{
				UnityConfiguration dotNetConfiguration = (UnityConfiguration) configuration;

				for(val module : ModuleManager.getInstance(configuration.getProject()).getModules())
				{
					DotNetModuleExtension extension = ModuleUtilCore.getExtension(module, Unity3dModuleExtension.class);
					if(extension != null)
					{
						dotNetConfiguration.setName(module.getName());
						dotNetConfiguration.setModule(module);
						dotNetConfiguration.setWorkingDirectory(extension.getOutputDir());
						break;
					}
				}
			}

			@Override
			public boolean isApplicable(@NotNull Project project)
			{
				return ModuleExtensionHelper.getInstance(project).hasModuleExtension(Unity3dModuleExtension.class);
			}
		});
	}
}
