/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.unity3d.base.asmdef;

import com.dslplatform.json.DslJson;
import consulo.logging.Logger;
import consulo.util.io.CharsetToolkit;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Reads {@code .asmdef} files into {@link AsmDefDescriptor}.
 *
 * @author VISTALL
 */
public class AsmDefReader {
    private static final Logger LOG = Logger.getInstance(AsmDefReader.class);

    private static final DslJson<Object> ourJson =
        new DslJson<>(new DslJson.Settings<>().includeServiceLoader(AsmDefReader.class.getClassLoader()));

    @Nullable
    public static AsmDefDescriptor read(@Nonnull VirtualFile file) {
        try (InputStream stream = file.getInputStream()) {
            return read(stream);
        }
        catch (IOException e) {
            LOG.warn("Failed to read asmdef " + file.getPath(), e);
            return null;
        }
    }

    @Nullable
    public static AsmDefDescriptor read(@Nonnull Path path) {
        try (InputStream stream = Files.newInputStream(path)) {
            return read(stream);
        }
        catch (IOException e) {
            LOG.warn("Failed to read asmdef " + path, e);
            return null;
        }
    }

    @Nullable
    public static AsmDefDescriptor read(byte[] content) {
        try (InputStream stream = new ByteArrayInputStream(content)) {
            return read(stream);
        }
        catch (IOException e) {
            LOG.warn("Malformed asmdef", e);
            return null;
        }
    }

    @Nullable
    private static AsmDefDescriptor read(@Nonnull InputStream rawStream) throws IOException {
        byte[] json = toUtf8(new BufferedInputStream(rawStream).readAllBytes());
        if (json.length == 0) {
            return null;
        }

        try {
            return ourJson.deserialize(AsmDefDescriptor.class, json, json.length);
        }
        catch (IOException e) {
            LOG.warn("Malformed asmdef", e);
            return null;
        }
    }

    /**
     * Unity writes some asmdef files with a byte order mark, and the json reader only accepts plain utf-8.
     * A utf-16/utf-32 marker means the payload needs transcoding, not just trimming.
     */
    private static byte[] toUtf8(byte[] content) {
        Charset charset = CharsetToolkit.guessFromBOM(content);
        if (charset == null) {
            return content;
        }

        int bomLength = CharsetToolkit.getBOMLength(content, charset);
        if (bomLength == 0) {
            return content;
        }

        if (StandardCharsets.UTF_8.equals(charset)) {
            return Arrays.copyOfRange(content, bomLength, content.length);
        }

        return new String(content, bomLength, content.length - bomLength, charset).getBytes(StandardCharsets.UTF_8);
    }

    private AsmDefReader() {
    }
}
