package org.mustbe.consulo.unity3d.run.before;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.AccessToken;

/**
 * @author VISTALL
 * @since 21.01.2016
 */
public class UnityRefreshQueue
{
	private static Map<String, Runnable> ourCache = new ConcurrentHashMap<String, Runnable>();

	@NotNull
	public static AccessToken wantRefresh(final String uuid, Runnable runnable)
	{
		ourCache.put(uuid, runnable);

		return new AccessToken()
		{
			@Override
			public void finish()
			{
				ourCache.remove(uuid);
			}
		};
	}

	public static void refreshReceived(String uuid)
	{
		Runnable runnable = ourCache.remove(uuid);
		if(runnable == null)
		{
			return;
		}
		runnable.run();
	}
}
