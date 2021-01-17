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

package consulo.unity3d;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
@Deprecated
public class Unity3dBundle extends AbstractBundle
{
	private static final Unity3dBundle ourInstance = new Unity3dBundle();

	private Unity3dBundle()
	{
		super("messages.Unity3dBundle");
	}

	public static String message(@PropertyKey(resourceBundle = "messages.Unity3dBundle") String key)
	{
		return ourInstance.getMessage(key);
	}

	public static String message(@PropertyKey(resourceBundle = "messages.Unity3dBundle") String key, Object... params)
	{
		return ourInstance.getMessage(key, params);
	}
}
