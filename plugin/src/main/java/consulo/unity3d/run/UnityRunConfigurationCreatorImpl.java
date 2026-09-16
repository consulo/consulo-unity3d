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

import consulo.annotation.component.ServiceImpl;
import consulo.application.AccessRule;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunConfiguration;
import consulo.project.Project;
import consulo.unity3d.base.run.UnityRunConfigurationCreator;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * @author VISTALL
 */
@Singleton
@ServiceImpl
public class UnityRunConfigurationCreatorImpl implements UnityRunConfigurationCreator {
    private static final String UNITY_EDITOR_ATTACH = "Attach to Unity Editor";

    @Override
    public void createDefaultConfiguration(@Nonnull Project project) {
        RunManager runManager = RunManager.getInstance(project);
        List<RunConfiguration> allConfigurationsList = runManager.getAllConfigurationsList();

        Optional<RunConfiguration> first =
            allConfigurationsList.stream().filter(runConfiguration -> UNITY_EDITOR_ATTACH.equals(runConfiguration.getName())).findFirst();
        if (first.isPresent()) {
            return;
        }

        ConfigurationFactory factory = Unity3dAttachApplicationType.getInstance().getConfigurationFactories()[0];

        RunnerAndConfigurationSettings configurationSettings = runManager.createRunConfiguration(UNITY_EDITOR_ATTACH, factory);
        Unity3dAttachConfiguration configuration = (Unity3dAttachConfiguration) configurationSettings.getConfiguration();
        configuration.setAttachTarget(Unity3dAttachConfiguration.AttachTarget.UNITY_EDITOR);

        configurationSettings.setSingleton(true);

        runManager.addConfiguration(configurationSettings, false);

        AccessRule.read(() -> runManager.setSelectedConfiguration(configurationSettings));
    }
}
