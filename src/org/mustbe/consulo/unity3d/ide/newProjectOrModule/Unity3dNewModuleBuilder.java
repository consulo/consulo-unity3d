package org.mustbe.consulo.unity3d.ide.newProjectOrModule;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.module.roots.DotNetLibraryOrderEntryImpl;
import org.mustbe.consulo.ide.impl.NewModuleBuilder;
import org.mustbe.consulo.ide.impl.NewModuleBuilderProcessor;
import org.mustbe.consulo.ide.impl.NewModuleContext;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.csharp.module.extension.Unity3dCSharpMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dMutableModuleExtension;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.impl.ModuleRootLayerImpl;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dNewModuleBuilder implements NewModuleBuilder
{
	@Override
	public void setupContext(@NotNull NewModuleContext context)
	{
		context.addItem("#Unity3d", "Unity3D", Unity3dIcons.Unity3d);
		context.addItem("#Unity3dEmptyProject", "Empty", AllIcons.RunConfigurations.Application);

		context.setupItem(new String[]{
				"#Unity3d",
				"#Unity3dEmptyProject"
		}, new NewModuleBuilderProcessor<Unity3dNewModuleBuilderPanel>()
		{
			@NotNull
			@Override
			public Unity3dNewModuleBuilderPanel createConfigurationPanel()
			{
				return new Unity3dNewModuleBuilderPanel();
			}

			@Override
			public void setupModule(@NotNull Unity3dNewModuleBuilderPanel panel,
					@NotNull ContentEntry contentEntry,
					@NotNull ModifiableRootModel modifiableRootModel)
			{
				ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableRootModel.getCurrentLayer();

				// first we need enable .NET module extension
				Unity3dMutableModuleExtension unityExtension = layer.getExtensionWithoutCheck(Unity3dMutableModuleExtension.class);
				assert unityExtension != null;

				unityExtension.setEnabled(true);

				Sdk sdk = panel.getSdk();
				if(sdk != null)
				{

					unityExtension.getInheritableSdk().set(null, sdk);
				}

				Unity3dCSharpMutableModuleExtension csharpExtension = layer.getExtensionWithoutCheck
						(Unity3dCSharpMutableModuleExtension.class);

				assert csharpExtension != null;
				csharpExtension.setEnabled(true);

				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "mscorlib"));
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System"));
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Core"));
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine"));
			}
		});
	}
}
