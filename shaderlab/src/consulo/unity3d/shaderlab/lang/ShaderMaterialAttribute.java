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

package consulo.unity3d.shaderlab.lang;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public enum ShaderMaterialAttribute
{
	Enum("UnityEditor.MaterialEnumDrawer"),
	KeywordEnum("UnityEditor.MaterialKeywordEnumDrawer"),
	PowerSlider("UnityEditor.MaterialPowerSliderDrawer"),
	Toggle("UnityEditor.MaterialToggleDrawer"),
	Space("UnityEditor.MaterialSpaceDecorator"),
	Header("UnityEditor.MaterialHeaderDecorator");

	private String myType;

	ShaderMaterialAttribute(String type)
	{
		myType = type;
	}

	public String getType()
	{
		return myType;
	}
}
