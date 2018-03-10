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

import javax.annotation.Nonnull;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.cgshader.lexer.CGLexer;
import consulo.cgshader.lexer.CGTokens;
import consulo.lang.LanguageVersion;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public class CGParserDefinition implements ParserDefinition
{
	private static final IFileElementType FILE_ELEMENT_TYPE = new IFileElementType(CGLanguage.INSTANCE);

	@Nonnull
	@Override
	public Lexer createLexer(@Nonnull LanguageVersion languageVersion)
	{
		return new CGLexer();
	}

	@Nonnull
	@Override
	public PsiParser createParser(@Nonnull LanguageVersion languageVersion)
	{
		return new PsiParser()
		{
			@Nonnull
			@Override
			public ASTNode parse(@Nonnull IElementType root, @Nonnull PsiBuilder builder, @Nonnull LanguageVersion languageVersion)
			{
				PsiBuilder.Marker mark = builder.mark();
				while(!builder.eof())
				{
					builder.advanceLexer();
				}
				mark.done(root);
				return builder.getTreeBuilt();
			}
		};
	}

	@Nonnull
	@Override
	public IFileElementType getFileNodeType()
	{
		return FILE_ELEMENT_TYPE;
	}

	@Nonnull
	@Override
	public TokenSet getWhitespaceTokens(@Nonnull LanguageVersion languageVersion)
	{
		return TokenSet.create(CGTokens.WHITE_SPACE);
	}

	@Nonnull
	@Override
	public TokenSet getCommentTokens(@Nonnull LanguageVersion languageVersion)
	{
		return TokenSet.create(CGTokens.LINE_COMMENT, CGTokens.BLOCK_COMMENT);
	}

	@Nonnull
	@Override
	public TokenSet getStringLiteralElements(@Nonnull LanguageVersion languageVersion)
	{
		return TokenSet.create(CGTokens.STRING_LITERAL);
	}

	@Nonnull
	@Override
	public PsiElement createElement(ASTNode node)
	{
		return new ASTWrapperPsiElement(node);
	}

	@Override
	public PsiFile createFile(FileViewProvider viewProvider)
	{
		return new PsiFileBase(viewProvider, CGLanguage.INSTANCE)
		{
		};
	}

	@Nonnull
	@Override
	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
	{
		return SpaceRequirements.MAY;
	}
}
