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

package consulo.unity3d.importing.newImport;

import consulo.application.ReadAction;
import consulo.language.file.FileTypeManager;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.unity3d.base.asmdef.AsmDefDescriptor;
import consulo.unity3d.base.asmdef.AsmDefFileDescriptor;
import consulo.unity3d.base.asmdef.AsmDefReader;
import consulo.unity3d.base.scene.Unity3dMetaManager;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import jakarta.annotation.Nonnull;

import java.util.Map;

/**
 * @author VISTALL
 * @since 26/03/2021
 */
class AsmDefFileVisitor extends VirtualFileVisitor {
    private static final Logger LOG = Logger.getInstance(AsmDefFileVisitor.class);

    private final FileTypeManager myFileTypeManager;
    private final UnityAssemblyType myType;
    private final Map<String, UnityAssemblyContext> myAssemblies;
    private final Unity3dMetaManager myUnityMetaManager;

    AsmDefFileVisitor(Project project, UnityAssemblyType type, Map<String, UnityAssemblyContext> assemblies) {
        myType = type;
        myAssemblies = assemblies;
        myFileTypeManager = FileTypeManager.getInstance();
        myUnityMetaManager = Unity3dMetaManager.getInstance(project);
    }

    @Override
    public boolean visitFile(@Nonnull VirtualFile file) {
        if (myFileTypeManager.isFileIgnored(file)) {
            return false;
        }

        if (!AsmDefFileDescriptor.EXTENSION.equals(file.getExtension())) {
            return super.visitFile(file);
        }

        AsmDefDescriptor def = AsmDefReader.read(file);
        if (def == null) {
            return super.visitFile(file);
        }

        String name = def.name;

        if (StringUtil.isEmptyOrSpaces(name)) {
            LOG.warn("Assembly definition without a name: " + file.getPath());
            return super.visitFile(file);
        }

        // the standard assemblies are seeded into the map before this visitor runs. unity itself rejects an
        // asmdef named after a predefined assembly, and letting one through here would hand the standard
        // importer a user context and scatter every loose script into it
        UnityAssemblyContext existing = myAssemblies.get(name);
        if (existing != null && existing.getType() == UnityAssemblyType.STANDARD) {
            LOG.warn("Assembly definition '" + name + "' at " + file.getPath()
                + " uses the name of a predefined Unity assembly - ignoring it");
            return super.visitFile(file);
        }

        UnityAssemblyContext context = new UnityAssemblyContext(myType, name, file, def);

        myAssemblies.put(name, context);

        String guid = ReadAction.compute(() -> myUnityMetaManager.getGUID(file));
        if (guid != null) {
            myAssemblies.put("GUID:" + guid, context);
        }

        return super.visitFile(file);
    }
}
