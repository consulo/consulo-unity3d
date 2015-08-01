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

package org.mustbe.consulo.unity3d.module;

import org.consulo.module.extension.ModuleInheritableNamedPointer;
import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.consulo.util.pointers.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
public class EmptyModuleInheritableNamedPointer<T extends Named> implements MutableModuleInheritableNamedPointer<T>
{
	private static final EmptyModuleInheritableNamedPointer ourInstance = new EmptyModuleInheritableNamedPointer();

	@NotNull
	@SuppressWarnings("unchecked")
	public static <T extends Named> EmptyModuleInheritableNamedPointer<T> empty()
	{
		return ourInstance;
	}

	@Nullable
	@Override
	public Module getModule()
	{
		return null;
	}

	@Nullable
	@Override
	public String getModuleName()
	{
		return null;
	}

	@Override
	public boolean isNull()
	{
		return true;
	}

	@Nullable
	@Override
	public T get()
	{
		return null;
	}

	@Nullable
	@Override
	public String getName()
	{
		return "null";
	}

	@Override
	public void set(ModuleInheritableNamedPointer<T> value)
	{
	}

	@Override
	public void set(@Nullable String moduleName, @Nullable String name)
	{
	}

	@Override
	public void set(@Nullable Module module, @Nullable T named)
	{
	}
}
