package org.mustbe.consulo.unity3d.jsonApi;

/**
 * @author VISTALL
 * @since 19.01.2016
 */
public class UnityTestStatePostRequest
{
	public String uuid;
	public String name;
	public boolean suite;
	public boolean state;

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("UnityTestStatePostRequest{");
		sb.append("uuid='").append(uuid).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", suite=").append(suite);
		sb.append(", state=").append(state);
		sb.append('}');
		return sb.toString();
	}
}
