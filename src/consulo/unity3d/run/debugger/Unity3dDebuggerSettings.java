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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.options.Configurable;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.xdebugger.settings.DebuggerSettingsCategory;
import com.intellij.xdebugger.settings.XDebuggerSettings;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
public class Unity3dDebuggerSettings extends XDebuggerSettings<Unity3dDebuggerSettings>
{
	@Attribute("attach-to-single-process-without-dialog")
	public boolean myAttachToSingleProcessWithoutDialog;

	public Unity3dDebuggerSettings()
	{
		super("unity3d");
	}

	@NotNull
	@Override
	public Collection<? extends Configurable> createConfigurables(@NotNull DebuggerSettingsCategory category)
	{
		if(category == DebuggerSettingsCategory.GENERAL)
		{
			return Collections.singletonList(new Unity3dDebuggerConfigurable());
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
