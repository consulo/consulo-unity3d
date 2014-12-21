package org.mustbe.consulo.unity3d.run.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.debugger.DotNetDebugHelper;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import mono.debugger.AssemblyMirror;
import mono.debugger.TypeMirror;

/**
 * @author VISTALL
 * @since 21.12.14
 */
public class UnityDebugHelper extends DotNetDebugHelper
{
	@Nullable
	@Override
	public TypeMirror findTypeMirrorFromAssemblies(String vmQName,
			@NotNull AssemblyMirror[] assemblyMirrors,
			@NotNull DotNetTypeDeclaration typeDeclaration)
	{
		for(AssemblyMirror assemblyMirror : assemblyMirrors)
		{
			TypeMirror typeByQualifiedName = assemblyMirror.findTypeByQualifiedName(vmQName, false);
			if(typeByQualifiedName != null)
			{
				return typeByQualifiedName;
			}
		}
		return null;
	}
}
