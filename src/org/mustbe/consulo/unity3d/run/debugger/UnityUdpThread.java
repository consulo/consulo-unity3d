/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.unity3d.run.debugger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class UnityUdpThread extends Thread
{
	private static final Pattern ourPacketPattern = Pattern.compile("\\[IP\\] (?<ip>.*) \\[Port\\] (?<port>.*) \\[Flags\\] (?<flags>.*)" +
			" \\[Guid\\] (?<guid>.*) \\[EditorId\\] (?<editorid>.*) \\[Version\\] (?<version>.*)" +
			" \\[Id\\] (?<id>[^:]+)(:(?<debuggerPort>\\d+))? \\[Debug\\] (?<debug>.*)");

	private static final ThreadGroup ourThreadGroup = new ThreadGroup("Unity Player Service Thread Group");
	private final UnityPlayerService myService;
	private final MulticastSocket myServerSocket;
	private boolean myFinished;

	public UnityUdpThread(UnityPlayerService service, MulticastSocket serverSocket, int port, NetworkInterface networkInterface)
	{
		super(ourThreadGroup, String.format("Port: %d, NetworkInterface: %s", port, networkInterface.getDisplayName()));
		myServerSocket = serverSocket;
		setPriority(MIN_PRIORITY);
		myService = service;
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
				String sentence = new String(receivePacket.getData());

				Matcher matcher = ourPacketPattern.matcher(sentence);
				if(!matcher.find())
				{
					continue;
				}
				UnityPlayer player = new UnityPlayer(matcher);
				myService.addPlayer(player);
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

	public void dispose()
	{
		myFinished = true;
		myServerSocket.close();
	}
}
