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

import consulo.content.bundle.Sdk;
import consulo.ide.moduleImport.ModuleImportContext;
import consulo.project.Project;
import consulo.unity3d.jsonApi.UnityOpenFilePostHandlerRequest;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 31-Jan-17
 */
public class UnityModuleImportContext extends ModuleImportContext
{
	private Sdk mySdk;
	private UnityOpenFilePostHandlerRequest myRequestor;

	public UnityModuleImportContext(@Nullable Project project)
	{
		super(project);
	}

	public void setSdk(Sdk sdk)
	{
		mySdk = sdk;
	}

	public Sdk getSdk()
	{
		return mySdk;
	}

	public void setRequestor(UnityOpenFilePostHandlerRequest requestor)
	{
		myRequestor = requestor;
	}

	public UnityOpenFilePostHandlerRequest getRequestor()
	{
		return myRequestor;
	}
}
