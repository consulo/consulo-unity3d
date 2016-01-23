package org.mustbe.consulo.unity3d.jsonApi;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.mustbe.buildInWebServer.api.JsonPostRequestHandler;
import org.mustbe.consulo.unity3d.run.test.Unity3dTestSession;
import org.mustbe.consulo.unity3d.run.test.Unity3dTestSessionManager;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import com.intellij.execution.testframework.sm.runner.events.TestFailedEvent;
import com.intellij.execution.testframework.sm.runner.events.TestFinishedEvent;
import com.intellij.execution.testframework.sm.runner.events.TestStartedEvent;
import com.intellij.execution.testframework.sm.runner.events.TestSuiteFinishedEvent;
import com.intellij.execution.testframework.sm.runner.events.TestSuiteStartedEvent;

/**
 * @author VISTALL
 * @since 19.01.2016
 */
public class UnityTestStatePostHandler extends JsonPostRequestHandler<UnityTestStatePostRequest>
{
	protected UnityTestStatePostHandler()
	{
		super("unityTestState", UnityTestStatePostRequest.class);
	}

	@NotNull
	@Override
	public JsonResponse handle(@NotNull UnityTestStatePostRequest request)
	{
		UUID uuid = UUID.fromString(request.uuid);
		Unity3dTestSession session = Unity3dTestSessionManager.getInstance().findSession(uuid);
		if(session == null)
		{
			return JsonResponse.asError("no session");
		}

		GeneralTestEventsProcessor processor = session.getProcessor();
		ProcessHandler processHandler = session.getProcessHandler();

		String name = request.name;
		switch(request.type)
		{
			case TestStarted:
				processor.onTestStarted(new TestStartedEvent(name, null));
				break;
			case TestFailed:
				processor.onTestFailure(new TestFailedEvent(name, "", null, false, null, null));
				break;
			case TestFinished:
				processor.onTestFinished(new TestFinishedEvent(name, 0L));
				break;
			case SuiteStarted:
				processor.onSuiteStarted(new TestSuiteStartedEvent(name, null));
				break;
			case SuiteFinished:
				processor.onSuiteFinished(new TestSuiteFinishedEvent(name));
				break;
			case RunFinished:
				processor.onFinishTesting();
				processHandler.destroyProcess();
				break;
		}

		return JsonResponse.asSuccess(null);
	}
}
