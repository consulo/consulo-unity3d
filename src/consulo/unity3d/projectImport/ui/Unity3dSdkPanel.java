/*
 * Copyright 2013-2014 must-be.org
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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.ui.components.panels.HorizontalLayout;
import consulo.annotations.RequiredDispatchThread;
import consulo.roots.ui.configuration.SdkComboBox;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dSdkPanel extends JPanel
{
	private SdkComboBox myComboBox;

	public Unity3dSdkPanel(@Nullable String requiredVersion)
	{
		super(new VerticalFlowLayout());

		ProjectSdksModel projectSdksModel = new ProjectSdksModel();
		projectSdksModel.reset();

		JButton button = new JButton("Ne\u001Bw...");
		Condition<SdkTypeId> filter = Conditions.<SdkTypeId>is(Unity3dBundleType.getInstance());
		myComboBox = new SdkComboBox(projectSdksModel, filter, true);
		myComboBox.setSetupButton(button, null, projectSdksModel, null, new Condition<Sdk>()
		{
			@Override
			@RequiredDispatchThread
			public boolean value(final Sdk sdk)
			{
				ApplicationManager.getApplication().runWriteAction(new Runnable()
				{
					@Override
					public void run()
					{
						SdkTable.getInstance().addSdk(sdk);
					}
				});
				return false;
			}
		});

		if(requiredVersion != null)
		{
			for(Sdk sdk : projectSdksModel.getSdks())
			{
				if(filter.value(sdk.getSdkType()))
				{
					String versionString = sdk.getVersionString();
					if(Comparing.equal(requiredVersion, versionString))
					{
						myComboBox.setSelectedSdk(sdk);
						break;
					}
				}
			}
		}
		JPanel panel = new JPanel(new HorizontalLayout(0, SwingConstants.CENTER));
		panel.add(LabeledComponent.left(myComboBox, "Unity SDK"), HorizontalLayout.LEFT);
		panel.add(button, HorizontalLayout.RIGHT);
		add(panel);
	}

	@Nullable
	public Sdk getSdk()
	{
		return myComboBox.getSelectedSdk();
	}
}
