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

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.execution.test.sm.runner.GeneralTestEventsProcessor;
import consulo.ide.ServiceManager;
import consulo.process.ProcessHandler;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 18.01.2016
 */
@Singleton
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class Unity3dTestSessionManager
{
	@Nonnull
	public static Unity3dTestSessionManager getInstance()
	{
		return ServiceManager.getService(Unity3dTestSessionManager.class);
	}

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
