package org.mustbe.consulo.unity3d.csharp.module.extension;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import com.intellij.openapi.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dCSharpModuleExtension extends BaseCSharpModuleExtension<Unity3dCSharpModuleExtension>
{
	public Unity3dCSharpModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
	}

	@NotNull
	@Override
	public DotNetCompilerOptionsBuilder createCompilerOptionsBuilder()
	{
		throw new IllegalArgumentException();
	}
}
