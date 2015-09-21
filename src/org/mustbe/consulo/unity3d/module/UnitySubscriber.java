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

package org.mustbe.consulo.unity3d.module;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.unity3d.Unity3dMetaFileType;
import com.intellij.ProjectTopics;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;

/**
 * @author VISTALL
 * @since 25.01.15
 */
public class UnitySubscriber extends AbstractProjectComponent
{
	public UnitySubscriber(Project project)
	{
		super(project);
	}

	@Override
	public void initComponent()
	{
		myProject.getMessageBus().connect().subscribe(ProjectTopics.MODULE_LAYERS,
				new UnitySyncModuleRootLayerListener());

		VirtualFileAdapter virtualFileAdapter = new VirtualFileAdapter()
		{
			@Override
			@RequiredReadAction
			public void beforeFileDeletion(@NotNull VirtualFileEvent event)
			{
				VirtualFile parent = event.getParent();
				if(parent == null)
				{
					return;
				}

				Module module = Unity3dModuleExtensionUtil.getRootModule(myProject);
				if(module == null)
				{
					return;
				}

				final VirtualFile metaFile = parent.findChild(event.getFile().getNameWithoutExtension() + "." +
						Unity3dMetaFileType.INSTANCE.getDefaultExtension());
				if(metaFile == null)
				{
					return;
				}

				WriteCommandAction.runWriteCommandAction(myProject, new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							metaFile.delete(null);
						}
						catch(IOException ignored)
						{
						}
					}
				});
			}
		};
		//VirtualFileManager.getInstance().addVirtualFileListener(virtualFileAdapter, myProject);
	}
}
