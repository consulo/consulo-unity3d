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

package consulo.unity3d.projectImport;

import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.content.bundle.SdkType;
import consulo.project.Project;
import consulo.project.ProjectRunOnceExtension;
import consulo.unity3d.jsonApi.UnityOpenFilePostHandlerRequest;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-07-16
 */
@ExtensionImpl
public class UnityRunOneExtension implements ProjectRunOnceExtension<UnityRunOneExtension.Data> {
    public record Data(String sdkName, UnityOpenFilePostHandlerRequest request) {
    }

    public static final String ID = "unity-import";

    private final Project myProject;
    private final SdkTable mySdkTable;

    @Inject
    public UnityRunOneExtension(Project project, SdkTable sdkTable) {
        myProject = project;
        mySdkTable = sdkTable;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Class<Data> getInputClass() {
        return UnityRunOneExtension.Data.class;
    }

    @Override
    public void run(Data data) {
        Sdk sdk = mySdkTable.findSdk(data.sdkName());

        Unity3dProjectImporter.syncProjectStep(myProject, sdk, data.request(), true);
    }
}
