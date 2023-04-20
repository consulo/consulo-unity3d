/**
 * @author VISTALL
 * @since 18-Sep-22
 */
module consulo.unity3d
{
	// TODO remove in future
	requires java.desktop;

	requires consulo.ide.api;

	requires consulo.language.impl;

	requires consulo.unity3d.shaderlab;

	requires consulo.unity3d.cg.shader;

	requires consulo.csharp.psi.impl;
	requires consulo.csharp;

	requires consulo.nunit.api;

	requires consulo.javascript.base.api;
	requires consulo.javascript.json.javascript.impl;

	requires org.jetbrains.plugins.yaml;

	requires consulo.dotnet.mono.debugger.impl;
	requires mono.soft.debugging;
	
	requires com.sun.jna;
	requires com.sun.jna.platform;

	requires com.google.gson;

	requires org.apache.httpcomponents.httpcore;
	requires org.apache.httpcomponents.httpmime;

	requires dd.plist;

	opens consulo.unity3d.usages to consulo.util.xml.serializer;
	opens consulo.unity3d.run to consulo.util.xml.serializer;
}