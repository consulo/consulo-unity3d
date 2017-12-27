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

/**
 * @author VISTALL
 * @since 15.04.2015
 */
public class UnityProcess
{
	private int myPid;
	private String myName;
	private String myHost;
	private int myPort;

	public UnityProcess(int pid, String name, String host, int port)
	{
		myPid = pid;
		myName = name;
		myHost = host;
		myPort = port;
	}

	public String getName()
	{
		return myName;
	}

	public String getHost()
	{
		return myHost;
	}

	public int getPort()
	{
		return myPort;
	}

	@Override
	public int hashCode()
	{
		return myPid;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof UnityProcess && ((UnityProcess) obj).myPid == myPid;
	}
}
