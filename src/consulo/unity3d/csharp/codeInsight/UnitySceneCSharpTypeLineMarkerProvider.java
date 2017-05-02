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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.indexing.FileBasedIndex;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.editor.UnitySceneFile;
import consulo.unity3d.scene.Unity3dAssetUtil;
import consulo.unity3d.scene.index.Unity3dYMLAssetIndexExtension;

/**
 * @author VISTALL
 * @since 02-May-17
 */
public class UnitySceneCSharpTypeLineMarkerProvider extends LineMarkerProviderDescriptor
{
	@Nullable
	@Override
	public String getName()
	{
		return "Unity C# Scene Component";
	}

	@NotNull
	public Icon getIcon()
	{
		return Unity3dIcons.Unity3dLineMarker;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element)
	{
		CSharpTypeDeclaration typeDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
		if(typeDeclaration != null)
		{
			String uuid = Unity3dAssetUtil.getUUID(PsiUtilCore.getVirtualFile(typeDeclaration));
			if(uuid == null || typeDeclaration.isNested())
			{
				return null;
			}
			GlobalSearchScope filter = GlobalSearchScope.projectScope(typeDeclaration.getProject());
			CommonProcessors.FindFirstProcessor<VirtualFile> processor = new CommonProcessors.FindFirstProcessor<>();
			FileBasedIndex.getInstance().processFilesContainingAllKeys(Unity3dYMLAssetIndexExtension.KEY, Collections.singleton(uuid), filter, null, processor);

			if(processor.isFound())
			{
				return new LineMarkerInfo<>(element, element.getTextRange(), Unity3dIcons.Unity3dLineMarker, Pass.LINE_MARKERS, element12 ->
				{
					final CSharpTypeDeclaration typeDeclaration12 = CSharpLineMarkerUtil.getNameIdentifierAs(element12, CSharpTypeDeclaration.class);
					if(typeDeclaration12 != null)
					{
						String uuid12 = Unity3dAssetUtil.getUUID(PsiUtilCore.getVirtualFile(typeDeclaration12));
						if(uuid12 == null)
						{
							return "";
						}

						Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance().getContainingFiles(Unity3dYMLAssetIndexExtension.KEY, uuid12, GlobalSearchScope.projectScope
								(typeDeclaration12.getProject()));

						MultiMap<String, String> map = MultiMap.create();
						for(VirtualFile file : containingFiles)
						{
							map.putValue(file.getExtension(), VfsUtil.getRelativePath(file, typeDeclaration12.getProject().getBaseDir()));
						}

						StringBuilder builder = new StringBuilder();
						boolean first = true;
						for(Map.Entry<String, Collection<String>> entry : map.entrySet())
						{
							String text = "";
							if(!first)
							{
								text = "<br>";
							}
							else
							{
								first = false;
							}
							text += "<b>Imported in *." + entry.getKey() + ":</b><br>";

							List<String> items = new ArrayList<>(entry.getValue());
							ContainerUtil.sort(items);

							List<String> firstItems = ContainerUtil.getFirstItems(items, 10);
							if(firstItems.size() != items.size())
							{
								firstItems.add("<b>... " + (items.size() - firstItems.size()) + " others.</b>");
							}
							text += StringUtil.join(firstItems, s -> " > " + s, "<br>");
							builder.append(text);
						}
						return builder.toString();
					}
					return "";
				}, (e, elt) ->
				{
					CSharpTypeDeclaration typeDeclaration1 = CSharpLineMarkerUtil.getNameIdentifierAs(elt, CSharpTypeDeclaration.class);
					if(typeDeclaration1 != null)
					{
						String uuid1 = Unity3dAssetUtil.getUUID(PsiUtilCore.getVirtualFile(typeDeclaration1));
						if(uuid1 == null)
						{
							return;
						}

						Collection<VirtualFile> temp = FileBasedIndex.getInstance().getContainingFiles(Unity3dYMLAssetIndexExtension.KEY, uuid1, GlobalSearchScope.projectScope(typeDeclaration1
								.getProject()));

						VirtualFile[] assetFiles = temp.toArray(new VirtualFile[temp.size()]);
						assetFiles = Unity3dAssetUtil.sortAssetFiles(assetFiles);

						List<UnitySceneFile> map = ContainerUtil.map(assetFiles, virtualFile -> new UnitySceneFile(element.getProject(), virtualFile));

						PsiElementListNavigator.openTargets(e, map.toArray(new NavigatablePsiElement[0]), "View Unity assets", "View Unity " + "assets", new DefaultPsiElementCellRenderer()
						{
							@Override
							protected Icon getIcon(PsiElement element1)
							{
								return ((NavigatablePsiElement) element1).getPresentation().getIcon(false);
							}
						});
					}
				}, GutterIconRenderer.Alignment.LEFT);
			}
		}
		return null;
	}
}
