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

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.ide.impl.util.NewOrImportModuleUtil;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.mac.foundation.Foundation;
import com.intellij.ui.mac.foundation.ID;
import com.intellij.ui.mac.foundation.MacUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import consulo.awt.TargetAWT;
import consulo.builtInServer.impl.net.json.RequestFocusHttpRequestHandler;
import consulo.builtInServer.json.JsonPostRequestHandler;
import consulo.moduleImport.ModuleImportContext;
import consulo.moduleImport.ModuleImportProvider;
import consulo.moduleImport.ui.ModuleImportProcessor;
import consulo.platform.Platform;
import consulo.ui.UIAccess;
import consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.projectImport.Unity3dModuleImportProvider;
import consulo.util.concurrent.AsyncResult;
import consulo.util.lang.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

	@Nonnull
	@Override
	public JsonResponse handle(@Nonnull final UnityOpenFilePostHandlerRequest body)
	{
		String contentType = body.contentType;

		if(!ourSupportedContentTypes.contains(contentType))
		{
			return JsonResponse.asError("unsupported-content-type");
		}

		UIUtil.invokeLaterIfNeeded(() ->
		{
			UIAccess uiAccess = UIAccess.current();

			VirtualFile projectVirtualFile = LocalFileSystem.getInstance().findFileByPath(body.projectPath);
			if(projectVirtualFile != null)
			{
				Project targetProject = null;
				Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
				for(Project openProject : openProjects)
				{
					if(ProjectUtil.isSameProject(body.projectPath, openProject))
					{
						targetProject = openProject;
						break;
					}
				}

				if(targetProject == null)
				{
					if(!new File(projectVirtualFile.getPath(), Project.DIRECTORY_STORE_FOLDER).exists())
					{
						String sdkPath = Platform.current().os().isMac() ? body.editorPath : new File(body.editorPath).getParentFile().getParentFile().getPath();

						VirtualFile sdkFileHome = LocalFileSystem.getInstance().findFileByPath(sdkPath);
						if(sdkFileHome == null)
						{
							RequestFocusHttpRequestHandler.activateFrame(WindowManager.getInstance().findVisibleFrame());
							Messages.showErrorDialog("Unity path is not resolved: " + sdkPath, "Consulo");
							return;
						}

						Sdk targetSdk = null;
						List<Sdk> sdksOfType = SdkTable.getInstance().getSdksOfType(Unity3dBundleType.getInstance());
						for(Sdk sdk : sdksOfType)
						{
							VirtualFile homeDirectory = sdk.getHomeDirectory();
							if(sdkFileHome.equals(homeDirectory))
							{
								targetSdk = sdk;
								break;
							}
						}

						if(targetSdk == null)
						{
							targetSdk = SdkConfigurationUtil.createAndAddSDK(sdkPath, Unity3dBundleType.getInstance(), false);
						}

						if(targetSdk == null)
						{
							RequestFocusHttpRequestHandler.activateFrame(WindowManager.getInstance().findVisibleFrame());
							Messages.showErrorDialog("Unity SDK cant add by path: " + sdkPath, "Consulo");
							return;
						}


						Unity3dModuleImportProvider importProvider = new Unity3dModuleImportProvider(targetSdk, body);

						AsyncResult<Pair<ModuleImportContext, ModuleImportProvider<ModuleImportContext>>> result = AsyncResult.undefined();

						ModuleImportProcessor.showImportChooser(null, projectVirtualFile, Collections.singletonList(importProvider), result);

						result.doWhenDone(pair -> {
							ModuleImportContext context = pair.getFirst();

							ModuleImportProvider<ModuleImportContext> provider = pair.getSecond();

							AsyncResult<Project> importProjectAsync = NewOrImportModuleUtil.importProject(context, provider);

							importProjectAsync.doWhenDone((newProject) -> ProjectManager.getInstance().openProjectAsync(newProject, uiAccess).doWhenDone((project) -> postOpenFileRequest(project,
									uiAccess, body)));
						});
					}
					else
					{
						AsyncResult<Project> result = ProjectManager.getInstance().openProjectAsync(projectVirtualFile, uiAccess);

						result.doWhenDone((project) -> postOpenFileRequest(project, uiAccess, body));
					}
				}
				else
				{
					final Project project = targetProject;
					StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> postOpenFileRequest(project, uiAccess, body));
				}
			}
		});
		return JsonResponse.asSuccess(null);
	}

	private void postOpenFileRequest(@Nullable Project project, @Nonnull UIAccess uiAccess, @Nonnull UnityOpenFilePostHandlerRequest body)
	{
		uiAccess.give(() ->
		{
			activateFrame(project, body);

			openFile(project, body);
		});
	}

	private static void activateFrame(@Nullable Project openedProject, @Nonnull UnityOpenFilePostHandlerRequest body)
	{
		if(openedProject == null)
		{
			return;
		}

		IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(openedProject);
		if(ideFrame == null || !ideFrame.getWindow().isVisible())
		{
			return;
		}

		RequestFocusHttpRequestHandler.activateFrame(ideFrame);

		Platform.OperatingSystem os = Platform.current().os();
		if(os.isMac())
		{
			ID id = MacUtil.findWindowFromJavaWindow(TargetAWT.to(ideFrame.getWindow()));
			if(id != null)
			{
				Foundation.invoke(id, "makeKeyAndOrderFront:", ID.NIL);
			}
		}
		else if(os.isWindows())
		{
			Pointer windowPointer = Native.getWindowPointer(TargetAWT.to(ideFrame.getWindow()));
			User32.INSTANCE.SetForegroundWindow(new WinDef.HWND(windowPointer));
		}
	}

	public static void openFile(@Nullable Project openedProject, @Nonnull UnityOpenFilePostHandlerRequest body)
	{
		if(openedProject == null)
		{
			return;
		}

		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(body.filePath);
		if(fileByPath != null)
		{
			OpenFileDescriptor descriptor = new OpenFileDescriptor(openedProject, fileByPath, body.line - 1, -1);
			FileEditorManager.getInstance(openedProject).openTextEditor(descriptor, true);
		}
	}
}
