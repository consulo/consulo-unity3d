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

package consulo.unity3d.run.debugger;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.options.Configurable;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.xdebugger.impl.settings.XDebuggerSettingManagerImpl;
import com.intellij.xdebugger.settings.DebuggerSettingsCategory;
import com.intellij.xdebugger.settings.XDebuggerSettings;
import consulo.annotations.DeprecationInfo;
import consulo.options.SimpleConfigurableByProperties;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.Components;
import consulo.ui.Layouts;
import consulo.ui.RequiredUIAccess;
import consulo.ui.VerticalLayout;
import consulo.unity3d.Unity3dBundle;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
@Deprecated
@DeprecationInfo("Old attach settings, will be dropped with old attach")
public class Unity3dDebuggerSettings extends XDebuggerSettings<Unity3dDebuggerSettings>
{
	private static class OurConfigurable extends SimpleConfigurableByProperties implements Configurable
	{
		@Nls
		@Override
		public String getDisplayName()
		{
			return "Unity3D";
		}

		@RequiredUIAccess
		@NotNull
		@Override
		protected Component createLayout(PropertyBuilder propertyBuilder)
		{
			Unity3dDebuggerSettings settings = XDebuggerSettingManagerImpl.getInstanceImpl().getSettings(Unity3dDebuggerSettings.class);

			VerticalLayout layout = Layouts.vertical();
			CheckBox attachToSingleCheckBox = Components.checkBox(Unity3dBundle.message("attach.to.single.process.without.dialog.box"));
			layout.add(attachToSingleCheckBox);
			propertyBuilder.add(attachToSingleCheckBox, settings::isAttachToSingleProcessWithoutDialog,
					settings::setAttachToSingleProcessWithoutDialog);
			return layout;
		}
	}

	@Attribute("attach-to-single-process-without-dialog")
	public boolean myAttachToSingleProcessWithoutDialog;

	public Unity3dDebuggerSettings()
	{
		super("unity3d");
	}

	public void setAttachToSingleProcessWithoutDialog(boolean attachToSingleProcessWithoutDialog)
	{
		myAttachToSingleProcessWithoutDialog = attachToSingleProcessWithoutDialog;
	}

	public boolean isAttachToSingleProcessWithoutDialog()
	{
		return myAttachToSingleProcessWithoutDialog;
	}

	@NotNull
	@Override
	public Collection<? extends Configurable> createConfigurables(@NotNull DebuggerSettingsCategory category)
	{
		if(category == DebuggerSettingsCategory.GENERAL)
		{
			return Collections.singletonList(new OurConfigurable());
		}
		return super.createConfigurables(category);
	}

	@Nullable
	@Override
	public Unity3dDebuggerSettings getState()
	{
		return this;
	}

	@Override
	public void loadState(Unity3dDebuggerSettings state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
