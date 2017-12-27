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

package consulo.unity3d.projectImport;

import com.intellij.openapi.projectRoots.Sdk;
import consulo.moduleImport.ModuleImportContext;
import consulo.unity3d.jsonApi.UnityOpenFilePostHandlerRequest;

/**
 * @author VISTALL
 * @since 31-Jan-17
 */
public class UnityModuleImportContext extends ModuleImportContext
{
	private Sdk mySdk;
	private UnityOpenFilePostHandlerRequest myRequestor;

	public UnityModuleImportContext setSdk(Sdk sdk)
	{
		mySdk = sdk;
		return this;
	}

	public Sdk getSdk()
	{
		return mySdk;
	}

	public UnityModuleImportContext setRequestor(UnityOpenFilePostHandlerRequest requestor)
	{
		myRequestor = requestor;
		return this;
	}

	public UnityOpenFilePostHandlerRequest getRequestor()
	{
		return myRequestor;
	}

	@Override
	public void dispose()
	{
		super.dispose();
		mySdk = null;
	}
}
