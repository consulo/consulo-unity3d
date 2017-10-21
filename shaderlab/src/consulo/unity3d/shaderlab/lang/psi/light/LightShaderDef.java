/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.shaderlab.lang.psi.light;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.unity3d.shaderlab.lang.psi.ShaderDef;
import consulo.unity3d.shaderlab.lang.psi.ShaderProperty;

/**
 * @author VISTALL
 * @since 21-Oct-17
 */
public class LightShaderDef extends LightElement implements ShaderDef
{
	private static final Key<Map<String, ShaderDef>> ourKey = Key.create("getDefaultShaders");

	@NotNull
	public static Map<String, ShaderDef> getDefaultShaders(@NotNull Project project)
	{
		Map<String, ShaderDef> data = project.getUserData(ourKey);
		if(data != null)
		{
			return data;
		}

		data = new HashMap<>();

		String[] keys = new String[]{
				"Vertex-Lit",
				"Diffuse",
				"Specular",
				"Bumped Diffuse",
				"Bumped Specular",
				"Parallax Diffuse",
				"Parallax Bumped Specular",
				"Decal",
				"Diffuse",
				"Detail",
				"Transparent Vertex-Lit",
				"Transparent Diffuse",
				"Transparent Specular",
				"Transparent Bumped Diffuse",
				"Transparent Bumped Specular",
				"Transparent Parallax Diffuse",
				"Transparent Parallax Specular",
				"Transparent Cutout Vertex-Lit",
				"Transparent Cutout Diffuse",
				"Transparent Cutout Specular",
				"Transparent Cutout Bumped Diffuse",
				"Transparent Cutout Bumped Specular",
				"Self-Illuminated Vertex-Lit",
				"Self-Illuminated Diffuse",
				"Self-Illuminated Specular",
				"Self-Illuminated Normal mapped Diffuse",
				"Self-Illuminated Normal mapped Specular",
				"Self-Illuminated Parallax Diffuse",
				"Self-Illuminated Parallax Specular",
				"Reflective Vertex-Lit",
				"Reflective Diffuse",
				"Reflective Specular",
				"Reflective Bumped Diffuse",
				"Reflective Bumped Specular",
				"Reflective Parallax Diffuse",
				"Reflective Parallax Specular",
				"Reflective Normal Mapped Unlit",
				"Reflective Normal mapped Vertex-lit",
		};

		for(String key : keys)
		{
			String quoted = StringUtil.QUOTER.fun(key);
			data.put(quoted, new LightShaderDef(project, quoted));
		}

		project.putUserData(ourKey, data);

		return data;
	}

	private final String myName;

	public LightShaderDef(@NotNull Project project, @NotNull String name)
	{
		super(PsiManager.getInstance(project), ShaderLabLanguage.INSTANCE);
		myName = name;
	}

	@Override
	public String toString()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		return myName;
	}

	@NotNull
	@Override
	public List<ShaderProperty> getProperties()
	{
		return Collections.emptyList();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}

	@Nullable
	@Override
	public ShaderLabRole getRole()
	{
		return ShaderLabRole.Shader;
	}
}
