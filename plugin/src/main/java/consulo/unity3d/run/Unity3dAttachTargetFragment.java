/*
 * Copyright 2013-2026 consulo.io
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

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.RadioGroup;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import consulo.util.lang.StringUtil;

/**
 * What a {@link Unity3dAttachConfiguration} attaches to: the Unity Editor, a process by name, or a process picked in a dialog.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
class Unity3dAttachTargetFragment implements RunConfigurationFragment<Unity3dAttachConfiguration> {
    @Override
    public String getId() {
        return "unityAttachTarget";
    }

    @Override
    public LocalizeValue getName() {
        return LocalizeValue.localizeTODO("Attach to");
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<Unity3dAttachConfiguration> createEditor(Project project, Disposable parent) {
        RadioGroup<Unity3dAttachConfiguration.AttachTarget> group = RadioGroup.create();
        VerticalLayout targets = group.fillByEnum(Unity3dAttachConfiguration.AttachTarget.class, Unity3dAttachTargetFragment::getText);

        TextBox nameField = TextBox.create();
        group.addValueListener(value -> nameField.setEnabled(value == Unity3dAttachConfiguration.AttachTarget.BY_NAME));

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(LocalizeValue.localizeTODO("Attach to:"), targets);
                layout.addLabeled(LocalizeValue.localizeTODO("Process name:"), nameField);
            }

            @RequiredUIAccess
            @Override
            public void reset(Unity3dAttachConfiguration target) {
                group.setValue(target.getAttachTarget(), false);
                nameField.setValue(StringUtil.notNullize(target.getProcessName()));
                nameField.setEnabled(target.getAttachTarget() == Unity3dAttachConfiguration.AttachTarget.BY_NAME);
            }

            @RequiredUIAccess
            @Override
            public void apply(Unity3dAttachConfiguration target) {
                Unity3dAttachConfiguration.AttachTarget attachTarget = group.getValue();
                target.setAttachTarget(attachTarget == null ? Unity3dAttachConfiguration.AttachTarget.UNITY_EDITOR : attachTarget);
                target.setProcessName(nameField.getValue());
            }
        };
    }

    private static LocalizeValue getText(Unity3dAttachConfiguration.AttachTarget target) {
        return switch (target) {
            case UNITY_EDITOR -> LocalizeValue.localizeTODO("Unity Editor");
            case BY_NAME -> LocalizeValue.localizeTODO("Process");
            case FROM_DIALOG -> LocalizeValue.localizeTODO("Selected process in dialog");
        };
    }
}
