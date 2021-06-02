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

import com.intellij.util.ArrayUtil;
import org.intellij.lang.annotations.RegExp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @since 24.01.15
 */
public enum Unity3dDefineByVersion
{
	UNITY_2_6("2.6.\\d"),
	UNITY_2_6_1("2.6.1", UNITY_2_6),
	UNITY_3_0("3.0.\\d"),
	UNITY_3_0_0("3.0.0", UNITY_3_0),
	UNITY_3_1("3.1.\\d", UNITY_3_0),
	UNITY_3_2("3.2.\\d", UNITY_3_0),
	UNITY_3_3("3.3.\\d", UNITY_3_0),
	UNITY_3_4("3.4.\\d", UNITY_3_0),
	UNITY_3_5("3.5.\\d", UNITY_3_0),
	UNITY_4_0("4.0.\\d"),
	UNITY_4_0_1("4.0.1", UNITY_4_0),
	UNITY_4_1("4.1.\\d", UNITY_4_0),
	UNITY_4_2("4.2.\\d", UNITY_4_0),
	UNITY_4_3("4.3.\\d", UNITY_4_0),
	UNITY_4_5("4.5.\\d", UNITY_4_0),
	UNITY_4_6("4.6.\\d", UNITY_4_0),
	UNITY_4_7("4.7.\\d", UNITY_4_0),
	UNITY_5_0("5.0.\\d"),
	UNITY_5_1("5.1.\\d", UNITY_5_0),
	UNITY_5_2("5.2.\\d", UNITY_5_0),
	UNITY_5_3("5.3.\\d", UNITY_5_0),
	UNITY_5_4("5.4.\\d", UNITY_5_0),
	UNITY_5_5("5.5.\\d", UNITY_5_0),
	UNITY_5_6("5.6.\\d", UNITY_5_0),
	UNITY_2017_1("2017.1.\\d"),
	UNITY_2017_2("2017.2.\\d"),
	UNITY_2017_3("2017.3.\\d"),
	UNITY_2017_4("2017.4.\\d"),
	UNITY_2018_1("2018.1.\\d"),
	UNITY_2018_2("2018.2.\\d"),
	UNITY_2018_3("2018.3.\\d"),
	UNITY_2019_1("2019.1.\\d"),
	UNITY_2019_2("2019.2.\\d"),
	UNITY_2019_3("2019.3.\\d"),
	UNITY_2020_1("2020.1.\\d"),
	UNITY_2020_2("2020.2.\\d"),
	UNITY_2020_3("2020.3.\\d"),
	UNITY_2021_1("2021.1.\\d"),
	UNITY_2021_2("2021.2.\\d"),
	UNKNOWN("\\d.\\d.\\d");

	private final Pattern myVersionPattern;
	private Unity3dDefineByVersion[] myMajorVersions;

	Unity3dDefineByVersion(@RegExp String versionRegexp, Unity3dDefineByVersion... majorVersions)
	{
		myMajorVersions = majorVersions;
		myVersionPattern = Pattern.compile(versionRegexp);
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
