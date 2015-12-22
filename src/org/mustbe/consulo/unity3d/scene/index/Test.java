/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d.scene.index;

import gnu.trove.THashMap;

import java.util.Map;

/**
 * @author VISTALL
 * @since 22.12.2015
 */
public class Test
{

	private static String ourString;

	public static void main(String[] args)
	{
		String inputData = "guid: 0000000010000000e000010000003007" +
				"sda das das dasd sa das {guid: 0000000010000000e000010000003006}" +
				"d" +
				"d" +
				"d" +
				"d" +
				"guid: 0000000010000000e000020000003005" +
				"dsadasdas guid: 41414141";


		Map<String, Void> map = new THashMap<String, Void>();

		CharSequence contentAsText = inputData;
		for(int i = 0; i < contentAsText.length(); i++)
		{
			if(isGuid(i, contentAsText))
			{
				i += 6;

				String cut = cut(i, i + 32, contentAsText);
				if(cut != null)
				{
					i += 32;
				}

				System.out.println("aaa " + cut);
			}
		}

	}

	private static String cut(int i, int max, CharSequence charSequence)
	{
		if(max >= charSequence.length())
		{
			return null;
		}
		return charSequence.subSequence(i, max).toString();
	}

	private static boolean isGuid(int i, CharSequence contentAsText)
	{
		return have(i, contentAsText, 'g') &&
				have(++i, contentAsText, 'u') &&
				have(++i, contentAsText, 'i') &&
				have(++i, contentAsText, 'd') &&
				have(++i, contentAsText, ':') &&
				have(++i, contentAsText, ' ');
	}

	private static boolean have(int i, CharSequence charSequence, char c)
	{
		if(i >= charSequence.length())
		{
			return false;
		}
		char c1 = charSequence.charAt(i);
		return c1 == c;
	}
}
