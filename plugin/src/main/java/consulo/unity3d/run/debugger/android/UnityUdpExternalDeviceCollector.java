package consulo.unity3d.run.debugger.android;

import consulo.application.progress.ProgressIndicator;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.unity3d.run.debugger.UnityExternalDevice;
import consulo.unity3d.run.debugger.UnityExternalDeviceCollector;

import jakarta.annotation.Nonnull;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 27/01/2021
 */
public class UnityUdpExternalDeviceCollector implements UnityExternalDeviceCollector
{
	private static final Logger LOG = Logger.getInstance(UnityUdpExternalDeviceCollector.class);

	private static final int[] ourPorts = {
			54997,
			34997,
			57997,
			58997
	};

	private static final String ourUdpGroupIp = "225.0.0.222";

	private final List<UnityUdpListeningThread> myThreads = new ArrayList<>();

	@Override
	public void initialize(Project project, @Nonnull ProgressIndicator indicator, @Nonnull Consumer<UnityExternalDevice> consumer)
	{
		indicator.setText("Preparing Network Listeners...");

		bind(indicator, consumer);
	}

	private void bind(@Nonnull ProgressIndicator progressIndicator, Consumer<UnityExternalDevice> consumer)
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

						UnityUdpListeningThread udpThread = new UnityUdpListeningThread(consumer, serverSocket, playerMulticastPort, networkInterface);
						udpThread.start();
						myThreads.add(udpThread);

						succBinds++;

						progressIndicator.setText2("Binding " + networkInterface + ", port: " + playerMulticastPort);

						LOG.info("Successfully binding network interface " + networkInterface + ", port: " + playerMulticastPort);
					}
					catch(Exception e)
					{
						failBinds++;
						LOG.warn(e);
					}
				}
			}
			LOG.info("Port status: " + succBinds + " vs " + failBinds);
		}
		catch(Exception e)
		{
			LOG.error(e);
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
		for(UnityUdpListeningThread thread : myThreads)
		{
			thread.dispose();
		}
	}
}
