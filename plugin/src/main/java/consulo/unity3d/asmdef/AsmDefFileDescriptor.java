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

package consulo.unity3d.asmdef;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import consulo.json.jom.JomFileDescriptor;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 22/03/2021
 */
public class AsmDefFileDescriptor extends JomFileDescriptor<AsmDefElement>
{
	public static final String EXTENSION = "asmdef";

	public AsmDefFileDescriptor()
	{
		super(AsmDefElement.class);
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return PlatformIconGroup.nodesPpLib();
	}

	@Override
	public boolean isMyFile(@Nonnull PsiFile psiFile)
	{
		String extension = FileUtil.getExtension(psiFile.getName());
		if(EXTENSION.equals(extension))
		{
			return true;
		}
		return false;
	}
}
