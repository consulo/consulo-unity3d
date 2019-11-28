package consulo.unity3d.usages;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageView;
import com.intellij.usages.impl.RuleAction;
import com.intellij.usages.rules.PsiElementUsage;
import com.intellij.usages.rules.UsageFilteringRule;
import com.intellij.usages.rules.UsageFilteringRuleProvider;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2018-03-10
 */
public class Unity3dAssetUsageFilteringRuleProvider implements UsageFilteringRuleProvider
{
	private static class ShowAssetUsageAction extends RuleAction
	{
		private ShowAssetUsageAction(UsageView view)
		{
			super(view, "Show assets usage", Unity3dIcons.Unity3d);
		}

		@RequiredUIAccess
		@Override
		public void update(AnActionEvent e)
		{
			super.update(e);
			Project project = e.getProject();
			e.getPresentation().setVisible(project != null && Unity3dModuleExtensionUtil.getRootModuleExtension(project) != null);
		}

		@Override
		protected boolean getOptionValue()
		{
			return Unity3dAssetUsageSettings.getInstance().SHOW_USAGES_IN_ASSETS;
		}

		@Override
		protected void setOptionValue(boolean value)
		{
			Unity3dAssetUsageSettings.getInstance().SHOW_USAGES_IN_ASSETS = value;
		}
	}

	private static class AssetUsageFilteringRule implements UsageFilteringRule
	{
		@Override
		public boolean isVisible(@Nonnull Usage usage, @Nonnull UsageTarget[] targets)
		{
			if(usage instanceof PsiElementUsage)
			{
				PsiFile containingFile = ((PsiElementUsage) usage).getElement().getContainingFile();
				if(containingFile != null && containingFile.getFileType() == Unity3dYMLAssetFileType.INSTANCE)
				{
					return false;
				}
			}
			return true;
		}
	}

	@Nonnull
	@Override
	public UsageFilteringRule[] getActiveRules(@Nonnull Project project)
	{
		if(!Unity3dAssetUsageSettings.getInstance().SHOW_USAGES_IN_ASSETS)
		{
			return new UsageFilteringRule[]{new AssetUsageFilteringRule()};
		}
		return UsageFilteringRule.EMPTY_ARRAY;
	}

	@Nonnull
	@Override
	public AnAction[] createFilteringActions(@Nonnull UsageView view)
	{
		if(view.getPresentation().isCodeUsages())
		{
			return new AnAction[]{new ShowAssetUsageAction(view)};
		}
		else
		{
			return AnAction.EMPTY_ARRAY;
		}
	}
}
