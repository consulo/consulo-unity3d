/*
 * Copyright 2013-2016 consulo.io
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

package consulo.unity3d.base.module.ui;

import consulo.disposer.Disposable;
import consulo.dotnet.module.extension.DotNetConfigurationPanel;
import consulo.module.ui.extension.ModuleExtensionBundleBoxBuilder;
import consulo.ui.Component;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;
import consulo.unity3d.base.module.Unity3dRootMutableModuleExtension;
import consulo.unity3d.localize.Unity3dLocalize;

import java.util.List;

/**
 * @author VISTALL
 * @see DotNetConfigurationPanel
 * @since 27.10.14
 */
public final class UnityConfigurationPanel {
    private UnityConfigurationPanel() {
    }

    @RequiredUIAccess
    public static Component create(
        Unity3dRootMutableModuleExtension extension,
        List<String> variables,
        Disposable uiDisposable,
        Runnable updater
    ) {
        ModuleExtensionBundleBoxBuilder<Unity3dRootMutableModuleExtension> sdkBoxBuilder =
            ModuleExtensionBundleBoxBuilder.create(extension, uiDisposable, updater);
        sdkBoxBuilder.sdkTypeClass(extension.getSdkTypeClass());
        sdkBoxBuilder.sdkPointerFunc(Unity3dRootMutableModuleExtension::getInheritableSdk);

        TextBox namespacePrefixBox = TextBox.create(extension.getNamespacePrefix());
        namespacePrefixBox.addValueListener(event -> extension.setNamespacePrefix(event.getValue()));

        FormBuilder formBuilder = FormBuilder.create();
        formBuilder.addLabeled(Unity3dLocalize.namespacePrefixLabel(), namespacePrefixBox);

        VerticalLayout topLayout = VerticalLayout.create();
        topLayout.add(sdkBoxBuilder.build());
        topLayout.add(formBuilder.build());

        DockLayout layout = DockLayout.create();
        layout.top(topLayout);
        layout.center(DotNetConfigurationPanel.createVariableList(variables));
        return layout;
    }
}
