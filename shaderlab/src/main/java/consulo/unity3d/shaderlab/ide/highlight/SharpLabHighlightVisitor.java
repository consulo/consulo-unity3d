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

package consulo.unity3d.shaderlab.ide.highlight;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.ASTNode;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.editor.rawHighlight.HighlightInfoHolder;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.language.editor.rawHighlight.HighlightVisitor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import consulo.unity3d.shaderlab.lang.psi.*;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class SharpLabHighlightVisitor extends SharpLabElementVisitor implements HighlightVisitor
{
	private HighlightInfoHolder myHolder;

	@Override
	public void visitProperty(ShaderPropertyElement p)
	{
		super.visitProperty(p);

		PsiElement nameIdentifier = p.getNameIdentifier();
		if(nameIdentifier != null)
		{
			myHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(nameIdentifier).textAttributes(DefaultLanguageHighlighterColors.INSTANCE_FIELD).create());
		}
	}

	@Override
	public void visitPropertyType(ShaderPropertyTypeElement type)
	{
		super.visitPropertyType(type);

		PsiElement element = type.getTargetElement();

		ShaderLabPropertyType shaderLabPropertyType = ShaderLabPropertyType.find(element.getText());
		if(shaderLabPropertyType == null)
		{
			myHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.WRONG_REF).range(element).descriptionAndTooltip("Wrong type").create());
		}
		else
		{
			myHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(element).textAttributes(DefaultLanguageHighlighterColors.TYPE_ALIAS_NAME).create());
		}
	}

	@Override
	@RequiredReadAction
	public void visitReference(ShaderReference reference)
	{
		if(!reference.isSoft())
		{
			PsiElement resolve = reference.resolve();
			if(resolve == null)
			{
				myHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.WRONG_REF).range(reference.getReferenceElement()).descriptionAndTooltip("'" + reference.getReferenceName() + "' is not " +
						"resolved").create());
			}
			else
			{
				ShaderReference.ResolveKind kind = reference.kind();
				TextAttributesKey key = null;
				switch(kind)
				{
					case ATTRIBUTE:
						key = DefaultLanguageHighlighterColors.METADATA;
						break;
					case PROPERTY:
						key = DefaultLanguageHighlighterColors.INSTANCE_FIELD;
						break;
					default:
						return;
				}
				myHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(reference.getReferenceElement()).textAttributes(key).create());
			}
		}
	}

	@Override
	public void visitElement(PsiElement element)
	{
		super.visitElement(element);

		ASTNode node = element.getNode();

		if(node != null)
		{
			if(node.getElementType() == ShaderLabKeyTokens.VALUE_KEYWORD)
			{
				myHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(node).textAttributes(DefaultLanguageHighlighterColors.MACRO_KEYWORD).create());
			}
			else if(node.getElementType() == ShaderLabKeyTokens.START_KEYWORD)
			{
				myHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(node).textAttributes(DefaultLanguageHighlighterColors.KEYWORD).create());
			}
		}
	}

	@Override
	public void visit(@Nonnull PsiElement element)
	{
		element.accept(this);
	}

	@Override
	public boolean analyze(@Nonnull PsiFile file, boolean updateWholeFile, @Nonnull HighlightInfoHolder holder, @Nonnull Runnable action)
	{
		myHolder = holder;
		action.run();
		return true;
	}
}
