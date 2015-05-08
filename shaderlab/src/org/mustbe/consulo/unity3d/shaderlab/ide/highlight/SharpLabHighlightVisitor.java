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

package org.mustbe.consulo.unity3d.shaderlab.ide.highlight;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabFile;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderProperty;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderPropertyType;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.SharpLabElementVisitor;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class SharpLabHighlightVisitor extends SharpLabElementVisitor implements HighlightVisitor
{
	private HighlightInfoHolder myHolder;

	@Override
	public void visitProperty(ShaderProperty p)
	{
		super.visitProperty(p);

		PsiElement nameIdentifier = p.getNameIdentifier();
		if(nameIdentifier != null)
		{
			myHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(nameIdentifier).textAttributes
					(DefaultLanguageHighlighterColors.INSTANCE_FIELD).create());
		}
	}

	@Override
	public void visitPropertyType(ShaderPropertyType type)
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
			myHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(element).textAttributes
					(DefaultLanguageHighlighterColors.TYPE_ALIAS_NAME).create());
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
