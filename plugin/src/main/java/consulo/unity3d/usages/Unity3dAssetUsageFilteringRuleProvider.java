package consulo.unity3d.usages;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;
import consulo.usage.RuleAction;
import consulo.usage.Usage;
import consulo.usage.UsageTarget;
import consulo.usage.UsageView;
import consulo.usage.rule.PsiElementUsage;
import consulo.usage.rule.UsageFilteringRule;
import consulo.usage.rule.UsageFilteringRuleProvider;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2018-03-10
 */
@ExtensionImpl
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
			Project project = e.getData(Project.KEY);
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
