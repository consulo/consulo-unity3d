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

package org.mustbe.consulo.unity3d.module.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;

import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.consulo.module.extension.ui.ModuleExtensionSdkBoxBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.dotnet.DotNetBundle;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dRootModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dRootMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dTarget;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.NullableFunction;

/**
 * @author VISTALL
 * @see org.mustbe.consulo.dotnet.module.extension.DotNetConfigurationPanel
 * @since 27.10.14
 */
public class UnityConfigurationPanel extends JPanel
{
	@RequiredDispatchThread
	public UnityConfigurationPanel(final Unity3dRootMutableModuleExtension extension, final List<String> variables, final Runnable updater)
	{
		super(new VerticalFlowLayout(true, true));
		ModuleExtensionSdkBoxBuilder<Unity3dRootMutableModuleExtension> sdkBoxBuilder = ModuleExtensionSdkBoxBuilder.create(extension, updater);
		sdkBoxBuilder.sdkTypeClass(extension.getSdkTypeClass());
		sdkBoxBuilder.sdkPointerFunc(new NullableFunction<Unity3dRootMutableModuleExtension, MutableModuleInheritableNamedPointer<Sdk>>()
		{
			@Nullable
			@Override
			public MutableModuleInheritableNamedPointer<Sdk> fun(Unity3dRootMutableModuleExtension mutableModuleExtension)
			{
				return mutableModuleExtension.getInheritableSdk();
			}
		});
		add(sdkBoxBuilder.build());

		final ComboBox target = new ComboBox(Unity3dTarget.values());
		target.setRenderer(new ColoredListCellRendererWrapper<Unity3dTarget>()
		{
			@Override
			protected void doCustomize(JList list, Unity3dTarget value, int index, boolean selected, boolean hasFocus)
			{
				String presentation = value.getPresentation();
				int i = presentation.indexOf('(');
				if(i != -1)
				{
					append(presentation.substring(0, i));
					append(presentation.substring(i, presentation.length()), SimpleTextAttributes.GRAY_ATTRIBUTES);
				}
				else
				{
					append(presentation);
				}
			}
		});
		target.setSelectedItem(extension.getBuildTarget());
		target.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					extension.setBuildTarget((Unity3dTarget) target.getSelectedItem());
				}
			}
		});

		final JBTextField fileNameField = new JBTextField(extension.getFileName());
		fileNameField.getEmptyText().setText(Unity3dRootModuleExtension.FILE_NAME);
		fileNameField.getDocument().addDocumentListener(new DocumentAdapter()
		{
			@Override
			protected void textChanged(DocumentEvent documentEvent)
			{
				extension.setFileName(fileNameField.getText());
			}
		});

		add(LabeledComponent.left(fileNameField, DotNetBundle.message("file.label")));

		final JBTextField outputDirectoryField = new JBTextField(extension.getOutputDir());
		outputDirectoryField.getEmptyText().setText(DotNetModuleExtension.DEFAULT_OUTPUT_DIR);
		outputDirectoryField.getDocument().addDocumentListener(new DocumentAdapter()
		{
			@Override
			protected void textChanged(DocumentEvent documentEvent)
			{
				extension.setOutputDir(outputDirectoryField.getText());
			}
		});

		add(LabeledComponent.left(outputDirectoryField, DotNetBundle.message("output.dir.label")));
		add(LabeledComponent.left(target, "Target:"));

		final CollectionListModel<String> dataModel = new CollectionListModel<String>(variables)
		{
			@Override
			public int getSize()
			{
				return variables.size();
			}

			@Override
			public String getElementAt(int index)
			{
				return variables.get(index);
			}

			@Override
			public void add(final String element)
			{
				int i = variables.size();
				variables.add(element);
				fireIntervalAdded(this, i, i);
			}

			@Override
			public void remove(@NotNull final String element)
			{
				int i = variables.indexOf(element);
				variables.remove(element);
				fireIntervalRemoved(this, i, i);
			}

			@Override
			public void remove(int index)
			{
				variables.remove(index);
				fireIntervalRemoved(this, index, index);
			}
		};

		final JBList variableList = new JBList(dataModel);
		ToolbarDecorator variableDecorator = ToolbarDecorator.createDecorator(variableList);
		variableDecorator.setAddAction(new AnActionButtonRunnable()
		{
			@Override
			public void run(AnActionButton anActionButton)
			{
				String name = Messages.showInputDialog(UnityConfigurationPanel.this, DotNetBundle.message("new.variable.message"),
						DotNetBundle.message("new.variable.title"), null, null, new InputValidator()
				{
					@Override
					public boolean checkInput(String s)
					{
						return !variables.contains(s);
					}

					@Override
					public boolean canClose(String s)
					{
						return true;
					}
				});

				if(StringUtil.isEmpty(name))
				{
					return;
				}

				dataModel.add(name);
			}
		});
		add(new JBLabel("Preprocessor variables:"));
		add(variableDecorator.createPanel());
	}
}
