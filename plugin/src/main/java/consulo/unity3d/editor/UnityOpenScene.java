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

package consulo.unity3d.editor;

/**
 * @author VISTALL
 * @since 17.01.2016
 * <p>
 * WARNING: dont change name, if unity plugin is not changed, name used in request url gen
 */
public class UnityOpenScene
{
	public final String file;

	public UnityOpenScene(String file)
	{
		this.file = file;
	}
}
