package consulo.unity3d.importing;

import consulo.application.Application;
import consulo.application.progress.EmptyProgressIndicator;
import consulo.it.AllowLogError;
import consulo.it.AllowWriteLockUnderUIThread;
import consulo.it.HeadlessApplicationExtension;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.unity3d.base.module.Unity3dRootModuleExtension;
import consulo.unity3d.importing.Unity3dProjectImporter;
import consulo.unity3d.importing.newImport.UnityProjectImporterWithAsmDef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static consulo.unity3d.importing.UnityImportTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * An asmdef whose name matches the project directory. The root module is named after the project and carries
 * the unity root extension and the sdk, so it must survive the import.
 */
@ExtendWith(HeadlessApplicationExtension.class)
@AllowWriteLockUnderUIThread
@AllowLogError({
    "consulo.virtualFileSystem.internal.BaseVirtualFileManager",
    "consulo.component.impl.internal.inject.BaseComponentAdapter",
    "consulo.component.impl.internal.messagebus.MessageBusImpl",
    "consulo.project.impl.internal.StartupManagerImpl",
    "consulo.application.impl.internal.BaseApplication",
    "consulo.ui.ex.impl.internal.action.ActionManagerImpl"
})
public class RootModuleHijackTest {
    @Test
    public void asmDefNamedLikeProjectMustNotReplaceRootModule(Application application, ProjectManager projectManager) throws Exception {
        Path parent = Files.createTempDirectory("consulo-unity3d-root");
        Path directory = parent.resolve("MyGame");
        Files.createDirectories(directory);

        writeAsset(directory, "Scripts/MyGame.asmdef", asmdef("MyGame"));
        writeAsset(directory, "Scripts/GameScript.cs", "public class GameScript {}");

        Project project = openProject(application, projectManager, directory);
        try {
            assertThat(project.getName()).isEqualTo("MyGame");
            findFile(directory.resolve("Assets"));

            List<Module> modules = UnityProjectImporterWithAsmDef.importOrUpdate(
                project, null, null, new EmptyProgressIndicator(), List.of());

            System.out.println("MODULES  -> " + modules.stream().map(Module::getName).toList());
            System.out.println("IN MODEL -> "
                + Arrays.stream(ModuleManager.getInstance(project).getModules()).map(Module::getName).toList());

            Module root = ModuleManager.getInstance(project)
                .findModuleByName(Unity3dProjectImporter.getRootModuleName(project));
            assertThat(root).as("the root module must still exist").isNotNull();

            Unity3dRootModuleExtension rootExtension =
                ModuleRootManager.getInstance(root).getExtension(Unity3dRootModuleExtension.class);
            System.out.println("ROOT EXT -> " + rootExtension);

            assertThat(rootExtension)
                .as("the project root module must keep the unity ROOT extension, not be rebuilt as a child assembly")
                .isNotNull();

            assertThat(ModuleRootManager.getInstance(root).getContentRoots()[0].getPath())
                .as("the root module must stay rooted at the project dir, not at the asmdef folder")
                .isEqualTo(directory.toString());
        }
        finally {
            closeProject(project);
        }
    }
}
