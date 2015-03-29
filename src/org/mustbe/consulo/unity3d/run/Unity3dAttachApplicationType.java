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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtensionUtil;
import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class Unity3dAttachApplicationType extends ConfigurationTypeBase
{
	public static Unity3dAttachApplicationType getInstance()
	{
		return CONFIGURATION_TYPE_EP.findExtension(Unity3dAttachApplicationType.class);
	}

	public Unity3dAttachApplicationType()
	{
		super("#UnityAttachApplication", "Unity Attach", "", Unity3dIcons.Unity3d);

		addFactory(new ConfigurationFactoryEx(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new Unity3dAttachConfiguration(project, this, null);
			}

			@Override
			public void onNewConfigurationCreated(@NotNull RunConfiguration configuration)
			{
				if(configuration instanceof Unity3dApplicationConfiguration)
				{
					((Unity3dApplicationConfiguration) configuration).setGeneratedName();
				}
			}

			@Override
			@RequiredReadAction
			public boolean isApplicable(@NotNull Project project)
			{
				return Unity3dModuleExtensionUtil.getRootModuleExtension(project) != null;
			}
		});
	}
}
