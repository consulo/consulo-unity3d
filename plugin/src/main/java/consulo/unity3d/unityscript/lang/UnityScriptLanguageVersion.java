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

package consulo.unity3d.unityscript.lang;

import javax.annotation.Nonnull;

import com.intellij.lang.PsiParser;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JavaScriptParsingFlexLexer;
import com.intellij.lang.javascript.highlighting.JSHighlighter;
import com.intellij.lexer.Lexer;
import consulo.javascript.lang.BaseJavaScriptLanguageVersion;
import consulo.javascript.lang.JavaScriptLanguage;
import consulo.javascript.lang.parsing.EcmaScript4Parser;

/**
 * @author VISTALL
 * @since 04.04.2015
 */
public class UnityScriptLanguageVersion extends BaseJavaScriptLanguageVersion
{
	@Nonnull
	public static UnityScriptLanguageVersion getInstance()
	{
		return JavaScriptLanguage.INSTANCE.findVersionByClass(UnityScriptLanguageVersion.class);
	}

	private final DialectOptionHolder myDialectOptionHolder = new DialectOptionHolder(true, false);

	public UnityScriptLanguageVersion()
	{
		super("UNITY_SCRIPT");
	}

	@Nonnull
	@Override
	public PsiParser createParser()
	{
		return new EcmaScript4Parser();
	}

	@Nonnull
	@Override
	public JSHighlighter getSyntaxHighlighter()
	{
		return new JSHighlighter(myDialectOptionHolder);
	}

	@Nonnull
	@Override
	public Lexer createLexer()
	{
		return new JavaScriptParsingFlexLexer(myDialectOptionHolder);
	}
}
