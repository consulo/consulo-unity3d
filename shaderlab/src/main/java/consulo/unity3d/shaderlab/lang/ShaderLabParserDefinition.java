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

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.lang.LanguageVersion;
import consulo.unity3d.shaderlab.lang.lexer.ShaderLabLexer;
import consulo.unity3d.shaderlab.lang.parser.ShaderLabParser;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabFile;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabStubElements;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokenSets;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderLabParserDefinition implements ParserDefinition
{
	@Nonnull
	@Override
	public Lexer createLexer(@Nonnull LanguageVersion languageVersion)
	{
		return new ShaderLabLexer();
	}

	@Nonnull
	@Override
	public PsiParser createParser(@Nonnull LanguageVersion languageVersion)
	{
		return new ShaderLabParser();
	}

	@Nonnull
	@Override
	public IFileElementType getFileNodeType()
	{
		return ShaderLabStubElements.FILE;
	}

	@Nonnull
	@Override
	public TokenSet getWhitespaceTokens(@Nonnull LanguageVersion languageVersion)
	{
		return ShaderLabTokenSets.WHITESPACES;
	}

	@Nonnull
	@Override
	public TokenSet getCommentTokens(@Nonnull LanguageVersion languageVersion)
	{
		return ShaderLabTokenSets.COMMENTS;
	}

	@Nonnull
	@Override
	public TokenSet getStringLiteralElements(@Nonnull LanguageVersion languageVersion)
	{
		return TokenSet.EMPTY;
	}

	@Nonnull
	@Override
	public PsiElement createElement(ASTNode node)
	{
		System.out.println(node.getElementType());
		return new ASTWrapperPsiElement(node);
	}

	@Override
	public PsiFile createFile(FileViewProvider viewProvider)
	{
		return new ShaderLabFile(viewProvider);
	}

	@Nonnull
	@Override
	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
	{
		return SpaceRequirements.MAY;
	}
}
