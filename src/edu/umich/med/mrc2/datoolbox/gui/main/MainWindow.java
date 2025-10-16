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

package edu.umich.med.mrc2.datoolbox.gui.main;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentPointer;
import edu.umich.med.mrc2.datoolbox.data.ExperimentSwitchController;
import edu.umich.med.mrc2.datoolbox.data.ExperimentSwitchController.ExperimentState;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;
import edu.umich.med.mrc2.datoolbox.dbparse.DbParserFrame;
import edu.umich.med.mrc2.datoolbox.gui.adducts.AdductManagerFrame;
import edu.umich.med.mrc2.datoolbox.gui.assay.AssayManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.datexp.DataExplorerPlotFrame;
import edu.umich.med.mrc2.datoolbox.gui.expsetup.ExperimentSetupDraw;
import edu.umich.med.mrc2.datoolbox.gui.filetools.FileToolsDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.organization.OrganizationManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp.DatabaseExperimentSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.DataExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.NewMetabolomicsExperimentDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.raw.RawDataUploadPrepDialog;
import edu.umich.med.mrc2.datoolbox.gui.lims.METLIMSPanel;
import edu.umich.med.mrc2.datoolbox.gui.mstools.MSToolsFrame;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.preferences.PreferencesDialog;
import edu.umich.med.mrc2.datoolbox.gui.preferences.SmoothingFilterManager;
import edu.umich.med.mrc2.datoolbox.gui.preferences.TableLayoutManager;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExperimentOpenComponent;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.RawDataAnalysisExperimentSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.refsamples.ReferenceSampleManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.users.UserManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.RecentDataManager;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.Project;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskControlListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.gui.TaskProgressPanel;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerExperimentDataFetchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.LoadExperimentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.OpenMetabolomicsProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.OpenStoredRawDataAnalysisExperimentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveExperimentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveMetabolomicsProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveStoredRawDataAnalysisExperimentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ExperimentRawDataFileOpenTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

/**
 * @author araskind
 *
 */
public class MainWindow extends JFrame
		implements ExperimentView, ActionListener, WindowListener,
		ItemListener, TaskListener, TaskControlListener, 
		PersistentLayout, BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = -1562468261440779387L;
	private Preferences preferences;
	public static final String WINDOW_WIDTH = "WINDOW_WIDTH";
	public static final String WINDOW_HEIGTH = "WINDOW_HEIGTH";
	public static final String WINDOW_X = "WINDOW_X";
	public static final String WINDOW_Y = "WINDOW_Y";
	public static final String EXPERIMENT_BASE = "EXPERIMENT_BASE";
	private File experimentBaseDirectory; 

	private static PreferencesDialog preferencesDialog;
	private NewMetabolomicsExperimentDialog newMetabolomicsExperimentDialog;
	private static DbParserFrame dbParserFrame;
	private static MSToolsFrame msToolsFrame;
	private static AdductManagerFrame adductManagerFrame;
	private static AssayManagerDialog assayMethodsManagerDialog;
	private static ReferenceSampleManagerDialog referenceSampleManagerDialog;
	private static TaskProgressPanel progressPanel;
	private static JDialog progressDialogue;
	private static DataExplorerPlotFrame dataExplorerPlotDialog;
	private RawDataUploadPrepDialog rawDataUploadPrepDialog;
	private IdTrackerLoginDialog idtLogin;

	private MainMenuBar mainMenuBar;
	private static ExperimentSetupDraw experimentSetupDraw;
	private static LinkedHashMap<PanelList, DockableMRC2ToolboxPanel> panels;
	private LinkedHashMap<PanelList, Boolean> panelShowing;
	
	private ExperimentSwitchController experimentSwitchController;
	
	private boolean savingAsCopy;

	private DataAnalysisProject currentExperiment;
	private DataPipeline activeDataPipeline;

	private CControl control;
	private CGrid grid;
	public static StatusBar statusBar;

	private DataExportDialog exportDialog;
	private FileToolsDialog fileToolsDialog;
	
	private RawDataAnalysisExperimentSetupDialog rawDataAnalysisExperimentSetupDialog;
	private IdTrackerPasswordActionUnlockDialog confirmActionDialog;

	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "MainWindow.layout");
	private static final Icon scriptIcon = GuiUtils.getIcon("script", 32);

	public MainWindow() throws HeadlessException {

		super(BuildInformation.getProgramName());
		MRC2ToolBoxCore.getTaskController().addTaskControlListener(this);
		initWindow();

		currentExperiment = null;
		activeDataPipeline = null;
		experimentSwitchController = new ExperimentSwitchController();
		experimentSetupDraw.switchDataPipeline(null, null);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		// Handle tabs show / hide
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet()) {

			if (entry.getKey().getName().equals(command)) {

				AbstractButton aButton = (AbstractButton) event.getSource();
				boolean selected = aButton.getModel().isSelected();
				togglePanel(entry.getKey(), selected);
			}
		}
		if (command.equals(MainActionCommands.SAVE_EXPERIMENT_COMMAND.getName()))
			saveExperimentAndContinue();

		if (command.equals(MainActionCommands.SAVE_EXPERIMENT_COPY_COMMAND.getName()))
			saveExperimentCopy();

		if (command.equals(MainActionCommands.CLOSE_EXPERIMENT_COMMAND.getName()))
			closeExperiment();

		if (command.equals(MainActionCommands.NEW_METABOLOMICS_EXPERIMENT_COMMAND.getName()))
			showNewExperimentDialog(ProjectType.DATA_ANALYSIS, null);
		
		if (command.equals(MainActionCommands.NEW_METABOLOMICS_XML_EXPERIMENT_COMMAND.getName()))
			showNewExperimentDialog(ProjectType.DATA_ANALYSIS_NEW_FORMAT, null);

		if (command.equals(MainActionCommands.CREATE_NEW_METABOLOMICS_EXPERIMENT_COMMAND.getName())
				|| command.equals(MainActionCommands.CREATE_NEW_METABOLOMICS_XML_EXPERIMENT_COMMAND.getName()))
			createNewMetabolomicsExperiment();
		
		if (command.equals(MainActionCommands.NEW_RAW_DATA_EXPERIMENT_SETUP_COMMAND.getName()))
			showNewExperimentDialog(ProjectType.RAW_DATA_ANALYSIS, null);
		
		if (command.equals(MainActionCommands.NEW_RAW_DATA_EXPERIMENT_COMMAND.getName())) 
			createNewRawDataAnalysisExperiment();

		if (command.equals(MainActionCommands.SHOW_IDTRACKER_LOGIN_COMMAND.getName()))
			showIdTrackerLogin();

		if (command.equals(MainActionCommands.IDTRACKER_LOGIN_COMMAND.getName()))
			loginIdTracker();

		if (command.equals(MainActionCommands.IDTRACKER_LOGOUT_COMMAND.getName()))
			logoutIdTracker();

		if (command.equals(MainActionCommands.OPEN_METABOLOMICS_EXPERIMENT_COMMAND.getName()))
			openExperiment(ProjectType.DATA_ANALYSIS);
		
		if (command.equals(MainActionCommands.OPEN_RAW_DATA_EXPERIMENT_COMMAND.getName()))
			openExperiment(ProjectType.RAW_DATA_ANALYSIS);
		
		if (command.equals(MainActionCommands.GO_TO_EXPERIMENT_FOLDER_COMMAND.getName()))
			goToExperimentFolder();
		
		if (command.equals(MainActionCommands.OPEN_RAW_DATA_EXPERIMENT_FROM_DATABASE_COMMAND.getName()))
			openRawDataExperimentFromDatabase();
		
		if (command.equals(MainActionCommands.NEW_METABOLOMICS_XML_EXPERIMENT_COMMAND.getName())) {
			
		}
		if (command.equals(MainActionCommands.OPEN_METABOLOMICS_XML_EXPERIMENT_COMMAND.getName()))
			openExperiment(ProjectType.DATA_ANALYSIS_NEW_FORMAT);
		
		if (command.equals(MainActionCommands.SAVE_AS_XML_EXPERIMENT_COMMAND.getName()))
			saveMetabolomicsExperimentInNewFormat();
		
		if (command.equals(MainActionCommands.SHOW_MS_TOOLBOX_COMMAND.getName()))
			showMsTools();

		if (command.equals(MainActionCommands.SHOW_CHEM_MOD_EDITOR_COMMAND.getName()))
			showAdductManager();
		
		if (command.equals(MainActionCommands.SHOW_ASSAY_METHOD_MANAGER_COMMAND.getName()))
			showAssayMethodsManagerDialog();

		if (command.equals(MainActionCommands.SHOW_REFERENCE_SAMPLE_MANAGER_COMMAND.getName()))
			showReferenceSampleManagerDialog();

		if (command.equals(MainActionCommands.EXPORT_RESULTS_COMMAND.getName()))
			exportAnalysisResults(null);

		if (DataExportDialog.getExportTypeByName(command) != null)
			exportAnalysisResults(command);

//		if (command.equals(MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND.getName()))
//			exportAnalysisResults(MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND);
//
//		if (command.equals(MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND.getName()))
//			exportAnalysisResults(MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND);
//
//		if (command.equals(MainActionCommands.EXPORT_RESULTS_4METAB_COMBINER_COMMAND.getName()))
//			exportAnalysisResults(MainActionCommands.EXPORT_RESULTS_4METAB_COMBINER_COMMAND);
		
		if (command.equals(MainActionCommands.SHOW_RAWA_DATA_UPLOAD_PREP_DIALOG.getName()))
			showRawDataUploadPrepDialog();

		if (command.equals(MainActionCommands.EDIT_PREFERENCES_COMMAND.getName()))
			editPreferences();

		if (command.equals(MainActionCommands.SHOW_HELP_COMMAND.getName()))
			showHelp();

		if (command.equals(MainActionCommands.EXIT_COMMAND.getName()))
			exitProgram();
		
		if(command.equals(MainActionCommands.SHOW_USER_MANAGER_COMMAND.getName()))
			showUserManager();
		
		if(command.equals(MainActionCommands.SHOW_ORGANIZATION_MANAGER_COMMAND.getName()))
			showOrganizationManager();
		
		if(command.equals(MainActionCommands.ABOUT_BOX_COMMAND.getName()))
			showAboutSoftwareBox();
		
		if(command.equals(MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND.getName()))
			showFileToolsDialog();
		
		if(command.equals(MainActionCommands.SHOW_WEB_HELP_COMMAND.getName()))
			showOnlineHelp();
		
		if(command.equals(MainActionCommands.CLEAR_RECENT_EXPERIMENTS_COMMAND.getName())
				|| command.equals(MainActionCommands.CLEAR_RECENT_FEATURE_COLLECTIONS_COMMAND.getName())
				|| command.equals(MainActionCommands.CLEAR_RECENT_FEATURE_CLUSTER_DATA_SETS_COMMAND.getName())) {
			clearRecentObjects(command);
		}
		if(command.startsWith(MainActionCommands.OPEN_RECENT_FEATURE_COLLECTION_COMMAND.name()))
			loadRecentFeatureCollection(command);
		
		if(command.startsWith(MainActionCommands.OPEN_RECENT_FEATURE_CLUSTER_DATA_SET_COMMAND.name()))
			loadRecentFeatureClusterDataSet(command);
		
		if(command.startsWith(MainActionCommands.OPEN_RECENT_METABOLOMICS_EXPERIMENT_COMMAND.name())
				|| command.startsWith(MainActionCommands.OPEN_RECENT_IDTRACKER_EXPERIMENT_COMMAND.name())
				|| command.startsWith(MainActionCommands.OPEN_RECENT_OFFLINE_RAW_DATA_EXPERIMENT_COMMAND.name())) {
			openRecentExperiment(command);
		}
	}
	
	private void openRecentExperiment(String command) {

		String[]parts = command.split("\\|");
		String expId = parts[parts.length - 1];
		ExperimentPointer ep = 
				RecentDataManager.getRecentExperimentById(expId);
		if(ep == null) {
			MessageDialog.showErrorMsg(
					"Requested experiment not found", this.getContentPane());
			return;
		}
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null) {
			
			if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getId().equals(expId))
				return;
			else {
				MessageDialog.showWarningMsg(
						"Please close the active metabolomics experiment\n\"" +
						MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getName() + 
						"\"", this.getContentPane());
				return;
			}
		}
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getId().equals(expId))
				return;
			else {
				MessageDialog.showWarningMsg(
						"Please close the active offline raw data analysis experiment\n\"" +
						MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getName() + 
						"\"", this.getContentPane());
				return;
			}
		}
		if(ep.getProjectType().equals(ProjectType.DATA_ANALYSIS)) {
			
			File experimentFile = ep.getExperimentFile();
			if(!experimentFile.exists()) {
				
				MessageDialog.showErrorMsg(
						"Project file for \"" + ep.getName() + "\" not found at\n"
								+ experimentFile.getAbsolutePath(), this.getContentPane());
				return;
			}
			LoadExperimentTask ltp = new LoadExperimentTask(experimentFile);
			ltp.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(ltp);	
		}
		if(ep.getProjectType().equals(ProjectType.RAW_DATA_ANALYSIS)) {
			
			File experimentFile = ep.getExperimentFile();
			if(!experimentFile.exists()) {
				
				MessageDialog.showErrorMsg(
						"Project file for \"" + ep.getName() + "\" not found at\n"
								+ experimentFile.getAbsolutePath(), this.getContentPane());
				return;
			}
			OpenStoredRawDataAnalysisExperimentTask ltp = 
					new OpenStoredRawDataAnalysisExperimentTask(experimentFile, true);
			ltp.addTaskListener(getPanel(PanelList.RAW_DATA_EXAMINER));
			MRC2ToolBoxCore.getTaskController().addTask(ltp);
		}
		if(ep.getProjectType().equals(ProjectType.ID_TRACKER_DATA_ANALYSIS)) {
			
			LIMSExperiment idTrackerExperiment = IDTDataCache.getExperimentById(expId);
			IDTrackerExperimentDataFetchTask task = 
					new IDTrackerExperimentDataFetchTask(idTrackerExperiment);
			
			MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.ID_WORKBENCH);
			task.addTaskListener(MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH));
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}

	private void loadRecentFeatureClusterDataSet(String command) {
		
		String cdsId = command.replace(
				MainActionCommands.OPEN_RECENT_FEATURE_CLUSTER_DATA_SET_COMMAND.name() + "|", "");

		IMSMSClusterDataSet cds = 
				MSMSClusterDataSetManager.getMSMSClusterDataSetById(cdsId);
		if(cds == null) {
			MessageDialog.showErrorMsg(
					"Requested feature cluster data set not found", this.getContentPane());
			return;
		}
		IDWorkbenchPanel panel = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);		
		panel.loadMSMSClusterDataSet(cds);
	}

	private void loadRecentFeatureCollection(String command) {

		String fsId = command.replace(
				MainActionCommands.OPEN_RECENT_FEATURE_COLLECTION_COMMAND.name() + "|", "");
		MsFeatureInfoBundleCollection fColl = 
				FeatureCollectionManager.getMsFeatureInfoBundleCollectionById(fsId);
		if(fColl == null) {
			MessageDialog.showErrorMsg(
					"Requested feature collection not found", this.getContentPane());
			return;
		}
		IDWorkbenchPanel panel = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);		
		panel.loadMSMSFeatureInformationBundleCollection(fColl);
	}

	private void clearRecentObjects(String command) {

		if(command.equals(MainActionCommands.CLEAR_RECENT_EXPERIMENTS_COMMAND.getName()))
			RecentDataManager.clearRecentExperiments();
		
		if(command.equals(MainActionCommands.CLEAR_RECENT_FEATURE_COLLECTIONS_COMMAND.getName()))
			RecentDataManager.clearRecentFeatureCollections();
		
		if(command.equals(MainActionCommands.CLEAR_RECENT_FEATURE_CLUSTER_DATA_SETS_COMMAND.getName()))
			RecentDataManager.clearRecentFeatureClusterDataSets();
		
		RecentDataManager.saveDataToFile();
		updateGuiWithRecentData();
	}

	private void saveMetabolomicsExperimentInNewFormat() {
		
		if (currentExperiment == null)
			return;
		
		SaveMetabolomicsProjectTask task = 
				new SaveMetabolomicsProjectTask(currentExperiment);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void goToExperimentFolder() {
		
		File expDir = null;
		if (currentExperiment != null) 			
			expDir = currentExperiment.getExperimentDirectory();
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) 
			expDir = MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getExperimentDirectory();
		
		if(expDir != null && expDir.exists()) {
			
			try {
				Desktop.getDesktop().open(expDir);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void showOnlineHelp() {

		URL url = null;
		try {
			url = new URL(MRC2ToolBoxConfiguration.ONLINE_HELP_URL);			
			try {
				if (Desktop.isDesktopSupported()) 												
					Desktop.getDesktop().browse(url.toURI());
				
			} catch (Exception ex) {
				// ex.printStackTrace();
			}				
		} catch (MalformedURLException e1) {

		}
	}

	private void createNewRawDataAnalysisExperiment() {
		
		Collection<String>errors = validateRawDataAnalysisExperimentSetup();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), 
					rawDataAnalysisExperimentSetupDialog);
			return;
		}				
		experimentBaseDirectory = 
				new File(rawDataAnalysisExperimentSetupDialog.ExperimentLocationPath());	
		savePreferences();
		RawDataAnalysisProject newExperiment = new RawDataAnalysisProject(
				rawDataAnalysisExperimentSetupDialog.getExperimentName(), 
				rawDataAnalysisExperimentSetupDialog.getExperimentDescription(), 
				experimentBaseDirectory,
				MRC2ToolBoxCore.getIdTrackerUser());
		newExperiment.setInstrument(rawDataAnalysisExperimentSetupDialog.getInstrument());
		
		List<DataFile> msmsDataFiles = 
				rawDataAnalysisExperimentSetupDialog.getMSMSDataFiles().stream().
				map(f -> new DataFile(f)).collect(Collectors.toList());		
		newExperiment.addMSMSDataFiles(msmsDataFiles);
		
		List<DataFile> msOneDataFiles = 
				rawDataAnalysisExperimentSetupDialog.getMSOneDataFiles().stream().
				map(f -> new DataFile(f)).collect(Collectors.toList());		
		newExperiment.addMSOneDataFiles(msOneDataFiles);
		
		boolean copyDataToExperiment = 
				rawDataAnalysisExperimentSetupDialog.copyRawDataToExperiment();
		rawDataAnalysisExperimentSetupDialog.dispose();
		
		//	Save experiment file
		ProjectUtils.saveStorableRawDataAnalysisExperiment(newExperiment);
		
		//	Set experiment as active
		MRC2ToolBoxCore.setActiveOfflineRawDataAnalysisExperiment(newExperiment);
		StatusBar.setExperimentName(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getName());
		
		//	Load raw data
		ExperimentRawDataFileOpenTask task = 
				new ExperimentRawDataFileOpenTask(newExperiment, copyDataToExperiment);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
	}
	
	private Collection<String>validateRawDataAnalysisExperimentSetup(){
		
		Collection<String>errors = new ArrayList<String>();
		String name = rawDataAnalysisExperimentSetupDialog.getExperimentName();
		String location = rawDataAnalysisExperimentSetupDialog.ExperimentLocationPath();

		if(name.isEmpty())
			errors.add("Experiment name must be specified");
		
		if(location.isEmpty())		
			errors.add("Experiment location (directory on the disk) must be specified");
		
		if(!location.isEmpty() && !name.isEmpty()) {	

			File experimentDir = Paths.get(location, name).toFile();
			if(experimentDir.exists())
				errors.add("Experiment \"" + name + "\" alredy exists at " + location);
		}		
		if(rawDataAnalysisExperimentSetupDialog.getMSMSDataFiles().isEmpty())
			errors.add("No raw data files added to the experiment");
		
		if(rawDataAnalysisExperimentSetupDialog.getInstrument() == null)
			errors.add("Instrument must be specified");
		
		return errors;
	}
		
	public void showFileToolsDialog() {
		
		fileToolsDialog = new FileToolsDialog();
		fileToolsDialog.setLocationRelativeTo(this.getContentPane());
		fileToolsDialog.setVisible(true);
	}
	
	private void showAboutSoftwareBox() {

		AboutSoftwareDialog absd = new AboutSoftwareDialog();
		absd.setLocationRelativeTo(this.getContentPane());
		absd.setVisible(true);
	}

	private void showUserManager() {

		UserManagerDialog umd = new UserManagerDialog();
		umd.setLocationRelativeTo(this);
		umd.setVisible(true);
	}
	
	private void showOrganizationManager() {
		
		OrganizationManagerDialog omd = new OrganizationManagerDialog();
		omd.setLocationRelativeTo(this);
		omd.setVisible(true);
	}

	private void showRawDataUploadPrepDialog() {

		rawDataUploadPrepDialog = new RawDataUploadPrepDialog();
		rawDataUploadPrepDialog.setLocationRelativeTo(this.getContentPane());
		rawDataUploadPrepDialog.setVisible(true);
	}

	public void showIdTrackerLogin() {

		idtLogin = new IdTrackerLoginDialog(this);
		idtLogin.setLocationRelativeTo(this.getContentPane());
		idtLogin.setVisible(true);
	}

	private void loginIdTracker() {
		
		if (!ConnectionManager.connectionDefined()) {
			MainWindow.displayErrorMessage("Connection error", 
					"Database connection not defined!");
			return;
		}
		String userId =  idtLogin.getUserName();
		String password = idtLogin.getPassword();
		LIMSUser user = null;
		if(userId.trim().isEmpty()) {
			MessageDialog.showErrorMsg("User ID can not be empty!", idtLogin);
			return;
		}
		if(password.trim().isEmpty()) {
			MessageDialog.showErrorMsg("Password can not be empty!", idtLogin);
			return;
		}
		try {
			user = UserUtils.getUserLogon(userId, password);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(user == null) {
			MessageDialog.showErrorMsg("Unable to log in", idtLogin);
			return;
		}
		else {
			MRC2ToolBoxCore.setIdTrackerUser(user);
			MRC2ToolBoxCore.getMainWindow().setIdTrackerUser(user);

			((METLIMSPanel) MRC2ToolBoxCore.getMainWindow().
					getPanel(PanelList.LIMS)).refreshLimsData();
			
			((IDTrackerLimsManagerPanel) MRC2ToolBoxCore.getMainWindow().
					getPanel(PanelList.ID_TRACKER_LIMS)).refreshIdTrackerdata();
		
			idtLogin.dispose();
		}
	}

	public void updateGuiWithRecentData() {

		mainMenuBar.updateGuiWithRecentData();		
		experimentSetupDraw.updateGuiWithRecentData();
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			entry.getValue().updateGuiWithRecentData();
	}

	private void logoutIdTracker() {
		
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null 
				|| MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			MessageDialog.showWarningMsg(
					"Please close the experiment first", 
					this.getContentPane());
			return;
		}

		int result = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to log out from ID tracker?", this.getContentPane());
		
		if(result != JOptionPane.YES_OPTION)
			return;
		
		for(PanelList panelType : PanelList.getPanelListForConfiguration(
				BuildInformation.getStartupConfiguration())) {
			DockableMRC2ToolboxPanel guiPanel = panels.get(panelType);
			if(guiPanel != null)
				guiPanel.clearPanel();
		}	
		MRC2ToolBoxCore.setIdTrackerUser(null);
		mainMenuBar.setIdTrackerUser(null);
	}

	private void showAssayMethodsManagerDialog() {

		assayMethodsManagerDialog = new AssayManagerDialog();
		assayMethodsManagerDialog.setLocationRelativeTo(this);
		assayMethodsManagerDialog.setVisible(true);
	}

	private void showReferenceSampleManagerDialog(){

		referenceSampleManagerDialog = new ReferenceSampleManagerDialog();
		referenceSampleManagerDialog.setLocationRelativeTo(this);
		referenceSampleManagerDialog.setVisible(true);
	}
	
	private void showAdductManager() {
		
		adductManagerFrame = new AdductManagerFrame(); 
		adductManagerFrame.setLocationRelativeTo(this);
		adductManagerFrame.setVisible(true);
	}
	
	public static AdductManagerFrame getAdductManagerFrame() {
		return adductManagerFrame;
	}

	private void showMsTools() {
	
		msToolsFrame.setLocationRelativeTo(this);
		msToolsFrame.setVisible(true);
	}

	private void showDbParser() {

		if(dbParserFrame == null)
			dbParserFrame = new DbParserFrame();

		dbParserFrame.setLocationRelativeTo(this);
		dbParserFrame.setVisible(true);
	}

	@Override
	public void allTasksFinished(boolean atf) {

		if (atf)
			hideProgressDialog();
	}

	public void clearGui(boolean complete) {

		if (complete) {
			panels.get(PanelList.DESIGN).clearPanel();
			experimentSetupDraw.clearPanel();
			this.setTitle(BuildInformation.getProgramName());
			StatusBar.clearExperimentData();
			clearPanel();
		}
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet()) {

			if(complete || (!complete && !entry.getKey().equals(PanelList.DESIGN))) {

				if (entry.getValue() != null)
					((DockableMRC2ToolboxPanel) entry.getValue()).clearPanel();
			}
		}
	}

	@Override
	public void closeExperiment() {
		
		if (currentExperiment == null 
				&& MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			int selectedValue = MessageDialog.showChoiceMsg(
					"Are you sure you want to clear the workspace?",
					this.getContentPane());
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;
			
			if (selectedValue == JOptionPane.YES_OPTION) {
				clearGuiAfterExperimentClosed();
			}
		}
		else {
			String yesNoQuestion = "You are going to close current experiment,"
					+ " do you want to save the results (Yes - save, No - discard)?";
			int selectedValue = MessageDialog.showChooseOrCancelMsg(
					yesNoQuestion, this.getContentPane());
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;

			if (selectedValue == JOptionPane.YES_OPTION) {
				
				ProjectType activeExperimentType = null;
				if(currentExperiment != null)
					activeExperimentType = ProjectType.DATA_ANALYSIS;
				
				if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null)
					activeExperimentType = ProjectType.RAW_DATA_ANALYSIS;
						
				experimentSwitchController = new ExperimentSwitchController(
						true,
						ExperimentState.CLOSING_EXPERIMENT,
						false,
						activeExperimentType, 
						null);			
				saveExperiment();
			}
			else {
				clearGuiAfterExperimentClosed();
			}
		}
	}
	
	public void saveExperiment() {
		
		if(currentExperiment != null) {
			
			saveMetabolomicsExperimentInNewFormat();
//			SaveExperimentTask spt = new SaveExperimentTask(currentExperiment);
//			spt.addTaskListener(this);
//			MRC2ToolBoxCore.getTaskController().addTask(spt);
		}
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			
			SaveStoredRawDataAnalysisExperimentTask task = 
					new SaveStoredRawDataAnalysisExperimentTask(
							MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment());
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}		
	}
	
	private void saveExperimentAndContinue() {
		
		ProjectType activeExperimentType = null;
		if(currentExperiment != null)
			activeExperimentType = ProjectType.DATA_ANALYSIS;
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null)
			activeExperimentType = ProjectType.RAW_DATA_ANALYSIS;
				
		experimentSwitchController = new ExperimentSwitchController(
				true,
				null,
				false,
				activeExperimentType, 
				null);			
		saveExperiment();
	}

	public static void displayErrorMessage(String title, String msg) {

		if(msg == null || msg.isBlank())
			return;

		String wrappedMsg;
		if (msg.contains("\n"))
			wrappedMsg = msg;
		else
			wrappedMsg = TextUtils.wrapText(msg, 80);

		JOptionPane.showMessageDialog(MRC2ToolBoxCore.getMainWindow(), 
				wrappedMsg, title, JOptionPane.ERROR_MESSAGE);
	}

	private void editPreferences() {

		preferencesDialog.setLocationRelativeTo(this);
		preferencesDialog.setVisible(true);
	}

	public void exitProgram() {

		if (currentExperiment != null || MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			
			String yesNoQuestion = "You are going to close current experiment, "
					+ "do you want to save the results (Yes - save, No - discard)?";
			int selectedValue = MessageDialog.showChooseOrCancelMsg(yesNoQuestion);
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;
			
			if (selectedValue == JOptionPane.YES_OPTION) {

				experimentSwitchController = new ExperimentSwitchController(
						true,
						null,
						true,
						null, 
						null);	
				saveExperiment();
			}
			if (selectedValue == JOptionPane.NO_OPTION) {

				if (MessageDialog.showChoiceWithWarningMsg(
						"Are you sure you want to exit?", this.getContentPane()) == JOptionPane.YES_OPTION)
					MRC2ToolBoxCore.shutDown();
			}
		}
		else {
			if (MessageDialog.showChoiceWithWarningMsg(
					"Are you sure you want to exit?", this.getContentPane()) == JOptionPane.YES_OPTION)
				MRC2ToolBoxCore.shutDown();
		}
	}
	
	private void exportAnalysisResults(String command) {
		
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if (currentExperiment == null || currentExperiment.getActiveDataPipeline() == null)
			return;
		
		MainActionCommands exportType = null;
		if(command != null) {
			
			exportType =  DataExportDialog.getExportTypeByName(command);
			if(exportType == null) {
				MessageDialog.showWarningMsg("Invalid export type.", this.getContentPane());
				return;
			}
		}
		exportDialog = new DataExportDialog();
		if(exportType != null)
			exportDialog.setExportType(exportType);
		
		exportDialog.setBaseDirectory(currentExperiment.getExportsDirectory());
		String dsName = currentExperiment.getActiveFeatureSetForDataPipeline(
				currentExperiment.getActiveDataPipeline()).getName();
		exportDialog.setTitle("Export data for " + dsName);
		exportDialog.setLocationRelativeTo(this);
		exportDialog.setVisible(true);
	}

	public DockableMRC2ToolboxPanel getPanel(PanelList panelType) {
		return panels.get(panelType);
	}

	public ExperimentSetupDraw getPreferencesDraw() {
		return experimentSetupDraw;
	}

	public static void hideProgressDialog() {
		progressDialogue.setVisible(false);
	}

	private void initProgressDialog() {

		progressPanel = MRC2ToolBoxCore.getTaskController().getTaskPanel();
		progressDialogue = new JDialog(this, "Task in progress...", ModalityType.APPLICATION_MODAL);
		progressDialogue.setTitle("Operation in progress ...");
		progressDialogue.setSize(new Dimension(600, 150));
		progressDialogue.setPreferredSize(new Dimension(600, 150));
		progressDialogue.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		progressDialogue.getContentPane().setLayout(new BorderLayout());
		progressDialogue.getContentPane().add(progressPanel, BorderLayout.CENTER);
		progressDialogue.setLocationRelativeTo(this);
		progressDialogue.pack();
		progressDialogue.setVisible(false);
	}

	private void initExperimentLoadTask(ProjectType experimentType) {

		JFileChooser chooser = new ImprovedFileChooser();
		chooser.setPreferredSize(new Dimension(800, 640));
		chooser.setCurrentDirectory(experimentBaseDirectory);
		chooser.setDialogTitle("Select project file");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);	
		FileNameExtensionFilter experimentFileFilter = 
				new FileNameExtensionFilter(
						experimentType.getDescription(), 
						experimentType.getExtension());
		chooser.setFileFilter(experimentFileFilter);
		if(experimentType.equals(ProjectType.RAW_DATA_ANALYSIS))		
			chooser.setAccessory(new RawDataExperimentOpenComponent(chooser));			

		Task projectLoadTask = null;
		if (chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION)			
			projectLoadTask = constructProjectLoadTask(experimentType, chooser);
		
		if(projectLoadTask != null) {
			
			TaskListener taskListener = this;
			if(experimentType.equals(ProjectType.RAW_DATA_ANALYSIS))
				taskListener = getPanel(PanelList.RAW_DATA_EXAMINER);
			
			projectLoadTask.addTaskListener(taskListener);			
			savePreferences();
			MRC2ToolBoxCore.getTaskController().addTask(projectLoadTask);
		}
	}
	
	private Task constructProjectLoadTask(
			ProjectType projectType, 
			JFileChooser chooser) {
		
		Task projectLoadTask = null;
		File experimentFile = null;
		String extension = projectType.getExtension();
		File selectedFile = chooser.getSelectedFile();	
		
		if(selectedFile.isDirectory()) {
			List<Path> pfList = FIOUtils.findFilesByExtension(
					Paths.get(selectedFile.getAbsolutePath()), extension);
			if(pfList == null || pfList.isEmpty()) {
				MessageDialog.showWarningMsg(selectedFile.getName() + 
						" is not a valid " + projectType.getDescription(), chooser);
				return null;
			}
			experimentFile = pfList.get(0).toFile();
			experimentBaseDirectory = selectedFile.getParentFile();
		}
		else {
			experimentFile = selectedFile;
			experimentBaseDirectory = experimentFile.getParentFile().getParentFile();
		}
		if(experimentFile != null) {
			
			if(projectType.equals(ProjectType.DATA_ANALYSIS))
				projectLoadTask = new LoadExperimentTask(experimentFile);
			
			if(projectType.equals(ProjectType.DATA_ANALYSIS_NEW_FORMAT))
				projectLoadTask = new OpenMetabolomicsProjectTask(experimentFile);
			
			if(projectType.equals(ProjectType.RAW_DATA_ANALYSIS)) {	
				
				boolean loadResults = true;
				RawDataExperimentOpenComponent rdeoc = 
						(RawDataExperimentOpenComponent)chooser.getAccessory();
				if(rdeoc != null)
					loadResults = rdeoc.loadResults();
					
				projectLoadTask = new OpenStoredRawDataAnalysisExperimentTask(experimentFile, loadResults);	
			}
		}
		return projectLoadTask;
	}

	private synchronized void initWindow() {

		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		setSize(new Dimension(1000, 800));
		setPreferredSize(new Dimension(1000, 800));
		setMinimumSize(new Dimension(400, 400));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(((ImageIcon) scriptIcon).getImage());
		mainMenuBar = new MainMenuBar(this);
		mainMenuBar.setIdTrackerUser(null);
		setJMenuBar(mainMenuBar);

//		toolBar = new MainToolbar(this);
//		getContentPane().add(toolBar, BorderLayout.NORTH);		
		mainMenuBar.setIdTrackerUser(null);
		
		statusBar = new StatusBar();
		getContentPane().add(statusBar, BorderLayout.SOUTH);

		control = new CControl( this );
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);

		grid = new CGrid( control );
		// Initialize panels and panel display flags
		panels = new LinkedHashMap<PanelList, DockableMRC2ToolboxPanel>();
		panelShowing = new LinkedHashMap<PanelList, Boolean>();
		
		try {
			TableLayoutManager.loadLayouts();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SmoothingFilterManager.loadFilterMap();
		
		for(PanelList panelType : PanelList.getPanelListForConfiguration(BuildInformation.getStartupConfiguration())) {
			
			try {
				panels.put(panelType, (DockableMRC2ToolboxPanel) panelType.getPanelClass().getDeclaredConstructor().newInstance());			
				panelShowing.put(panelType, panelType.isVisibleByDefault());
				panels.get(panelType).switchDataPipeline(null, null);
				control.addDockable(panels.get(panelType));
				grid.add( 0, 0, 100, 100, panels.get(panelType));
			}
			catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(BuildInformation.getStartupConfiguration().equals(
				StartupConfiguration.COMPLETE_TOOLBOX))
			experimentSetupDraw = (ExperimentSetupDraw) panels.get(PanelList.EXPERIMENT_SETUP);
		
		for(PanelList panelType : PanelList.getPanelListForConfiguration(
				BuildInformation.getStartupConfiguration()))
			panelShowing.put(panelType, panels.get(panelType).isVisible());

		mainMenuBar.refreshPanelsMenu(panelShowing);

		initProgressDialog();

		preferencesDialog = new PreferencesDialog();
		preferencesDialog.loadPreferences(MRC2ToolBoxConfiguration.getPreferences());
		msToolsFrame = new MSToolsFrame();

		control.getContentArea().deploy( grid );
		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
		addWindowListener(this);
		revalidate();
	}

	@Override
	public void itemStateChanged(ItemEvent event) {

		if (event.getItem() instanceof DataPipeline && event.getStateChange() == ItemEvent.SELECTED) {

			DataPipeline newPipeline = (DataPipeline) event.getItem();
			PanelList activePanel = getActivePanel();
			if(activePanel == null)
				activePanel = PanelList.FEATURE_DATA;

			switchDataPipeline(currentExperiment, newPipeline);
		}
	}

	public PanelList getActivePanel() {

		PanelList active = null;
		if(control.getFocusedCDockable() == null)
			return null;

		for(PanelList type : PanelList.getPanelListForConfiguration(
				BuildInformation.getStartupConfiguration())) {

			if(control.getFocusedCDockable().equals(panels.get(type)))
				return type;
		}
		return active;
	}

	@Override
	public void numberOfWaitingTasksChanged(int numOfTasks) {

		// if(numOfTasks > 0)
		// showProgressWindow();
	}
	
	public void showNewExperimentDialog(
			ProjectType newExperimentType, 
			LIMSExperiment newLimsExperiment) {
		
		if (currentExperiment != null 
				|| MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			
			int selectedValue = 
					MessageDialog.showChooseOrCancelMsg(
							"Save current experiment before creating a new one?", 
							this.getContentPane());
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;
			
			if (selectedValue == JOptionPane.YES_OPTION) {
				
				ProjectType activeExperimentType = ProjectType.DATA_ANALYSIS;
				if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null)
					activeExperimentType = ProjectType.RAW_DATA_ANALYSIS;
				
				experimentSwitchController = new ExperimentSwitchController(
						true,
						ExperimentState.NEW_EXPERIMENT,
						false,
						activeExperimentType, 
						newExperimentType);
				experimentSwitchController.setLimsExperiment(newLimsExperiment);			
				saveExperiment();
			}			
			//	TODO LIMS experiment
			else {
				clearGuiAfterExperimentClosed();
				if(newExperimentType.equals(ProjectType.DATA_ANALYSIS) || 
						newExperimentType.equals(ProjectType.DATA_ANALYSIS_NEW_FORMAT))
					showNewMetabolomicsExperimentDialog(newExperimentType, newLimsExperiment);
												
				if(newExperimentType.equals(ProjectType.RAW_DATA_ANALYSIS)) 
					showNewRawDataAnalysisExperimentDialog();	
			}
		}
		else {
			if(newExperimentType.equals(ProjectType.DATA_ANALYSIS) || 
					newExperimentType.equals(ProjectType.DATA_ANALYSIS_NEW_FORMAT))
				showNewMetabolomicsExperimentDialog(newExperimentType, newLimsExperiment);
											
			if(newExperimentType.equals(ProjectType.RAW_DATA_ANALYSIS)) 
				showNewRawDataAnalysisExperimentDialog();			
		}
	}

	private void openExperiment(ProjectType newExperimentType) {

		if (currentExperiment != null 
				|| MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {

			int selectedValue = 
					MessageDialog.showChooseOrCancelMsg(
							"Save current experiment before opening another one?", 
							this.getContentPane());
			if(selectedValue == JOptionPane.CANCEL_OPTION)
				return;
			
			if (selectedValue == JOptionPane.YES_OPTION) {

				ProjectType activeExperimentType = ProjectType.DATA_ANALYSIS;
				if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null)
					activeExperimentType = ProjectType.RAW_DATA_ANALYSIS;
				
				experimentSwitchController = new ExperimentSwitchController(
						true,
						ExperimentState.EXISTING_EXPERIMENT,
						false,
						activeExperimentType, 
						newExperimentType);
				saveExperiment();
				return;
			}
			if (selectedValue == JOptionPane.NO_OPTION) {

				clearGuiAfterExperimentClosed();
				initExperimentLoadTask(newExperimentType);
				return;
			}
		}
		else
			initExperimentLoadTask(newExperimentType);
	}
	
	private void openRawDataExperimentFromDatabase() {
		
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null) {
			
			MessageDialog.showWarningMsg(
					"Please close the active metabolomics experiment\n\"" +
					MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getName() + 
					"\"", this.getContentPane());
			return;			
		}
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			
			MessageDialog.showWarningMsg(
					"Please close the active offline raw data analysis experiment\n\"" +
					MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getName() + 
					"\"", this.getContentPane());
			return;		
		}		
		DatabaseExperimentSelectorDialog experimentSelectorDialog = 
				new DatabaseExperimentSelectorDialog();
		experimentSelectorDialog.setLocationRelativeTo(this.getContentPane());
		experimentSelectorDialog.setVisible(true);
	}

	public void reloadDesign() {

		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			entry.getValue().reloadDesign();

		experimentSetupDraw.switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	private void saveExperimentCopy() {

		if (currentExperiment != null) {

			saveExperiment();
			savingAsCopy = true;
		}
	}

	private void showHelp() {
		MessageDialog.showInfoMsg(
				MainActionCommands.SHOW_HELP_COMMAND.getName());
	}
	
	private void showNewRawDataAnalysisExperimentDialog() {
			
		rawDataAnalysisExperimentSetupDialog = 
				new RawDataAnalysisExperimentSetupDialog(this);
		rawDataAnalysisExperimentSetupDialog.setLocationRelativeTo(
				this.getContentPane());
		rawDataAnalysisExperimentSetupDialog.setVisible(true);
	}
	
	private void showNewMetabolomicsExperimentDialog(
			ProjectType projectType, 
			LIMSExperiment newLimsExperiment) {
		
		newMetabolomicsExperimentDialog = 
				new NewMetabolomicsExperimentDialog(this, projectType);
		newMetabolomicsExperimentDialog.setLimsExperiment(newLimsExperiment);
		newMetabolomicsExperimentDialog.setLocationRelativeTo(this);
		newMetabolomicsExperimentDialog.setVisible(true);
	}

	public void createNewMetabolomicsExperiment() {
		
		Collection<String>errors = 
				newMetabolomicsExperimentDialog.validateExperimentData();
		if(!errors.isEmpty()){
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), newMetabolomicsExperimentDialog);
			return;
		}
		if(newMetabolomicsExperimentDialog.getProjectType().equals(ProjectType.DATA_ANALYSIS)) {
			
			createNewMetabolomicsExperimentInLegacyFormat(
					newMetabolomicsExperimentDialog.getProjectParentFolder(), 
					newMetabolomicsExperimentDialog.getProjectName(), 
					newMetabolomicsExperimentDialog.getProjectDescription(), 
					newMetabolomicsExperimentDialog.getLimsExperiment());
		}
		if(newMetabolomicsExperimentDialog.getProjectType().equals(ProjectType.DATA_ANALYSIS_NEW_FORMAT)) {
			
			createNewMetabolomicsExperimentInXMLFormat(
					newMetabolomicsExperimentDialog.getProjectParentFolder(), 
					newMetabolomicsExperimentDialog.getProjectName(), 
					newMetabolomicsExperimentDialog.getProjectDescription(), 
					newMetabolomicsExperimentDialog.getLimsExperiment());
		}		
		newMetabolomicsExperimentDialog.dispose();
	}
	
	private void createNewMetabolomicsExperimentInXMLFormat(
			File parentDirectory, 
			String experimentName,
			String experimentDescription, 
			LIMSExperiment limsExperiment) {

		createNewMetabolomicsExperiment(
				parentDirectory, 
				experimentName,
				experimentDescription, 
				limsExperiment);
		saveMetabolomicsExperimentInNewFormat();
	}

	private void createNewMetabolomicsExperimentInLegacyFormat(
			File parentDirectory, 
			String experimentName,
			String experimentDescription, 
			LIMSExperiment limsExperiment) {
		
		createNewMetabolomicsExperiment(
				parentDirectory, 
				experimentName,
				experimentDescription, 
				limsExperiment);
		saveExperiment();
	}
	
	private void createNewMetabolomicsExperiment(
			File parentDirectory, 
			String experimentName,
			String experimentDescription, 
			LIMSExperiment limsExperiment) {
		
		DataAnalysisProject newExperiment = 
				new DataAnalysisProject(experimentName, 
										experimentDescription, 
										parentDirectory);
		if(experimentDescription == null || experimentDescription.isEmpty())
			newExperiment.setDescription(experimentName);

		if(limsExperiment != null) {
			
			if(limsExperiment.getExperimentDesign() != null)
				newExperiment.getExperimentDesign().replaceDesign(
						limsExperiment.getExperimentDesign());
	
			newExperiment.setLimsExperiment(limsExperiment);
			newExperiment.setLimsProject(limsExperiment.getProject());
		}
		MRC2ToolBoxCore.setActiveMetabolomicsExperiment(newExperiment);
		setGuiFromActiveExperiment();

		//	Add design listeners
		currentExperiment.getExperimentDesign().addListener(experimentSetupDraw);
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			currentExperiment.getExperimentDesign().addListener(entry.getValue());
		
		experimentBaseDirectory = parentDirectory;
		savePreferences();

		experimentSwitchController = 
				new ExperimentSwitchController(false, null, false, ProjectType.DATA_ANALYSIS, null);
		
	}
	
	public void showPanel(PanelList panelType) {

		if (!panelShowing.get(panelType)) {

			panels.get(panelType).setVisible(true);
			panelShowing.put(panelType, Boolean.TRUE);
		}
		control.getController().setFocusedDockable((Dockable) panels.get(panelType).intern(), true);
		mainMenuBar.refreshPanelsMenu(panelShowing);
	}

	public void hidePanel(PanelList panelType) {

		if (!panelShowing.get(panelType))
			return;

		panels.get(panelType).setVisible(false);
		panelShowing.put(panelType, Boolean.FALSE);
		mainMenuBar.refreshPanelsMenu(panelShowing);
	}

	public static void showProgressDialog() {

		if (!progressDialogue.isVisible() && !MRC2ToolBoxCore.getTaskController().getTaskQueue().isEmpty()) {

			try {
				progressDialogue.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
				progressDialogue.setVisible(true);
			} catch (Exception e) {

				// e.printStackTrace();
			}
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			// Load experiment
			if (e.getSource().getClass().equals(LoadExperimentTask.class))
				finalizeExperimentLoad((LoadExperimentTask) e.getSource());
			
			if (e.getSource().getClass().equals(OpenMetabolomicsProjectTask.class))
				finalizeMetabolomicsProjectLoad((OpenMetabolomicsProjectTask) e.getSource());
			
			//	Save metabolomics experiment
			if (e.getSource().getClass().equals(SaveExperimentTask.class) )
				finalizeExperimentSave(((SaveExperimentTask)e.getSource()).getExperimentToSave());
			
			if (e.getSource().getClass().equals(SaveMetabolomicsProjectTask.class) )
				finalizeExperimentSave(((SaveMetabolomicsProjectTask)e.getSource()).getProject());
			
			if(e.getSource().getClass().equals(SaveStoredRawDataAnalysisExperimentTask.class))
				finalizeExperimentSave(((SaveStoredRawDataAnalysisExperimentTask)e.getSource()).getExperimentToSave());
							
			if(e.getSource().getClass().equals(ExperimentRawDataFileOpenTask.class))
				finalizeExperimentRawDataLoad((ExperimentRawDataFileOpenTask)e.getSource());
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED) {
			MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
			hideProgressDialog();
		}
	}

	private void finalizeExperimentRawDataLoad(ExperimentRawDataFileOpenTask task) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		
		showPanel(PanelList.RAW_DATA_EXAMINER);
		RawDataExaminerPanel rawDataPanel = 
				(RawDataExaminerPanel)getPanel(PanelList.RAW_DATA_EXAMINER);
		rawDataPanel.finalizeExperimentRawDataLoad(task);
	}
	
	private void finalizeExperimentSave(Project experiment) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		
		if(experiment != null) {
			RecentDataManager.addExperiment(experiment);
			updateGuiWithRecentData();
		}
		if(experimentSwitchController.isExitProgram()) {
			MRC2ToolBoxCore.shutDown();
			return;
		}
		//	If just saving current experiment
		if(experimentSwitchController.getExperimentState() == null)			
			return;
		else	// If opening existing project or creating a new one
			clearGuiAfterExperimentClosed();
		
		if(experimentSwitchController.getExperimentState().equals(
				ExperimentSwitchController.ExperimentState.NEW_EXPERIMENT)) {
			
			showNewExperimentDialog(
					experimentSwitchController.getNewExperimentType(),
					experimentSwitchController.getLimsExperiment());			
		}
		if(experimentSwitchController.getExperimentState().equals(
				ExperimentSwitchController.ExperimentState.EXISTING_EXPERIMENT))
			initExperimentLoadTask(experimentSwitchController.getNewExperimentType());

		//	TODO if(savingAsCopy) {}
	}

	private void finalizeExperimentLoad(LoadExperimentTask eTask) {

		MRC2ToolBoxCore.setActiveMetabolomicsExperiment(eTask.getNewExperiment());				
		setGuiFromActiveExperiment();
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		hideProgressDialog();
		RecentDataManager.addExperiment(MRC2ToolBoxCore.getActiveMetabolomicsExperiment());
		updateGuiWithRecentData();
	}
		
	private void finalizeMetabolomicsProjectLoad(OpenMetabolomicsProjectTask task) {

		MRC2ToolBoxCore.setActiveMetabolomicsExperiment(task.getProject());				
		setGuiFromActiveExperiment();
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		hideProgressDialog();
		RecentDataManager.addExperiment(MRC2ToolBoxCore.getActiveMetabolomicsExperiment());
		updateGuiWithRecentData();
	}
	
	private void clearGuiAfterExperimentClosed() {
		
		MRC2ToolBoxCore.setActiveMetabolomicsExperiment(null);
		switchDataPipeline(null,  null);
		MRC2ToolBoxCore.setActiveOfflineRawDataAnalysisExperiment(null);
		MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.RAW_DATA_EXAMINER).clearPanel();
		MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH).clearPanel();
		RawDataManager.releaseAllDataSources();		
		RecentDataManager.removeFeatureCollection(
				FeatureCollectionManager.activeExperimentFeatureSet);
		setTitle(BuildInformation.getProgramName());
		System.gc();
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline pipeline) {

		currentExperiment = project;
		activeDataPipeline = pipeline;
		if(currentExperiment != null)
			currentExperiment.setActiveDataPipeline(activeDataPipeline);

		experimentSetupDraw.switchDataPipeline(currentExperiment, activeDataPipeline);
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			entry.getValue().switchDataPipeline(currentExperiment, activeDataPipeline);
		
//		toolBar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
		
		if(dataExplorerPlotDialog != null) {
			dataExplorerPlotDialog.clearPanels();
			dataExplorerPlotDialog.setVisible(false);
		}	
		StatusBar.switchDataPipeline(project, pipeline);
	}

	public void switchPanelForDataPipeline(
			DataPipeline pipeline, PanelList activePanel) {

		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		activeDataPipeline = pipeline;
		currentExperiment.setActiveDataPipeline(activeDataPipeline);
		setGuiFromActiveExperiment();

		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			entry.getValue().switchDataPipeline(currentExperiment, activeDataPipeline);

		if(activePanel == null)
			activePanel = getActivePanel();

		// Make sure relevant panels are active
		if(activePanel != null) {

			if(!panelShowing.get(activePanel))
				panelShowing.put(activePanel, Boolean.TRUE);

			showPanel(activePanel);
		}
		experimentSetupDraw.switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	private void togglePanel(PanelList key, boolean selected) {

		if (selected)
			showPanel(key);
		else
			hidePanel(key);
	}

	/**
	 * Update all GUI elements from current project
	 */

	@Override
	public void setGuiFromActiveExperiment() {

		setTitle(BuildInformation.getProgramName());		
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null) {
			setGuiFromMetabolomicsProject();
			return;
		}
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			setGuiFromRawDataAnalysisProject();
			return;
		}
	}
	
	private void setGuiFromRawDataAnalysisProject() {
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {

			this.setTitle(BuildInformation.getProgramName() + " - " + 
					MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getName());
			StatusBar.showRawDataAnalysisExperimentData(
					MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment());;
		}
		//	TODO more
	}
	
	private void setGuiFromMetabolomicsProject() {
		
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		activeDataPipeline = null;
		if(currentExperiment != null) {
			
			activeDataPipeline = currentExperiment.getActiveDataPipeline();
			// Set window title and status bar
			this.setTitle(BuildInformation.getProgramName() + " - " + currentExperiment.getName());
			StatusBar.switchDataPipeline(currentExperiment, activeDataPipeline);
		}
		// Update menu
		mainMenuBar.updateMenuFromExperiment(currentExperiment, activeDataPipeline);

		// Update project information
		experimentSetupDraw.switchDataPipeline(currentExperiment, activeDataPipeline);

		//	TODO this may need updating for new panel display controls
		if (activeDataPipeline != null) {

			panelShowing.put(PanelList.FEATURE_DATA, 
					currentExperiment.dataPipelineHasData(activeDataPipeline));
			panelShowing.put(PanelList.WORKLIST, 
					currentExperiment.acquisitionMethodHasLinkedWorklist(activeDataPipeline.getAcquisitionMethod()));
			panelShowing.put(PanelList.DUPLICATES, 
					currentExperiment.hasDuplicateClusters(activeDataPipeline));
			panelShowing.put(PanelList.CORRELATIONS, 
					currentExperiment.correlationClustersCalculatedForDataPipeline(activeDataPipeline));
		}
		currentExperiment.getExperimentDesign().addListener(experimentSetupDraw);	
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet()) {
			entry.getValue().switchDataPipeline(currentExperiment, activeDataPipeline);
			currentExperiment.getExperimentDesign().addListener(entry.getValue());
			currentExperiment.getExperimentDesign().getDesignSubsets().
				forEach(ss -> ss.addListener(entry.getValue()));			
		}
		showPanel(PanelList.FEATURE_DATA);
	}

	public static DbParserFrame getDbParserFrame() {

		if(dbParserFrame == null)
			dbParserFrame = new DbParserFrame();

		return dbParserFrame;
	}

	@Override
	public void windowActivated(WindowEvent arg0) {

	}

	@Override
	public void windowClosed(WindowEvent arg0) {

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		exitProgram();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {

	}

	@Override
	public void windowIconified(WindowEvent arg0) {

	}
	@Override
	public void windowOpened(WindowEvent arg0) {

	}
	
	@Override
	public synchronized void clearPanel() {

	}

	/**
	 * @return the mzRtPlotDialog
	 */
	public static DataExplorerPlotFrame getDataExplorerPlotDialog() {

		if(dataExplorerPlotDialog == null)
			dataExplorerPlotDialog = new DataExplorerPlotFrame();

		return dataExplorerPlotDialog;
	}

	@Override
	public void loadLayout(File layoutFile) {
		
		if(layoutConfigFile.exists()) {
			try {
				control.readXML(layoutFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//	Update panel visibility map
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			panelShowing.put(entry.getKey(), entry.getValue().isVisible());

		mainMenuBar.refreshPanelsMenu(panelShowing);		
	}

	@Override
	public void saveLayout(File layoutFile) {

		try {
			control.writeXML(layoutFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		TableLayoutManager.saveLayouts();
		SmoothingFilterManager.saveFilterMap();
	}

	public void saveApplicationLayout() {

		for(int i=0; i<control.getCDockableCount(); i++) {

			CDockable uiObject = control.getCDockable(i);
			if(uiObject instanceof PersistentLayout) {

				File layoutFile = ((PersistentLayout)uiObject).getLayoutFile();
				if(layoutFile == null) 
					System.err.println("No layout file for " + ((DefaultCDockable)uiObject).getTitleText());		
				else {
					((PersistentLayout)uiObject).saveLayout(layoutFile);
				}
			}
		}
		saveLayout(layoutConfigFile);
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	public void setIdTrackerUser(LIMSUser user) {
		mainMenuBar.setIdTrackerUser(user);
	}
	
	public static ExperimentSetupDraw getExperimentSetupDraw() {
		return experimentSetupDraw;
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;

		int width = preferences.getInt(WINDOW_WIDTH, 1000);
		int heigh = preferences.getInt(WINDOW_HEIGTH, 800);
		setSize(new Dimension(width, heigh));
		setPreferredSize(new Dimension(width, heigh));		
		int x = preferences.getInt(WINDOW_X, 100);
		int y = preferences.getInt(WINDOW_Y, 100);		
		setLocation(x,y);
		
		experimentBaseDirectory = new File(preferences.get(EXPERIMENT_BASE, 
				MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(MainWindow.class.getName()));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(MainWindow.class.getName());
		preferences.putInt(WINDOW_WIDTH, getWidth());
		preferences.putInt(WINDOW_HEIGTH, getHeight());	
		Point location = getLocation();
		preferences.putInt(WINDOW_X, location.x);
		preferences.putInt(WINDOW_Y, location.y);
		
		preferences.put(EXPERIMENT_BASE, experimentBaseDirectory.getAbsolutePath());
	}

	public void reauthenticateAdminCommand(String command) {
		
		confirmActionDialog = 
				new IdTrackerPasswordActionUnlockDialog(this, command);
		confirmActionDialog.setUser(MRC2ToolBoxCore.getIdTrackerUser());
		confirmActionDialog.setLocationRelativeTo(this.getContentPane());
		confirmActionDialog.setVisible(true);
	}
	
	public void verifyAdminPassword() {
		
		if(confirmActionDialog == null 
				|| !confirmActionDialog.isVisible() 
				|| !confirmActionDialog.isDisplayable())
			return;
				
		LIMSUser currentUser = MRC2ToolBoxCore.getIdTrackerUser();
		if(currentUser == null) {
			
			if(confirmActionDialog != null && confirmActionDialog.isVisible())
				confirmActionDialog.dispose();
			
			MessageDialog.showErrorMsg("Password incorrect!", this.getContentPane());
			return;
		}		
		if(!currentUser.isSuperUser()) {
			
			if(confirmActionDialog != null && confirmActionDialog.isVisible())
				confirmActionDialog.dispose();
			
			MessageDialog.showErrorMsg(
					"You do not have administrative priviledges.", 
					this.getContentPane());
			return;
		}
		LIMSUser user = null;	
		try {
			user = UserUtils.getUserLogon(
					MRC2ToolBoxCore.getIdTrackerUser().getUserName(), 
					confirmActionDialog.getPassword());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(user == null) {
			MessageDialog.showErrorMsg("Password incorrect!", confirmActionDialog);
			return;
		}
		else {	
			String command = confirmActionDialog.getActionCommand2confirm();
			confirmActionDialog.dispose();
			executeAdminCommand(command);
		}
	}
	
	private void executeAdminCommand(String command) {
		// TODO Auto-generated
	}
	
}
























