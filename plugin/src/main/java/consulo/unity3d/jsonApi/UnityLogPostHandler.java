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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.builtinWebServer.json.JsonPostRequestHandler;
import consulo.ui.ex.MessageCategory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 07-Jun-16
 */
@ExtensionImpl
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

	@Nonnull
	@Override
	public JsonResponse handle(@Nonnull final UnityLogPostHandlerRequest request)
	{
		ApplicationManager.getApplication().getMessageBus().syncPublisher(UnityLogHandler.TOPIC).handle(request);

		return JsonResponse.asSuccess(null);
	}
}
