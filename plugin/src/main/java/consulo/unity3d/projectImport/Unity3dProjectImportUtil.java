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
import com.intellij.openapi.application.ApplicationManager;
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
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ArrayUtil;
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
import consulo.module.extension.MutableModuleExtension;
import consulo.roots.ContentFolderScopes;
import consulo.roots.impl.ExcludedContentFolderTypeProvider;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.roots.types.BinariesOrderRootType;
import consulo.roots.types.DocumentationOrderRootType;
import consulo.unity3d.Unity3dBundle;
import consulo.unity3d.Unity3dMetaFileType;
import consulo.unity3d.UnityPluginFileValidator;
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
import consulo.unity3d.packages.Unity3dManifest;
import consulo.unity3d.packages.orderEntry.Unity3dPackageOrderEntry;
import consulo.unity3d.run.Unity3dAttachApplicationType;
import consulo.unity3d.run.Unity3dAttachConfiguration;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;
import consulo.vfs.util.ArchiveVfsUtil;
import org.jetbrains.yaml.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author VISTALL
 * @since 03.04.2015
 */
public class Unity3dProjectImportUtil
{
	public static final String ASSETS_DIRECTORY = "Assets";

	public static final String[] FIRST_PASS_PATHS = new String[]{
			"Assets/Standard Assets",
			"Assets/Pro Standard Assets",
			"Assets/Plugins"
	};

	private static final String UNITY_EDITOR = "UNITY_EDITOR";

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

			Ref<Boolean> received = Ref.create(Boolean.FALSE);

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
			UnityPluginFileValidator.runValidation(project);
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

		Unity3dManifest manifest = Unity3dManifest.parse(project);

		int scriptRuntimeVersion = 0;
		VirtualFile projectSettingsFile = baseDir.findFileByRelativePath("ProjectSettings/ProjectSettings.asset");
		if(projectSettingsFile != null && projectSettingsFile.getFileType() == Unity3dYMLAssetFileType.INSTANCE)
		{
			Integer version = AccessRule.read(() ->
			{
				PsiFile file = PsiManager.getInstance(project).findFile(projectSettingsFile);
				if(file instanceof YAMLFile)
				{
					List<YAMLDocument> documents = ((YAMLFile) file).getDocuments();
					for(YAMLDocument document : documents)
					{
						YAMLValue topLevelValue = document.getTopLevelValue();
						if(topLevelValue instanceof YAMLMapping)
						{
							YAMLKeyValue playerSettings = ((YAMLMapping) topLevelValue).getKeyValueByKey("PlayerSettings");
							if(playerSettings != null)
							{
								YAMLValue value = playerSettings.getValue();
								if(value instanceof YAMLMapping)
								{
									YAMLKeyValue scriptingRuntimeVersion = ((YAMLMapping) value).getKeyValueByKey("scriptingRuntimeVersion");
									if(scriptingRuntimeVersion != null)
									{
										String valueText = scriptingRuntimeVersion.getValueText();
										return StringUtil.parseInt(valueText, 0);
									}
								}
							}
						}
					}
				}

				return 0;
			});
			assert version != null;
			scriptRuntimeVersion = version;
		}

		final ModifiableModuleModel newModel = fromProjectStructure ? originalModel : AccessRule.read(() -> ModuleManager.getInstance(project).getModifiableModel());

		List<Module> modules = new ArrayList<>(5);

		ContainerUtil.addIfNotNull(modules, createRootModule(project, newModel, unitySdk, progressIndicator, defines, scriptRuntimeVersion));
		progressIndicator.setFraction(0.1);

		MultiMap<Module, VirtualFile> sourceFilesByModule = MultiMap.create();

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleFirstPass(project, newModel, unitySdk, sourceFilesByModule, progressIndicator, manifest));
		progressIndicator.setFraction(0.25);

		ContainerUtil.addIfNotNull(modules, createAssemblyUnityScriptModuleFirstPass(project, newModel, unitySdk, sourceFilesByModule, progressIndicator, manifest));
		progressIndicator.setFraction(0.5);

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleEditor(project, newModel, unitySdk, sourceFilesByModule, progressIndicator, manifest));
		progressIndicator.setFraction(0.75);

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModule(project, newModel, unitySdk, sourceFilesByModule, progressIndicator, manifest));

		progressIndicator.setFraction(1);
		progressIndicator.setText(null);

		if(!fromProjectStructure)
		{
			WriteAction.runAndWait(newModel::commit);
		}
		return modules;
	}

	private static Module createAssemblyCSharpModule(Project project,
													 ModifiableModuleModel newModel,
													 final Sdk unityBundle,
													 MultiMap<Module, VirtualFile> virtualFilesByModule,
													 ProgressIndicator progressIndicator,
													 Unity3dManifest manifest)
	{
		String[] paths = {ASSETS_DIRECTORY};
		return createAndSetupModule("Assembly-CSharp", project, newModel, paths, unityBundle, layer ->
		{
			layer.addInvalidModuleEntry("Assembly-UnityScript-firstpass");
			layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, progressIndicator, manifest);
	}

	private static Module createAssemblyUnityScriptModuleFirstPass(Project project,
																   ModifiableModuleModel newModel,
																   Sdk unityBundle,
																   MultiMap<Module, VirtualFile> virtualFilesByModule,
																   ProgressIndicator progressIndicator,
																   Unity3dManifest manifest)
	{
		return createAndSetupModule("Assembly-UnityScript-firstpass", project, newModel, FIRST_PASS_PATHS, unityBundle, null, "unity3d-unityscript-child", JavaScriptFileType.INSTANCE,
				virtualFilesByModule, progressIndicator, manifest);
	}

	private static Module createAssemblyCSharpModuleFirstPass(Project project,
															  ModifiableModuleModel newModel,
															  Sdk unityBundle,
															  MultiMap<Module, VirtualFile> virtualFilesByModule,
															  ProgressIndicator progressIndicator,
															  Unity3dManifest manifest)
	{
		return createAndSetupModule("Assembly-CSharp-firstpass", project, newModel, FIRST_PASS_PATHS, unityBundle, null, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule,
				progressIndicator, manifest);
	}

	private static Module createAssemblyCSharpModuleEditor(final Project project,
														   ModifiableModuleModel newModel,
														   final Sdk unityBundle,
														   MultiMap<Module, VirtualFile> virtualFilesByModule,
														   ProgressIndicator progressIndicator,
														   Unity3dManifest manifest)
	{
		final List<String> paths = new ArrayList<>();
		paths.add("Assets/Standard Assets/Editor");
		paths.add("Assets/Pro Standard Assets/Editor");
		paths.add("Assets/Plugins/Editor");

		final VirtualFile baseDir = project.getBaseDir();

		final VirtualFile assetsDir = baseDir.findFileByRelativePath(ASSETS_DIRECTORY);
		if(assetsDir != null)
		{
			VfsUtil.visitChildrenRecursively(assetsDir, new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@Nonnull VirtualFile file)
				{
					if(file.isDirectory() && "Editor".equals(file.getName()))
					{
						paths.add(VfsUtil.getRelativePath(file, baseDir, '/'));
					}
					return true;
				}
			});
		}

		final String[] pathsAsArray = ArrayUtil.toStringArray(paths);
		return createAndSetupModule("Assembly-CSharp-Editor", project, newModel, pathsAsArray, unityBundle, layer ->
		{
			layer.addInvalidModuleEntry("Assembly-UnityScript-firstpass");
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
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, progressIndicator, manifest);
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	private static Module createAndSetupModule(@Nonnull String moduleName,
											   @Nonnull Project project,
											   @Nonnull ModifiableModuleModel modifiableModuleModels,
											   @Nonnull String[] paths,
											   @Nullable Sdk unityBundle,
											   @Nullable Consumer<ModuleRootLayerImpl> setupConsumer,
											   @Nonnull String moduleExtensionId,
											   @Nonnull FileType fileType,
											   @Nonnull MultiMap<Module, VirtualFile> virtualFilesByModule,
											   @Nonnull ProgressIndicator progressIndicator,
											   @Nonnull Unity3dManifest manifest)
	{
		progressIndicator.setText(Unity3dBundle.message("syncing.0.module", moduleName));

		for(int i = 0; i < paths.length; i++)
		{
			paths[i] = project.getBasePath() + "/" + paths[i];
		}

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

		final List<VirtualFile> toAdd = new ArrayList<>();
		final List<VirtualFile> libraryFiles = new ArrayList<>();

		final double fraction = progressIndicator.getFraction();
		for(int i = 0; i < paths.length; i++)
		{
			String path = paths[i];

			VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(path);
			if(fileByPath != null)
			{
				VfsUtil.visitChildrenRecursively(fileByPath, new VirtualFileVisitor()
				{
					@Override
					public boolean visitFile(@Nonnull VirtualFile file)
					{
						if(file.getFileType() == fileType)
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
			}

			final double newFraction = fraction + 0.25f * (i / (float) paths.length);
			UIUtil.invokeLaterIfNeeded(() -> progressIndicator.setFraction(newFraction));
		}

		modifiableModel.removeAllLayers(true);

		// it will return Default layer
		final ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableModel.getCurrentLayer();

		for(VirtualFile virtualFile : toAdd)
		{
			layer.addContentEntry(virtualFile);
		}

		if(setupConsumer != null)
		{
			ApplicationManager.getApplication().runReadAction(() -> setupConsumer.consume(layer));
		}

		layer.getExtensionWithoutCheck(Unity3dChildMutableModuleExtension.class).setEnabled(true);
		// enable correct unity lang extension
		MutableModuleExtension langExtension = layer.<MutableModuleExtension>getExtensionWithoutCheck(moduleExtensionId);
		assert langExtension != null;
		langExtension.setEnabled(true);
		if(langExtension instanceof CSharpSimpleMutableModuleExtension)
		{
			CSharpLanguageVersion languageVersion;
			if(isVersionHigherOrEqual(unityBundle, "2019.0"))
			{
				languageVersion = CSharpLanguageVersion._7_1;
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

		for(Map.Entry<String, String> entry : manifest.getFilteredDependencies().entrySet())
		{
			layer.addOrderEntry(new Unity3dPackageOrderEntry(layer, entry.getKey() + "@" + entry.getValue()));
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

		WriteAction.runAndWait(modifiableModel::commit);
		return module;
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
	private static Module createRootModule(@Nonnull final Project project,
										   @Nonnull ModifiableModuleModel newModel,
										   @Nullable Sdk unityBundle,
										   @Nonnull ProgressIndicator progressIndicator,
										   @Nullable Collection<String> defines,
										   int scriptRuntimeVersion)
	{
		Ref<String> namespacePrefix = Ref.create();
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

		modifiableModel.removeAllLayers(true);

		// return Default layer
		ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableModel.getCurrentLayer();

		ContentEntry contentEntry = layer.addContentEntry(projectUrl);

		Unity3dRootMutableModuleExtension extension = layer.getExtensionWithoutCheck(Unity3dRootMutableModuleExtension.class);
		assert extension != null;
		extension.setEnabled(true);
		extension.setNamespacePrefix(namespacePrefix.get());
		extension.setScriptRuntimeVersion(scriptRuntimeVersion);
		extension.getInheritableSdk().set(null, unityBundle);

		List<String> variables = extension.getVariables();
		if(defines != null)
		{
			variables.addAll(defines);
		}
		// fallback
		else
		{
			variables.add(UNITY_EDITOR);
			variables.add("DEBUG");
			variables.add("TRACE");

			Unity3dDefineByVersion unity3dDefineByVersion = getUnity3dDefineByVersion(unityBundle);
			if(unity3dDefineByVersion != Unity3dDefineByVersion.UNKNOWN)
			{
				for(Unity3dDefineByVersion majorVersion : unity3dDefineByVersion.getMajorVersions())
				{
					variables.add(majorVersion.name());
				}
				variables.add(unity3dDefineByVersion.name());
			}
		}

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
