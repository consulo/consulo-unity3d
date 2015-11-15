/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d.unityscript.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.debugger.DotNetDebugContext;
import org.mustbe.consulo.dotnet.debugger.DotNetDebuggerProvider;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.unity3d.unityscript.lang.UnityScriptLanguageVersion;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import mono.debugger.StackFrameMirror;

/**
 * @author VISTALL
 * @since 27.04.2015
 */
public class UnityScriptDebuggerProvider extends DotNetDebuggerProvider
{
	@NotNull
	@Override
	public PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull PsiElement element, @NotNull String s, boolean b)
	{
		throw new IllegalArgumentException();
	}

	@Override
	public void evaluate(@NotNull StackFrameMirror stackFrameMirror,
			@NotNull DotNetDebugContext context,
			@NotNull String s,
			@Nullable PsiElement element,
			@NotNull XDebuggerEvaluator.XEvaluationCallback xEvaluationCallback,
			@Nullable XSourcePosition sourcePosition)
	{

	}

	@Override
	public void evaluate(@NotNull StackFrameMirror stackFrameMirror,
			@NotNull DotNetDebugContext dotNetDebugContext,
			@NotNull DotNetReferenceExpression dotNetReferenceExpression,
			@NotNull XDebuggerEvaluator.XEvaluationCallback xEvaluationCallback)
	{

	}

	@Override
	public boolean isSupported(@NotNull PsiFile psiFile)
	{
		return psiFile instanceof JSFile && psiFile.getLanguageVersion() instanceof UnityScriptLanguageVersion;
	}

	@Override
	public Language getEditorLanguage()
	{
		return JavascriptLanguage.INSTANCE;
	}
}
