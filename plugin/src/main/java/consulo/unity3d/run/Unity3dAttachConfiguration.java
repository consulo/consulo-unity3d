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

import consulo.annotation.access.RequiredReadAction;
import consulo.compiler.execution.CompileStepBeforeRun;
import consulo.execution.DefaultExecutionResult;
import consulo.execution.configuration.*;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.process.ExecutionException;
import consulo.process.NopProcessHandler;
import consulo.project.Project;
import consulo.unity3d.run.debugger.UnityDebugProcessInfo;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.util.xml.serializer.XmlSerializer;
import org.jdom.Element;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class Unity3dAttachConfiguration extends LocatableConfigurationBase implements ModuleRunProfile, RunConfigurationWithSuppressedDefaultRunAction, CompileStepBeforeRun.Suppressor
{
	public enum AttachTarget
	{
		UNITY_EDITOR,
		BY_NAME,
		FROM_DIALOG
	}

	private AttachTarget myAttachTarget = AttachTarget.UNITY_EDITOR;
	private String myProcessName;

	// for attaching from attach action
	@Nullable
	private UnityDebugProcessInfo myForceUnityProcess;

	public Unity3dAttachConfiguration(Project project, ConfigurationFactory factory)
	{
		super(project, factory, "Unity Debug Attach");
	}

	public Unity3dAttachConfiguration(Project project, String name, ConfigurationFactory factory)
	{
		super(project, factory, name);
	}

	public void setAttachTarget(AttachTarget attachTarget)
	{
		myAttachTarget = attachTarget;
	}

	public AttachTarget getAttachTarget()
	{
		return myAttachTarget;
	}

	public void setProcessName(String processName)
	{
		myProcessName = processName;
	}

	public String getProcessName()
	{
		return myProcessName;
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException
	{
		super.readExternal(element);

		XmlSerializer.deserializeInto(this, element);
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException
	{
		super.writeExternal(element);

		XmlSerializer.serializeInto(this, element);
	}

	@Nonnull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
	{
		return new Unity3dConfigurationEditor();
	}

	@Nullable
	@Override
	public RunProfileState getState(@Nonnull Executor executor, @Nonnull ExecutionEnvironment env) throws ExecutionException
	{
		return (executor1, runner) -> new DefaultExecutionResult(null, new NopProcessHandler());
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public Module[] getModules()
	{
		return ModuleManager.getInstance(getProject()).getModules();
	}

	public UnityDebugProcessInfo getForceUnityProcess()
	{
		return myForceUnityProcess;
	}

	public void setForceUnityProcess(UnityDebugProcessInfo forceUnityProcess)
	{
		myForceUnityProcess = forceUnityProcess;
	}
}
