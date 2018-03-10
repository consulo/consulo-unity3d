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

package consulo.cgshader.completion;

import java.util.Set;

import javax.annotation.Nonnull;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.StandardPatterns;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.cgshader.CGLanguage;
import consulo.cgshader.lexer.CGKeywords;
import consulo.codeInsight.completion.CompletionProvider;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public class CGCompletionContributor extends CompletionContributor
{
	private static final Set<String> ourMethods = ContainerUtil.newHashSet("abs", "acos", "all", "any", "asin", "atan", "atan2", "ceil", "clamp", "clip", "cos", "cosh", "cross", "ddx", "ddy",
			"degrees", "determinant", "distance", "dot", "exp", "exp2", "faceforward", "floatToIntBits", "floatToRawIntBits", "floor", "fmod", "frac", "frexp", "fwidth", "intBitsToFloat",
			"isfinite", "isinf", "isnan", "ldexp", "length", "lerp", "lit", "log", "log10", "log2", "max", "min", "modf", "mul", "normalize", "pow", "radians", "reflect", "refract", "round",
			"rsqrt", "saturate", "sign", "sin", "sincos", "sinh", "smoothstep", "sqrt", "step", "tan", "tanh", "tex1D", "tex1DARRAY", "tex1DARRAYbias", "tex1DARRAYcmpbias", "tex1DARRAYcmplod",
			"tex1DARRAYfetch", "tex1DARRAYlod", "tex1DARRAYproj", "tex1DARRAYsize", "tex1Dbias", "tex1Dcmpbias", "tex1Dcmplod", "tex1Dfetch", "tex1Dlod", "tex1Dproj", "tex1Dsize", "tex2D",
			"tex2DARRAY", "tex2DARRAYbias", "tex2DARRAYfetch", "tex2DARRAYlod", "tex2DARRAYproj", "tex2DARRAYsize", "tex2Dbias", "tex2Dcmpbias", "tex2Dcmplod", "tex2Dfetch", "tex2Dlod", "tex2Dproj",
			"tex2Dsize", "tex3D", "tex3Dbias", "tex3Dfetch", "tex3Dlod", "tex3Dproj", "tex3Dsize", "texBUF", "texBUFsize", "texCUBE", "texCUBEARRAY", "texCUBEARRAYbias", "texCUBEARRAYlod",
			"texCUBEARRAYsize", "texCUBEbias", "texCUBElod", "texCUBEproj", "texCUBEsize", "texRECT", "texRECTbias", "texRECTfetch", "texRECTlod", "texRECTproj", "texRECTsize", "transpose", "trunc");

	public CGCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement().withLanguage(CGLanguage.INSTANCE), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				for(String keyword : CGKeywords.KEYWORDS)
				{
					result.addElement(LookupElementBuilder.create(keyword).bold());
				}

				for(String m : ourMethods)
				{
					result.addElement(LookupElementBuilder.create(m + "()").withIcon(AllIcons.Nodes.Method).withInsertHandler(ParenthesesInsertHandler.getInstance(true)));
				}
			}
		});
	}
}
