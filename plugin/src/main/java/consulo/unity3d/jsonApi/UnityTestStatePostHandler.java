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

import consulo.annotation.component.ExtensionImpl;
import consulo.builtinWebServer.json.JsonPostRequestHandler;
import consulo.execution.test.sm.runner.GeneralTestEventsProcessor;
import consulo.execution.test.sm.runner.event.*;
import consulo.process.ProcessHandler;
import consulo.unity3d.run.test.Unity3dTestSession;
import consulo.unity3d.run.test.Unity3dTestSessionManager;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import java.util.UUID;

/**
 * @author VISTALL
 * @since 19.01.2016
 */
@ExtensionImpl
public class UnityTestStatePostHandler extends JsonPostRequestHandler<UnityTestStatePostRequest>
{
	public UnityTestStatePostHandler()
	{
		super("unityTestState", UnityTestStatePostRequest.class);
	}

	@Nonnull
	@Override
	public JsonResponse handle(@Nonnull UnityTestStatePostRequest request)
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
