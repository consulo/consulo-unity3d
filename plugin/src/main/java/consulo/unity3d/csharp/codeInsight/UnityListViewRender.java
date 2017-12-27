/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.csharp.codeInsight;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.ui.UIUtil;

/**
 * @author VISTALL
 * @since 04-Sep-17
 */
abstract class UnityListViewRender implements ListCellRenderer<UnityAssetWrapper>
{
	private JComponent myPanel;

	private ColoredListCellRenderer<UnityAssetWrapper> myLeft;
	private ColoredListCellRenderer<UnityAssetWrapper> myRight;

	UnityListViewRender()
	{
		myPanel = new JPanel(new BorderLayout());
		myLeft = createLeft();
		myRight = createRight();
	}

	protected abstract ColoredListCellRenderer<UnityAssetWrapper> createLeft();

	protected abstract ColoredListCellRenderer<UnityAssetWrapper> createRight();

	@Override
	public Component getListCellRendererComponent(JList<? extends UnityAssetWrapper> list, UnityAssetWrapper value, int index, boolean isSelected, boolean cellHasFocus)
	{
		myPanel.removeAll();

		myPanel.setBackground(UIUtil.getListBackground(isSelected));
		myPanel.add(myLeft.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus), BorderLayout.CENTER);
		myPanel.add(myRight.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus), BorderLayout.EAST);
		return myPanel;
	}
}
