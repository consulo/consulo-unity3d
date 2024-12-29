package consulo.unity3d.run.debugger;

import jakarta.annotation.Nullable;

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
