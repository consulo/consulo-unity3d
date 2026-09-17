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
 * An assembly the importer refuses to build a module for must not take part in the later dependency passes.
 * It has no module root layer, and feeding that null to addAsDependency used to throw.
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
public class SkippedAssemblyTest {
    @Test
    public void skippedAssemblyWithReferencesMustNotBreakImport(Application application, ProjectManager projectManager)
        throws Exception {
        Path parent = Files.createTempDirectory("consulo-unity3d-skipped");
        Path directory = parent.resolve("MyGame");
        Files.createDirectories(directory);

        // named exactly like the root module, and carrying references - so it is skipped, yet the dependency
        // passes still see it
        writeAsset(directory, "Scripts/Root.asmdef", asmdef("MyGame (root)", "Core"));
        writeAsset(directory, "Scripts/GameScript.cs", "public class GameScript {}");
        writeAsset(directory, "Scripts/Core/Core.asmdef", asmdef("Core"));
        writeAsset(directory, "Scripts/Core/CoreScript.cs", "public class CoreScript {}");

        Project project = openProject(application, projectManager, directory);
        try {
            findFile(directory.resolve("Assets"));

            List<Module> modules = UnityProjectImporterWithAsmDef.importOrUpdate(
                project, null, null, new EmptyProgressIndicator(), List.of());

            System.out.println("SKIPPED-CASE MODULES -> " + modules.stream().map(Module::getName).toList());

            assertThat(modules).isNotEmpty();
            assertThat(modules.stream().map(Module::getName)).contains("MyGame (root)", "Core");
        }
        finally {
            closeProject(project);
        }
    }
}
