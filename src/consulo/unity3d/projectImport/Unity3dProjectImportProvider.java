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

package consulo.unity3d.projectImport;

import javax.swing.Icon;

import org.jetbrains.annotations.Nullable;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportProvider;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.projectImport.ui.Unity3dWizardStep;

/**
 * @author VISTALL
 * @since 29.12.14
 */
public class Unity3dProjectImportProvider extends ProjectImportProvider
{
	public Unity3dProjectImportProvider()
	{
		super(new Unity3dProjectImportBuilder());
	}

	@Nullable
	@Override
	public String getFileSample()
	{
		return "<b>Unity3D</b> project";
	}

	@Override
	public boolean canImport(VirtualFile fileOrDirectory, @Nullable Project project)
	{
		return fileOrDirectory.isDirectory() && fileOrDirectory.findFileByRelativePath("ProjectSettings/ProjectSettings.asset") != null;
	}

	@Override
	public ModuleWizardStep[] createSteps(WizardContext context)
	{
		return new ModuleWizardStep[]{
				new Unity3dWizardStep(context)
		};
	}

	@Nullable
	@Override
	public Icon getIconForFile(VirtualFile file)
	{
		if(canImport(file, null))
		{
			return Unity3dIcons.Unity3d;
		}
		else
		{
			return null;
		}
	}
}
