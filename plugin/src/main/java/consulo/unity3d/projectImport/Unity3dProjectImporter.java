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

import consulo.application.*;
import consulo.application.eap.EarlyAccessProgramManager;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.content.ContentFolderTypeProvider;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.DocumentationOrderRootType;
import consulo.content.base.ExcludedContentFolderTypeProvider;
import consulo.content.bundle.Sdk;
import consulo.content.library.Library;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.dotnet.impl.roots.orderEntry.DotNetLibraryOrderEntryModel;
import consulo.dotnet.impl.roots.orderEntry.DotNetLibraryOrderEntryType;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunConfiguration;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.language.file.FileTypeManager;
import consulo.logging.Logger;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ContentFolder;
import consulo.module.content.layer.ModifiableModuleRootLayer;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.orderEntry.LibraryOrderEntry;
import consulo.module.extension.MutableModuleExtension;
import consulo.project.Project;
import consulo.project.ui.notification.Notification;
import consulo.project.ui.notification.NotificationType;
import consulo.ui.ex.awt.UIUtil;
import consulo.unity3d.Unity3dMetaFileType;
import consulo.unity3d.UnityNotificationGroup;
import consulo.unity3d.UnityPluginValidator;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.bundle.Unity3dDefineByVersion;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.editor.UnityRequestDefines;
import consulo.unity3d.jsonApi.UnityOpenFilePostHandler;
import consulo.unity3d.jsonApi.UnityOpenFilePostHandlerRequest;
import consulo.unity3d.jsonApi.UnityPingPong;
import consulo.unity3d.jsonApi.UnitySetDefines;
import consulo.unity3d.localize.Unity3dLocalize;
import consulo.unity3d.module.Unity3dChildMutableModuleExtension;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.module.Unity3dRootMutableModuleExtension;
import consulo.unity3d.nunit.module.extension.Unity3dNUnitMutableModuleExtension;
import consulo.unity3d.packages.orderEntry.Unity3dPackageOrderEntryModel;
import consulo.unity3d.packages.orderEntry.Unity3dPackageOrderEntryType;
import consulo.unity3d.projectImport.newImport.UnityProjectImporterWithAsmDef;
import consulo.unity3d.run.Unity3dAttachApplicationType;
import consulo.unity3d.run.Unity3dAttachConfiguration;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.MultiMap;
import consulo.util.dataholder.Key;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.TimeoutUtil;
import consulo.util.lang.Version;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 03.04.2015
 */
public class Unity3dProjectImporter
{
	private static final Logger LOG = Logger.getInstance(Unity3dProjectImporter.class);

	public static final String ASSETS_DIRECTORY = "Assets";
	public static final String PACKAGES_DIRECTORY = "Packages";

	public static final String[] FIRST_PASS_PATHS = new String[]{
			"Assets/Standard Assets",
			"Assets/Pro Standard Assets",
			"Assets/Plugins"
	};

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

	public static void syncProjectStep(@Nonnull final Project project, @Nullable final Sdk sdk, @Nullable UnityOpenFilePostHandlerRequest requestor, final boolean runValidator)
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

				importAfterDefinesInBackground(project, sdk, requestor, runValidator, o);
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
		UIUtil.invokeLaterIfNeeded(() -> new Notification(UnityNotificationGroup.INSTANCE, project.getApplication().getName().get(), "UnityEditor is not responding.<br>Defines is not resolved.",
				NotificationType
						.INFORMATION).notify(project));
	}

	/**
	 * this method will called from webservice thread
	 */
	private static void importAfterDefinesInBackground(@Nonnull final Project project,
													   @Nullable final Sdk sdk,
													   @Nullable UnityOpenFilePostHandlerRequest requestor,
													   final boolean runValidator,
													   UnitySetDefines unitySetDefines)
	{
		Task.Backgroundable.queue(project, "Sync Project", indicator ->
		{
			HeavyProcessLatch.INSTANCE.performOperation(HeavyProcessLatch.Type.Syncing, "Unity Sync Prroject", () ->
			{
				importAfterDefines(project, sdk, runValidator, indicator, requestor, unitySetDefines);
			});
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

			if(EarlyAccessProgramManager.is(UnityAsmDefImportEapDescriptor.class))
			{
				UnityProjectImporterWithAsmDef.importOrUpdate(project, sdk, null, indicator, defines);
			}
			else
			{
				importOrUpdateOld(project, sdk, null, indicator, defines);
			}

			createRunConfiguration(project);
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
	private static List<Module> importOrUpdateOld(@Nonnull Project project,
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

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleEditor(newModel, sourceFilesByModule, context));
		progressIndicator.setFraction(0.50);

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

		context.getProgressIndicator().setTextValue(Unity3dLocalize.syncing0Module(moduleName));

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

		fillModuleDependencies(module, modifiableModel, List.of(packageDir, context.getProject().getBaseDir().findChild(ASSETS_DIRECTORY)), it ->
		{
			ContentEntry entry = it.addContentEntry(packageDir);

			entry.addFolder(packageDir, ProductionContentFolderTypeProvider.getInstance());
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, new MultiMap<>(), context, moduleName);

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
			layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, context);
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
			VirtualFileUtil.visitChildrenRecursively(assetsDir, new VirtualFileVisitor()
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

			layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
			layer.addInvalidModuleEntry("Assembly-CSharp");

			layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("UnityEditor.Graphs"));

			if(isVersionHigherOrEqual(unityBundle, "4.6.0"))
			{
				layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("UnityEditor.UI"));
			}

			if(isVersionHigherOrEqual(unityBundle, "5.3.0"))
			{
				layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("nunit.framework"));
				layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("UnityEngine.TestRunner"));
				layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("UnityEditor.TestRunner"));

				// enable nunit
				layer.getExtensionWithoutCheck(Unity3dNUnitMutableModuleExtension.class).setEnabled(true);
			}

			if(isVersionHigherOrEqual(unityBundle, "2017.1.0"))
			{
				layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("UnityEditor.Timeline"));
			}

			if(isVuforiaEnabled(unityBundle, project))
			{
				layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("Vuforia.UnityExtensions.Editor"));
			}
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, context);
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	private static Module createAndSetupModule(@Nonnull String moduleName,
											   @Nonnull ModifiableModuleModel modifiableModuleModels,
											   @Nonnull Collection<VirtualFile> moduleDirs,
											   @Nullable Consumer<ModifiableModuleRootLayer> setupConsumer,
											   @Nonnull String moduleExtensionId,
											   @Nonnull FileType fileType,
											   @Nonnull MultiMap<Module, VirtualFile> virtualFilesByModule,
											   @Nonnull UnityProjectImportContext context)
	{
		ProgressIndicator progressIndicator = context.getProgressIndicator();

		progressIndicator.setTextValue(Unity3dLocalize.syncing0Module(moduleName));

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

		fillModuleDependencies(module, modifiableModel, moduleDirs, setupConsumer, moduleExtensionId, fileType, virtualFilesByModule, context, null);

		return module;
	}

	private static void createRunConfiguration(Project project)
	{
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
	}

	private static void fillModuleDependencies(@Nonnull Module module,
											   @Nonnull ModifiableRootModel modifiableRootModel,
											   @Nonnull Collection<VirtualFile> moduleSources,
											   @Nullable Consumer<ModifiableModuleRootLayer> setupConsumer,
											   @Nonnull String moduleExtensionId,
											   @Nonnull FileType fileType,
											   @Nonnull MultiMap<Module, VirtualFile> virtualFilesByModule,
											   @Nonnull UnityProjectImportContext context,
											   @Nullable String currentPackageName)
	{
		Project project = context.getProject();
		ProgressIndicator progressIndicator = context.getProgressIndicator();

		final boolean isUnityModule = currentPackageName == null;
		final List<VirtualFile> toAdd = new ArrayList<>();
		final List<VirtualFile> libraryFiles = new ArrayList<>();

		final double fraction = progressIndicator.getFraction();
		int i = 0;
		for(VirtualFile dir : moduleSources)
		{
			VirtualFileUtil.visitChildrenRecursively(dir, new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@Nonnull VirtualFile file)
				{
					if(FileTypeManager.getInstance().isFileIgnored(file))
					{
						return false;
					}

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
		final ModifiableModuleRootLayer layer = (ModifiableModuleRootLayer) modifiableRootModel.getCurrentLayer();

		for(VirtualFile virtualFile : toAdd)
		{
			layer.addSingleContentEntry(virtualFile);
		}

		if(setupConsumer != null)
		{
			Application.get().runReadAction(() -> setupConsumer.accept(layer));
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

		DotNetLibraryOrderEntryType type = DotNetLibraryOrderEntryType.getInstance();

		layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("mscorlib"));
		layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("UnityEditor"));
		layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("UnityEngine"));

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
					String ideaUrl = VirtualFileUtil.pathToUrl(targetDirectory.getPath());
					layer.addCustomOderEntry(Unity3dPackageOrderEntryType.getInstance(), new Unity3dPackageOrderEntryModel(name, null, ideaUrl));
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
							String ideaUrl = VirtualFileUtil.pathToUrl(libraryHome.toString());
							layer.addCustomOderEntry(Unity3dPackageOrderEntryType.getInstance(), new Unity3dPackageOrderEntryModel(name, null, ideaUrl));
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
				layer.addCustomOderEntry(Unity3dPackageOrderEntryType.getInstance(), new Unity3dPackageOrderEntryModel(name, value, null));
			}
		}

		for(String moduleName : context.getPackageModules())
		{
			if(Objects.equals(moduleName, currentPackageName))
			{
				// skip self package reference
				continue;
			}

			AccessRule.read(() -> layer.addInvalidModuleEntry(moduleName));
		}

		if(isVersionHigherOrEqual(unityBundle, "4.6.0"))
		{
			layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("UnityEngine.UI"));
		}

		if(isVersionHigherOrEqual(unityBundle, "5.1.0"))
		{
			layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("UnityEngine.Networking"));
			layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("UnityEngine.Analytics"));
		}
		if(isVersionHigherOrEqual(unityBundle, "5.2.0"))
		{
			layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("UnityEngine.Advertisements"));
		}

		if(isVersionHigherOrEqual(unityBundle, "5.3.0"))
		{
			layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("UnityEngine.Purchasing"));
		}

		if(isVersionHigherOrEqual(unityBundle, "2017.1.0"))
		{
			layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("UnityEngine.Timeline"));
		}

		if(isVuforiaEnabled(unityBundle, project))
		{
			layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("Vuforia.UnityExtensions"));
		}

		layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("System"));
		layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("System.Core"));
		layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("System.Runtime.Serialization"));
		layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("System.Xml"));
		layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("System.Xml.Linq"));
		layer.addCustomOderEntry(type, new DotNetLibraryOrderEntryModel("System.Net.Http"));

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

	public static void addAsLibrary(VirtualFile virtualFile, ModifiableModuleRootLayer layer)
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
	public static Module createRootModule(@Nonnull Project project,
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
				ContentFolder[] contentFolders = ModuleRootManager.getInstance(rootModule).getContentFolders(ContentFolderTypeProvider.onlyExcluded());
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

		progressIndicator.setTextValue(Unity3dLocalize.syncing0Module(rootModule.getName()));


		final ModifiableRootModel modifiableModel = AccessRule.read(() -> ModuleRootManager.getInstance(rootModule).getModifiableModel());
		assert modifiableModel != null;
		modifiableModel.removeAllLayers(true);

		// return Default layer
		ModifiableModuleRootLayer layer = (ModifiableModuleRootLayer) modifiableModel.getCurrentLayer();

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
