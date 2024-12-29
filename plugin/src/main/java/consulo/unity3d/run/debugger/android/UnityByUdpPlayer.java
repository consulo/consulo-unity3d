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

import consulo.unity3d.run.debugger.BaseUnityExternalDevice;
import consulo.unity3d.run.debugger.UnityDebugProcessInfo;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.net.InetAddress;
import java.util.Map;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class UnityByUdpPlayer extends BaseUnityExternalDevice
{
	private final String myIp;
	private final long myGuid;
	private final String myId;
	private final boolean mySupportDebugging;
	private int myDebuggerPort;

	public UnityByUdpPlayer(@Nonnull InetAddress address, @Nonnull Map<String, String> map)
	{
		// ip + port is not target for connect
		myIp = address.getHostAddress();
		myId = map.get("id");
		myGuid = Long.parseLong(map.get("guid"));
		mySupportDebugging = "1".equals(StringUtil.notNullize(map.get("debug")).trim());
		if(mySupportDebugging)
		{
			String packagename = StringUtil.notNullize(map.get("packagename"));

			if("iPhonePlayer".equals(packagename))
			{
				myDebuggerPort = 56000;
			}
			else
			{
				String debuggerPort = map.get("debuggerPort");
				if(debuggerPort == null)
				{
					myDebuggerPort = 56000 + (int) (myGuid % 1000);
				}
				else
				{
					myDebuggerPort = Integer.parseInt(debuggerPort);
				}
			}
		}
		update();
	}

	@Nullable
	@Override
	public UnityDebugProcessInfo mapToDebuggerProcess()
	{
		if(isSupportDebugging())
		{
			return new UnityDebugProcessInfo((int) getGuid(), getId(), getIp(), getDebuggerPort());
		}

		return null;
	}

	public String getId()
	{
		return myId;
	}

	public String getIp()
	{
		return myIp;
	}

	public long getGuid()
	{
		return myGuid;
	}

	public int getDebuggerPort()
	{
		return myDebuggerPort;
	}

	public boolean isSupportDebugging()
	{
		return mySupportDebugging;
	}

	@Override
	public String toString()
	{
		return "UnityPlayer{" +
				"myIp='" + myIp + '\'' +
				", myGuid=" + myGuid +
				", myId='" + myId + '\'' +
				", mySupportDebugging=" + mySupportDebugging +
				", myDebuggerPort=" + myDebuggerPort +
				", myLastUpdateTime=" + myLastUpdateTime +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		UnityByUdpPlayer player = (UnityByUdpPlayer) o;

		if(myGuid != player.myGuid)
		{
			return false;
		}
		if(myIp != null ? !myIp.equals(player.myIp) : player.myIp != null)
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = myIp != null ? myIp.hashCode() : 0;
		result = 31 * result + (int) (myGuid ^ (myGuid >>> 32));
		return result;
	}
}
