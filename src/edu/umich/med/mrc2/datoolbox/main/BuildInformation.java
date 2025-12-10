package edu.umich.med.mrc2.datoolbox.main;

public final class BuildInformation {

	public static final String versionNumber = "2";
	public static final String revisionNumber = "2.0.02";
	public static final String timeStamp = "12-09-2025 18:42";
	public static final StartupConfiguration programConfiguration = 
			StartupConfiguration.COMPLETE_TOOLBOX;
	
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