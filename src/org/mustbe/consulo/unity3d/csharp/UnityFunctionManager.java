/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.unity3d.csharp;

import gnu.trove.THashMap;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.consulo.lombok.annotations.LazyInstance;
import org.consulo.lombok.annotations.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.JDOMUtil;

/**
 * @author VISTALL
 * @since 19.12.14
 */
@Logger
public class UnityFunctionManager
{
	public static class FunctionInfo
	{
		private final Map<String, DotNetTypeRef> myParameters = new LinkedHashMap<String, DotNetTypeRef>();
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
					myParameters.put(name, createTypeRef(type));
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

		@NotNull
		public Map<String, DotNetTypeRef> getParameters()
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

		@NotNull
		private static DotNetTypeRef createTypeRef(@NotNull String type)
		{
			int count = 0;
			int i = 0;
			while((i = type.lastIndexOf("[]")) != -1)
			{
				type = type.substring(0, i);
				count ++;
			}
			DotNetTypeRef typeRef = new CSharpTypeRefByQName(type);

			for(int j = 0; j < count; j++)
			{
				typeRef = new CSharpArrayTypeRef(typeRef, 0);
			}
			return typeRef;
		}
	}

	private Map<String, FunctionInfo> myFunctions = new THashMap<String, FunctionInfo>();

	@LazyInstance
	@NotNull
	public static UnityFunctionManager getInstance()
	{
		return new UnityFunctionManager();
	}

	public UnityFunctionManager()
	{
		try
		{
			Document document = JDOMUtil.loadDocument(UnityFunctionManager.class.getResourceAsStream("/functions.xml"));
			for(Element element : document.getRootElement().getChildren())
			{
				FunctionInfo functionInfo = new FunctionInfo(element);
				myFunctions.put(functionInfo.myName, functionInfo);
			}
		}
		catch(JDOMException e)
		{
			LOGGER.error(e);
		}
		catch(IOException e)
		{
			LOGGER.error(e);
		}
	}

	@NotNull
	public Collection<FunctionInfo> getFunctionInfos()
	{
		return myFunctions.values();
	}

	public FunctionInfo getFunctionInfo(String name)
	{
		return myFunctions.get(name);
	}
}
