package org.mustbe.consulo.unity3d.run;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.consulo.lombok.annotations.ProjectService;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;

/**
 * @author VISTALL
 * @since 18.01.2016
 */
@ProjectService
public class Unity3dTestSessionManager
{
	private Map<UUID, GeneralTestEventsProcessor> mySessions = new ConcurrentHashMap<UUID, GeneralTestEventsProcessor>();

	public UUID newSession(GeneralTestEventsProcessor processor)
	{
		UUID uuid = UUID.randomUUID();
		mySessions.put(uuid, processor);
		return uuid;
	}

	public void disposeSession(UUID uuid)
	{
		mySessions.remove(uuid);
	}
}
