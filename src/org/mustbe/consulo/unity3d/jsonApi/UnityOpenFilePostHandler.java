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

import java.awt.Frame;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.buildInWebServer.api.JsonPostRequestHandler;
import org.mustbe.buildInWebServer.api.RequestFocusHttpRequestHandler;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.BitUtil;
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

					if(openedProject == null)
					{
						VirtualFile projectDir = projectVirtualFile.findChild(Project.DIRECTORY_STORE_FOLDER);
						if(projectDir == null)
						{
							JFrame visibleFrame = WindowManager.getInstance().findVisibleFrame();
							activateFrame(visibleFrame);
							Messages.showErrorDialog("Project with path: " + body.projectPath + " is not imported", "Consulo");
							return;
						}

						try
						{
							openedProject = ProjectManager.getInstance().loadAndOpenProject(projectVirtualFile.getPath());
						}
						catch(Exception e)
						{
							Messages.showErrorDialog("Fail to open project by path: " + projectVirtualFile.getPath(), "Consulo");
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

	public static boolean activateFrame(@Nullable final JFrame frame)
	{
		if(frame != null)
		{
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					int extendedState = frame.getExtendedState();
					if(BitUtil.isSet(extendedState, Frame.ICONIFIED))
					{
						extendedState = BitUtil.set(extendedState, Frame.ICONIFIED, false);
						frame.setExtendedState(extendedState);
					}

					// fixme [vistall] dirty hack - show frame on top
					frame.setAlwaysOnTop(true);
					frame.setAlwaysOnTop(false);
					frame.requestFocus();
				}
			};
			//noinspection SSBasedInspection
			SwingUtilities.invokeLater(runnable);
			return true;
		}
		return false;
	}
}
