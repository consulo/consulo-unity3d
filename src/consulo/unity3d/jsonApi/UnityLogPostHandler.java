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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.problems.Problem;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.util.ObjectUtil;
import com.intellij.util.ui.MessageCategory;
import com.intellij.util.ui.UIUtil;
import consulo.buildInWebServer.api.JsonPostRequestHandler;
import consulo.dotnet.compiler.DotNetCompilerMessage;
import consulo.unity3d.UnityConsoleService;

/**
 * @author VISTALL
 * @since 07-Jun-16
 */
public class UnityLogPostHandler extends JsonPostRequestHandler<UnityLogPostHandlerRequest>
{
	private static Map<String, Integer> ourTypeMap = new HashMap<>();

	static
	{
		ourTypeMap.put("Error", MessageCategory.ERROR);
		ourTypeMap.put("Assert", MessageCategory.ERROR);
		ourTypeMap.put("Warning", MessageCategory.WARNING);
		ourTypeMap.put("Log", MessageCategory.INFORMATION);
		ourTypeMap.put("Exception", MessageCategory.ERROR);
	}

	public UnityLogPostHandler()
	{
		super("unityLog", UnityLogPostHandlerRequest.class);
	}

	@NotNull
	@Override
	public JsonResponse handle(@NotNull final UnityLogPostHandlerRequest request)
	{
		int value = ObjectUtil.notNull(ourTypeMap.get(request.type), MessageCategory.INFORMATION);

		//noinspection MagicConstant
		ApplicationManager.getApplication().getMessageBus().syncPublisher(UnityLogHandler.TOPIC).handle(value, request.condition, request.stackTrace);

		UIUtil.invokeLaterIfNeeded(() -> UnityConsoleService.byPath(request.projectPath, (project, panel) ->
		{
			DotNetCompilerMessage message = UnityLogParser.extractFileInfo(project, request.condition);

			if(message != null)
			{
				VirtualFile fileByUrl = message.getFileUrl() == null ? null : VirtualFileManager.getInstance().findFileByUrl(message.getFileUrl());
				if(fileByUrl != null && value == MessageCategory.ERROR)
				{
					Problem problem = WolfTheProblemSolver.getInstance(project).convertToProblem(fileByUrl, message.getLine(), message.getColumn(), new String[]{message.getMessage()});
					if(problem != null)
					{
						WolfTheProblemSolver.getInstance(project).reportProblems(fileByUrl, Collections.singletonList(problem));
					}
				}

				panel.addMessage(value, new String[]{message.getMessage()}, fileByUrl, message.getLine() - 1, message.getColumn(), null);
			}
			else
			{
				panel.addMessage(value, new String[]{
						request.condition,
						request.stackTrace
				}, null, -1, -1, null);
			}
		}));

		return JsonResponse.asSuccess(null);
	}
}
