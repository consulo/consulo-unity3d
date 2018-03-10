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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public class UnityPingPong
{
	public static class Token<T>
	{
		private String myUUID;
		private Consumer<T> myConsumer;

		public Token(String uuid, Consumer<T> consumer)
		{
			myUUID = uuid;
			myConsumer = consumer;
		}

		public void finish(@Nullable T token)
		{
			ourCache.remove(myUUID);

			myConsumer.accept(token);
		}
	}

	private static Map<String, Consumer<Object>> ourCache = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	@Nonnull
	public static <T> Token<T> wantReply(final String uuid, @Nonnull Consumer<T> consumer)
	{
		Consumer<Object> old = ourCache.put(uuid, (Consumer<Object>) consumer);
		if(old != null)
		{
			throw new IllegalArgumentException("Duplicated request");
		}

		return new Token<T>(uuid, consumer);
	}

	public static void replyReceived(String uuid, Object o)
	{
		Consumer<Object> runnable = ourCache.remove(uuid);
		if(runnable == null)
		{
			return;
		}
		runnable.accept(o);
	}
}
