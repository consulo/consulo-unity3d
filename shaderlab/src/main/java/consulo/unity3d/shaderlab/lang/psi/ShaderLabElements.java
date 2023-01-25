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

package consulo.unity3d.shaderlab.lang.psi;

import consulo.language.ast.ElementTypeAsPsiFactory;
import consulo.language.ast.IElementType;
import consulo.unity3d.shaderlab.lang.ShaderLabLanguage;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public interface ShaderLabElements
{
	IElementType SHADER_DEF = ShaderLabStubElements.SHADER_DEF;

	IElementType PROPERTY_LIST = new ElementTypeAsPsiFactory("PROPERTY_LIST", ShaderLabLanguage.INSTANCE, ShaderPropertyList::new);

	IElementType PROPERTY = new ElementTypeAsPsiFactory("PROPERTY", ShaderLabLanguage.INSTANCE, ShaderPropertyElement::new);

	IElementType PROPERTY_TYPE = new ElementTypeAsPsiFactory("PROPERTY_TYPE", ShaderLabLanguage.INSTANCE, ShaderPropertyTypeElement::new);

	IElementType PROPERTY_VALUE = new ElementTypeAsPsiFactory("PROPERTY_VALUE", ShaderLabLanguage.INSTANCE, ShaderPropertyValue::new);

	IElementType PROPERTY_ATTRIBUTE = new ElementTypeAsPsiFactory("PROPERTY_ATTRIBUTE", ShaderLabLanguage.INSTANCE, ShaderPropertyAttribute::new);

	IElementType PROPERTY_OPTION = new ElementTypeAsPsiFactory("PROPERTY_OPTION", ShaderLabLanguage.INSTANCE, ShaderPropertyOption::new);

	IElementType REFERENCE = new ElementTypeAsPsiFactory("REFERENCE", ShaderLabLanguage.INSTANCE, ShaderReference::new);

	IElementType TAG_LIST = new ElementTypeAsPsiFactory("TAG_LIST", ShaderLabLanguage.INSTANCE, ShaderTagList::new);

	IElementType TAG = new ElementTypeAsPsiFactory("TAG", ShaderLabLanguage.INSTANCE, ShaderTag::new);

	IElementType MATERIAL = new ElementTypeAsPsiFactory("MATERIAL", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand::new);

	IElementType BLEND = new ElementTypeAsPsiFactory("BLEND", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand::new);

	IElementType FOG = new ElementTypeAsPsiFactory("FOG", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand::new);

	IElementType PASS = new ElementTypeAsPsiFactory("PASS", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand::new);

	IElementType STENCIL = new ElementTypeAsPsiFactory("STENCIL", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand::new);

	IElementType SUB_SHADER = new ElementTypeAsPsiFactory("SUB_SHADER", ShaderLabLanguage.INSTANCE, ShaderCompositeCommand::new);

	IElementType SIMPLE_VALUE = new ElementTypeAsPsiFactory("SIMPLE_VALUE", ShaderLabLanguage.INSTANCE, ShaderSimpleValue::new);

	IElementType PAIR_VALUE = new ElementTypeAsPsiFactory("PAIR_VALUE", ShaderLabLanguage.INSTANCE, ShaderPairValue::new);

	IElementType SET_TEXTURE = new ElementTypeAsPsiFactory("SET_TEXTURE", ShaderLabLanguage.INSTANCE, ShaderSetTexture::new);

	IElementType CG_SHADER = new ElementTypeAsPsiFactory("CG_SHADER", ShaderLabLanguage.INSTANCE, ShaderCGScript::new);
}
