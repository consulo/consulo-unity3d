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

package consulo.cgshader.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import consulo.cgshader.CGLanguage;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public interface CGTokens extends TokenType
{
	IElementType LINE_COMMENT = new IElementType("CG_LINE_COMMENT", CGLanguage.INSTANCE);
	IElementType BLOCK_COMMENT = new IElementType("CG_BLOCK_COMMENT", CGLanguage.INSTANCE);

	IElementType LBRACE = new IElementType("CG_LBRACE", CGLanguage.INSTANCE);
	IElementType RBRACE = new IElementType("CG_RBRACE", CGLanguage.INSTANCE);
	IElementType LPAR = new IElementType("CG_LPAR", CGLanguage.INSTANCE);
	IElementType RPAR = new IElementType("CG_RPAR", CGLanguage.INSTANCE);
	IElementType LBRACKET = new IElementType("CG_LBRACKET", CGLanguage.INSTANCE);
	IElementType RBRACKET = new IElementType("CG_RBRACKET", CGLanguage.INSTANCE);
	IElementType KEYWORD = new IElementType("CG_KEYWORD", CGLanguage.INSTANCE);
	IElementType STRING_LITERAL = new IElementType("CG_STRING_LITERAL", CGLanguage.INSTANCE);
	IElementType NUMBER_LITERAL = new IElementType("CG_NUMBER_LITERAL", CGLanguage.INSTANCE);
	IElementType MACRO_KEYWORD = new IElementType("CG_MACRO_KEYWORD", CGLanguage.INSTANCE);
	IElementType TEXT = new IElementType("CG_TEXT", CGLanguage.INSTANCE);
}
