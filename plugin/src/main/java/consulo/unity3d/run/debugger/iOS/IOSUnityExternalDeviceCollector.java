package consulo.unity3d.run.debugger.iOS;

import com.sun.jna.Native;
import consulo.application.AccessRule;
import consulo.application.progress.ProgressIndicator;
import consulo.application.util.concurrent.AppExecutorUtil;
import consulo.content.bundle.Sdk;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.unity3d.module.Unity3dModuleExtensionUtil;
import consulo.unity3d.module.Unity3dRootModuleExtension;
import consulo.unity3d.run.debugger.UnityExternalDevice;
import consulo.unity3d.run.debugger.UnityExternalDeviceCollector;
import consulo.util.lang.ObjectUtil;

import jakarta.annotation.Nonnull;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 27/01/2021
 */
public class IOSUnityExternalDeviceCollector implements UnityExternalDeviceCollector
{
	private static final Logger LOG = Logger.getInstance(IOSUnityExternalDeviceCollector.class);

	private final Object myUnityIOsUpdateLock = ObjectUtil.sentinel("unity ios update lock");

	private UnityEditoriOSNative myUnityEditorOSNative;

	private Future<?> myIOsUpdateFuture = CompletableFuture.completedFuture(null);

	@Override
	public void initialize(Project project, @Nonnull ProgressIndicator indicator, @Nonnull Consumer<UnityExternalDevice> consumer)
	{
		indicator.setText("Preparing iOS over USB connection...");

		loadIOsLibrary(project);
	}

	private void loadIOsLibrary(@Nonnull Project project)
	{
		Unity3dRootModuleExtension extension = AccessRule.read(() -> Unity3dModuleExtensionUtil.getRootModuleExtension(project));
		if(extension == null)
		{
			return;
		}

		Sdk sdk = extension.getSdk();
		if(sdk == null)
		{
			return;
		}

		Platform platform = Platform.current();
		File lib = null;

		if(platform.os().isWindows())
		{
			lib = new File(sdk.getHomePath(), "Editor/Data/PlaybackEngines/iOSSupport/x86_64/UnityEditor.iOS.Native.dll");
		}
		else if(platform.os().isLinux())
		{
			lib = new File(sdk.getHomePath(), "Editor/Data/PlaybackEngines/iOSSupport/x86_64/UnityEditor.iOS.Native.so");
		}
		else
		{
			LOG.warn("Unsupported platform: " + platform.os().name());
			return;
		}

		synchronized(myUnityIOsUpdateLock)
		{
			myIOsUpdateFuture.cancel(false);

			try
			{
				UnityEditoriOSNative unityEditoriOSNative = Native.load(lib.getCanonicalPath(), UnityEditoriOSNative.class);

				long returnValue = unityEditoriOSNative.InitializePlatformSupportNativeLibrary();
				
				unityEditoriOSNative.StartUsbmuxdListenThread();

				myUnityEditorOSNative = unityEditoriOSNative;

				myIOsUpdateFuture = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
					synchronized(myUnityIOsUpdateLock)
					{
						if(myUnityEditorOSNative == null)
						{
							return;
						}


						int deviceCount = myUnityEditorOSNative.UsbmuxdGetDeviceCount();

						UnityEditoriOSNative.iOSDeviceByReference reference = new UnityEditoriOSNative.iOSDeviceByReference();
						boolean b = myUnityEditorOSNative.UsbmuxdGetDevice(0, reference);
						// TODO get devices
						System.out.println();
					}
				}, 5, 5, TimeUnit.SECONDS);
			}
			catch(Throwable e)
			{
				LOG.warn(e);
			}
		}
	}

	@Override
	public void dispose()
	{
		synchronized(myUnityIOsUpdateLock)
		{
			myIOsUpdateFuture.cancel(false);

			if(myUnityEditorOSNative != null)
			{
				myUnityEditorOSNative.StopUsbmuxdListenThread();
			}

			Native.unregister(UnityEditoriOSNative.class);
		}
	}
}
