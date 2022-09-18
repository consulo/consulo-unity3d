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

import consulo.ide.setting.module.CustomOrderEntryTypeEditor;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.ui.ex.ColoredTextContainer;
import consulo.unity3d.Unity3dIcons;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2018-09-19
 */
public class Unity3dPackageOrderEntryTypeEditor implements CustomOrderEntryTypeEditor<Unity3dPackageOrderEntryModel>
{
	@Nonnull
	@Override
	public Consumer<ColoredTextContainer> getRender(@Nonnull CustomOrderEntry<Unity3dPackageOrderEntryModel> orderEntry)
	{
		return render ->
		{
			render.append(orderEntry.getPresentableName());
			render.setIcon(Unity3dIcons.Unity3d);
		};
	}

	@Nonnull
	@Override
	public String getOrderTypeId()
	{
		return Unity3dPackageOrderEntryType.ID;
	}
}

