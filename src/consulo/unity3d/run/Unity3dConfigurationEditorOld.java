/*
 * Copyright 2013-2017 consulo.io
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

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;

/**
 * @author VISTALL
 * @since 10.11.14
 */
@Deprecated
public class Unity3dConfigurationEditorOld extends SettingsEditor<RunConfiguration>
{
	@Override
	protected void resetEditorFrom(RunConfiguration runConfiguration)
	{
	}

	@Override
	protected void applyEditorTo(RunConfiguration runConfiguration) throws ConfigurationException
	{
	}

	@NotNull
	@Override
	protected JComponent createEditor()
	{
		return new JPanel();
	}
}
