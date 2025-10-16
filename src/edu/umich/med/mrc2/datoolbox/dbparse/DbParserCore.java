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

package edu.umich.med.mrc2.datoolbox.dbparse;

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
import java.util.Properties;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.LoggerFactory;

import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.gui.preferences.DatabaseConnectionSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.impl.DbParserTaskControllerImpl;

public class DbParserCore {
	
	public static String dataDir = "." + File.separator + "data" + File.separator;
	public static final File lockFile = new File(dataDir + File.separator + "CpdDbParserApp.lock");
	public static String configDir = dataDir + "config" + File.separator;
	public static String iconDir = dataDir + "icons" + File.separator;
	public static String fleTypeIconDir = iconDir + "DF" + File.separator;
	public static String tmpDir = dataDir + "tmp" + File.separator;
	
	private static DbParserTaskControllerImpl taskController;
	private static org.slf4j.Logger logger;
	
	private static CompositeCacheManager compositeCacheManager;
	private static Properties cacheProps;
	public static CacheAccess<Object, Object> dbUploadCache;
	
	private static DbParserFrame mainWindow;

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
		File file = new File(configDir + "log4j2.xml");
		((LoggerContext) LogManager.getContext(false)).setConfigLocation(file.toURI());			
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
        initCacheSysytem();
        
		taskController = new DbParserTaskControllerImpl();
		taskController.initModule();
		taskController.setMaxRunningThreads(MRC2ToolBoxConfiguration.getMaxThreadNumber());

		mainWindow = new DbParserFrame();
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(true);
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
		dbUploadCache = JCS.getInstance("dbUploadCache");
		dbUploadCache.clear();
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
	
	public static DbParserTaskControllerImpl getTaskController() {
		return taskController;
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
	
	public static void shutDown() {
		
		if(lockFile != null)
			lockFile.delete();
		
		try {
			dbUploadCache.clear();
		} catch (CacheException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			JCS.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		mainWindow.savePreferences();
		mainWindow.dispose();
		
		ConnectionManager.closeDataSource();
		System.gc();
		System.exit(0);
	}

	public static DbParserFrame getMainWindow() {
		return mainWindow;
	}
}
