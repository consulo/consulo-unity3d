package org.mustbe.consulo.unity3d.unityscript.lang.impl;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.unity3d.Unity3dTypes;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptDotNetTypeDeclaration extends LightElement implements DotNetTypeDeclaration
{
	private String myNameWithoutExtension;
	private JSFile myFile;

	@RequiredReadAction
	public UnityScriptDotNetTypeDeclaration(@NotNull String nameWithoutExtension, @NotNull JSFile file)
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

	@NotNull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return new DotNetTypeRef[] {new CSharpTypeRefByQName(Unity3dTypes.UnityEngine.MonoBehaviour)};
	}

	@RequiredReadAction
	@Override
	public boolean isInheritor(@NotNull String qname, boolean deep)
	{
		return DotNetInheritUtil.isInheritor(this, qname, deep);
	}

	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return null;
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

	@NotNull
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

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return new DotNetNamedElement[0];
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier dotNetModifier)
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

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		myFile.setName(name + "." + JavaScriptFileType.INSTANCE.getDefaultExtension());
		return this;
	}

	@Override
	public boolean isValid()
	{
		return myFile.isValid();
	}

	@NotNull
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
