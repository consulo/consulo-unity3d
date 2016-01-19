package org.mustbe.consulo.unity3d.run;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.editor.UnityEditorCommunication;
import org.mustbe.consulo.unity3d.editor.UnityRunTest;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import com.intellij.execution.testframework.sm.runner.GeneralToSMTRunnerEventsConvertor;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.ui.AttachToProcessListener;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerUIActionsHandler;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.execution.testframework.sm.runner.ui.statistics.StatisticsPanel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.xdebugger.DefaultDebugProcessHandler;

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
	public ExecutionResult execute(Executor executor, @NotNull ProgramRunner runner) throws ExecutionException
	{
		Unity3dTestConfiguration profile = (Unity3dTestConfiguration) myEnvironment.getRunProfile();

		TestConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(profile, TEST_FRAMEWORK_NAME, executor);

		testConsoleProperties.setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);

		String splitterPropertyName = SMTestRunnerConnectionUtil.getSplitterPropertyName(TEST_FRAMEWORK_NAME);

		final SMTRunnerConsoleView consoleView = new SMTRunnerConsoleView(testConsoleProperties, splitterPropertyName);

		final Ref<UUID> ref = Ref.create();

		consoleView.addAttachToProcessListener(new AttachToProcessListener()
		{
			@Override
			public void onAttachToProcess(@NotNull ProcessHandler processHandler)
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
		runTest.type = profile.getTypeVmQName();

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

	private static UUID attachEventsProcessors(@NotNull final TestConsoleProperties consoleProperties,
			final SMTestRunnerResultsForm resultsViewer,
			final StatisticsPanel statisticsPane,
			final ProcessHandler processHandler,
			@NotNull final String testFrameworkName)
	{
		//build messages consumer
		final OutputToGeneralTestEventsConverter outputConsumer;
		outputConsumer = new OutputToGeneralTestEventsConverter(testFrameworkName, consoleProperties);

		//events processor
		final GeneralTestEventsProcessor eventsProcessor = new GeneralToSMTRunnerEventsConvertor(consoleProperties.getProject(), resultsViewer.getTestsRootNode(), testFrameworkName);

		final UUID uuid = Unity3dTestSessionManager.getInstance(consoleProperties.getProject()).newSession(eventsProcessor);

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

				Unity3dTestSessionManager.getInstance(consoleProperties.getProject()).disposeSession(uuid);

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
