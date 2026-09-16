package consulo.unity3d.importing;

import consulo.application.Application;
import consulo.it.AllowLogError;
import consulo.it.HeadlessApplicationExtension;
import consulo.project.ProjectManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves a real headless application boots on the importing module's test classpath.
 */
@ExtendWith(HeadlessApplicationExtension.class)
@AllowLogError({
    "consulo.virtualFileSystem.internal.BaseVirtualFileManager",
    "consulo.component.impl.internal.messagebus.MessageBusImpl",
    "consulo.project.impl.internal.StartupManagerImpl",
    "consulo.application.impl.internal.BaseApplication",
    "consulo.ui.ex.impl.internal.action.ActionManagerImpl"
})
public class HeadlessBootTest {
    @Test
    public void applicationBoots(Application application, ProjectManager projectManager) {
        assertThat(application).isNotNull();
        assertThat(projectManager).isNotNull();
    }
}
