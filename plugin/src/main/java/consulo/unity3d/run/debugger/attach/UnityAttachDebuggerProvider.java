/*
 * Copyright 2013-2021 consulo.io
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

package consulo.unity3d.run.debugger.attach;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.debug.attach.LocalAttachHost;
import consulo.execution.debug.attach.XAttachDebugger;
import consulo.execution.debug.attach.XAttachDebuggerProvider;
import consulo.execution.debug.attach.XAttachHost;
import consulo.process.ProcessInfo;
import consulo.project.Project;
import consulo.unity3d.run.debugger.UnityDebugProcessInfo;
import consulo.unity3d.run.debugger.UnityProcessDialog;
import consulo.util.dataholder.UserDataHolder;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 09/01/2021
 */
@ExtensionImpl
public class UnityAttachDebuggerProvider implements XAttachDebuggerProvider
{
	@Override
	public boolean isAttachHostApplicable(@Nonnull XAttachHost attachHost)
	{
		return attachHost == LocalAttachHost.INSTANCE;
	}

	@Nonnull
	@Override
	public List<XAttachDebugger> getAvailableDebuggers(@Nonnull Project project, @Nonnull XAttachHost hostInfo, @Nonnull ProcessInfo process, @Nonnull UserDataHolder contextHolder)
	{
		UnityDebugProcessInfo unityProcess = UnityProcessDialog.tryParseIfUnityProcess(process);
		if(unityProcess != null)
		{
			return List.of(UnityAttachDebugger.INSTANCE);
		}
		return List.of();
	}
}
