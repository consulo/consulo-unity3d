/*
 * Copyright 2013-2016 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.unity3d.projectImport.ui;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.projectRoots.impl.SdkImpl;
import consulo.awt.TargetAWT;
import consulo.bundle.ui.BundleBox;
import consulo.bundle.ui.BundleBoxBuilder;
import consulo.disposer.Disposable;
import consulo.ide.newProject.ui.UnifiedProjectOrModuleNameStep;
import consulo.ide.settings.impl.ShowSdksSettingsUtil;
import consulo.localize.LocalizeValue;
import consulo.ui.Button;
import consulo.ui.ComboBox;
import consulo.ui.Label;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.DockLayout;
import consulo.ui.model.MutableListModel;
import consulo.ui.style.StandardColors;
import consulo.ui.util.FormBuilder;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.localize.Unity3dLocalize;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.UnityModuleImportContext;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 01.02.15
 */
public class Unity3dWizardStep extends UnifiedProjectOrModuleNameStep<UnityModuleImportContext>
{
	private final UnityModuleImportContext myContext;

	private Disposable myUiDisposable;

	private BundleBox myBundleBox;

	public Unity3dWizardStep(UnityModuleImportContext context)
	{
		super(context);
		myContext = context;
	}

	@RequiredUIAccess
	@Override
	protected void extend(@Nonnull FormBuilder builder)
	{
		super.extend(builder);

		String requiredVersion = Unity3dProjectImporter.loadVersionFromProject(myContext.getPath());

		myUiDisposable = Disposable.newDisposable();
		BundleBoxBuilder boxBuilder = BundleBoxBuilder.create(myUiDisposable);
		boxBuilder.withSdkTypeFilterByType(Unity3dBundleType.getInstance());

		ComboBox<BundleBox.BundleBoxItem> comboBox = (myBundleBox = boxBuilder.build()).getComponent();
		DockLayout dock = DockLayout.create();
		dock.center(comboBox);
		dock.right(Button.create(LocalizeValue.localizeTODO("Select..."), clickEvent -> {
			JComponent awtComponent = (JComponent) TargetAWT.to(myBundleBox.getComponent());

			showAddSdk(awtComponent, sdk -> {
				WriteAction.run(() -> SdkTable.getInstance().addSdk(sdk));

				MutableListModel<BundleBox.BundleBoxItem> listModel = (MutableListModel<BundleBox.BundleBoxItem>) comboBox.getListModel();

				BundleBox.BaseBundleBoxItem item = new BundleBox.BaseBundleBoxItem(sdk);

				listModel.add(item);

				UIAccess.current().give(() -> comboBox.setValue(item));
			});
		}));
		builder.addLabeled(Unity3dLocalize.unityName(), dock);

		if(requiredVersion != null)
		{
			for(BundleBox.BundleBoxItem item : comboBox.getListModel())
			{
				Sdk bundle = item.getBundle();

				if(bundle != null)
				{
					String versionString = bundle.getVersionString();
					if(Objects.equals(requiredVersion, versionString))
					{
						comboBox.setValue(item);
						break;
					}
				}
			}
		}

		if(comboBox.getValue() == null && comboBox.getListModel().getSize() > 0)
		{
			myBundleBox.getComponent().setValueByIndex(0);
		}

		if(requiredVersion != null)
		{
			Label versionLabel = Label.create(Unity3dLocalize.requiredUnityVersionIs0(requiredVersion));
			versionLabel.setForeground(StandardColors.GRAY);

			builder.addBottom(versionLabel);
		}
	}

	@RequiredUIAccess
	public static void showAddSdk(@Nonnull JComponent awtComponent, @RequiredUIAccess Consumer<Sdk> sdkConsumer)
	{
		ShowSdksSettingsUtil sdksSettingsUtil = (ShowSdksSettingsUtil) ShowSettingsUtil.getInstance();

		Unity3dBundleType type = Unity3dBundleType.getInstance();

		if(type.supportsCustomCreateUI())
		{
			type.showCustomCreateUI(sdksSettingsUtil.getSdksModel(), awtComponent, sdkConsumer::accept);
		}
		else
		{
			SdkConfigurationUtil.selectSdkHome(type, home -> {
				String newSdkName = SdkConfigurationUtil.createUniqueSdkName(type, home, sdksSettingsUtil.getSdksModel().getBundles());
				final SdkImpl newSdk = new SdkImpl(SdkTable.getInstance(), newSdkName, type);
				newSdk.setHomePath(home);

				sdkConsumer.accept(newSdk);
			});
		}
	}

	@Override
	public void onStepLeave(@Nonnull UnityModuleImportContext context)
	{
		super.onStepLeave(context);

		if(myBundleBox != null)
		{
			String selectedBundleName = myBundleBox.getSelectedBundleName();
			if(selectedBundleName != null)
			{
				context.setSdk(SdkTable.getInstance().findSdk(selectedBundleName));
			}
		}
	}

	@Override
	public void disposeUIResources()
	{
		super.disposeUIResources();

		if(myUiDisposable != null)
		{
			myUiDisposable.disposeWithTree();
			myUiDisposable = null;
		}
	}
}
