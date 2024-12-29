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

import consulo.application.ApplicationManager;
import consulo.disposer.Disposer;
import consulo.execution.DefaultExecutionResult;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.DefaultDebugProcessHandler;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.ProgramRunner;
import consulo.execution.test.TestConsoleProperties;
import consulo.execution.test.sm.SMTestRunnerConnectionUtil;
import consulo.execution.test.sm.runner.GeneralTestEventsProcessor;
import consulo.execution.test.sm.runner.GeneralToSMTRunnerEventsConvertor;
import consulo.execution.test.sm.runner.OutputToGeneralTestEventsConverter;
import consulo.execution.test.sm.runner.SMTRunnerConsoleProperties;
import consulo.execution.test.sm.ui.AttachToProcessListener;
import consulo.execution.test.sm.ui.SMTRunnerConsoleView;
import consulo.execution.test.sm.ui.SMTRunnerUIActionsHandler;
import consulo.execution.test.sm.ui.SMTestRunnerResultsForm;
import consulo.execution.test.sm.ui.statistic.StatisticsPanel;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessOutputTypes;
import consulo.process.event.ProcessAdapter;
import consulo.process.event.ProcessEvent;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.editor.UnityRunTest;
import consulo.util.dataholder.Key;
import consulo.util.lang.ref.SimpleReference;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 18.01.2016
 */
public class Unity3dTestRunState implements RunProfileState
{
	private static final String TEST_FRAMEWORK_NAME = "Unity Test";

	private final ExecutionEnvironment myEnvironment;

	public Unity3dTestRunState(ExecutionEnvironment environment)
	{
		myEnvironment = environment;
	}

	@Nullable
	@Override
	@RequiredUIAccess
	public ExecutionResult execute(Executor executor, @Nonnull ProgramRunner runner) throws ExecutionException
	{
		Unity3dTestConfiguration profile = (Unity3dTestConfiguration) myEnvironment.getRunProfile();

		TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(profile, TEST_FRAMEWORK_NAME, executor);

		testConsoleProperties.setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);

		String splitterPropertyName = SMTestRunnerConnectionUtil.getSplitterPropertyName(TEST_FRAMEWORK_NAME);

		final SMTRunnerConsoleView consoleView = new SMTRunnerConsoleView(testConsoleProperties, splitterPropertyName);

		final SimpleReference<UUID> ref = SimpleReference.create();

		consoleView.addAttachToProcessListener(new AttachToProcessListener()
		{
			@Override
			public void onAttachToProcess(@Nonnull ProcessHandler processHandler)
			{
				SMTestRunnerResultsForm resultsForm = consoleView.getResultsViewer();

				ref.set(attachEventsProcessors(consoleView.getProperties(), resultsForm, resultsForm.getStatisticsPane(), processHandler, TEST_FRAMEWORK_NAME));
			}
		});
		consoleView.setHelpId("reference.runToolWindow.testResultsTab");
		consoleView.initUI();

		final ProcessHandler osProcessHandler = new DefaultDebugProcessHandler();

		consoleView.attachToProcess(osProcessHandler);

		UnityRunTest runTest = new UnityRunTest();
		runTest.uuid = ref.get().toString();
		runTest.type = "";

		if(!UnityEditorCommunication.request(profile.getProject(), runTest, true))
		{
			ApplicationManager.getApplication().executeOnPooledThread(new Runnable()
			{
				@Override
				public void run()
				{
					osProcessHandler.notifyTextAvailable("UnityEditor dont received request, maybe is not run", ProcessOutputTypes.STDERR);
					osProcessHandler.destroyProcess();
				}
			});
		}

		return new DefaultExecutionResult(consoleView, osProcessHandler);
	}

	private static UUID attachEventsProcessors(@Nonnull final TestConsoleProperties consoleProperties,
			final SMTestRunnerResultsForm resultsViewer,
			final StatisticsPanel statisticsPane,
			final ProcessHandler processHandler,
			@Nonnull final String testFrameworkName)
	{
		//build messages consumer
		final OutputToGeneralTestEventsConverter outputConsumer;
		outputConsumer = new OutputToGeneralTestEventsConverter(testFrameworkName, consoleProperties);

		//events processor
		final GeneralTestEventsProcessor eventsProcessor = new GeneralToSMTRunnerEventsConvertor(consoleProperties.getProject(), resultsViewer.getTestsRootNode(), testFrameworkName);

		final UUID uuid = Unity3dTestSessionManager.getInstance().newSession(processHandler, eventsProcessor);

		// ui actions
		final SMTRunnerUIActionsHandler uiActionsHandler = new SMTRunnerUIActionsHandler(consoleProperties);

		// subscribes event processor on output consumer events
		outputConsumer.setProcessor(eventsProcessor);
		// subscribes result viewer on event processor
		eventsProcessor.addEventsListener(resultsViewer);
		// subscribes test runner's actions on results viewer events
		resultsViewer.addEventsListener(uiActionsHandler);
		// subscribes statistics tab viewer on event processor
		eventsProcessor.addEventsListener(statisticsPane.createTestEventsListener());

		processHandler.addProcessListener(new ProcessAdapter()
		{
			@Override
			public void processTerminated(final ProcessEvent event)
			{
				outputConsumer.flushBufferBeforeTerminating();

				eventsProcessor.onFinishTesting();

				Unity3dTestSessionManager.getInstance().disposeSession(uuid);

				Disposer.dispose(eventsProcessor);
				Disposer.dispose(outputConsumer);
			}

			@Override
			public void onTextAvailable(final ProcessEvent event, final Key outputType)
			{
				outputConsumer.process(event.getText(), outputType);
			}
		});
		return uuid;
	}
}
