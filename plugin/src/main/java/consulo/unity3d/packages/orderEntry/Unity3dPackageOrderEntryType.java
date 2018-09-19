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

import javax.annotation.Nonnull;

import org.jdom.Element;
import com.intellij.openapi.util.InvalidDataException;
import consulo.roots.ModuleRootLayer;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.roots.orderEntry.OrderEntryType;

/**
 * @author VISTALL
 * @since 2018-09-19
 */
public class Unity3dPackageOrderEntryType implements OrderEntryType<Unity3dPackageOrderEntry>
{
	@Nonnull
	public static Unity3dPackageOrderEntryType getInstance()
	{
		return EP_NAME.findExtension(Unity3dPackageOrderEntryType.class);
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "unity-package";
	}

	@Nonnull
	@Override
	public Unity3dPackageOrderEntry loadOrderEntry(@Nonnull Element element, @Nonnull ModuleRootLayer moduleRootLayer) throws InvalidDataException
	{
		String name = element.getAttributeValue("name");
		return new Unity3dPackageOrderEntry((ModuleRootLayerImpl) moduleRootLayer, name);
	}

	@Override
	public void storeOrderEntry(@Nonnull Element element, @Nonnull Unity3dPackageOrderEntry dotNetLibraryOrderEntry)
	{
		element.setAttribute("name", dotNetLibraryOrderEntry.getPresentableName());
	}
}
