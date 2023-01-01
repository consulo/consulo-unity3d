/*
 * Copyright 2013-2022 consulo.io
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

package consulo.unity3d.uxml;

import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.xml.ide.highlighter.XmlFileType;
import consulo.xml.ide.highlighter.XmlLikeFileType;
import consulo.xml.lang.xml.XMLLanguage;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18-Sep-22
 */
public class UXMLFileType extends XmlLikeFileType
{
	public static final UXMLFileType INSTANCE = new UXMLFileType();

	private UXMLFileType()
	{
		super(XMLLanguage.INSTANCE);
	}

	@Nonnull
	@Override
	public String getDefaultExtension()
	{
		return "uxml";
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "UNITY_UXML";
	}

	@Nonnull
	@Override
	public LocalizeValue getDescription()
	{
		return LocalizeValue.localizeTODO("Unity Extensible Markup Language");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return XmlFileType.INSTANCE.getIcon();
	}
}
