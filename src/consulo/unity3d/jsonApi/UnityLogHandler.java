/*
 * Copyright 2013-2017 consulo.io
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

import org.intellij.lang.annotations.MagicConstant;
import com.intellij.util.messages.Topic;
import com.intellij.util.ui.MessageCategory;

/**
 * @author VISTALL
 * @since 05-Jun-17
 */
public interface UnityLogHandler
{
	Topic<UnityLogHandler> TOPIC = Topic.create("unityLogHandler", UnityLogHandler.class);

	void handle(@MagicConstant(valuesFromClass = MessageCategory.class) int messageCategory, String text, String stacktrace);
}