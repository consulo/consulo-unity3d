/*
 * Copyright 2013-2025 consulo.io
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

package consulo.unity3d.asset.binary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author VISTALL
 * @since 2025-01-24
 */
public class AssetHeaderReader {
    public static String read(ByteBuffer in) throws IOException {
        AssetHeader header = new AssetHeader();
        header.read(in);
        in.order(header.order());
        String version = null;

        if (header.version() > 6) {
            version = readStringNull(255, in, StandardCharsets.US_ASCII);
        }
        return version;
    }

    private static String readStringNull(int limit, ByteBuffer in, Charset charset) throws IOException {
        // read bytes until the first null byte
        byte[] raw = new byte[limit];
        int length = 0;
        while (length < raw.length && (raw[length] = in.get()) != 0) {
            length++;
        }

        return new String(raw, 0, length, charset);
    }
}
