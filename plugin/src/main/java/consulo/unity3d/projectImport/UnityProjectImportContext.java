/*
 * Copyright 2013-2020 consulo.io
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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import consulo.application.AccessRule;
import consulo.logging.Logger;
import consulo.unity3d.bundle.Unity3dDefineByVersion;
import consulo.unity3d.packages.Unity3dManifest;
import consulo.unity3d.scene.Unity3dYMLAssetFileType;
import org.jetbrains.yaml.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-06-24
 */
public class UnityProjectImportContext
{
	private static final Logger LOG = Logger.getInstance(UnityProjectImportContext.class);
	private static final String UNITY_EDITOR = "UNITY_EDITOR";

	public static UnityProjectImportContext load(@Nullable Collection<String> rootDefines, @Nonnull Project project, @Nonnull VirtualFile baseDir)
	{
		int scriptRuntimeVersion = 0;

		// load script version
		VirtualFile projectSettingsFile = baseDir.findFileByRelativePath("ProjectSettings/ProjectSettings.asset");
		if(projectSettingsFile != null && projectSettingsFile.getFileType() == Unity3dYMLAssetFileType.INSTANCE)
		{
			Integer version = AccessRule.read(() ->
			{
				PsiFile file = PsiManager.getInstance(project).findFile(projectSettingsFile);
				if(file instanceof YAMLFile)
				{
					List<YAMLDocument> documents = ((YAMLFile) file).getDocuments();
					for(YAMLDocument document : documents)
					{
						YAMLValue topLevelValue = document.getTopLevelValue();
						if(topLevelValue instanceof YAMLMapping)
						{
							YAMLKeyValue playerSettings = ((YAMLMapping) topLevelValue).getKeyValueByKey("PlayerSettings");
							if(playerSettings != null)
							{
								YAMLValue value = playerSettings.getValue();
								if(value instanceof YAMLMapping)
								{
									YAMLKeyValue scriptingRuntimeVersion = ((YAMLMapping) value).getKeyValueByKey("scriptingRuntimeVersion");
									if(scriptingRuntimeVersion != null)
									{
										String valueText = scriptingRuntimeVersion.getValueText();
										return StringUtil.parseInt(valueText, 0);
									}
								}
							}
						}
					}
				}

				return 0;
			});
			assert version != null;
			scriptRuntimeVersion = version;
		}

		VirtualFile argumentsFile = baseDir.findFileByRelativePath(Unity3dProjectImportUtil.ASSETS_DIRECTORY + "/csc.rsp");
		if(argumentsFile == null)
		{
			argumentsFile = baseDir.findFileByRelativePath(Unity3dProjectImportUtil.ASSETS_DIRECTORY + "/mcs.rsp");
		}

		List<String> additionalDefines = new ArrayList<>();
		if(argumentsFile != null)
		{
			try
			{
				String text = VfsUtil.loadText(argumentsFile);
				String[] lines = StringUtil.splitByLines(text);
				for(String line : lines)
				{
					String prefix = "-define:";
					if(line.startsWith(prefix))
					{
						String def = line.substring(prefix.length(), line.length());
						String[] values = def.split(",");
						for(String value : values)
						{
							if(StringUtil.isEmptyOrSpaces(value))
							{
								continue;
							}

							additionalDefines.add(value.trim());
						}
					}
				}
			}
			catch(IOException e)
			{
				LOG.warn(e);
			}
		}

		return new UnityProjectImportContext(rootDefines, additionalDefines, scriptRuntimeVersion, Unity3dManifest.parse(project));
	}

	private final int myScriptRuntimeVersion;

	/**
	 * Main defines. If null - will fallback to default
	 */
	private final Collection<String> myRootDefines;

	private final Collection<String> myAdditionalDefines;

	private final Unity3dManifest myManifest;

	private UnityProjectImportContext(Collection<String> rootDefines, @Nonnull Collection<String> additionalDefines, int scriptRuntimeVersion, Unity3dManifest manifest)
	{
		myRootDefines = rootDefines;
		myAdditionalDefines = additionalDefines;
		myScriptRuntimeVersion = scriptRuntimeVersion;
		myManifest = manifest;
	}

	public int getScriptRuntimeVersion()
	{
		return myScriptRuntimeVersion;
	}

	@Nullable
	public Collection<String> getRootDefines()
	{
		return myRootDefines;
	}

	@Nonnull
	public Collection<String> getAdditionalDefines()
	{
		return myAdditionalDefines;
	}

	@Nonnull
	public Unity3dManifest getManifest()
	{
		return myManifest;
	}

	@Nonnull
	public Collection<String> calculateDefines(@Nullable Sdk unityBundle)
	{
		List<String> variables = new ArrayList<>();

		Collection<String> rootDefines = getRootDefines();
		if(rootDefines != null)
		{
			variables.addAll(rootDefines);
		}
		// fallback
		else
		{
			variables.add(UNITY_EDITOR);
			variables.add("DEBUG");
			variables.add("TRACE");

			Unity3dDefineByVersion unity3dDefineByVersion = Unity3dProjectImportUtil.getUnity3dDefineByVersion(unityBundle);
			if(unity3dDefineByVersion != Unity3dDefineByVersion.UNKNOWN)
			{
				for(Unity3dDefineByVersion majorVersion : unity3dDefineByVersion.getMajorVersions())
				{
					variables.add(majorVersion.name());
				}
				variables.add(unity3dDefineByVersion.name());
			}
		}

		variables.addAll(myAdditionalDefines);

		return variables;
	}
}
