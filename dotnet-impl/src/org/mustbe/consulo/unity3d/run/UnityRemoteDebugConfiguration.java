package org.mustbe.consulo.unity3d.run;

import java.util.ArrayList;
import java.util.Collection;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EmptyRunProfileState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import lombok.val;

/**
 * @author VISTALL
 * @since 10.11.14
 */
public class UnityRemoteDebugConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> implements
		RunConfigurationWithSuppressedDefaultRunAction, CompileStepBeforeRun.Suppressor
{
	public UnityRemoteDebugConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	public UnityRemoteDebugConfiguration(RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(configurationModule, factory);
	}

	@Override
	public Collection<Module> getValidModules()
	{
		val list = new ArrayList<Module>();
		for(val module : ModuleManager.getInstance(getProject()).getModules())
		{
			if(ModuleUtilCore.getExtension(module, DotNetModuleExtension.class) != null)
			{
				list.add(module);
			}
		}
		return list;
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException
	{
		super.readExternal(element);
		readModule(element);
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException
	{
		super.writeExternal(element);
		writeModule(element);
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
	{
		return new UnityRemoteDebugConfigurationEditor(getProject());
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException
	{
		return EmptyRunProfileState.INSTANCE;
	}
}
