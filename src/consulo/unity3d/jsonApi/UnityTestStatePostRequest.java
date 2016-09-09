package consulo.unity3d.jsonApi;

/**
 * @author VISTALL
 * @since 19.01.2016
 */
public class UnityTestStatePostRequest
{
	public enum Type
	{
		TestStarted,
		TestFailed,
		TestIgnored,
		TestFinished,
		TestOutput,
		SuiteStarted,
		SuiteFinished,
		RunFinished
	}

	public String uuid;
	public String name;
	public Type type;
	public String message;
	public String messageType;
	public String stackTrace;
	public double time;

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("UnityTestStatePostRequest{");
		sb.append("uuid='").append(uuid).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", type=").append(type);
		sb.append(", message=").append(message);
		sb.append(", messageType=").append(messageType);
		sb.append(", stackTrace=").append(stackTrace);
		sb.append(", time=").append(time);
		sb.append('}');
		return sb.toString();
	}
}
