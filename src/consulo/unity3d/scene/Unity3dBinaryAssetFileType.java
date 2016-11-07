package consulo.unity3d.scene;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.unity3d.Unity3dIcons;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class Unity3dBinaryAssetFileType implements FileType
{
	public static final Unity3dBinaryAssetFileType INSTANCE = new Unity3dBinaryAssetFileType();

	private Unity3dBinaryAssetFileType()
	{
	}

	@NotNull
	@Override
	public String getId()
	{
		return "UNITY_BINARY_ASSET";
	}

	@NotNull
	@Override
	public String getDescription()
	{
		return "Unity binary asset file";
	}

	@NotNull
	@Override
	public String getDefaultExtension()
	{
		return "";
	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return Unity3dIcons.Unity3d;
	}

	@Override
	public boolean isBinary()
	{
		return true;
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}

	@Nullable
	@Override
	public String getCharset(@NotNull VirtualFile file, byte[] content)
	{
		return null;
	}
}
