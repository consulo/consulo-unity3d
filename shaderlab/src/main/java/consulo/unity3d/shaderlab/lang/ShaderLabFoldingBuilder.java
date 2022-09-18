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

package consulo.unity3d.shaderlab.lang;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.folding.CustomFoldingBuilder;
import consulo.language.editor.folding.FoldingDescriptor;
import consulo.language.psi.PsiElement;
import consulo.unity3d.shaderlab.lang.psi.ShaderBraceOwner;
import consulo.unity3d.shaderlab.lang.psi.ShaderRoleOwner;
import consulo.unity3d.shaderlab.lang.psi.SharpLabElementVisitor;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
@ExtensionImpl
public class ShaderLabFoldingBuilder extends CustomFoldingBuilder
{
	@Override
	protected void buildLanguageFoldRegions(@Nonnull final List<FoldingDescriptor> descriptors, @Nonnull PsiElement root, @Nonnull Document document, boolean quick)
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
	protected String getLanguagePlaceholderText(@Nonnull ASTNode node, @Nonnull TextRange range)
	{
		PsiElement psi = node.getPsi();
		assert psi instanceof ShaderRoleOwner : psi.getClass().getSimpleName();
		PsiElement firstChild = psi.getFirstChild();
		return firstChild.getText();
	}

	@Override
	protected boolean isRegionCollapsedByDefault(@Nonnull ASTNode node)
	{
		return false;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return ShaderLabLanguage.INSTANCE;
	}
}
