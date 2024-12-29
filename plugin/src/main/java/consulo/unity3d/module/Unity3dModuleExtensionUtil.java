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

package consulo.unity3d.module;

import consulo.annotation.access.RequiredReadAction;
import consulo.module.Module;
import consulo.project.Project;
import consulo.unity3d.Unity3dProjectService;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 29.03.2015
 */
public class Unity3dModuleExtensionUtil
{
	@Nullable
	@RequiredReadAction
	public static Module getRootModule(@Nonnull Project project)
	{
		return Unity3dProjectService.getInstance(project).getRootModule();
	}

	@Nullable
	@RequiredReadAction
	public static Unity3dRootModuleExtension getRootModuleExtension(@Nonnull Project project)
	{
		return Unity3dProjectService.getInstance(project).getRootModuleExtension();
	}
}
