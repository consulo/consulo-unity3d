package org.mustbe.consulo.unity3d.module;

import java.io.File;

import org.consulo.module.extension.impl.ModuleInheritableNamedPointerImpl;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.execution.DebugConnectionInfo;
import org.mustbe.consulo.dotnet.module.extension.BaseDotNetModuleExtension;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import com.intellij.execution.configurations.GeneralCommandLine;
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
	public GeneralCommandLine createDefaultCommandLine(@NotNull String s, @Nullable DebugConnectionInfo debugConnectionInfo)
	{
		return new GeneralCommandLine();
	}

	@NotNull
	@Override
	public String getDebugFileExtension()
	{
		return ".mdb";
	}
}
