package consulo.unity3d.importing;

import consulo.annotation.component.ServiceImpl;
import consulo.project.Project;
import consulo.unity3d.base.scene.Unity3dMetaManager;
import consulo.unity3d.base.scene.Unity3dYMLAsset;
import consulo.util.collection.MultiMap;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Stands in for the index backed manager, which lives in the plugin module and is therefore not on the
 * importing module's test classpath. GUIDs are derived from the file name so they stay predictable.
 */
@Singleton
@ServiceImpl
public class TestUnity3dMetaManager implements Unity3dMetaManager {
    @Inject
    public TestUnity3dMetaManager(Project project) {
    }

    @Nullable
    @Override
    public String getGUID(@Nonnull VirtualFile virtualFile) {
        return "guid-" + virtualFile.getNameWithoutExtension();
    }

    @Nullable
    @Override
    public VirtualFile findFileByGUID(@Nonnull String guid) {
        return null;
    }

    @Nonnull
    @Override
    public MultiMap<VirtualFile, Unity3dYMLAsset> findAssetAsAttach(@Nonnull VirtualFile file) {
        return MultiMap.empty();
    }
}
