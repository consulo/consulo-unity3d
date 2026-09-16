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

package consulo.unity3d.base.editor;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.application.Application;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

/**
 * Talks to a running UnityEditor over its local http api.
 * <p>
 * The implementation needs process discovery and ui, so it lives in the main plugin module. Callers that only
 * need to send a request - the project importer, for one - depend on this interface instead.
 *
 * @author VISTALL
 */
@ServiceAPI(ComponentScope.APPLICATION)
public interface UnityEditorCommunicator {
    @Nonnull
    static UnityEditorCommunicator getInstance() {
        return Application.get().getInstance(UnityEditorCommunicator.class);
    }

    /**
     * Posts {@code postObject} to the running editor. The request url is derived from the object's simple class
     * name, so those names are part of the protocol and must not be renamed.
     *
     * @param silent when {@code false}, report a missing editor to the user
     * @return {@code true} when the editor accepted the request
     */
    boolean request(@Nonnull Project project, @Nonnull Object postObject, boolean silent);
}
