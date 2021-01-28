package consulo.unity3d.run.debugger;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 27/01/2021
 */
public interface UnityExternalDeviceCollector
{
	void initialize(Project project, @Nonnull ProgressIndicator indicator, @Nonnull Consumer<UnityExternalDevice> consumer);

	void dispose();
}
