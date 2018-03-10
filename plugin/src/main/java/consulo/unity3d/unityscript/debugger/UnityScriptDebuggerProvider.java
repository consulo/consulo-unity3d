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

package consulo.unity3d.unityscript.debugger;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XNamedValue;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.DotNetDebuggerProvider;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.javascript.lang.JavaScriptLanguage;
import consulo.unity3d.unityscript.lang.UnityScriptLanguageVersion;

/**
 * @author VISTALL
 * @since 27.04.2015
 */
public class UnityScriptDebuggerProvider extends DotNetDebuggerProvider
{
	@Nonnull
	@Override
	public PsiFile createExpressionCodeFragment(@Nonnull Project project, @Nonnull PsiElement element, @Nonnull String s, boolean b)
	{
		throw new IllegalArgumentException();
	}

	@Override
	public void evaluate(@Nonnull DotNetStackFrameProxy stackFrameMirror,
			@Nonnull DotNetDebugContext context,
			@Nonnull String s,
			@Nullable PsiElement element,
			@Nonnull XDebuggerEvaluator.XEvaluationCallback callback,
			@Nullable XSourcePosition sourcePosition)
	{
		callback.errorOccurred("UnityScript evaluation is not supported");
	}

	@Override
	public void evaluate(@Nonnull DotNetStackFrameProxy stackFrameMirror,
			@Nonnull DotNetDebugContext dotNetDebugContext,
			@Nonnull DotNetReferenceExpression dotNetReferenceExpression,
			@Nonnull Set<Object> set,
			@Nonnull Consumer<XNamedValue> consumer)
	{
	}

	@Override
	public boolean isSupported(@Nonnull PsiFile psiFile)
	{
		return psiFile instanceof JSFile && psiFile.getLanguageVersion() instanceof UnityScriptLanguageVersion;
	}

	@Override
	public Language getEditorLanguage()
	{
		return JavaScriptLanguage.INSTANCE;
	}
}
