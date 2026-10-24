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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.application.progress.Task;
import consulo.dataContext.DataContext;
import consulo.document.FileDocumentManager;
import consulo.execution.BeforeRunFragment;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.localize.LocalizeValue;
import consulo.project.ui.notification.NotificationService;
import consulo.ui.UIAccess;
import consulo.ui.image.Image;
import consulo.unity3d.base.UnityNotificationGroup;
import consulo.unity3d.base.jsonApi.UnityPingPong;
import consulo.unity3d.editor.UnityEditorCommunication;
import consulo.unity3d.editor.UnityRefresh;
import consulo.unity3d.icon.Unity3dIconGroup;
import consulo.unity3d.run.test.Unity3dTestConfiguration;
import consulo.util.lang.TimeoutUtil;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * Refreshes the Unity Editor before a Unity test configuration runs.
 *
 * @author VISTALL
 * @since 21.01.2016
 */
@ExtensionImpl
public class UnityRefreshBeforeRunFragment implements BeforeRunFragment<UnityRefreshBeforeRunTask> {
    public static final String ID = "unity.refresh.task";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public LocalizeValue getName() {
        return LocalizeValue.localizeTODO("UnityEditor refresh");
    }

    @Override
    public @Nullable Image getIcon(RunConfiguration configuration) {
        return Unity3dIconGroup.unity3d();
    }

    @Override
    public Class<UnityRefreshBeforeRunTask> getStateClass() {
        return UnityRefreshBeforeRunTask.class;
    }

    @Override
    public @Nullable UnityRefreshBeforeRunTask createTask(RunConfiguration configuration) {
        return configuration instanceof Unity3dTestConfiguration ? new UnityRefreshBeforeRunTask() : null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public CompletableFuture<?> executeAsync(UIAccess uiAccess,
                                             DataContext context,
                                             RunConfiguration configuration,
                                             ExecutionEnvironment env,
                                             UnityRefreshBeforeRunTask task) {
        CompletableFuture<Void> result = new CompletableFuture<>();

        uiAccess.give(() -> {
            FileDocumentManager.getInstance().saveAllDocuments();

            Task.Backgroundable.queue(env.getProject(), "Queue UnityEditor refresh", true, indicator -> {
                boolean[] receiveData = new boolean[1];

                UnityRefresh postObject = new UnityRefresh();

                UnityPingPong.Token<Boolean> accessToken = UnityPingPong.wantReply(postObject.uuid, o -> {
                    if (o) {
                        result.complete(null);
                    }
                    else {
                        result.completeExceptionally(new CancellationException());
                    }
                    receiveData[0] = o;
                });

                boolean request = UnityEditorCommunication.request(env.getProject(), postObject, true);
                if (!request) {
                    NotificationService.getInstance().newInfo(UnityNotificationGroup.INSTANCE)
                        .title(Application.get().getName())
                        .content(LocalizeValue.localizeTODO("UnityEditor is not responding"))
                        .notify(env.getProject());

                    accessToken.finish(Boolean.FALSE);
                    return;
                }

                while (!receiveData[0]) {
                    if (indicator.isCanceled()) {
                        accessToken.finish(Boolean.FALSE);
                        break;
                    }

                    TimeoutUtil.sleep(500L);
                }
            });
        });

        return result;
    }
}
