/**
 * @author VISTALL
 * @since 2026-09-16
 */
module consulo.unity3d.importing {
    // TODO remove in future
    requires java.desktop;
    requires consulo.ui.ex.awt.api;

    requires transitive consulo.unity3d.base;

    requires consulo.module.ui.api;
    requires consulo.module.creation.api;
    requires consulo.project.ui.api;


    requires org.jetbrains.plugins.yaml;

    requires com.google.gson;

    exports consulo.unity3d.importing;
    exports consulo.unity3d.importing.change;
    exports consulo.unity3d.importing.newImport;
    exports consulo.unity3d.importing.newImport.standardImporter;
    exports consulo.unity3d.importing.ui;

    opens consulo.unity3d.importing to com.google.gson;
    opens consulo.unity3d.importing.newImport to com.google.gson;
}
