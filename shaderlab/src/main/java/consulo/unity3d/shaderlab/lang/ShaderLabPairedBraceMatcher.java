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
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
@ExtensionImpl
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

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return ShaderLabLanguage.INSTANCE;
	}
}
