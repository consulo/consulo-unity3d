package consulo.unity3d.scene;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.unity3d.Unity3dIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class Unity3dYMLAssetFileType extends LanguageFileType
{
	public static final Unity3dYMLAssetFileType INSTANCE = new Unity3dYMLAssetFileType();

	private Unity3dYMLAssetFileType()
	{
		super(PlainTextLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public String getName()
	{
		return "UNITY_YML_ASSET";
	}

	@NotNull
	@Override
	public String getDescription()
	{
		return "Unity yml asset file";
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
		return Unity3dIcons.Shader;
	}
}
