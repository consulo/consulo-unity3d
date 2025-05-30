/*
 * Copyright 2013-2016 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.unity3d.bundle;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.application.util.SystemInfo;
import consulo.content.bundle.SdkType;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.platform.PlatformOperatingSystem;
import consulo.platform.os.WindowsOperatingSystem;
import consulo.ui.image.Image;
import consulo.unity3d.Unity3dIcons;
import consulo.util.lang.SystemProperties;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 28.09.14
 */
@ExtensionImpl
public class Unity3dBundleType extends SdkType {
    private static final Logger LOG = Logger.getInstance(Unity3dBundleType.class);

    public static final String UNKNOWN_VERSION = "0.0.0";

    @Nullable
    public static String getApplicationPath(@Nullable String sdkPath) {
        if (sdkPath == null) {
            return null;
        }
        if (SystemInfo.isMac) {
            return sdkPath + "/Contents/MacOS/Unity";
        }
        else if (SystemInfo.isWindows) {
            return sdkPath + "/Editor/Unity.exe";
        }
        else if (SystemInfo.isLinux) {
            return sdkPath + "/Editor/Unity";
        }
        return null;
    }

    @Nonnull
    public static Unity3dBundleType getInstance() {
        return Application.get().getExtensionPoint(SdkType.class).findExtensionOrFail(Unity3dBundleType.class);
    }

    public Unity3dBundleType() {
        super("UNITY3D");
    }

    @Nullable
    @Override
    public Image getIcon() {
        return Unity3dIcons.Unity3d;
    }

    @Nonnull
    @Override
    public Collection<String> suggestHomePaths() {
        List<String> paths = new ArrayList<>();
        if (SystemInfo.isMac) {
            paths.add("/Applications/Unity/Unity.app");
            File hubPath = new File("/Applications/Unity/Hub/Editor");
            if (hubPath.exists()) {
                for (File versionPath : hubPath.listFiles()) {
                    File unityApp = new File(versionPath, "Unity.app");
                    if (unityApp.exists()) {
                        paths.add(unityApp.getPath());
                    }
                }
            }
        }
        else if (SystemInfo.isWindows) {
            // x64 windows
            paths.add("C:/Program Files (x86)/Unity");
            // x32 windows
            paths.add("C:/Program Files/Unity");
        }
        else if (SystemInfo.isLinux) {
            paths.add("/opt/Unity");

            File unityHub = new File(SystemProperties.getUserHome(), "Unity/Hub/Editor/");
            if (unityHub.exists()) {
                for (File file : unityHub.listFiles()) {
                    if (file.isDirectory()) {
                        paths.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return paths;
    }

    @Override
    public boolean canCreatePredefinedSdks() {
        return true;
    }

    @Override
    public boolean isValidSdkHome(String s) {
        String applicationPath = getApplicationPath(s);
        return applicationPath != null && new File(applicationPath).exists();
    }

    @Nullable
    @Override
    public String getVersionString(String sdkHome) {
        try {
            PlatformOperatingSystem os = Platform.current().os();
            if (os instanceof WindowsOperatingSystem win) {
                return win.getWindowsFileVersion(Path.of(sdkHome, "Editor", "Unity.exe"), 3);
            }
            else if (os.isMac()) {
                NSObject rootObject = PropertyListParser.parse(sdkHome + "/Contents/Info.plist");
                if (rootObject instanceof NSDictionary) {
                    NSString version = (NSString) ((NSDictionary) rootObject).get("CFBundleVersion");
                    assert version != null;
                    return filterReleaseInfo(version.getContent());
                }
            }
            else if (os.isLinux()) {
                // fixme [vistall] maybe something better?
                File packageManagerDir = new File(sdkHome, "/Editor/Data/PackageManager/Unity/PackageManager");
                if (packageManagerDir.exists()) {
                    File[] files = packageManagerDir.listFiles();
                    assert files != null;
                    for (File file : files) {
                        if (file.isDirectory()) {
                            return file.getName();
                        }
                    }
                }

                File modulesFile = new File(sdkHome, "modules.json");
                if (modulesFile.exists()) {
                    File parentFile = modulesFile.getParentFile();
                    return parentFile.getName();
                }
            }
        }
        catch (Exception e) {
            LOG.error(e);
        }
        return UNKNOWN_VERSION;
    }

    /**
     * We need cut full version before 5.0.0f4 after 5.0.0
     */
    public static String filterReleaseInfo(String version) {
        char[] chars = version.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char aChar : chars) {
            if (Character.isDigit(aChar) || aChar == '.') {
                builder.append(aChar);
            }
            else {
                break;
            }
        }
        return builder.toString();
    }

    @Override
    public String suggestSdkName(String s, String sdkHome) {
        return getPresentableName() + " " + getVersionString(sdkHome);
    }

    @Nonnull
    @Override
    public String getPresentableName() {
        return "Unity3D";
    }
}
