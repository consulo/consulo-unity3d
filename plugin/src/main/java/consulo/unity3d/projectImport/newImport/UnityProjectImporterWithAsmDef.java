/*
 * Copyright 2013-2021 consulo.io
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

package consulo.unity3d.projectImport.newImport;

import consulo.application.AccessRule;
import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.application.progress.ProgressIndicator;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.content.library.Library;
import consulo.content.library.LibraryTable;
import consulo.content.library.LibraryTablesRegistrar;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.dotnet.impl.roots.orderEntry.DotNetLibraryOrderEntryModel;
import consulo.dotnet.impl.roots.orderEntry.DotNetLibraryOrderEntryType;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.language.file.FileTypeManager;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableModuleRootLayer;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.unity3d.asmdef.AsmDefElement;
import consulo.unity3d.module.Unity3dChildMutableModuleExtension;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.packages.Unity3dPackageWatcher;
import consulo.unity3d.packages.library.UnityPackageLibraryType;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.UnityProjectImportContext;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharp;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharpEditor;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharpFirstPass;
import consulo.unity3d.projectImport.newImport.standardImporter.StandardModuleImporter;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
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

/**
 * @author VISTALL
 * @since 23/03/2021
 */
public class UnityProjectImporterWithAsmDef
{
	private static final Logger LOG = Logger.getInstance(UnityProjectImporterWithAsmDef.class);

	public static final String EDITOR_DIRECTORY = "Editor";
	// not ignored case
	public static final String EDITOR_PLATFORM = "Editor";

	private static StandardModuleImporter[] ourStandardModuleImporters = {
			new AssemblyCSharpFirstPass(),
			new AssemblyCSharpEditor(),
			new AssemblyCSharp(),
	};

	@Nonnull
	public static List<Module> importOrUpdate(@Nonnull Project project,
											  @Nullable Sdk unitySdk,
											  @Nullable ModifiableModuleModel originalModel,
											  @Nonnull ProgressIndicator progressIndicator,
											  @Nullable Collection<String> defines)
	{
		boolean fromProjectStructure = originalModel != null;

		progressIndicator.setIndeterminate(true);

		VirtualFile baseDir = project.getBaseDir();
		assert baseDir != null;

		UnityProjectImportContext context = UnityProjectImportContext.load(project, defines, baseDir, progressIndicator, unitySdk);

		ModifiableModuleModel newModulesModel = fromProjectStructure ? originalModel : AccessRule.read(() -> ModuleManager.getInstance(project).getModifiableModel());
		assert newModulesModel != null;

		List<Module> modules = new ArrayList<>();

		modules.add(Unity3dProjectImporter.createRootModule(project, newModulesModel, unitySdk, progressIndicator, context));

		VirtualFile assetsDir = baseDir.findChild(Unity3dProjectImporter.ASSETS_DIRECTORY);
		if(assetsDir == null)
		{
			progressIndicator.setTextValue(LocalizeValue.empty());

			if(!fromProjectStructure)
			{
				WriteAction.runAndWait(newModulesModel::commit);
			}

			return modules;
		}

		List<Runnable> writeCommits = new ArrayList<>();

		Map<String, UnityAssemblyContext> asmdefs = new TreeMap<>();
		// standard modules
		for(StandardModuleImporter importer : ourStandardModuleImporters)
		{
			asmdefs.put(importer.getName(), new UnityAssemblyContext(UnityAssemblyType.STANDARD, importer.getName(), null, null));
		}

		VirtualFileUtil.visitChildrenRecursively(assetsDir, new AsmDefFileVisitor(project, UnityAssemblyType.FROM_SOURCE, asmdefs));

		VirtualFile packagesDir = baseDir.findChild(Unity3dProjectImporter.PACKAGES_DIRECTORY);
		if(packagesDir != null && packagesDir.isDirectory())
		{
			for(VirtualFile packageDirectory : packagesDir.getChildren())
			{
				if(packageDirectory.isDirectory() && packageDirectory.findChild("package.json") != null)
				{
					VirtualFileUtil.visitChildrenRecursively(packageDirectory, new AsmDefFileVisitor(project, UnityAssemblyType.FROM_EMBEDDED_PACKAGE, asmdefs));
				}
			}
		}

		Set<String> unityModules = new LinkedHashSet<>();
		if(unitySdk != null)
		{
			Set<String> managedDirectories = new HashSet<>();

			Unity3dRootModuleExtension.collectManagedDirectories(unitySdk, Platform.current().os(), managedDirectories::add);

			for(String dir : managedDirectories)
			{
				Path path = Path.of(dir, "UnityEngine");
				if(Files.exists(path))
				{
					try
					{
						Files.walk(path).forEach(maybeDll -> {
							String fileName = maybeDll.getFileName().toString();

							if(DotNetModuleFileType.isDllFile(maybeDll.getFileName().toString()))
							{
								unityModules.add(FileUtil.getNameWithoutExtension(fileName));
							}
						});
					}
					catch(IOException e)
					{
						LOG.warn(e);
					}
				}
			}

			// remove default libraries
			unityModules.remove("UnityEngine");
			unityModules.remove("UnityEditor");
		}

		// set of registered file urls
		Set<String> registeredFiles = new HashSet<>();

		// analyze first of all assemby defs
		for(UnityAssemblyContext assemblyContext : asmdefs.values())
		{
			VirtualFile asmdefFile = assemblyContext.getAsmdefFile();
			if(asmdefFile == null)
			{
				continue;
			}

			progressIndicator.setTextValue(LocalizeValue.localizeTODO("Analyzing " + assemblyContext.getName()));

			//boolean isAllowEditorDir = assemblyContext.getAsmDefElement().getIncludePlatforms().contains(EDITOR_PLATFORM);

			VirtualFileUtil.visitChildrenRecursively(asmdefFile.getParent(), new VirtualFileVisitor()
			{
				@Override
				public boolean visitFile(@Nonnull VirtualFile file)
				{
					if(FileTypeManager.getInstance().isFileIgnored(file))
					{
						return false;
					}

					// editor dir stopper
					if(EDITOR_DIRECTORY.equalsIgnoreCase(file.getName()))
					{
						return false;
					}

					if(file.getFileType() == CSharpFileType.INSTANCE)
					{
						assemblyContext.addSourceFile(file);

						registeredFiles.add(file.getUrl());
					}
					else if(file.getFileType() == DotNetModuleFileType.INSTANCE)
					{
						assemblyContext.addAssembly(file);
					}

					return super.visitFile(file);
				}
			});
		}

		for(StandardModuleImporter importer : ourStandardModuleImporters)
		{
			UnityAssemblyContext assemblyContext = asmdefs.get(importer.getName());

			assert assemblyContext != null;

			importer.analyzeSourceFiles(project, assemblyContext, registeredFiles);
		}

		LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(context.getProject());

		LibraryTable.ModifiableModel librariesModModel = ReadAction.compute(libraryTable::getModifiableModel);

		initializePackageLibraries(context, asmdefs, writeCommits, librariesModModel);

		writeCommits.add(librariesModModel::commit);

		Map<UnityAssemblyContext, ModifiableModuleRootLayer> moduleLayers = new HashMap<>();

		// first asmdefs
		for(UnityAssemblyContext assemblyContext : asmdefs.values())
		{
			UnityAssemblyType type = assemblyContext.getType();
			if(type != UnityAssemblyType.FROM_SOURCE && type != UnityAssemblyType.FROM_EMBEDDED_PACKAGE)
			{
				continue;
			}

			VirtualFile asmdefFile = Objects.requireNonNull(assemblyContext.getAsmdefFile());

			Module assembyModule = newModulesModel.findModuleByName(Objects.requireNonNull(assemblyContext.getName()));
			if(assembyModule != null)
			{
				newModulesModel.disposeModule(assembyModule);
			}

			VirtualFile parent = asmdefFile.getParent();
			assembyModule = newModulesModel.newModule(assemblyContext.getName(), parent.getPath());
			modules.add(assembyModule);
			final Module finalAssembyModule = assembyModule;

			ModifiableRootModel rootModel = ReadAction.compute(() -> ModuleRootManager.getInstance(finalAssembyModule).getModifiableModel());

			rootModel.addContentEntry(parent).addFolder(parent, ProductionContentFolderTypeProvider.getInstance());

			ModifiableModuleRootLayer layer = (ModifiableModuleRootLayer) rootModel.getCurrentLayer();

			boolean isEditor = ReadAction.compute(() -> assemblyContext.getAsmDefElement().getIncludePlatforms().contains(EDITOR_PLATFORM));

			initializeModuleExtension(layer, unityModules, isEditor);

			analyzeAndAddDependencyTree(assemblyContext, asmdefs);

			moduleLayers.put(assemblyContext, layer);

			writeCommits.add(rootModel::commit);
		}

		for(StandardModuleImporter importer : ourStandardModuleImporters)
		{
			UnityAssemblyContext assemblyContext = asmdefs.get(importer.getName());

			assert assemblyContext != null;

			Module assembyModule = newModulesModel.findModuleByName(Objects.requireNonNull(assemblyContext.getName()));
			if(assembyModule != null)
			{
				newModulesModel.disposeModule(assembyModule);
			}

			assembyModule = newModulesModel.newModule(assemblyContext.getName(), null);
			modules.add(assembyModule);
			final Module finalAssembyModule = assembyModule;

			ModifiableRootModel rootModel = ReadAction.compute(() -> ModuleRootManager.getInstance(finalAssembyModule).getModifiableModel());

			for(VirtualFile file : assemblyContext.getSourceFiles())
			{
				rootModel.addSingleContentEntry(file);
			}

			ModifiableModuleRootLayer layer = (ModifiableModuleRootLayer) rootModel.getCurrentLayer();

			initializeModuleExtension(layer, unityModules, importer.isEditorModule());

			for(UnityAssemblyContext maybeDepContext : asmdefs.values())
			{
				// skip standard modules
				if(maybeDepContext.getType() == UnityAssemblyType.STANDARD)
				{
					continue;
				}

				processDependencies(assemblyContext, maybeDepContext, asmdefs, new HashSet<>());
			}

			moduleLayers.put(assemblyContext, layer);

			writeCommits.add(rootModel::commit);
		}

		for(UnityAssemblyContext assemblyContext : asmdefs.values())
		{
			UnityAssemblyType type = assemblyContext.getType();
			if(type == UnityAssemblyType.FROM_EXTERNAL_PACKAGE || type == UnityAssemblyType.STANDARD)
			{
				// it's library not module or non asm module
				continue;
			}

			analyzeAndAddDependencyTree(assemblyContext, asmdefs);
		}

		for(UnityAssemblyContext assemblyContext : asmdefs.values())
		{
			ModifiableModuleRootLayer rootLayer = moduleLayers.get(assemblyContext);

			for(UnityAssemblyContext dependency : assemblyContext.getDependencies())
			{
				addAsDependency(dependency, rootLayer);
			}

			for(VirtualFile libFile : assemblyContext.getAssemblies())
			{
				Unity3dProjectImporter.addAsLibrary(libFile, rootLayer);
			}
		}

		// todo


		progressIndicator.setIndeterminate(false);
		progressIndicator.setFraction(1);
		progressIndicator.setTextValue(LocalizeValue.empty());

		if(!fromProjectStructure)
		{
			writeCommits.add(newModulesModel::commit);
		}

		WriteAction.runAndWait(() -> {
			for(Runnable writeCommit : writeCommits)
			{
				writeCommit.run();
			}
		});

		return modules;
	}

	private static void analyzeAndAddDependencyTree(UnityAssemblyContext target, Map<String, UnityAssemblyContext> asmdefs)
	{
		AsmDefElement defElement = Objects.requireNonNull(target.getAsmDefElement());

		Set<String> processed = new HashSet<>();

		Set<String> references = ReadAction.compute(defElement::getReferences);
		for(String reference : references)
		{
			UnityAssemblyContext depContext = asmdefs.get(reference);
			if(depContext != null)
			{
				processDependencies(target, depContext, asmdefs, processed);
			}
		}

		// TODO [VISTALL] handle it? Set<String> optionalUnityReferences = defElement.getOptionalUnityReferences();
	}

	private static void processDependencies(UnityAssemblyContext target, UnityAssemblyContext dep, Map<String, UnityAssemblyContext> asmdefs, Set<String> processed)
	{
		if(!processed.add(dep.getName()))
		{
			return;
		}

		target.addDependency(dep);

		AsmDefElement def = dep.getAsmDefElement();
		if(def == null)
		{
			return;
		}

		Set<String> references = ReadAction.compute(def::getReferences);
		for(String reference : references)
		{
			UnityAssemblyContext depContext = asmdefs.get(reference);
			if(depContext != null)
			{
				processDependencies(target, depContext, asmdefs, processed);
			}
		}

		// TODO [VISTALL] handle it? Set<String> optionalUnityReferences = defElement.getOptionalUnityReferences();
	}

	private static void addAsDependency(UnityAssemblyContext asmContext, ModifiableModuleRootLayer layer)
	{
		if(asmContext.getType() == UnityAssemblyType.FROM_EMBEDDED_PACKAGE || asmContext.getType() == UnityAssemblyType.FROM_SOURCE)
		{
			ReadAction.run(() -> layer.addInvalidModuleEntry(asmContext.getName()));
		}
		else if(asmContext.getType() == UnityAssemblyType.FROM_EXTERNAL_PACKAGE)
		{
			Library library = Objects.requireNonNull(asmContext.getLibrary());

			layer.addLibraryEntry(library);
		}
		else
		{
			throw new UnsupportedOperationException("unsupported dependency: " + asmContext.getType());
		}
	}

	private static void initializeModuleExtension(ModifiableModuleRootLayer layer, Set<String> unityModules, boolean editor)
	{
		layer.getExtensionWithoutCheck(Unity3dChildMutableModuleExtension.class).setEnabled(true);

		CSharpSimpleMutableModuleExtension<?> extension = layer.getExtensionWithoutCheck("unity3d-csharp-child");
		assert extension != null;
		extension.setEnabled(true);

		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("mscorlib"));
		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("UnityEditor"));
		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("UnityEngine"));
		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("System"));
		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("System.Core"));
		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("System.Runtime.Serialization"));
		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("System.Xml"));
		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("System.Xml.Linq"));
		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("System.Net.Http"));

		layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("UnityEngine"));

		if(editor)
		{
			layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel("UnityEditor"));
		}

		for(String unityModule : unityModules)
		{
			layer.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel(unityModule));
		}
	}

	private static void initializePackageLibraries(UnityProjectImportContext context,
												   Map<String, UnityAssemblyContext> asmdefs,
												   List<Runnable> writeCommits,
												   LibraryTable.ModifiableModel librariesModModel)
	{
		Project project = context.getProject();

		for(Map.Entry<String, String> entry : context.getManifest().dependencies.entrySet())
		{
			String name = entry.getKey();

			String packageVersion = entry.getValue();
			if(packageVersion.startsWith("file"))
			{
				try
				{
					URL url = new URL(packageVersion);
					File targetDirectory = new File(url.getFile());

					initializePackageLibraryFileUrl(context, targetDirectory, packageVersion, writeCommits, asmdefs, librariesModModel);
				}
				catch(Exception e)
				{
					LOG.warn(e);
				}
			}
			// git url
			// we can't calculate without unity. try guest from Library dir
			else if(packageVersion.startsWith("git") || packageVersion.startsWith("https") || packageVersion.endsWith(".git"))
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

							initializePackageLibraryGit(context, libraryHome.toFile(), writeCommits, asmdefs, librariesModModel);
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
				String packageId = entry.getKey();

				initializePackageLibraryExternal(context, packageId, packageVersion, writeCommits, asmdefs, librariesModModel);
			}
		}
	}

	private static void initializePackageLibraryFileUrl(UnityProjectImportContext context,
														File packageDir,
														String url,
														List<Runnable> writeCommits,
														Map<String, UnityAssemblyContext> asmdefs,
														LibraryTable.ModifiableModel librariesModModel)
	{
		initializePackageLibrary(context, packageDir, packageDir + "@" + url.hashCode(), writeCommits, asmdefs, librariesModModel);
	}

	private static void initializePackageLibraryGit(UnityProjectImportContext context,
													File packageDir,
													List<Runnable> writeCommits,
													Map<String, UnityAssemblyContext> asmdefs,
													LibraryTable.ModifiableModel librariesModModel)
	{
		initializePackageLibrary(context, packageDir, packageDir.getName(), writeCommits, asmdefs, librariesModModel);
	}

	private static void initializePackageLibraryExternal(UnityProjectImportContext context,
														 String packageId,
														 String packageVersion,
														 List<Runnable> writeCommits,
														 Map<String, UnityAssemblyContext> asmdefs,
														 LibraryTable.ModifiableModel librariesModModel)
	{
		Unity3dPackageWatcher unity3dPackageWatcher = Unity3dPackageWatcher.getInstance();

		File packageDir = null;

		Sdk unityBundle = context.getUnityBundle();
		if(unityBundle != null)
		{
			File temp = new File(unity3dPackageWatcher.getBuiltInPackagesPath(unityBundle), packageId);
			if(temp.exists())
			{
				packageDir = temp;
			}
		}

		String packageWithVersion = packageId + "@" + packageVersion;

		if(packageDir == null)
		{

			for(String packageDirPath : unity3dPackageWatcher.getPackageDirPaths())
			{
				File temp = new File(packageDirPath, packageWithVersion);

				if(temp.exists())
				{
					packageDir = temp;
					break;
				}
			}
		}

		if(packageDir == null)
		{
			return;
		}

		initializePackageLibrary(context, packageDir, packageWithVersion, writeCommits, asmdefs, librariesModModel);
	}

	private static void initializePackageLibrary(UnityProjectImportContext context,
												 File packageDir,
												 String packageName,
												 List<Runnable> writeCommits,
												 Map<String, UnityAssemblyContext> asmdefs,
												 LibraryTable.ModifiableModel librariesModModel)
	{
		VirtualFile packageVDir = LocalFileSystem.getInstance().findFileByIoFile(packageDir);
		if(packageVDir == null)
		{
			return;
		}

		Map<String, UnityAssemblyContext> assemblies = new HashMap<>();

		VirtualFileUtil.visitChildrenRecursively(packageVDir, new AsmDefFileVisitor(context.getProject(), UnityAssemblyType.FROM_EXTERNAL_PACKAGE, assemblies));

		asmdefs.putAll(assemblies);

		for(UnityAssemblyContext unityAssemblyContext : assemblies.values())
		{
			String libraryName = "Unity: " + packageName + " [" + unityAssemblyContext.getName() + "]";

			VirtualFile asmDirectory = unityAssemblyContext.getAsmDirectory();

			// something strange happens
			if(asmDirectory == null)
			{
				continue;
			}

			Library oldLibrary = librariesModModel.getLibraryByName(libraryName);
			if(oldLibrary != null)
			{
				librariesModModel.removeLibrary(oldLibrary);
			}

			Library library = librariesModModel.createLibrary(libraryName, UnityPackageLibraryType.ID);
			Library.ModifiableModel modifiableModel = library.getModifiableModel();

			unityAssemblyContext.setLibrary(library);

			modifiableModel.addRoot(asmDirectory, BinariesOrderRootType.getInstance());
			modifiableModel.addRoot(asmDirectory, SourcesOrderRootType.getInstance());

			// we need add exclude roots to skip directories with another asmdefs in same package
			for(UnityAssemblyContext anotherAssembly : assemblies.values())
			{
				// ignored self
				if(unityAssemblyContext == anotherAssembly || anotherAssembly.getAsmDirectory() == null)
				{
					continue;
				}

				VirtualFile anotherDirectory = anotherAssembly.getAsmDirectory();

				if(VirtualFileUtil.isAncestor(asmDirectory, anotherDirectory, false))
				{
					modifiableModel.addExcludedRoot(anotherDirectory.getUrl());
				}
			}

			writeCommits.add(modifiableModel::commit);
		}
	}
}
