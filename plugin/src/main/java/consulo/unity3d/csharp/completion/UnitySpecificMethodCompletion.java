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

package consulo.unity3d.csharp.completion;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.codeEditor.CaretModel;
import consulo.csharp.impl.ide.completion.CSharpMemberAddByCompletionContributor;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.icon.IconDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.csharp.UnityFunctionManager;
import consulo.unity3d.module.Unity3dModuleExtension;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 19.12.14
 */
@ExtensionImpl
public class UnitySpecificMethodCompletion implements CSharpMemberAddByCompletionContributor
{
	@RequiredReadAction
	@Override
	public void processCompletion(@Nonnull CompletionParameters completionParameters,
			@Nonnull ProcessingContext processingContext,
			@Nonnull Consumer<LookupElement> completionResultSet,
			@Nonnull CSharpTypeDeclaration typeDeclaration)
	{
		Unity3dModuleExtension extension = ModuleUtilCore.getExtension(typeDeclaration, Unity3dModuleExtension.class);
		if(extension == null)
		{
			return;
		}

		for(Map.Entry<String, Map<String, UnityFunctionManager.FunctionInfo>> entry : UnityFunctionManager.getInstance().getFunctionsByType().entrySet())
		{
			String typeName = entry.getKey();

			if(!DotNetInheritUtil.isParent(typeName, typeDeclaration, true))
			{
				continue;
			}

			for(UnityFunctionManager.FunctionInfo functionInfo : entry.getValue().values())
			{
				UnityFunctionManager.FunctionInfo nonParameterListCopy = functionInfo.createNonParameterListCopy();
				if(nonParameterListCopy != null)
				{
					completionResultSet.accept(buildLookupItem(nonParameterListCopy, typeDeclaration));
				}

				completionResultSet.accept(buildLookupItem(functionInfo, typeDeclaration));
			}
		}
	}

	@Nonnull
	@RequiredReadAction
	private static LookupElementBuilder buildLookupItem(UnityFunctionManager.FunctionInfo functionInfo, CSharpTypeDeclaration scope)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("void ");
		builder.append(functionInfo.getName());
		builder.append("(");

		boolean first = true;
		for(Map.Entry<String, String> entry : functionInfo.getParameters().entrySet())
		{
			if(first)
			{
				first = false;
			}
			else
			{
				builder.append(", ");
			}

			DotNetTypeRef typeRef = UnityFunctionManager.createTypeRef(scope, entry.getValue());
			builder.append(CSharpTypeRefPresentationUtil.buildShortText(typeRef));
			builder.append(" ");
			builder.append(entry.getKey());
		}
		builder.append(")");

		String presentationText = builder.toString();
		builder.append("{\n");
		builder.append("}");

		LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(builder.toString());
		lookupElementBuilder = lookupElementBuilder.withPresentableText(presentationText);
		lookupElementBuilder = lookupElementBuilder.withLookupString(functionInfo.getName());
		lookupElementBuilder = lookupElementBuilder.withTailText("{...}", true);

		IconDescriptor iconDescriptor = new IconDescriptor(new IconDescriptor(AllIcons.Nodes.Method).toIcon());
		iconDescriptor.setRightIcon(Unity3dIcons.EventMethod);

		lookupElementBuilder = lookupElementBuilder.withIcon(iconDescriptor.toIcon());

		lookupElementBuilder = lookupElementBuilder.withInsertHandler(new InsertHandler<LookupElement>()
		{
			@Override
			@RequiredUIAccess
			public void handleInsert(InsertionContext context, LookupElement item)
			{
				CaretModel caretModel = context.getEditor().getCaretModel();

				PsiElement elementAt = context.getFile().findElementAt(caretModel.getOffset() - 1);
				if(elementAt == null)
				{
					return;
				}

				DotNetVirtualImplementOwner virtualImplementOwner = PsiTreeUtil.getParentOfType(elementAt, DotNetVirtualImplementOwner.class);
				if(virtualImplementOwner == null)
				{
					return;
				}

				if(virtualImplementOwner instanceof CSharpMethodDeclaration)
				{
					PsiElement codeBlock = ((CSharpMethodDeclaration) virtualImplementOwner).getCodeBlock().getElement();
					if(codeBlock instanceof CSharpBlockStatementImpl)
					{
						DotNetStatement[] statements = ((CSharpBlockStatementImpl) codeBlock).getStatements();
						if(statements.length > 0)
						{
							caretModel.moveToOffset(statements[0].getTextOffset() + statements[0].getTextLength());
						}
						else
						{
							caretModel.moveToOffset(((CSharpBlockStatementImpl) codeBlock).getLeftBrace().getTextOffset() + 1);
						}
					}
				}

				context.commitDocument();

				CodeStyleManager.getInstance(context.getProject()).reformat(virtualImplementOwner);
			}
		});
		return lookupElementBuilder;
	}
}
