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

package consulo.unity3d.run.debugger;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.ThreeState;
import com.intellij.util.concurrency.AppExecutorUtil;
import consulo.disposer.Disposable;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.run.debugger.android.UnityUdpExternalDeviceCollector;
import consulo.unity3d.run.debugger.iOS.IOSUnityExternalDeviceCollector;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 10.11.14
 */
@Singleton
public class UnityExternalDeviceManager implements Disposable
{
	@Nonnull
	public static UnityExternalDeviceManager getInstance()
	{
		return Application.get().getInstance(UnityExternalDeviceManager.class);
	}

	private final Map<UnityExternalDevice, UnityExternalDevice> myPlayers = new ConcurrentHashMap<>();

	private Future<?> myUpdateFuture;

	private ThreeState myBindState = ThreeState.NO;

	// FIXME [VISTALL] ep? but there no sense, due it will not never extended
	private final List<UnityExternalDeviceCollector> myDeviceCollectors = List.of(
			new UnityUdpExternalDeviceCollector()//,
			//new IOSUnityExternalDeviceCollector()
	);

	private void runUpdateTask()
	{
		myUpdateFuture = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() ->
		{
			for(Iterator<Map.Entry<UnityExternalDevice, UnityExternalDevice>> iterator = myPlayers.entrySet().iterator(); iterator.hasNext(); )
			{
				Map.Entry<UnityExternalDevice, UnityExternalDevice> next = iterator.next();

				if(!next.getKey().isAvailable())
				{
					iterator.remove();
				}
			}
		}, 5, 5, TimeUnit.SECONDS);
	}

	@RequiredUIAccess
	public void bindAndRun(@Nonnull Project project, @Nonnull Runnable runnable)
	{
		switch(myBindState)
		{
			case YES:
				runnable.run();
				break;
			case UNSURE:
				// nothing
				break;
			default:
				myBindState = ThreeState.UNSURE;

				new Task.Backgroundable(project, "Preparing...")
				{
					@Override
					public void run(@Nonnull ProgressIndicator progressIndicator)
					{
						for(UnityExternalDeviceCollector deviceCollector : myDeviceCollectors)
						{
							deviceCollector.initialize(project, progressIndicator, UnityExternalDeviceManager.this::addDevice);
						}
					}

					@RequiredUIAccess
					@Override
					public void onFinished()
					{
						myBindState = ThreeState.YES;

						runUpdateTask();

						runnable.run();
					}
				}.queue();
				break;
		}
	}

	@Override
	public void dispose()
	{
		if(myUpdateFuture != null)
		{
			myUpdateFuture.cancel(false);
		}

		for(UnityExternalDeviceCollector deviceCollector : myDeviceCollectors)
		{
			deviceCollector.dispose();
		}
	}

	@Nonnull
	public Collection<UnityExternalDevice> getDevices()
	{
		return myPlayers.values();
	}

	public void addDevice(@Nonnull UnityExternalDevice player)
	{
		UnityExternalDevice otherPlayer = myPlayers.get(player);
		if(otherPlayer != null)
		{
			otherPlayer.update();
		}
		else
		{
			myPlayers.put(player, player);
		}
	}
}
