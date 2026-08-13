/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.owl.graph.OWLGraphWrapper;
import edu.umich.med.mrc2.datoolbox.gui.owl.graph.io.ParserWrapper;
import edu.umich.med.mrc2.datoolbox.gui.preferences.DatabaseConnectionSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.impl.TaskControllerImpl;
import umich.ms.datatypes.LCMSData;

public final class MRC2ToolBoxCore {

	public static String trackerSoftwareName = "MetIDTracker";
	
	public static String dataDir = "." + File.separator + "data" + File.separator;
	public static final File lockFile = new File(dataDir + File.separator + "app.lock");
	public static String configDir = dataDir + "config" + File.separator;
	public static String logDir = dataDir + "logs" + File.separator;
	public static String iconDir = dataDir + "icons" + File.separator;
	public static String fleTypeIconDir = iconDir + "DF" + File.separator;
	public static String referenceDir = dataDir + "reference" + File.separator;
	public static String qualMethodsDir = dataDir + "qualmethods" + File.separator;
	public static String libraryDir = dataDir + "libraries" + File.separator;
	public static String msSearchDir = dataDir + "mssearch" + File.separator;
	public static String tmpDir = dataDir + "tmp" + File.separator;
	
	private static final String classyFireOntology = dataDir  + "/obo/ChemOnt_2_1.obo";
	private static OWLGraphWrapper  graph = null;
	
	private static MainWindow mainWindow;
	private static TaskControllerImpl taskController;
	private static DataAnalysisProject currentExperiment;
	private static RawDataAnalysisProject activeOfflineRawDataAnalysisExperiment;	
	private static RenjinScriptEngine rScriptEngine;

	private static LIMSUser idTrackerUser;
		
	private static CompositeCacheManager compositeCacheManager;
	public static CacheAccess<Object, Object> msFeatureCache;
	public static CacheAccess<Object, Object> featureChromatogramCache;
	public static CacheAccess<Object, Object> compoundIdCache;
	public static CacheAccess<Object, Object> msmsLibraryCache;
	
	private static Collection<CompoundLibrary>activeMsLibraries;	
	public static String COMPONENT_IDENTIFIER ="COMPONENT_IDENTIFIER";
	private static Map<DataFile, LCMSData>rawDataMap;

	public static DataAnalysisProject getActiveMetabolomicsExperiment() {
		return currentExperiment;
	}

	public static MainWindow getMainWindow() {
		return mainWindow;
	}

	public static TaskControllerImpl getTaskController() {
		return taskController;
	}	
	
	private static final Logger logger= LogManager.getLogger(MRC2ToolBoxCore.class);

	public static void main(String[] args) {
		
		logger.info("Statring the program");
		
		//	Prevent second copy running
		createFileLock();
	
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
	
		initDatabaseConnection();
		
        final SplashScreen splash = SplashScreen.getSplashScreen();
        Graphics2D g = null;
        if (splash != null)
        	g = splash.createGraphics();

        if (g != null) {
            renderSplashFrame(g, "Starting program ");
            splash.update();
        }  
		currentExperiment = null;
		taskController = new TaskControllerImpl();
		taskController.initModule();
		taskController.setMaxRunningThreads(MRC2ToolBoxConfiguration.getMaxThreadNumber());
		rawDataMap = new HashMap<>();
		activeMsLibraries = new TreeSet<>();
	    
		populateDataCashFromDatabase(splash, g);
		
		mainWindow = new MainWindow();
		mainWindow.loadPreferences();
		mainWindow.setVisible(true);
        if (splash != null){
        	try {
				splash.close();
			} catch (IllegalStateException e) {
				//	logger.error("Failed to create GUI!", e);
			}
        }
        mainWindow.showIdTrackerLogin();
	}
	
	public static void startClassyFireOntology() {		
		
	    ClassyFireOntologyLoader ontologyLoader = 
	    		new ClassyFireOntologyLoader();
	    Thread t = new Thread(ontologyLoader);
	    t.start();
	}
	
	private static void createFileLock() {
		
		FileLock lock = null;
		try (FileChannel fc = FileChannel.open(lockFile.toPath(),
	            StandardOpenOption.CREATE,
	            StandardOpenOption.WRITE)){
		    lock = fc.tryLock();
		} catch (IOException e) {
		    logger.error("Failed to create the lock file!", e);
		}
	    if (lock == null) {
	        logger.error("Another instance of the software is already running!");
	        System.exit(1);
	    }
	}
	
	private static void initDatabaseConnection() {
		
		boolean conectionSetupTried = false;
		if(!ConnectionManager.connectionDefined()) {
			conectionSetupTried = true;
			showDatabaseSetup();
		}			
		Connection conn = null;
		try {
			conn = ConnectionManager.getTestConnection();
		} catch (Exception e2) {
			 logger.error("Failed to establish database connection!", e2);
		}
		if(conn == null && !conectionSetupTried)
			showDatabaseSetup();
		
		try {
			conn = ConnectionManager.getConnection();
		} catch (Exception e1) {
			logger.error("Failed to establish database connection!", e1);
		}
		if(conn == null) {
			MessageDialog.showErrorMsg(
					"Database connection can not be established, exiting the program");
			System.exit(1);
		} else {
			try {
				conn.close();
			} catch (SQLException e1) {
				logger.error("Failed to close database connection properly!", e1);
			}
		}
	}
	
	private static void populateDataCashFromDatabase(SplashScreen splash, Graphics2D g) {

		initCacheSysytem();
		
        if (g != null) {
        	renderSplashFrame(g, "                                              ");
        	splash.update();
            renderSplashFrame(g, "Reading adduct data");
            splash.update();
        }
		AdductManager.refreshAdductList();
        if (g != null) {
        	renderSplashFrame(g, "                                              ");
        	splash.update();
            renderSplashFrame(g, "Initializing user interface");
            splash.update();
        }
	}

	public static void shutDown() {

		RawDataManager.releaseAllDataSources();		

		compositeCacheManager.shutDown();
		
		RecentDataManager.saveDataToFile();
		mainWindow.saveApplicationLayout();
		mainWindow.savePreferences();
		mainWindow.dispose();
		
		if(lockFile.exists()) {
			try {
				Files.delete(lockFile.toPath());
			} catch (IOException e) {
				logger.error("Failed to delete lock file!", e);
			}
		}
		ConnectionManager.closeDataSource();
		System.exit(0);
	}
	
	private static void showDatabaseSetup() {
		
		DatabaseConnectionSetupDialog dbConnectionDialog = 
				new DatabaseConnectionSetupDialog();
		
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();    
        int w = dbConnectionDialog.getSize().width;
        int h = dbConnectionDialog.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        dbConnectionDialog.setLocation(x, y);
        dbConnectionDialog.setVisible(true);
	}
	
	public static void setActiveMetabolomicsExperiment(DataAnalysisProject newExperiment) {
		MRC2ToolBoxCore.currentExperiment = newExperiment;
	}

    static void renderSplashFrame(Graphics2D g, String message) {

        g.setComposite(AlphaComposite.Clear);
        g.fillRect(30,180,300,40);
        g.setPaintMode();
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.BLUE);
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g.drawString(message + " ...", 50, 215);
        
        g.setColor(Color.BLACK);
        g.drawString(BuildInformation.getVersionAndBuildDate(), 115, 100);
    }
    
    private static class ClassyFireOntologyLoader implements Runnable {

        private ClassyFireOntologyLoader() {

        }

        public void run() {
        	
    		ParserWrapper pw = new ParserWrapper();	
    		try {
    			graph = pw.parseToOWLGraph(classyFireOntology);
    		} catch (OWLOntologyCreationException e) {
    			logger.error("Failed to parse compound ontology file!", e);
    		} catch (IOException e) {
    			logger.error("Failed to read compound ontology file!", e);
    		}
        }
    }
    
	public static OWLGraphWrapper getClassyFireOntologyGraph() {
		
		if(graph == null)
			startClassyFireOntology();
			
		return graph;
	}

	/**
	 * @return the rScriptEngine
	 */
	public static RenjinScriptEngine getrScriptEngine() {

		if(rScriptEngine == null) {

			RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
			rScriptEngine = factory.getScriptEngine();
		}
		return rScriptEngine;
	}

	public static Map<DataFile, LCMSData> getRawDataMap() {
		return rawDataMap;
	}

	public static LCMSData getRawData(DataFile file) {
		return rawDataMap.get(file);
	}

	public static void removeDataSource(DataFile file) {
		rawDataMap.remove(file);
	}

	public static void addRawData(DataFile file, LCMSData data) {

		if(currentExperiment == null)
			return;

		rawDataMap.put(file, data);
	}

	/**
	 * @return the idTrackerUser
	 */
	public static LIMSUser getIdTrackerUser() {
		return idTrackerUser;
	}

	/**
	 * @param idTrackerUser the idTrackerUser to set
	 */
	public static void setIdTrackerUser(LIMSUser idTrackerUser) {
		MRC2ToolBoxCore.idTrackerUser = idTrackerUser;
	}

	private static void initCacheSysytem() {
		
		compositeCacheManager = CompositeCacheManager.getUnconfiguredInstance();
		Properties cacheProps = new Properties(); 
		try (FileReader pfr = new FileReader(configDir + "cache.ccf")){

			cacheProps.load(pfr); 
			File tmp = new File(tmpDir);
			cacheProps.put("jcs.auxiliary.DC.attributes.DiskPath", tmp.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Failed to read cache configuration file!", e);
		}
		compositeCacheManager.configure(cacheProps);
		
		msFeatureCache = new CacheAccess<>(compositeCacheManager.getCache("msFeatureCache"));
		msFeatureCache.clear();
			
		featureChromatogramCache = new CacheAccess<>(compositeCacheManager.getCache("featureChromatogramCache"));
		featureChromatogramCache.clear();
		
		compoundIdCache = new CacheAccess<>(compositeCacheManager.getCache("compoundIdCache"));
		compoundIdCache.clear();
		
		msmsLibraryCache = new CacheAccess<>(compositeCacheManager.getCache("msmsLibraryCache"));
		msmsLibraryCache.clear();
	}

	public static RawDataAnalysisProject getActiveOfflineRawDataAnalysisExperiment() {
		return activeOfflineRawDataAnalysisExperiment;
	}

	public static void setActiveOfflineRawDataAnalysisExperiment(
			RawDataAnalysisProject newRawDataAnalysisExperiment) {
		activeOfflineRawDataAnalysisExperiment = newRawDataAnalysisExperiment;
	}

	public static Collection<CompoundLibrary> getActiveMsLibraries() {
		return activeMsLibraries;
	}
}
