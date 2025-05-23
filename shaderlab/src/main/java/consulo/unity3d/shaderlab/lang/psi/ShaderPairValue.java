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

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRoleHolder;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderPairValue extends ShaderLabElement implements ShaderRoleOwner
{
	public ShaderPairValue(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Override
	@Nullable
	public ShaderLabRole getRole()
	{
		PsiElement element = findNotNullChildByType(ShaderLabKeyTokens.START_KEYWORD);
		return ShaderLabRoleHolder.findRole(element.getText());
	}

	@Override
	public void accept(SharpLabElementVisitor visitor)
	{
		visitor.visitPairValue(this);
	}
}