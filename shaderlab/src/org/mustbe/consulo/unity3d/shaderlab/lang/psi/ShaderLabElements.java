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

import org.mustbe.consulo.unity3d.shaderlab.lang.ShaderLabLanguage;
import com.intellij.psi.tree.ElementTypeAsPsiFactory;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public interface ShaderLabElements
{
	IElementType SHADER_DEF = ShaderLabStubElements.SHADER_DEF;

	IElementType PROPERTY_LIST = new ElementTypeAsPsiFactory("PROPERTY_LIST", ShaderLabLanguage.INSTANCE, ShaderPropertyList.class);

	IElementType PROPERTY = new ElementTypeAsPsiFactory("PROPERTY", ShaderLabLanguage.INSTANCE, ShaderProperty.class);

	IElementType PROPERTY_TYPE = new ElementTypeAsPsiFactory("PROPERTY_TYPE", ShaderLabLanguage.INSTANCE, ShaderPropertyType.class);

	IElementType PROPERTY_VALUE = new ElementTypeAsPsiFactory("PROPERTY_VALUE", ShaderLabLanguage.INSTANCE, ShaderPropertyValue.class);

	IElementType PROPERTY_ATTRIBUTE = new ElementTypeAsPsiFactory("PROPERTY_ATTRIBUTE", ShaderLabLanguage.INSTANCE, ShaderPropertyAttribute.class);

	IElementType PROPERTY_OPTION = new ElementTypeAsPsiFactory("PROPERTY_OPTION", ShaderLabLanguage.INSTANCE, ShaderPropertyOption.class);

	IElementType REFERENCE = new ElementTypeAsPsiFactory("REFERENCE", ShaderLabLanguage.INSTANCE, ShaderReference.class);

	IElementType FALLBACK = new ElementTypeAsPsiFactory("FALLBACK", ShaderLabLanguage.INSTANCE, ShaderFallback.class);
}
