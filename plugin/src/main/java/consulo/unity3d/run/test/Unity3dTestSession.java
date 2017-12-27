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

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;

/**
 * @author VISTALL
 * @since 21.01.2016
 */
public class Unity3dTestSession
{
	private GeneralTestEventsProcessor myProcessor;
	private ProcessHandler myProcessHandler;

	public Unity3dTestSession(ProcessHandler processHandler, GeneralTestEventsProcessor processor)
	{
		myProcessor = processor;
		myProcessHandler = processHandler;
	}

	public GeneralTestEventsProcessor getProcessor()
	{
		return myProcessor;
	}

	public ProcessHandler getProcessHandler()
	{
		return myProcessHandler;
	}
}
