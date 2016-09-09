package consulo.unity3d.unityscript.debugger;

import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.debugger.DotNetDebuggerSourceLineResolver;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptSourceLineResolver extends DotNetDebuggerSourceLineResolver
{
	@RequiredReadAction
	@Nullable
	@Override
	public String resolveParentVmQName(@NotNull PsiElement element)
	{
		Module rootModule = Unity3dModuleExtensionUtil.getRootModule(element.getProject());
		if(rootModule == null)
		{
			return null;
		}
		VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
		return virtualFile == null ? null : virtualFile.getNameWithoutExtension();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public Set<PsiElement> getAllExecutableChildren(@NotNull PsiElement element)
	{
		return Collections.emptySet();
	}
}
