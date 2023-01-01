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

package consulo.cgshader;

import consulo.annotation.component.ExtensionImpl;
import consulo.cgshader.lexer.CGTokens;
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
@ExtensionImpl
public class CGBraceMatcher implements PairedBraceMatcher
{
	private static final BracePair[] ourPairs = new BracePair[]{
			new BracePair(CGTokens.LBRACE, CGTokens.RBRACE, true),
			new BracePair(CGTokens.LPAR, CGTokens.RPAR, false),
			new BracePair(CGTokens.LBRACKET, CGTokens.RBRACKET, false),
	};

	@Override
	public BracePair[] getPairs()
	{
		return ourPairs;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CGLanguage.INSTANCE;
	}
}
