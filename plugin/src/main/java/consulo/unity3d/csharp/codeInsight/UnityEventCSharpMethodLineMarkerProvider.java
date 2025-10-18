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

package consulo.unity3d.csharp.codeInsight;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.csharp.impl.ide.lineMarkerProvider.CSharpLineMarkerUtil;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.Language;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProviderDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.csharp.UnityFunctionManager;
import consulo.unity3d.icon.Unity3dIconGroup;
import consulo.unity3d.module.Unity3dModuleExtension;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Map;

/**
 * @author VISTALL
 * @since 19.12.14
 */
@ExtensionImpl
public class UnityEventCSharpMethodLineMarkerProvider extends LineMarkerProviderDescriptor {
    @Nonnull
    @Override
    public LocalizeValue getName() {
        return LocalizeValue.localizeTODO("Unity C# Event Method");
    }

    @Nullable
    @Override
    public Image getIcon() {
        return Unity3dIconGroup.lightningbolt();
    }

    @RequiredReadAction
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@Nonnull PsiElement element) {
        return createMarker(element);
    }

    @Nullable
    @RequiredReadAction
    private static LineMarkerInfo createMarker(final PsiElement element) {
        CSharpMethodDeclaration methodDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpMethodDeclaration.class);
        if (methodDeclaration != null) {
            Unity3dModuleExtension extension = ModuleUtilCore.getExtension(element, Unity3dModuleExtension.class);
            if (extension == null) {
                return null;
            }

            UnityFunctionManager.FunctionInfo magicMethod = findMagicMethod(methodDeclaration);
            if (magicMethod != null) {
                return new LineMarkerInfo<>(element, element.getTextRange(), Unity3dIcons.EventMethod, Pass.LINE_MARKERS, (e) -> magicMethod.getDescription(), null, GutterIconRenderer.Alignment
                    .LEFT);
            }
        }

        return null;
    }

    @RequiredReadAction
    public static UnityFunctionManager.FunctionInfo findMagicMethod(@Nonnull CSharpMethodDeclaration methodDeclaration) {
        PsiElement maybeTypeDeclaration = methodDeclaration.getParent();
        if (maybeTypeDeclaration instanceof CSharpTypeDeclaration) {
            UnityFunctionManager functionManager = UnityFunctionManager.getInstance();
            for (Map.Entry<String, Map<String, UnityFunctionManager.FunctionInfo>> entry : functionManager.getFunctionsByType().entrySet()) {
                UnityFunctionManager.FunctionInfo functionInfo = entry.getValue().get(methodDeclaration.getName());
                if (functionInfo == null) {
                    continue;
                }

                String typeName = entry.getKey();
                if (DotNetInheritUtil.isParent(typeName, (DotNetTypeDeclaration) maybeTypeDeclaration, true)) {
                    if (!isEqualParameters(functionInfo.getParameters(), methodDeclaration)) {
                        return null;
                    }

                    return functionInfo;
                }
            }
        }
        return null;
    }

    @RequiredReadAction
    private static boolean isEqualParameters(Map<String, String> funcParameters, DotNetParameterListOwner parameterListOwner) {
        DotNetParameter[] parameters = parameterListOwner.getParameters();
        if (parameters.length == 0) {
            return true;
        }
        if (parameters.length != funcParameters.size()) {
            return false;
        }

        int i = 0;
        for (String expectedType : funcParameters.values()) {
            DotNetParameter parameter = parameters[i++];

            DotNetTypeRef typeRef = UnityFunctionManager.createTypeRef(parameter, expectedType);
            if (!CSharpTypeUtil.isTypeEqual(parameter.toTypeRef(true), typeRef)) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CSharpLanguage.INSTANCE;
    }
}
