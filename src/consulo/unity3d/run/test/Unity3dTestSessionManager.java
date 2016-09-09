package consulo.unity3d.run.test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import consulo.lombok.annotations.ApplicationService;

/**
 * @author VISTALL
 * @since 18.01.2016
 */
@ApplicationService
public class Unity3dTestSessionManager
{
	private Map<UUID, Unity3dTestSession> mySessions = new ConcurrentHashMap<UUID, Unity3dTestSession>();

	public UUID newSession(ProcessHandler processHandler, GeneralTestEventsProcessor processor)
	{
		UUID uuid = UUID.randomUUID();
		mySessions.put(uuid, new Unity3dTestSession(processHandler, processor));
		return uuid;
	}

	@Nullable
	public Unity3dTestSession findSession(UUID uuid)
	{
		return mySessions.get(uuid);
	}

	public void disposeSession(UUID uuid)
	{
		mySessions.remove(uuid);
	}
}
