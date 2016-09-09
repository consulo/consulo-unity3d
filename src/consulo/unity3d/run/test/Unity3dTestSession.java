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
