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

import com.dslplatform.json.CompiledJson;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-24
 */
@CompiledJson
public class Unity3dAttachConfigurationOptions extends RunConfigurationOptions {
    private @Nullable String myAttachTarget;
    private @Nullable String myProcessName;

    /**
     * The {@link Unity3dAttachConfiguration.AttachTarget} name, {@code null} for the Unity Editor.
     */
    @Attribute("attachTarget")
    public @Nullable String getAttachTarget() {
        return myAttachTarget;
    }

    public void setAttachTarget(@Nullable String attachTarget) {
        myAttachTarget = attachTarget;
    }

    @Attribute("processName")
    public @Nullable String getProcessName() {
        return myProcessName;
    }

    public void setProcessName(@Nullable String processName) {
        myProcessName = StringUtil.nullize(processName, true);
    }
}
