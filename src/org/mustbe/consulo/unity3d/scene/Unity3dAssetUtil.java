/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.unity3d.scene;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.Unity3dMetaFileType;
import org.yaml.snakeyaml.Yaml;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 10.03.2016
 */
@Logger
public class Unity3dAssetUtil
{
	@Nullable
	public static String getUUID(VirtualFile virtualFile)
	{
		if(virtualFile == null)
		{
			return null;
		}
		String name = virtualFile.getName();

		VirtualFile parent = virtualFile.getParent();
		if(parent == null)
		{
			return null;
		}

		VirtualFile child = parent.findChild(name + "." + Unity3dMetaFileType.INSTANCE.getDefaultExtension());
		if(child != null)
		{
			Yaml yaml = new Yaml();
			InputStream inputStream = null;
			try
			{
				inputStream = child.getInputStream();
				Object load = yaml.load(inputStream);
				if(load instanceof Map)
				{
					Object guid = ((Map) load).get("guid");
					if(guid instanceof String)
					{
						return (String) guid;
					}
				}
			}
			catch(IOException e)
			{
				LOGGER.warn(e);
			}
			finally
			{
				if(inputStream != null)
				{
					try
					{
						inputStream.close();
					}
					catch(IOException e)
					{
						//
					}
				}
			}
		}
		return null;
	}
}
