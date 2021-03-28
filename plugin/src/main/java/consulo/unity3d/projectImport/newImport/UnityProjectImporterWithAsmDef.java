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

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import consulo.application.AccessRule;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import consulo.dotnet.roots.orderEntry.DotNetLibraryOrderEntryImpl;
import consulo.logging.Logger;
import consulo.roots.ModifiableModuleRootLayer;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.roots.impl.ProductionContentFolderTypeProvider;
import consulo.roots.types.BinariesOrderRootType;
import consulo.roots.types.SourcesOrderRootType;
import consulo.unity3d.module.Unity3dChildMutableModuleExtension;
import consulo.unity3d.packages.Unity3dPackageWatcher;
import consulo.unity3d.packages.library.UnityPackageLibraryType;
import consulo.unity3d.projectImport.Unity3dProjectImporter;
import consulo.unity3d.projectImport.UnityProjectImportContext;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharp;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharpEditor;
import consulo.unity3d.projectImport.newImport.standardImporter.AssemblyCSharpFirstPass;
import consulo.unity3d.projectImport.newImport.standardImporter.StandardModuleImporter;

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
			progressIndicator.setText(null);

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

		VfsUtil.visitChildrenRecursively(assetsDir, new AssemblyFileVisitor(project, UnityAssemblyType.FROM_SOURCE, asmdefs));

		VirtualFile packagesDir = baseDir.findChild(Unity3dProjectImporter.PACKAGES_DIRECTORY);
		if(packagesDir != null && packagesDir.isDirectory())
		{
			for(VirtualFile packageDirectory : packagesDir.getChildren())
			{
				if(packageDirectory.isDirectory() && packageDirectory.findChild("package.json") != null)
				{
					VfsUtil.visitChildrenRecursively(packageDirectory, new AssemblyFileVisitor(project, UnityAssemblyType.FROM_EMBEDDED_PACKAGE, asmdefs));
				}
			}
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

			progressIndicator.setText("Analyzing " + assemblyContext.getName());

			//boolean isAllowEditorDir = assemblyContext.getAsmDefElement().getIncludePlatforms().contains(EDITOR_PLATFORM);

			VfsUtil.visitChildrenRecursively(asmdefFile.getParent(), new VirtualFileVisitor()
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

		initializePackageLibraries(context, asmdefs, writeCommits);

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

			assembyModule = newModulesModel.newModule(assemblyContext.getName(), asmdefFile.getParent().getPath());
			modules.add(assembyModule);
			final Module finalAssembyModule = assembyModule;

			ModifiableRootModel rootModel = ReadAction.compute(() -> ModuleRootManager.getInstance(finalAssembyModule).getModifiableModel());

			rootModel.addContentEntry(asmdefFile.getParent()).addFolder(asmdefFile.getParent(), ProductionContentFolderTypeProvider.getInstance());

			initializeModuleExtension((ModifiableModuleRootLayer) rootModel.getCurrentLayer());

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

			initializeModuleExtension((ModifiableModuleRootLayer) rootModel.getCurrentLayer());
			
			writeCommits.add(rootModel::commit);
		}


		// todo


		progressIndicator.setIndeterminate(false);
		progressIndicator.setFraction(1);
		progressIndicator.setText(null);

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

	private static void initializeModuleExtension(ModifiableModuleRootLayer layer)
	{
		layer.getExtensionWithoutCheck(Unity3dChildMutableModuleExtension.class).setEnabled(true);

		CSharpSimpleMutableModuleExtension<?> extension = layer.getExtensionWithoutCheck("unity3d-csharp-child");
		assert extension != null;
		extension.setEnabled(true);

		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "mscorlib"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "UnityEditor"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "UnityEngine"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "System"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "System.Core"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "System.Runtime.Serialization"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "System.Xml"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "System.Xml.Linq"));
		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "System.Net.Http"));

		layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "UnityEngine"));
	}

	private static void initializePackageLibraries(UnityProjectImportContext context, Map<String, UnityAssemblyContext> asmdefs, List<Runnable> writeCommits)
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

					initializePackageLibraryFileUrl(context, targetDirectory, packageVersion, writeCommits);
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

							initializePackageLibraryGit(context, libraryHome.toFile(), writeCommits);
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

				initializePackageLibraryExternal(context, packageId, packageVersion, writeCommits);
			}
		}
	}

	private static void initializePackageLibraryFileUrl(UnityProjectImportContext context, File packageDir, String url, List<Runnable> writeCommits)
	{
		initializePackageLibrary(context, packageDir, packageDir + "@" + url.hashCode(), writeCommits);
	}

	private static void initializePackageLibraryGit(UnityProjectImportContext context, File packageDir, List<Runnable> writeCommits)
	{
		initializePackageLibrary(context, packageDir, packageDir.getName(), writeCommits);
	}

	private static void initializePackageLibraryExternal(UnityProjectImportContext context, String packageId, String packageVersion, List<Runnable> writeCommits)
	{
		List<String> packageDirPaths = Unity3dPackageWatcher.getInstance().getPackageDirPaths();

		String packageWithVersion = packageId + "@" + packageVersion;

		File packageDir = null;
		for(String packageDirPath : packageDirPaths)
		{
			File temp = new File(packageDirPath, packageWithVersion);

			if(temp.exists())
			{
				packageDir = temp;
				break;
			}
		}

		if(packageDir == null)
		{
			return;
		}

		initializePackageLibrary(context, packageDir, packageWithVersion, writeCommits);
	}

	private static void initializePackageLibrary(UnityProjectImportContext context, File packageDir, String packageName, List<Runnable> writeCommits)
	{
		VirtualFile packageVDir = LocalFileSystem.getInstance().findFileByIoFile(packageDir);
		if(packageVDir == null)
		{
			return;
		}

		Map<String, UnityAssemblyContext> assemblies = new HashMap<>();

		VfsUtil.visitChildrenRecursively(packageVDir, new AssemblyFileVisitor(context.getProject(), UnityAssemblyType.FROM_EXTERNAL_PACKAGE, assemblies));

		LibraryTableBase libraryTable = (LibraryTableBase) ProjectLibraryTable.getInstance(context.getProject());

		LibraryTableBase.ModifiableModelEx librariesModModel = (LibraryTableBase.ModifiableModelEx) ReadAction.compute(libraryTable::getModifiableModel);

		for(UnityAssemblyContext unityAssemblyContext : assemblies.values())
		{
			String libraryName = "Unity: " + packageName + " [" + unityAssemblyContext.getName() + "]";

			unityAssemblyContext.setLibraryName(libraryName);

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

			LibraryEx library = (LibraryEx) librariesModModel.createLibrary(libraryName, UnityPackageLibraryType.ID);
			LibraryEx.ModifiableModelEx modifiableModel = library.getModifiableModel();

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

				if(VfsUtil.isAncestor(asmDirectory, anotherDirectory, false))
				{
					modifiableModel.addExcludedRoot(anotherDirectory.getUrl());
				}
			}

			WriteAction.runAndWait(modifiableModel::commit);
		}

		WriteAction.runAndWait(librariesModModel::commit);
	}
}
