/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.unity3d.scene.Unity3dBinarySceneFileType;
import org.mustbe.consulo.unity3d.scene.Unity3dYMLSceneFileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;

/**
 * @author VISTALL
 * @since 02.03.2015
 */
public class Unity3dFileTypeFactory extends FileTypeFactory
{
	@Override
	public void createFileTypes(@NotNull FileTypeConsumer consumer)
	{
		consumer.consume(Unity3dMetaFileType.INSTANCE);
		consumer.consume(Unity3dBinarySceneFileType.INSTANCE);
		consumer.consume(Unity3dYMLSceneFileType.INSTANCE);
	}
}
