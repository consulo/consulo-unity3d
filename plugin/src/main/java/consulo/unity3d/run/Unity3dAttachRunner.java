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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.document.FileDocumentManager;
import consulo.dotnet.mono.debugger.MonoVirtualMachineListener;
import consulo.dotnet.util.DebugConnectionInfo;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.XDebuggerManager;
import consulo.execution.runner.AsyncProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.RunContentDescriptor;
import consulo.execution.ui.console.ConsoleView;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerStopper;
import consulo.process.ProcessOutputTypes;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.run.debugger.UnityDebugProcess;
import consulo.unity3d.run.debugger.UnityDebugProcessInfo;
import consulo.unity3d.run.debugger.UnityExternalDeviceManager;
import consulo.unity3d.run.debugger.UnityProcessDialog;
import consulo.util.collection.ContainerUtil;
import consulo.util.concurrent.AsyncResult;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import mono.debugger.VirtualMachine;

import java.util.List;

/**
 * @author VISTALL
 * @since 10.11.14
 */
@ExtensionImpl
public class Unity3dAttachRunner extends AsyncProgramRunner {
    @Nonnull
    @Override
    public String getRunnerId() {
        return "Unity3dAttachRunner";
    }

    @Override
    public boolean canRun(@Nonnull String executorId, @Nonnull RunProfile profile) {
        return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) && profile instanceof Unity3dAttachConfiguration;
    }

    @Nonnull
    @RequiredUIAccess
    public static RunContentDescriptor runContentDescriptor(ExecutionResult executionResult,
                                                            @Nonnull final ExecutionEnvironment environment,
                                                            @Nonnull UnityDebugProcessInfo selected,
                                                            @Nullable final ConsoleView consoleView,
                                                            boolean insideEditor) throws ExecutionException {
        final XDebugSession debugSession = XDebuggerManager.getInstance(environment.getProject()).startSession(environment, session ->
        {
            DebugConnectionInfo debugConnectionInfo = new DebugConnectionInfo(selected.getHost(), selected.getPort(), true);
            final UnityDebugProcess process = new UnityDebugProcess(session, environment.getRunProfile(), debugConnectionInfo, consoleView, insideEditor);
            process.setExecutionResult(executionResult);

            process.getDebugThread().addListener(new MonoVirtualMachineListener() {
                private boolean myDisconnected = false;

                @Override
                public void connectionSuccess(@Nonnull VirtualMachine machine) {
                    ProcessHandler processHandler = process.getProcessHandler();
                    processHandler.notifyTextAvailable(String.format("Success attach to '%s' at %s:%d\n", selected.getName(), selected.getHost(), selected.getPort()), ProcessOutputTypes.STDOUT);
                }

                @Override
                public void connectionStopped() {
                    if (myDisconnected) {
                        return;
                    }

                    myDisconnected = true;
                    ProcessHandler processHandler = process.getProcessHandler();
                    processHandler.notifyTextAvailable(String.format("Disconnected from '%s' at %s:%d\n", selected.getName(), selected.getHost(), selected.getPort()), ProcessOutputTypes.STDERR);
                    ProcessHandlerStopper.stop(processHandler);
                }

                @Override
                public void connectionFailed() {
                    ProcessHandler processHandler = process.getProcessHandler();
                    processHandler.notifyTextAvailable(String.format("Failed attach to '%s' at %s:%d\n", selected.getName(), selected.getHost(), selected.getPort()), ProcessOutputTypes.STDERR);
                    ProcessHandlerStopper.stop(processHandler);
                }
            });
            process.start();
            return process;
        });

        return debugSession.getRunContentDescriptor();
    }

    @Nonnull
    @Override
    @RequiredUIAccess
    protected AsyncResult<RunContentDescriptor> execute(@Nonnull ExecutionEnvironment environment, @Nonnull RunProfileState state) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();

        AsyncResult<RunContentDescriptor> result = AsyncResult.undefined();

        Unity3dAttachConfiguration runProfile = (Unity3dAttachConfiguration) environment.getRunProfile();

        ExecutionResult executionResult = state.execute(environment.getExecutor(), this);

        UnityDebugProcessInfo forceUnityProcess = runProfile.getForceUnityProcess();
        if (forceUnityProcess != null) {
            setRunDescriptor(result, environment, executionResult, forceUnityProcess);
            return result;
        }

        switch (runProfile.getAttachTarget()) {
            case UNITY_EDITOR:
                new Task.Backgroundable(environment.getProject(), "Searching Unity Editor...", false) {
                    private UnityDebugProcessInfo myUnityProcess;

                    @Override
                    public void run(@Nonnull ProgressIndicator progressIndicator) {
                        myUnityProcess = UnityEditorCommunication.findEditorProcess();
                    }

                    @RequiredUIAccess
                    @Override
                    public void onSuccess() {
                        if (myUnityProcess != null) {
                            try {
                                result.setDone(runContentDescriptor(executionResult, environment, myUnityProcess, null, true));
                            }
                            catch (ExecutionException e) {
                                result.rejectWithThrowable(e);
                            }
                        }
                        else {
                            result.rejectWithThrowable(new ExecutionException("Unity Editor is not running"));
                        }
                    }
                }.queue();
                return result;
            case BY_NAME:
                UnityExternalDeviceManager.getInstance().bindAndRun(environment.getProject(), () ->
                {
                    new Task.Backgroundable(environment.getProject(), "Searching process by name: " + runProfile.getProcessName()) {
                        private UnityDebugProcessInfo myUnityProcess;

                        @Override
                        public void run(@Nonnull ProgressIndicator progressIndicator) {
                            for (UnityDebugProcessInfo unityProcess : UnityProcessDialog.collectItems()) {
                                if (StringUtil.isEmpty(runProfile.getProcessName()) || Comparing.equal(unityProcess.getName(), runProfile.getProcessName())) {
                                    myUnityProcess = unityProcess;
                                    break;
                                }
                            }
                        }

                        @RequiredUIAccess
                        @Override
                        public void onFinished() {
                            setRunDescriptor(result, environment, executionResult, myUnityProcess);
                        }
                    }.queue();
                });
                break;
            case FROM_DIALOG:
                UnityExternalDeviceManager.getInstance().bindAndRun(environment.getProject(), () ->
                {
                    UnityProcessDialog dialog = new UnityProcessDialog(environment.getProject());

                    List<UnityDebugProcessInfo> unityProcesses = dialog.showAndGetResult();

                    UnityDebugProcessInfo process = ContainerUtil.getFirstItem(unityProcesses);
                    if (process == null) {
                        result.setDone(null);
                        return;
                    }
                    setRunDescriptor(result, environment, executionResult, process);
                });
                break;
        }
        return result;
    }

    @RequiredUIAccess
    private static void setRunDescriptor(AsyncResult<RunContentDescriptor> result,
                                         ExecutionEnvironment environment,
                                         ExecutionResult executionResult,
                                         @Nullable UnityDebugProcessInfo process) {
        if (process == null) {
            result.rejectWithThrowable(new ExecutionException("Process not find for attach"));
            return;
        }

        try {
            result.setDone(runContentDescriptor(executionResult, environment, process, null, true));
        }
        catch (ExecutionException e) {
            result.rejectWithThrowable(e);
        }
    }
}
