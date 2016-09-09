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

package consulo.unity3d.shaderlab.lang.psi;

import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import com.intellij.psi.tree.IElementType;
import consulo.psi.tree.ElementTypeAsPsiFactory;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public interface ShaderLabElements
{
	IElementType SHADER_DEF = ShaderLabStubElements.SHADER_DEF;

	IElementType PROPERTY_LIST = new ElementTypeAsPsiFactory("PROPERTY_LIST", ShaderLabLanguage.INSTANCE, ShaderPropertyList.class);

	IElementType PROPERTY = new ElementTypeAsPsiFactory("PROPERTY", ShaderLabLanguage.INSTANCE, ShaderPropertyElement.class);

	IElementType PROPERTY_TYPE = new ElementTypeAsPsiFactory("PROPERTY_TYPE", ShaderLabLanguage.INSTANCE, ShaderPropertyTypeElement.class);

	IElementType PROPERTY_VALUE = new ElementTypeAsPsiFactory("PROPERTY_VALUE", ShaderLabLanguage.INSTANCE, ShaderPropertyValue.class);

	IElementType PROPERTY_ATTRIBUTE = new ElementTypeAsPsiFactory("PROPERTY_ATTRIBUTE", ShaderLabLanguage.INSTANCE, ShaderPropertyAttribute.class);

	IElementType PROPERTY_OPTION = new ElementTypeAsPsiFactory("PROPERTY_OPTION", ShaderLabLanguage.INSTANCE, ShaderPropertyOption.class);

	IElementType REFERENCE = new ElementTypeAsPsiFactory("REFERENCE", ShaderLabLanguage.INSTANCE, ShaderReference.class);

	IElementType TAG_LIST = new ElementTypeAsPsiFactory("TAG_LIST", ShaderLabLanguage.INSTANCE, ShaderTagList.class);

	IElementType TAG = new ElementTypeAsPsiFactory("TAG", ShaderLabLanguage.INSTANCE, ShaderTag.class);

	IElementType MATERIAL = new ElementTypeAsPsiFactory("MATERIAL", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand.class);

	IElementType FOG = new ElementTypeAsPsiFactory("FOG", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand.class);

	IElementType PASS = new ElementTypeAsPsiFactory("PASS", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand.class);

	IElementType SUB_SHADER = new ElementTypeAsPsiFactory("SUB_SHADER", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand.class);

	IElementType SIMPLE_VALUE = new ElementTypeAsPsiFactory("SIMPLE_VALUE", ShaderLabLanguage.INSTANCE, ShaderSimpleValue.class);

	IElementType PAIR_VALUE = new ElementTypeAsPsiFactory("PAIR_VALUE", ShaderLabLanguage.INSTANCE, ShaderPairValue.class);

	IElementType SET_TEXTURE = new ElementTypeAsPsiFactory("SET_TEXTURE", ShaderLabLanguage.INSTANCE, ShaderSetTexture.class);

	IElementType CG_SHADER = new ElementTypeAsPsiFactory("CG_SHADER", ShaderLabLanguage.INSTANCE, ShaderCGScript.class);
}
