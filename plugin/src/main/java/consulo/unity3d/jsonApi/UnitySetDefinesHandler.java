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

import java.util.Arrays;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.buildInWebServer.api.JsonPostRequestHandler;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootMutableModuleExtension;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public class UnitySetDefinesHandler extends JsonPostRequestHandler<UnitySetDefines>
{
	public UnitySetDefinesHandler()
	{
		super("unitySetDefines", UnitySetDefines.class);
	}

	@NotNull
	@Override
	public JsonResponse handle(@NotNull UnitySetDefines unitySetDefines)
	{
		if(unitySetDefines.uuid != null)
		{
			UnityPingPong.replyReceived(unitySetDefines.uuid, unitySetDefines);
		}
		else
		{
			updateDefines(unitySetDefines);
		}
		return JsonResponse.asSuccess(null);
	}

	private static void updateDefines(@NotNull UnitySetDefines unitySetDefines)
	{
		ModifiableRootModel modifiableRootModel = ApplicationManager.getApplication().runReadAction(new Computable<ModifiableRootModel>()
		{
			@Override
			public ModifiableRootModel compute()
			{
				VirtualFile maybeProjectDir = LocalFileSystem.getInstance().findFileByPath(unitySetDefines.projectPath);
				if(maybeProjectDir == null)
				{
					return null;
				}

				Project project = null;
				Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
				for(Project openProject : openProjects)
				{
					if(maybeProjectDir.equals(openProject.getBaseDir()))
					{
						project = openProject;
						break;
					}
				}
				if(project == null)
				{
					return null;
				}

				Module rootModule = Unity3dModuleExtensionUtil.getRootModule(project);
				if(rootModule == null)
				{
					return null;
				}

				return ModuleRootManager.getInstance(rootModule).getModifiableModel();
			}
		});

		if(modifiableRootModel == null)
		{
			return;
		}

		new Notification("unity", ApplicationNamesInfo.getInstance().getProductName(), "Build Target Changed.<br>Defines updated.", NotificationType.WARNING).notify(modifiableRootModel.getProject());

		Unity3dRootMutableModuleExtension extension = modifiableRootModel.getExtension(Unity3dRootMutableModuleExtension.class);
		assert extension != null;

		extension.getVariables().clear();
		extension.getVariables().addAll(new TreeSet<>(Arrays.<String>asList(unitySetDefines.defines)));

		WriteCommandAction.runWriteCommandAction(modifiableRootModel.getProject(), modifiableRootModel::commit);
	}
}
