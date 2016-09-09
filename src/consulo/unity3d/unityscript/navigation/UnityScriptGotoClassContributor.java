package consulo.unity3d.unityscript.navigation;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.unity3d.Unity3dIcons;
import consulo.unity3d.unityscript.index.UnityScriptIndexKeys;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.GotoClassContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import consulo.ide.IconDescriptor;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptGotoClassContributor implements ChooseByNameContributorEx, GotoClassContributor
{
	@Nullable
	@Override
	public String getQualifiedName(NavigationItem item)
	{
		return null;
	}

	@Nullable
	@Override
	public String getQualifiedNameSeparator()
	{
		return ".";
	}

	@NotNull
	@Override
	public String[] getNames(Project project, boolean includeNonProjectItems)
	{
		CommonProcessors.CollectProcessor<String> processor = new CommonProcessors.CollectProcessor<String>(ContainerUtil.<String>newTroveSet());
		processNames(processor, GlobalSearchScope.allScope(project), IdFilter.getProjectIdFilter(project, includeNonProjectItems));
		return processor.toArray(ArrayUtil.STRING_ARRAY_FACTORY);
	}

	@NotNull
	@Override
	public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems)
	{
		CommonProcessors.CollectProcessor<NavigationItem> processor = new CommonProcessors.CollectProcessor<NavigationItem>(ContainerUtil
				.<NavigationItem>newTroveSet());
		processElementsWithName(name, processor, new FindSymbolParameters(pattern, name, GlobalSearchScope.allScope(project),
				IdFilter.getProjectIdFilter(project, includeNonProjectItems)));
		return processor.toArray(NavigationItem.ARRAY_FACTORY);
	}

	@Override
	public void processNames(@NotNull Processor<String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter)
	{
		StubIndex.getInstance().processAllKeys(UnityScriptIndexKeys.FILE_BY_NAME_INDEX, processor, scope, filter);
	}

	@Override
	public void processElementsWithName(@NotNull String name,
			@NotNull final Processor<NavigationItem> processor,
			@NotNull FindSymbolParameters parameters)
	{
		StubIndex.getInstance().processElements(UnityScriptIndexKeys.FILE_BY_NAME_INDEX, name, parameters.getProject(), parameters.getSearchScope(),
				parameters.getIdFilter(), JSFile.class, new Processor<JSFile>()
		{
			@Override
			public boolean process(final JSFile file)
			{
				return processor.process(new FakePsiElement()
				{
					@Override
					public String getName()
					{
						return FileUtil.getNameWithoutExtension(file.getName());
					}

					@Nullable
					@Override
					public Icon getIcon(boolean open)
					{
						IconDescriptor descriptor = new IconDescriptor(AllIcons.Nodes.Class);
						descriptor.addLayerIcon(Unity3dIcons.Js);
						descriptor.setRightIcon(AllIcons.Nodes.C_public);
						return descriptor.toIcon();
					}

					@Override
					public PsiElement getParent()
					{
						return file;
					}
				});
			}
		});
	}
}
