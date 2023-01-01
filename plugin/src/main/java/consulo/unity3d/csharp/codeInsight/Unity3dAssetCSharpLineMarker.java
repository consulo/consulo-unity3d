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

import consulo.annotation.access.RequiredReadAction;
import consulo.application.AllIcons;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.TextAttributesKey;
import consulo.csharp.impl.ide.highlight.CSharpHighlightKey;
import consulo.csharp.impl.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.ui.PsiElementListNavigator;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.awt.ColoredListCellRenderer;
import consulo.ui.ex.util.TextAttributesUtil;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.scene.Unity3dAssetUtil;
import consulo.unity3d.scene.Unity3dMetaManager;
import consulo.unity3d.scene.index.Unity3dYMLAsset;
import consulo.unity3d.scene.index.Unity3dYMLAssetIndexExtension;
import consulo.unity3d.scene.index.Unity3dYMLField;
import consulo.unity3d.shaderlab.icon.ShaderLabIconGroup;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.MultiMap;
import consulo.util.lang.Comparing;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 30-Aug-17
 */
public enum Unity3dAssetCSharpLineMarker
{
	Type(CSharpTypeDeclaration.class, Unity3dIcons.Unity3dLineMarker)
			{
				@Nonnull
				@Override
				public GutterIconNavigationHandler<PsiElement> createNavigationHandler()
				{
					return (mouseEvent, element) ->
					{
						CSharpTypeDeclaration type = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
						if(type != null)
						{
							MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(type.getProject(), PsiUtilCore.getVirtualFile(type));
							if(files.isEmpty())
							{
								return;
							}

							List<UnityAssetWrapper> list = new ArrayList<>();
							for(Map.Entry<VirtualFile, Collection<Unity3dYMLAsset>> entry : files.entrySet())
							{
								for(Unity3dYMLAsset asset : entry.getValue())
								{
									String gameObjectName = asset.getGameObjectName();
									if(gameObjectName == null)
									{
										continue;
									}

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
										protected void customizeCellRenderer(@Nonnull JList<? extends UnityAssetWrapper> jList, UnityAssetWrapper unityAssetWrapper, int i, boolean b, boolean b1)
										{
											setIcon(ShaderLabIconGroup.shader());

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
										protected void customizeCellRenderer(@Nonnull JList<? extends UnityAssetWrapper> jList, UnityAssetWrapper unityAssetWrapper, int i, boolean b, boolean b1)
										{
											String relativePath = VirtualFileUtil.getRelativePath(unityAssetWrapper.getVirtualFile(), type.getProject().getBaseDir());

											append(relativePath, SimpleTextAttributes.GRAY_ATTRIBUTES);
										}
									};
								}
							});
						}
					};
				}

				@Nonnull
				@Override
				public Function<PsiElement, String> createTooltipFunction()
				{
					return element -> "Attached to unity scene. Click for view";
				}

				@RequiredReadAction
				@Override
				public boolean isAvailable(@Nonnull PsiElement element)
				{
					if(!Unity3dAssetUtil.isPrimaryType(element))
					{
						return false;
					}

					MultiMap<VirtualFile, Unity3dYMLAsset> temp = Unity3dYMLAsset.findAssetAsAttach(element.getProject(), PsiUtilCore.getVirtualFile(element));
					return !temp.isEmpty();
				}
			},
	Field(CSharpFieldDeclaration.class, AllIcons.Gutter.WriteAccess)
			{
				@Nonnull
				@Override
				public GutterIconNavigationHandler<PsiElement> createNavigationHandler()
				{
					return (mouseEvent, element) ->
					{
						final CSharpFieldDeclaration field = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpFieldDeclaration.class);
						if(field != null)
						{
							MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(field.getProject(), PsiUtilCore.getVirtualFile(field));

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
										@RequiredUIAccess
										protected void customizeCellRenderer(@Nonnull JList<? extends UnityAssetWrapper> jList, UnityAssetWrapper unityAssetWrapper, int i, boolean b, boolean b1)
										{
											setIcon(ShaderLabIconGroup.shader());

											String prefix = null;
											TextAttributesKey prefixKey = null;
											String value = unityAssetWrapper.getField().getValue();

											TextAttributesKey key = null;
											DotNetTypeRef typeRef = field.toTypeRef(true);

											Pair<String, DotNetTypeDeclaration> typeElement = CSharpTypeUtil.resolveTypeElement(typeRef);
											if(typeElement != null)
											{
												if(DotNetTypes.System.String.equals(typeElement.getFirst()))
												{
													key = CSharpHighlightKey.STRING;
												}

												if(DotNetTypes.System.Char.equals(typeElement.getFirst()))
												{
													key = CSharpHighlightKey.STRING;
												}

												if(ArrayUtil.contains(typeElement.getFirst(), myNumberTypes))
												{
													key = CSharpHighlightKey.NUMBER;
												}

												if(DotNetTypes.System.Boolean.equals(typeElement.getFirst()))
												{
													key = CSharpHighlightKey.KEYWORD;
													value = String.valueOf(StringUtil.parseInt(value, 0) == 1);
												}

												DotNetTypeDeclaration typeDeclaration = typeElement.getSecond();
												if(typeDeclaration.isEnum())
												{
													int index = StringUtil.parseInt(value, 0);

													CSharpEnumConstantDeclaration[] constants = PsiTreeUtil.getChildrenOfType(typeDeclaration, CSharpEnumConstantDeclaration.class);

													CSharpEnumConstantDeclaration declaration = ArrayUtil2.safeGet(constants, index);
													value = declaration == null ? value : declaration.getName();
													if(declaration != null)
													{
														prefix = typeDeclaration.getPresentableQName() + ".";
														key = CSharpHighlightKey.CONSTANT;
													}
												}

												if(key == null && typeDeclaration.isStruct())
												{
													prefix = "new ";
													prefixKey = CSharpHighlightKey.KEYWORD;

													value = typeDeclaration.getPresentableQName() + "()" + value;
												}
											}

											SimpleTextAttributes textAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
											SimpleTextAttributes prefixAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
											if(key != null)
											{
												textAttributes = TextAttributesUtil.fromTextAttributes(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(key));
											}

											if(prefixKey != null)
											{
												prefixAttributes = TextAttributesUtil.fromTextAttributes(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(prefixKey));
											}

											if(value.startsWith(Unity3dYMLAssetIndexExtension.ourCustomGUIDPrefix))
											{
												String guid = value.substring(Unity3dYMLAssetIndexExtension.ourCustomGUIDPrefix.length(), value.length());

												VirtualFile fileByGUID = Unity3dMetaManager.getInstance(project).findFileByGUID(guid);
												if(fileByGUID != null)
												{
													value = VirtualFileUtil.getRelativePath(fileByGUID, field.getProject().getBaseDir());
												}
												else
												{
													value = "guid: " + guid;
												}
											}

											if(prefix != null)
											{
												append(prefix, prefixAttributes);
											}

											append(value, textAttributes);

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
										protected void customizeCellRenderer(@Nonnull JList<? extends UnityAssetWrapper> jList, UnityAssetWrapper unityAssetWrapper, int i, boolean b, boolean b1)
										{
											String relativePath = VirtualFileUtil.getRelativePath(unityAssetWrapper.getVirtualFile(), field.getProject().getBaseDir());

											append(relativePath, SimpleTextAttributes.GRAY_ATTRIBUTES);
										}
									};
								}
							});
						}

					};
				}

				@Nonnull
				@Override
				public Function<PsiElement, String> createTooltipFunction()
				{
					return element -> "Scene view initialize. Click for view";
				}

				@RequiredReadAction
				@Override
				public boolean isAvailable(@Nonnull PsiElement element)
				{
					CSharpFieldDeclaration field = (CSharpFieldDeclaration) element;
					if(!Unity3dAssetUtil.isPrimaryType(field.getParent()))
					{
						return false;
					}

					MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(field.getProject(), PsiUtilCore.getVirtualFile(field));

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
	private final Image myIcon;

	private Unity3dAssetCSharpLineMarker(Class<? extends PsiElement> elementClass, Image icon)
	{
		myElementClass = elementClass;
		myIcon = icon;
	}

	@Nonnull
	public Image getIcon()
	{
		return myIcon;
	}

	@Nonnull
	public Class<? extends PsiElement> getElementClass()
	{
		return myElementClass;
	}

	@Nonnull
	public abstract GutterIconNavigationHandler<PsiElement> createNavigationHandler();

	@Nonnull
	public abstract Function<PsiElement, String> createTooltipFunction();

	@RequiredReadAction
	public abstract boolean isAvailable(@Nonnull PsiElement element);
}
