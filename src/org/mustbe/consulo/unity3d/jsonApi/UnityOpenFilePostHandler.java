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

package org.mustbe.consulo.unity3d.jsonApi;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.mustbe.buildInWebServer.api.JsonPostRequestHandler;
import org.mustbe.buildInWebServer.api.RequestFocusHttpRequestHandler;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;

/**
 * @author VISTALL
 * @since 14.11.2015
 */
public class UnityOpenFilePostHandler extends JsonPostRequestHandler<UnityOpenFilePostHandlerRequest>
{
	private static final Set<String> ourSupportedContentTypes = ContainerUtil.newHashSet("UnityEditor.MonoScript", "UnityEngine.Shader");

	public UnityOpenFilePostHandler()
	{
		super("unityOpenFile", UnityOpenFilePostHandlerRequest.class);
	}

	@NotNull
	@Override
	public JsonResponse handle(@NotNull final UnityOpenFilePostHandlerRequest body)
	{
		String contentType = body.contentType;
		if(!ourSupportedContentTypes.contains(contentType))
		{
			return JsonResponse.asError("unsupported-content-type");
		}

		UIUtil.invokeLaterIfNeeded(new Runnable()
		{
			@Override
			public void run()
			{
				VirtualFile projectVirtualFile = LocalFileSystem.getInstance().findFileByPath(body.projectPath);
				if(projectVirtualFile != null)
				{
					Project openedProject = null;
					Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
					for(Project openProject : openProjects)
					{
						if(projectVirtualFile.equals(openProject.getBaseDir()))
						{
							openedProject = openProject;
							break;
						}
					}

					if(openedProject != null)
					{
						IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(openedProject);
						RequestFocusHttpRequestHandler.activateFrame(ideFrame);

						VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(body.filePath);
						if(fileByPath != null)
						{
							OpenFileDescriptor descriptor = new OpenFileDescriptor(openedProject, fileByPath, body.line, -1);
							FileEditorManager.getInstance(openedProject).openTextEditor(descriptor, true);
						}
					}
				}
			}
		});
		return JsonResponse.asSuccess(null);
	}
}
