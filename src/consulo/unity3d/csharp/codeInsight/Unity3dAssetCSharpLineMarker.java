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
import javax.swing.JList;
import javax.swing.SwingConstants;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Function;
import com.intellij.util.containers.MultiMap;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightKey;
import consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.scene.Unity3dAssetUtil;
import consulo.unity3d.scene.index.Unity3dYMLAsset;
import consulo.unity3d.scene.index.Unity3dYMLField;

/**
 * @author VISTALL
 * @since 30-Aug-17
 */
public enum Unity3dAssetCSharpLineMarker
{
	Type(CSharpTypeDeclaration.class, Unity3dIcons.Unity3dLineMarker)
			{
				@NotNull
				@Override
				public GutterIconNavigationHandler<PsiElement> createNavigationHandler()
				{
					return (mouseEvent, element) ->
					{
						CSharpTypeDeclaration type = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
						if(type != null)
						{
							MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(type.getProject(), PsiUtilCore.getVirtualFile(type), false);
							if(files.isEmpty())
							{
								return;
							}

							List<UnityAssetWrapper> list = new ArrayList<>();
							for(Map.Entry<VirtualFile, Collection<Unity3dYMLAsset>> entry : files.entrySet())
							{
								for(Unity3dYMLAsset asset : entry.getValue())
								{
									list.add(new UnityAssetWrapper(entry.getKey(), asset, asset.getStartOffset(), null, type.getProject()));
								}
							}

							list.sort((o1, o2) -> StringUtil.naturalCompare(o1.getVirtualFile().getPath(), o2.getVirtualFile().getPath()));

							PsiElementListNavigator.openTargets(mouseEvent, list.toArray(new NavigatablePsiElement[list.size()]), "Unity scenes", null, new UnityListViewRender()
							{
								@Override
								protected ColoredListCellRenderer<UnityAssetWrapper> createLeft()
								{
									return new ColoredListCellRenderer<UnityAssetWrapper>()
									{
										@Override
										protected void customizeCellRenderer(@NotNull JList<? extends UnityAssetWrapper> jList, UnityAssetWrapper unityAssetWrapper, int i, boolean b, boolean b1)
										{
											setIcon(Unity3dIcons.Shader);

											append(unityAssetWrapper.getAsset().getGameObjectName());
										}
									};
								}

								@Override
								protected ColoredListCellRenderer<UnityAssetWrapper> createRight()
								{
									return new ColoredListCellRenderer<UnityAssetWrapper>()
									{
										@Override
										protected void customizeCellRenderer(@NotNull JList<? extends UnityAssetWrapper> jList, UnityAssetWrapper unityAssetWrapper, int i, boolean b, boolean b1)
										{
											String relativePath = VfsUtil.getRelativePath(unityAssetWrapper.getVirtualFile(), type.getProject().getBaseDir());

											append(relativePath, SimpleTextAttributes.GRAY_ATTRIBUTES, 100, SwingConstants.RIGHT);
										}
									};
								}
							});
						}
					};
				}

				@NotNull
				@Override
				public Function<PsiElement, String> createTooltipFunction()
				{
					return element -> "Attached to unity scene. Click for view";
				}

				@RequiredReadAction
				@Override
				public boolean isAvailable(@NotNull PsiElement element)
				{
					if(!Unity3dAssetUtil.isPrimaryType(element))
					{
						return false;
					}

					MultiMap<VirtualFile, Unity3dYMLAsset> temp = Unity3dYMLAsset.findAssetAsAttach(element.getProject(), PsiUtilCore.getVirtualFile(element), true);
					return !temp.isEmpty();
				}
			},
	Field(CSharpFieldDeclaration.class, AllIcons.Actions.New)
			{
				@NotNull
				@Override
				public GutterIconNavigationHandler<PsiElement> createNavigationHandler()
				{
					return (mouseEvent, element) ->
					{
						final CSharpFieldDeclaration field = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpFieldDeclaration.class);
						if(field != null)
						{
							MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(field.getProject(), PsiUtilCore.getVirtualFile(field), false);

							if(files.isEmpty())
							{
								return;
							}

							Project project = field.getProject();
							String name = field.getName();

							List<UnityAssetWrapper> list = new ArrayList<>();
							for(Map.Entry<VirtualFile, Collection<Unity3dYMLAsset>> entry : files.entrySet())
							{
								for(Unity3dYMLAsset asset : entry.getValue())
								{
									for(Unity3dYMLField yamlField : asset.getValues())
									{
										if(Comparing.equal(yamlField.getName(), name))
										{
											list.add(new UnityAssetWrapper(entry.getKey(), asset, yamlField, project));
										}
									}
								}
							}

							list.sort((o1, o2) -> StringUtil.naturalCompare(o1.getField().getValue(), o2.getField().getValue()));

							NavigatablePsiElement[] ts = list.toArray(new NavigatablePsiElement[list.size()]);
							PsiElementListNavigator.openTargets(mouseEvent, ts, "Scene field initialize", null, new UnityListViewRender()
							{
								private String[] myNumberTypes = new String[]{
										DotNetTypes.System.Byte,
										DotNetTypes.System.SByte,
										DotNetTypes.System.Int16,
										DotNetTypes.System.UInt16,
										DotNetTypes.System.Int32,
										DotNetTypes.System.UInt32,
										DotNetTypes.System.Int64,
										DotNetTypes.System.UInt64,
										DotNetTypes.System.Decimal,
										DotNetTypes.System.Single,
										DotNetTypes.System.Double,
								};

								@Override
								protected ColoredListCellRenderer<UnityAssetWrapper> createLeft()
								{
									return new ColoredListCellRenderer<UnityAssetWrapper>()
									{
										@Override
										@RequiredDispatchThread
										protected void customizeCellRenderer(@NotNull JList<? extends UnityAssetWrapper> jList, UnityAssetWrapper unityAssetWrapper, int i, boolean b, boolean b1)
										{
											setIcon(Unity3dIcons.Shader);

											TextAttributesKey key = null;
											DotNetTypeRef typeRef = field.toTypeRef(true);
											if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, field, DotNetTypes.System.String))
											{
												key = CSharpHighlightKey.STRING;
											}

											if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, field, DotNetTypes.System.Char))
											{
												key = CSharpHighlightKey.STRING;
											}

											for(String numberType : myNumberTypes)
											{
												if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, field, numberType))
												{
													key = CSharpHighlightKey.NUMBER;
													break;
												}
											}

											SimpleTextAttributes textAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
											if(key != null)
											{
												textAttributes = SimpleTextAttributes.fromTextAttributes(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(key));
											}

											append(unityAssetWrapper.getField().getValue(), textAttributes);

											append(" " + unityAssetWrapper.getAsset().getGameObjectName(), SimpleTextAttributes.GRAY_ATTRIBUTES);
										}
									};
								}

								@Override
								protected ColoredListCellRenderer<UnityAssetWrapper> createRight()
								{
									return new ColoredListCellRenderer<UnityAssetWrapper>()
									{
										@Override
										protected void customizeCellRenderer(@NotNull JList<? extends UnityAssetWrapper> jList, UnityAssetWrapper unityAssetWrapper, int i, boolean b, boolean b1)
										{
											String relativePath = VfsUtil.getRelativePath(unityAssetWrapper.getVirtualFile(), field.getProject().getBaseDir());

											append(relativePath, SimpleTextAttributes.GRAY_ATTRIBUTES, 100, SwingConstants.RIGHT);
										}
									};
								}
							});
						}

					};
				}

				@NotNull
				@Override
				public Function<PsiElement, String> createTooltipFunction()
				{
					return element -> "Scene view initialize. Click for view";
				}

				@RequiredReadAction
				@Override
				public boolean isAvailable(@NotNull PsiElement element)
				{
					CSharpFieldDeclaration field = (CSharpFieldDeclaration) element;
					if(!Unity3dAssetUtil.isPrimaryType(field.getParent()))
					{
						return false;
					}

					MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(field.getProject(), PsiUtilCore.getVirtualFile(field), false);

					if(files.isEmpty())
					{
						return false;
					}

					String name = field.getName();

					for(Map.Entry<VirtualFile, Collection<Unity3dYMLAsset>> entry : files.entrySet())
					{
						for(Unity3dYMLAsset unity3dYMLAsset : entry.getValue())
						{
							for(Unity3dYMLField yamlField : unity3dYMLAsset.getValues())
							{
								if(yamlField.getName().equals(name))
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

	@RequiredReadAction
	public abstract boolean isAvailable(@NotNull PsiElement element);
}
