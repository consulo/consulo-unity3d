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

package consulo.unity3d.scene.prefab;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.ui.PlatformColors;
import consulo.annotations.RequiredReadAction;
import consulo.editor.notifications.EditorNotificationProvider;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.editor.UnityOpenScene;
import consulo.unity3d.scene.Unity3dAssetUtil;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;
import consulo.unity3d.scene.index.Unity3dYMLAsset;

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
		final String uuid = Unity3dAssetUtil.getGUID(myProject, file);
		if(uuid == null)
		{
			return null;
		}
		final VirtualFile[] results = getVirtualFiles(file);
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
						VirtualFile[] virtualFiles = getVirtualFiles(file);
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
		panel.setText("Used in scenes: " + StringUtil.join(results, virtualFile -> VfsUtil.getRelativePath(virtualFile, myProject.getBaseDir()), ", "));
		return panel;
	}

	@NotNull
	private VirtualFile[] getVirtualFiles(VirtualFile file)
	{
		MultiMap<VirtualFile, Unity3dYMLAsset> map = Unity3dYMLAsset.findAssetAsAttach(myProject, file, false);
		return VfsUtil.toVirtualFileArray(map.keySet());
	}
}
