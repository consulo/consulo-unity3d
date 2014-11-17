package org.mustbe.consulo.unity3d.module;

import com.intellij.ide.macro.Macro;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;

/**
 * @author VISTALL
 * @since 17.11.14
 */
public class UnityFileNameMacro extends Macro
{
	@Override
	public String getName()
	{
		return "UnityFileName";
	}

	@Override
	public String getDescription()
	{
		return "Unity File Name";
	}

	@Override
	public String expand(DataContext dataContext)
	{
		final Module module = LangDataKeys.MODULE.getData(dataContext);
		if(module == null)
		{
			return null;
		}
		Unity3dModuleExtension extension = ModuleUtilCore.getExtension(module, Unity3dModuleExtension.class);
		if(extension != null)
		{
			return extension.getFileName();
		}
		return null;
	}
}