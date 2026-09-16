package consulo.unity3d.importing;

import consulo.unity3d.base.asmdef.AsmDefDescriptor;
import consulo.unity3d.base.asmdef.AsmDefReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test - proves the dsl-json converter for asmdef is generated and discoverable.
 */
public class AsmDefReaderTest {
    @Test
    public void readsNameAndReferences() {
        String json = """
            {
                "name": "Game",
                "references": ["Core"],
                "includePlatforms": ["Editor"],
                "allowUnsafeCode": false
            }
            """;

        AsmDefDescriptor def = AsmDefReader.read(json.getBytes(StandardCharsets.UTF_8));

        assertThat(def).isNotNull();
        assertThat(def.name).isEqualTo("Game");
        assertThat(def.getReferences()).containsExactly("Core");
        assertThat(def.getIncludePlatforms()).containsExactly("Editor");
        assertThat(def.allowUnsafeCode).isFalse();
    }

    @Test
    public void toleratesUnknownAndMissingFields() {
        AsmDefDescriptor def = AsmDefReader.read("{\"name\":\"Core\",\"versionDefines\":[],\"rootNamespace\":\"\"}".getBytes(StandardCharsets.UTF_8));

        assertThat(def).isNotNull();
        assertThat(def.name).isEqualTo("Core");
        assertThat(def.getReferences()).isEmpty();
        assertThat(def.getIncludePlatforms()).isEmpty();
    }
}
