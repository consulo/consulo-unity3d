package consulo.unity3d.projectImport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.consulo.module.extension.MutableModuleExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFilePropertyPusher;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.dotnet.dll.DotNetModuleFileType;
import org.mustbe.consulo.roots.impl.ExcludedContentFolderTypeProvider;
import org.mustbe.consulo.unity3d.Unity3dBundle;
import org.mustbe.consulo.unity3d.Unity3dMetaFileType;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import org.mustbe.consulo.unity3d.bundle.Unity3dDefineByVersion;
import org.mustbe.consulo.unity3d.module.Unity3dChildMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtensionUtil;
import org.mustbe.consulo.unity3d.module.Unity3dRootModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dRootMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.Unity3dTarget;
import org.mustbe.consulo.unity3d.nunit.module.extension.Unity3dNUnitMutableModuleExtension;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.application.ApplicationManager;
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
import com.intellij.openapi.roots.impl.ModuleRootLayerImpl;
import com.intellij.openapi.roots.impl.PushedFilePropertiesUpdater;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.types.BinariesOrderRootType;
import com.intellij.openapi.roots.types.DocumentationOrderRootType;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.util.ArchiveVfsUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.ui.UIUtil;
import consulo.dotnet.roots.orderEntry.DotNetLibraryOrderEntryImpl;
import consulo.lombok.annotations.Logger;
import consulo.unity3d.UnityPluginFileValidator;

/**
 * @author VISTALL
 * @since 03.04.2015
 */
@Logger
public class Unity3dProjectUtil
{
	public static final String ASSETS_DIRECTORY = "Assets";

	public static final Key<Getter<Sdk>> NEWLY_IMPORTED_PROJECT_SDK = Key.create("unity.new.project");

	public static final String[] FIRST_PASS_PATHS = new String[]{
			"Assets/Standard Assets",
			"Assets/Pro Standard Assets",
			"Assets/Plugins"
	};

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

	public static void syncProject(@NotNull final Project project, @Nullable final Sdk sdk, final boolean runValidator)
	{
		new Task.Modal(project, "Sync project", false)
		{
			@Override
			public void run(@NotNull final ProgressIndicator indicator)
			{
				DumbService.allowStartingDumbModeInside(DumbModePermission.MAY_START_BACKGROUND, new Runnable()
				{
					@Override
					public void run()
					{

						try
						{
							project.putUserData(CSharpFilePropertyPusher.ourDisableAnyEvents, Boolean.TRUE);
							Unity3dProjectUtil.importOrUpdate(project, sdk, null, indicator);
						}
						finally
						{
							project.putUserData(CSharpFilePropertyPusher.ourDisableAnyEvents, null);

							PushedFilePropertiesUpdater.getInstance(project).pushAll(new CSharpFilePropertyPusher());
						}

						if(runValidator)
						{
							UnityPluginFileValidator.runValidation(project);
						}
					}
				});
			}
		}.queue();
	}

	@NotNull
	private static List<Module> importOrUpdate(@NotNull final Project project, @Nullable Sdk unitySdk, @Nullable ModifiableModuleModel originalModel, @NotNull ProgressIndicator progressIndicator)
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

		List<Module> modules = new ArrayList<Module>(5);

		ContainerUtil.addIfNotNull(modules, createRootModule(project, newModel, unitySdk, progressIndicator));
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
		final List<String> paths = new ArrayList<String>();
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
			}
		}, "unity3d-csharp-child", CSharpFileType.INSTANCE, virtualFilesByModule, progressIndicator);
	}

	@NotNull
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

		modifiableModel.removeAllLayers(false);

		final List<VirtualFile> toAdd = new ArrayList<VirtualFile>();
		final List<VirtualFile> libraryFiles = new ArrayList<VirtualFile>();

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
			UIUtil.invokeLaterIfNeeded(new Runnable()
			{
				@Override
				public void run()
				{
					progressIndicator.setFraction(newFraction);
				}
			});
		}

		for(final Unity3dTarget unity3dTarget : Unity3dTarget.values())
		{
			final ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableModel.addLayer(unity3dTarget.getPresentation(), null, false);

			for(VirtualFile virtualFile : toAdd)
			{
				layer.addContentEntry(virtualFile);
			}

			if(setupConsumer != null)
			{
				ApplicationManager.getApplication().runReadAction(new Runnable()
				{
					@Override
					public void run()
					{
						setupConsumer.consume(layer);
					}
				});
			}

			layer.getExtensionWithoutCheck(Unity3dChildMutableModuleExtension.class).setEnabled(true);
			// enable correct unity lang extension
			layer.<MutableModuleExtension>getExtensionWithoutCheck(moduleExtensionId).setEnabled(true);

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
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Core"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl(layer, "System.Xml.Linq"));

			for(VirtualFile virtualFile : libraryFiles)
			{
				addAsLibrary(virtualFile, layer);
			}
		}

		modifiableModel.setCurrentLayer(Unity3dTarget.Editor.name());
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
	private static Module createRootModule(@NotNull final Project project, @NotNull ModifiableModuleModel newModel, @Nullable Sdk unityBundle, @NotNull ProgressIndicator progressIndicator)
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
		modifiableModel.removeAllLayers(false);

		for(Unity3dTarget unity3dTarget : Unity3dTarget.values())
		{
			ModuleRootLayerImpl layer = (ModuleRootLayerImpl) modifiableModel.addLayer(unity3dTarget.getPresentation(), null, false);

			ContentEntry contentEntry = layer.addContentEntry(projectUrl);

			Unity3dRootMutableModuleExtension extension = layer.getExtensionWithoutCheck(Unity3dRootMutableModuleExtension.class);
			assert extension != null;
			extension.setEnabled(true);
			extension.setBuildTarget(unity3dTarget);
			extension.getInheritableSdk().set(null, unityBundle);

			extension.getVariables().add(unity3dTarget.getDefineName());

			Unity3dDefineByVersion unity3dDefineByVersion = getUnity3dDefineByVersion(unityBundle);
			if(unity3dDefineByVersion != Unity3dDefineByVersion.UNKNOWN)
			{
				for(Unity3dDefineByVersion majorVersion : unity3dDefineByVersion.getMajorVersions())
				{
					extension.getVariables().add(majorVersion.name());
				}
				extension.getVariables().add(unity3dDefineByVersion.name());
			}

			// exclude temp dirs
			contentEntry.addFolder(projectUrl + "/" + Project.DIRECTORY_STORE_FOLDER, ExcludedContentFolderTypeProvider.getInstance());
			contentEntry.addFolder(projectUrl + "/Library", ExcludedContentFolderTypeProvider.getInstance());
			contentEntry.addFolder(projectUrl + "/Temp", ExcludedContentFolderTypeProvider.getInstance());
			contentEntry.addFolder(projectUrl + "/test_Data", ExcludedContentFolderTypeProvider.getInstance());
		}

		modifiableModel.setCurrentLayer(Unity3dTarget.Editor.name());

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
