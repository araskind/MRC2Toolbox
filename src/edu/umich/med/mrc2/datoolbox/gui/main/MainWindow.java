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

package edu.umich.med.mrc2.datoolbox.gui.main;

import java.awt.BorderLayout;
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
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ProjectSwitchController;
import edu.umich.med.mrc2.datoolbox.data.ProjectSwitchController.ProjectState;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;
import edu.umich.med.mrc2.datoolbox.gui.adducts.AdductManagerFrame;
import edu.umich.med.mrc2.datoolbox.gui.assay.AssayManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.datexp.DataExplorerPlotFrame;
import edu.umich.med.mrc2.datoolbox.gui.dbparse.DbParserFrame;
import edu.umich.med.mrc2.datoolbox.gui.filetools.FileToolsDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.organization.OrganizationManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.DataExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.NewProjectDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.raw.RawDataUploadPrepDialog;
import edu.umich.med.mrc2.datoolbox.gui.lims.METLIMSPanel;
import edu.umich.med.mrc2.datoolbox.gui.mstools.MSToolsFrame;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.preferences.PreferencesDialog;
import edu.umich.med.mrc2.datoolbox.gui.preferences.SmoothingFilterManager;
import edu.umich.med.mrc2.datoolbox.gui.preferences.TableLlayoutManager;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.ProjectSetupDraw;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataProjectOpenComponent;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.RawDataAnalysisProjectSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.refsamples.ReferenceSampleManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.users.UserManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.CompoundIdentificationProject;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskControlListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.gui.TaskProgressPanel;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.LoadProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.OpenStoredRawDataAnalysisProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveStoredRawDataAnalysisProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ProjectRawDataFileOpenTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

/**
 * @author araskind
 *
 */
public class MainWindow extends JFrame
		implements ProjectView, ActionListener, WindowListener,
		ItemListener, TaskListener, TaskControlListener, PersistentLayout, BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = -1562468261440779387L;
	private Preferences preferences;
	public static final String WINDOW_WIDTH = "WINDOW_WIDTH";
	public static final String WINDOW_HEIGTH = "WINDOW_HEIGTH";
	public static final String WINDOW_X = "WINDOW_X";
	public static final String WINDOW_Y = "WINDOW_Y";
	public static final String PROJECT_BASE = "PROJECT_BASE";
	private File projectBaseDirectory; 

	private static PreferencesDialog preferencesDialog;
	private static NewProjectDialog newProjectFrame;
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
	private static ProjectSetupDraw projectDashBooard;
	private static LinkedHashMap<PanelList, DockableMRC2ToolboxPanel> panels;
	private LinkedHashMap<PanelList, Boolean> panelShowing;
	
	private ProjectSwitchController projectSwitchController;
	
	private boolean savingAsCopy;

	private DataAnalysisProject currentProject;
	private DataPipeline activeDataPipeline;

	private CControl control;
	private CGrid grid;
	public static StatusBar statusBar;

	private DataExportDialog exportDialog;
	private FileToolsDialog fileToolsDialog;
	
	private RawDataAnalysisProjectSetupDialog rawDataAnalysisProjectSetupDialog;
	private IdTrackerPasswordActionUnlockDialog confirmActionDialog;

	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "MainWindow.layout");
	private static final Icon scriptIcon = GuiUtils.getIcon("script", 32);

	public MainWindow() throws HeadlessException {

		super(BuildInformation.getProgramName());
		MRC2ToolBoxCore.getTaskController().addTaskControlListener(this);
		initWindow();

		currentProject = null;
		activeDataPipeline = null;
		projectSwitchController = new ProjectSwitchController();
		
//		saveOnExitRequested = false;
//		showNewMetabolomicProjectDialog = false;
//		showOpenProjectDialog = false;
//		savingAsCopy = false;
		
//		projectDashBooard.setActionListener(this);
		projectDashBooard.switchDataPipeline(null, null);
//		((FeatureDataPanel)getPanel(PanelList.FEATURE_DATA)).setProjectActionListener(this);	
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
		if (command.equals(MainActionCommands.SAVE_PROJECT_COMMAND.getName()))
			saveProjectAndContinue();

		if (command.equals(MainActionCommands.SAVE_PROJECT_COPY_COMMAND.getName()))
			saveProjectCopy();

		if (command.equals(MainActionCommands.CLOSE_PROJECT_COMMAND.getName()))
			closeProject();

		if (command.equals(MainActionCommands.NEW_PROJECT_COMMAND.getName()))
			showNewProjectDialog(ProjectType.DATA_ANALYSIS, null, null);
		
		if (command.equals(MainActionCommands.NEW_RAW_DATA_PROJECT_SETUP_COMMAND.getName()))
			showNewProjectDialog(ProjectType.RAW_DATA_ANALYSIS, null, null);
		
		if (command.equals(MainActionCommands.NEW_RAW_DATA_PROJECT_COMMAND.getName())) 
			createNewRawDataAnalysisProject();

		if (command.equals(MainActionCommands.NEW_CPD_ID_PROJECT_COMMAND.getName()))
			showNewProjectDialog(ProjectType.FEATURE_IDENTIFICATION, null, null);

		if (command.equals(MainActionCommands.SHOW_ID_TRACKER_LOGIN_COMMAND.getName()))
			showIdTrackerLogin();

		if (command.equals(MainActionCommands.LOGIN_TO_ID_TRACKER_COMMAND.getName()))
			loginIdTracker();

		if (command.equals(MainActionCommands.ID_TRACKER_LOGOUT_COMMAND.getName()))
			logoutIdTracker();

		if (command.equals(MainActionCommands.OPEN_PROJECT_COMMAND.getName()))
			openProject(ProjectType.DATA_ANALYSIS);
		
		if (command.equals(MainActionCommands.OPEN_RAW_DATA_PROJECT_COMMAND.getName()))
			openProject(ProjectType.RAW_DATA_ANALYSIS);	

		if (command.equals(MainActionCommands.SHOW_MS_TOOLBOX_COMMAND.getName()))
			showMsTools();

		if (command.equals(MainActionCommands.SHOW_CHEM_MOD_EDITOR_COMMAND.getName()))
			showAdductManager();
		
		if (command.equals(MainActionCommands.SHOW_ASSAY_METHOD_MANAGER_COMMAND.getName()))
			showAssayMethodsManagerDialog();

		if (command.equals(MainActionCommands.SHOW_REFERENCE_SAMPLE_MANAGER_COMMAND.getName()))
			showReferenceSampleManagerDialog();

		if (command.equals(MainActionCommands.EXPORT_RESULTS_COMMAND.getName()))
			exportAnalysisResults();

		if (command.equals(MainActionCommands.EXPORT_RESULTS_4R_COMMAND.getName()))
			exportAnalysisResults(MainActionCommands.EXPORT_RESULTS_4R_COMMAND);

		if (command.equals(MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND.getName()))
			exportAnalysisResults(MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND);

		if (command.equals(MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND.getName()))
			exportAnalysisResults(MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND);

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
	}
	
	private void createNewRawDataAnalysisProject() {
		
		Collection<String>errors = validateRawDataAnalysisProjectSetup();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), 
					rawDataAnalysisProjectSetupDialog);
			return;
		}				
		projectBaseDirectory = 
				new File(rawDataAnalysisProjectSetupDialog.getProjectLocationPath());	
		savePreferences();
		RawDataAnalysisProject newProject = new RawDataAnalysisProject(
				rawDataAnalysisProjectSetupDialog.getProjectName(), 
				rawDataAnalysisProjectSetupDialog.getProjectDescription(), 
				projectBaseDirectory,
				MRC2ToolBoxCore.getIdTrackerUser());
		newProject.setInstrument(rawDataAnalysisProjectSetupDialog.getInstrument());
		
		List<DataFile> msmsDataFiles = 
				rawDataAnalysisProjectSetupDialog.getMSMSDataFiles().stream().
				map(f -> new DataFile(f)).collect(Collectors.toList());		
		newProject.addMSMSDataFiles(msmsDataFiles);
		
		List<DataFile> msOneDataFiles = 
				rawDataAnalysisProjectSetupDialog.getMSOneDataFiles().stream().
				map(f -> new DataFile(f)).collect(Collectors.toList());		
		newProject.addMSOneDataFiles(msOneDataFiles);
		
		boolean copyDataToProject = 
				rawDataAnalysisProjectSetupDialog.copyRawDataToProject();
		rawDataAnalysisProjectSetupDialog.dispose();
		
		//	Save project file
		ProjectUtils.saveStorableRawDataAnalysisProject(newProject);
		
		//	Set project as active
		MRC2ToolBoxCore.setActiveRawDataAnalysisProject(newProject);
		StatusBar.setProjectName(MRC2ToolBoxCore.getActiveRawDataAnalysisProject().getName());
		
		//	 Load raw data
		ProjectRawDataFileOpenTask task = 
				new ProjectRawDataFileOpenTask(newProject, copyDataToProject);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
	}
	
	private Collection<String>validateRawDataAnalysisProjectSetup(){
		
		Collection<String>errors = new ArrayList<String>();
		String name = rawDataAnalysisProjectSetupDialog.getProjectName();
		String location = rawDataAnalysisProjectSetupDialog.getProjectLocationPath();

		if(name.isEmpty())
			errors.add("Project name can not be empty");
		
		if(location.isEmpty())		
			errors.add("Project name can not be empty");
		
		if(!location.isEmpty() && !name.isEmpty()) {	

			File projectDir = Paths.get(location, name).toFile();
			if(projectDir.exists())
				errors.add("Project \"" + name + "\" alredy exists at " + location);
		}		
		if(rawDataAnalysisProjectSetupDialog.getMSMSDataFiles().isEmpty())
			errors.add("No raw data files added to the project");
		
		if(rawDataAnalysisProjectSetupDialog.getInstrument() == null)
			errors.add("Instrument has to be specified");
		
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

			((IDTrackerLimsManagerPanel) MRC2ToolBoxCore.getMainWindow().
					getPanel(PanelList.ID_TRACKER_LIMS)).refreshIdTrackerdata();
			
			((METLIMSPanel) MRC2ToolBoxCore.getMainWindow().
					getPanel(PanelList.LIMS)).refreshLimsData();
			
			idtLogin.dispose();
		}
	}

	private void logoutIdTracker() {
		
		if(MRC2ToolBoxCore.getCurrentProject() != null 
				|| MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {
			MessageDialog.showWarningMsg(
					"Please close the project first", 
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
//		toolBar.setIdTrackerUser(null);
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
			projectDashBooard.clearPanel();
			this.setTitle(BuildInformation.getProgramName());
			StatusBar.clearProjectData();
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
	public void closeProject() {

		if (currentProject != null || MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {

			String yesNoQuestion = "You are going to close current project,"
					+ " do you want to save the results (Yes - save, No - discard)?";
			int selectedValue = MessageDialog.showChooseOrCancelMsg(
					yesNoQuestion, this.getContentPane());
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;

			if (selectedValue == JOptionPane.YES_OPTION) {
				//saveOnCloseRequested = true;
				
				ProjectType activeProjectType = null;
				if(currentProject != null)
					activeProjectType = ProjectType.DATA_ANALYSIS;
				
				if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null)
					activeProjectType = ProjectType.RAW_DATA_ANALYSIS;
						
				projectSwitchController = new ProjectSwitchController(
						true,
						ProjectState.CLOSING_PROJECT,
						false,
						activeProjectType, 
						null);			
				saveProject();
				return;
			}
			else
				clearGuiAfterProjectClosed();
		}
	}
	
	public void saveProject() {
		
		if(currentProject != null) {
			SaveProjectTask spt = new SaveProjectTask(currentProject);
			spt.addTaskListener(MRC2ToolBoxCore.getMainWindow());
			MRC2ToolBoxCore.getTaskController().addTask(spt);
		}
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {
			
			SaveStoredRawDataAnalysisProjectTask task = 
					new SaveStoredRawDataAnalysisProjectTask(
							MRC2ToolBoxCore.getActiveRawDataAnalysisProject());
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}		
	}
	
	private void saveProjectAndContinue() {
		
		ProjectType activeProjectType = null;
		if(currentProject != null)
			activeProjectType = ProjectType.DATA_ANALYSIS;
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null)
			activeProjectType = ProjectType.RAW_DATA_ANALYSIS;
				
		projectSwitchController = new ProjectSwitchController(
				true,
				null,
				false,
				activeProjectType, 
				null);			
		saveProject();
	}

	public static void displayErrorMessage(String title, String msg) {

		assert msg != null;

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

		if (currentProject != null || MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {
			
			String yesNoQuestion = "You are going to close current project, "
					+ "do you want to save the results (Yes - save, No - discard)?";
			int selectedValue = MessageDialog.showChooseOrCancelMsg(yesNoQuestion);
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;
			
			if (selectedValue == JOptionPane.YES_OPTION) {

				projectSwitchController = new ProjectSwitchController(
						true,
						null,
						true,
						null, 
						null);	
				saveProject();
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

	private void exportAnalysisResults(MainActionCommands exportType) {

		currentProject = MRC2ToolBoxCore.getCurrentProject();
		if (currentProject == null || currentProject.getActiveDataPipeline() == null)
			return;

		exportDialog = new DataExportDialog();
		exportDialog.setExportType(exportType);
		exportDialog.setBaseDirectory(currentProject.getExportsDirectory());
		String dsName = currentProject.getActiveFeatureSetForDataPipeline(
				currentProject.getActiveDataPipeline()).getName();
		exportDialog.setTitle("Export data for " + dsName);
		exportDialog.setLocationRelativeTo(this);
		exportDialog.setVisible(true);
	}

	private void exportAnalysisResults() {

		currentProject = MRC2ToolBoxCore.getCurrentProject();
		if (currentProject == null || currentProject.getActiveDataPipeline() == null)
			return;
		
		exportDialog = new DataExportDialog();
		exportDialog.setLocationRelativeTo(this);
		exportDialog.setVisible(true);
	}

	public DockableMRC2ToolboxPanel getPanel(PanelList panelType) {
		return panels.get(panelType);
	}

	public ProjectSetupDraw getPreferencesDraw() {
		return projectDashBooard;
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

	private void initProjectLoadTask(ProjectType projectType) {

		JFileChooser chooser = new ImprovedFileChooser();
		chooser.setPreferredSize(new Dimension(800, 640));
		chooser.setCurrentDirectory(projectBaseDirectory);
		chooser.setDialogTitle("Select project file");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);	
		FileNameExtensionFilter projectFileFilter = null;
		RawDataProjectOpenComponent acc = null;
		boolean loadResults = false;
		if(projectType.equals(ProjectType.DATA_ANALYSIS)) {
			projectFileFilter = new FileNameExtensionFilter("Raw data project files",
					MRC2ToolBoxConfiguration.PROJECT_FILE_EXTENSION);
		}
		if(projectType.equals(ProjectType.RAW_DATA_ANALYSIS)) {			
			projectFileFilter = new FileNameExtensionFilter("Raw data project files",
					MRC2ToolBoxConfiguration.RAW_DATA_PROJECT_FILE_EXTENSION);
			acc = new RawDataProjectOpenComponent(chooser);
			chooser.setAccessory(acc);
			chooser.setSize(800, 640);
		}
		chooser.setFileFilter(projectFileFilter);
		File projectFile = null;
		if (chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
			
			File selectedFile = chooser.getSelectedFile();		
			if(projectType.equals(ProjectType.DATA_ANALYSIS)) {

				if(selectedFile.isDirectory()) {
					List<String> pfList = FIOUtils.findFilesByExtension(
							Paths.get(selectedFile.getAbsolutePath()), 
							MRC2ToolBoxConfiguration.PROJECT_FILE_EXTENSION);
					if(pfList == null || pfList.isEmpty()) {
						MessageDialog.showWarningMsg(selectedFile.getName() + 
								" is not a valid metabolomics project", chooser);
						return;
					}
					projectFile = new File(pfList.get(0));
					projectBaseDirectory = selectedFile.getParentFile();
				}
				else {
					projectFile = selectedFile;
					projectBaseDirectory = projectFile.getParentFile().getParentFile();
				}		
			}
			if(projectType.equals(ProjectType.RAW_DATA_ANALYSIS)) {
				
				loadResults = acc.loadResults();
				
				if(selectedFile.isDirectory()) {
					List<String> pfList = FIOUtils.findFilesByExtension(
							Paths.get(selectedFile.getAbsolutePath()), 
							MRC2ToolBoxConfiguration.RAW_DATA_PROJECT_FILE_EXTENSION);
					if(pfList == null || pfList.isEmpty()) {
						MessageDialog.showWarningMsg(selectedFile.getName() + 
								" is not a valid raw data analysis project", chooser);
						return;
					}
					projectFile = new File(pfList.get(0));
					projectBaseDirectory = selectedFile.getParentFile();
				}
				else {
					projectFile = selectedFile;
					projectBaseDirectory = projectFile.getParentFile().getParentFile();
				}
			}
		}
		if (projectFile == null || !projectFile.exists())
			return;

		savePreferences();
		if(projectType.equals(ProjectType.DATA_ANALYSIS)) {

			LoadProjectTask ltp = new LoadProjectTask(projectFile);
			ltp.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(ltp);			
		}
		if(projectType.equals(ProjectType.RAW_DATA_ANALYSIS)) {
			
			OpenStoredRawDataAnalysisProjectTask ltp = 
					new OpenStoredRawDataAnalysisProjectTask(projectFile, loadResults);
			ltp.addTaskListener(getPanel(PanelList.RAW_DATA_EXAMINER));
			MRC2ToolBoxCore.getTaskController().addTask(ltp);
		}
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
		
		TableLlayoutManager.loadLayouts();
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
		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.COMPLETE_TOOLBOX))
			projectDashBooard = (ProjectSetupDraw) panels.get(PanelList.PROJECT_SETUP);
			
//		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.COMPLETE_TOOLBOX)) {
//			projectDashBooard = new ProjectSetupDraw();
//			grid.add( -25, 0, 25, 100, projectDashBooard);	
//		}		
		for(PanelList panelType : PanelList.getPanelListForConfiguration(BuildInformation.getStartupConfiguration()))
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

			switchDataPipeline(currentProject, newPipeline);
		}
	}

	public PanelList getActivePanel() {

		PanelList active = null;
		if(control.getFocusedCDockable() == null)
			return null;

		for(PanelList type : PanelList.getPanelListForConfiguration(BuildInformation.getStartupConfiguration())) {

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
	
	public void showNewProjectDialog(
			ProjectType newProjectType, 
			ExperimentDesign design,
			LIMSExperiment newLimsExperiment) {
		
		if (currentProject != null || MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {
			
			int selectedValue = 
					MessageDialog.showChooseOrCancelMsg(
							"Save current project before creating a new one?", this.getContentPane());
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;
			
			if (selectedValue == JOptionPane.YES_OPTION) {
				
				ProjectType activeProjectType = ProjectType.DATA_ANALYSIS;
				if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null)
					activeProjectType = ProjectType.RAW_DATA_ANALYSIS;
				
				projectSwitchController = new ProjectSwitchController(
						true,
						ProjectState.NEW_PROJECT,
						false,
						activeProjectType, 
						newProjectType);
				projectSwitchController.setLimsExperiment(newLimsExperiment);			
				saveProject();
			}
			
			//	TODO LIMS experiment
			else {
				clearGuiAfterProjectClosed();
				if(newProjectType.equals(ProjectType.DATA_ANALYSIS))
					showNewMetabolomicsProjectDialog(design, newLimsExperiment);
												
				if(newProjectType.equals(ProjectType.RAW_DATA_ANALYSIS)) 
					showNewRawDataAnalysisProjectDialog();	
			}
		}
		else {
			if(newProjectType.equals(ProjectType.DATA_ANALYSIS))
				showNewMetabolomicsProjectDialog(design, newLimsExperiment);
											
			if(newProjectType.equals(ProjectType.RAW_DATA_ANALYSIS)) 
				showNewRawDataAnalysisProjectDialog();			
		}
	}
	
//	public void showNewProjectFromLimsExperimentDialogue(ProjectType type, LIMSExperiment activeExperiment) {
//
//		newProjectFrame = new NewProjectDialog(this);
//		newProjectFrame.setDesign(null);
//		newProjectFrame.setLimsExperiment(activeExperiment);
//		newProjectFrame.setProjectType(type);
//
//		int selectedValue = JOptionPane.YES_OPTION;
//		if (currentProject != null)
//			selectedValue = MessageDialog.showChoiceMsg("Current project will be saved and closed, proceed?");
//
//		if (selectedValue == JOptionPane.YES_OPTION) {
//
//			if (currentProject != null) {
//				runSaveProjectTask();
//			}
//			else {
//				newProjectFrame.setLocationRelativeTo(this);
//				newProjectFrame.setVisible(true);
//			}
//		}
//	}

	private void openProject(ProjectType newProjectType) {

		if (currentProject != null || MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {

			int selectedValue = 
					MessageDialog.showChooseOrCancelMsg(
							"Save current project before opening another one?", this.getContentPane());
			if(selectedValue == JOptionPane.CANCEL_OPTION)
				return;
			
			if (selectedValue == JOptionPane.YES_OPTION) {

				ProjectType activeProjectType = ProjectType.DATA_ANALYSIS;
				if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null)
					activeProjectType = ProjectType.RAW_DATA_ANALYSIS;
				
				projectSwitchController = new ProjectSwitchController(
						true,
						ProjectState.EXISTING_PROJECT,
						false,
						activeProjectType, 
						newProjectType);
				saveProject();
				return;
			}
			if (selectedValue == JOptionPane.NO_OPTION) {

				clearGuiAfterProjectClosed();
				initProjectLoadTask(newProjectType);
				return;
			}
		}
		else
			initProjectLoadTask(newProjectType);
	}

	public void reloadDesign() {

		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			entry.getValue().reloadDesign();

		projectDashBooard.switchDataPipeline(currentProject, activeDataPipeline);
	}

	private void saveProjectCopy() {

		if (currentProject != null) {

			saveProject();
			savingAsCopy = true;
		}
	}

	private File selectProjectCopyDirectory() {

		JFileChooser chooser = new ImprovedFileChooser();
		File inputFile = null;

		chooser.setCurrentDirectory(new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()));
		chooser.setPreferredSize(new Dimension(800, 640));
		chooser.setDialogTitle("Select the name and destination for the project copy");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			inputFile = chooser.getSelectedFile();

		return inputFile;
	}

	//	TODO shift data loading to specific panels
	public void showDataLoader(String command) {

//		if(dataLoader == null)
//			dataLoader = new DataImportDialog();
//
//		dataLoader.clearInputFileField();
//
//		if (command.equals(MainActionCommands.LOAD_DATA_COMMAND.getName()))
//			dataLoader.setDataType(DataType.QUANT_DATA_MATRIX);
//
//		if (command.equals(MainActionCommands.LOAD_WORKLIST_COMMAND.getName()))
//			dataLoader.setDataType(DataType.WORKLIST_TEXT);
//
//		if (command.equals(MainActionCommands.LOAD_DESIGN_COMMAND.getName()))
//			dataLoader.setDataType(DataType.DESIGN);
//
//		if (command.equals(MainActionCommands.LOAD_LIBRARY_COMMAND.getName()))
//			dataLoader.setDataType(DataType.LIBRARY);
//
//		if (activeDataPipeline != null)
//			dataLoader.setActiveAssay(activeDataPipeline);
//
//		dataLoader.setLocationRelativeTo(this);
//		dataLoader.setVisible(true);
	}

	private void showHelp() {
		MessageDialog.showInfoMsg(
				MainActionCommands.SHOW_HELP_COMMAND.getName());
	}
	
	private void showNewRawDataAnalysisProjectDialog() {
			
		rawDataAnalysisProjectSetupDialog = new RawDataAnalysisProjectSetupDialog(this);
		rawDataAnalysisProjectSetupDialog.setLocationRelativeTo(this.getContentPane());
		rawDataAnalysisProjectSetupDialog.setVisible(true);
	}
	
	private void showNewMetabolomicsProjectDialog(
			ExperimentDesign design, 
			LIMSExperiment newLimsExperiment) {
		
		newProjectFrame = new NewProjectDialog(this);
		newProjectFrame.setDesign(design);
		newProjectFrame.setLimsExperiment(newLimsExperiment);
		newProjectFrame.setProjectType(ProjectType.DATA_ANALYSIS);
		newProjectFrame.setLocationRelativeTo(this);
		newProjectFrame.setVisible(true);
	}

	public void createNewProjectFromLimsExperiment(
			File projectFile, 
			String projectDescription,
			ProjectType projectType, 
			LIMSExperiment activeExperiment) {

		String name = "";
		if(projectFile != null)
			name = projectFile.getName();

		if (name == null || name.isEmpty()) {
			MessageDialog.showWarningMsg("Please provide a name for the project!");
			return;
		}
		File projectDirectory = projectFile.getParentFile();
		if (projectFile.exists()) {
			MessageDialog.showWarningMsg("Project with this name already exists in this location!\n"
					+ "Please choose a different name or different parent directory.");
			return;
		}
		if (projectDescription.isEmpty())
			projectDescription = name;

		currentProject = new DataAnalysisProject(name, projectDescription, projectDirectory, projectType);
		if(activeExperiment.getExperimentDesign() != null)
			currentProject.getExperimentDesign().replaceDesign(activeExperiment.getExperimentDesign());

		currentProject.setLimsExperiment(activeExperiment);
		currentProject.setLimsProject(activeExperiment.getProject());

		MRC2ToolBoxCore.setCurrentProject(currentProject);
		setGuiFromActiveProject();

		//	Add design listeners
		currentProject.getExperimentDesign().addListener(projectDashBooard);
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			currentProject.getExperimentDesign().addListener(entry.getValue());
		
		projectBaseDirectory = projectDirectory.getParentFile();
		savePreferences();

		projectSwitchController = 
				new ProjectSwitchController(false, null, false, ProjectType.DATA_ANALYSIS, null);
		SaveProjectTask spt = new SaveProjectTask(currentProject);
		spt.addTaskListener(MRC2ToolBoxCore.getMainWindow());
		MRC2ToolBoxCore.getTaskController().addTask(spt);
	}

	public void createNewProjectFromIDTrackerExperiment(
			File projectFile,
			String projectDescription,
			LIMSExperiment activeExperiment) {

		String name = "";
		if(projectFile != null)
			name = projectFile.getName();

		if (name == null || name.isEmpty()) {
			MessageDialog.showWarningMsg("Please provide a name for the project!");
			return;
		}
		File projectDirectory = projectFile.getParentFile();

		if (projectFile.exists()) {
			MessageDialog.showWarningMsg("Project with this name already exists in this location!\n"
					+ "Please choose a different name or different parent directory.");
			return;
		}
		if (projectDescription.isEmpty())
			projectDescription = name;

		currentProject = new DataAnalysisProject(
				name, projectDescription,
				projectDirectory, ProjectType.ID_TRACKER_DATA_ANALYSIS);
		if(activeExperiment.getExperimentDesign() != null)
			currentProject.getExperimentDesign().replaceDesign(activeExperiment.getExperimentDesign());

		currentProject.setLimsExperiment(activeExperiment);
		currentProject.setLimsProject(activeExperiment.getProject());
		MRC2ToolBoxCore.setCurrentProject(currentProject);
		setGuiFromActiveProject();

		//	Add design listeners
		currentProject.getExperimentDesign().addListener(projectDashBooard);
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			currentProject.getExperimentDesign().addListener(entry.getValue());
		
		SaveProjectTask spt = new SaveProjectTask(currentProject);
		spt.addTaskListener(MRC2ToolBoxCore.getMainWindow());
		MRC2ToolBoxCore.getTaskController().addTask(spt);
	}

	public void createNewProject(
			File projectFile, 
			String projectDescription, 
			ProjectType projectType, 
			ExperimentDesign design) {

		String name = "";

		if(projectFile != null)
			name = projectFile.getName();

		if (name == null || name.isEmpty()) {
			MessageDialog.showWarningMsg("Please provide a name for the project!");
			return;
		}
		File projectDirectory = projectFile.getParentFile();

		if (projectFile.exists()) {
			MessageDialog.showWarningMsg("Project with this name already exists in this location!\n"
					+ "Please choose a different name or different parent directory.");
			return;
		}
		if (projectDescription.isEmpty())
			projectDescription = name;

		projectBaseDirectory = projectDirectory.getParentFile();
		savePreferences();
		
		currentProject = new DataAnalysisProject(name, projectDescription, projectDirectory, projectType);
		if(design != null)
			currentProject.getExperimentDesign().replaceDesign(design);

		MRC2ToolBoxCore.setCurrentProject(currentProject);
		setGuiFromActiveProject();

		//	Add design listeners
		currentProject.getExperimentDesign().addListener(projectDashBooard);
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			currentProject.getExperimentDesign().addListener(entry.getValue());
		
		projectSwitchController = 
				new ProjectSwitchController(true, null, false, ProjectType.DATA_ANALYSIS, null);
		
		SaveProjectTask spt = new SaveProjectTask(currentProject);
		spt.addTaskListener(MRC2ToolBoxCore.getMainWindow());
		MRC2ToolBoxCore.getTaskController().addTask(spt);
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

			// Load project
			if (e.getSource().getClass().equals(LoadProjectTask.class))
				finalizeProjectLoad((LoadProjectTask) e.getSource());

			//	Save metabolomics project
			if (e.getSource().getClass().equals(SaveProjectTask.class) || 
					e.getSource().getClass().equals(SaveStoredRawDataAnalysisProjectTask.class))
				finalizeProjectSave();
			
			//	Save raw data analysis project
//			if (e.getSource().getClass().equals(SaveStoredRawDataAnalysisProjectTask.class))
//				finalizeRawDataAnalysisProjectSave();
			
			if(e.getSource().getClass().equals(ProjectRawDataFileOpenTask.class))
				finalizeProjectRawDataLoad((ProjectRawDataFileOpenTask)e.getSource());
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED) {
			MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
			hideProgressDialog();
		}
	}
	
	private void finalizeProjectRawDataLoad(ProjectRawDataFileOpenTask task) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		
		showPanel(PanelList.RAW_DATA_EXAMINER);
		RawDataExaminerPanel rawDataPanel = 
				(RawDataExaminerPanel)getPanel(PanelList.RAW_DATA_EXAMINER);
		rawDataPanel.finalizeProjectRawDataLoad(task);
	}
	
	private void finalizeProjectSave() {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		
		if(projectSwitchController.isExitProgram()) {
			MRC2ToolBoxCore.shutDown();
			return;
		}
		//	If just saving current project
		if(projectSwitchController.getProjectState() == null)			
			return;
		else	// If opening existing project or creating a new one
			clearGuiAfterProjectClosed();
		
		if(projectSwitchController.getProjectState().equals(
				ProjectSwitchController.ProjectState.NEW_PROJECT)) {
			
			showNewProjectDialog(
					projectSwitchController.getNewProjectType(),
					null, 
					projectSwitchController.getLimsExperiment());			
		}
		if(projectSwitchController.getProjectState().equals(
				ProjectSwitchController.ProjectState.EXISTING_PROJECT))
			initProjectLoadTask(projectSwitchController.getNewProjectType());

		//	TODO if(savingAsCopy) {}
	}

	private void finalizeProjectLoad(LoadProjectTask eTask) {

		MRC2ToolBoxCore.setCurrentProject(eTask.getNewProject());
		setGuiFromActiveProject();
	}
	
	private void clearGuiAfterProjectClosed() {
		
		MRC2ToolBoxCore.setCurrentProject(null);
		switchDataPipeline(null,  null);
		MRC2ToolBoxCore.setActiveRawDataAnalysisProject(null);
		MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.RAW_DATA_EXAMINER).clearPanel();
		MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH).clearPanel();
		RawDataManager.releaseAllDataSources();
		setTitle(BuildInformation.getProgramName());
		System.gc();
	}
	
	public void clearGuiAfterRawDataAnalysisProjectClosed() {	
		
		MRC2ToolBoxCore.setActiveRawDataAnalysisProject(null);
		MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.RAW_DATA_EXAMINER).clearPanel();
		MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH).clearPanel();
		RawDataManager.releaseAllDataSources();
		System.gc();
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline pipeline) {

		currentProject = project;
		activeDataPipeline = pipeline;
		if(currentProject != null)
			currentProject.setActiveDataPipeline(activeDataPipeline);

		projectDashBooard.switchDataPipeline(currentProject, activeDataPipeline);
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			entry.getValue().switchDataPipeline(currentProject, activeDataPipeline);
		
//		toolBar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
		
		if(dataExplorerPlotDialog != null) {
			dataExplorerPlotDialog.clearPanels();
			dataExplorerPlotDialog.setVisible(false);
		}	
		StatusBar.switchDataPipeline(project, pipeline);
	}

	public void switchPanelForDataPipeline(
			DataPipeline pipeline, PanelList activePanel) {

		currentProject = MRC2ToolBoxCore.getCurrentProject();
		activeDataPipeline = pipeline;
		currentProject.setActiveDataPipeline(activeDataPipeline);
		setGuiFromActiveProject();

		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			entry.getValue().switchDataPipeline(currentProject, activeDataPipeline);

		if(activePanel == null)
			activePanel = getActivePanel();

		// Make sure relevant panels are active
		if(activePanel != null) {

			if(!panelShowing.get(activePanel))
				panelShowing.put(activePanel, Boolean.TRUE);

			showPanel(activePanel);
		}
		projectDashBooard.switchDataPipeline(currentProject, activeDataPipeline);
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
	public void setGuiFromActiveProject() {

		setTitle(BuildInformation.getProgramName());		
		if(MRC2ToolBoxCore.getCurrentProject() != null) {
			setGuiFromMetabolomicsProject();
			return;
		}
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {
			setGuiFromRawDataAnalysisProject();
			return;
		}
	}
	
	private void setGuiFromRawDataAnalysisProject() {
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {

			this.setTitle(BuildInformation.getProgramName() + " - " + 
					MRC2ToolBoxCore.getActiveRawDataAnalysisProject().getName());
			StatusBar.showRawDataAnalysisProjectData(
					MRC2ToolBoxCore.getActiveRawDataAnalysisProject());;
		}
		//	TODO more
	}
	
	private void setGuiFromMetabolomicsProject() {
		
		currentProject = MRC2ToolBoxCore.getCurrentProject();
		activeDataPipeline = null;
		if(currentProject != null) {
			
			activeDataPipeline = currentProject.getActiveDataPipeline();
			// Set window title and status bar
			this.setTitle(BuildInformation.getProgramName() + " - " + currentProject.getName());
			StatusBar.switchDataPipeline(currentProject, activeDataPipeline);
		}
		// Update menu
		mainMenuBar.updateMenuFromProject(currentProject, activeDataPipeline);

		// Update project information
		projectDashBooard.switchDataPipeline(currentProject, activeDataPipeline);

		//	TODO this may need updating for new panel display controls
		if (activeDataPipeline != null &&  !(currentProject instanceof CompoundIdentificationProject)) {

			panelShowing.put(PanelList.FEATURE_DATA, 
					currentProject.dataPipelineHasData(activeDataPipeline));
			panelShowing.put(PanelList.WORKLIST, 
					currentProject.acquisitionMethodHasLinkedWorklist(activeDataPipeline.getAcquisitionMethod()));
			panelShowing.put(PanelList.DUPLICATES, 
					currentProject.hasDuplicateClusters(activeDataPipeline));
			panelShowing.put(PanelList.CORRELATIONS, 
					currentProject.correlationClustersCalculatedForDataPipeline(activeDataPipeline));
		} else {
			//	TODO deal with ID project
/*			if(currentProject instanceof CompoundIdentificationProject)
				showPanel(PanelList.ID_WORKBENCH);*/
		}
		currentProject.getExperimentDesign().addListener(projectDashBooard);	
		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet()) {
			entry.getValue().switchDataPipeline(currentProject, activeDataPipeline);
			currentProject.getExperimentDesign().addListener(entry.getValue());
			currentProject.getExperimentDesign().getDesignSubsets().
				forEach(ss -> ss.addListener(entry.getValue()));			
		}
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
		TableLlayoutManager.saveLayouts();
		SmoothingFilterManager.saveFilterMap();
	}

	public void saveApplicationLayout() {

		for(int i=0; i<control.getCDockableCount(); i++) {

			CDockable uiObject = control.getCDockable(i);
			if(uiObject instanceof PersistentLayout) {

				File layoutFile = ((PersistentLayout)uiObject).getLayoutFile();
				if(layoutFile == null) 
					System.err.println("No layout file for " + ((DefaultCDockable)uiObject).getTitleText());		
				else
					((PersistentLayout)uiObject).saveLayout(layoutFile);
			}
		}
		saveLayout(layoutConfigFile);
		TableLlayoutManager.saveLayouts();
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	public void setIdTrackerUser(LIMSUser user) {
//		toolBar.setIdTrackerUser(user);
		mainMenuBar.setIdTrackerUser(user);
	}

	/**
	 * @return the newProjectFrame
	 */
	public static NewProjectDialog getNewProjectFrame() {
		return newProjectFrame;
	}

	public static ProjectSetupDraw getProjectDashBooard() {
		return projectDashBooard;
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
		
		projectBaseDirectory = new File(preferences.get(PROJECT_BASE, 
				MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()));
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
		
		preferences.put(PROJECT_BASE, projectBaseDirectory.getAbsolutePath());
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
























