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

package org.mustbe.consulo.unity3d;

import java.io.File;
import java.io.IOException;

import consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.LocalFileOperationsHandler;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ThrowableConsumer;

/**
 * @author VISTALL
 * @since 17.12.2015
 */
@Logger
public class Unity3dLocalFileSystemComponent implements ApplicationComponent
{
	private static final String META_SUFFIX = ".meta";

	private LocalFileOperationsHandler myHandler = new LocalFileOperationsHandler()
	{
		@Override
		public boolean delete(VirtualFile file) throws IOException
		{
			return handle(file, new ThrowableConsumer<VirtualFile, IOException>()
			{
				@Override
				public void consume(VirtualFile virtualFile) throws IOException
				{
					virtualFile.delete(null);
				}
			});
		}

		@Override
		public boolean move(VirtualFile file, final VirtualFile toDir) throws IOException
		{
			return handle(file, new ThrowableConsumer<VirtualFile, IOException>()
			{
				@Override
				public void consume(VirtualFile virtualFile) throws IOException
				{
					virtualFile.move(null, toDir);
				}
			});
		}

		@Nullable
		@Override
		public File copy(VirtualFile file, VirtualFile toDir, String copyName) throws IOException
		{
			return null;
		}

		@Override
		public boolean rename(VirtualFile file, final String newName) throws IOException
		{
			return handle(file, new ThrowableConsumer<VirtualFile, IOException>()
			{
				@Override
				public void consume(VirtualFile virtualFile) throws IOException
				{
					virtualFile.rename(null, newName + META_SUFFIX);
				}
			});
		}

		@Override
		public boolean createFile(VirtualFile dir, String name) throws IOException
		{
			return false;
		}

		@Override
		public boolean createDirectory(VirtualFile dir, String name) throws IOException
		{
			return false;
		}

		@Override
		public void afterDone(ThrowableConsumer<LocalFileOperationsHandler, IOException> invoker)
		{

		}

		private boolean handle(VirtualFile file, ThrowableConsumer<VirtualFile, IOException> consumer)
		{
			if(file.getFileType() == Unity3dMetaFileType.INSTANCE)
			{
				return false;
			}
			VirtualFile parent = file.getParent();
			if(parent == null)
			{
				return false;
			}
			VirtualFile metaFile = parent.findChild(file.getName() + META_SUFFIX);
			if(metaFile != null)
			{
				try
				{
					consumer.consume(metaFile);
				}
				catch(IOException e)
				{
					LOGGER.error(e);
				}
			}
			return false;
		}
	};

	@Override
	public void initComponent()
	{
		LocalFileSystem.getInstance().registerAuxiliaryFileOperationsHandler(myHandler);
	}

	@Override
	public void disposeComponent()
	{
		LocalFileSystem.getInstance().unregisterAuxiliaryFileOperationsHandler(myHandler);
	}

	@NotNull
	@Override
	public String getComponentName()
	{
		return "Unity3dLocalFileSystemComponent";
	}
}
