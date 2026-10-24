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
package consulo.unity3d.run;

import org.jspecify.annotations.Nullable;

/**
 * The bean properties a Unity attach configuration wrote before the fragment model.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
public class Unity3dAttachConfigurationLegacyState {
    private Unity3dAttachConfiguration.@Nullable AttachTarget myAttachTarget;
    private @Nullable String myProcessName;

    public Unity3dAttachConfiguration.@Nullable AttachTarget getAttachTarget() {
        return myAttachTarget;
    }

    public void setAttachTarget(Unity3dAttachConfiguration.@Nullable AttachTarget attachTarget) {
        myAttachTarget = attachTarget;
    }

    public @Nullable String getProcessName() {
        return myProcessName;
    }

    public void setProcessName(@Nullable String processName) {
        myProcessName = processName;
    }
}
