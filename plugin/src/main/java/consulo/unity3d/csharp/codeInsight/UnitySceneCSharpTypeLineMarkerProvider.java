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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.scene.Unity3dAssetUtil;

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
}
