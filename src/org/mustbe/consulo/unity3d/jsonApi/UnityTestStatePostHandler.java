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
import com.intellij.execution.testframework.sm.runner.events.TestIgnoredEvent;
import com.intellij.execution.testframework.sm.runner.events.TestOutputEvent;
import com.intellij.execution.testframework.sm.runner.events.TestStartedEvent;
import com.intellij.execution.testframework.sm.runner.events.TestSuiteFinishedEvent;
import com.intellij.execution.testframework.sm.runner.events.TestSuiteStartedEvent;
import com.intellij.openapi.util.text.StringUtil;

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
			case TestIgnored:
				processor.onTestIgnored(new TestIgnoredEvent(name, StringUtil.notNullize(request.message), request.stackTrace));
				break;
			case TestFailed:
				processor.onTestFailure(new TestFailedEvent(name, StringUtil.notNullize(request.message), request.stackTrace, false, null, null));
				break;
			case TestOutput:
				boolean stdOut = "Log".equals(request.messageType) || "Warning".equals(request.messageType);
				StringBuilder builder = new StringBuilder(request.message);
				if(!stdOut)
				{
					builder.append("\n");
					String[] strings = StringUtil.splitByLines(request.stackTrace);
					for(String line : strings)
					{
						builder.append("  at ").append(line).append("\n");
					}
				}
				processor.onTestOutput(new TestOutputEvent(name, builder.toString(), stdOut));
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
