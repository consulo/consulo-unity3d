package consulo.unity3d.scene;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class Unity3dAssetFileTypeDetector implements FileTypeRegistry.FileTypeDetector
{
	public static final String[] ourAssetExtensions = {"unity", "prefab"};

	@Nullable
	@Override
	public FileType detect(@NotNull VirtualFile file, @NotNull ByteSequence firstBytes, @Nullable CharSequence firstCharsIfText)
	{
		if(ArrayUtil.contains(file.getExtension(), ourAssetExtensions))
		{
			if(firstCharsIfText == null)
			{
				return Unity3dBinaryAssetFileType.INSTANCE;
			}
			if(firstCharsIfText.length() > 5)
			{
				CharSequence sequence = firstCharsIfText.subSequence(0, 5);
				if(StringUtil.equals("%YAML", sequence))
				{
					return Unity3dYMLAssetFileType.INSTANCE;
				}
			}

			return Unity3dBinaryAssetFileType.INSTANCE;
		}
		return null;
	}

	@Override
	public int getVersion()
	{
		return 2;
	}
}
