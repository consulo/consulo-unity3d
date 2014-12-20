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
					myParameters.put(parameters.getAttributeValue("name"), parameters.getAttributeValue("type"));
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
