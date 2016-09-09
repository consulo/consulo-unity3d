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
import org.jetbrains.annotations.Nullable;
import consulo.unity3d.run.debugger.UnityProcess;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.CompatibilityAwareRunProfile;
import com.intellij.execution.configuration.EmptyRunProfileState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import consulo.annotations.RequiredReadAction;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class Unity3dAttachConfiguration extends LocatableConfigurationBase implements ModuleRunProfile,
		RunConfigurationWithSuppressedDefaultRunAction, CompileStepBeforeRun.Suppressor, CompatibilityAwareRunProfile
{
	private UnityProcess myUnityProcess;

	public Unity3dAttachConfiguration(Project project, ConfigurationFactory factory)
	{
		super(project, factory, "dummy");
		myUnityProcess = new UnityProcess(-1, "", "", -1);
	}

	public Unity3dAttachConfiguration(Project project, ConfigurationFactory factory, @NotNull UnityProcess unityProcess)
	{
		super(project, factory, "Unity3D Attach to PID: " + unityProcess.hashCode());
		myUnityProcess = unityProcess;
	}

	public UnityProcess getUnityProcess()
	{
		return myUnityProcess;
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
	{
		return new Unity3dConfigurationEditor();
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException
	{
		return EmptyRunProfileState.INSTANCE;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public Module[] getModules()
	{
		return ModuleManager.getInstance(getProject()).getModules();
	}

	@Override
	public boolean mustBeStoppedToRun(@NotNull RunConfiguration configuration)
	{
		return configuration != this && configuration instanceof Unity3dAttachConfiguration && myUnityProcess.hashCode() == (
				(Unity3dAttachConfiguration) configuration).getUnityProcess().hashCode();
	}
}
