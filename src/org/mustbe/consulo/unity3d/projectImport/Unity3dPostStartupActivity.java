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

package org.mustbe.consulo.unity3d.projectImport;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Getter;

/**
 * @author VISTALL
 * @since 24.08.2015
 */
public class Unity3dPostStartupActivity implements StartupActivity
{
	@Override
	public void runActivity(final Project project)
	{
		if(ApplicationManager.getApplication().isUnitTestMode())
		{
			return;
		}
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				Getter<Sdk> getter = project.getUserData(Unity3dProjectUtil.NEWLY_IMPORTED_PROJECT_SDK);
				if(getter != null)
				{
					project.putUserData(Unity3dProjectUtil.NEWLY_IMPORTED_PROJECT_SDK, null);

					Unity3dProjectUtil.syncProject(project, getter.get());
				}
			}
		};

		if(project.isInitialized())
		{
			task.run();
		}
		else
		{
			StartupManager.getInstance(project).registerPostStartupActivity(task);
		}
	}
}
