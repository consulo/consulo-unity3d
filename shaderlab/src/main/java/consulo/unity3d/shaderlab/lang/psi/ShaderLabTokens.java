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

package consulo.unity3d.shaderlab.lang.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public interface ShaderLabTokens extends TokenType
{
	IElementType IDENTIFIER = new IElementType("IDENTIFIER", ShaderLabLanguage.INSTANCE);

	IElementType LINE_COMMENT = new IElementType("LINE_COMMENT", ShaderLabLanguage.INSTANCE);

	IElementType BLOCK_COMMENT = new IElementType("BLOCK_COMMENT", ShaderLabLanguage.INSTANCE);

	IElementType INTEGER_LITERAL = new IElementType("INTEGER_LITERAL", ShaderLabLanguage.INSTANCE);

	IElementType STRING_LITERAL = new IElementType("STRING_LITERAL", ShaderLabLanguage.INSTANCE);

	IElementType COMMA = new IElementType("COMMA", ShaderLabLanguage.INSTANCE);

	IElementType LBRACE = new IElementType("LBRACE", ShaderLabLanguage.INSTANCE);

	IElementType RBRACE = new IElementType("RBRACE", ShaderLabLanguage.INSTANCE);

	IElementType EQ = new IElementType("EQ", ShaderLabLanguage.INSTANCE);

	IElementType LPAR = new IElementType("LPAR", ShaderLabLanguage.INSTANCE);

	IElementType RPAR = new IElementType("RPAR", ShaderLabLanguage.INSTANCE);

	IElementType LBRACKET = new IElementType("LBRACKET", ShaderLabLanguage.INSTANCE);

	IElementType RBRACKET = new IElementType("RBRACKET", ShaderLabLanguage.INSTANCE);

	IElementType CGPROGRAM_KEYWORD = new IElementType("CGPROGRAM_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType CGINCLUDE_KEYWORD = new IElementType("CGINCLUDE_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType ENDCG_KEYWORD = new IElementType("ENDCG_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType SHADERSCRIPT = new IElementType("SHADERSCRIPT", ShaderLabLanguage.INSTANCE);
}
