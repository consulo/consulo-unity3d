package org.mustbe.consulo.unity3d.run;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.module.extension.ModuleExtensionHelper;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import lombok.val;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class UnityRemoteDebugApplication extends ConfigurationTypeBase
{
	public static UnityRemoteDebugApplication getInstance()
	{
		return CONFIGURATION_TYPE_EP.findExtension(UnityRemoteDebugApplication.class);
	}

	public UnityRemoteDebugApplication()
	{
		super("#UnityRemoteDebugApplication", "Unity Debug", "", Unity3dIcons.Unity3d);

		addFactory(new ConfigurationFactoryEx(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new UnityRemoteDebugConfiguration("Unnamed", new RunConfigurationModule(project), this);
			}

			@Override
			public void onNewConfigurationCreated(@NotNull RunConfiguration configuration)
			{
				UnityRemoteDebugConfiguration dotNetConfiguration = (UnityRemoteDebugConfiguration) configuration;

				for(val module : ModuleManager.getInstance(configuration.getProject()).getModules())
				{
					Unity3dModuleExtension extension = ModuleUtilCore.getExtension(module, Unity3dModuleExtension.class);
					if(extension != null)
					{
						dotNetConfiguration.setName(module.getName());
						dotNetConfiguration.setModule(module);
						break;
					}
				}
			}

			@Override
			public boolean isApplicable(@NotNull Project project)
			{
				return ModuleExtensionHelper.getInstance(project).hasModuleExtension(Unity3dModuleExtension.class);
			}
		});
	}
}
