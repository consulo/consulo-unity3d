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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import consulo.annotations.RequiredReadAction;
import consulo.unity3d.scene.Unity3dMetaManager;

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
	@NotNull
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
