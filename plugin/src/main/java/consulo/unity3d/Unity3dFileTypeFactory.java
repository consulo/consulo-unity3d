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

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.util.text.StringUtil;
import consulo.json.JsonFileType;
import consulo.unity3d.asmdef.AsmDefFileDescriptor;
import consulo.unity3d.scene.Unity3dAssetFileTypeDetector;
import consulo.unity3d.scene.Unity3dBinaryAssetFileType;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 02.03.2015
 */
public class Unity3dFileTypeFactory extends FileTypeFactory
{
	@Override
	public void createFileTypes(@Nonnull FileTypeConsumer consumer)
	{
		consumer.consume(Unity3dMetaFileType.INSTANCE);
		consumer.consume(Unity3dBinaryAssetFileType.INSTANCE, StringUtil.join(Unity3dAssetFileTypeDetector.ourAssetExtensions, ";"));
		consumer.consume(Unity3dYMLAssetFileType.INSTANCE);

		consumer.consume(Unity3dBinaryFileType.INSTANCE, "fbx");

		consumer.consume(JsonFileType.INSTANCE, AsmDefFileDescriptor.EXTENSION);
	}
}
