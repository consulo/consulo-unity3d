/*
 * Copyright 2013-2015 must-be.org
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

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.io.FileUtil;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * @author VISTALL
 * @since 24.01.15
 */
public class WindowsVersionHelper
{
	public interface Version extends Library
	{

		Version INSTANCE = (Version) Native.loadLibrary("Version", Version.class, W32APIOptions.UNICODE_OPTIONS);

		public int GetFileVersionInfoSizeW(String lptstrFilename, int dwDummy);

		public boolean GetFileVersionInfoW(String lptstrFilename, int dwHandle, int dwLen, Pointer lpData);

		public int VerQueryValueW(Pointer pBlock, String lpSubBlock, PointerByReference lplpBuffer, IntByReference puLen);

	}

	public static class VS_FIXEDFILEINFO extends com.sun.jna.Structure
	{
		private static final List __FIELDS = Arrays.asList(
				"dwSignature",
				"dwStrucVersion",
				"dwFileVersionMS",
				"dwFileVersionLS",
				"dwProductVersionMS",
				"dwProductVersionLS",
				"dwFileFlagsMask",
				"dwFileFlags",
				"dwFileOS",
				"dwFileType",
				"dwFileSubtype",
				"dwFileDateMS",
				"dwFileDateLS"
		);

		public int dwSignature;
		public int dwStrucVersion;
		public int dwFileVersionMS;
		public int dwFileVersionLS;
		public int dwProductVersionMS;
		public int dwProductVersionLS;
		public int dwFileFlagsMask;
		public int dwFileFlags;
		public int dwFileOS;
		public int dwFileType;
		public int dwFileSubtype;
		public int dwFileDateMS;
		public int dwFileDateLS;

		public VS_FIXEDFILEINFO(com.sun.jna.Pointer p)
		{
			super(p);
		}

		@Override
		protected List getFieldOrder()
		{
			return __FIELDS;
		}
	}

	@NotNull
	public static String getVersion(String path) throws Exception
	{
		path = FileUtil.toSystemDependentName(path);
		int dwDummy = 0;
		int versionlength = Version.INSTANCE.GetFileVersionInfoSizeW(path, dwDummy);

		Pointer lpData = new Memory(versionlength);

		PointerByReference lplpBuffer = new PointerByReference();
		IntByReference puLen = new IntByReference();
		Version.INSTANCE.GetFileVersionInfoW(path, 0, versionlength, lpData);
		Version.INSTANCE.VerQueryValueW(lpData, "\\", lplpBuffer, puLen);

		VS_FIXEDFILEINFO lplpBufStructure = new VS_FIXEDFILEINFO(lplpBuffer.getValue());
		lplpBufStructure.read();

		int[] rtnData = new int[4];
		rtnData[0] = lplpBufStructure.dwFileVersionMS >> 16;
		rtnData[1] = lplpBufStructure.dwFileVersionMS & 0xffff;
		rtnData[2] = lplpBufStructure.dwFileVersionLS >> 16;
		rtnData[3] = lplpBufStructure.dwFileVersionLS & 0xffff;
		return rtnData[0] + "." + rtnData[1] + "." + rtnData[2];
	}
}
