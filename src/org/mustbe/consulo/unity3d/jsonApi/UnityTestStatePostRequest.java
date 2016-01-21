package org.mustbe.consulo.unity3d.jsonApi;

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
		TestFinished,
		SuiteStarted,
		SuiteFinished,
		RunFinished
	}

	public String uuid;
	public String name;
	public Type type;

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("UnityTestStatePostRequest{");
		sb.append("uuid='").append(uuid).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", type=").append(type);
		sb.append('}');
		return sb.toString();
	}
}
