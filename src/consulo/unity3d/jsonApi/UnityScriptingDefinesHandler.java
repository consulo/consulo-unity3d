/*
 * Copyright 2013-2016 must-be.org
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

package consulo.unity3d.jsonApi;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import consulo.unity3d.UnityConsoleService;
import org.jetbrains.annotations.NotNull;
import org.mustbe.buildInWebServer.api.JsonPostRequestHandler;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtensionUtil;
import org.mustbe.consulo.unity3d.module.Unity3dRootModuleExtension;

import java.util.List;

/**
 * @author electrowolff
 * @since 08-Aug-18
 */
public class UnityScriptingDefinesHandler extends JsonPostRequestHandler<UnityScriptingDefinesHandlerRequest>
{
	public UnityScriptingDefinesHandler()
	{
		super("unityScriptingDefines", UnityScriptingDefinesHandlerRequest.class);
	}

	@NotNull
	@Override
	public JsonResponse handle(@NotNull final UnityScriptingDefinesHandlerRequest request)
	{
		final String[] defines = request.getDefines();

		Notifications.Bus.notify(new Notification(
				"Unity",
				"UnityScriptingDefinesHandler",
				"Received " + defines.length + " scripting defines.",
				NotificationType.INFORMATION));

		UIUtil.invokeLaterIfNeeded(new Runnable()
		{
			@Override
			public void run()
			{
				UnityConsoleService consoleService = UnityConsoleService.findByProjectPath(request.projectPath);
				if(consoleService != null)
				{
					Project project = consoleService.getProject();
					Unity3dRootModuleExtension extension = Unity3dModuleExtensionUtil.getRootModuleExtension(project);

					List<String> variables = extension.getVariables();

					variables.clear();

					for (String define : defines)
					{
						variables.add(define);
					}
				}
			}
		});

		return JsonResponse.asSuccess(null);
	}
}
