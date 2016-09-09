package consulo.unity3d.editor;

/**
 * @author VISTALL
 * @since 17.01.2016
 * <p>
 * WARNING: dont change name, if unity plugin is not changed, name used in request url gen
 */
public class UnityOpenScene
{
	public final String file;

	public UnityOpenScene(String file)
	{
		this.file = file;
	}
}
