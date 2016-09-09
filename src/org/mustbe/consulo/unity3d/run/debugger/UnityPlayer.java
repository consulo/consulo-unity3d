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

import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class UnityPlayer
{
	private static final long UPDATE_TIME = 5000L;

	private final String myIp;
	private final long myGuid;
	private final String myId;
	private final boolean mySupportDebugging;
	private int myDebuggerPort;

	private long myLastUpdateTime;

	public UnityPlayer(@NotNull Matcher matcher)
	{
		myIp = matcher.group("ip");
		myId = matcher.group("id");
		myGuid = Long.parseLong(matcher.group("guid"));
		mySupportDebugging = "1".equals(StringUtil.notNullize(matcher.group("debug")).trim());
		if(mySupportDebugging)
		{
			String debuggerPort = matcher.group("debuggerPort");
			if(debuggerPort == null)
			{
				myDebuggerPort = 56000 + (int) (myGuid % 1000);
			}
			else
			{
				myDebuggerPort = Integer.parseInt(debuggerPort);
			}
		}
		update();
	}

	public boolean isAvailable()
	{
		return myLastUpdateTime > System.currentTimeMillis();
	}

	public String getId()
	{
		return myId;
	}

	public void update()
	{
		myLastUpdateTime = System.currentTimeMillis() + UPDATE_TIME;
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

		UnityPlayer player = (UnityPlayer) o;

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
