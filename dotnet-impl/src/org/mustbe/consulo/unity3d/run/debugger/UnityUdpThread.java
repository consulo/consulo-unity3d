package org.mustbe.consulo.unity3d.run.debugger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;


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
