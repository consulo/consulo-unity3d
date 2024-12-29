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

package consulo.unity3d.module.ui;

import consulo.dotnet.DotNetBundle;
import consulo.module.ui.extension.ModuleExtensionSdkBoxBuilder;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.InputValidator;
import consulo.ui.ex.awt.*;
import consulo.ui.ex.awt.event.DocumentAdapter;
import consulo.unity3d.module.Unity3dRootMutableModuleExtension;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.util.List;

/**
 * @author VISTALL
 * @see consulo.dotnet.module.extension.DotNetConfigurationPanel
 * @since 27.10.14
 */
public class UnityConfigurationPanel extends JPanel
{
	@RequiredUIAccess
	public UnityConfigurationPanel(final Unity3dRootMutableModuleExtension extension, final List<String> variables, final Runnable updater)
	{
		super(new VerticalFlowLayout(true, true));
		ModuleExtensionSdkBoxBuilder<Unity3dRootMutableModuleExtension> sdkBoxBuilder = ModuleExtensionSdkBoxBuilder.create(extension, updater);
		sdkBoxBuilder.sdkTypeClass(extension.getSdkTypeClass());
		sdkBoxBuilder.sdkPointerFunc(Unity3dRootMutableModuleExtension::getInheritableSdk);
		add(sdkBoxBuilder.build());

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
			public void remove(@Nonnull final String element)
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

		final JBTextField namespacePrefixField = new JBTextField(extension.getNamespacePrefix());
		namespacePrefixField.getDocument().addDocumentListener(new DocumentAdapter()
		{
			@Override
			protected void textChanged(DocumentEvent documentEvent)
			{
				extension.setNamespacePrefix(namespacePrefixField.getText());
			}
		});

		add(LabeledComponent.create(namespacePrefixField, "Namespace Prefix:"));

		final JBList variableList = new JBList(dataModel);
		ToolbarDecorator variableDecorator = ToolbarDecorator.createDecorator(variableList);
		variableDecorator.setAddAction(anActionButton -> {
			String name1 = Messages.showInputDialog(UnityConfigurationPanel.this, DotNetBundle.message("new.variable.message"), DotNetBundle.message("new.variable.title"), null, null,
					new InputValidator()
			{
				@RequiredUIAccess
				@Override
				public boolean checkInput(String s)
				{
					return !variables.contains(s);
				}

				@RequiredUIAccess
				@Override
				public boolean canClose(String s)
				{
					return true;
				}
			});

			if(StringUtil.isEmpty(name1))
			{
				return;
			}

			dataModel.add(name1);
		});
		add(new JBLabel("Preprocessor variables:"));
		add(variableDecorator.createPanel());
	}
}
