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
import consulo.csharp.lang.CSharpFileType;
import consulo.dotnet.debugger.impl.DotNetConfurationWithDefaultDebugFileType;
import consulo.execution.DefaultExecutionResult;
import consulo.execution.configuration.*;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.file.LanguageFileType;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.process.ExecutionException;
import consulo.process.NopProcessHandler;
import consulo.project.Project;
import consulo.unity3d.run.debugger.UnityDebugProcessInfo;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class Unity3dAttachConfiguration extends LocatableConfigurationBase implements
    ModuleRunProfile,
    RunConfigurationWithSuppressedDefaultRunAction,
    CompileStepBeforeRun.Suppressor,
    DotNetConfurationWithDefaultDebugFileType {
    public enum AttachTarget {
        UNITY_EDITOR,
        BY_NAME,
        FROM_DIALOG
    }

    // for attaching from attach action
    @Nullable
    private UnityDebugProcessInfo myForceUnityProcess;

    public Unity3dAttachConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory, "Unity Debug Attach");
    }

    public Unity3dAttachConfiguration(Project project, String name, ConfigurationFactory factory) {
        super(project, factory, name);
    }

    @Override
    protected Class<Unity3dAttachConfigurationOptions> getOptionsClass() {
        return Unity3dAttachConfigurationOptions.class;
    }

    @Override
    public Unity3dAttachConfigurationOptions getOptions() {
        return (Unity3dAttachConfigurationOptions) super.getOptions();
    }

    @Override
    protected void convertLegacyState(LegacyRunConfigurationConverter converter) {
        super.convertLegacyState(converter);

        Unity3dAttachConfigurationLegacyState old = converter.readXml(Unity3dAttachConfigurationLegacyState.class);

        Unity3dAttachConfigurationOptions options = new Unity3dAttachConfigurationOptions();
        AttachTarget attachTarget = old.getAttachTarget();
        options.setAttachTarget(attachTarget == null || attachTarget == AttachTarget.UNITY_EDITOR ? null : attachTarget.name());
        options.setProcessName(old.getProcessName());
        converter.setOptions(options);
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        return List.of(new Unity3dAttachTargetFragment());
    }

    public void setAttachTarget(AttachTarget attachTarget) {
        getOptions().setAttachTarget(attachTarget == AttachTarget.UNITY_EDITOR ? null : attachTarget.name());
    }

    public AttachTarget getAttachTarget() {
        String name = getOptions().getAttachTarget();
        if (name != null) {
            for (AttachTarget target : AttachTarget.values()) {
                if (target.name().equals(name)) {
                    return target;
                }
            }
        }
        return AttachTarget.UNITY_EDITOR;
    }

    public void setProcessName(@Nullable String processName) {
        getOptions().setProcessName(processName);
    }

    @Nullable
    public String getProcessName() {
        return getOptions().getProcessName();
    }

    @Nonnull
    @Override
    public LanguageFileType getDefaultDebuggerFileType() {
        return CSharpFileType.INSTANCE;
    }

    @Nullable
    @Override
    public RunProfileState getState(@Nonnull Executor executor, @Nonnull ExecutionEnvironment env) throws ExecutionException {
        return (executor1, runner) -> new DefaultExecutionResult(null, new NopProcessHandler());
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public Module[] getModules() {
        return ModuleManager.getInstance(getProject()).getModules();
    }

    public UnityDebugProcessInfo getForceUnityProcess() {
        return myForceUnityProcess;
    }

    public void setForceUnityProcess(UnityDebugProcessInfo forceUnityProcess) {
        myForceUnityProcess = forceUnityProcess;
    }
}
