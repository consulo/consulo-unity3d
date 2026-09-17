package consulo.unity3d.importing;

import consulo.unity3d.base.asmdef.AsmDefDescriptor;
import consulo.unity3d.base.asmdef.AsmDefReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the dsl-json converter for asmdef, and the byte order marks Unity writes into some of them.
 */
public class AsmDefReaderTest {
    private static final String JSON = "{\"name\": \"Game\", \"references\": [\"Core\"]}";

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

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
        AsmDefDescriptor def = AsmDefReader.read(
            "{\"name\":\"Core\",\"versionDefines\":[],\"rootNamespace\":\"\"}".getBytes(StandardCharsets.UTF_8));

        assertThat(def).isNotNull();
        assertThat(def.name).isEqualTo("Core");
        assertThat(def.getReferences()).isEmpty();
        assertThat(def.getIncludePlatforms()).isEmpty();
    }

    @Test
    public void readsFilesWrittenWithAByteOrderMark() {
        for (String charsetName : new String[]{"UTF-8", "UTF-16LE", "UTF-16BE", "UTF-32LE", "UTF-32BE"}) {
            AsmDefDescriptor def = AsmDefReader.read(withBom(charsetName));

            assertThat(def).as("asmdef written as %s with a bom", charsetName).isNotNull();
            assertThat(def.name).as("name, %s", charsetName).isEqualTo("Game");
            assertThat(def.getReferences()).as("references, %s", charsetName).containsExactly("Core");
        }
    }

    @Test
    public void readsBomMarkedFileFromDisk() throws Exception {
        Path file = Files.createTempFile("asmdef-bom", ".asmdef");
        try {
            Files.write(file, withBom("UTF-8"));

            AsmDefDescriptor def = AsmDefReader.read(file);

            assertThat(def).isNotNull();
            assertThat(def.name).isEqualTo("Game");
        }
        finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void emptyAndMalformedFilesAreRejectedWithoutThrowing() {
        assertThat(AsmDefReader.read(new byte[0])).isNull();
        assertThat(AsmDefReader.read(UTF8_BOM)).as("a file holding nothing but a bom").isNull();
        assertThat(AsmDefReader.read("not json at all".getBytes(StandardCharsets.UTF_8))).isNull();
    }

    private static byte[] withBom(String charsetName) {
        Charset charset = Charset.forName(charsetName);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // the jdk writes the bom itself for UTF-16/UTF-32 big endian variants, so only add it where it does not
        switch (charsetName) {
            case "UTF-8" -> out.writeBytes(UTF8_BOM);
            case "UTF-16LE" -> out.writeBytes(new byte[]{(byte) 0xFF, (byte) 0xFE});
            case "UTF-16BE" -> out.writeBytes(new byte[]{(byte) 0xFE, (byte) 0xFF});
            case "UTF-32LE" -> out.writeBytes(new byte[]{(byte) 0xFF, (byte) 0xFE, 0, 0});
            case "UTF-32BE" -> out.writeBytes(new byte[]{0, 0, (byte) 0xFE, (byte) 0xFF});
            default -> throw new IllegalArgumentException(charsetName);
        }
        out.writeBytes(JSON.getBytes(charset));
        return out.toByteArray();
    }
}
