package consulo.unity3d.unityscript.lang;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.PsiParser;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JavaScriptParsingFlexLexer;
import com.intellij.lang.javascript.highlighting.JSHighlighter;
import com.intellij.lexer.Lexer;
import consulo.javascript.lang.BaseJavaScriptLanguageVersion;
import consulo.javascript.lang.JavaScriptLanguage;
import consulo.javascript.lang.parsing.EcmaScript4Parser;
import consulo.lombok.annotations.Lazy;

/**
 * @author VISTALL
 * @since 04.04.2015
 */
public class UnityScriptLanguageVersion extends BaseJavaScriptLanguageVersion
{
	@NotNull
	@Lazy
	public static UnityScriptLanguageVersion getInstance()
	{
		return JavaScriptLanguage.INSTANCE.findVersionByClass(UnityScriptLanguageVersion.class);
	}

	private final DialectOptionHolder myDialectOptionHolder = new DialectOptionHolder(true, false);

	public UnityScriptLanguageVersion()
	{
		super("UNITY_SCRIPT");
	}

	@NotNull
	@Override
	public PsiParser createParser()
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
	public Lexer createLexer()
	{
		return new JavaScriptParsingFlexLexer(myDialectOptionHolder);
	}
}
