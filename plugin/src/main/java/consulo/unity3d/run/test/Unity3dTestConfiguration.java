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

package consulo.unity3d.run.test;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.dotnet.debugger.impl.DotNetConfurationWithDefaultDebugFileType;
import consulo.execution.configuration.*;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.file.LanguageFileType;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.util.xml.serializer.XmlSerializer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jdom.Element;

/**
 * @author VISTALL
 * @since 18.01.2016
 */
public class Unity3dTestConfiguration extends LocatableConfigurationBase implements ModuleRunConfiguration, DotNetConfurationWithDefaultDebugFileType {
    public Unity3dTestConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);

        XmlSerializer.deserializeInto(this, element);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        XmlSerializer.serializeInto(this, element);
    }

    @Nonnull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new Unity3dTestConfigurationEditor();
    }

    @Nullable
    @Override
    @RequiredUIAccess
    public RunProfileState getState(@Nonnull Executor executor, @Nonnull final ExecutionEnvironment environment) throws ExecutionException {
        return new Unity3dTestRunState(environment);
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public Module[] getModules() {
        return ModuleManager.getInstance(getProject()).getModules();
    }

    @Nonnull
    @Override
    public LanguageFileType getDefaultDebuggerFileType() {
        return CSharpFileType.INSTANCE;
    }
}
