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
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        try {
            return read(file.contentsToByteArray());
        }
        catch (IOException e) {
            LOG.warn("Failed to read asmdef " + file.getPath(), e);
            return null;
        }
    }

    @Nullable
    public static AsmDefDescriptor read(@Nonnull Path path) {
        try {
            return read(Files.readAllBytes(path));
        }
        catch (IOException e) {
            LOG.warn("Failed to read asmdef " + path, e);
            return null;
        }
    }

    @Nullable
    public static AsmDefDescriptor read(byte[] content) {
        if (content.length == 0) {
            return null;
        }

        try {
            return ourJson.deserialize(AsmDefDescriptor.class, content, content.length);
        }
        catch (IOException e) {
            LOG.warn("Malformed asmdef", e);
            return null;
        }
    }

    private AsmDefReader() {
    }
}
