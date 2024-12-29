/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.scene.reference;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 03-Sep-17
 */
public abstract class Unity3dKeyValueReferenceBase implements PsiReference
{
	protected YAMLKeyValue myKeyValue;

	public Unity3dKeyValueReferenceBase(YAMLKeyValue keyValue)
	{
		myKeyValue = keyValue;
	}

	@RequiredReadAction
	@Override
	public PsiElement getElement()
	{
		return myKeyValue;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public TextRange getRangeInElement()
	{
		PsiElement key = myKeyValue.getKey();
		if(key == null)
		{
			return new TextRange(0, myKeyValue.getTextLength());
		}
		// cut :
		return new TextRange(0, key.getTextLength() - 1);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getCanonicalText()
	{
		return myKeyValue.getKeyText();
	}

	@RequiredWriteAction
	@Override
	public PsiElement handleElementRename(String name) throws IncorrectOperationException
	{
		myKeyValue.setName(name);
		return null;
	}

	@RequiredWriteAction
	@Override
	public PsiElement bindToElement(@Nonnull PsiElement psiElement) throws IncorrectOperationException
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public boolean isReferenceTo(PsiElement psiElement)
	{
		return PsiManager.getInstance(myKeyValue.getProject()).areElementsEquivalent(psiElement, resolve());
	}

	@RequiredReadAction
	@Override
	public boolean isSoft()
	{
		return true;
	}
}
