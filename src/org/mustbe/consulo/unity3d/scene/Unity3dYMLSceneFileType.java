package org.mustbe.consulo.unity3d.scene;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class Unity3dYMLSceneFileType extends LanguageFileType
{
	public static final Unity3dYMLSceneFileType INSTANCE = new Unity3dYMLSceneFileType();

	private Unity3dYMLSceneFileType()
	{
		super(PlainTextLanguage.INSTANCE);
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
		return "Unity yml scene file";
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
}
