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

import java.awt.GridBagConstraints;

import com.intellij.ide.util.newProjectWizard.ProjectNameStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import consulo.unity3d.Unity3dBundle;
import consulo.unity3d.projectImport.Unity3dProjectUtil;
import consulo.unity3d.projectImport.UnityModuleImportContext;

/**
 * @author VISTALL
 * @since 01.02.15
 */
public class Unity3dWizardStep extends ProjectNameStep
{
	private Unity3dSdkPanel mySdkPanel;
	private UnityModuleImportContext myContext;

	public Unity3dWizardStep(UnityModuleImportContext context, WizardContext wizardContext)
	{
		super(wizardContext);
		myContext = context;

		String version = Unity3dProjectUtil.loadVersionFromProject(wizardContext.getProjectFileDirectory());
		mySdkPanel = new Unity3dSdkPanel(version);
		myAdditionalContentPanel.add(mySdkPanel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, JBUI.emptyInsets(), 0,
				0));

		if(version != null)
		{
			JBLabel versionLabel = new JBLabel(Unity3dBundle.message("required.unity.version.is.0", version));
			versionLabel.setForeground(JBColor.GRAY);
			myAdditionalContentPanel.add(versionLabel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, JBUI.emptyInsets(), 0, 0));
		}
	}

	@Override
	public void updateDataModel()
	{
		super.updateDataModel();

		myContext.setSdk(mySdkPanel.getSdk());
	}
}
