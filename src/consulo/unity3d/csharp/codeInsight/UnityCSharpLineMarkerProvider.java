/*
 * Copyright 2013-2014 must-be.org
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

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.Unity3dTypes;
import consulo.unity3d.csharp.UnityFunctionManager;
import consulo.unity3d.editor.UnitySceneFile;
import consulo.unity3d.module.Unity3dModuleExtension;
import consulo.unity3d.scene.Unity3dAssetUtil;
import consulo.unity3d.scene.index.Unity3dYMLAssetIndexExtension;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ConstantFunction;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.indexing.FileBasedIndex;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.lombok.annotations.Logger;

/**
 * @author VISTALL
 * @since 19.12.14
 */
@Logger
public class UnityCSharpLineMarkerProvider implements LineMarkerProvider
{
	@RequiredReadAction
	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element)
	{
		return null;
	}

	@Override
	@RequiredReadAction
	public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result)
	{
		for(PsiElement element : elements)
		{
			LineMarkerInfo marker = createMarker(element);
			if(marker != null)
			{
				result.add(marker);
			}
		}
	}

	@Nullable
	@RequiredReadAction
	private static LineMarkerInfo createMarker(final PsiElement element)
	{
		CSharpMethodDeclaration methodDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpMethodDeclaration.class);
		if(methodDeclaration != null)
		{
			UnityFunctionManager.FunctionInfo functionInfo = UnityFunctionManager.getInstance().getFunctionInfo(element.getText());
			if(functionInfo == null)
			{
				return null;
			}
			Unity3dModuleExtension extension = ModuleUtilCore.getExtension(element, Unity3dModuleExtension.class);
			if(extension == null)
			{
				return null;
			}
			PsiElement maybeTypeDeclaration = methodDeclaration.getParent();
			if(maybeTypeDeclaration instanceof CSharpTypeDeclaration && DotNetInheritUtil.isParent(Unity3dTypes.UnityEngine.MonoBehaviour, (DotNetTypeDeclaration) maybeTypeDeclaration, true))
			{
				if(!isEqualParameters(functionInfo.getParameters(), methodDeclaration))
				{
					return null;
				}

				return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), Unity3dIcons.EventMethod, Pass.UPDATE_OVERRIDEN_MARKERS, new ConstantFunction<PsiElement,
						String>(functionInfo.getDescription()), null, GutterIconRenderer.Alignment.LEFT);
			}
		}

		CSharpTypeDeclaration typeDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
		if(typeDeclaration != null)
		{
			String uuid = Unity3dAssetUtil.getUUID(PsiUtilCore.getVirtualFile(typeDeclaration));
			if(uuid == null || typeDeclaration.isNested())
			{
				return null;
			}
			GlobalSearchScope filter = GlobalSearchScope.projectScope(typeDeclaration.getProject());
			CommonProcessors.FindFirstProcessor<VirtualFile> processor = new CommonProcessors.FindFirstProcessor<VirtualFile>();
			FileBasedIndex.getInstance().processFilesContainingAllKeys(Unity3dYMLAssetIndexExtension.KEY, Collections.singleton(uuid), filter, null, processor);

			if(processor.isFound())
			{
				return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), Unity3dIcons.Unity3dLineMarker, Pass.UPDATE_OVERRIDEN_MARKERS, new Function<PsiElement, String>()
				{
					@Override
					public String fun(final PsiElement element)
					{
						final CSharpTypeDeclaration typeDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
						if(typeDeclaration != null)
						{
							String uuid = Unity3dAssetUtil.getUUID(PsiUtilCore.getVirtualFile(typeDeclaration));
							if(uuid == null)
							{
								return "";
							}

							Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance().getContainingFiles(Unity3dYMLAssetIndexExtension.KEY, uuid,
									GlobalSearchScope.projectScope(typeDeclaration.getProject()));

							MultiMap<String, String> map = MultiMap.create();
							for(VirtualFile file : containingFiles)
							{
								map.putValue(file.getExtension(), VfsUtil.getRelativePath(file, typeDeclaration.getProject().getBaseDir()));
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

								List<String> items = new ArrayList<String>(entry.getValue());
								ContainerUtil.sort(items);

								List<String> firstItems = ContainerUtil.getFirstItems(items, 10);
								if(firstItems.size() != items.size())
								{
									firstItems.add("<b>... " + (items.size() - firstItems.size()) + " others.</b>");
								}
								text += StringUtil.join(firstItems, new Function<String, String>()
								{
									@Override
									public String fun(String s)
									{
										return " > " + s;
									}
								}, "<br>");
								builder.append(text);
							}
							return builder.toString();
						}
						return "";
					}
				}, new GutterIconNavigationHandler<PsiElement>()

				{
					@Override
					public void navigate(MouseEvent e, PsiElement elt)
					{
						CSharpTypeDeclaration typeDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(elt, CSharpTypeDeclaration.class);
						if(typeDeclaration != null)
						{
							String uuid = Unity3dAssetUtil.getUUID(PsiUtilCore.getVirtualFile(typeDeclaration));
							if(uuid == null)
							{
								return;
							}

							Collection<VirtualFile> temp = FileBasedIndex.getInstance().getContainingFiles(Unity3dYMLAssetIndexExtension.KEY, uuid,
									GlobalSearchScope.projectScope(typeDeclaration.getProject()));

							VirtualFile[] assetFiles = temp.toArray(new VirtualFile[temp.size()]);
							assetFiles = Unity3dAssetUtil.sortAssetFiles(assetFiles);

							List<UnitySceneFile> map = ContainerUtil.map(assetFiles, new Function<VirtualFile, UnitySceneFile>()
							{
								@Override
								@RequiredReadAction
								public UnitySceneFile fun(VirtualFile virtualFile)
								{
									return new UnitySceneFile(element.getProject(), virtualFile);
								}
							});

							PsiElementListNavigator.openTargets(e, map.toArray(new NavigatablePsiElement[0]), "View Unity assets", "View Unity assets", new DefaultPsiElementCellRenderer()
							{
								@Override
								protected Icon getIcon(PsiElement element)
								{
									return ((NavigatablePsiElement) element).getPresentation().getIcon(false);
								}
							});
						}
					}
				}, GutterIconRenderer.Alignment.LEFT
				);
			}
		}
		return null;
	}

	@RequiredReadAction
	private static boolean isEqualParameters(Map<String, String> funcParameters, DotNetParameterListOwner parameterListOwner)
	{
		DotNetParameter[] parameters = parameterListOwner.getParameters();
		if(parameters.length == 0)
		{
			return true;
		}
		if(parameters.length != funcParameters.size())
		{
			return false;
		}

		int i = 0;
		for(String expectedType : funcParameters.values())
		{
			DotNetParameter parameter = parameters[i++];

			DotNetTypeRef typeRef = UnityFunctionManager.createTypeRef(parameter, expectedType);
			if(!CSharpTypeUtil.isTypeEqual(parameter.toTypeRef(true), typeRef, parameter))
			{
				return false;
			}
		}
		return true;
	}
}
