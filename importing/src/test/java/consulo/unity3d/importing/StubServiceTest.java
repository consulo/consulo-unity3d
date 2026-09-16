package consulo.unity3d.importing;

import consulo.application.Application;
import consulo.it.AllowLogError;
import consulo.it.HeadlessApplicationExtension;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.unity3d.base.scene.Unity3dMetaManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;

import static consulo.unity3d.importing.UnityImportTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves a test supplied @ServiceImpl is discovered, so the importer can run without the plugin module.
 */
@ExtendWith(HeadlessApplicationExtension.class)
@AllowLogError({
    "consulo.virtualFileSystem.internal.BaseVirtualFileManager",
    "consulo.component.impl.internal.messagebus.MessageBusImpl",
    "consulo.project.impl.internal.StartupManagerImpl",
    "consulo.application.impl.internal.BaseApplication",
    "consulo.ui.ex.impl.internal.action.ActionManagerImpl"
})
public class StubServiceTest {
    @Test
    public void testStubReplacesThePluginService(Application application, ProjectManager projectManager) throws Exception {
        Path directory = Files.createTempDirectory("consulo-unity3d-stub");
        Path asmdef = writeAsset(directory, "Scripts/Game.asmdef", asmdef("Game"));

        Project project = openProject(application, projectManager, directory);
        try {
            Unity3dMetaManager manager = Unity3dMetaManager.getInstance(project);

            assertThat(manager).isInstanceOf(TestUnity3dMetaManager.class);
            assertThat(manager.getGUID(findFile(asmdef))).isEqualTo("guid-Game");
        }
        finally {
            closeProject(project);
        }
    }
}
