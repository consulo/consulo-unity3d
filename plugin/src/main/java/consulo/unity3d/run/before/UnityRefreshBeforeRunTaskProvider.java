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

package consulo.unity3d.run.before;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Key;
import com.intellij.util.TimeoutUtil;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.editor.UnityRefresh;
import consulo.unity3d.jsonApi.UnityPingPong;
import consulo.unity3d.run.test.Unity3dTestConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 21.01.2016
 */
public class UnityRefreshBeforeRunTaskProvider extends BeforeRunTaskProvider<UnityRefreshBeforeRunTask>
{
	private static final Key<UnityRefreshBeforeRunTask> ourKey = Key.create("unity.refresh.task");

	@Nonnull
	@Override
	public Key<UnityRefreshBeforeRunTask> getId()
	{
		return ourKey;
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return Unity3dIcons.Unity3d;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "UnityEditor refresh";
	}

	@Override
	public boolean isConfigurable()
	{
		return false;
	}

	@Nullable
	@Override
	public UnityRefreshBeforeRunTask createTask(RunConfiguration runConfiguration)
	{
		return runConfiguration instanceof Unity3dTestConfiguration ? new UnityRefreshBeforeRunTask(ourKey) : null;
	}

	@RequiredUIAccess
	@Nonnull
	@Override
	public AsyncResult<Void> configureTask(RunConfiguration runConfiguration, UnityRefreshBeforeRunTask task)
	{
		return AsyncResult.rejected();
	}

	@Nonnull
	@Override
	public AsyncResult<Void> executeTaskAsync(UIAccess uiAccess, DataContext context, RunConfiguration configuration, ExecutionEnvironment env, UnityRefreshBeforeRunTask task)
	{
		AsyncResult<Void> result = AsyncResult.undefined();

		uiAccess.give(() -> {
			FileDocumentManager.getInstance().saveAllDocuments();

			Task.Backgroundable.queue(env.getProject(), "Queue UnityEditor refresh", true, indicator -> {
				boolean[] receiveData = new boolean[1];

				UnityRefresh postObject = new UnityRefresh();

				UnityPingPong.Token<Boolean> accessToken = UnityPingPong.wantReply(postObject.uuid, o -> {
					if(o)
					{
						result.setDone();
					}
					else
					{
						result.setRejected();
					}
					receiveData[0] = o;
				});

				boolean request = UnityEditorCommunication.request(env.getProject(), postObject, true);
				if(!request)
				{
					new Notification("unity", ApplicationNamesInfo.getInstance().getProductName(), "UnityEditor is not responding", NotificationType.INFORMATION).notify(env.getProject());

					accessToken.finish(Boolean.FALSE);
					return;
				}

				while(!receiveData[0])
				{
					if(indicator.isCanceled())
					{
						accessToken.finish(Boolean.FALSE);
						break;
					}

					TimeoutUtil.sleep(500L);
				}
			});
		}).doWhenRejectedWithThrowable(result::rejectWithThrowable);

		return result;
	}
}
