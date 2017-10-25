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

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.csharp.ide.completion.util.SpaceInsertHandler;
import consulo.dotnet.psi.search.searches.DirectTypeInheritorsSearch;
import consulo.ide.IconDescriptorUpdaters;
import consulo.unity3d.shaderlab.lang.ShaderLabFileType;
import consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabCompositeRole;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRoles;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabSimpleRole;
import consulo.unity3d.shaderlab.lang.psi.ShaderBraceOwner;
import consulo.unity3d.shaderlab.lang.psi.ShaderDef;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabKeyTokens;
import consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;
import consulo.unity3d.shaderlab.lang.psi.ShaderPropertyTypeElement;
import consulo.unity3d.shaderlab.lang.psi.ShaderSimpleValue;
import consulo.unity3d.shaderlab.lang.psi.light.LightShaderDef;
import consulo.unity3d.shaderlab.lang.psi.stub.index.ShaderDefIndex;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderLabCompletionContributor extends CompletionContributor
{
	public ShaderLabCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement(ShaderLabTokens.IDENTIFIER).withParent(ShaderPropertyTypeElement.class), new CompletionProvider()

		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				for(ShaderLabPropertyType shaderLabPropertyType : ShaderLabPropertyType.values())
				{
					LookupElementBuilder builder = LookupElementBuilder.create(shaderLabPropertyType.getPresentableName());
					builder = builder.bold();

					result.addElement(builder);
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement().withParent(ShaderBraceOwner.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				ShaderBraceOwner braceOwner = PsiTreeUtil.getParentOfType(parameters.getPosition(), ShaderBraceOwner.class);
				if(braceOwner == null)
				{
					return;
				}
				ShaderLabRole role = braceOwner.getRole();
				if(!(role instanceof ShaderLabCompositeRole))
				{
					return;
				}

				for(final ShaderLabRole labRole : ((ShaderLabCompositeRole) role).getRoles())
				{
					LookupElementBuilder builder = LookupElementBuilder.create(labRole.getName());
					if(labRole instanceof ShaderLabCompositeRole)
					{
						builder = builder.withInsertHandler(BracesInsertHandler.INSTANCE);
					}
					else
					{
						final String defaultInsertValue = labRole.getDefaultInsertValue();
						if(defaultInsertValue == null)
						{
							builder = builder.withInsertHandler(SpaceInsertHandler.INSTANCE);
						}
						else
						{
							builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
							{
								@Override
								public void handleInsert(InsertionContext context, LookupElement item)
								{
									int offset = context.getTailOffset();
									offset = TailType.insertChar(context.getEditor(), offset, ' ');
									context.getDocument().insertString(offset, defaultInsertValue);

									Caret currentCaret = context.getEditor().getCaretModel().getCurrentCaret();
									currentCaret.setSelection(offset, offset + defaultInsertValue.length());
								}
							});
						}
					}

					result.addElement(builder);
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(ShaderLabKeyTokens.START_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result)
			{
				ShaderSimpleValue simpleValue = PsiTreeUtil.getParentOfType(parameters.getPosition(), ShaderSimpleValue.class);
				if(simpleValue == null)
				{
					return;
				}
				ShaderLabRole role = simpleValue.getRole();
				if(role == null || role != ShaderLabRoles.Fallback && role != ShaderLabRoles.UsePass)
				{
					return;
				}

				Project project = parameters.getPosition().getProject();
				Collection<String> allKeys = ShaderDefIndex.getInstance().getAllKeys(project);
				for(String key : allKeys)
				{
					ProgressManager.checkCanceled();

					Collection<ShaderDef> shaderDefs = ShaderDefIndex.getInstance().get(key, project, GlobalSearchScope.projectScope(project));
					if(shaderDefs.isEmpty())
					{
						continue;
					}
					LookupElementBuilder builder = LookupElementBuilder.create(key);
					builder = builder.withIcon(ShaderLabFileType.INSTANCE.getIcon());
					result.addElement(builder);
				}

				for(ShaderDef shaderDef : LightShaderDef.getDefaultShaders(project).values())
				{
					ProgressManager.checkCanceled();

					LookupElementBuilder builder = LookupElementBuilder.create(shaderDef.getName());
					builder = builder.withIcon(new LayeredIcon(ShaderLabFileType.INSTANCE.getIcon(), AllIcons.Nodes.FinalMark));
					result.addElement(builder);
				}

				String defaultInsertValue = role.getDefaultInsertValue();
				if(defaultInsertValue != null)
				{
					result.addElement(LookupElementBuilder.create(defaultInsertValue).bold());
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(ShaderLabKeyTokens.START_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result)
			{
				ShaderSimpleValue simpleValue = PsiTreeUtil.getParentOfType(parameters.getPosition(), ShaderSimpleValue.class);
				if(simpleValue == null)
				{
					return;
				}
				ShaderLabRole role = simpleValue.getRole();
				if(!(role instanceof ShaderLabSimpleRole))
				{
					return;
				}
				for(String value : ((ShaderLabSimpleRole) role).getValues())
				{
					LookupElementBuilder builder = LookupElementBuilder.create(value);
					builder = builder.bold();
					result.addElement(builder);
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(ShaderLabKeyTokens.START_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result)
			{
				ShaderSimpleValue simpleValue = PsiTreeUtil.getParentOfType(parameters.getPosition(), ShaderSimpleValue.class);
				if(simpleValue == null)
				{
					return;
				}
				ShaderLabRole role = simpleValue.getRole();
				if(role == null || role != ShaderLabRoles.CustomEditor)
				{
					return;
				}

				DirectTypeInheritorsSearch.search(parameters.getPosition().getProject(), "UnityEditor.ShaderGUI").forEach(typeDeclaration ->
				{
					String vmQName = typeDeclaration.getVmQName();

					LookupElementBuilder builder = LookupElementBuilder.create(StringUtil.QUOTER.fun(vmQName));
					builder = builder.withIcon(IconDescriptorUpdaters.getIcon(typeDeclaration, 0));
					builder = builder.withPresentableText(vmQName);
					result.addElement(builder);
				});
			}
		});
	}
}
