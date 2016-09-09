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

package consulo.cgshader;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public class CGFileType extends LanguageFileType
{
	public static final CGFileType INSTANCE = new CGFileType();

	public CGFileType()
	{
		super(CGLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public String getName()
	{
		return "CG";
	}

	@NotNull
	@Override
	public String getDescription()
	{
		return "CG shader files";
	}

	@NotNull
	@Override
	public String getDefaultExtension()
	{
		return "cg";
	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return AllIcons.FileTypes.Text;
	}
}
