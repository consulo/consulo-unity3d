package consulo.unity3d.usages;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author VISTALL
 * @since 2018-03-10
 */
@Singleton
@State(name = "Unity3dAssetUsageSettings", storages = @Storage("other.xml"))
public class Unity3dAssetUsageSettings implements PersistentStateComponent<Unity3dAssetUsageSettings>
{
	@Nonnull
	public static Unity3dAssetUsageSettings getInstance()
	{
		return ServiceManager.getService(Unity3dAssetUsageSettings.class);
	}

	public boolean SHOW_USAGES_IN_ASSETS;

	@Override
	public Unity3dAssetUsageSettings getState()
	{
		return this;
	}

	@Override
	public void loadState(final Unity3dAssetUsageSettings state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
