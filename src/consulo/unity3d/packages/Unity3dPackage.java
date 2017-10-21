/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.packages;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Version;

/**
 * @author VISTALL
 * @since 21-Oct-17
 */
public class Unity3dPackage
{
	private final String myId;
	private final Version myVersion;
	private final String myPath;

	public Unity3dPackage(@NotNull String id, @NotNull Version version, @NotNull String path)
	{
		myId = id;
		myVersion = version;
		myPath = path;
	}

	@NotNull
	public String getId()
	{
		return myId;
	}

	@NotNull
	public Version getVersion()
	{
		return myVersion;
	}

	@NotNull
	public String getPath()
	{
		return myPath;
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

		Unity3dPackage that = (Unity3dPackage) o;

		if(!myId.equals(that.myId))
		{
			return false;
		}
		if(!myVersion.equals(that.myVersion))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = myId.hashCode();
		result = 31 * result + (myVersion.hashCode());
		return result;
	}
}
