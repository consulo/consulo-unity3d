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

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.util.Consumer;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.storage.HeavyProcessLatch;
import com.intellij.util.ui.UIUtil;
import consulo.application.AccessRule;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.dotnet.roots.orderEntry.DotNetLibraryOrderEntryImpl;
import consulo.logging.Logger;
import consulo.module.extension.MutableModuleExtension;
import consulo.roots.ContentFolderScopes;
import consulo.roots.impl.ExcludedContentFolderTypeProvider;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.roots.impl.ProductionContentFolderTypeProvider;
import consulo.roots.types.BinariesOrderRootType;
import consulo.roots.types.DocumentationOrderRootType;
import consulo.unity3d.Unity3dBundle;
import consulo.unity3d.Unity3dMetaFileType;
import consulo.unity3d.UnityPluginValidator;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.bundle.Unity3dDefineByVersion;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.editor.UnityRequestDefines;
import consulo.unity3d.jsonApi.UnityOpenFilePostHandler;
import consulo.unity3d.jsonApi.UnityOpenFilePostHandlerRequest;
import consulo.unity3d.jsonApi.UnityPingPong;
import consulo.unity3d.jsonApi.UnitySetDefines;
import consulo.unity3d.module.Unity3dChildMutableModuleExtension;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.module.Unity3dRootMutableModuleExtension;
import consulo.unity3d.nunit.module.extension.Unity3dNUnitMutableModuleExtension;
import consulo.unity3d.packages.orderEntry.Unity3dPackageOrderEntry;
import consulo.unity3d.run.Unity3dAttachApplicationType;
import consulo.unity3d.run.Unity3dAttachConfiguration;
import consulo.util.dataholder.Key;
import consulo.util.lang.ref.SimpleReference;
import consulo.vfs.util.ArchiveVfsUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author VISTALL
 * @since 03.04.2015
 */
public class Unity3dProjectImporter
{
	private static final Logger LOG = Logger.getInstance(Unity3dProjectImporter.class);

	public static final String ASSETS_DIRECTORY = "Assets";

	public static final String[] FIRST_PASS_PATHS = new String[]{
			"Assets/Standard Assets",
			"Assets/Pro Standard Assets",
			"Assets/Plugins"
	};

	private static final String ASSEMBLY_UNITYSCRIPT_FIRSTPASS = "Assembly-UnityScript-firstpass";

	private static final String UNITY_EDITOR_ATTACH = "Attach to Unity Editor";

	public static final Key<Boolean> ourInProgressFlag = Key.create("Unity3dProjectUtil#ourInProgressFlag");

	@Nullable
	public static String loadVersionFromProject(@Nonnull String path)
	{
		File file = new File(path, "ProjectSettings/ProjectVersion.txt");
		if(!file.exists())
		{
			return null;
		}

		try
		{
			List<String> lines = FileUtil.loadLines(file);
			String prefix = "m_EditorVersion:";
			for(String line : lines)
			{
				if(line.startsWith(prefix))
				{
					String version = line.substring(prefix.length(), line.length());

					return Unity3dBundleType.filterReleaseInfo(version.trim());
				}
			}
		}
		catch(IOException ignored)
		{
		}
		return null;
	}

	public static void syncProjectStep1(@Nonnull final Project project, @Nullable final Sdk sdk, @Nullable UnityOpenFilePostHandlerRequest requestor, final boolean runValidator)
	{
		// set flag
		project.putUserData(ourInProgressFlag, Boolean.TRUE);

		Task.Backgroundable.queue(project, "Fetching defines from UnityEditor", indicator ->
		{
			UnityRequestDefines request = new UnityRequestDefines();

			SimpleReference<Boolean> received = SimpleReference.create(Boolean.FALSE);

			UnityPingPong.Token<UnitySetDefines> token = UnityPingPong.wantReply(request.uuid, o ->
			{
				received.set(Boolean.TRUE);

				syncProjectStep2(project, sdk, requestor, runValidator, o);
			});

			if(!UnityEditorCommunication.request(project, request, true))
			{
				token.finish(null);
				notifyAboutUnityEditorProblem(project);
				return;
			}

			int i = 0;
			while(!received.get())
			{
				if(i == 5)
				{
					token.finish(null);

					notifyAboutUnityEditorProblem(project);
					break;
				}

				TimeoutUtil.sleep(500L);

				i++;
			}
		});
	}

	private static void notifyAboutUnityEditorProblem(Project project)
	{
		UIUtil.invokeLaterIfNeeded(() -> new Notification("unity", ApplicationNamesInfo.getInstance().getProductName(), "UnityEditor is not responding.<br>Defines is not resolved.", NotificationType
				.INFORMATION).notify(project));
	}

	/**
	 * this method will called from webservice thread
	 */
	private static void syncProjectStep2(@Nonnull final Project project,
										 @Nullable final Sdk sdk,
										 @Nullable UnityOpenFilePostHandlerRequest requestor,
										 final boolean runValidator,
										 UnitySetDefines unitySetDefines)
	{
		Task.Backgroundable.queue(project, "Sync Project", indicator ->
		{
			AccessToken accessToken = HeavyProcessLatch.INSTANCE.processStarted("unity sync project");
			try
			{
				importAfterDefines(project, sdk, runValidator, indicator, requestor, unitySetDefines);
			}
			finally
			{
				accessToken.finish();
			}
		});
	}

	private static void importAfterDefines(@Nonnull final Project project,
										   @Nullable final Sdk sdk,
										   final boolean runValidator,
										   @Nonnull ProgressIndicator indicator,
										   @Nullable UnityOpenFilePostHandlerRequest requestor,
										   @Nullable UnitySetDefines setDefines)
	{
		try
		{
			Collection<String> defines = null;
			if(setDefines != null)
			{
				VirtualFile maybeProjectDir = LocalFileSystem.getInstance().findFileByPath(setDefines.projectPath);
				if(maybeProjectDir != null && maybeProjectDir.equals(project.getBaseDir()))
				{
					defines = new TreeSet<>(Arrays.asList(setDefines.defines));
				}
			}

			importOrUpdate(project, sdk, null, indicator, defines);
		}
		finally
		{
			project.putUserData(ourInProgressFlag, null);
		}

		if(runValidator)
		{
			UnityPluginValidator.runValidation(project);
		}

		if(requestor != null)
		{
			UIUtil.invokeLaterIfNeeded(() -> UnityOpenFilePostHandler.openFile(project, requestor));
		}
	}

	@Nonnull
	private static List<Module> importOrUpdate(@Nonnull Project project,
											   @Nullable Sdk unitySdk,
											   @Nullable ModifiableModuleModel originalModel,
											   @Nonnull ProgressIndicator progressIndicator,
											   @Nullable Collection<String> defines)
	{
		boolean fromProjectStructure = originalModel != null;

		VirtualFile baseDir = project.getBaseDir();
		assert baseDir != null;

		UnityProjectImportContext context = UnityProjectImportContext.load(project, defines, baseDir, progressIndicator, unitySdk);

		ModifiableModuleModel newModel = fromProjectStructure ? originalModel : AccessRule.read(() -> ModuleManager.getInstance(project).getModifiableModel());
		assert newModel != null;

		List<Module> modules = new ArrayList<>(5);

		ContainerUtil.addIfNotNull(modules, createRootModule(project, newModel, unitySdk, progressIndicator, context));
		progressIndicator.setFraction(0.1);

		MultiMap<Module, VirtualFile> sourceFilesByModule = MultiMap.create();

		VirtualFile packagesDir = baseDir.findChild("Packages");
		if(packagesDir != null && packagesDir.isDirectory())
		{
			for(VirtualFile file : packagesDir.getChildren())
			{
				if(file.isDirectory() && file.findChild("package.json") != null)
				{
					modules.add(createPackageModule(file, newModel, context));
				}
			}
		}

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleFirstPass(newModel, sourceFilesByModule, context));
		progressIndicator.setFraction(0.25);

		ContainerUtil.addIfNotNull(modules, createAssemblyUnityScriptModuleFirstPass(newModel, sourceFilesByModule, context));
		progressIndicator.setFraction(0.5);

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleEditor(newModel, sourceFilesByModule, context));
		progressIndicator.setFraction(0.75);

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModule(newModel, unitySdk, sourceFilesByModule, context));

		progressIndicator.setFraction(1);
		progressIndicator.setText(null);

		if(!fromProjectStructure)
		{
			WriteAction.runAndWait(newModel::commit);
		}
		return modules;
	}

	private static Module createPackageModule(@Nonnull VirtualFile packageDir, @Nonnull ModifiableModuleModel modifiableModuleModels, @Nonnull UnityProjectImportContext context)
	{
		String moduleName = packageDir.getName();

		context.getProgressIndicator().setText(Unity3dBundle.message("syncing.0.module", moduleName));

		Module temp = modifiableModuleModels.findModuleByName(moduleName);
		final Module module;
		if(temp == null)
		{
			module = modifiableModuleModels.newModule(moduleName, null);
		}
		else
		{
			module = temp;
		}

		final ModifiableRootModel modifiableModel = AccessRule.read(() -> ModuleRootManager.getInstance(module).getModifiableModel());
		assert modifiableModel != null;

		fillModuleDependencies(module, modifiableModel, List.of(packageDir), it ->
		{
			ContentEntry entry = it.addContentEntry(packageDir);

			entry.addFolder(packageDir, ProductionContentFolderTypeProvider.getInstance());
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, new MultiMap<>(), context, false);

		context.addPackageModule(packageDir.getName());

		return module;
	}

	private static Module createAssemblyCSharpModule(ModifiableModuleModel newModel,
													 Sdk unityBundle,
													 MultiMap<Module, VirtualFile> virtualFilesByModule,
													 UnityProjectImportContext context)
	{
		VirtualFile mainDir = context.getProject().getBaseDir().findFileByRelativePath(ASSETS_DIRECTORY);
		List<VirtualFile> moduleDirs = mainDir == null ? List.of() : List.of(mainDir);
		return createAndSetupModule("Assembly-CSharp", newModel, moduleDirs, layer ->
		{
			if(!isVersionHigherOrEqual(unityBundle, "2018.2"))
			{
				layer.addInvalidModuleEntry(ASSEMBLY_UNITYSCRIPT_FIRSTPASS);
			}

			layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, context);
	}

	private static Module createAssemblyUnityScriptModuleFirstPass(ModifiableModuleModel newModel, MultiMap<Module, VirtualFile> virtualFilesByModule, UnityProjectImportContext context)
	{
		if(isVersionHigherOrEqual(context.getUnityBundle(), "2018.2"))
		{
			Module module = newModel.findModuleByName(ASSEMBLY_UNITYSCRIPT_FIRSTPASS);
			if(module != null)
			{
				newModel.disposeModule(module);
			}
			return null;
		}
		else
		{
			VirtualFile baseDir = context.getProject().getBaseDir();
			List<VirtualFile> moduleDirs = new ArrayList<>();
			for(String passPath : FIRST_PASS_PATHS)
			{
				VirtualFile file = baseDir.findFileByRelativePath(passPath);
				if(file != null)
				{
					moduleDirs.add(file);
				}
			}

			return createAndSetupModule(ASSEMBLY_UNITYSCRIPT_FIRSTPASS, newModel, moduleDirs, null, "unity3d-unityscript-child", JavaScriptFileType.INSTANCE,
					virtualFilesByModule, context);
		}
	}

	private static Module createAssemblyCSharpModuleFirstPass(ModifiableModuleModel newModel, MultiMap<Module, VirtualFile> virtualFilesByModule, UnityProjectImportContext context)
	{
		VirtualFile baseDir = context.getProject().getBaseDir();
		List<VirtualFile> moduleDirs = new ArrayList<>();
		for(String passPath : FIRST_PASS_PATHS)
		{
			VirtualFile file = baseDir.findFileByRelativePath(passPath);
			if(file != null)
			{
				moduleDirs.add(file);
			}
		}
		return createAndSetupModule("Assembly-CSharp-firstpass", newModel, moduleDirs, null, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, context);
	}

	private static Module createAssemblyCSharpModuleEditor(ModifiableModuleModel newModel, MultiMap<Module, VirtualFile> virtualFilesByModule, UnityProjectImportContext context)
	{
		Project project = context.getProject();

		final List<VirtualFile> moduleDirs = new ArrayList<>();

		final VirtualFile baseDir = project.getBaseDir();

		final VirtualFile assetsDir = baseDir.findFileByRelativePath(ASSETS_DIRECTORY);
		if(assetsDir != null)
		{
			VfsUtil.visitChildrenRecursively(assetsDir, new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@Nonnull VirtualFile file)
				{
					if(file.isDirectory() && StringUtil.equalsIgnoreCase("Editor", file.getNameSequence()))
					{
						moduleDirs.add(file);
					}
					return true;
				}
			});
		}

		return createAndSetupModule("Assembly-CSharp-Editor", newModel, moduleDirs, layer ->
		{
			Sdk unityBundle = context.getUnityBundle();
			if(!isVersionHigherOrEqual(unityBundle, "2018.2"))
			{
				layer.addInvalidModuleEntry(ASSEMBLY_UNITYSCRIPT_FIRSTPASS);
			}

			layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
			layer.addInvalidModuleEntry("Assembly-CSharp");

			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor.Graphs"));

			if(isVersionHigherOrEqual(unityBundle, "4.6.0"))
			{
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor.UI"));
			}

			if(isVersionHigherOrEqual(unityBundle, "5.3.0"))
			{
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "nunit.framework"));
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.TestRunner"));
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor.TestRunner"));

				// enable nunit
				layer.getExtensionWithoutCheck(Unity3dNUnitMutableModuleExtension.class).setEnabled(true);
			}

			if(isVersionHigherOrEqual(unityBundle, "2017.1.0"))
			{
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor.Timeline"));
			}

			if(isVuforiaEnabled(unityBundle, project))
			{
				layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "Vuforia.UnityExtensions.Editor"));
			}
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, context);
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	private static Module createAndSetupModule(@Nonnull String moduleName,
											   @Nonnull ModifiableModuleModel modifiableModuleModels,
											   @Nonnull Collection<VirtualFile> moduleDirs,
											   @Nullable Consumer<ModuleRootLayerImpl> setupConsumer,
											   @Nonnull String moduleExtensionId,
											   @Nonnull FileType fileType,
											   @Nonnull MultiMap<Module, VirtualFile> virtualFilesByModule,
											   @Nonnull UnityProjectImportContext context)
	{
		Project project = context.getProject();
		ProgressIndicator progressIndicator = context.getProgressIndicator();

		progressIndicator.setText(Unity3dBundle.message("syncing.0.module", moduleName));

		Module temp = modifiableModuleModels.findModuleByName(moduleName);
		final Module module;
		if(temp == null)
		{
			module = modifiableModuleModels.newModule(moduleName, null);
		}
		else
		{
			module = temp;
		}

		final ModifiableRootModel modifiableModel = AccessRule.read(() -> ModuleRootManager.getInstance(module).getModifiableModel());
		assert modifiableModel != null;

		fillModuleDependencies(module, modifiableModel, moduleDirs, setupConsumer, moduleExtensionId, fileType, virtualFilesByModule, context, true);

		RunManager runManager = RunManager.getInstance(project);
		List<RunConfiguration> allConfigurationsList = runManager.getAllConfigurationsList();

		Optional<RunConfiguration> first = allConfigurationsList.stream().filter(runConfiguration -> UNITY_EDITOR_ATTACH.equals(runConfiguration.getName())).findFirst();
		if(!first.isPresent())
		{
			ConfigurationFactory factory = Unity3dAttachApplicationType.getInstance().getConfigurationFactories()[0];

			RunnerAndConfigurationSettings configurationSettings = runManager.createRunConfiguration(UNITY_EDITOR_ATTACH, factory);
			Unity3dAttachConfiguration configuration = (Unity3dAttachConfiguration) configurationSettings.getConfiguration();
			configuration.setAttachTarget(Unity3dAttachConfiguration.AttachTarget.UNITY_EDITOR);

			configurationSettings.setSingleton(true);

			runManager.addConfiguration(configurationSettings, false);

			AccessRule.read(() -> runManager.setSelectedConfiguration(configurationSettings));
		}
		return module;
	}

	private static void fillModuleDependencies(@Nonnull Module module,
											   @Nonnull ModifiableRootModel modifiableRootModel,
											   @Nonnull Collection<VirtualFile> moduleSources,
											   @Nullable Consumer<ModuleRootLayerImpl> setupConsumer,
											   @Nonnull String moduleExtensionId,
											   @Nonnull FileType fileType,
											   @Nonnull MultiMap<Module, VirtualFile> virtualFilesByModule,
											   @Nonnull UnityProjectImportContext context,
											   boolean isUnityModule)
	{
		Project project = context.getProject();
		ProgressIndicator progressIndicator = context.getProgressIndicator();

		final List<VirtualFile> toAdd = new ArrayList<>();
		final List<VirtualFile> libraryFiles = new ArrayList<>();

		final double fraction = progressIndicator.getFraction();
		int i = 0;
		for(VirtualFile dir : moduleSources)
		{
			VfsUtil.visitChildrenRecursively(dir, new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@Nonnull VirtualFile file)
				{
					if(isUnityModule && file.getFileType() == fileType)
					{
						if(virtualFilesByModule.containsScalarValue(file))
						{
							return true;
						}

						virtualFilesByModule.putValue(module, file);
						toAdd.add(file);

						VirtualFile parent = file.getParent();
						VirtualFile metaFile = parent.findChild(file.getName() + "." + Unity3dMetaFileType.INSTANCE.getDefaultExtension());
						if(metaFile != null)
						{
							toAdd.add(metaFile);
						}
					}
					else if(file.getFileType() == DotNetModuleFileType.INSTANCE)
					{
						libraryFiles.add(file);
					}
					return true;
				}
			});

			final double newFraction = fraction + 0.25f * (i / (float) moduleSources.size());
			UIUtil.invokeLaterIfNeeded(() -> progressIndicator.setFraction(newFraction));
		}

		modifiableRootModel.removeAllLayers(true);

		// it will return Default layer
		final ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableRootModel.getCurrentLayer();

		for(VirtualFile virtualFile : toAdd)
		{
			layer.addSingleContentEntry(virtualFile);
		}

		if(setupConsumer != null)
		{
			Application.get().runReadAction(() -> setupConsumer.consume(layer));
		}

		layer.getExtensionWithoutCheck(Unity3dChildMutableModuleExtension.class).setEnabled(true);
		// enable correct unity lang extension
		MutableModuleExtension langExtension = layer.<MutableModuleExtension>getExtensionWithoutCheck(moduleExtensionId);
		assert langExtension != null;
		langExtension.setEnabled(true);

		Sdk unityBundle = context.getUnityBundle();
		if(langExtension instanceof CSharpSimpleMutableModuleExtension)
		{
			CSharpLanguageVersion languageVersion;
			if(isVersionHigherOrEqual(unityBundle, "2018.3"))
			{
				languageVersion = CSharpLanguageVersion.HIGHEST;
			}
			else if(isVersionHigherOrEqual(unityBundle, "5.5.0"))
			{
				languageVersion = CSharpLanguageVersion._6_0;
			}
			else
			{
				languageVersion = CSharpLanguageVersion._4_0;
			}
			((CSharpSimpleMutableModuleExtension) langExtension).setLanguageVersion(languageVersion);
		}

		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "mscorlib"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine"));

		if(isUnityModule)
		{
			for(Map.Entry<String, String> entry : context.getManifest().dependencies.entrySet())
			{
				String name = entry.getKey();
				String value = entry.getValue();
				if(value.startsWith("file"))
				{
					try
					{
						URL url = new URL(value);
						File targetDirectory = new File(url.getFile());
						String ideaUrl = VfsUtilCore.pathToUrl(targetDirectory.getPath());
						layer.addOrderEntry(new Unity3dPackageOrderEntry(layer, name, null, ideaUrl));
					}
					catch(Exception e)
					{
						LOG.warn(e);
					}
				}
				// git url
				// we can't calculate without unity. try guest from Library dir
				else if(value.startsWith("git") || value.startsWith("https") || value.endsWith(".git"))
				{
					Path path = Paths.get(project.getBasePath(), "Library", "PackageCache");
					if(Files.exists(path))
					{
						try
						{
							Optional<Path> firstDir = Files.walk(path, 1).filter(it -> it.getFileName().toString().startsWith(name)).findFirst();
							if(firstDir.isPresent())
							{
								Path libraryHome = firstDir.get();
								String ideaUrl = VfsUtilCore.pathToUrl(libraryHome.toString());
								layer.addOrderEntry(new Unity3dPackageOrderEntry(layer, name, null, ideaUrl));
							}
						}
						catch(IOException e)
						{
							LOG.warn(e);
						}
					}
				}
				else
				{
					layer.addOrderEntry(new Unity3dPackageOrderEntry(layer, name, value, null));
				}
			}

			for(String moduleName : context.getPackageModules())
			{
				AccessRule.read(() -> layer.addInvalidModuleEntry(moduleName));
			}
		}

		if(isVersionHigherOrEqual(unityBundle, "4.6.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.UI"));
		}

		if(isVersionHigherOrEqual(unityBundle, "5.1.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Networking"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Analytics"));
		}
		if(isVersionHigherOrEqual(unityBundle, "5.2.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Advertisements"));
		}

		if(isVersionHigherOrEqual(unityBundle, "5.3.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Purchasing"));
		}

		if(isVersionHigherOrEqual(unityBundle, "2017.1.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Timeline"));
		}

		if(isVuforiaEnabled(unityBundle, project))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "Vuforia.UnityExtensions"));
		}

		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Core"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Runtime.Serialization"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml.Linq"));

		for(VirtualFile virtualFile : libraryFiles)
		{
			addAsLibrary(virtualFile, layer);
		}

		WriteAction.runAndWait(modifiableRootModel::commit);
	}

	private static boolean isVuforiaEnabled(Sdk unityBundle, Project project)
	{
		boolean versionHigherOrEqual = isVersionHigherOrEqual(unityBundle, "2017.2.0");

		VirtualFile baseDir = project.getBaseDir();
		assert baseDir != null;
		VirtualFile assetsDir = baseDir.findChild("Assets");
		VirtualFile vuforia = assetsDir == null ? null : assetsDir.findChild("Vuforia");
		return versionHigherOrEqual && vuforia != null;
	}

	@Nonnull
	private static Version parseBundleVersion(@Nullable Sdk unityBundle)
	{
		String currentVersionString = unityBundle == null ? Unity3dBundleType.UNKNOWN_VERSION : unityBundle.getVersionString();
		return parseVersion(currentVersionString);
	}

	private static boolean isVersionHigherOrEqual(@Nullable Sdk unityBundle, @Nonnull String requiredVersionString)
	{
		Version currentVersion = parseBundleVersion(unityBundle);
		Version requiredVersion = parseVersion(requiredVersionString);
		return currentVersion.isOrGreaterThan(requiredVersion.major, requiredVersion.minor, requiredVersion.bugfix);
	}

	@Nonnull
	public static Version parseVersion(@Nullable String versionString)
	{
		if(versionString == null)
		{
			return new Version(0, 0, 0);
		}
		List<String> list = StringUtil.split(versionString, ".");
		if(list.size() >= 3)
		{
			try
			{
				return new Version(Integer.parseInt(list.get(0)), Integer.parseInt(list.get(1)), Integer.parseInt(list.get(2)));
			}
			catch(NumberFormatException ignored)
			{
			}
		}
		return new Version(0, 0, 0);
	}

	private static void addAsLibrary(VirtualFile virtualFile, ModuleRootLayerImpl layer)
	{
		if(virtualFile.getFileType() == DotNetModuleFileType.INSTANCE)
		{
			VirtualFile archiveRootForLocalFile = ArchiveVfsUtil.getArchiveRootForLocalFile(virtualFile);
			if(archiveRootForLocalFile != null)
			{
				Library library = layer.getModuleLibraryTable().createLibrary();
				Library.ModifiableModel modifiableModel = library.getModifiableModel();
				modifiableModel.addRoot(archiveRootForLocalFile, BinariesOrderRootType.getInstance());
				VirtualFile docFile = virtualFile.getParent().findChild(virtualFile.getNameWithoutExtension() + ".xml");
				if(docFile != null)
				{
					modifiableModel.addRoot(docFile, DocumentationOrderRootType.getInstance());
				}
				modifiableModel.commit();

				LibraryOrderEntry libraryOrderEntry = layer.findLibraryOrderEntry(library);
				assert libraryOrderEntry != null;
				libraryOrderEntry.setExported(true);
			}
		}
	}

	@Nonnull
	private static Module createRootModule(@Nonnull Project project,
										   @Nonnull ModifiableModuleModel newModel,
										   @Nullable Sdk unityBundle,
										   @Nonnull ProgressIndicator progressIndicator,
										   @Nonnull UnityProjectImportContext context)
	{
		SimpleReference<String> namespacePrefix = SimpleReference.create();
		Set<String> excludedUrls = new TreeSet<>();

		String projectUrl = project.getBaseDir().getUrl();
		excludedUrls.add(projectUrl + "/" + Project.DIRECTORY_STORE_FOLDER);
		excludedUrls.add(projectUrl + "/Library");
		excludedUrls.add(projectUrl + "/Temp");
		excludedUrls.add(projectUrl + "/test_Data");

		final Module rootModule;
		Unity3dRootModuleExtension rootModuleExtension = AccessRule.read(() -> Unity3dModuleExtensionUtil.getRootModuleExtension(project));
		if(rootModuleExtension != null)
		{
			namespacePrefix.set(rootModuleExtension.getNamespacePrefix());

			rootModule = rootModuleExtension.getModule();
			AccessRule.read(() ->
			{
				ContentFolder[] contentFolders = ModuleRootManager.getInstance(rootModule).getContentFolders(ContentFolderScopes.excluded());
				for(ContentFolder contentFolder : contentFolders)
				{
					excludedUrls.add(contentFolder.getUrl());
				}
			});
		}
		else
		{
			rootModule = newModel.newModule(project.getName(), project.getBasePath());
		}

		progressIndicator.setText(Unity3dBundle.message("syncing.0.module", rootModule.getName()));


		final ModifiableRootModel modifiableModel = AccessRule.read(() -> ModuleRootManager.getInstance(rootModule).getModifiableModel());
		assert modifiableModel != null;
		modifiableModel.removeAllLayers(true);

		// return Default layer
		ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableModel.getCurrentLayer();

		ContentEntry contentEntry = layer.addContentEntry(projectUrl);

		Unity3dRootMutableModuleExtension extension = layer.getExtensionWithoutCheck(Unity3dRootMutableModuleExtension.class);
		assert extension != null;
		extension.setEnabled(true);
		extension.setNamespacePrefix(namespacePrefix.get());
		extension.setScriptRuntimeVersion(context.getScriptRuntimeVersion());
		extension.getInheritableSdk().set(null, unityBundle);

		List<String> variables = extension.getVariables();
		variables.addAll(context.calculateDefines(unityBundle));

		// exclude temp dirs
		for(String excludedUrl : excludedUrls)
		{
			contentEntry.addFolder(excludedUrl, ExcludedContentFolderTypeProvider.getInstance());
		}

		WriteAction.runAndWait(modifiableModel::commit);
		return rootModule;
	}

	@Nonnull
	public static Unity3dDefineByVersion getUnity3dDefineByVersion(@Nullable Sdk sdk)
	{
		Version currentBundleVersion = parseBundleVersion(sdk);
		return Unity3dDefineByVersion.find(currentBundleVersion.toString());
	}
}
