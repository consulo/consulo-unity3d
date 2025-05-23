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

package consulo.cgshader;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Commenter;
import consulo.language.Language;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
/**
 * @author VISTALL
 * @since 11.10.2015
 */
@ExtensionImpl
public class CGCommenter implements Commenter
{
	@Nullable
	@Override
	public String getLineCommentPrefix()
	{
		return "//";
	}

	@Nullable
	@Override
	public String getBlockCommentPrefix()
	{
		return "/*";
	}

	@Nullable
	@Override
	public String getBlockCommentSuffix()
	{
		return "*/";
	}

	@Nullable
	@Override
	public String getCommentedBlockCommentPrefix()
	{
		return null;
	}

	@Nullable
	@Override
	public String getCommentedBlockCommentSuffix()
	{
		return null;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CGLanguage.INSTANCE;
	}
}
