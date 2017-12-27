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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import consulo.annotations.RequiredReadAction;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public class ShaderCGScript extends ShaderLabElement implements PsiLanguageInjectionHost
{
	public ShaderCGScript(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	@RequiredReadAction
	public PsiElement getScriptBlock()
	{
		return findChildByType(ShaderLabTokens.SHADERSCRIPT);
	}

	@Override
	public void accept(SharpLabElementVisitor visitor)
	{
		visitor.visitCGScript(this);
	}

	@Override
	@RequiredReadAction
	public boolean isValidHost()
	{
		return getScriptBlock() != null;
	}

	@Override
	public PsiLanguageInjectionHost updateText(@NotNull String text)
	{
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper()
	{
		return LiteralTextEscaper.createSimple(this);
	}
}
