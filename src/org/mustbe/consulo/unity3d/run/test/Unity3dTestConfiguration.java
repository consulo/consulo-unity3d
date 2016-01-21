/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.unity3d.run.test;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtension;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.ModuleRunConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializer;

/**
 * @author VISTALL
 * @since 18.01.2016
 */
public class Unity3dTestConfiguration extends LocatableConfigurationBase implements ModuleRunConfiguration
{
	public String TYPE_VM_QNAME;

	public Unity3dTestConfiguration(Project project, ConfigurationFactory factory, String name)
	{
		super(project, factory, name);
	}

	public void setTypeVmQName(String typeVmQName)
	{
		TYPE_VM_QNAME = typeVmQName;
	}

	public String getTypeVmQName()
	{
		return TYPE_VM_QNAME;
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException
	{
		super.readExternal(element);

		XmlSerializer.deserializeInto(this, element);
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException
	{
		super.writeExternal(element);

		XmlSerializer.serializeInto(this, element);
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
	{
		return new Unity3dTestConfigurationEditor();
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull final ExecutionEnvironment environment) throws ExecutionException
	{
		return new Unity3dTestRunState(environment);
	}

	@NotNull
	@Override
	public Module[] getModules()
	{
		List<Module> modules = new ArrayList<Module>();
		for(Module module : modules)
		{
			Unity3dModuleExtension extension = ModuleUtilCore.getExtension(module, Unity3dModuleExtension.class);
			if(extension == null)
			{
				continue;
			}

			if(module.getName().contains("Editor"))
			{
				modules.add(module);
			}
		}
		return ContainerUtil.toArray(modules, Module.ARRAY_FACTORY);
	}
}
