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

package consulo.unity3d.base;

import consulo.util.lang.StringUtil;
import consulo.util.lang.Version;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * Well-known directory names inside a Unity project, plus version parsing.
 * <p>
 * Lives in the base module so that module extensions and package handling can reach them without
 * depending on the import implementation.
 *
 * @author VISTALL
 */
public class Unity3dPaths {
    public static final String ASSETS_DIRECTORY = "Assets";
    public static final String PACKAGES_DIRECTORY = "Packages";

    /**
     * Directories compiled into {@code Assembly-CSharp-firstpass} by Unity, before every other script.
     */
    public static final String[] FIRST_PASS_PATHS = new String[]{
        "Assets/Standard Assets",
        "Assets/Pro Standard Assets",
        "Assets/Plugins"
    };

    public static Version parseVersion(@Nullable String versionString) {
        if (versionString == null) {
            return new Version(0, 0, 0);
        }

        List<String> list = StringUtil.split(versionString, ".");
        if (list.size() >= 3) {
            try {
                return new Version(Integer.parseInt(list.get(0)), Integer.parseInt(list.get(1)), Integer.parseInt(list.get(2)));
            }
            catch (NumberFormatException ignored) {
            }
        }
        return new Version(0, 0, 0);
    }

    private Unity3dPaths() {
    }
}
