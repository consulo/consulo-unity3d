/**
 * @author VISTALL
 * @since 18-Sep-22
 */
module consulo.unity3d
{
    // TODO remove in future
    requires java.desktop;

    requires consulo.game.framework.api;

    requires consulo.ide.api;

    requires consulo.language.impl;

    requires consulo.unity3d.shaderlab;

    requires consulo.unity3d.cg.shader;

    requires consulo.csharp.psi.impl;
    requires consulo.csharp;

    requires consulo.nunit.api;

    requires consulo.json.api;
    requires consulo.json.jom.api;

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
    opens consulo.unity3d.editor to com.google.gson;
    opens consulo.unity3d.packages to com.google.gson, consulo.application.impl;
    opens consulo.unity3d.jsonApi to com.google.gson;
    opens consulo.unity3d.projectImport.newImport to com.google.gson;
}