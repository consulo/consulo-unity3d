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

package org.mustbe.consulo.unity3d.run;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.application.options.ModuleListCellRenderer;
import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import lombok.val;

/**
 * @author VISTALL
 * @since 17.11.2014
 */
public class UnityConfigurationEditor extends SettingsEditor<UnityConfiguration>
{
	private final Project myProject;

	private JComboBox myModuleComboBox;
	private CommonProgramParametersPanel myProgramParametersPanel;

	public UnityConfigurationEditor(Project project)
	{
		myProject = project;
	}

	@Override
	protected void resetEditorFrom(UnityConfiguration runConfiguration)
	{
		myProgramParametersPanel.reset(runConfiguration);
		myModuleComboBox.setSelectedItem(runConfiguration.getConfigurationModule().getModule());
		myProgramParametersPanel.setModuleContext(runConfiguration.getConfigurationModule().getModule());
	}

	@Override
	protected void applyEditorTo(UnityConfiguration runConfiguration) throws ConfigurationException
	{
		myProgramParametersPanel.applyTo(runConfiguration);
		runConfiguration.getConfigurationModule().setModule((Module) myModuleComboBox.getSelectedItem());
	}

	@NotNull
	@Override
	protected JComponent createEditor()
	{
		myProgramParametersPanel = new CommonProgramParametersPanel();

		myModuleComboBox = new JComboBox();
		myModuleComboBox.setRenderer(new ModuleListCellRenderer());
		for(val module : ModuleManager.getInstance(myProject).getModules())
		{
			if(ModuleUtilCore.getExtension(module, DotNetModuleExtension.class) != null)
			{
				myModuleComboBox.addItem(module);
			}
		}
		myModuleComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					myProgramParametersPanel.setModuleContext((Module) myModuleComboBox.getSelectedItem());
				}
			}
		});

		FormBuilder formBuilder = FormBuilder.createFormBuilder();
		formBuilder.addLabeledComponent("Module", myModuleComboBox);

		myProgramParametersPanel.add(formBuilder.getPanel());
		return myProgramParametersPanel;
	}
}
