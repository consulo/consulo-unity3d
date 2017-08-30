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
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.ui.UIUtil;
import consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.editor.UnitySceneFile;
import consulo.unity3d.scene.Unity3dAssetUtil;
import consulo.unity3d.scene.index.Unity3dYMLAsset;

/**
 * @author VISTALL
 * @since 30-Aug-17
 */
public enum Unity3dAssetCSharpLineMarker
{
	Type(CSharpTypeDeclaration.class, Unity3dIcons.Unity3dLineMarker)
			{
				@Override
				public boolean needSkip(@NotNull PsiElement element)
				{
					return ((CSharpTypeDeclaration) element).isNested();
				}

				@NotNull
				@Override
				public GutterIconNavigationHandler<PsiElement> createNavigationHandler()
				{
					return (mouseEvent, element) ->
					{
						CSharpTypeDeclaration type = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
						if(type != null)
						{
							MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(PsiUtilCore.getVirtualFile(type), type.getProject(), true);
							if(files.isEmpty())
							{
								return;
							}

							VirtualFile[] assetFiles = Unity3dAssetUtil.sortAssetFiles(VfsUtil.toVirtualFileArray(files.keySet()));

							List<UnitySceneFile> map = ContainerUtil.map(assetFiles, virtualFile -> new UnitySceneFile(type.getProject(), virtualFile));

							PsiElementListNavigator.openTargets(mouseEvent, map.toArray(new NavigatablePsiElement[0]), "View Unity assets", "View Unity assets", new DefaultPsiElementCellRenderer()
							{
								@Override
								protected Icon getIcon(PsiElement element1)
								{
									return ((NavigatablePsiElement) element1).getPresentation().getIcon(false);
								}
							});
						}
					};
				}

				@NotNull
				@Override
				public Function<PsiElement, String> createTooltipFunction()
				{
					return element ->
					{
						final CSharpTypeDeclaration mirror = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
						if(mirror != null)
						{
							MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(PsiUtilCore.getVirtualFile(mirror), mirror.getProject(), true);

							if(files.isEmpty())
							{
								return "";
							}

							MultiMap<String, String> map = MultiMap.create();
							for(VirtualFile file : files.keySet())
							{
								map.putValue(file.getExtension(), VfsUtil.getRelativePath(file, mirror.getProject().getBaseDir()));
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
					};
				}

				@Override
				public boolean isAvailable(@NotNull PsiElement element)
				{
					MultiMap<VirtualFile, Unity3dYMLAsset> temp = Unity3dYMLAsset.findAssetAsAttach(PsiUtilCore.getVirtualFile(element), element.getProject(), true);
					return !temp.isEmpty();
				}
			},
	Field(CSharpFieldDeclaration.class, AllIcons.General.OverridingMethod)
			{
				@NotNull
				@Override
				public GutterIconNavigationHandler<PsiElement> createNavigationHandler()
				{
					return (mouseEvent, element) ->
					{
						CSharpFieldDeclaration field = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpFieldDeclaration.class);
						if(field != null)
						{
							PsiElement nameIdentifier = ((CSharpTypeDeclaration) field.getParent()).getNameIdentifier();
							Unity3dAssetCSharpLineMarker.Type.createNavigationHandler().navigate(mouseEvent, nameIdentifier.getFirstChild());
						}
					};
				}

				@NotNull
				@Override
				public Function<PsiElement, String> createTooltipFunction()
				{
					return element ->
					{
						final CSharpFieldDeclaration field = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpFieldDeclaration.class);
						if(field != null)
						{
							MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(PsiUtilCore.getVirtualFile(field), field.getProject(), true);

							if(files.isEmpty())
							{
								return "";
							}

							String name = field.getName();

							MultiMap<String, String> valueMap = MultiMap.create();
							for(Map.Entry<VirtualFile, Collection<Unity3dYMLAsset>> entry : files.entrySet())
							{
								VirtualFile key = entry.getKey();
								Collection<Unity3dYMLAsset> value = entry.getValue();

								String relativePath = VfsUtil.getRelativePath(key, field.getProject().getBaseDir());

								for(Unity3dYMLAsset unity3dYMLAsset : value)
								{
									for(Couple<String> couple : unity3dYMLAsset.getValues())
									{
										if(couple.getFirst().equals(name))
										{
											valueMap.putValue(relativePath, couple.getSecond());
										}
									}
								}
							}

							if(valueMap.isEmpty())
							{
								return "";
							}

							StringBuilder builder = new StringBuilder();
							builder.append("<b>Scene value initialize:</b><br>");

							boolean first = true;
							for(Map.Entry<String, Collection<String>> entry : valueMap.entrySet())
							{
								for(String value : entry.getValue())
								{
									if(!first)
									{
										builder.append("<br>");
									}
									else
									{
										first = false;
									}

									builder.append("&nbsp;").append(entry.getKey()).append("&nbsp;").append(UIUtil.rightArrow()).append("&nbsp;").append("<code>").append(value).append("</code>");
								}
							}
							return builder.toString();
						}
						return "";
					};
				}

				@Override
				public boolean isAvailable(@NotNull PsiElement element)
				{
					CSharpFieldDeclaration field = (CSharpFieldDeclaration) element;
					MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(PsiUtilCore.getVirtualFile(field), field.getProject(), false);

					if(files.isEmpty())
					{
						return false;
					}

					String name = field.getName();

					for(Map.Entry<VirtualFile, Collection<Unity3dYMLAsset>> entry : files.entrySet())
					{
						for(Unity3dYMLAsset unity3dYMLAsset : entry.getValue())
						{
							for(Couple<String> couple : unity3dYMLAsset.getValues())
							{
								if(couple.getFirst().equals(name))
								{
									return true;
								}
							}
						}
					}

					return false;
				}
			};

	private final Class<? extends PsiElement> myElementClass;
	private final Icon myIcon;

	private Unity3dAssetCSharpLineMarker(Class<? extends PsiElement> elementClass, Icon icon)
	{
		myElementClass = elementClass;
		myIcon = icon;
	}

	@NotNull
	public Icon getIcon()
	{
		return myIcon;
	}

	@NotNull
	public Class<? extends PsiElement> getElementClass()
	{
		return myElementClass;
	}

	@NotNull
	public abstract GutterIconNavigationHandler<PsiElement> createNavigationHandler();

	@NotNull
	public abstract Function<PsiElement, String> createTooltipFunction();

	public abstract boolean isAvailable(@NotNull PsiElement element);

	public boolean needSkip(@NotNull PsiElement element)
	{
		return false;
	}
}
