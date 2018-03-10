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

package consulo.unity3d.unityscript.lang.impl;

import org.jetbrains.annotations.NonNls;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.unity3d.Unity3dTypes;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptDotNetTypeDeclaration extends LightElement implements DotNetTypeDeclaration
{
	private String myNameWithoutExtension;
	private JSFile myFile;

	@RequiredReadAction
	public UnityScriptDotNetTypeDeclaration(@Nonnull String nameWithoutExtension, @Nonnull JSFile file)
	{
		super(file.getManager(), file.getLanguage());
		myNameWithoutExtension = nameWithoutExtension;
		myFile = file;
	}

	@Override
	public boolean isInterface()
	{
		return false;
	}

	@Override
	public boolean isStruct()
	{
		return false;
	}

	@Override
	public boolean isEnum()
	{
		return false;
	}

	@Override
	public boolean isNested()
	{
		return false;
	}

	@Nullable
	@Override
	public DotNetTypeList getExtendList()
	{
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return new DotNetTypeRef[]{new CSharpTypeRefByQName(myFile, Unity3dTypes.UnityEngine.MonoBehaviour)};
	}

	@RequiredReadAction
	@Override
	public boolean isInheritor(@Nonnull String qname, boolean deep)
	{
		return DotNetInheritUtil.isInheritor(this, qname, deep);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmQName()
	{
		return myNameWithoutExtension;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmName()
	{
		return myNameWithoutExtension;
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		return getVmName();
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return null;
	}

	@Nonnull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return DotNetGenericParameter.EMPTY_ARRAY;
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return new DotNetNamedElement[0];
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier dotNetModifier)
	{
		if(dotNetModifier == DotNetModifier.PUBLIC)
		{
			return true;
		}
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myNameWithoutExtension;
	}

	@Override
	public String toString()
	{
		return getClass() + ":" + myFile;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
	{
		myFile.setName(name + "." + JavaScriptFileType.INSTANCE.getDefaultExtension());
		return this;
	}

	@Override
	public boolean isValid()
	{
		return myFile.isValid();
	}

	@Nonnull
	@Override
	public PsiElement getNavigationElement()
	{
		return myFile;
	}

	@Override
	public PsiFile getContainingFile()
	{
		return myFile;
	}
}
