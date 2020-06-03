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


import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.ThreeState;
import com.intellij.util.concurrency.AppExecutorUtil;
import consulo.disposer.Disposable;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 10.11.14
 */
@Singleton
public class UnityPlayerService implements Disposable
{
	private static final Logger LOGGER = Logger.getInstance(UnityPlayerService.class);

	@Nonnull
	public static UnityPlayerService getInstance()
	{
		return ServiceManager.getService(UnityPlayerService.class);
	}

	private static final int[] ourPorts = {
			54997,
			34997,
			57997,
			58997
	};

	private static final String ourUdpGroupIp = "225.0.0.222";

	private final List<UnityUdpThread> myThreads = new ArrayList<>();

	private final Map<UnityPlayer, UnityPlayer> myPlayers = new ConcurrentHashMap<>();

	private Future<?> myUpdateFuture;

	private ThreeState myBindState = ThreeState.NO;

	private void runUpdateTask()
	{
		myUpdateFuture = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay((Runnable) () ->
		{
			for(Iterator<Map.Entry<UnityPlayer, UnityPlayer>> iterator = myPlayers.entrySet().iterator(); iterator.hasNext(); )
			{
				Map.Entry<UnityPlayer, UnityPlayer> next = iterator.next();

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

				new Task.Backgroundable(project, "Preparing network listeners...")
				{
					@Override
					public void run(@Nonnull ProgressIndicator progressIndicator)
					{
						bind(progressIndicator);
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

	private void bind(@Nonnull ProgressIndicator progressIndicator)
	{
		try
		{
			int succBinds = 0;
			int failBinds = 0;
			InetAddress groupAddress = InetAddress.getByName(ourUdpGroupIp);

			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while(networkInterfaces.hasMoreElements())
			{
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				if(!haveIp4Address(networkInterface))
				{
					continue;
				}

				for(int playerMulticastPort : ourPorts)
				{
					final MulticastSocket serverSocket;
					try
					{
						serverSocket = new MulticastSocket(playerMulticastPort);
						serverSocket.setReuseAddress(true);
						serverSocket.setBroadcast(true);
						serverSocket.setNetworkInterface(networkInterface);
						serverSocket.joinGroup(groupAddress);

						UnityUdpThread udpThread = new UnityUdpThread(this, serverSocket, playerMulticastPort, networkInterface);
						udpThread.start();
						myThreads.add(udpThread);

						succBinds++;

						progressIndicator.setText2("Binding " + networkInterface + ", port: " + playerMulticastPort);

						LOGGER.info("Successfully binding network interface " + networkInterface + ", port: " + playerMulticastPort);
					}
					catch(Exception e)
					{
						failBinds++;
						LOGGER.warn(e);
					}
				}
			}
			LOGGER.info("Port status: " + succBinds + " vs " + failBinds);
		}
		catch(Exception e)
		{
			LOGGER.error(e);
		}
	}

	private boolean haveIp4Address(NetworkInterface networkInterface)
	{
		Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
		while(inetAddresses.hasMoreElements())
		{
			InetAddress address = inetAddresses.nextElement();
			if(address instanceof Inet4Address)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void dispose()
	{
		if(myUpdateFuture != null)
		{
			myUpdateFuture.cancel(false);
		}

		for(UnityUdpThread thread : myThreads)
		{
			thread.dispose();
		}
	}

	@Nonnull
	public Collection<UnityPlayer> getPlayers()
	{
		return myPlayers.values();
	}

	public void addPlayer(@Nonnull UnityPlayer player)
	{
		UnityPlayer otherPlayer = myPlayers.get(player);
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
