/*
 * Copyright 2013-2015 must-be.org
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
 * @since 14.11.2015
 */
public class UnityOpenFilePostHandlerRequest
{
	public String projectPath;
	public String contentType;
	public String filePath;
	// on MacOS it will be '/Applications/Unity/Unity.app' but on windows will be 'Unity/Editor/Unity.exe'
	public String editorPath;
	public int line;
}
