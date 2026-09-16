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
 * An asmdef named after a predefined Unity assembly must not take over the standard one.
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
public class ReservedAssemblyNameTest {
    @Test
    public void asmDefNamedAssemblyCSharpMustNotHijackTheStandardOne(Application application, ProjectManager projectManager)
        throws Exception {
        Path directory = Files.createTempDirectory("consulo-unity3d-reserved");

        writeAsset(directory, "Ball.cs", "public class Ball {}");
        writeAsset(directory, "Scripts/Assembly-CSharp.asmdef", asmdef("Assembly-CSharp"));
        writeAsset(directory, "Scripts/GameScript.cs", "public class GameScript {}");

        Project project = openProject(application, projectManager, directory);
        try {
            findFile(directory.resolve("Assets"));

            List<Module> modules = UnityProjectImporterWithAsmDef.importOrUpdate(
                project, null, null, new EmptyProgressIndicator(), List.of());

            System.out.println("RESERVED MODULES -> " + modules.stream().map(Module::getName).toList());

            assertThat(modules.stream().map(Module::getName).filter("Assembly-CSharp"::equals))
                .as("exactly one Assembly-CSharp module may exist")
                .hasSize(1);

            Module standard = ModuleManager.getInstance(project).findModuleByName("Assembly-CSharp");
            assertThat(standard).isNotNull();

            // the standard Assembly-CSharp is created with a null dir - a hijacked one would be rooted at the asmdef
            String[] roots = Arrays.stream(ModuleRootManager.getInstance(standard).getContentRoots())
                .map(f -> f.getPath()).toArray(String[]::new);
            System.out.println("Assembly-CSharp ROOTS -> " + Arrays.toString(roots));

            assertThat(roots)
                .as("the standard Assembly-CSharp must not be rooted at the asmdef folder")
                .doesNotContain(directory.resolve("Assets/Scripts").toString());
        }
        finally {
            closeProject(project);
        }
    }
}
