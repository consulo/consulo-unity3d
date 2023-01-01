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
public record Unity3dYMLField(String name, String value, int offset)
{
	@Deprecated
	public String getName()
	{
		return name();
	}

	@Deprecated
	public String getValue()
	{
		return value();
	}

	@Deprecated
	public int getOffset()
	{
		return offset();
	}
}
