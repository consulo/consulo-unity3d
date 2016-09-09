package consulo.unity3d.unityscript.module.extension;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import consulo.annotations.RequiredReadAction;
import consulo.extension.impl.ModuleExtensionImpl;
import consulo.javascript.lang.JavaScriptLanguage;
import consulo.javascript.module.extension.JavaScriptModuleExtension;
import consulo.lang.LanguageVersion;
import consulo.module.extension.ModuleInheritableNamedPointer;
import consulo.roots.ModuleRootLayer;
import consulo.unity3d.module.EmptyModuleInheritableNamedPointer;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.unityscript.lang.UnityScriptLanguageVersion;

/**
 * @author VISTALL
 * @since 02.04.2015
 */
public class Unity3dScriptModuleExtension extends ModuleExtensionImpl<Unity3dScriptModuleExtension> implements JavaScriptModuleExtension<Unity3dScriptModuleExtension>
{
	public Unity3dScriptModuleExtension(@NotNull String id, @NotNull ModuleRootLayer moduleRootLayer)
	{
		super(id, moduleRootLayer);
	}

	@NotNull
	@Override
	public ModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return EmptyModuleInheritableNamedPointer.empty();
	}

	@Nullable
	@Override
	@RequiredReadAction
	public Sdk getSdk()
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(getProject());
		if(rootModuleExtension != null)
		{
			return rootModuleExtension.getSdk();
		}
		return null;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getSdkName()
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(getProject());
		if(rootModuleExtension != null)
		{
			return rootModuleExtension.getSdkName();
		}
		return null;
	}

	@NotNull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		throw new UnsupportedOperationException("Use root module extension");
	}

	@NotNull
	@Override
	public LanguageVersion<JavaScriptLanguage> getLanguageVersion()
	{
		return UnityScriptLanguageVersion.getInstance();
	}
}
