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

package consulo.unity3d.jsonApi;

import consulo.ui.ex.MessageCategory;
import consulo.util.lang.ObjectUtil;
import org.intellij.lang.annotations.MagicConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 07-Jun-16
 */
public class UnityLogPostHandlerRequest
{
	private static Map<String, Integer> ourTypeMap = new HashMap<>();

	static
	{
		ourTypeMap.put("Error", MessageCategory.ERROR);
		ourTypeMap.put("Assert", MessageCategory.ERROR);
		ourTypeMap.put("Warning", MessageCategory.WARNING);
		ourTypeMap.put("Log", MessageCategory.INFORMATION);
		ourTypeMap.put("Exception", MessageCategory.ERROR);
	}

	public String condition;
	public String stackTrace;
	public String projectPath;
	public String type;

	@MagicConstant(valuesFromClass = MessageCategory.class)
	public int getMessageCategory()
	{
		return ObjectUtil.notNull(ourTypeMap.get(type), MessageCategory.INFORMATION);
	}
}
