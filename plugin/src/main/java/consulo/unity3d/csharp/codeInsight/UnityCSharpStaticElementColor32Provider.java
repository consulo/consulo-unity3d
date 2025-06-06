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
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.psi.ElementColorProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.ui.color.ColorValue;
import consulo.ui.color.RGBColor;
import consulo.unity3d.Unity3dTypes;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 01.04.2015
 */
@ExtensionImpl
public class UnityCSharpStaticElementColor32Provider implements ElementColorProvider {
    @Nullable
    @Override
    @RequiredReadAction
    public ColorValue getColorFrom(@Nonnull PsiElement element) {
        IElementType elementType = element.getNode().getElementType();
        if (elementType == CSharpTokens.NEW_KEYWORD) {
            PsiElement parent = element.getParent();
            if (!(parent instanceof CSharpNewExpression)) {
                return null;
            }

            PsiElement resolvedElementMaybeConstructor = ((CSharpNewExpression) parent).resolveToCallable();
            if (!(resolvedElementMaybeConstructor instanceof CSharpConstructorDeclaration)) {
                return null;
            }

            DotNetType newType = ((CSharpNewExpression) parent).getNewType();
            if (newType == null) {
                return null;
            }

            if (UnityCSharpStaticElementColorProvider.parentIsColorType(resolvedElementMaybeConstructor, Unity3dTypes.UnityEngine.Color32)) {
                ResolveResult validResult = CSharpResolveUtil.findFirstValidResult(((CSharpNewExpression) parent).multiResolve(false));
                if (!(validResult instanceof MethodResolveResult)) {
                    return null;
                }

                MethodResolvePriorityInfo calcResult = ((MethodResolveResult) validResult).getCalcResult();
                Map<String, Integer> map = new HashMap<String, Integer>(4);
                for (NCallArgument nCallArgument : calcResult.getArguments()) {
                    String parameterName = nCallArgument.getParameterName();
                    if (parameterName == null) {
                        continue;
                    }
                    CSharpCallArgument callArgument = nCallArgument.getCallArgument();
                    if (callArgument == null) {
                        continue;
                    }
                    DotNetExpression argumentExpression = callArgument.getArgumentExpression();
                    if (argumentExpression == null) {
                        continue;
                    }

                    Object value = new ConstantExpressionEvaluator(argumentExpression).getValue();
                    if (value instanceof Number) {
                        int intValue = ((Number) value).intValue();
                        if (intValue < 0 || intValue > 255) {
                            return null;
                        }
                        map.put(parameterName, intValue);
                    }
                    else {
                        return null;
                    }
                }

                if (map.size() == 4) {
                    return new RGBColor(map.get("r"), map.get("g"), map.get("b"), map.get("a") / 255f);
                }
            }
        }

        return null;
    }

    @Override
    @RequiredWriteAction
    public void setColorTo(@Nonnull PsiElement element, @Nonnull ColorValue color) {
        CSharpNewExpression newExpression = PsiTreeUtil.getParentOfType(element, CSharpNewExpression.class);
        assert newExpression != null;
        DotNetType newType = newExpression.getNewType();
        assert newType != null;
        StringBuilder builder = new StringBuilder().append("new ").append(newType.getText()).append("(");

        RGBColor rgbColor = color.toRGB();

        builder.append(rgbColor.getRed()).append(", ");
        builder.append(rgbColor.getGreen()).append(", ");
        builder.append(rgbColor.getBlue()).append(", ");
        builder.append((int) (rgbColor.getAlpha() * 255f));
        builder.append(")");

        CSharpNewExpression expression = (CSharpNewExpression) CSharpFileFactory.createExpression(element.getProject(), builder.toString());

        newExpression.getParameterList().replace(expression.getParameterList());
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CSharpLanguage.INSTANCE;
    }
}
