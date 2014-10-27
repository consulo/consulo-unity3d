package org.mustbe.consulo.unity3d.module.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.DotNetBundle;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtensionWithSdkPanel;
import org.mustbe.consulo.dotnet.module.extension.DotNetMutableModuleExtension;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import lombok.val;

/**
 * @author VISTALL
 * @see org.mustbe.consulo.dotnet.module.extension.DotNetConfigurationPanel
 * @since 27.10.14
 */
public class UnityConfigurationPanel extends JPanel
{
	public UnityConfigurationPanel(final DotNetMutableModuleExtension<?> extension, final List<String> variables, final Runnable updater)
	{
		super(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
		add(DotNetModuleExtensionWithSdkPanel.create(extension, EmptyRunnable.INSTANCE));


		val namespacePrefixField = new JBTextField(extension.getNamespacePrefix());
		namespacePrefixField.getDocument().addDocumentListener(new DocumentAdapter()
		{
			@Override
			protected void textChanged(DocumentEvent documentEvent)
			{
				extension.setNamespacePrefix(namespacePrefixField.getText());
			}
		});

		final LabeledComponent<JBTextField> namespaceComponent = LabeledComponent.left(namespacePrefixField, "Namespace:");

		val allowSourceRootsBox = new JBCheckBox(DotNetBundle.message("allow.source.roots.label"), extension.isAllowSourceRoots());
		allowSourceRootsBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				extension.setAllowSourceRoots(allowSourceRootsBox.isSelected());
				namespaceComponent.setVisible(!allowSourceRootsBox.isSelected());

				updater.run();
			}
		});
		add(allowSourceRootsBox);
		add(namespaceComponent);

		val dataModel = new CollectionListModel<String>(variables)
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

		val variableList = new JBList(dataModel);
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
