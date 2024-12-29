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
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.csharp.impl.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.lang.CSharpLanguage;
import consulo.language.Language;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProviderDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.scene.Unity3dAssetUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 02-May-17
 */
@ExtensionImpl
public class UnitySceneCSharpTypeLineMarkerProvider extends LineMarkerProviderDescriptor
{
	@Nullable
	@Override
	public String getName()
	{
		return "Unity C# Scene Component";
	}

	@Nonnull
	public Image getIcon()
	{
		return Unity3dIcons.Unity3dLineMarker;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public LineMarkerInfo getLineMarkerInfo(@Nonnull PsiElement element)
	{
		for(Unity3dAssetCSharpLineMarker marker : Unity3dAssetCSharpLineMarker.values())
		{
			Class<? extends PsiElement> clazz = marker.getElementClass();

			PsiElement declaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, clazz);
			if(declaration != null)
			{
				String uuid = Unity3dAssetUtil.getGUID(element.getProject(), PsiUtilCore.getVirtualFile(declaration));
				if(uuid == null )
				{
					return null;
				}

				if(marker.isAvailable(declaration))
				{
					return new LineMarkerInfo<>(element, element.getTextRange(), marker.getIcon(), Pass.LINE_MARKERS, marker.createTooltipFunction(), marker.createNavigationHandler(),
							GutterIconRenderer.Alignment.LEFT);
				}
			}
		}
		return null;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
