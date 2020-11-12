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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import consulo.ide.newProject.ui.ProjectOrModuleNameStep;
import consulo.unity3d.Unity3dBundle;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.UnityModuleImportContext;

import java.awt.*;

/**
 * @author VISTALL
 * @since 01.02.15
 */
public class Unity3dWizardStep extends ProjectOrModuleNameStep<UnityModuleImportContext>
{
	public Unity3dWizardStep(UnityModuleImportContext context)
	{
		super(context);

		String version = Unity3dProjectImporter.loadVersionFromProject(context.getPath());
		Unity3SdkPanel sdkPanel = new Unity3SdkPanel(context, version);
		myAdditionalContentPanel.add(sdkPanel.getPanel(), BorderLayout.NORTH);

		if(version != null)
		{
			JBLabel versionLabel = new JBLabel(Unity3dBundle.message("required.unity.version.is.0", version));
			versionLabel.setForeground(JBColor.GRAY);
			myAdditionalContentPanel.add(versionLabel, BorderLayout.SOUTH);
		}
	}
}
