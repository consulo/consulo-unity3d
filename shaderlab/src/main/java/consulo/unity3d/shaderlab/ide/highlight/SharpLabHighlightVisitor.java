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

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.annotations.RequiredReadAction;
import consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabFile;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabKeyTokens;
import consulo.unity3d.shaderlab.lang.psi.ShaderPropertyElement;
import consulo.unity3d.shaderlab.lang.psi.ShaderPropertyTypeElement;
import consulo.unity3d.shaderlab.lang.psi.ShaderReference;
import consulo.unity3d.shaderlab.lang.psi.SharpLabElementVisitor;

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
	public boolean suitableForFile(@NotNull PsiFile file)
	{
		return file instanceof ShaderLabFile;
	}

	@Override
	public void visit(@NotNull PsiElement element)
	{
		element.accept(this);
	}

	@Override
	public boolean analyze(@NotNull PsiFile file, boolean updateWholeFile, @NotNull HighlightInfoHolder holder, @NotNull Runnable action)
	{
		myHolder = holder;
		action.run();
		return true;
	}

	@NotNull
	@Override
	public HighlightVisitor clone()
	{
		return new SharpLabHighlightVisitor();
	}

	@Override
	public int order()
	{
		return 0;
	}
}
