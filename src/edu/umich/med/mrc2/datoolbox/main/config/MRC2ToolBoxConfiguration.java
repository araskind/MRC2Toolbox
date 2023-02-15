/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.main.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import edu.umich.med.mrc2.datoolbox.data.enums.DatabseDialect;
import edu.umich.med.mrc2.datoolbox.data.enums.IntensityFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.RetentionUnits;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;

public class MRC2ToolBoxConfiguration {
	
	private static Properties properties;
	private static String PWD_ENCRYPTION_KEY;

	//	Values not subject to change
	public static final String EXPERIMENT_FILE_EXTENSION = "caproject";
	public static final String ID_EXPERIMENT_FILE_EXTENSION = "idproject";
	public static final String RAW_DATA_EXPERIMENT_FILE_EXTENSION = "rdproject";
	public static final String DATA_MATRIX_EXTENSION = "dmat";
	public static final String DATA_EXPORT_DIRECTORY = "exports";
	public static final String RAW_DATA_DIRECTORY = "RawData";
	public static final String TAB_DATA_DELIMITER = "\t";
	
	//	Maybe change to Experiment later, make back-compatible with existing projects
	public static final String UNCOMPRESSED_EXPERIMENT_FILES_DIRECTORY = "ProjectFiles";
	public static final String PROJECT_FILE_NAME = "Project.xml";
	public static final String FEATURE_CHROMATOGRAMS_FILE_NAME = "FeatureChromatograms.xml";

	//	LIMS connection	
	public static String metLimsUser;
	public static String metLimsPassword;
	public static String metLimsHost;
	public static String metLimsSid;

	//	Preferences object
	private static Preferences prefs = 
			Preferences.userRoot().node(MRC2ToolBoxConfiguration.class.getName());

	//	Unified database configuration
	private static final String DATABASE_USER = "databaseUser";
	private static final String DATABASE_PASSWORD = "databasePassword";
	private static final String DATABASE_CONNECTION_STRING = "databaseConnectionString";
	private static final String DATABASE_TYPE = "databaseType";
	private static final String DATABASE_HOST = "databaseHost";
	private static final String DATABASE_NAME_SID = "databaseNameSid";
	private static final String DATABASE_SCHEMA = "databaseSchema";
	private static final String DATABASE_PORT = "databasePort";
	
	//	Experiments directory
	public static final String EXPERIMENTS_DIR = "defaultExperimentsDirectory";
	public static final String EXPERIMENTS_DIR_DEFAULT = 
			"." + File.separator + "data" + File.separator + "experiments";

	//	Data/reports directory
	public static final String DATA_DIR = "defaultDataDirectory";
	public static final String DATA_DIR_DEFAULT = ".";

	//	Thread number
	public static final String MAX_THREADS = "maxThreadNumber";
	public static final int MAX_THREADS_DEFAULT = 6;

    public static final String MZ_FORMAT = "mzFormat";
    public static final String MZ_FORMAT_DEFAULT = "#.####";
    public static final NumberFormat defaultMzFormat = new DecimalFormat(MZ_FORMAT_DEFAULT);

    public static final String RETENTION_UNITS = "retentionUnits";
    public static final String RETENTION_UNITS_DEFAULT = RetentionUnits.MINUTES.getName();

    public static final String RT_FORMAT = "rtFormat";
    public static final String RT_FORMAT_DEFAULT = "#.###";
    public static final NumberFormat defaultRtFormat = new DecimalFormat(RT_FORMAT_DEFAULT);

    public static final String PPM_FORMAT = "ppmFormat";
    public static final String PPM_FORMAT_DEFAULT = "#.#";

    public static final String INTENSITY_FORMAT = "intensityFormat";
    public static final String INTENSITY_FORMAT_DEFAULT = "#,###";

    public static final String INTENSITY_NOTATION = "intensityNotation";
    public static final String INTENSITY_NOTATION_DEFAULT = IntensityFormat.DECIMAL.getName();

    public static final String INTENSITY_DECIMALS = "intensityDecimals";
    public static final Integer INTENSITY_DECIMALS_DEFAULT = 4;

    public static final String SPECTRUM_INTENSITY_FORMAT = "spectrumIntensityFormat";
    public static final String SPECTRUM_INTENSITY_FORMAT_DEFAULT = "#.#";

    public static final String DATE_TIME_FORMAT = "dateTimeFormat";
    public static final String DATE_TIME_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    public static final String FILE_TIMESTAMP_FORMAT = "fileTimeStampFormat";
    public static final String FILE_TIMESTAMP_FORMAT_DEFAULT = "yyyyMMdd_HHmmss";

    public static final String QUAL_AUTOMATION_EXECUTABLE_FILE = "qualAutomationExecutableFile";
    public static final String QUAL_AUTOMATION_EXECUTABLE_FILE_DEFAULT = "";

    public static final String QUAL_XIC_TEMPLATE_FILE = "qualXicTemplateFile";
    public static final String QUAL_XIC_TEMPLATE_FILE_DEFAULT = "";

    public static final String QUAL_XIC_METHOD_DIR = "qualXicMethodDir";
    public static final String QUAL_XIC_METHOD_DIR_DEFAULT = "";

    public static final String XIC_MASS_ACURACY = "xicMassAcuracy";
    public static final Double XIC_MASS_ACURACY_DEFAULT =  40.0d;

    public static final String SAMPLE_ID_MASK = "sampleIdMask";
    public static final String SAMPLE_ID_MASK_DEFAULT =  "S\\d{8}|R\\d{9}|CS\\d{7}|R\\d{8}";

    public static final String SAMPLE_NAME_MASK = "sampleNameMask";
    public static final String SAMPLE_NAME_MASK_DEFAULT =  "";

    //	R and RJava
    public static final String R_BINARY_PATH = "rBinaryPath";
    public static final String R_BINARY_PATH_DEFAULT =  "";

    public static final String R_JAVA_DIR_PATH = "rJavaDirectoryPath";
    public static final String R_JAVA_DIR_PATH_DEFAULT =  "";

    //	NIST MSSEARCH
    public static final String NIST_MSSEARCH_DIR_PATH = "nistMsDirectoryPath";
    public static final String NIST_MSSEARCH_DIR_PATH_DEFAULT =  "";

    public static final String NIST_PEPSEARCH_EXECUTABLE_FILE = "nistPepSearchExecutableFile";
    public static final String NIST_PEPSEARCH_EXECUTABLE_FILE_DEFAULT = "";
    
    //	Raw data repository
    public static final String RAW_DATA_REPOSITORY_DIR_PATH = "RAW_DATA_REPOSITORY_DIR_PATH";
    public static final String RAW_DATA_REPOSITORY_DIR_PATH_DEFAULT = "";
    
    //	Sirius binary location
    public static final String SIRIUS_BINARY_PATH = "SIRIUS_BINARY_PATH";
    public static final String SIRIUS_BINARY_PATH_DEFAULT = "";
    
    //	lib2nist location
    public static final String LIB2NIST_BINARY_PATH = "LIB2NIST_BINARY_PATH";
    public static final String LIB2NIST_BINARY_PATH_DEFAULT = "";
    
    public static final String PERCOLATOR_BINARY_PATH = "PERCOLATOR_BINARY_PATH";
    public static final String PERCOLATOR_BINARY_PATH_DEFAULT = "";

    //	TODO - move to MGF block
    public static final String MASS_ACCURACY = "massAccuracy";
    public static final Double MASS_ACCURACY_DEFAULT = 20.0d;

    public static final String RT_WINDOW = "rtWindow";
    public static final Double RT_WINDOW_DEFAULT = 0.05d;

    /*
     *
     * */
    public static final String CORRELATION_CUTOFF = "correlationCutoff";
    public static final Double CORRELATION_CUTOFF_DEFAULT =  0.5d;

    public static final String FREQUENCY_WEIGHT = "frequencyWeight";
    public static final Double FREQUENCY_WEIGHT_DEFAULT = 40.0d;

    public static final String AREA_WEIGHT = "areaWeight";
    public static final Double AREA_WEIGHT_DEFAULT = 60.0d;

    //	NIST search file names
    public static final String NIST_EXECUTABLE_FILE = "nistms$.exe";
    public static final String NIST_CONFIGURATION_FILE = "nistms.ini";
    public static final String NIST_PRIMARY_LOCATOR_FILE = "AUTOIMP.MSD";
    public static final String NIST_SECONDARY_LOCATOR_FILE = "FILSPEC.FIL";
    public static final String NIST_SEARCH_READY_FILE = "SRCREADY.TXT";
    public static final String NIST_SEARCH_HIT_LIST_FILE = "SRCRESLT.TXT";
    
    // MSCONVERT	    
    public static final String MS_CONVERT_EXECUTABLE_FILE = "msConvertExecutableFile";
    public static final String MS_CONVERT_EXECUTABLE_FILE_DEFAULT = "";
    
    //	Spectrum entropy
    public static final String SPECTRUM_ENTROPY_MASS_ERROR = "SPECTRUM_ENTROPY_MASS_ERROR";
    public static final double SPECTRUM_ENTROPY_MASS_ERROR_DEFAULT = 0.05d;
    
    public static final String SPECTRUM_ENTROPY_MASS_ERROR_TYPE = "SPECTRUM_ENTROPY_MASS_ERROR_TYPE";
    public static final MassErrorType SPECTRUM_ENTROPY_MASS_ERROR_TYPE_DEFAULT = MassErrorType.Da;
    
    public static final String SPECTRUM_ENTROPY_NOISE_CUTOFF = "SPECTRUM_ENTROPY_NOISE_CUTOFF";
    public static final double SPECTRUM_ENTROPY_NOISE_CUTOFF_DEFAULT = 0.01d;
    
    public static final String  ONLINE_HELP_URL = "https://metidtracker.github.io/midtdocs/index.html";

	//	Params
    private static NumberFormat mzFormat;
    private static NumberFormat rtFormat;
    private static RetentionUnits rtUnits;
    private static DecimalFormat ppmFormat;
    private static NumberFormat intensityFormat;
    private static IntensityFormat intensityNotation;
    private static NumberFormat spectrumIntensityFormat;
    private static DateFormat dateTimeFormat;
    private static DateFormat fileTimeStampFormat;
    private static double xicMassAcuracy;
    private static double rtWindow;
	private static double massAccuracy;
	
	public static final DateFormat defaultTimeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static void initConfiguration() {
		
		mzFormat = new DecimalFormat(prefs.get(MZ_FORMAT, MZ_FORMAT_DEFAULT));
		String rtu = prefs.get(RETENTION_UNITS, RETENTION_UNITS_DEFAULT);
		for(RetentionUnits ru : RetentionUnits.values()) {

			if(ru.getName().equals(rtu))
				rtUnits = ru;
		}
		rtFormat = new DecimalFormat(prefs.get(RT_FORMAT, RT_FORMAT_DEFAULT));
		ppmFormat = new DecimalFormat(prefs.get(PPM_FORMAT, PPM_FORMAT_DEFAULT));
		String intNotation = prefs.get(INTENSITY_NOTATION, INTENSITY_NOTATION_DEFAULT);
		for(IntensityFormat inot : IntensityFormat.values()) {

			if(inot.getName().equals(intNotation))
				intensityNotation = inot;
		}
		intensityFormat = new DecimalFormat(prefs.get(INTENSITY_FORMAT, INTENSITY_FORMAT_DEFAULT));
		spectrumIntensityFormat = new DecimalFormat(prefs.get(SPECTRUM_INTENSITY_FORMAT, SPECTRUM_INTENSITY_FORMAT_DEFAULT));
		dateTimeFormat = new SimpleDateFormat(prefs.get(DATE_TIME_FORMAT, DATE_TIME_FORMAT_DEFAULT));
		fileTimeStampFormat = new SimpleDateFormat(prefs.get(FILE_TIMESTAMP_FORMAT, FILE_TIMESTAMP_FORMAT_DEFAULT));
		xicMassAcuracy = prefs.getDouble(XIC_MASS_ACURACY, XIC_MASS_ACURACY_DEFAULT);
		massAccuracy = prefs.getDouble(MASS_ACCURACY, MASS_ACCURACY_DEFAULT);
		rtWindow = prefs.getDouble(RT_WINDOW, RT_WINDOW_DEFAULT);
		
		try {
			properties = getPropertyValues();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(properties != null) {
			
			if(properties.get("PWD_ENCRYPTION_KEY") != null)
				PWD_ENCRYPTION_KEY = (String)properties.get("PWD_ENCRYPTION_KEY");
			
			if(properties.get("metLimsUser") != null)
				metLimsUser = (String)properties.get("metLimsUser");
			
			if(properties.get("metLimsPassword") != null)
				metLimsPassword = (String)properties.get("metLimsPassword");
						
			if(properties.get("metLimsHost") != null)
				metLimsHost = (String)properties.get("metLimsHost");
						
			if(properties.get("metLimsSid") != null)
				metLimsSid = (String)properties.get("metLimsSid");		
		}
	}
	
	public static String getEncryptionkey() {
		return PWD_ENCRYPTION_KEY;
	}
	
	public static String getDatabaseConnectionString() {
		
		String encrypted = prefs.get(DATABASE_CONNECTION_STRING, "");
		String connectionString = "";
		try {
			connectionString = UserUtils.decryptString(encrypted);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return connectionString;
	}
	
	public static void setDatabaseConnectionString(String connectionString)  {
		
		String encrypted = "";
		try {
			encrypted = UserUtils.encryptString(connectionString);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		prefs.put(DATABASE_CONNECTION_STRING, encrypted);
	}

	public static String getDatabaseUserName() {
		
		String encrypted = prefs.get(DATABASE_USER, "");
		String userName = "";
		try {
			userName = UserUtils.decryptString(encrypted);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return userName;
	}
	
	public static void setDatabaseUserName(String userName)  {
		
		String encrypted = "";
		try {
			encrypted = UserUtils.encryptString(userName);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		prefs.put(DATABASE_USER, encrypted);
	}
	
	public static String getDatabasePassword() {
		
		String encrypted = prefs.get(DATABASE_PASSWORD, "");
		String password = "";
		try {
			password = UserUtils.decryptString(encrypted);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return password;
	}
	
	public static void setDatabasePassword(String password)  {
		
		String encrypted = "";
		try {
			encrypted = UserUtils.encryptString(password);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		prefs.put(DATABASE_PASSWORD, encrypted);
	}
	
	public static DatabseDialect getDatabaseType() {
		String dbType =  
				prefs.get(DATABASE_TYPE, DatabseDialect.Oracle.name());
		return DatabseDialect.valueOf(dbType);
	}
	
	public static void setDatabaseType(DatabseDialect dbType) {
		prefs.put(DATABASE_TYPE, dbType.name());
	}
	
	public static String getDatabaseHost() {
		return prefs.get(DATABASE_HOST, "");
	}
	
	public static void setDatabaseHost(String host) {
		prefs.put(DATABASE_HOST, host);
	}
	
	public static String getDatabaseNameSid() {
		return prefs.get(DATABASE_NAME_SID, "");
	}
	
	public static void setDatabaseNameSid(String sid) {
		prefs.put(DATABASE_NAME_SID, sid);
	}
	
	public static String getDatabaseSchema() {
		return prefs.get(DATABASE_SCHEMA, "");
	}
	
	public static void setDatabaseSchema(String schema) {
		prefs.put(DATABASE_SCHEMA, schema);
	}
		
	public static String getDatabasePort() {
		return prefs.get(DATABASE_PORT, "");
	}
	
	public static void setDatabasePort(String sid) {
		prefs.put(DATABASE_PORT, sid);
	}
	
	//	Get values
	public static char getTabDelimiter() {
		return TAB_DATA_DELIMITER.charAt(0);
	}

	public static String getProperty(String property) {
		return prefs.get(property, null);
	}

	/**
	 * @param massAccuracy the massAccuracy to set
	 */
	public static void setMassAccuracy(double massAcc) {
		prefs.putDouble(MASS_ACCURACY, massAcc);
		massAccuracy = massAcc;
	}

	public static void setRtWindow(double rtWindowNew) {
		prefs.putDouble(RT_WINDOW, rtWindowNew);
		rtWindow = prefs.getDouble(RT_WINDOW, RT_WINDOW_DEFAULT);
	}

	public static void setProperty(String property, String value) {
		prefs.put(property, value);
	}

	public static void setQualTemplate(String qualTemplateNew) {
		prefs.put(QUAL_XIC_TEMPLATE_FILE, qualTemplateNew);
	}

	public static void setXicMassAcuracy(double xicMassAcuracyNew) {
		prefs.putDouble(XIC_MASS_ACURACY, xicMassAcuracyNew);
		xicMassAcuracy = prefs.getDouble(XIC_MASS_ACURACY, XIC_MASS_ACURACY_DEFAULT);
	}

	public static void setDateTimeFormat(String dtf) {
		prefs.put(DATE_TIME_FORMAT, dtf);
		dateTimeFormat = new SimpleDateFormat(prefs.get(DATE_TIME_FORMAT, DATE_TIME_FORMAT_DEFAULT));
	}

	public static void setIntensityFormat(String intensityFormatNew) {
		prefs.put(INTENSITY_FORMAT, intensityFormatNew);
		intensityFormat = new DecimalFormat(prefs.get(INTENSITY_FORMAT, INTENSITY_FORMAT_DEFAULT));
	}

	public static void setMzFormat(String mzFormatNew) {
		prefs.put(MZ_FORMAT, mzFormatNew);
		mzFormat = new DecimalFormat(prefs.get(MZ_FORMAT, MZ_FORMAT_DEFAULT));
	}

	public static void setRtFormat(String rtFormatNew) {
		prefs.put(RT_FORMAT, rtFormatNew);
		rtFormat = new DecimalFormat(prefs.get(RT_FORMAT, RT_FORMAT_DEFAULT));
	}

	public static void setSpectrumIntensityFormat(String spectrumIntensityFormatNew) {
		prefs.put(SPECTRUM_INTENSITY_FORMAT, spectrumIntensityFormatNew);
		rtFormat = new DecimalFormat(prefs.get(SPECTRUM_INTENSITY_FORMAT, SPECTRUM_INTENSITY_FORMAT_DEFAULT));
	}

	public static void setPpmFormat(String ppmFormatNew) {
		prefs.put(PPM_FORMAT, ppmFormatNew);
		ppmFormat = new DecimalFormat(prefs.get(PPM_FORMAT, PPM_FORMAT_DEFAULT));
	}

	/**
	 * @return the mzFormat
	 */
	public static NumberFormat getMzFormat() {
		return mzFormat;
	}
	
	/**
	 * @return the rtFormat
	 */
	public static NumberFormat getRtFormat() {
		return rtFormat;
	}

	/**
	 * @return the spectrumIntensityFormat
	 */
	public static NumberFormat getSpectrumIntensityFormat() {
		return spectrumIntensityFormat;
	}

	/**
	 * @return the ppmFormat
	 */
	public static DecimalFormat getPpmFormat() {
		return ppmFormat;
	}

	/**
	 * @return the rtWindow
	 */
	public static double getRtWindow() {
		return rtWindow;
	}

	/**
	 * @return the dateTimeFormat
	 */
	public static DateFormat getDateTimeFormat() {
		return dateTimeFormat;
	}

	/**
	 * @return the fileTimeStampFormat
	 */
	public static DateFormat getFileTimeStampFormat() {
		return fileTimeStampFormat;
	}

	/**
	 * @param fileTimeStampFormat the fileTimeStampFormat to set
	 */
	public static void setFileTimeStampFormat(String newFormat) {
		prefs.put(FILE_TIMESTAMP_FORMAT, newFormat);
		fileTimeStampFormat = new SimpleDateFormat(newFormat);
	}

	/**
	 * @return the intensityFormat
	 */
	public static NumberFormat getIntensityFormat() {
		return intensityFormat;
	}

	/**
	 * @return the maxThreadNumber
	 */
	public static int getMaxThreadNumber() {
		return prefs.getInt(MAX_THREADS, MAX_THREADS_DEFAULT);
	}

	/**
	 * @param maxThreadNumber the maxThreadNumber to set
	 */
	public static void setMaxThreadNumber(int maxNumber) {
		prefs.putInt(MAX_THREADS, maxNumber);
		MRC2ToolBoxCore.getTaskController().setMaxRunningThreads(maxNumber);
	}

	/**
	 * @return the xicMassAcuracy
	 */
	public static double getXicMassAcuracy() {
		return xicMassAcuracy;
	}

	/**
	 * @return the qualTemplate
	 */
	public static String getXicTemplateFile() {
		return prefs.get(QUAL_XIC_TEMPLATE_FILE, QUAL_XIC_TEMPLATE_FILE_DEFAULT);
	}

	/**
	 * @return the prefs
	 */
	public static Preferences getPreferences() {
		return prefs;
	}

	/**
	 * @return the qualAutomationExecutableFile
	 */
	public static String getQualAutomationExecutableFile() {
		return prefs.get(QUAL_AUTOMATION_EXECUTABLE_FILE, QUAL_AUTOMATION_EXECUTABLE_FILE_DEFAULT);
	}

	/**
	 * @param qualAutomationExecutableFile the qualAutomationExecutableFile to set
	 */
	public static void setQualAutomationExecutableFile(String qaExecutableFile) {
		prefs.put(QUAL_AUTOMATION_EXECUTABLE_FILE, qaExecutableFile);
	}
	
	public static String getMsConvertExecutableFile() {
		return prefs.get(MS_CONVERT_EXECUTABLE_FILE, MS_CONVERT_EXECUTABLE_FILE_DEFAULT);
	}

	/**
	 * @param qualAutomationExecutableFile the qualAutomationExecutableFile to set
	 */
	public static void setMsConvertExecutableFile(String mscExecutableFile) {
		prefs.put(MS_CONVERT_EXECUTABLE_FILE, mscExecutableFile);
	}

	/**
	 * @return the rtUnits
	 */
	public static RetentionUnits getRtUnits() {
		return rtUnits;
	}

	/**
	 * @param rtUnits the rtUnits to set
	 */
	public static void setRtUnits(RetentionUnits rtu) {

		for(RetentionUnits ru : RetentionUnits.values()) {

			if(ru.equals(rtu)) {
				prefs.put(RETENTION_UNITS, ru.getName());
				rtUnits = rtu;
			}
		}
	}

	/**
	 * @return the intensityNotation
	 */
	public static IntensityFormat getIntensityNotation() {
		return intensityNotation;
	}

	/**
	 * @param intensityNotation the intensityNotation to set
	 */
	public static void setIntensityNotation(IntensityFormat inotn) {

		for(IntensityFormat inot : IntensityFormat.values()) {

			if(inot.equals(inotn)) {

				prefs.put(INTENSITY_NOTATION, inot.getName());
				intensityNotation = inotn;
			}
		}
	}

	/**
	 * @return the intensityDecimals
	 */
	public static int getIntensityDecimals() {
		return prefs.getInt(INTENSITY_DECIMALS, INTENSITY_DECIMALS_DEFAULT);
	}

	/**
	 * @param intensityDecimals the intensityDecimals to set
	 */
	public static void setIntensityDecimals(int intDecimals) {
		prefs.putInt(INTENSITY_DECIMALS, intDecimals);
	}

	public static String getQualXicMethodDir() {
		return prefs.get(QUAL_XIC_METHOD_DIR, QUAL_XIC_METHOD_DIR_DEFAULT);
	}

	public static void setQualXicMethodDir(String xicMethodDir) {
		prefs.put(QUAL_XIC_METHOD_DIR, xicMethodDir);
	}

	/**
	 * @return the sampleIdMask
	 */
	public static String getSampleIdMask() {
		return prefs.get(SAMPLE_ID_MASK, SAMPLE_ID_MASK_DEFAULT);
	}

	/**
	 * @return the sampleNameMask
	 */
	public static String getSampleNameMask() {
		return prefs.get(SAMPLE_NAME_MASK, SAMPLE_NAME_MASK_DEFAULT);
	}

	/**
	 * @param sampleIdMask the sampleIdMask to set
	 */
	public static void setSampleIdMask(String sidMask) {
		prefs.put(SAMPLE_ID_MASK, sidMask);
	}

	/**
	 * @param sampleNameMask the sampleNameMask to set
	 */
	public static void setSampleNameMask(String snameMask) {
		prefs.put(SAMPLE_NAME_MASK, snameMask);
	}

	public static double getMassAccuracy() {
		return massAccuracy;
	}

	public static String getrBinaryPath() {
		return prefs.get(R_BINARY_PATH, R_BINARY_PATH_DEFAULT);
	}

	public static String getrNistMsDir() {
		return prefs.get(NIST_MSSEARCH_DIR_PATH, NIST_MSSEARCH_DIR_PATH_DEFAULT);
	}

	public static void setNistMsDir(String nistMsDir) {
		prefs.put(NIST_MSSEARCH_DIR_PATH, nistMsDir);
	}

	public static String getNISTPepSearchExecutableFile() {
		return prefs.get(NIST_PEPSEARCH_EXECUTABLE_FILE, NIST_PEPSEARCH_EXECUTABLE_FILE_DEFAULT);
	}

	public static void setNISTPepSearchExecutableFile(String pepSearchExecutableFile) {
		prefs.put(NIST_PEPSEARCH_EXECUTABLE_FILE, pepSearchExecutableFile);
	}

	public static void setrBinaryPath(String rBinaryPath) {
		prefs.put(R_BINARY_PATH, rBinaryPath);
	}

	public static String getrJavaDir() {
		return prefs.get(R_JAVA_DIR_PATH, R_JAVA_DIR_PATH_DEFAULT);
	}

	public static void setrJavaDir(String rJavaDir) {
		prefs.put(R_JAVA_DIR_PATH, rJavaDir);
	}

	public static String getDefaultExperimentsDirectory() {
		return prefs.get(EXPERIMENTS_DIR, EXPERIMENTS_DIR_DEFAULT);
	}

	public static void setDefaultExperimentsDirectory(String defaultExperimentsDirectory) {
		prefs.put(EXPERIMENTS_DIR, defaultExperimentsDirectory);
	}

	public static String getDefaultDataDirectory() {
		return prefs.get(DATA_DIR, DATA_DIR_DEFAULT);
	}

	public static void setDefaultdataDirectory(String defaultDataDirectory) {
		prefs.put(DATA_DIR, defaultDataDirectory);
	}
	
	public static String getRawDataRepository() {
		return prefs.get(RAW_DATA_REPOSITORY_DIR_PATH, RAW_DATA_REPOSITORY_DIR_PATH_DEFAULT);
	}

	public static void setRawDataRepository(String rawDataRepository) {
		prefs.put(RAW_DATA_REPOSITORY_DIR_PATH, rawDataRepository);
	}
	
	public static String getSiriusBinaryPath() {
		return prefs.get(SIRIUS_BINARY_PATH, SIRIUS_BINARY_PATH_DEFAULT);
	}

	public static void setSiriusBinaryPath(String siriusBinaryPath) {
		prefs.put(SIRIUS_BINARY_PATH, siriusBinaryPath);
	}
	
	public static String getLib2NistBinaryPath() {
		return prefs.get(LIB2NIST_BINARY_PATH, LIB2NIST_BINARY_PATH_DEFAULT);
	}

	public static void setLib2NistBinaryPath(String lib2NistBinaryPath) {
		prefs.put(LIB2NIST_BINARY_PATH, lib2NistBinaryPath);
	}
	
	public static String getPercolatorBinaryPath() {
		return prefs.get(PERCOLATOR_BINARY_PATH, PERCOLATOR_BINARY_PATH_DEFAULT);
	}

	public static void setPercolatorBinaryPath(String percolatorBinaryPath) {
		prefs.put(PERCOLATOR_BINARY_PATH, percolatorBinaryPath);
	}
	
	public static double getSpectrumEntropyMassError() {
		return prefs.getDouble(SPECTRUM_ENTROPY_MASS_ERROR, 
				SPECTRUM_ENTROPY_MASS_ERROR_DEFAULT);
	}

	public static void setSpectrumEntropyMassError(double massError) {
		prefs.putDouble(SPECTRUM_ENTROPY_MASS_ERROR, massError);
	}
	
	public static MassErrorType getSpectrumEntropyMassErrorType() {
		return MassErrorType.getTypeByName(
				prefs.get(SPECTRUM_ENTROPY_MASS_ERROR_TYPE, 
						SPECTRUM_ENTROPY_MASS_ERROR_TYPE_DEFAULT.name()));
	}

	public static void setSpectrumEntropyMassErrorType(MassErrorType massErrorType) {
		prefs.put(SPECTRUM_ENTROPY_MASS_ERROR_TYPE, massErrorType.name());
	}
	
	public static double getSpectrumEntropyNoiseCutoff() {
		return prefs.getDouble(SPECTRUM_ENTROPY_NOISE_CUTOFF, 
				SPECTRUM_ENTROPY_NOISE_CUTOFF_DEFAULT);
	}

	public static void setSpectrumEntropyNoiseCutoff(double noiseCutoff) {
		prefs.putDouble(SPECTRUM_ENTROPY_NOISE_CUTOFF, noiseCutoff);
	}
	
	private static Properties getPropertyValues() throws IOException {
		
		Properties prop = new Properties();
		InputStream inputStream = null;
		String configFileName = "config.properties";
		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.IDTRACKER))
			configFileName = "idtracker_config.properties";
		
		try {
			inputStream = MRC2ToolBoxConfiguration.class.getClassLoader().getResourceAsStream(configFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + configFileName + "' not found in the classpath");
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			if(inputStream != null)
				inputStream.close();
		}
		return prop;
	}
}



































