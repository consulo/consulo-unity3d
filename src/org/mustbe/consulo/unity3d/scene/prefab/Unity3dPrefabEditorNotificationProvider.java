/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.unity3d.scene.prefab;

import java.util.Collections;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.editor.UnityEditorCommunication;
import org.mustbe.consulo.unity3d.editor.UnityOpenScene;
import org.mustbe.consulo.unity3d.scene.Unity3dAssetUtil;
import org.mustbe.consulo.unity3d.scene.Unity3dYMLAssetFileType;
import org.mustbe.consulo.unity3d.scene.index.Unity3dYMLAssetIndexExtension;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Function;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.ui.PlatformColors;
import consulo.annotations.RequiredReadAction;
import consulo.editor.notifications.EditorNotificationProvider;

/**
 * @author VISTALL
 * @since 10.03.2016
 */
public class Unity3dPrefabEditorNotificationProvider implements EditorNotificationProvider<EditorNotificationPanel>
{
	private static final Key<EditorNotificationPanel> ourKey = Key.create("Unity3dPrefabEditorNotificationProvider");

	private Project myProject;

	public Unity3dPrefabEditorNotificationProvider(Project project)
	{
		myProject = project;
	}

	@NotNull
	@Override
	public Key<EditorNotificationPanel> getKey()
	{
		return ourKey;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor)
	{
		if(file.getFileType() != Unity3dYMLAssetFileType.INSTANCE || !"prefab".equals(file.getExtension()))
		{
			return null;
		}
		final String uuid = Unity3dAssetUtil.getUUID(file);
		if(uuid == null)
		{
			return null;
		}
		final VirtualFile[] results = getVirtualFiles(uuid);
		if(results.length == 0)
		{
			return null;
		}
		final EditorNotificationPanel panel = new EditorNotificationPanel()
		{
			{
				HyperlinkLabel label = new HyperlinkLabel("Navigate", PlatformColors.BLUE, getBackground(), PlatformColors.BLUE);
				label.addHyperlinkListener(new HyperlinkAdapter()
				{
					@Override
					protected void hyperlinkActivated(HyperlinkEvent e)
					{
						VirtualFile[] virtualFiles = getVirtualFiles(uuid);
						int size = virtualFiles.length;
						if(size == 1)
						{
							VirtualFile firstElement = ArrayUtil.getFirstElement(virtualFiles);
							assert firstElement != null;
							UnityEditorCommunication.request(myProject, new UnityOpenScene(firstElement.getPath()), false);
						}
						else if(size > 0)
						{
							BaseListPopupStep<VirtualFile> popupStep = new BaseListPopupStep<VirtualFile>("Open scene", results)
							{
								@NotNull
								@Override
								public String getTextFor(VirtualFile value)
								{
									return VfsUtil.getRelativePath(value, myProject.getBaseDir());
								}

								@Override
								public Icon getIconFor(VirtualFile aValue)
								{
									return aValue.getFileType().getIcon();
								}

								@Override
								public PopupStep onChosen(VirtualFile selectedValue, boolean finalChoice)
								{
									UnityEditorCommunication.request(myProject, new UnityOpenScene(selectedValue.getPath()), false);
									return FINAL_CHOICE;
								}
							};
							if(!(e.getSource() instanceof JComponent))
							{
								return;
							}
							JComponent source = (JComponent) e.getSource();
							ListPopup listPopup = JBPopupFactory.getInstance().createListPopup(popupStep);
							listPopup.showUnderneathOf(source);
						}
					}
				});
				myLinksPanel.add(label);
			}
		};
		panel.setText("Used in scenes: " + StringUtil.join(results, new Function<VirtualFile, String>()
		{
			@Override
			public String fun(VirtualFile virtualFile)
			{
				return VfsUtil.getRelativePath(virtualFile, myProject.getBaseDir());
			}
		}, ", "));
		return panel;
	}

	@NotNull
	private VirtualFile[] getVirtualFiles(String uuid)
	{
		GlobalSearchScope filter = GlobalSearchScope.projectScope(myProject);
		CommonProcessors.CollectUniquesProcessor<VirtualFile> processor = new CommonProcessors.CollectUniquesProcessor<VirtualFile>();
		FileBasedIndex.getInstance().processFilesContainingAllKeys(Unity3dYMLAssetIndexExtension.KEY, Collections.singleton(uuid), filter, null, processor);

		return Unity3dAssetUtil.sortAssetFiles(processor.toArray(VirtualFile.EMPTY_ARRAY));
	}
}
