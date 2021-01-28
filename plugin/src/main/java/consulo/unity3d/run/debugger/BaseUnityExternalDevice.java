package consulo.unity3d.run.debugger;

/**
 * @author VISTALL
 * @since 27/01/2021
 */
public abstract class BaseUnityExternalDevice implements UnityExternalDevice
{
	private static final long UPDATE_TIME = 5000L;

	protected long myLastUpdateTime;

	@Override
	public boolean isAvailable()
	{
		return myLastUpdateTime > System.currentTimeMillis();
	}

	@Override
	public void update()
	{
		myLastUpdateTime = System.currentTimeMillis() + UPDATE_TIME;
	}
}
