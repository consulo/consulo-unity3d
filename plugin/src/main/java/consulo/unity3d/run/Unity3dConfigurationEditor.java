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
import consulo.localize.LocalizeValue;
import consulo.ui.Component;
import consulo.ui.RadioButton;
import consulo.ui.RadioGroup;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.LabeledLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.Indenter;
import consulo.ui.util.LabeledBuilder;
import consulo.ui.util.LabeledComponents;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class Unity3dConfigurationEditor extends SettingsEditor<Unity3dAttachConfiguration> {
    private RadioButton myUnityEditorButton;
    private RadioButton myProcessWithNameButton;
    private RadioButton mySelectFromDialogButton;
    private TextBox myNameTextField;

    private RadioGroup<Unity3dAttachConfiguration.AttachTarget> myAttachGroup;

    @Override
    @RequiredUIAccess
    protected void resetEditorFrom(Unity3dAttachConfiguration runConfiguration) {
        selectRadioButton(runConfiguration.getAttachTarget()).setValue(true);
        myNameTextField.setValue(runConfiguration.getProcessName());
    }

    @Override
    @RequiredUIAccess
    protected void applyEditorTo(Unity3dAttachConfiguration runConfiguration) throws ConfigurationException {
        for (Unity3dAttachConfiguration.AttachTarget target : Unity3dAttachConfiguration.AttachTarget.values()) {
            RadioButton radioButton = selectRadioButton(target);
            if (radioButton.getValue()) {
                runConfiguration.setAttachTarget(target);
                break;
            }
        }
        runConfiguration.setProcessName(myNameTextField.getValue());
    }

    @Nullable
    @Override
    @RequiredUIAccess
    protected Component createUIComponent() {
        LabeledLayout layout = LabeledLayout.create("Attach to");

        myAttachGroup = RadioGroup.create();

        VerticalLayout vertical = VerticalLayout.create();
        layout.set(vertical);

        myUnityEditorButton = myAttachGroup.newButton(LocalizeValue.localizeTODO("Unity Editor"), Unity3dAttachConfiguration.AttachTarget.UNITY_EDITOR);
        vertical.add(myUnityEditorButton);

        myProcessWithNameButton = myAttachGroup.newButton(LocalizeValue.localizeTODO("Process"), Unity3dAttachConfiguration.AttachTarget.BY_NAME);
        vertical.add(myProcessWithNameButton);

        myNameTextField = TextBox.create();
        myNameTextField.setEnabled(false);
        vertical.add(Indenter.indent(LabeledBuilder.filled(LocalizeValue.localizeTODO("Name"), myNameTextField)));
        myProcessWithNameButton.addValueListener(valueEvent -> myNameTextField.setEnabled(valueEvent.getValue()));

        mySelectFromDialogButton = myAttachGroup.newButton(LocalizeValue.localizeTODO("Selected process in dialog"), Unity3dAttachConfiguration.AttachTarget.FROM_DIALOG);
        vertical.add(mySelectFromDialogButton);

        return layout;
    }

    private RadioButton selectRadioButton(Unity3dAttachConfiguration.AttachTarget target) {
        return switch (target) {
            case UNITY_EDITOR -> myUnityEditorButton;
            case BY_NAME -> myProcessWithNameButton;
            case FROM_DIALOG -> mySelectFromDialogButton;
            default -> throw new IllegalArgumentException(target.name());
        };
    }
}
