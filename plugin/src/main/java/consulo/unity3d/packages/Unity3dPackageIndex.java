/*
 * Copyright 2013-2017 consulo.io
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

package consulo.unity3d.packages;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 21-Oct-17
 */
public class Unity3dPackageIndex
{
	public static final Unity3dPackageIndex EMPTY = new Unity3dPackageIndex(Collections.emptyList());

	private final List<Unity3dPackage> myTopPackages;

	public Unity3dPackageIndex(List<Unity3dPackage> topPackages)
	{
		myTopPackages = topPackages;
	}

	@NotNull
	public List<Unity3dPackage> getTopPackages()
	{
		return myTopPackages;
	}
}
