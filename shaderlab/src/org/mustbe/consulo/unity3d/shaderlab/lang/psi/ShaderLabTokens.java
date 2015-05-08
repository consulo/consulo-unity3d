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

package org.mustbe.consulo.unity3d.shaderlab.lang.psi;

import org.mustbe.consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

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

	IElementType SHADER_KEYWORD = new IElementType("SHADER_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType SUBSHADER_KEYWORD = new IElementType("SUBSHADER_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType TAGS_KEYWORD = new IElementType("TAGS_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType PASS_KEYWORD = new IElementType("PASS_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType LIGHTING_KEYWORD = new IElementType("LIGHTING_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType ZWRITE_KEYWORD = new IElementType("ZWRITE_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType CULL_KEYWORD = new IElementType("CULL_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType SET_TEXTURE_KEYWORD = new IElementType("SET_TEXTURE_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType MATRIX_KEYWORD = new IElementType("MATRIX_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType CONSTANT_COLOR_KEYWORD = new IElementType("CONSTANT_COLOR_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType VALUE_KEYWORD = new IElementType("VALUE_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType COLOR_KEYWORD = new IElementType("COLOR_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType PROPERTIES_KEYWORD = new IElementType("PROPERTIES_KEYWORD", ShaderLabLanguage.INSTANCE);

	IElementType FALLBACK_KEYWORD = new IElementType("FALLBACK_KEYWORD", ShaderLabLanguage.INSTANCE);

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
