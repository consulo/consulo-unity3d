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
