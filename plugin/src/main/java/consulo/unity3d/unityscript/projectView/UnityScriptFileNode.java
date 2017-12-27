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

package consulo.unity3d.unityscript.projectView;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.ide.IconDescriptorUpdaters;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptFileNode extends PsiFileNode
{
	private static final Logger LOGGER = Logger.getInstance(UnityScriptFileNode.class);

	public UnityScriptFileNode(Project project, @NotNull PsiFile value, ViewSettings viewSettings)
	{
		super(project, value, viewSettings);
	}

	@Override
	public void update(final PresentationData data)
	{
		if(!validate())
		{
			return;
		}

		final PsiElement value = extractPsiFromValue();
		LOGGER.assertTrue(value.isValid());

		data.setIcon(IconDescriptorUpdaters.getIcon(getValue(), Iconable.ICON_FLAG_VISIBILITY));
		data.setPresentableText(FileUtil.getNameWithoutExtension(getValue().getName()));
	}
}
