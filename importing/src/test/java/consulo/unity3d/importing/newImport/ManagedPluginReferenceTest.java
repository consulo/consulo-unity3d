package consulo.unity3d.importing.newImport;

import consulo.application.Application;
import consulo.it.AllowLogError;
import consulo.it.HeadlessApplicationExtension;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.unity3d.base.asmdef.AsmDefDescriptor;
import consulo.unity3d.base.asmdef.AsmDefReader;
import consulo.unity3d.importing.UnityImportTestSupport;
import consulo.virtualFileSystem.VirtualFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Which managed plugins an assembly can see. Unity auto references plugins into every assembly unless the plugin
 * opts out with isExplicitlyReferenced, or the assembly opts out with overrideReferences - in which case only the
 * dlls named in precompiledReferences are visible.
 */
@ExtendWith(HeadlessApplicationExtension.class)
@AllowLogError({
    "consulo.virtualFileSystem.internal.BaseVirtualFileManager",
    "consulo.component.impl.internal.inject.BaseComponentAdapter",
    "consulo.component.impl.internal.messagebus.MessageBusImpl",
    "consulo.project.impl.internal.StartupManagerImpl",
    "consulo.application.impl.internal.BaseApplication",
    "consulo.ui.ex.impl.internal.action.ActionManagerImpl"
})
public class ManagedPluginReferenceTest {
    @Test
    public void autoReferencedPluginsReachEveryAssemblyUnlessOverridden(Application application, ProjectManager projectManager)
        throws Exception {
        Path directory = Files.createTempDirectory("consulo-unity3d-plugins");
        Path plugins = directory.resolve("Assets/Plugins");
        Files.createDirectories(plugins);

        writeDll(plugins, "Vendor.Auto.dll", true);
        writeDll(plugins, "Vendor.Explicit.dll", false);

        Project project = UnityImportTestSupport.openProject(application, projectManager, directory);
        try {
            VirtualFile assets = UnityImportTestSupport.findFile(directory.resolve("Assets"));

            Map<String, VirtualFile> index = UnityProjectImporterWithAsmDef.collectManagedPlugins(assets);
            assertThat(index.keySet()).containsExactlyInAnyOrder("Vendor.Auto.dll", "Vendor.Explicit.dll");

            // a predefined assembly - no asmdef at all, so everything auto referenced is visible
            assertThat(names(UnityProjectImporterWithAsmDef.resolvePluginReferences(standard(), index)))
                .as("auto referenced plugins reach assemblies with no asmdef")
                .containsExactly("Vendor.Auto.dll");

            // asmdef leaving overrideReferences at its default - same as above
            assertThat(names(UnityProjectImporterWithAsmDef.resolvePluginReferences(asmdef("{\"name\":\"Core\"}"), index)))
                .as("overrideReferences defaults to false, so auto referenced plugins still apply")
                .containsExactly("Vendor.Auto.dll");

            // overrideReferences true - only what precompiledReferences names, opt-out flag irrelevant
            UnityAssemblyContext strict = asmdef(
                "{\"name\":\"Strict\",\"overrideReferences\":true,\"precompiledReferences\":[\"Vendor.Explicit.dll\"]}");
            assertThat(names(UnityProjectImporterWithAsmDef.resolvePluginReferences(strict, index)))
                .as("overrideReferences means only the named dlls, including ones that opted out of auto reference")
                .containsExactly("Vendor.Explicit.dll");

            // overrideReferences true with nothing named - sees no plugins at all
            UnityAssemblyContext isolated = asmdef("{\"name\":\"Isolated\",\"overrideReferences\":true}");
            assertThat(UnityProjectImporterWithAsmDef.resolvePluginReferences(isolated, index)).isEmpty();
        }
        finally {
            UnityImportTestSupport.closeProject(project);
        }
    }

    private static void writeDll(Path dir, String name, boolean autoReferenced) throws Exception {
        Files.write(dir.resolve(name), new byte[]{'M', 'Z'});
        Files.writeString(dir.resolve(name + ".meta"),
            "fileFormatVersion: 2\nguid: 0123456789abcdef0123456789abcdef\nPluginImporter:\n  isExplicitlyReferenced: "
                + (autoReferenced ? 0 : 1) + "\n");
    }

    private static UnityAssemblyContext standard() {
        return new UnityAssemblyContext(UnityAssemblyType.STANDARD, "Assembly-CSharp", null, null);
    }

    private static UnityAssemblyContext asmdef(String json) {
        AsmDefDescriptor def = AsmDefReader.read(json.getBytes(StandardCharsets.UTF_8));
        assertThat(def).isNotNull();
        return new UnityAssemblyContext(UnityAssemblyType.FROM_SOURCE, def.name, null, def);
    }

    private static List<String> names(List<VirtualFile> files) {
        return files.stream().map(VirtualFile::getName).toList();
    }
}
