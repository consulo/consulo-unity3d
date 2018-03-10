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

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiQualifiedReferenceElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Consumer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetPsiSearcher;
import consulo.unity3d.shaderlab.ide.refactoring.ShaderRefactorUtil;
import consulo.unity3d.shaderlab.lang.ShaderMaterialAttribute;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRoles;
import consulo.unity3d.shaderlab.lang.psi.light.LightShaderDef;
import consulo.unity3d.shaderlab.lang.psi.stub.index.ShaderDefIndex;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderReference extends ShaderLabElement implements PsiQualifiedReferenceElement
{
	public static enum ResolveKind
	{
		ATTRIBUTE,
		ANOTHER_SHADER,
		SHADER_GUI,
		PROPERTY,
	}

	private static final TokenSet ourTokens = TokenSet.create(ShaderLabTokens.IDENTIFIER, ShaderLabTokens.STRING_LITERAL);

	public ShaderReference(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Nonnull
	public ResolveKind kind()
	{
		PsiElement parent = getParent();
		if(parent instanceof ShaderPropertyAttribute)
		{
			return ResolveKind.ATTRIBUTE;
		}
		else if(parent instanceof ShaderSetTexture)
		{
			return ResolveKind.PROPERTY;
		}
		else if(parent instanceof ShaderSimpleValue)
		{
			ShaderLabRole role = ((ShaderSimpleValue) parent).getRole();
			if(role == ShaderLabRoles.CustomEditor)
			{
				return ResolveKind.SHADER_GUI;
			}

			if(role == ShaderLabRoles.Fallback || role == ShaderLabRoles.UsePass)
			{
				return ResolveKind.ANOTHER_SHADER;
			}
		}
		return ResolveKind.PROPERTY;
	}

	@Override
	public PsiReference getReference()
	{
		return this;
	}

	@RequiredReadAction
	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@Nonnull
	@RequiredReadAction
	public PsiElement getReferenceElement()
	{
		return findNotNullChildByType(ourTokens);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public TextRange getRangeInElement()
	{
		PsiElement referenceElement = getReferenceElement();
		return new TextRange(0, referenceElement.getTextLength());
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement resolve()
	{
		GlobalSearchScope scope = GlobalSearchScope.allScope(getProject());
		ResolveKind kind = kind();
		String referenceName = getReferenceName();
		switch(kind)
		{
			case ATTRIBUTE:
				try
				{
					ShaderMaterialAttribute attribute = ShaderMaterialAttribute.valueOf(referenceName);
					DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(getProject()).findType(attribute.getType(), scope, CSharpTransform.INSTANCE);
					if(type != null)
					{
						return type;
					}
				}
				catch(IllegalArgumentException ignored)
				{
					//
				}
				break;
			case SHADER_GUI:
				DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(getProject()).findType(StringUtil.unquoteString(referenceName), scope, CSharpTransform.INSTANCE);
				if(type != null)
				{
					return type;
				}
				break;
			case ANOTHER_SHADER:
				ShaderDef shaderDef = LightShaderDef.getDefaultShaders(getProject()).get(referenceName);
				if(shaderDef != null)
				{
					return shaderDef;
				}
				Collection<ShaderDef> shaderDefs = ShaderDefIndex.getInstance().get(referenceName, getProject(), scope);
				return ContainerUtil.getFirstItem(shaderDefs);
			case PROPERTY:
				PsiFile containingFile = getContainingFile();
				if(!(containingFile instanceof ShaderLabFile))
				{
					return null;
				}
				for(ShaderProperty shaderProperty : ((ShaderLabFile) containingFile).getProperties())
				{
					if(Objects.equals(referenceName, shaderProperty.getName()))
					{
						return shaderProperty;
					}
				}
				break;
		}
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getCanonicalText()
	{
		return getText();
	}

	@RequiredWriteAction
	@Override
	public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException
	{
		ShaderRefactorUtil.replaceIdentifier(getReferenceElement(), newElementName);
		return this;
	}

	@RequiredWriteAction
	@Override
	public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	@RequiredReadAction
	public boolean isReferenceTo(PsiElement element)
	{
		return element.isEquivalentTo(resolve());
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Object[] getVariants()
	{
		ResolveKind kind = kind();
		final List<LookupElement> values = new SmartList<>();
		switch(kind)
		{
			case ATTRIBUTE:
				for(ShaderMaterialAttribute attribute : ShaderMaterialAttribute.values())
				{
					LookupElementBuilder builder = LookupElementBuilder.create(attribute.name());
					builder = builder.withIcon(AllIcons.Nodes.Class);
					builder = builder.withTypeText(attribute.getType(), true);
					values.add(builder);
				}
				break;
			case PROPERTY:
				PsiFile containingFile = getContainingFile();
				if(containingFile instanceof ShaderLabFile)
				{
					consumeProperties((ShaderLabFile) containingFile, values::add);
				}
				break;
		}
		return values.toArray();
	}

	@RequiredReadAction
	public static void consumeProperties(@Nonnull ShaderLabFile file, @Nonnull Consumer<LookupElement> consumer)
	{
		for(ShaderProperty shaderProperty : file.getProperties())
		{
			String name = shaderProperty.getName();
			if(name == null)
			{
				continue;
			}
			LookupElementBuilder builder = LookupElementBuilder.create(name);
			builder = builder.withIcon(AllIcons.Nodes.Property);
			ShaderPropertyType type = shaderProperty.getType();
			if(type != null)
			{
				builder = builder.withTypeText(type.getTargetText(), true);
			}
			consumer.consume(builder);
		}
	}

	@RequiredReadAction
	@Override
	public boolean isSoft()
	{
		return false;
	}

	@Override
	public void accept(SharpLabElementVisitor visitor)
	{
		visitor.visitReference(this);
	}

	@Nullable
	@Override
	public PsiElement getQualifier()
	{
		return findChildByClass(ShaderReference.class);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getReferenceName()
	{
		PsiElement referenceElement = getReferenceElement();
		return referenceElement.getText();
	}
}
