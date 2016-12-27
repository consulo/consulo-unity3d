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

/**
 * @author VISTALL
 * @since 19.01.2016
 */
public class UnityTestStatePostRequest
{
	public enum Type
	{
		TestStarted,
		TestFailed,
		TestIgnored,
		TestFinished,
		TestOutput,
		SuiteStarted,
		SuiteFinished,
		RunFinished
	}

	public String uuid;
	public String name;
	public Type type;
	public String message;
	public String messageType;
	public String stackTrace;
	public double time;

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("UnityTestStatePostRequest{");
		sb.append("uuid='").append(uuid).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", type=").append(type);
		sb.append(", message=").append(message);
		sb.append(", messageType=").append(messageType);
		sb.append(", stackTrace=").append(stackTrace);
		sb.append(", time=").append(time);
		sb.append('}');
		return sb.toString();
	}
}
