/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.scene.reference;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.unity3d.scene.Unity3dMetaManager;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 03-Sep-17
 */
public class Unity3dAssetGUIDReference extends Unity3dKeyValueReferenceBase
{
	public Unity3dAssetGUIDReference(YAMLKeyValue keyValue)
	{
		super(keyValue);
	}

	@RequiredReadAction
	@Override
	public PsiElement getElement()
	{
		return myKeyValue;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public TextRange getRangeInElement()
	{
		YAMLValue value = myKeyValue.getValue();
		if(value == null)
		{
			return super.getRangeInElement();
		}
		int startOffsetInParent = value.getStartOffsetInParent();
		return new TextRange(startOffsetInParent, startOffsetInParent + value.getTextLength());
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement resolve()
	{
		Project project = myKeyValue.getProject();
		if(DumbService.isDumb(project))
		{
			return null;
		}
		String valueText = myKeyValue.getValueText();

		if(StringUtil.isEmpty(valueText))
		{
			return null;
		}
		VirtualFile fileByGUID = Unity3dMetaManager.getInstance(project).findFileByGUID(valueText);
		if(fileByGUID != null)
		{
			return PsiManager.getInstance(project).findFile(fileByGUID);
		}
		return null;
	}
}
