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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.ui.UIUtil;
import consulo.ui.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.editor.UnityRefresh;
import consulo.unity3d.jsonApi.UnityPingPong;
import consulo.unity3d.run.test.Unity3dTestConfiguration;

/**
 * @author VISTALL
 * @since 21.01.2016
 */
public class UnityRefreshBeforeRunTaskProvider extends BeforeRunTaskProvider<UnityRefreshBeforeRunTask>
{
	private static final Key<UnityRefreshBeforeRunTask> ourKey = Key.create("unity.refresh.task");

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

	@Nullable
	@Override
	public Image getTaskIcon(UnityRefreshBeforeRunTask task)
	{
		return Unity3dIcons.Unity3d;
	}

	@Override
	public String getName()
	{
		return "UnityEditor refresh";
	}

	@Override
	public String getDescription(UnityRefreshBeforeRunTask task)
	{
		return getName();
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

	@Override
	public boolean canExecuteTask(RunConfiguration configuration, UnityRefreshBeforeRunTask task)
	{
		return true;
	}

	@Override
	public boolean executeTask(DataContext context, RunConfiguration configuration, final ExecutionEnvironment env, UnityRefreshBeforeRunTask task)
	{
		final Semaphore done = new Semaphore();
		done.down();
		final Ref<Boolean> ref = Ref.create();

		UIUtil.invokeLaterIfNeeded(new Runnable()
		{
			@Override
			public void run()
			{
				FileDocumentManager.getInstance().saveAllDocuments();

				new Task.Backgroundable(env.getProject(), "Queue UnityEditor refresh", true)
				{
					private boolean myReceiveData;
					private UnityPingPong.Token<Boolean> myAccessToken;

					@Override
					public void run(@Nonnull ProgressIndicator indicator)
					{
						UnityRefresh postObject = new UnityRefresh();
						myAccessToken = UnityPingPong.wantReply(postObject.uuid, o -> {
							ref.set(o);
							myReceiveData = o;
						});

						boolean request = UnityEditorCommunication.request(env.getProject(), postObject, true);
						if(!request)
						{
							new Notification("unity", ApplicationNamesInfo.getInstance().getProductName(), "UnityEditor is not responding", NotificationType.INFORMATION).notify(env.getProject());

							myAccessToken.finish(Boolean.FALSE);
							done.up();
							return;
						}

						while(!myReceiveData)
						{
							if(indicator.isCanceled())
							{
								myAccessToken.finish(Boolean.FALSE);
								break;
							}

							TimeoutUtil.sleep(500L);
						}

						done.up();
					}
				}.queue();
			}
		});

		done.waitFor();
		return ref.get() == Boolean.TRUE;
	}
}
