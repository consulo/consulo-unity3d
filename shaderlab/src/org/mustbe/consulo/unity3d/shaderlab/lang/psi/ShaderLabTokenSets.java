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

import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public interface ShaderLabTokenSets
{
	TokenSet COMMENTS = TokenSet.create(ShaderLabTokens.BLOCK_COMMENT, ShaderLabTokens.LINE_COMMENT);

	TokenSet WHITESPACES = TokenSet.create(ShaderLabTokens.WHITE_SPACE);

	TokenSet KEYWORDS = TokenSet.create(ShaderLabTokens.SHADER_KEYWORD, ShaderLabTokens.CGPROGRAM_KEYWORD, ShaderLabTokens.ENDCG_KEYWORD,
			ShaderLabTokens.PROPERTIES_KEYWORD);
}
