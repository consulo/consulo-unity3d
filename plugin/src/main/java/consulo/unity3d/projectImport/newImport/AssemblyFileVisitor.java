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

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import consulo.annotation.access.RequiredReadAction;
import consulo.json.JsonFileType;
import consulo.json.jom.JomElement;
import consulo.json.jom.JomFileElement;
import consulo.json.jom.JomManager;
import consulo.unity3d.asmdef.AsmDefElement;
import consulo.unity3d.asmdef.AsmDefFileDescriptor;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author VISTALL
 * @since 26/03/2021
 */
class AssemblyFileVisitor extends VirtualFileVisitor
{
	private final PsiManager myPsiManager;
	private final JomManager myJomManager;
	private final FileTypeManager myFileTypeManager;
	private final UnityAssemblyType myType;
	private final Map<String, UnityAssemblyContext> myAssemblies;

	public AssemblyFileVisitor(Project project, UnityAssemblyType type, Map<String, UnityAssemblyContext> assemblies)
	{
		myType = type;
		myAssemblies = assemblies;
		myFileTypeManager = FileTypeManager.getInstance();
		myPsiManager = PsiManager.getInstance(project);
		myJomManager = JomManager.getInstance(project);
	}

	@Override
	@RequiredReadAction
	public boolean visitFile(@Nonnull VirtualFile file)
	{
		if(myFileTypeManager.isFileIgnored(file))
		{
			return false;
		}

		if(file.getFileType() == JsonFileType.INSTANCE && AsmDefFileDescriptor.EXTENSION.equals(file.getExtension()))
		{
			ReadAction.run(() -> {
				PsiFile maybeJsonFile = myPsiManager.findFile(file);
				if(maybeJsonFile != null)
				{
					JomFileElement<JomElement> fileElement = myJomManager.getFileElement(maybeJsonFile);
					if(fileElement != null && fileElement.getRootElement() instanceof AsmDefElement def)
					{
						String name = def.getName();
						if(!StringUtil.isEmptyOrSpaces(name))
						{
							myAssemblies.put(name, new UnityAssemblyContext(myType, name, file, def));
						}
					}
				}
			});
		}
		return super.visitFile(file);
	}
}
