/**
 * @author VISTALL
 * @since 2026-09-16
 */
module consulo.unity3d.base {
    // TODO remove in future
    requires java.desktop;
    requires consulo.ui.ex.awt.api;

    requires transitive consulo.ide.api;
    requires transitive consulo.language.api;
    requires transitive consulo.game.framework.api;

    requires transitive consulo.csharp;

    requires consulo.module.ui.api;
    requires consulo.module.creation.api;
    requires consulo.project.ui.api;

    requires consulo.json.api;
    requires consulo.json.jom.api;

    requires com.sun.jna;
    requires com.sun.jna.platform;

    requires org.jetbrains.plugins.yaml;

    requires com.dslplatform.json;

    requires com.google.gson;

    requires dd.plist;

    exports consulo.unity3d.base;
    exports consulo.unity3d.base.asmdef;
    exports consulo.unity3d.base.bundle;
    exports consulo.unity3d.base.csharp.module.extension;
    exports consulo.unity3d.base.editor;
    exports consulo.unity3d.base.fileType;
    exports consulo.unity3d.base.jsonApi;
    exports consulo.unity3d.base.module;
    exports consulo.unity3d.base.module.ui;
    exports consulo.unity3d.base.packages;
    exports consulo.unity3d.base.run;
    exports consulo.unity3d.base.scene;
    exports consulo.unity3d.base.packages.library;
    exports consulo.unity3d.base.packages.orderEntry;

    exports consulo.unity3d.icon;
    exports consulo.unity3d.localize;

    opens consulo.unity3d.base.jsonApi to com.google.gson;
    opens consulo.unity3d.base.editor to com.google.gson;
    opens consulo.unity3d.base.packages to com.google.gson, consulo.application.impl;
}
