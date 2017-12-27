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

package consulo.unity3d.jsonApi;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
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
import consulo.buildInWebServer.api.JsonPostRequestHandler;
import consulo.unity3d.run.test.Unity3dTestSession;
import consulo.unity3d.run.test.Unity3dTestSessionManager;

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
				long time = (long) (request.time * 1000L);
				processor.onTestFinished(new TestFinishedEvent(name, time));
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
