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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbModePermission;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.storage.HeavyProcessLatch;
import com.intellij.util.ui.UIUtil;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.dotnet.roots.orderEntry.DotNetLibraryOrderEntryImpl;
import consulo.module.extension.MutableModuleExtension;
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
import consulo.vfs.util.ArchiveVfsUtil;

/**
 * @author VISTALL
 * @since 03.04.2015
 */
public class Unity3dProjectUtil
{
	public static final String ASSETS_DIRECTORY = "Assets";

	public static final String[] FIRST_PASS_PATHS = new String[]{
			"Assets/Standard Assets",
			"Assets/Pro Standard Assets",
			"Assets/Plugins"
	};

	private static final String UNITY_EDITOR = "UNITY_EDITOR";

	public static final Key<Boolean> ourInProgressFlag = Key.create("Unity3dProjectUtil#ourInProgressFlag");

	@Nullable
	public static String loadVersionFromProject(@NotNull String path)
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

	public static void syncProjectStep1(@NotNull final Project project, @Nullable final Sdk sdk, @Nullable UnityOpenFilePostHandlerRequest requestor, final boolean runValidator)
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
				return;
			}

			int i = 0;
			while(!received.get())
			{
				if(i == 5)
				{
					token.finish(null);

					UIUtil.invokeLaterIfNeeded(() -> new Notification("unity", ApplicationNamesInfo.getInstance().getProductName(), "UnityEditor is not responding.<br>Defines is not resolved.",
							NotificationType.WARNING).notify(project));
					break;
				}

				TimeoutUtil.sleep(500L);

				i++;
			}
		});
	}

	/**
	 * this method will called from webservice thread
	 */
	private static void syncProjectStep2(@NotNull final Project project,
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
				DumbService.allowStartingDumbModeInside(DumbModePermission.MAY_START_BACKGROUND, () -> importAfterDefines(project, sdk, runValidator, indicator, requestor, unitySetDefines));
			}
			finally
			{
				accessToken.finish();
			}
		});
	}

	private static void importAfterDefines(@NotNull final Project project,
			@Nullable final Sdk sdk,
			final boolean runValidator,
			@NotNull ProgressIndicator indicator,
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

			Unity3dProjectUtil.importOrUpdate(project, sdk, null, indicator, defines);
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

	@NotNull
	private static List<Module> importOrUpdate(@NotNull final Project project,
			@Nullable Sdk unitySdk,
			@Nullable ModifiableModuleModel originalModel,
			@NotNull ProgressIndicator progressIndicator,
			@Nullable Collection<String> defines)
	{
		boolean fromProjectStructure = originalModel != null;

		final ModifiableModuleModel newModel = fromProjectStructure ? originalModel : ApplicationManager.getApplication().runReadAction(new Computable<ModifiableModuleModel>()
		{
			@Override
			public ModifiableModuleModel compute()
			{
				return ModuleManager.getInstance(project).getModifiableModel();
			}
		});

		List<Module> modules = new ArrayList<>(5);

		ContainerUtil.addIfNotNull(modules, createRootModule(project, newModel, unitySdk, progressIndicator, defines));
		progressIndicator.setFraction(0.1);

		MultiMap<Module, VirtualFile> sourceFilesByModule = MultiMap.create();

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleFirstPass(project, newModel, unitySdk, sourceFilesByModule, progressIndicator));
		progressIndicator.setFraction(0.25);

		ContainerUtil.addIfNotNull(modules, createAssemblyUnityScriptModuleFirstPass(project, newModel, unitySdk, sourceFilesByModule, progressIndicator));
		progressIndicator.setFraction(0.5);

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModuleEditor(project, newModel, unitySdk, sourceFilesByModule, progressIndicator));
		progressIndicator.setFraction(0.75);

		ContainerUtil.addIfNotNull(modules, createAssemblyCSharpModule(project, newModel, unitySdk, sourceFilesByModule, progressIndicator));

		progressIndicator.setFraction(1);
		progressIndicator.setText(null);

		if(!fromProjectStructure)
		{
			new WriteAction<Object>()
			{
				@Override
				protected void run(Result<Object> result) throws Throwable
				{
					newModel.commit();
				}
			}.execute();
		}
		return modules;
	}

	private static Module createAssemblyCSharpModule(Project project,
			ModifiableModuleModel newModel,
			final Sdk unityBundle,
			MultiMap<Module, VirtualFile> virtualFilesByModule,
			ProgressIndicator progressIndicator)
	{
		String[] paths = {ASSETS_DIRECTORY};
		return createAndSetupModule("Assembly-CSharp", project, newModel, paths, unityBundle, new Consumer<ModuleRootLayerImpl>()
		{
			@Override
			public void consume(ModuleRootLayerImpl layer)
			{
				layer.addInvalidModuleEntry("Assembly-UnityScript-firstpass");
				layer.addInvalidModuleEntry("Assembly-CSharp-firstpass");
			}
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, progressIndicator);
	}

	private static Module createAssemblyUnityScriptModuleFirstPass(final Project project,
			ModifiableModuleModel newModel,
			final Sdk unityBundle,
			MultiMap<Module, VirtualFile> virtualFilesByModule,
			ProgressIndicator progressIndicator)
	{

		return createAndSetupModule("Assembly-UnityScript-firstpass", project, newModel, FIRST_PASS_PATHS, unityBundle, null, "unity3d-unityscript-child", JavaScriptFileType.INSTANCE,
				virtualFilesByModule, progressIndicator);
	}

	private static Module createAssemblyCSharpModuleFirstPass(final Project project,
			ModifiableModuleModel newModel,
			Sdk unityBundle,
			MultiMap<Module, VirtualFile> virtualFilesByModule,
			ProgressIndicator progressIndicator)
	{
		return createAndSetupModule("Assembly-CSharp-firstpass", project, newModel, FIRST_PASS_PATHS, unityBundle, null, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule,
				progressIndicator);
	}

	private static Module createAssemblyCSharpModuleEditor(final Project project,
			ModifiableModuleModel newModel,
			final Sdk unityBundle,
			MultiMap<Module, VirtualFile> virtualFilesByModule,
			ProgressIndicator progressIndicator)
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
				public boolean visitFile(@NotNull VirtualFile file)
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
		return createAndSetupModule("Assembly-CSharp-Editor", project, newModel, pathsAsArray, unityBundle, new Consumer<ModuleRootLayerImpl>()
		{
			@Override
			public void consume(final ModuleRootLayerImpl layer)
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

					// enable nunit
					layer.getExtensionWithoutCheck(Unity3dNUnitMutableModuleExtension.class).setEnabled(true);
				}

				if(isVersionHigherOrEqual(unityBundle, "2017.1.0"))
				{
					layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor.Timeline"));
				}
			}
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, progressIndicator);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	private static Module createAndSetupModule(@NotNull String moduleName,
			@NotNull Project project,
			@NotNull ModifiableModuleModel modifiableModuleModels,
			@NotNull String[] paths,
			@Nullable Sdk unitySdk,
			@Nullable final Consumer<ModuleRootLayerImpl> setupConsumer,
			@NotNull String moduleExtensionId,
			@NotNull final FileType fileType,
			@NotNull final MultiMap<Module, VirtualFile> virtualFilesByModule,
			@NotNull final ProgressIndicator progressIndicator)
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

		final ModifiableRootModel modifiableModel = ApplicationManager.getApplication().runReadAction(new Computable<ModifiableRootModel>()
		{
			@Override
			public ModifiableRootModel compute()
			{
				return ModuleRootManager.getInstance(module).getModifiableModel();
			}
		});

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
					public boolean visitFile(@NotNull VirtualFile file)
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
			CSharpLanguageVersion languageVersion = isVersionHigherOrEqual(unitySdk, "5.5.0") ? CSharpLanguageVersion._6_0 : CSharpLanguageVersion._4_0;
			((CSharpSimpleMutableModuleExtension) langExtension).setLanguageVersion(languageVersion);
		}

		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "mscorlib"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEditor"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine"));
		if(isVersionHigherOrEqual(unitySdk, "4.6.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.UI"));
		}
		if(isVersionHigherOrEqual(unitySdk, "5.1.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Networking"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Analytics"));
		}
		if(isVersionHigherOrEqual(unitySdk, "5.2.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Advertisements"));
		}
		if(isVersionHigherOrEqual(unitySdk, "5.3.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Purchasing"));
		}
		if(isVersionHigherOrEqual(unitySdk, "2017.1.0"))
		{
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "UnityEngine.Timeline"));
		}
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Core"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml.Linq"));

		for(VirtualFile virtualFile : libraryFiles)
		{
			addAsLibrary(virtualFile, layer);
		}

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				modifiableModel.commit();
			}
		}.execute();
		return module;
	}

	@NotNull
	private static Version parseBundleVersion(@Nullable Sdk unityBundle)
	{
		String currentVersionString = unityBundle == null ? Unity3dBundleType.UNKNOWN_VERSION : unityBundle.getVersionString();
		return parseVersion(currentVersionString);
	}

	private static boolean isVersionHigherOrEqual(@Nullable Sdk unityBundle, @NotNull String requiredVersionString)
	{
		Version currentVersion = parseBundleVersion(unityBundle);
		Version requiredVersion = parseVersion(requiredVersionString);
		return currentVersion.isOrGreaterThan(requiredVersion.major, requiredVersion.minor, requiredVersion.bugfix);
	}

	@NotNull
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

	@NotNull
	private static Module createRootModule(@NotNull final Project project,
			@NotNull ModifiableModuleModel newModel,
			@Nullable Sdk unityBundle,
			@NotNull ProgressIndicator progressIndicator,
			@Nullable Collection<String> defines)
	{
		final Module rootModule;
		Unity3dRootModuleExtension rootModuleExtension = ApplicationManager.getApplication().runReadAction(new Computable<Unity3dRootModuleExtension>()

		{
			@Override
			public Unity3dRootModuleExtension compute()
			{
				return Unity3dModuleExtensionUtil.getRootModuleExtension(project);
			}
		});
		if(rootModuleExtension != null)
		{
			rootModule = rootModuleExtension.getModule();
		}
		else
		{
			rootModule = newModel.newModule(project.getName(), project.getBasePath());
		}

		progressIndicator.setText(Unity3dBundle.message("syncing.0.module", rootModule.getName()));

		String projectUrl = project.getBaseDir().getUrl();

		final ModifiableRootModel modifiableModel = ApplicationManager.getApplication().runReadAction(new Computable<ModifiableRootModel>()
		{
			@Override
			public ModifiableRootModel compute()
			{
				return ModuleRootManager.getInstance(rootModule).getModifiableModel();
			}
		});

		modifiableModel.removeAllLayers(true);

		// return Default layer
		ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableModel.getCurrentLayer();

		ContentEntry contentEntry = layer.addContentEntry(projectUrl);

		Unity3dRootMutableModuleExtension extension = layer.getExtensionWithoutCheck(Unity3dRootMutableModuleExtension.class);
		assert extension != null;
		extension.setEnabled(true);
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
		contentEntry.addFolder(projectUrl + "/" + Project.DIRECTORY_STORE_FOLDER, ExcludedContentFolderTypeProvider.getInstance());
		contentEntry.addFolder(projectUrl + "/Library", ExcludedContentFolderTypeProvider.getInstance());
		contentEntry.addFolder(projectUrl + "/Temp", ExcludedContentFolderTypeProvider.getInstance());
		contentEntry.addFolder(projectUrl + "/test_Data", ExcludedContentFolderTypeProvider.getInstance());

		new WriteAction<Object>()
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				modifiableModel.commit();
			}
		}.execute();
		return rootModule;
	}

	@NotNull
	public static Unity3dDefineByVersion getUnity3dDefineByVersion(@Nullable Sdk sdk)
	{
		Version currentBundleVersion = parseBundleVersion(sdk);
		return Unity3dDefineByVersion.find(currentBundleVersion.toString());
	}
}
