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
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Function;
import com.intellij.util.containers.MultiMap;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.scene.Unity3dAssetUtil;
import consulo.unity3d.scene.index.Unity3dYAMLField;
import consulo.unity3d.scene.index.Unity3dYMLAsset;

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
							MultiMap<VirtualFile, Unity3dYMLAsset> files = Unity3dYMLAsset.findAssetAsAttach(type.getProject(), PsiUtilCore.getVirtualFile(type), true);
							if(files.isEmpty())
							{
								return;
							}

							List<Pair<VirtualFile, Unity3dYMLAsset>> list = new ArrayList<>();
							for(Map.Entry<VirtualFile, Collection<Unity3dYMLAsset>> entry : files.entrySet())
							{
								for(Unity3dYMLAsset asset : entry.getValue())
								{
									list.add(Pair.create(entry.getKey(), asset));
								}
							}

							BaseListPopupStep<Pair<VirtualFile, Unity3dYMLAsset>> step = new BaseListPopupStep<Pair<VirtualFile, Unity3dYMLAsset>>("Unity scenes", list)
							{
								@Override
								public Icon getIconFor(Pair<VirtualFile, Unity3dYMLAsset> value)
								{
									return Unity3dIcons.Unity3d;
								}

								@NotNull
								@Override
								public String getTextFor(Pair<VirtualFile, Unity3dYMLAsset> value)
								{
									return "GameObject: " + value.getSecond().getGameObjectName() + " in " + VfsUtil.getRelativePath(value.getFirst(), type.getProject().getBaseDir());
								}

								@Override
								public PopupStep onChosen(Pair<VirtualFile, Unity3dYMLAsset> selectedValue, boolean finalChoice)
								{
									return doFinalStep(() ->
									{
										Project project = element.getProject();
										OpenFileDescriptor descriptor = new OpenFileDescriptor(project, selectedValue.getFirst(), selectedValue.getSecond().getStartOffset());
										FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
									});
								}
							};

							ListPopup popup = JBPopupFactory.getInstance().createListPopup(step);

							popup.show(new RelativePoint(mouseEvent));
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
	Field(CSharpFieldDeclaration.class, AllIcons.General.OverridingMethod)
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

							String name = field.getName();

							List<Trinity<VirtualFile, Unity3dYMLAsset, Unity3dYAMLField>> list = new ArrayList<>();
							for(Map.Entry<VirtualFile, Collection<Unity3dYMLAsset>> entry : files.entrySet())
							{
								for(Unity3dYMLAsset asset : entry.getValue())
								{
									for(Unity3dYAMLField yamlField : asset.getValues())
									{
										if(Comparing.equal(yamlField.getName(), name))
										{
											list.add(Trinity.create(entry.getKey(), asset, yamlField));
										}
									}
								}
							}

							BaseListPopupStep<Trinity<VirtualFile, Unity3dYMLAsset, Unity3dYAMLField>> step = new BaseListPopupStep<Trinity<VirtualFile, Unity3dYMLAsset, Unity3dYAMLField>>("Scene "
									+ "field initializer", list)
							{
								@Override
								public Icon getIconFor(Trinity<VirtualFile, Unity3dYMLAsset, Unity3dYAMLField> value)
								{
									return Unity3dIcons.Shader;
								}

								@NotNull
								@Override
								public String getTextFor(Trinity<VirtualFile, Unity3dYMLAsset, Unity3dYAMLField> value)
								{
									return "Value '" + value.getThird().getValue() + "' at " + value.getSecond().getGameObjectName() + " in " + VfsUtil.getRelativePath(value.getFirst(), field
											.getProject().getBaseDir());
								}

								@Override
								public PopupStep onChosen(Trinity<VirtualFile, Unity3dYMLAsset, Unity3dYAMLField> selectedValue, boolean finalChoice)
								{
									return doFinalStep(() ->
									{
										Project project = element.getProject();
										OpenFileDescriptor descriptor = new OpenFileDescriptor(project, selectedValue.getFirst(), selectedValue.getThird().getOffset());
										FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
									});
								}
							};

							ListPopup popup = JBPopupFactory.getInstance().createListPopup(step);

							popup.show(new RelativePoint(mouseEvent));
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
							for(Unity3dYAMLField yamlField : unity3dYMLAsset.getValues())
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
