package consulo.unity3d.run.debugger;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27/01/2021
 */
public interface UnityExternalDevice
{
	boolean isAvailable();

	void update();

	@Nullable
	UnityDebugProcessInfo mapToDebuggerProcess();

	String toString();

	boolean equals(Object other);

	int hashCode();
}
