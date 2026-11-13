package edu.umich.med.mrc2.datoolbox.main;

public final class BuildInformation {

	public static final String versionNumber = "2";
	public static final String revisionNumber = "2.1.02";
	public static final String timeStamp = "11-11-2026 15:13";
	public static final StartupConfiguration programConfiguration = 
			StartupConfiguration.COMPLETE_TOOLBOX;
	
	private BuildInformation() {
		/* This utility class should not be instantiated */
	}
	
	public static String getProgramName() {
		return programConfiguration.getName() + " Version " + revisionNumber + " (" + timeStamp + ")";
	}
	
	public static String getVersionAndBuildDate() {
		return "Version " + revisionNumber + " (" + timeStamp + ")";
	}
	
	public static StartupConfiguration getStartupConfiguration() {
		return programConfiguration;
	}
}