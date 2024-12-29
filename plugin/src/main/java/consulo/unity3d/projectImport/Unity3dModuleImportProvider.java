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

package consulo.unity3d.projectImport;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.ide.moduleImport.ModuleImportProvider;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.project.Project;
import consulo.project.startup.StartupManager;
import consulo.ui.ex.wizard.WizardStep;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.jsonApi.UnityOpenFilePostHandlerRequest;
import consulo.unity3d.projectImport.ui.Unity3dWizardStep;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 29.12.14
 */
@ExtensionImpl
public class Unity3dModuleImportProvider implements ModuleImportProvider<UnityModuleImportContext>
{
	@Nullable
	private Sdk myPredefinedSdk;
	@Nullable
	private UnityOpenFilePostHandlerRequest myRequest;

	@Inject
	public Unity3dModuleImportProvider()
	{
	}

	public Unity3dModuleImportProvider(@Nonnull Sdk predefinedSdk, @Nonnull UnityOpenFilePostHandlerRequest request)
	{
		myPredefinedSdk = predefinedSdk;
		myRequest = request;
	}

	@Nonnull
	@Override
	public UnityModuleImportContext createContext(@Nullable Project project)
	{
		UnityModuleImportContext context = new UnityModuleImportContext(project);
		context.setSdk(myPredefinedSdk);
		context.setRequestor(myRequest);
		return context;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Unity3D";
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return Unity3dIcons.Unity3d;
	}

	@Override
	public boolean canImport(@Nonnull File fileOrDirectory)
	{
		return fileOrDirectory.isDirectory() && new File(fileOrDirectory, "ProjectSettings/ProjectSettings.asset").exists();
	}

	@RequiredReadAction
	@Override
	public void process(@Nonnull UnityModuleImportContext context, @Nonnull Project project, @Nonnull ModifiableModuleModel model, @Nonnull Consumer<Module> newModuleConsumer)
	{
		Sdk unitySdk = context.getSdk();
		UnityOpenFilePostHandlerRequest requestor = context.getRequestor();

		Module rootModule = model.newModule(project.getName(), project.getBasePath());

		newModuleConsumer.accept(rootModule);

		StartupManager.getInstance(project).registerPostStartupActivity(() -> Unity3dProjectImporter.syncProjectStep(project, unitySdk, requestor, true));

	}

	@Override
	public void buildSteps(@Nonnull Consumer<WizardStep<UnityModuleImportContext>> consumer, @Nonnull UnityModuleImportContext context)
	{
		consumer.accept(new Unity3dWizardStep(context));
	}

	@Nonnull
	@Override
	public String getFileSample()
	{
		return "<b>Unity3D</b> project";
	}
}
