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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.application.progress.ProgressManager;
import consulo.codeEditor.Caret;
import consulo.csharp.impl.ide.completion.util.SpaceInsertHandler;
import consulo.dotnet.psi.search.searches.DirectTypeInheritorsSearch;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.*;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.project.Project;
import consulo.ui.image.ImageEffects;
import consulo.unity3d.shaderlab.lang.ShaderLabFileType;
import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabCompositeRole;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRoles;
import consulo.unity3d.shaderlab.lang.psi.*;
import consulo.unity3d.shaderlab.lang.psi.light.LightShaderDef;
import consulo.unity3d.shaderlab.lang.psi.stub.index.ShaderDefIndex;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
@ExtensionImpl
public class ShaderLabCompletionContributor extends CompletionContributor
{
	public ShaderLabCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement(ShaderLabTokens.IDENTIFIER).withParent(ShaderPropertyTypeElement.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
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
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
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
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull final CompletionResultSet result)
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
					builder = builder.withIcon(ImageEffects.layered(ShaderLabFileType.INSTANCE.getIcon(), AllIcons.Nodes.FinalMark));
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
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull final CompletionResultSet result)
			{
				ShaderSimpleValue simpleValue = PsiTreeUtil.getParentOfType(parameters.getPosition(), ShaderSimpleValue.class);
				if(simpleValue == null)
				{
					return;
				}
				ShaderLabRole role = simpleValue.getRole();

				for(String value : role.getValues())
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
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull final CompletionResultSet result)
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

					LookupElementBuilder builder = LookupElementBuilder.create(StringUtil.QUOTER.apply(vmQName));
					builder = builder.withIcon(IconDescriptorUpdaters.getIcon(typeDeclaration, 0));
					builder = builder.withPresentableText(vmQName);
					result.addElement(builder);
				});
			}
		});
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return ShaderLabLanguage.INSTANCE;
	}
}
