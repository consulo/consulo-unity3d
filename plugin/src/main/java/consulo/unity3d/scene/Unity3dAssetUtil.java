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

package consulo.unity3d.scene;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 10.03.2016
 */
public class Unity3dAssetUtil
{
	@RequiredReadAction
	public static boolean isPrimaryType(@Nullable PsiElement element)
	{
		if(element == null)
		{
			return false;
		}

		PsiFile containingFile = element.getContainingFile();
		if(containingFile == null)
		{
			return false;
		}

		CSharpTypeDeclaration primaryType = findPrimaryType(containingFile);
		return primaryType != null && PsiManager.getInstance(containingFile.getProject()).areElementsEquivalent(primaryType, element);
	}

	@Nullable
	@RequiredReadAction
	public static CSharpTypeDeclaration findPrimaryType(@Nonnull PsiFile file)
	{
		Ref<CSharpTypeDeclaration> typeRef = Ref.create();
		file.accept(new PsiRecursiveElementWalkingVisitor()
		{
			@Override
			public void visitElement(PsiElement element)
			{
				if(element instanceof CSharpTypeDeclaration)
				{
					typeRef.set((CSharpTypeDeclaration) element);
					stopWalking();
				}
				super.visitElement(element);
			}
		});
		return typeRef.get();
	}

	@Nonnull
	public static VirtualFile[] sortAssetFiles(VirtualFile[] virtualFiles)
	{
		ContainerUtil.sort(virtualFiles, (o1, o2) -> weight(o1) - weight(o2));
		return virtualFiles;
	}

	private static int weight(VirtualFile virtualFile)
	{
		int i = ArrayUtil.indexOf(Unity3dAssetFileTypeDetector.ourAssetExtensionsArray, virtualFile.getExtension());
		if(i == -1)
		{
			return 1000;
		}
		else
		{
			return (i + 1) * 10;
		}
	}

	@Nullable
	public static String getGUID(@Nullable Project project, @Nullable VirtualFile virtualFile)
	{
		if(virtualFile == null || project == null)
		{
			return null;
		}
		return Unity3dMetaManager.getInstance(project).getGUID(virtualFile);
	}
}
