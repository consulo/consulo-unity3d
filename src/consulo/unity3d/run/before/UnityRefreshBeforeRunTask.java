package consulo.unity3d.run.before;

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.BeforeRunTask;
import com.intellij.openapi.util.Key;

/**
 * @author VISTALL
 * @since 21.01.2016
 */
public class UnityRefreshBeforeRunTask extends BeforeRunTask<UnityRefreshBeforeRunTask>
{
	public UnityRefreshBeforeRunTask(@NotNull Key<UnityRefreshBeforeRunTask> providerId)
	{
		super(providerId);
	}
}
