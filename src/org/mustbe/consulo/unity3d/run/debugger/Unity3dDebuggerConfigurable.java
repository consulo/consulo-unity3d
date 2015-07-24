/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d.run.debugger;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.Unity3dBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.xdebugger.impl.settings.XDebuggerSettingsManager;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
public class Unity3dDebuggerConfigurable implements Configurable
{
	private final Unity3dDebuggerSettings mySettings;

	private JBCheckBox myAttachToSingleProcessWithoutDialogBox;

	public Unity3dDebuggerConfigurable()
	{
		mySettings = XDebuggerSettingsManager.getInstanceImpl().getSettings(Unity3dDebuggerSettings.class);
	}

	@Nls
	@Override
	public String getDisplayName()
	{
		return "Unity3D";
	}

	@Nullable
	@Override
	public String getHelpTopic()
	{
		return null;
	}

	@Nullable
	@Override
	public JComponent createComponent()
	{
		JPanel root = new JPanel(new VerticalFlowLayout(true, false));
		myAttachToSingleProcessWithoutDialogBox = new JBCheckBox(Unity3dBundle.message("attach.to.single.process.without.dialog.box"),
				mySettings.myAttachToSingleProcessWithoutDialog);
		root.add(myAttachToSingleProcessWithoutDialogBox);

		return root;
	}

	@Override
	public boolean isModified()
	{
		return myAttachToSingleProcessWithoutDialogBox.isSelected() != mySettings.myAttachToSingleProcessWithoutDialog;
	}

	@Override
	public void apply() throws ConfigurationException
	{
		mySettings.myAttachToSingleProcessWithoutDialog = myAttachToSingleProcessWithoutDialogBox.isSelected();
	}

	@Override
	public void reset()
	{
		myAttachToSingleProcessWithoutDialogBox.setSelected(mySettings.myAttachToSingleProcessWithoutDialog);
	}

	@Override
	public void disposeUIResources()
	{

	}
}
