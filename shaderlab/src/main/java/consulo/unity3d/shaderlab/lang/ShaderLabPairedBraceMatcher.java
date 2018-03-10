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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderLabPairedBraceMatcher implements PairedBraceMatcher
{
	private final BracePair[] myBracePairs = new BracePair[]{
			new BracePair(ShaderLabTokens.LBRACE, ShaderLabTokens.RBRACE, true),
			new BracePair(ShaderLabTokens.LPAR, ShaderLabTokens.RPAR, false),
			new BracePair(ShaderLabTokens.LBRACKET, ShaderLabTokens.RBRACKET, false),
	};

	@Override
	public BracePair[] getPairs()
	{
		return myBracePairs;
	}

	@Override
	public boolean isPairedBracesAllowedBeforeType(@Nonnull IElementType lbraceType, @Nullable IElementType contextType)
	{
		return true;
	}

	@Override
	public int getCodeConstructStart(PsiFile file, int openingBraceOffset)
	{
		return openingBraceOffset;
	}
}
