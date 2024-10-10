package edu.umich.med.mrc2.datoolbox.main;

public final class BuildInformation {

	public static final String versionNumber = "1";
	public static final String revisionNumber = "1.2.85";
	public static final String timeStamp = "10-01-2024 13:52";
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