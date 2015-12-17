package org.mustbe.consulo.unity3d.unityscript.lang.impl;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.unity3d.unityscript.index.UnityScriptFileByNameIndex;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptPsiSearcher extends DotNetPsiSearcher
{
	private Project myProject;

	public UnityScriptPsiSearcher(Project project)
	{
		myProject = project;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public Collection<? extends DotNetTypeDeclaration> findTypesImpl(@NotNull String s,
			@NotNull GlobalSearchScope searchScope,
			@NotNull TypeResoleKind typeResoleKind)
	{
		Collection<JSFile> jsFiles = UnityScriptFileByNameIndex.getInstance().get(s, myProject, searchScope);
		if(jsFiles.isEmpty())
		{
			return Collections.emptyList();
		}
		JSFile firstItem = ContainerUtil.getFirstItem(jsFiles);
		return Collections.singletonList(new UnityScriptDotNetTypeDeclaration(s, firstItem));
	}
}
