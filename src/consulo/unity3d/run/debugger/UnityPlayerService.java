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


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.concurrency.AppExecutorUtil;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class UnityPlayerService implements ApplicationComponent
{
	private static final Logger LOGGER = Logger.getInstance(UnityPlayerService.class);

	@NotNull
	public static UnityPlayerService getInstance()
	{
		return ApplicationManager.getApplication().getComponent(UnityPlayerService.class);
	}

	private static final int[] ourPorts = {
			54997,
			34997,
			57997,
			58997
	};

	private static final String ourUdpGroupIp = "225.0.0.222";

	private List<UnityUdpThread> myThreads = new ArrayList<>();

	private ConcurrentMap<UnityPlayer, UnityPlayer> myPlayers = new ConcurrentHashMap<>();

	private Future<?> myUpdateFuture;

	@Override
	public void initComponent()
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
						UnityPlayerService.LOGGER.info("Successfully binding network interface " + networkInterface + ", port: " + playerMulticastPort);
					}
					catch(Exception e)
					{
						failBinds++;
						UnityPlayerService.LOGGER.warn(e);
					}
				}
			}
			LOGGER.info("Port status: " + succBinds + " vs " + failBinds);

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
		catch(Exception e)
		{
			Messages.showErrorDialog("Some problem with Unity debugger. Exception: " + ExceptionUtil.getThrowableText(e), "Consulo");
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
	public void disposeComponent()
	{
		myUpdateFuture.cancel(false);

		for(UnityUdpThread thread : myThreads)
		{
			thread.dispose();
		}
	}

	@NotNull
	@Override
	public String getComponentName()
	{
		return "PlayerListenerService";
	}

	@NotNull
	public Collection<UnityPlayer> getPlayers()
	{
		return myPlayers.values();
	}

	public void addPlayer(@NotNull UnityPlayer player)
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
