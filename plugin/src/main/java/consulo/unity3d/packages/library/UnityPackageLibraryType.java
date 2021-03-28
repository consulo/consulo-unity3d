/*
 * Copyright 2013-2021 consulo.io
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

package consulo.unity3d.packages.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.*;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.ui.image.Image;
import consulo.unity3d.icon.Unity3dIconGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 28/03/2021
 */
public class UnityPackageLibraryType extends LibraryType<LibraryProperties>
{
	public static PersistentLibraryKind<LibraryProperties> ID = new PersistentLibraryKind<>("unity")
	{
		@Nonnull
		@Override
		public LibraryProperties createDefaultProperties()
		{
			return DummyLibraryProperties.INSTANCE;
		}
	};

	public UnityPackageLibraryType()
	{
		super(ID);
	}

	@Nullable
	@Override
	public String getCreateActionName()
	{
		return null;
	}

	@Nullable
	@Override
	public NewLibraryConfiguration createNewLibrary(@Nonnull JComponent parentComponent, @Nullable VirtualFile contextDirectory, @Nonnull Project project)
	{
		return null;
	}

	@Nullable
	@Override
	public LibraryPropertiesEditor createPropertiesEditor(@Nonnull LibraryEditorComponent<LibraryProperties> editorComponent)
	{
		return null;
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return Unity3dIconGroup.unity3d();
	}
}
