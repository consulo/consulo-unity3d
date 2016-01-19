package org.mustbe.consulo.unity3d.jsonApi;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.mustbe.buildInWebServer.api.JsonPostRequestHandler;
import org.mustbe.consulo.unity3d.run.Unity3dTestSessionManager;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
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
		System.out.println(request);
		UUID uuid = UUID.fromString(request.uuid);
		GeneralTestEventsProcessor processor = Unity3dTestSessionManager.getInstance().getProcessor(uuid);
		if(processor == null)
		{
			return JsonResponse.asError("no session");
		}

		if(request.suite)
		{
			if(request.state)
			{
				processor.onSuiteStarted(new TestSuiteStartedEvent(request.name, null));
			}
			else
			{
				processor.onSuiteFinished(new TestSuiteFinishedEvent(request.name));
			}
		}
		else
		{
			if(request.state)
			{
				processor.onTestStarted(new TestStartedEvent(request.name, null));
			}
			else
			{
				processor.onTestFinished(new TestFinishedEvent(request.name, 0L));
			}
		}
		return JsonResponse.asSuccess(null);
	}
}
