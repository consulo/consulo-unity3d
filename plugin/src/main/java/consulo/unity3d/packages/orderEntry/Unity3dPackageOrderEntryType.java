/*
 * Copyright 2013-2018 consulo.io
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

package consulo.unity3d.packages.orderEntry;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryTypeProvider;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.InvalidDataException;
import org.jdom.Element;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 2018-09-19
 */
@ExtensionImpl
public class Unity3dPackageOrderEntryType implements CustomOrderEntryTypeProvider<Unity3dPackageOrderEntryModel>
{
	public static final String ID = "unity-package";

	@Nonnull
	public static Unity3dPackageOrderEntryType getInstance()
	{
		return EP.findExtensionOrFail(Application.get(), Unity3dPackageOrderEntryType.class);
	}

	@Nonnull
	@Override
	public String getId()
	{
		return ID;
	}

	@Nonnull
	@Override
	public Unity3dPackageOrderEntryModel loadOrderEntry(@Nonnull Element element, @Nonnull ModuleRootLayer moduleRootLayer) throws InvalidDataException
	{
		String name = element.getAttributeValue("name");
		String version = element.getAttributeValue("version");

		if(name.contains("@"))
		{
			List<String> values = StringUtil.split(name, "@");
			name = values.get(0);
			version = values.get(1);
		}
		String url = element.getAttributeValue("fileUrl");
		return new Unity3dPackageOrderEntryModel(name, version, url);
	}

	@Override
	public void storeOrderEntry(@Nonnull Element element, @Nonnull Unity3dPackageOrderEntryModel orderEntry)
	{
		element.setAttribute("name", orderEntry.getPresentableName());
		String version = orderEntry.getVersion();
		if(version != null)
		{
			element.setAttribute("version", version);
		}

		String fileUrl = orderEntry.getFileUrl();
		if(fileUrl != null)
		{
			element.setAttribute("fileUrl", fileUrl);
		}
	}
}
