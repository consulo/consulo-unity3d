package org.mustbe.consulo.unity3d.unityscript.projectView;

import consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
@Logger
public class UnityScriptFileNode extends PsiFileNode
{
	public UnityScriptFileNode(Project project, @NotNull PsiFile value, ViewSettings viewSettings)
	{
		super(project, value, viewSettings);
	}

	@Override
	public void update(final PresentationData data)
	{
		if(!validate())
		{
			return;
		}

		final PsiElement value = extractPsiFromValue();
		LOGGER.assertTrue(value.isValid());

		data.setIcon(IconDescriptorUpdaters.getIcon(getValue(), Iconable.ICON_FLAG_VISIBILITY));
		data.setPresentableText(FileUtil.getNameWithoutExtension(getValue().getName()));
	}
}
