package org.mustbe.consulo.unity3d.module;

/**
 * @author VISTALL
 * @since 17.11.14
 */
public enum Unity3dTarget
{
	Windows("Windows (32 bit)", "-buildWindowsPlayer", "$UnityFileName$.exe"),
	Windows64("Windows (64 bit)", "-buildWindows64Player",  "$UnityFileName$.exe"),
	OSX("MacOS (32 bit)", "-buildOSXPlayer", "$UnityFileName$.app"),
	OSX64("MacOS (64 bit)", "-buildOSX64Player", "$UnityFileName$.app"),
	OSXUniversal("MacOS (any)", "-buildOSXUniversalPlayer", "$UnityFileName$.app"),
	Linux32("Linux (32 bit)", "-buildLinux32Player", "$UnityFileName$"),
	Linux64("Linux (64 bit)", "-buildLinux64Player", "$UnityFileName$"),
	LinuxUniversal("Linux (any)", "-buildLinuxUniversalPlayer ", "$UnityFileName$");

	private final String myPresentation;
	private final String myCompilerOption;
	private final String myFileNameTemplate;

	Unity3dTarget(String presentation, String compilerOption, String fileNameTemplate)
	{
		myPresentation = presentation;
		myCompilerOption = compilerOption;
		myFileNameTemplate = fileNameTemplate;
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
