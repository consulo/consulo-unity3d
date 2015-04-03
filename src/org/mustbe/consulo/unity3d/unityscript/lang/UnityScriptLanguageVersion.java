package org.mustbe.consulo.unity3d.unityscript.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.javascript.lang.BaseJavaScriptLanguageVersion;
import org.mustbe.consulo.javascript.lang.parsing.EcmaScript4Parser;
import org.mustbe.consulo.unity3d.unityscript.module.extension.Unity3dScriptModuleExtension;
import com.intellij.lang.LanguageVersionWithDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JavaScriptParsingLexer;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.highlighting.JSHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 04.04.2015
 */
public class UnityScriptLanguageVersion extends BaseJavaScriptLanguageVersion implements LanguageVersionWithDefinition<JavascriptLanguage>
{
	private final DialectOptionHolder myDialectOptionHolder = new DialectOptionHolder(true, false);

	public UnityScriptLanguageVersion()
	{
		super("UNITY_SCRIPT");
	}

	@NotNull
	@Override
	public PsiParser createParser(@Nullable Project project)
	{
		return new EcmaScript4Parser();
	}

	@NotNull
	@Override
	public JSHighlighter getSyntaxHighlighter()
	{
		return new JSHighlighter(myDialectOptionHolder);
	}

	@NotNull
	@Override
	public Lexer createLexer(@Nullable Project project)
	{
		return new JavaScriptParsingLexer(myDialectOptionHolder);
	}

	@Override
	public boolean isMyElement(@Nullable PsiElement element)
	{
		if(element == null)
		{
			return false;
		}
		PsiFile containingFile = element.getContainingFile();
		if(containingFile == null)
		{
			return false;
		}
		return isMyFile(element.getProject(), containingFile.getVirtualFile());
	}

	@Override
	public boolean isMyFile(@Nullable Project project, @Nullable VirtualFile virtualFile)
	{
		if(project == null || virtualFile == null)
		{
			return false;
		}
		return ModuleUtilCore.getExtension(project, virtualFile, Unity3dScriptModuleExtension.class) != null;
	}
}
