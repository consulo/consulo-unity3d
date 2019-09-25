/*
 * Copyright 2013-2019 consulo.io
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

package consulo.unity3d.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.documentation.ExternalDocumentationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.platform.Platform;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2019-01-25
 */
public class UnityExternalDocumentationProvider implements ExternalDocumentationProvider, DocumentationProvider
{
	private static final String UnityEnginePrefix = "UnityEngine.";

	@Nullable
	@Override
	public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement)
	{
		return null;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public List<String> getUrlFor(PsiElement element, PsiElement originalElement)
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(element.getProject());
		if(rootModuleExtension == null)
		{
			return null;
		}

		Sdk sdk = rootModuleExtension.getSdk();
		if(sdk == null)
		{
			return null;
		}

		String fileName = getFileName(element);
		if(fileName == null)
		{
			return null;
		}

		String homePath = sdk.getHomePath();

		File file;
		if(Platform.current().os().isMac())
		{
			file = new File(homePath, "Contents/Documentation/en/ScriptReference/" + fileName + ".html");
		}
		else
		{
			file = new File(homePath, "Editor/Data/Documentation/en/ScriptReference/" + fileName + ".html");
		}

		try
		{
			return Collections.singletonList(StandardFileSystems.FILE_PROTOCOL_PREFIX + FileUtil.toSystemIndependentName(file.getCanonicalPath()));
		}
		catch(IOException e)
		{
			throw new UnsupportedOperationException(e);
		}
	}

	@RequiredReadAction
	private static String getFileName(PsiElement element)
	{
		if(!(element instanceof DotNetTypeDeclaration) && !(element instanceof DotNetLikeMethodDeclaration) && !(element instanceof DotNetFieldDeclaration) && !(element instanceof
				DotNetPropertyDeclaration))
		{
			return null;
		}

		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(element.getProject());
		if(rootModuleExtension == null)
		{
			return null;
		}

		DotNetTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(element, DotNetTypeDeclaration.class, false);
		if(typeDeclaration == null)
		{
			return null;
		}

		String qName = typeDeclaration.getPresentableQName();
		if(qName == null)
		{
			return null;
		}

		if(!StringUtil.startsWith(qName, UnityEnginePrefix))
		{
			return null;
		}

		StringBuilder builder = new StringBuilder();
		builder.append(StringUtil.trimStart(qName, UnityEnginePrefix));

		if(element != typeDeclaration)
		{
			builder.append("-");
			builder.append(StringUtil.toLowerCase(((PsiNamedElement) element).getName()));
		}
		return builder.toString();
	}

	@Nullable
	@Override
	public String generateDoc(PsiElement element, @Nullable PsiElement originalElement)
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element)
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context)
	{
		return null;
	}

	@Nullable
	@Override
	public String fetchExternalDocumentation(Project project, PsiElement element, List<String> docUrls)
	{
		return null;
	}

	@Override
	@RequiredReadAction
	public boolean hasDocumentationFor(PsiElement element, PsiElement originalElement)
	{
		return getFileName(element) != null;
	}

	@Override
	public boolean canPromptToConfigureDocumentation(PsiElement element)
	{
		return false;
	}

	@Override
	public void promptToConfigureDocumentation(PsiElement element)
	{
	}
}
