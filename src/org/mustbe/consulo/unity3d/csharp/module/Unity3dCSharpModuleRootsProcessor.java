/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.unity3d.csharp.module;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectProcedure;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.roots.ContentFolderTypeProvider;
import org.mustbe.consulo.roots.impl.ProductionContentFolderTypeProvider;
import org.mustbe.consulo.unity3d.csharp.module.extension.Unity3dCSharpModuleExtension;
import com.google.common.base.Predicate;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.impl.ModuleRootsProcessor;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 29.03.2015
 */
public class Unity3dCSharpModuleRootsProcessor extends ModuleRootsProcessor
{
	@Override
	public boolean containsFile(@NotNull TObjectIntHashMap<VirtualFile> roots, @NotNull final VirtualFile virtualFile)
	{
		return !roots.forEachKey(new TObjectProcedure<VirtualFile>()
		{
			@Override
			public boolean execute(VirtualFile object)
			{
				return !VfsUtil.isAncestor(object, virtualFile, false);
			}
		});
	}

	@NotNull
	@Override
	public VirtualFile[] getFiles(@NotNull ModuleRootModel moduleRootModel, @NotNull Predicate<ContentFolderTypeProvider> predicate)
	{
		if(predicate.apply(ProductionContentFolderTypeProvider.getInstance()))
		{
			return moduleRootModel.getContentRoots();
		}
		return VirtualFile.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public String[] getUrls(@NotNull ModuleRootModel moduleRootModel, @NotNull Predicate<ContentFolderTypeProvider> predicate)
	{
		if(predicate.apply(ProductionContentFolderTypeProvider.getInstance()))
		{
			return moduleRootModel.getContentRootUrls();
		}
		return ArrayUtil.EMPTY_STRING_ARRAY;
	}

	@Override
	public boolean canHandle(@NotNull ModuleRootModel moduleRootModel)
	{
		return moduleRootModel.getExtension(Unity3dCSharpModuleExtension.class) != null;
	}
}
