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

package consulo.unity3d.shaderlab.ide.editor;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.ElementColorProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFileFactory;
import consulo.project.Project;
import consulo.ui.color.ColorValue;
import consulo.ui.color.RGBColor;
import consulo.unity3d.shaderlab.lang.ShaderLabFileType;
import consulo.unity3d.shaderlab.lang.ShaderLabPropertyType;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabColorRole;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.unity3d.shaderlab.lang.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
@ExtensionImpl
public class ShaderLabElementColorProvider implements ElementColorProvider
{
	@RequiredReadAction
	@Nullable
	@Override
	public ColorValue getColorFrom(@Nonnull PsiElement element)
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

			return RGBColor.fromFloatValues(floats[0], floats[1], floats[2], floats[3]);
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

	@RequiredWriteAction
	@Override
	public void setColorTo(@Nonnull PsiElement element, @Nonnull ColorValue color)
	{
		float[] colorComponents = color.toRGB().getFloatValues();

		StringBuilder builder = new StringBuilder();
		builder.append("(");
		for(int i = 0; i < (colorComponents[3] == 1 ? colorComponents.length - 1 : colorComponents.length); i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}

			builder.append(colorComponents[i]);
		}
		builder.append(")");

		ShaderPropertyValue value = createValue(element.getProject(), builder.toString());

		element.replace(value);
	}

	@Nonnull
	@RequiredReadAction
	private static ShaderPropertyValue createValue(Project project, String text)
	{
		String full = "Shader \"dummy\" { Properties { _dummy(\"dummy\", Color)  = " + text + " } }";
		ShaderLabFile psiFile = (ShaderLabFile) PsiFileFactory.getInstance(project).createFileFromText(full, ShaderLabFileType.INSTANCE, full);

		return psiFile.getProperties().get(0).getValue();
	}
}
