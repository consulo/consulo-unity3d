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

import com.dslplatform.json.CompiledJson;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Set;

/**
 * Contents of an {@code .asmdef} file - a Unity assembly definition.
 * <p>
 * Read straight off disk rather than through psi, so that assembly analysis does not need an open editor or a
 * read lock.
 *
 * @author VISTALL
 */
@CompiledJson
public class AsmDefDescriptor {
    public @Nullable String name;

    public @Nullable String rootNamespace;

    public @Nullable Set<String> references;

    public @Nullable Set<String> optionalUnityReferences;

    public @Nullable Set<String> includePlatforms;

    public @Nullable Set<String> excludePlatforms;

    public @Nullable Set<String> defineConstraints;

    public @Nullable Set<String> precompiledReferences;

    public boolean allowUnsafeCode;

    public boolean overrideReferences;

    public boolean autoReferenced;

    public boolean noEngineReferences;

    @Nonnull
    public Set<String> getReferences() {
        return references == null ? Set.of() : references;
    }

    @Nonnull
    public Set<String> getOptionalUnityReferences() {
        return optionalUnityReferences == null ? Set.of() : optionalUnityReferences;
    }

    @Nonnull
    public Set<String> getIncludePlatforms() {
        return includePlatforms == null ? Set.of() : includePlatforms;
    }

    @Nonnull
    public Set<String> getExcludePlatforms() {
        return excludePlatforms == null ? Set.of() : excludePlatforms;
    }

    @Nonnull
    public Set<String> getDefineConstraints() {
        return defineConstraints == null ? Set.of() : defineConstraints;
    }

    @Nonnull
    public Set<String> getPrecompiledReferences() {
        return precompiledReferences == null ? Set.of() : precompiledReferences;
    }

    @Override
    public String toString() {
        return "AsmDefDescriptor{name='" + name + "'}";
    }
}
