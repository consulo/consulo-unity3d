/*
 * Copyright 2013-2015 must-be.org
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

package consulo.unity3d.shaderlab.lang;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.unity3d.shaderlab.lang.psi.ShaderBraceOwner;
import consulo.unity3d.shaderlab.lang.psi.ShaderRoleOwner;
import consulo.unity3d.shaderlab.lang.psi.SharpLabElementVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabFoldingBuilder extends CustomFoldingBuilder
{
	@Override
	protected void buildLanguageFoldRegions(@NotNull final List<FoldingDescriptor> descriptors,
			@NotNull PsiElement root,
			@NotNull Document document,
			boolean quick)
	{
		root.accept(new SharpLabElementVisitor()
		{
			@Override
			public void visitElement(PsiElement element)
			{
				if(element instanceof ShaderBraceOwner)
				{
					descriptors.add(new FoldingDescriptor(element, element.getTextRange()));
				}
				element.acceptChildren(this);
			}
		});
	}

	@Override
	protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range)
	{
		PsiElement psi = node.getPsi();
		assert psi instanceof ShaderRoleOwner : psi.getClass().getSimpleName();
		PsiElement firstChild = psi.getFirstChild();
		return firstChild.getText();
	}

	@Override
	protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node)
	{
		return false;
	}
}
