package consulo.unity3d.usages;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.ide.ServiceManager;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2018-03-10
 */
@Singleton
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
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
