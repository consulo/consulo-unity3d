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

import consulo.configurable.ConfigurationException;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.ui.*;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.LabeledLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.LabeledComponents;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class Unity3dConfigurationEditor extends SettingsEditor<Unity3dAttachConfiguration>
{
	private RadioButton myUnityEditorButton;
	private RadioButton myProcessWithNameButton;
	private RadioButton mySelectFromDialogButton;
	private TextBox myNameTextField;

	@Override
	@RequiredUIAccess
	protected void resetEditorFrom(Unity3dAttachConfiguration runConfiguration)
	{
		selectRadioButton(runConfiguration.getAttachTarget()).setValue(true);
		myNameTextField.setValue(runConfiguration.getProcessName());
	}

	@Override
	@RequiredUIAccess
	protected void applyEditorTo(Unity3dAttachConfiguration runConfiguration) throws ConfigurationException
	{
		for(Unity3dAttachConfiguration.AttachTarget target : Unity3dAttachConfiguration.AttachTarget.values())
		{
			RadioButton radioButton = selectRadioButton(target);
			if(radioButton.getValue())
			{
				runConfiguration.setAttachTarget(target);
				break;
			}
		}
		runConfiguration.setProcessName(myNameTextField.getValue());
	}

	@Nullable
	@Override
	@RequiredUIAccess
	protected Component createUIComponent()
	{
		LabeledLayout layout = LabeledLayout.create("Attach to");

		ValueGroup<Boolean> group = ValueGroups.boolGroup();
		VerticalLayout vertical = VerticalLayout.create();
		layout.set(vertical);

		myUnityEditorButton = RadioButton.create("Unity Editor");
		vertical.add(myUnityEditorButton);
		group.add(myUnityEditorButton);

		myProcessWithNameButton = RadioButton.create("Process");
		vertical.add(myProcessWithNameButton);
		group.add(myProcessWithNameButton);

		myNameTextField = TextBox.create();
		myNameTextField.setEnabled(false);
		vertical.add(LabeledComponents.leftFilled("Name", myNameTextField));
		myProcessWithNameButton.addValueListener(valueEvent -> myNameTextField.setEnabled(valueEvent.getValue()));

		mySelectFromDialogButton = RadioButton.create("Selected process in dialog");
		vertical.add(mySelectFromDialogButton);
		group.add(mySelectFromDialogButton);

		return layout;
	}

	private RadioButton selectRadioButton(Unity3dAttachConfiguration.AttachTarget target)
	{
		switch(target)
		{
			case UNITY_EDITOR:
				return myUnityEditorButton;
			case BY_NAME:
				return myProcessWithNameButton;
			case FROM_DIALOG:
				return mySelectFromDialogButton;
			default:
				throw new IllegalArgumentException(target.name());
		}
	}
}
