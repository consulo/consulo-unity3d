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

package consulo.unity3d.shaderlab.ide.completion;

import javax.annotation.Nullable;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;

/**
 * @author VISTALL
 * @see com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
 * @since 09.05.2015
 */
public class BracesInsertHandler<T extends LookupElement> implements InsertHandler<T>
{
	public static final BracesInsertHandler<LookupElement> INSTANCE = new BracesInsertHandler<LookupElement>();

	private final boolean mySpaceBeforeParentheses;
	private final boolean mySpaceBetweenParentheses;
	private final boolean myMayInsertRightParenthesis;
	private final boolean myAllowParametersOnNextLine;

	protected BracesInsertHandler(final boolean spaceBeforeParentheses, final boolean spaceBetweenParentheses, final boolean mayInsertRightParenthesis)
	{
		this(spaceBeforeParentheses, spaceBetweenParentheses, mayInsertRightParenthesis, false);
	}

	protected BracesInsertHandler(boolean spaceBeforeParentheses, boolean spaceBetweenParentheses, boolean mayInsertRightParenthesis, boolean allowParametersOnNextLine)
	{
		mySpaceBeforeParentheses = spaceBeforeParentheses;
		mySpaceBetweenParentheses = spaceBetweenParentheses;
		myMayInsertRightParenthesis = mayInsertRightParenthesis;
		myAllowParametersOnNextLine = allowParametersOnNextLine;
	}

	protected BracesInsertHandler()
	{
		this(false, false, true);
	}

	private static boolean isToken(@Nullable final PsiElement element, final String text)
	{
		return element != null && text.equals(element.getText());
	}

	protected boolean placeCaretInsideParentheses(final InsertionContext context, final T item)
	{
		return true;
	}

	@Override
	public void handleInsert(final InsertionContext context, final T item)
	{
		final Editor editor = context.getEditor();
		final Document document = editor.getDocument();
		context.commitDocument();
		PsiElement element = findNextToken(context);

		final char completionChar = context.getCompletionChar();
		final boolean putCaretInside = completionChar == '{' || placeCaretInsideParentheses(context, item);

		if(completionChar == '{')
		{
			context.setAddCompletionChar(false);
		}

		if(isToken(element, "{"))
		{
			int lparenthOffset = element.getTextRange().getStartOffset();
			if(mySpaceBeforeParentheses && lparenthOffset == context.getTailOffset())
			{
				document.insertString(context.getTailOffset(), " ");
				lparenthOffset++;
			}

			if(completionChar == '{' || completionChar == '\t')
			{
				editor.getCaretModel().moveToOffset(lparenthOffset + 1);
			}
			else
			{
				editor.getCaretModel().moveToOffset(context.getTailOffset());
			}

			context.setTailOffset(lparenthOffset + 1);

			PsiElement list = element.getParent();
			PsiElement last = list.getLastChild();
			if(isToken(last, "}"))
			{
				int rparenthOffset = last.getTextRange().getStartOffset();
				context.setTailOffset(rparenthOffset + 1);
				if(!putCaretInside)
				{
					for(int i = lparenthOffset + 1; i < rparenthOffset; i++)
					{
						if(!Character.isWhitespace(document.getCharsSequence().charAt(i)))
						{
							return;
						}
					}
					editor.getCaretModel().moveToOffset(context.getTailOffset());
				}
				else if(mySpaceBetweenParentheses && document.getCharsSequence().charAt(lparenthOffset) == ' ')
				{
					editor.getCaretModel().moveToOffset(lparenthOffset + 2);
				}
				else
				{
					editor.getCaretModel().moveToOffset(lparenthOffset + 1);
				}
				return;
			}
		}
		else
		{
			document.insertString(context.getTailOffset(), getSpace(mySpaceBeforeParentheses) + '{' + getSpace(mySpaceBetweenParentheses));
			editor.getCaretModel().moveToOffset(context.getTailOffset());
		}

		if(!myMayInsertRightParenthesis)
		{
			return;
		}

		if(context.getCompletionChar() == '{')
		{
			int tail = context.getTailOffset();
			if(tail < document.getTextLength() && StringUtil.isJavaIdentifierPart(document.getCharsSequence().charAt(tail)))
			{
				return;
			}
		}

		document.insertString(context.getTailOffset(), getSpace(mySpaceBetweenParentheses) + "}");
		if(!putCaretInside)
		{
			editor.getCaretModel().moveToOffset(context.getTailOffset());
		}
	}

	private static String getSpace(boolean needSpace)
	{
		return needSpace ? " " : "";
	}

	@Nullable
	protected PsiElement findNextToken(final InsertionContext context)
	{
		final PsiFile file = context.getFile();
		PsiElement element = file.findElementAt(context.getTailOffset());
		if(element instanceof PsiWhiteSpace)
		{
			if(!myAllowParametersOnNextLine && element.getText().contains("\n"))
			{
				return null;
			}
			element = file.findElementAt(element.getTextRange().getEndOffset());
		}
		return element;
	}
}
