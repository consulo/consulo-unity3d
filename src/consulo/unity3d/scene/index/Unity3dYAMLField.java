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

package consulo.unity3d.scene.index;

/**
 * @author VISTALL
 * @since 04-Sep-17
 */
public class Unity3dYAMLField
{
	private final String myName;
	private final String myValue;
	private final int myOffset;

	public Unity3dYAMLField(String name, String value, int offset)
	{
		myName = name;
		myValue = value;
		myOffset = offset;
	}

	public String getName()
	{
		return myName;
	}

	public String getValue()
	{
		return myValue;
	}

	public int getOffset()
	{
		return myOffset;
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

		Unity3dYAMLField that = (Unity3dYAMLField) o;

		if(myOffset != that.myOffset)
		{
			return false;
		}
		if(myName != null ? !myName.equals(that.myName) : that.myName != null)
		{
			return false;
		}
		if(myValue != null ? !myValue.equals(that.myValue) : that.myValue != null)
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = myName != null ? myName.hashCode() : 0;
		result = 31 * result + (myValue != null ? myValue.hashCode() : 0);
		result = 31 * result + myOffset;
		return result;
	}
}
