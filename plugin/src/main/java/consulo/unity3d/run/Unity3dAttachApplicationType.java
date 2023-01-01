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

package consulo.unity3d.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.configuration.ConfigurationFactoryEx;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.RunConfiguration;
import consulo.project.Project;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 10.11.14
 */
@ExtensionImpl
public class Unity3dAttachApplicationType extends ConfigurationTypeBase
{
	@Nonnull
	public static Unity3dAttachApplicationType getInstance()
	{
		return EP_NAME.findExtensionOrFail(Unity3dAttachApplicationType.class);
	}

	public Unity3dAttachApplicationType()
	{
		super("Unity3dAttachApplicationType", "Unity Debug Attach", "", Unity3dIcons.Unity3d);

		addFactory(new ConfigurationFactoryEx(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new Unity3dAttachConfiguration(project, this);
			}

			@Override
			public boolean isApplicable(@Nonnull Project project)
			{
				return Unity3dModuleExtensionUtil.getRootModuleExtension(project) != null;
			}
		});
	}
}
