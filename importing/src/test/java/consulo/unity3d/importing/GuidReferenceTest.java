package consulo.unity3d.importing;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.progress.EmptyProgressIndicator;
import consulo.it.AllowLogError;
import consulo.it.AllowWriteLockUnderUIThread;
import consulo.it.HeadlessApplicationExtension;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.unity3d.importing.newImport.UnityProjectImporterWithAsmDef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static consulo.unity3d.importing.UnityImportTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unity writes asmdef references as "GUID:&lt;meta guid&gt;" since 2019.1, alongside the older plain names.
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
public class GuidReferenceTest {
    @Test
    public void referenceByGuidResolvesLikeAReferenceByName(Application application, ProjectManager projectManager)
        throws Exception {
        Path directory = Files.createTempDirectory("consulo-unity3d-guid");

        // TestUnity3dMetaManager hands out "guid-<file name>", so Core.asmdef is "guid-Core"
        writeAsset(directory, "Scripts/Game.asmdef", asmdef("Game", "GUID:guid-Core"));
        writeAsset(directory, "Scripts/GameScript.cs", "public class GameScript {}");
        writeAsset(directory, "Scripts/Core/Core.asmdef", asmdef("Core"));
        writeAsset(directory, "Scripts/Core/CoreScript.cs", "public class CoreScript {}");

        Project project = openProject(application, projectManager, directory);
        try {
            findFile(directory.resolve("Assets"));

            UnityProjectImporterWithAsmDef.importOrUpdate(project, null, null, new EmptyProgressIndicator(), List.of());

            Module game = ModuleManager.getInstance(project).findModuleByName("Game");
            assertThat(game).isNotNull();

            List<String> dependencies = ReadAction.compute(() -> {
                List<String> names = new ArrayList<>();
                for (OrderEntry entry : ModuleRootManager.getInstance(game).getOrderEntries()) {
                    names.add(entry.getPresentableName());
                }
                return names;
            });
            System.out.println("Game order entries -> " + dependencies);

            assertThat(dependencies)
                .as("a GUID: reference must resolve to the same module a plain name would")
                .contains("Core");
        }
        finally {
            closeProject(project);
        }
    }
}
