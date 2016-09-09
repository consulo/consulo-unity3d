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

package consulo.unity3d.run;

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import consulo.unity3d.Unity3dIcons;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class Unity3dAttachApplicationType extends ConfigurationTypeBase
{
	public static final Unity3dAttachApplicationType ourDummyInstance = new Unity3dAttachApplicationType();

	public Unity3dAttachApplicationType()
	{
		super("#UnityAttachApplication", "Unity Attach", "", Unity3dIcons.Attach);

		addFactory(new ConfigurationFactoryEx(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new Unity3dAttachConfiguration(project, this);
			}

			@Override
			public boolean isApplicable(@NotNull Project project)
			{
				throw new UnsupportedOperationException();
			}
		});
	}
}
