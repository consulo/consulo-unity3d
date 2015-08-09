package org.mustbe.consulo.unity3d.scene;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class Unity3dBinarySceneFileType implements FileType
{
	public static final Unity3dBinarySceneFileType INSTANCE = new Unity3dBinarySceneFileType();

	private Unity3dBinarySceneFileType()
	{
	}

	@NotNull
	@Override
	public String getName()
	{
		return "UNITY_BINARY_SCENE";
	}

	@NotNull
	@Override
	public String getDescription()
	{
		return "Unity binary scene file";
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
