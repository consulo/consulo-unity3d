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

package consulo.unity3d;

import consulo.annotation.DeprecationInfo;
import consulo.ui.image.Image;
import consulo.unity3d.icon.Unity3dIconGroup;

// Generated Consulo DevKit plugin
@Deprecated(forRemoval = true)
@DeprecationInfo("Use Unit3dIconGroup")
public interface Unity3dIcons
{
	Image EditorLayer = Unity3dIconGroup.editorlayer();
	Image AssetsLayer = Unity3dIconGroup.assetslayer();
	Image PluginsLayer = Unity3dIconGroup.pluginslayer();
	Image GizmosLayer = Unity3dIconGroup.gizmoslayer();
	Image EventMethod = Unity3dIconGroup.lightningbolt();
	Image Unity3d = Unity3dIconGroup.unity3d();
	Image Unity3dLineMarker = Unity3dIconGroup.unity3dlinemarker();
}