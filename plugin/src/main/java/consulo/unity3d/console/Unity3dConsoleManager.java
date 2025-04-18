/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.console;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.Application;
import consulo.application.util.concurrent.AppExecutorUtil;
import consulo.disposer.Disposable;
import consulo.project.Project;
import consulo.unity3d.jsonApi.UnityLogHandler;
import consulo.unity3d.jsonApi.UnityLogPostHandlerRequest;
import consulo.unity3d.util.Unity3dProjectUtil;
import consulo.util.collection.MultiMap;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 03-Nov-17
 */
@Singleton
@ServiceAPI(value = ComponentScope.APPLICATION, lazy = false)
@ServiceImpl
public class Unity3dConsoleManager implements Disposable
{
	@Nonnull
	public static Unity3dConsoleManager getInstance()
	{
		return Application.get().getComponent(Unity3dConsoleManager.class);
	}

	private final MultiMap<Project, Consumer<Collection<UnityLogPostHandlerRequest>>> myMap = MultiMap.createConcurrentSet();
	private final Deque<UnityLogPostHandlerRequest> myMessages = new ConcurrentLinkedDeque<>();

	private Future<?> myProcessingTask;

	@Inject
	public Unity3dConsoleManager(Application application)
	{
		myProcessingTask = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(this::pop, 2, 2, TimeUnit.SECONDS);
		application.getMessageBus().connect().subscribe(UnityLogHandler.class, myMessages::add);
	}

	private void pop()
	{
		MultiMap<String, UnityLogPostHandlerRequest> map = MultiMap.empty();
		while(!myMessages.isEmpty())
		{
			UnityLogPostHandlerRequest request = myMessages.pollFirst();
			if(request == null)
			{
				continue;
			}

			if(map.isEmpty())
			{
				map = MultiMap.createLinkedSet();
			}

			map.putValue(request.projectPath, request);
		}

		if(map.isEmpty())
		{
			return;
		}

		for(Map.Entry<String, Collection<UnityLogPostHandlerRequest>> entry : map.entrySet())
		{
			Project projectByPath = Unity3dProjectUtil.findProjectByPath(entry.getKey());
			if(projectByPath == null)
			{
				continue;
			}

			Collection<Consumer<Collection<UnityLogPostHandlerRequest>>> consumers = myMap.get(projectByPath);
			for(Consumer<Collection<UnityLogPostHandlerRequest>> consumer : consumers)
			{
				consumer.accept(entry.getValue());
			}
		}
	}

	@Nonnull
	public Disposable registerProcessor(@Nonnull Project project, @Nonnull Consumer<Collection<UnityLogPostHandlerRequest>> consumer)
	{
		myMap.putValue(project, consumer);
		return () -> myMap.remove(project, consumer);
	}

	@Override
	public void dispose()
	{
		myMap.clear();

		if(myProcessingTask != null)
		{
			myProcessingTask.cancel(false);
		}
	}
}
