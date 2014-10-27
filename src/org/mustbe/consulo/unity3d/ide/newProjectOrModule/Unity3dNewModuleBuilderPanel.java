package org.mustbe.consulo.unity3d.ide.newProjectOrModule;

import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.projectRoots.impl.SdkListCellRenderer;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dNewModuleBuilderPanel extends JPanel
{
	private ComboBox myComboBox;

	public Unity3dNewModuleBuilderPanel()
	{
		super(new VerticalFlowLayout());

		SdkTable sdkTable = SdkTable.getInstance();
		myComboBox = new ComboBox();
		myComboBox.setRenderer(new SdkListCellRenderer("<none>"));

		myComboBox.addItem(null);



		for(Sdk o : sdkTable.getAllSdks())
		{
			SdkTypeId sdkType = o.getSdkType();
			if(sdkType == Unity3dBundleType.getInstance())
			{
				myComboBox.addItem(o);
			}
		}

		add(LabeledComponent.left(myComboBox, "Unity SDK"));
	}

	@Nullable
	public Sdk getSdk()
	{
		return (Sdk) myComboBox.getSelectedItem();
	}
}
