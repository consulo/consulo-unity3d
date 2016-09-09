package consulo.unity3d.unityscript.projectView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.annotations.RequiredDispatchThread;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptFileProjectViewProvider implements TreeStructureProvider, DumbAware
{
	private final Project myProject;

	public UnityScriptFileProjectViewProvider(Project project)
	{
		myProject = project;
	}

	@Override
	@RequiredDispatchThread
	public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings)
	{
		if(!myProject.isInitialized())
		{
			return children;
		}
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(myProject);
		if(rootModuleExtension == null)
		{
			return children;
		}

		List<AbstractTreeNode> nodes = new ArrayList<AbstractTreeNode>(children.size());
		for(AbstractTreeNode child : children)
		{
			Object value = child.getValue();
			if(value instanceof JSFile)
			{
				Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement((PsiElement) value);
				if(moduleForPsiElement != null)
				{
					nodes.add(new UnityScriptFileNode(myProject, (PsiFile) value, settings));
					continue;
				}
			}
			nodes.add(child);
		}
		return nodes;
	}

	@Nullable
	@Override
	public Object getData(Collection<AbstractTreeNode> selected, String dataName)
	{
		return null;
	}
}
