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

package org.mustbe.consulo.unity3d.shaderlab.ide.completion;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.unity3d.shaderlab.lang.ShaderLabFileType;
import org.mustbe.consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderPropertyType;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.stub.index.ShaderDefIndex;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.StandardPatterns;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderLabCompletionContributor extends CompletionContributor
{
	public ShaderLabCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement(ShaderLabTokens.IDENTIFIER).withParent(ShaderPropertyType.class), new CompletionProvider<CompletionParameters>()

		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				for(ShaderLabPropertyType shaderLabPropertyType : ShaderLabPropertyType.values())
				{
					LookupElementBuilder builder = LookupElementBuilder.create(shaderLabPropertyType.getPresentableName());
					builder = builder.bold();

					result.addElement(builder);
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(ShaderLabTokens
				.FALLBACK_KEYWORD)), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
					@NotNull final CompletionResultSet result)
			{
				ShaderDefIndex.getInstance().processAllKeys(parameters.getPosition().getProject(), new Processor<String>()
				{
					@Override
					public boolean process(String s)
					{
						LookupElementBuilder builder = LookupElementBuilder.create(s);
						builder = builder.withIcon(ShaderLabFileType.INSTANCE.getIcon());
						result.addElement(builder);
						return true;
					}
				});
			}
		});
	}
}
