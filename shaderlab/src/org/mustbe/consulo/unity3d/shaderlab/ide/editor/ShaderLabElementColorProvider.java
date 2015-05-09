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

package org.mustbe.consulo.unity3d.shaderlab.ide.editor;

import java.awt.Color;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import org.mustbe.consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabColorRole;
import org.mustbe.consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabTokens;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderProperty;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderPropertyType;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderPropertyValue;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderSimpleValue;
import com.intellij.openapi.editor.ElementColorProvider;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabElementColorProvider implements ElementColorProvider
{
	@Nullable
	@Override
	public Color getColorFrom(@NotNull PsiElement element)
	{
		if(element instanceof ShaderPropertyValue)
		{
			if(!isColor(element.getParent()))
			{
				return null;
			}

			List<PsiElement> children = ((ShaderPropertyValue) element).getElements(ShaderLabTokens.INTEGER_LITERAL);
			if(children.size() < 3)
			{
				return null;
			}

			float[] floats = new float[4];
			floats[3] = 1;
			try
			{
				for(int i = 0; i < children.size(); i++)
				{
					PsiElement psiElement = children.get(i);
					float v = Float.parseFloat(psiElement.getText());
					if(v > 1 || v < 0)
					{
						return null;
					}
					floats[i] = v;
				}
			}
			catch(NumberFormatException e)
			{
				return null;
			}

			//noinspection UseJBColor
			return new Color(floats[0], floats[1], floats[2], floats[3]);
		}
		return null;
	}

	private static boolean isColor(PsiElement element)
	{
		if(element == null)
		{
			return false;
		}
		if(element instanceof ShaderProperty)
		{
			ShaderProperty property = (ShaderProperty) element;
			ShaderPropertyType type = property.getType();
			if(type == null)
			{
				return false;
			}
			ShaderLabPropertyType shaderLabPropertyType = ShaderLabPropertyType.find(type.getTargetText());
			return shaderLabPropertyType == ShaderLabPropertyType.Color;
		}
		else if(element instanceof ShaderSimpleValue)
		{
			ShaderLabRole key = ((ShaderSimpleValue) element).getRole();
			return key instanceof ShaderLabColorRole;
		}
		return false;
	}

	@Override
	public void setColorTo(@NotNull PsiElement element, @NotNull Color color)
	{

	}
}
