/*
 * Copyright 2013-2016 consulo.io
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

package consulo.unity3d.run.test;

import javax.annotation.Nonnull;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import consulo.annotations.RequiredReadAction;
import consulo.awt.TargetAWT;
import consulo.ide.IconDescriptor;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;

/**
 * @author VISTALL
 * @since 18.01.2016
 */
public class Unity3dTestConfigurationType extends ConfigurationTypeBase
{
	@Nonnull
	public static Unity3dTestConfigurationType getInstance()
	{
		return CONFIGURATION_TYPE_EP.findExtension(Unity3dTestConfigurationType.class);
	}

	public Unity3dTestConfigurationType()
	{
		super("#Unity3dTestConfigurationType", "Unity Test", "", new IconDescriptor(TargetAWT.to(Unity3dIcons.Unity3d)).addLayerIcon(AllIcons.Nodes.JunitTestMark).toIcon());

		addFactory(new ConfigurationFactory(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new Unity3dTestConfiguration(project, this, "Unnamed");
			}

			@Override
			@RequiredReadAction
			public boolean isApplicable(@Nonnull Project project)
			{
				return Unity3dModuleExtensionUtil.getRootModuleExtension(project) != null;
			}
		});
	}
}
