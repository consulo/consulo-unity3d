package consulo.unity3d.jsonApi;

import org.jetbrains.annotations.NotNull;
import consulo.buildInWebServer.api.JsonPostRequestHandler;

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

	@NotNull
	@Override
	public JsonResponse handle(@NotNull UnityRefreshResponse unityRefreshResponse)
	{
		UnityPingPong.replyReceived(unityRefreshResponse.uuid, Boolean.TRUE);

		return JsonResponse.asSuccess(null);
	}
}
