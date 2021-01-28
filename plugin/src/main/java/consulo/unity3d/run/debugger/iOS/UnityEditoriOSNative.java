package consulo.unity3d.run.debugger.iOS;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.ptr.ByReference;

/**
 * @author VISTALL
 * @since 27/01/2021
 * <p>
 * const string nativeDllOsx = "UnityEditor.iOS.Extensions.Native.dylib";
 * const string nativeDllWin32 = "x86\\UnityEditor.iOS.Extensions.Native.dll";
 * const string nativeDllWin64 = "x86_64\\UnityEditor.iOS.Extensions.Native.dll";
 */
public interface UnityEditoriOSNative extends Library
{
	@Structure.FieldOrder({
			"productId",
			"udid"
	})
	class iOSDevice extends Structure
	{
		public int productId;

		public String udid;

		public iOSDevice(Pointer p)
		{
			super(p);
		}
	}

	class iOSDeviceByReference extends ByReference
	{
		public iOSDeviceByReference()
		{
			super(Native.POINTER_SIZE);
		}

		public iOSDevice getValue()
		{
			return new iOSDevice(getPointer());
		}
	}

	long InitializePlatformSupportNativeLibrary();

	void StartUsbmuxdListenThread();

	void StopUsbmuxdListenThread();

	int UsbmuxdGetDeviceCount();

	boolean UsbmuxdGetDevice(int index, iOSDeviceByReference reference);

	boolean StartIosProxyDelegate(short localPort, short devicePort, String deviceId);

	void StopIosProxyDelegate(short localPort);
}
