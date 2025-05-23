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

package consulo.unity3d.csharp;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.jdom.JDOMUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 19.12.14
 */
public class UnityFunctionManager
{
	public static class FunctionInfo
	{
		private final Map<String, String> myParameters = new LinkedHashMap<String, String>();
		private final String myDescription;
		private final String myName;

		FunctionInfo(Element element)
		{
			myName = element.getAttributeValue("name");
			myDescription = element.getChildText("description");
			Element parametersElement = element.getChild("parameters");
			if(parametersElement != null)
			{
				for(Element parameters : parametersElement.getChildren())
				{
					String name = parameters.getAttributeValue("name");
					String type = parameters.getAttributeValue("type");
					myParameters.put(name, type);
				}
			}
		}

		FunctionInfo(String name, String description)
		{
			myName = name;
			myDescription = description;
		}

		public String getName()
		{
			return myName;
		}

		@Nonnull
		public Map<String, String> getParameters()
		{
			return myParameters;
		}

		public String getDescription()
		{
			return myDescription;
		}

		public FunctionInfo createNonParameterListCopy()
		{
			if(myParameters.isEmpty())
			{
				return null;
			}

			return new FunctionInfo(myName, myDescription);
		}
	}

	private static final Logger LOG = Logger.getInstance(UnityFunctionManager.class);

	private Map<String, Map<String, FunctionInfo>> myFunctionsByType = new HashMap<>();

	private static UnityFunctionManager ourInstance = new UnityFunctionManager();

	@Nonnull
	public static UnityFunctionManager getInstance()
	{
		return ourInstance;
	}

	public UnityFunctionManager()
	{
		try
		{
			Document document = JDOMUtil.loadDocument(UnityFunctionManager.class.getResourceAsStream("/functions.xml"));
			for(Element typeElement : document.getRootElement().getChildren())
			{
				String typeName = typeElement.getAttributeValue("name");
				Map<String, FunctionInfo> value = new HashMap<>();
				myFunctionsByType.put(typeName, value);
				for(Element element : typeElement.getChildren())
				{
					FunctionInfo functionInfo = new FunctionInfo(element);
					value.put(functionInfo.myName, functionInfo);
				}
			}
		}
		catch(JDOMException | IOException e)
		{
			LOG.error(e);
		}
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef createTypeRef(@Nonnull PsiElement scope, @Nonnull String type)
	{
		int count = 0;
		int i = 0;
		while((i = type.lastIndexOf("[]")) != -1)
		{
			type = type.substring(0, i);
			count++;
		}

		Project project = scope.getProject();
		GlobalSearchScope resolveScope = scope.getResolveScope();

		DotNetTypeRef typeRef = new CSharpTypeRefByQName(project, resolveScope, type);

		for(int j = 0; j < count; j++)
		{
			typeRef = new CSharpArrayTypeRef(project, resolveScope, typeRef, 0);
		}
		return typeRef;
	}

	@Nonnull
	public Map<String, Map<String, FunctionInfo>> getFunctionsByType()
	{
		return myFunctionsByType;
	}
}
