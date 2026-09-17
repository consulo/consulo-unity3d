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
package consulo.unity3d.importing;

import consulo.application.Application;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.project.impl.internal.DumbServiceImpl;
import consulo.project.internal.UnindexedFilesScannerExecutor;
import consulo.project.ProjectManager;
import consulo.project.ProjectOpenContext;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Opens a real project on a Unity tree, mirroring {@code ScanningTestSupport} in the platform's it module.
 *
 * @author VISTALL
 */
public final class UnityImportTestSupport {
    public static final long TIMEOUT_SECONDS = 60;

    private UnityImportTestSupport() {
    }

    public static Project openProject(Application application, ProjectManager projectManager, Path directory) throws Exception {
        Project project = projectManager
            .openProjectAsync(directory, application.getLastUIAccess(), new ProjectOpenContext())
            .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(project).isNotNull();
        return project;
    }

    public static void closeProject(Project project) throws Exception {
        if (project.isDisposed()) {
            return;
        }

        // background scanning and dumb tasks keep touching the project; closing underneath them makes services
        // log "Already disposed" and fails whichever test happens to be running. Let them finish first.
        awaitIdle(project);

        ProjectManager.getInstance().closeAndDisposeAsync(project, project.getUIAccess()).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Waits until the project reaches smart mode and its scanning and dumb queues are empty.
     */
    public static void awaitIdle(Project project) throws Exception {
        DumbServiceImpl dumbService = (DumbServiceImpl) DumbService.getInstance(project);

        CountDownLatch smart = new CountDownLatch(1);
        dumbService.runWhenSmart(smart::countDown);
        smart.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        UnindexedFilesScannerExecutor executor = UnindexedFilesScannerExecutor.getInstance(project);
        waitFor(() -> !executor.isRunning().get()
            && !executor.hasQueuedTasks()
            && !dumbService.hasScheduledTasks()
            && !dumbService.isRunning()
            && !dumbService.isDumb());
    }

    private static void waitFor(BooleanSupplier condition) throws Exception {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS);
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(20);
        }
    }

    /**
     * Writes {@code Assets/<relativePath>} and returns the file.
     */
    public static Path writeAsset(Path directory, String relativePath, String content) throws Exception {
        Path file = directory.resolve("Assets").resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
        return file;
    }

    public static String asmdef(String name, String... references) {
        StringBuilder refs = new StringBuilder();
        for (String reference : references) {
            if (!refs.isEmpty()) {
                refs.append(", ");
            }
            refs.append('"').append(reference).append('"');
        }
        return "{\"name\": \"" + name + "\", \"references\": [" + refs + "]}";
    }

    public static VirtualFile findFile(Path path) {
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path);
        assertThat(file).as("%s must be visible in the vfs", path).isNotNull();
        return file;
    }
}
