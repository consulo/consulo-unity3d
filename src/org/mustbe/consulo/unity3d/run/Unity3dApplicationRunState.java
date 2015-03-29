/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d.run;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.compiler.DotNetMacroUtil;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtensionUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.macro.Macro;
import com.intellij.ide.macro.MacroManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author VISTALL
 * @since 29.03.2015
 */
public class Unity3dApplicationRunState extends CommandLineState
{
	public Unity3dApplicationRunState(ExecutionEnvironment environment)
	{
		super(environment);
	}

	@NotNull
	@Override
	@RequiredReadAction
	protected ProcessHandler startProcess() throws ExecutionException
	{
		Unity3dModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(getEnvironment().getProject());
		if(rootModuleExtension == null)
		{
			throw new ExecutionException("Unity3d setup is required");
		}

		GeneralCommandLine commandLine = new GeneralCommandLine();

		String templateFilePath = rootModuleExtension.getOutputDir() + "/" + rootModuleExtension.getBuildTarget().getFileNameTemplate();

		try
		{
			String filePath = MacroManager.getInstance().expandSilentMarcos(templateFilePath, true,
					DotNetMacroUtil.createContext(rootModuleExtension.getModule(), false));

			if(SystemInfo.isMac)
			{
				// need get app dir, like 'TestProject.app'
				String fileName = StringUtil.getShortName(filePath, '/');
				// cut '.app'
				String nameWithoutExtension = FileUtil.getNameWithoutExtension(fileName);

				commandLine.setExePath(filePath + "/Contents/MacOS/" + nameWithoutExtension);
			}
			else
			{
				commandLine.setExePath(filePath);
			}
		}
		catch(Macro.ExecutionCancelledException e)
		{
			throw new ExecutionException(e);
		}
		return new OSProcessHandler(commandLine);
	}
}
