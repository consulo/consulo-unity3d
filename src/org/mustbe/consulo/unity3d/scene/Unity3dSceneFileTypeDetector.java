package org.mustbe.consulo.unity3d.scene;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class Unity3dSceneFileTypeDetector implements FileTypeRegistry.FileTypeDetector
{
	public static class Yml
	{

	}
	@Nullable
	@Override
	public FileType detect(@NotNull VirtualFile file, @NotNull ByteSequence firstBytes, @Nullable CharSequence firstCharsIfText)
	{
		if("unity".equals(file.getExtension()))
		{
			if(firstCharsIfText == null)
			{
				return Unity3dBinarySceneFileType.INSTANCE;
			}
			if(firstCharsIfText.length() > 5)
			{
				CharSequence sequence = firstCharsIfText.subSequence(0, 5);
				if(StringUtil.equals("%YAML", sequence))
				{
					return Unity3dYMLSceneFileType.INSTANCE;
				}
			}

			return Unity3dBinarySceneFileType.INSTANCE;
		}
		return null;
	}

	@Override
	public int getVersion()
	{
		return 1;
	}
}
