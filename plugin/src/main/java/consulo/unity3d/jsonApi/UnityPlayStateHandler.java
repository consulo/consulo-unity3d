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

package consulo.unity3d.jsonApi;

import javax.annotation.Nonnull;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import consulo.builtInServer.json.JsonPostRequestHandler;
import consulo.unity3d.Unity3dConsoleToolWindowService;
import consulo.unity3d.util.Unity3dProjectUtil;

/**
 * @author VISTALL
 * @since 07-Jun-16
 */
public class UnityPlayStateHandler extends JsonPostRequestHandler<UnityPlayStateHandlerRequest>
{
	public UnityPlayStateHandler()
	{
		super("unityPlayState", UnityPlayStateHandlerRequest.class);
	}

	@Nonnull
	@Override
	public JsonResponse handle(@Nonnull final UnityPlayStateHandlerRequest request)
	{
		if(request.isPlaying)
		{
			UIUtil.invokeLaterIfNeeded(() ->
			{
				Project project = Unity3dProjectUtil.findProjectByPath(request.projectPath);
				if(project == null)
				{
					return;
				}
				Unity3dConsoleToolWindowService.getInstance(project).onPlay();
			});
		}
		return JsonResponse.asSuccess(null);
	}
}
