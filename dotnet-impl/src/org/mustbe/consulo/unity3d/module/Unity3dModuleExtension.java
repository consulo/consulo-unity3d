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

package org.mustbe.consulo.unity3d.module;

import java.io.File;

import org.consulo.module.extension.impl.ModuleInheritableNamedPointerImpl;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.compiler.DotNetMacroUtil;
import org.mustbe.consulo.dotnet.execution.DebugConnectionInfo;
import org.mustbe.consulo.dotnet.module.extension.BaseDotNetModuleExtension;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.ide.macro.Macro;
import com.intellij.ide.macro.MacroManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 28.09.14
 */
public class Unity3dModuleExtension extends BaseDotNetModuleExtension<Unity3dModuleExtension>
{
	public static final String FILE_NAME = MODULE_NAME;

	protected Unity3dTarget myBuildTarget = Unity3dTarget.Windows;

	public Unity3dModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@Override
	public void commit(@NotNull Unity3dModuleExtension mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);
		myBuildTarget = mutableModuleExtension.getBuildTarget();
	}

	@Override
	protected void getStateImpl(@NotNull Element element)
	{
		((ModuleInheritableNamedPointerImpl) getInheritableSdk()).toXml(element);
		element.setAttribute("output-dir", myOutputDirectory);
		element.setAttribute("namespace-prefix", getNamespacePrefix());
		element.setAttribute("build-target", myBuildTarget.name());
		element.setAttribute("file-name", myFileName);

		for(String variable : myVariables)
		{
			element.addContent(new Element("define").setText(variable));
		}
	}

	@Override
	protected void loadStateImpl(@NotNull Element element)
	{
		((ModuleInheritableNamedPointerImpl) getInheritableSdk()).fromXml(element);

		myFileName = element.getAttributeValue("file-name", FILE_NAME);
		myOutputDirectory = element.getAttributeValue("output-dir", DEFAULT_OUTPUT_DIR);
		myNamespacePrefix = element.getAttributeValue("namespace-prefix");
		myBuildTarget = Unity3dTarget.valueOf(element.getAttributeValue("build-target", Unity3dTarget.Windows.name()));

		for(Element defineElement : element.getChildren("define"))
		{
			myVariables.add(defineElement.getText());
		}
	}

	@NotNull
	@Override
	public String getFileName()
	{
		return StringUtil.notNullizeIfEmpty(myFileName, FILE_NAME);
	}

	@NotNull
	public Unity3dTarget getBuildTarget()
	{
		return myBuildTarget;
	}

	@Override
	public boolean isSupportCompilation()
	{
		return false;
	}

	@NotNull
	@Override
	public File[] getFilesForLibraries()
	{
		Sdk sdk = getSdk();
		if(sdk == null)
		{
			return EMPTY_FILE_ARRAY;
		}

		String homePath = sdk.getHomePath();
		if(homePath == null)
		{
			return EMPTY_FILE_ARRAY;
		}

		String pathForMono = Unity3dBundleType.getPathForMono(homePath, getLibrarySuffix());

		File[] array = EMPTY_FILE_ARRAY;

		File dir = new File(pathForMono);
		if(dir.exists())
		{
			File[] files = dir.listFiles();
			if(files != null)
			{
				array = ArrayUtil.mergeArrays(array, files);
			}
		}

		String managedPath = Unity3dBundleType.getManagedPath(homePath, getLibrarySuffix());

		dir = new File(managedPath);
		if(dir.exists())
		{
			File[] files = dir.listFiles();
			if(files != null)
			{
				array = ArrayUtil.mergeArrays(array, files);
			}
		}
		return array;
	}

	@NotNull
	public String getLibrarySuffix()
	{
		return "unity";
	}

	@NotNull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return Unity3dBundleType.class;
	}

	@NotNull
	@Override
	public GeneralCommandLine createDefaultCommandLine(@NotNull String s, @Nullable DebugConnectionInfo debugConnectionInfo) throws ExecutionException
	{
		GeneralCommandLine commandLine = new GeneralCommandLine();

		String fileName = getOutputDir() + "/" + myBuildTarget.getFileNameTemplate();

		try
		{
			String file = MacroManager.getInstance().expandSilentMarcos(fileName, true, DotNetMacroUtil.createContext(getModule(), false));

			if(!new File(file).exists())
			{
				throw new ExecutionException("Executable not exists");
			}
			commandLine.setExePath(file);
		}
		catch(Macro.ExecutionCancelledException e)
		{
			throw new ExecutionException(e);
		}
		return commandLine;
	}

	@NotNull
	@Override
	public String getDebugFileExtension()
	{
		return ".mdb";
	}
}
