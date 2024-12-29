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

package consulo.unity3d.run.debugger.android;

import consulo.unity3d.run.debugger.UnityExternalDevice;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class UnityUdpListeningThread extends Thread
{
	private static final ThreadGroup ourThreadGroup = new ThreadGroup("Unity Player Service Thread Group");
	private final Consumer<UnityExternalDevice> myPlayerConsumer;
	private final MulticastSocket myServerSocket;
	private boolean myFinished;

	public UnityUdpListeningThread(Consumer<UnityExternalDevice> playerConsumer, MulticastSocket serverSocket, int port, NetworkInterface networkInterface)
	{
		super(ourThreadGroup, String.format("Port: %d, NetworkInterface: %s", port, networkInterface.getDisplayName()));
		myServerSocket = serverSocket;
		setPriority(MIN_PRIORITY);
		myPlayerConsumer = playerConsumer;
	}

	@Override
	public void run()
	{
		byte[] receiveData = new byte[1024];
		while(!myFinished)
		{
			try
			{
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

				myServerSocket.receive(receivePacket);

				byte[] data = new byte[receivePacket.getLength()];

				System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, data.length);

				String sentence = new String(data);

				Map<String, String> map = parseValues(sentence);
				if(map.isEmpty())
				{
					return;
				}

				UnityByUdpPlayer player = new UnityByUdpPlayer(receivePacket.getAddress(), map);

				myPlayerConsumer.accept(player);
			}
			catch(IOException ignored)
			{
			}
			finally
			{
				try
				{
					Thread.sleep(1000L);
				}
				catch(InterruptedException ignored)
				{
				}
			}
		}
	}

	@Nonnull
	private Map<String, String> parseValues(String playerString)
	{
		if(playerString.length() == 0)
		{
			return Map.of();
		}

		if(playerString.charAt(playerString.length() - 1) == '\u0000')
		{
			playerString = playerString.substring(0, playerString.length() - 1);
		}

		int partStart = 0;
		int partLen = 0;
		List<String> strings = new ArrayList<>();
		for(int i = 0; i < playerString.length(); i++)
		{
			char c = playerString.charAt(i);
			if(c == '[')
			{
				partLen = i - partStart;
				if(partLen > 0)
				{
					strings.add(playerString.substring(partStart, partStart + partLen));
				}

				partStart = i + 1;
			}
			else if(c == ']')
			{
				partLen = i - partStart;
				if(partLen > 0)
				{
					strings.add(playerString.substring(partStart, partStart + partLen));
				}

				partStart = i + 1;
			}
		}

		partLen = playerString.length() - partStart;
		if(partLen > 0)
		{
			strings.add(playerString.substring(partStart, playerString.length()));
		}

		Map<String, String> map = new HashMap<>();

		for(int i = 0; i < strings.size(); i += 2)
		{
			map.put(strings.get(i).toLowerCase(Locale.US), strings.get(i + 1).trim());
		}
		return map;
	}

	public void dispose()
	{
		myFinished = true;
		myServerSocket.close();
	}
}
