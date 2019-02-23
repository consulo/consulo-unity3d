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

import consulo.builtInServer.json.JsonPostRequestHandler;

/**
 * @author VISTALL
 * @since 21.01.2016
 */
public class UnityRefreshResponseHandler extends JsonPostRequestHandler<UnityRefreshResponse>
{
	public UnityRefreshResponseHandler()
	{
		super("unityRefreshResponse", UnityRefreshResponse.class);
	}

	@Nonnull
	@Override
	public JsonResponse handle(@Nonnull UnityRefreshResponse unityRefreshResponse)
	{
		UnityPingPong.replyReceived(unityRefreshResponse.uuid, Boolean.TRUE);

		return JsonResponse.asSuccess(null);
	}
}
