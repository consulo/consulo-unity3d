/*
 * Copyright 2013-2026 consulo.io
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

package consulo.unity3d.base;

import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditorManager;
import consulo.navigation.OpenFileDescriptor;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.application.ui.wm.IdeFocusManager;
import consulo.unity3d.base.jsonApi.UnityOpenFilePostHandlerRequest;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Opens the file UnityEditor asked for. Lives in the base module so that both the web api handler and the
 * project importer can reach it without depending on each other.
 *
 * @author VISTALL
 */
public class UnityFileOpener {
    public static void openFile(@Nullable Project openedProject, @Nonnull UnityOpenFilePostHandlerRequest body) {
        if (openedProject == null) {
            return;
        }

        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPathIfCached(body.filePath);
        if (fileByPath == null) {
            return;
        }

        OpenFileDescriptor descriptor = OpenFileDescriptorFactory.getInstance(openedProject)
            .newBuilder(fileByPath)
            .line(body.line - 1)
            .build();

        Editor editor = FileEditorManager.getInstance(openedProject).openTextEditor(descriptor, true);

        if (editor != null) {
            IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> editor.getComponent().grabFocus());
        }
    }

    private UnityFileOpener() {
    }
}
