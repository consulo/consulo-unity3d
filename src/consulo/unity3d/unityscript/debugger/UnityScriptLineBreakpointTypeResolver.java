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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import consulo.dotnet.debugger.breakpoint.DotNetLineBreakpointType;
import consulo.unity3d.unityscript.module.extension.Unity3dScriptModuleExtension;
import consulo.xdebugger.breakpoints.XLineBreakpointTypeResolver;

/**
 * @author VISTALL
 * @since 27.04.2015
 */
public class UnityScriptLineBreakpointTypeResolver implements XLineBreakpointTypeResolver
{
	@Nullable
	@Override
	public XLineBreakpointType<?> resolveBreakpointType(@NotNull Project project, @NotNull VirtualFile virtualFile, int line)
	{
		Unity3dScriptModuleExtension extension = ModuleUtilCore.getExtension(project, virtualFile, Unity3dScriptModuleExtension.class);
		if(extension != null)
		{
			return DotNetLineBreakpointType.getInstance();
		}
		return null;
	}
}
