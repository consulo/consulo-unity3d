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

package consulo.unity3d.jsonApi;

import com.intellij.openapi.project.Project;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import consulo.dotnet.compiler.DotNetCompilerMessage;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;

/**
 * @author VISTALL
 * @since 07-Jun-16
 */
public class UnityLogParser
{
	private static final MSBaseDotNetCompilerOptionsBuilder ourDummy = new MSBaseDotNetCompilerOptionsBuilder();

	@RequiredReadAction
	public static DotNetCompilerMessage extractFileInfo(Project project, String line)
	{
		return ourDummy.convertToMessage(Unity3dModuleExtensionUtil.getRootModule(project), line);
	}
}
