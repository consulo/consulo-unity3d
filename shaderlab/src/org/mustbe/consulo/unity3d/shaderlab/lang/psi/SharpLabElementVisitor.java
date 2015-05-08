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

package org.mustbe.consulo.unity3d.shaderlab.lang.psi;

import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class SharpLabElementVisitor extends PsiElementVisitor
{
	public void visitShaderDef(ShaderDef shaderDef)
	{
		visitElement(shaderDef);
	}

	public void visitProperty(ShaderProperty p)
	{
		visitElement(p);
	}

	public void visitPropertyList(ShaderPropertyList list)
	{
		visitElement(list);
	}

	public void visitPropertyType(ShaderPropertyType type)
	{
		visitElement(type);
	}

	public void visitPropertyValue(ShaderPropertyValue value)
	{
		visitElement(value);
	}

	public void visitPropertyAttribute(ShaderPropertyAttribute attribute)
	{
		visitElement(attribute);
	}

	public void visitReference(ShaderReference reference)
	{
		visitElement(reference);
	}

	public void visitPropertyOption(ShaderPropertyOption option)
	{
		visitElement(option);
	}

	public void visitTags(ShaderTagList tags)
	{
		visitElement(tags);
	}

	public void visitTag(ShaderTag tag)
	{
		visitElement(tag);
	}

	public void visitPass(ShaderPass shaderPass)
	{
		visitElement(shaderPass);
	}

	public void visitSubShader(ShaderSubShader shaderSubShader)
	{
		visitElement(shaderSubShader);
	}

	public void visitSimpleValue(ShaderSimpleValue value)
	{
		visitElement(value);
	}

	public void visitSetTexture(ShaderSetTexture texture)
	{
		visitElement(texture);
	}
}
