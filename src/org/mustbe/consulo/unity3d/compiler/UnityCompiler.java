/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.unity3d.compiler;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.compiler.DotNetMacroUtil;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dTarget;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.ide.macro.Macro;
import com.intellij.ide.macro.MacroManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.compiler.EmptyValidityState;
import com.intellij.openapi.compiler.PackagingCompiler;
import com.intellij.openapi.compiler.ValidityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 17.11.14
 */
public class UnityCompiler implements PackagingCompiler
{

	@NotNull
	@Override
	public String getDescription()
	{
		return "UnityCompiler";
	}

	@Override
	public boolean validateConfiguration(CompileScope compileScope)
	{
		return true;
	}

	@Override
	public void init(@NotNull CompilerManager compilerManager)
	{

	}

	@Override
	public void processOutdatedItem(CompileContext compileContext, String s, @Nullable ValidityState validityState)
	{

	}

	@NotNull
	@Override
	public ProcessingItem[] getProcessingItems(final CompileContext compileContext)
	{
		return new ProcessingItem[]{
				new ProcessingItem()
				{
					@NotNull
					@Override
					public VirtualFile getFile()
					{
						return compileContext.getProject().getBaseDir();
					}

					@Nullable
					@Override
					public ValidityState getValidityState()
					{
						return new EmptyValidityState();
					}
				}
		};
	}

	@Override
	public ProcessingItem[] process(CompileContext compileContext, ProcessingItem[] processingItems)
	{
		ModuleManager moduleManager = ModuleManager.getInstance(compileContext.getProject());

		val baseDir = compileContext.getProject().getBaseDir();
		Module rootModule = ContainerUtil.find(moduleManager.getModules(), new Condition<Module>()
		{
			@Override
			public boolean value(Module module)
			{
				return Comparing.equal(baseDir, module.getModuleDir());
			}
		});

		if(rootModule == null)
		{
			return ProcessingItem.EMPTY_ARRAY;
		}

		Unity3dModuleExtension unity3dModuleExtension = ModuleUtilCore.getExtension(rootModule, Unity3dModuleExtension.class);
		if(unity3dModuleExtension == null)
		{
			return ProcessingItem.EMPTY_ARRAY;
		}

		Sdk sdk = unity3dModuleExtension.getSdk();
		if(sdk == null)
		{
			return ProcessingItem.EMPTY_ARRAY;
		}

		String applicationPath = Unity3dBundleType.getApplicationPath(sdk.getHomePath());

		List<String> args = new ArrayList<String>();
		args.add(applicationPath);
		args.add("-batchmode");
		args.add("-projectPath");
		args.add(rootModule.getModuleDirPath());

		Unity3dTarget buildTarget = unity3dModuleExtension.getBuildTarget();
		args.add(buildTarget.getCompilerOption());

		try
		{
			DataContext context = DotNetMacroUtil.createContext(rootModule, false);
			String newFile = MacroManager.getInstance().expandSilentMarcos(buildTarget.getFileNameTemplate(), true, context);
			String newPath = MacroManager.getInstance().expandSilentMarcos(unity3dModuleExtension.getOutputDir(), true, context);

			args.add(FileUtilRt.toSystemIndependentName(newPath + "/" + newFile));
			args.add("-quit");

			String workDir = FileUtilRt.toSystemIndependentName(newPath);
			FileUtil.createDirectory(new File(workDir));
			compileContext.addMessage(CompilerMessageCategory.INFORMATION, "Arguments: " + args, null, -1, -1);
			ProcessOutput processOutput = ExecUtil.execAndGetOutput(args, workDir);
			if(processOutput.getExitCode() != 0)
			{
				compileContext.addMessage(CompilerMessageCategory.ERROR, "Unity compilation error. Check log file", null, -1, -1);
			}
		}
		catch(Macro.ExecutionCancelledException e)
		{
			e.printStackTrace();
		}
		catch(ExecutionException e)
		{
			e.printStackTrace();
		}
		return processingItems;
	}

	@Override
	public ValidityState createValidityState(DataInput dataInput) throws IOException
	{
		return new EmptyValidityState();
	}
}
