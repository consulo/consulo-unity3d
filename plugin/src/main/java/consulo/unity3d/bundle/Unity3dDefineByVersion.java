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

package consulo.unity3d.bundle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.intellij.lang.annotations.RegExp;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 24.01.15
 */
public enum Unity3dDefineByVersion
{
	UNITY_2_6("2.6.\\d", null),
	UNITY_2_6_1("2.6.1", null, UNITY_2_6),
	UNITY_3_0("3.0.\\d", null),
	UNITY_3_0_0("3.0.0", null, UNITY_3_0),
	UNITY_3_1("3.1.\\d", null, UNITY_3_0),
	UNITY_3_2("3.2.\\d", null, UNITY_3_0),
	UNITY_3_3("3.3.\\d", null, UNITY_3_0),
	UNITY_3_4("3.4.\\d", null, UNITY_3_0),
	UNITY_3_5("3.5.\\d", null, UNITY_3_0),
	UNITY_4_0("4.0.\\d", null),
	UNITY_4_0_1("4.0.1", null, UNITY_4_0),
	UNITY_4_1("4.1.\\d", null, UNITY_4_0),
	UNITY_4_2("4.2.\\d", null, UNITY_4_0),
	UNITY_4_3("4.3.\\d", null, UNITY_4_0),
	UNITY_4_5("4.5.\\d", null, UNITY_4_0),
	UNITY_4_6("4.6.\\d", "UnityEditorConsuloPlugin4.6.dll", UNITY_4_0),
	UNITY_4_7("4.7.\\d", "UnityEditorConsuloPlugin4.6.dll", UNITY_4_0),
	UNITY_5_0("5.0.\\d", "UnityEditorConsuloPlugin5.dll"),
	UNITY_5_1("5.1.\\d", "UnityEditorConsuloPlugin5.dll", UNITY_5_0),
	UNITY_5_2("5.2.\\d", "UnityEditorConsuloPlugin5.dll", UNITY_5_0),
	UNITY_5_3("5.3.\\d", "UnityEditorConsuloPlugin5.3.dll", UNITY_5_0),
	UNITY_5_4("5.4.\\d", "UnityEditorConsuloPlugin5.3.dll", UNITY_5_0),
	UNITY_5_5("5.5.\\d", "UnityEditorConsuloPlugin5.3.dll", UNITY_5_0),
	UNITY_5_6("5.6.\\d", "UnityEditorConsuloPlugin5.6.dll", UNITY_5_0),
	UNITY_2017_1("2017.1.\\d", "UnityEditorConsuloPlugin5.6.dll"),
	UNITY_2017_2("2017.2.\\d", "UnityEditorConsuloPlugin2017.2.dll"),
	UNKNOWN("\\d.\\d.\\d", null);

	private final Pattern myVersionPattern;
	@Nullable
	private String myPluginFileName;
	private Unity3dDefineByVersion[] myMajorVersions;

	Unity3dDefineByVersion(@RegExp String versionRegexp, @Nullable String pluginFileName, Unity3dDefineByVersion... majorVersions)
	{
		myPluginFileName = pluginFileName;
		myMajorVersions = majorVersions;
		myVersionPattern = Pattern.compile(versionRegexp);
	}

	@Nullable
	public String getPluginFileName()
	{
		return myPluginFileName;
	}

	@Nonnull
	public Unity3dDefineByVersion[] getMajorVersions()
	{
		return myMajorVersions;
	}

	@Nonnull
	public static Unity3dDefineByVersion find(@Nullable String version)
	{
		if(version == null)
		{
			return UNKNOWN;
		}

		Unity3dDefineByVersion[] values = Unity3dDefineByVersion.values();
		for(Unity3dDefineByVersion unity3dDefineByVersion : ArrayUtil.reverseArray(values))
		{
			if(unity3dDefineByVersion == UNKNOWN)
			{
				continue;
			}
			Matcher matcher = unity3dDefineByVersion.myVersionPattern.matcher(version);
			if(matcher.find())
			{
				return unity3dDefineByVersion;
			}
		}
		return UNKNOWN;
	}
}
