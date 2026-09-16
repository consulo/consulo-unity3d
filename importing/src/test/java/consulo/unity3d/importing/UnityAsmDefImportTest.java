package consulo.unity3d.importing;

import consulo.application.Application;
import consulo.application.progress.EmptyProgressIndicator;
import consulo.it.AllowLogError;
import consulo.it.AllowWriteLockUnderUIThread;
import consulo.it.HeadlessApplicationExtension;
import consulo.module.Module;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.unity3d.importing.newImport.UnityProjectImporterWithAsmDef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static consulo.unity3d.importing.UnityImportTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real asmdef importer over a Unity tree built on disk.
 */
@ExtendWith(HeadlessApplicationExtension.class)
@AllowWriteLockUnderUIThread
@AllowLogError({
    "consulo.virtualFileSystem.internal.BaseVirtualFileManager",
    "consulo.component.impl.internal.messagebus.MessageBusImpl",
    "consulo.project.impl.internal.StartupManagerImpl",
    "consulo.application.impl.internal.BaseApplication",
    "consulo.ui.ex.impl.internal.action.ActionManagerImpl"
})
public class UnityAsmDefImportTest {
    @Test
    public void importsAssemblyPerAsmDef(Application application, ProjectManager projectManager) throws Exception {
        Path directory = Files.createTempDirectory("consulo-unity3d-asmdef");
        Files.createDirectories(directory.resolve("ProjectSettings"));
        Files.writeString(directory.resolve("ProjectSettings/ProjectVersion.txt"), "m_EditorVersion: 6000.5.9f1");

        writeAsset(directory, "Ball.cs", "public class Ball {}");
        writeAsset(directory, "Scripts/Game.asmdef", asmdef("Game", "Core"));
        writeAsset(directory, "Scripts/GameScript.cs", "public class GameScript {}");
        writeAsset(directory, "Scripts/Core/Core.asmdef", asmdef("Core"));
        writeAsset(directory, "Scripts/Core/CoreScript.cs", "public class CoreScript {}");

        Project project = openProject(application, projectManager, directory);
        try {
            findFile(directory.resolve("Assets"));

            List<Module> modules = UnityProjectImporterWithAsmDef.importOrUpdate(
                project, null, null, new EmptyProgressIndicator(), List.of());

            assertThat(modules).as("import must produce modules").isNotEmpty();

            List<String> names = modules.stream().map(Module::getName).toList();
            System.out.println("IMPORTED MODULES -> " + names);
            System.out.println("DISPOSED         -> " + modules.stream().map(m -> m.getName() + "=" + m.isDisposed()).toList());
            System.out.println("IN MODEL         -> "
                + java.util.Arrays.stream(consulo.module.ModuleManager.getInstance(project).getModules()).map(Module::getName).toList());

            assertThat(names).contains("Game", "Core");
        }
        finally {
            closeProject(project);
        }
    }
}
