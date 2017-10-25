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

import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.util.ui.JBUI;
import consulo.ui.Component;
import consulo.ui.VerticalLayout;
import consulo.util.ui.components.VerticalLayoutPanel;

/**
 * @author VISTALL
 * @since 18.01.16
 */
public class Unity3dTestConfigurationEditor extends SettingsEditor<Unity3dTestConfiguration>
{
	@Override
	protected void resetEditorFrom(Unity3dTestConfiguration runConfiguration)
	{
	}

	@Override
	protected void applyEditorTo(Unity3dTestConfiguration runConfiguration) throws ConfigurationException
	{
	}

	@Nullable
	@Override
	protected Component createUIComponent()
	{
		return VerticalLayout.create();
	}
}
