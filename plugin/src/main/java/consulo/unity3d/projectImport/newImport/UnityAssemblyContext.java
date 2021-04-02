/*
 * Copyright 2013-2021 consulo.io
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

package consulo.unity3d.projectImport.newImport;

import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.unity3d.asmdef.AsmDefElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author VISTALL
 * @since 23/03/2021
 */
public class UnityAssemblyContext
{
	@Nonnull
	private final UnityAssemblyType myType;
	@Nonnull
	private final String myName;
	private final VirtualFile myAsmdefFile;
	@Nullable
	private final VirtualFile myAsmDirectory;
	private final AsmDefElement myAsmDefElement;

	private Set<VirtualFile> mySourceFiles = new HashSet<>();
	private final Set<VirtualFile> myAssemblies = new LinkedHashSet<>();

	private Library myLibrary;

	private Set<UnityAssemblyContext> myDependencies = new HashSet<>();

	UnityAssemblyContext(@Nonnull UnityAssemblyType type, @Nonnull String name, @Nullable VirtualFile asmdefFile, AsmDefElement asmDefElement)
	{
		myType = type;
		myName = name;
		myAsmdefFile = asmdefFile;
		myAsmDefElement = asmDefElement;

		myAsmDirectory = asmdefFile != null ? asmdefFile.getParent() : null;
	}

	public void addSourceFile(@Nonnull VirtualFile virtualFile)
	{
		mySourceFiles.add(virtualFile);
	}

	public void addAssembly(@Nonnull VirtualFile virtualFile)
	{
		myAssemblies.add(virtualFile);
	}

	@Nonnull
	public Set<VirtualFile> getAssemblies()
	{
		return myAssemblies;
	}

	@Nonnull
	public UnityAssemblyType getType()
	{
		return myType;
	}

	@Nullable
	public AsmDefElement getAsmDefElement()
	{
		return myAsmDefElement;
	}

	@Nonnull
	public String getName()
	{
		return myName;
	}

	@Nullable
	public VirtualFile getAsmdefFile()
	{
		return myAsmdefFile;
	}

	@Nonnull
	public Set<VirtualFile> getSourceFiles()
	{
		return mySourceFiles;
	}

	@Nullable
	public VirtualFile getAsmDirectory()
	{
		return myAsmDirectory;
	}

	public void setLibrary(Library library)
	{
		myLibrary = library;
	}

	@Nullable
	public Library getLibrary()
	{
		return myLibrary;
	}

	public Set<UnityAssemblyContext> getDependencies()
	{
		return myDependencies;
	}

	public void addDependency(UnityAssemblyContext context)
	{
		myDependencies.add(context);
	}

	@Override
	public String toString()
	{
		return "UnityAssemblyContext{" +
				"myName='" + myName + '\'' +
				", myType=" + myType +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		UnityAssemblyContext that = (UnityAssemblyContext) o;
		return Objects.equals(myName, that.myName);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(myName);
	}
}
