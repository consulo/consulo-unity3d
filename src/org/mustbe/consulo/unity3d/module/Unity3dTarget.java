/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.unity3d.module;

/**
 * @author VISTALL
 * @since 17.11.14
 */
public enum Unity3dTarget
{
	Windows("Windows (32 bit)", "-buildWindowsPlayer", "$UnityFileName$.exe", "UNITY_STANDALONE_WIN"),
	Windows64("Windows (64 bit)", "-buildWindows64Player",  "$UnityFileName$.exe", "UNITY_STANDALONE_WIN"),
	OSX("MacOS (32 bit)", "-buildOSXPlayer", "$UnityFileName$.app", "UNITY_STANDALONE_OSX"),
	OSX64("MacOS (64 bit)", "-buildOSX64Player", "$UnityFileName$.app", "UNITY_STANDALONE_OSX"),
	OSXUniversal("MacOS (any)", "-buildOSXUniversalPlayer", "$UnityFileName$.app", "UNITY_STANDALONE_OSX"),
	Linux32("Linux (32 bit)", "-buildLinux32Player", "$UnityFileName$", "UNITY_STANDALONE_LINUX"),
	Linux64("Linux (64 bit)", "-buildLinux64Player", "$UnityFileName$", "UNITY_STANDALONE_LINUX"),
	LinuxUniversal("Linux (any)", "-buildLinuxUniversalPlayer ", "$UnityFileName$", "UNITY_STANDALONE_LINUX");

	private final String myPresentation;
	private final String myCompilerOption;
	private final String myFileNameTemplate;
	private final String myDefineName;

	Unity3dTarget(String presentation, String compilerOption, String fileNameTemplate, String defineName)
	{
		myPresentation = presentation;
		myCompilerOption = compilerOption;
		myFileNameTemplate = fileNameTemplate;
		myDefineName = defineName;
	}

	public String getDefineName()
	{
		return myDefineName;
	}

	public String getPresentation()
	{
		return myPresentation;
	}

	public String getCompilerOption()
	{
		return myCompilerOption;
	}

	public String getFileNameTemplate()
	{
		return myFileNameTemplate;
	}
}
