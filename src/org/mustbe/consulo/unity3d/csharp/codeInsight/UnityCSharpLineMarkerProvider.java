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

package org.mustbe.consulo.unity3d.csharp.codeInsight;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.Unity3dMetaFileType;
import org.mustbe.consulo.unity3d.Unity3dTypes;
import org.mustbe.consulo.unity3d.csharp.UnityFunctionManager;
import org.mustbe.consulo.unity3d.editor.UnitySceneFile;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
import org.mustbe.consulo.unity3d.scene.index.Unity3dYMLSceneIndexExtension;
import org.yaml.snakeyaml.Yaml;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ConstantFunction;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;

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
			String uuid = getUUID(PsiUtilCore.getVirtualFile(typeDeclaration));
			if(uuid == null)
			{
				return null;
			}
			GlobalSearchScope filter = GlobalSearchScope.projectScope(typeDeclaration.getProject());
			CommonProcessors.FindFirstProcessor<VirtualFile> processor = new CommonProcessors.FindFirstProcessor<VirtualFile>();
			FileBasedIndex.getInstance().processFilesContainingAllKeys(Unity3dYMLSceneIndexExtension.KEY, Collections.singleton(uuid), filter, null, processor);

			if(processor.isFound())
			{
				return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), Unity3dIcons.Unity3dLineMarker, Pass.UPDATE_OVERRIDEN_MARKERS, new Function<PsiElement, String>()
				{
					@Override
					public String fun(final PsiElement element)
					{
						CSharpTypeDeclaration typeDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
						if(typeDeclaration != null)
						{
							String uuid = getUUID(PsiUtilCore.getVirtualFile(typeDeclaration));
							if(uuid == null)
							{
								return "";
							}

							Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance().getContainingFiles(Unity3dYMLSceneIndexExtension.KEY, uuid, GlobalSearchScope.projectScope(typeDeclaration.getProject()));

							String text = "Imported in scene(s): ";
							text += StringUtil.join(containingFiles, new Function<VirtualFile, String>()
							{
								@Override
								public String fun(VirtualFile virtualFile)
								{
									return FileUtil.toSystemDependentName(virtualFile.getPath());
								}
							}, "\n");
							return text;
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
							String uuid = getUUID(PsiUtilCore.getVirtualFile(typeDeclaration));
							if(uuid == null)
							{
								return;
							}

							Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance().getContainingFiles(Unity3dYMLSceneIndexExtension.KEY, uuid, GlobalSearchScope.projectScope(typeDeclaration.getProject()));

							List<UnitySceneFile> map = ContainerUtil.map(containingFiles, new Function<VirtualFile, UnitySceneFile>()
							{
								@Override
								@RequiredReadAction
								public UnitySceneFile fun(VirtualFile virtualFile)
								{
									return new UnitySceneFile(element.getProject(), virtualFile);
								}
							});

							PsiElementListNavigator.openTargets(e, map.toArray(new NavigatablePsiElement[0]), "View Unity scenes", "View Unity scenes", new DefaultPsiElementCellRenderer()
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

	@Nullable
	private static String getUUID(VirtualFile virtualFile)
	{
		if(virtualFile == null)
		{
			return null;
		}
		String name = virtualFile.getName();

		VirtualFile parent = virtualFile.getParent();
		if(parent == null)
		{
			return null;
		}

		VirtualFile child = parent.findChild(name + "." + Unity3dMetaFileType.INSTANCE.getDefaultExtension());
		if(child != null)
		{
			Yaml yaml = new Yaml();
			InputStream inputStream = null;
			try
			{
				inputStream = child.getInputStream();
				Object load = yaml.load(inputStream);
				if(load instanceof Map)
				{
					Object guid = ((Map) load).get("guid");
					if(guid instanceof String)
					{
						return (String) guid;
					}
				}
			}
			catch(IOException e)
			{
				LOGGER.error(e);
			}
			finally
			{
				if(inputStream != null)
				{
					try
					{
						inputStream.close();
					}
					catch(IOException e)
					{
						//
					}
				}
			}
		}
		return null;
	}

	@RequiredReadAction
	private static boolean isEqualParameters(Map<String, DotNetTypeRef> funcParameters, DotNetParameterListOwner parameterListOwner)
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
		for(DotNetTypeRef expectedTypeRef : funcParameters.values())
		{
			DotNetParameter parameter = parameters[i++];

			if(!CSharpTypeUtil.isTypeEqual(parameter.toTypeRef(true), expectedTypeRef, parameter))
			{
				return false;
			}
		}
		return true;
	}
}
