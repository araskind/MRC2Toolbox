/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static OWLGraphWrapper graph;
	
	private static MainWindow mainWindow;
	private static TaskControllerImpl taskController;
	private static DataAnalysisProject currentExperiment;
	private static RawDataAnalysisProject activeOfflineRawDataAnalysisExperiment;	
	private static RenjinScriptEngine rScriptEngine;

	private static LIMSUser idTrackerUser;
	
	private static CompositeCacheManager compositeCacheManager;
	private static Properties cacheProps;
	public static CacheAccess<Object, Object> msFeatureCache;
	public static CacheAccess<Object, Object> featureChromatogramCache;
	public static CacheAccess<Object, Object> compoundIdCache;
	public static CacheAccess<Object, Object> msmsLibraryCache;
	
	private static Collection<CompoundLibrary>activeMsLibraries;
	
	public static String COMPONENT_IDENTIFIER ="COMPONENT_IDENTIFIER";

	@SuppressWarnings("rawtypes")
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
	
	private static Logger logger;

	public static void main(String[] args) {
		
		//	Prevent second copy running
//		FileLock lock = null;
//		try {
//		    FileChannel fc = FileChannel.open(lockFile.toPath(),
//		            StandardOpenOption.CREATE,
//		            StandardOpenOption.WRITE);
//		    lock = fc.tryLock();
//		} catch (IOException e) {
//		    throw new Error(e);
//		}
//	    if (lock == null) {
//	        System.out.println("Another instance of the software is already running");
//	        System.exit(1);
//	    }
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		
		//	Configure logging
//		File file = new File(configDir + "log4j2.xml");
//		((LoggerContext) LogManager.getContext(false)).setConfigLocation(file.toURI());		
		System.setProperty("logback.configurationFile", 
				MRC2ToolBoxCore.configDir + "logback-config.xml");
		System.setProperty("logback.statusListenerClass", 
				"ch.qos.logback.core.status.OnConsoleStatusListener"); 
		
		
		logger = LoggerFactory.getLogger(MRC2ToolBoxCore.class);
		logger.info("Statring the program");
		
		//	Stop all logs from printing to stdout
//		SysStreamsLogger.bindSystemStreams();
		
		//	Stop stdout printing but keep errors
//		SysStreamsLogger.bindOutputStream();
				
		MRC2ToolBoxConfiguration.initConfiguration();
		boolean conectionSetupTried = false;
		if(!ConnectionManager.connectionDefined()) {
			conectionSetupTried = true;
			showDatabaseSetup();
		}		
		Connection conn = null;
		try {
			conn = ConnectionManager.getTestConnection();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if(conn == null && !conectionSetupTried)
			showDatabaseSetup();
		
		try {
			conn = ConnectionManager.getConnection();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			//	e1.printStackTrace();
		}
		if(conn == null) {
			MessageDialog.showErrorMsg(
					"Database connection can not be established, exiting the program");
			System.exit(1);
		} else {
			try {
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
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
		rawDataMap = new HashMap<DataFile, LCMSData>();
		activeMsLibraries = new TreeSet<CompoundLibrary>();
	
	    ClassyFireOntologyLoader ontologyLoader = 
	    		new ClassyFireOntologyLoader();
	    Thread t = new Thread(ontologyLoader);
	    t.start();
	    
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
        initCacheSysytem();
        
//        if (g != null) {
//        	renderSplashFrame(g, "                                              ");
//        	splash.update();
//            renderSplashFrame(g, "Reading ClasyFire compound ontology");
//            splash.update();
//        }
		//initClassyFireOntology();
		
		mainWindow = new MainWindow();
		mainWindow.loadPreferences();
		mainWindow.setVisible(true);
        if (splash != null){
        	try {
				splash.close();
			} catch (IllegalStateException e) {
				//e.printStackTrace();
			}
        }
        mainWindow.showIdTrackerLogin();
	}

	public static void shutDown() {

		RawDataManager.releaseAllDataSources();		
		try {
			msFeatureCache.clear();
			featureChromatogramCache.clear();		
			compoundIdCache.clear();
			msmsLibraryCache.clear();
		} catch (CacheException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			JCS.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		RecentDataManager.saveDataToFile();
		mainWindow.saveApplicationLayout();
		mainWindow.savePreferences();
		mainWindow.dispose();
		
		if(lockFile != null)
			lockFile.delete();
		
		ConnectionManager.closeDataSource();
		System.gc();
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

        public ClassyFireOntologyLoader() {

        }

        public void run() {
    		ParserWrapper pw = new ParserWrapper();
    		graph = null;		
    		try {
    			graph = pw.parseToOWLGraph(classyFireOntology);
    		} catch (OWLOntologyCreationException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        }
    }
    
	public static OWLGraphWrapper getClassyFireOntologyGraph() {
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
		cacheProps = new Properties(); 
		try {
			FileReader pfr = new FileReader(configDir + "cache.ccf");
			cacheProps.load(pfr); 
			File tmp = new File(tmpDir);
			cacheProps.put("jcs.auxiliary.DC.attributes.DiskPath", tmp.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		compositeCacheManager.configure(cacheProps);
		
		msFeatureCache = JCS.getInstance("msFeatureCache");
		msFeatureCache.clear();
		
		featureChromatogramCache = JCS.getInstance("featureChromatogramCache");
		featureChromatogramCache.clear();
		
		compoundIdCache = JCS.getInstance("compoundIdCache");
		compoundIdCache.clear();
		
		msmsLibraryCache = JCS.getInstance("msmsLibraryCache");
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
