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

package consulo.unity3d.scene;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.LowMemoryWatcher;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ObjectUtil;
import com.intellij.util.indexing.FileBasedIndex;
import consulo.unity3d.Unity3dMetaFileType;
import consulo.unity3d.scene.index.Unity3dMetaIndexExtension;

/**
 * @author VISTALL
 * @since 01-Sep-17
 */
public class Unity3dMetaManager implements Disposable
{
	@NotNull
	public static Unity3dMetaManager getInstance(@NotNull Project project)
	{
		return ServiceManager.getService(project, Unity3dMetaManager.class);
	}

	private Project myProject;
	private Map<Integer, Object> myGUIDs = new ConcurrentHashMap<>();

	public Unity3dMetaManager(Project project)
	{
		myProject = project;
		myProject.getMessageBus().connect().subscribe(PsiModificationTracker.TOPIC, () -> myGUIDs.clear());

		LowMemoryWatcher.register(() -> myGUIDs.clear(), this);
	}

	@Nullable
	public String getGUID(@NotNull VirtualFile virtualFile)
	{
		String name = virtualFile.getName();

		VirtualFile parent = virtualFile.getParent();
		if(parent == null)
		{
			return null;
		}

		int targetId = FileBasedIndex.getFileId(virtualFile);

		Object o = myGUIDs.computeIfAbsent(targetId, integer ->
		{
			VirtualFile child = parent.findChild(name + "." + Unity3dMetaFileType.INSTANCE.getDefaultExtension());
			if(child != null)
			{
				int fileId = FileBasedIndex.getFileId(child);

				List<String> values = FileBasedIndex.getInstance().getValues(Unity3dMetaIndexExtension.KEY, fileId, GlobalSearchScope.allScope(myProject));
				if(values.isEmpty())
				{
					return ObjectUtil.NULL;
				}
				return values.get(0);
			}
			return ObjectUtil.NULL;
		});
		return o instanceof String ? (String) o : null;
	}

	@Override
	public void dispose()
	{
		myGUIDs.clear();
	}
}
