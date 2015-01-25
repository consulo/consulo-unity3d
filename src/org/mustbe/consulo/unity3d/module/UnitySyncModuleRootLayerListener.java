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

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.openapi.roots.ModuleRootLayerListener;
import com.intellij.openapi.roots.ModuleRootManager;
import lombok.val;

/**
 * @author VISTALL
 * @since 25.01.15
 */
public class UnitySyncModuleRootLayerListener extends ModuleRootLayerListener.Adapter
{
	@Override
	public void currentLayerChanged(@NotNull Module module,
			@NotNull String oldName,
			@NotNull ModuleRootLayer moduleRootLayer,
			@NotNull String newName,
			@NotNull ModuleRootLayer moduleRootLayer2)
	{
		if(!module.getProject().isOpen())
		{
			return;
		}
		ModuleManager moduleManager = ModuleManager.getInstance(module.getProject());
		for(Module anotherModule : moduleManager.getModules())
		{
			if(anotherModule == module)
			{
				continue;
			}

			ModuleRootManager rootManager = ModuleRootManager.getInstance(anotherModule);
			val modifiableModel = rootManager.getModifiableModel();
			modifiableModel.setCurrentLayer(newName);

			ApplicationManager.getApplication().runWriteAction(new Runnable()
			{
				@Override
				public void run()
				{
					modifiableModel.commit();
				}
			});
		}
	}
}
