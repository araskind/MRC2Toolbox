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
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.jcs3.JCS;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;
import edu.umich.med.mrc2.datoolbox.gui.adducts.AdductManagerFrame;
import edu.umich.med.mrc2.datoolbox.gui.adducts.chemmod.ChemicalModificationManagerFrame;
import edu.umich.med.mrc2.datoolbox.gui.assay.AssayManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.datexp.DataExplorerPlotFrame;
import edu.umich.med.mrc2.datoolbox.gui.dbparse.DbParserFrame;
import edu.umich.med.mrc2.datoolbox.gui.filetools.FileToolsDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.organization.OrganizationManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.io.DataExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.IntegratedReportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.NewProjectDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.mwtab.MWTabExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.raw.RawDataUploadPrepDialog;
import edu.umich.med.mrc2.datoolbox.gui.labnote.LabNoteBookPanel;
import edu.umich.med.mrc2.datoolbox.gui.lims.METLIMSPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.MoTrPACDataTrackingPanel;
import edu.umich.med.mrc2.datoolbox.gui.mstools.MSToolsFrame;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.preferences.PreferencesDialog;
import edu.umich.med.mrc2.datoolbox.gui.preferences.TableLlayoutManager;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.ProjectSetupDraw;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
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
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskControlListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.gui.TaskProgressPanel;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.LoadProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveProjectTask;
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

//	public static final String PROG_NAME = "MRC2 Data Analysis Toolbox";

	private static PreferencesDialog preferencesDialog;
	private static NewProjectDialog newProjectFrame;
	private static DbParserFrame dbParserFrame;
	private static MSToolsFrame msToolsFrame;
	private static ChemicalModificationManagerFrame chemicalModificationManagerFrame;
	private static AdductManagerFrame adductManagerFrame;
	private static AssayManagerDialog assayMethodsManagerDialog;
	private static ReferenceSampleManagerDialog referenceSampleManagerDialog;
	private static TaskProgressPanel progressPanel;
	private static JDialog progressDialogue;
	private static DataExplorerPlotFrame dataExplorerPlotDialog;
	private RawDataUploadPrepDialog rawDataUploadPrepDialog;
	private IdTrackerLoginDialog idtLogin;

	private MainMenuBar mainMenuBar;
	private MainToolbar toolBar;
	private static ProjectSetupDraw projectDashBooard;
	private static LinkedHashMap<PanelList, DockableMRC2ToolboxPanel> panels;
	private LinkedHashMap<PanelList, Boolean> panelShowing;

	private boolean saveOnExitRequested;
	private boolean saveOnCloseRequested;
	private boolean showNewProjectDialog;
	private boolean showOpenProjectDialog;
	private boolean savingAsCopy;

	private DataAnalysisProject currentProject;
	private DataPipeline activeDataPipeline;

	private CControl control;
	private CGrid grid;
	public static StatusBar statusBar;

	private DataExportDialog exportDialog;
	private IntegratedReportDialog integratedReportDialog;
	private MWTabExportDialog mwTabExportDialog;
	private FileToolsDialog fileToolsDialog;

	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "MainWindow.layout");
	private static final Icon scriptIcon = GuiUtils.getIcon("script", 32);

	public MainWindow() throws HeadlessException {

		super(BuildInformation.getProgramName());
		MRC2ToolBoxCore.getTaskController().addTaskControlListener(this);

		initWindow();

		currentProject = null;
		activeDataPipeline = null;
		saveOnExitRequested = false;
		showNewProjectDialog = false;
		showOpenProjectDialog = false;
		savingAsCopy = false;
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
			saveProject();

		if (command.equals(MainActionCommands.SAVE_PROJECT_COPY_COMMAND.getName()))
			saveProjectCopy();

		if (command.equals(MainActionCommands.CLOSE_PROJECT_COMMAND.getName()))
			closeProject();

		if (command.equals(MainActionCommands.NEW_PROJECT_COMMAND.getName()))
			showNewProjectDialog(ProjectType.DATA_ANALYSIS, null);

		if (command.equals(MainActionCommands.NEW_CPD_ID_PROJECT_COMMAND.getName()))
			showNewProjectDialog(ProjectType.FEATURE_IDENTIFICATION, null);

		if (command.equals(MainActionCommands.SHOW_ID_TRACKER_LOGIN_COMMAND.getName()))
			showIdTrackerLogin();

		if (command.equals(MainActionCommands.LOGIN_TO_ID_TRACKER_COMMAND.getName()))
			loginIdTracker();

		if (command.equals(MainActionCommands.ID_TRACKER_LOGOUT_COMMAND.getName()))
			logoutIdTracker();

		if (command.equals(MainActionCommands.OPEN_PROJECT_COMMAND.getName()))
			openProject();

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

		if (command.equals(MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND.getName()))
			showIntegratedReportDialog();

		if (command.equals(MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND.getName()))
			showMwTabReportDialog();

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
			
		if(command.equals(MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND.getName()))
			showFileToolsDialog();
	}
	
	private void showFileToolsDialog() {
		
		fileToolsDialog = new FileToolsDialog();
		fileToolsDialog.setLocationRelativeTo(this);
		fileToolsDialog.setVisible(true);
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

	private void showMwTabReportDialog() {

		if(currentProject == null)
			return;

		mwTabExportDialog = new MWTabExportDialog();
		mwTabExportDialog.setLocationRelativeTo(this.getContentPane());
		mwTabExportDialog.setVisible(true);
	}

	private void showIdTrackerLogin() {

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
			//MessageDialogue.showInfoMsg("You are now logged into ID tracker database as " + user.getFullName(), this);
			((IDTrackerLimsManagerPanel) MRC2ToolBoxCore.getMainWindow().
					getPanel(PanelList.ID_TRACKER_LIMS)).refreshIdTrackerdata();
			idtLogin.dispose();
		}
	}

	private void logoutIdTracker() {

		int result = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to log out from ID tracker?", this.getContentPane());

		if(result == JOptionPane.YES_OPTION) {
			MRC2ToolBoxCore.setIdTrackerUser(null);
			toolBar.setIdTrackerUser(null);

			LabNoteBookPanel notebook = (LabNoteBookPanel)getPanel(PanelList.LAB_NOTEBOOK);
			if(notebook != null)
				notebook.clearPanel();
			
			IDTrackerLimsManagerPanel idtLims = (IDTrackerLimsManagerPanel)getPanel(PanelList.ID_TRACKER_LIMS);
			if(idtLims != null)
				idtLims.clearPanel();
			
			IDWorkbenchPanel idwb = (IDWorkbenchPanel)getPanel(PanelList.ID_WORKBENCH);
			if(idwb != null)
				idwb.clearPanel();
			
			METLIMSPanel metlimsPanel = (METLIMSPanel)getPanel(PanelList.LIMS);
			if(metlimsPanel != null)
				metlimsPanel.clearPanel();
			
			MoTrPACDataTrackingPanel motrpacPanel = (MoTrPACDataTrackingPanel)getPanel(PanelList.MOTRPAC_REPORT_TRACKER);
			if(motrpacPanel != null)
				motrpacPanel.clearPanel();	
		}
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
			toolBar.noProject();
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

		if (currentProject != null) {

			String yesNoQuestion = "You are going to close current project,"
					+ " do you want to save the results (Yes - save, No - discard)?";
			int selectedValue = MessageDialog.showChooseOrCancelMsg(
					yesNoQuestion, this.getContentPane());
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;

			if (selectedValue == JOptionPane.YES_OPTION) {
				saveOnCloseRequested = true;
				runSaveProjectTask();
				return;
			}
			clearGuiAfterProjectClosed();
		}
	}
	
	public void runSaveProjectTask() {
		
		if(currentProject == null)
			return;

		SaveProjectTask spt = new SaveProjectTask(currentProject);
		spt.addTaskListener(MRC2ToolBoxCore.getMainWindow());
		MRC2ToolBoxCore.getTaskController().addTask(spt);
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

		String yesNoQuestion = "You are going to close current project, "
				+ "do you want to save the results (Yes - save, No - discard)?";
		int selectedValue = -1;
		if (currentProject != null) {
			
			selectedValue = MessageDialog.showChooseOrCancelMsg(yesNoQuestion);
			if (selectedValue == JOptionPane.YES_OPTION) {

				saveOnExitRequested = true;
				runSaveProjectTask();
				clearGui(true);
				currentProject = null;
				MRC2ToolBoxCore.setCurrentProject(currentProject);
			}
			if (selectedValue == JOptionPane.NO_OPTION) {

				saveOnExitRequested = false;
				clearGui(true);
				currentProject = null;
				MRC2ToolBoxCore.setCurrentProject(currentProject);
			}
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;
		}
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {
			
					
			RawDataExaminerPanel rdaPanel = 
					(RawDataExaminerPanel)getPanel(PanelList.RAW_DATA_EXAMINER);
			rdaPanel.closeRawDataAnalysisProject(true);
			return;
		}
		if (!saveOnExitRequested) {
			if (MessageDialog.showChoiceWithWarningMsg(
					"Are you sure you want to exit?", this.getContentPane()) == JOptionPane.YES_OPTION)
				shutDown();
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

	private void initProjectLoadTask() {

		File savedProjectFile = selectProjectFile();
		if (savedProjectFile != null && savedProjectFile.exists()) {

			LoadProjectTask ltp = new LoadProjectTask(savedProjectFile);
			ltp.addTaskListener(this);
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
		setJMenuBar(mainMenuBar);

		toolBar = new MainToolbar(this);
		getContentPane().add(toolBar, BorderLayout.NORTH);		
		toolBar.setIdTrackerUser(null);
		
		statusBar = new StatusBar();
		getContentPane().add(statusBar, BorderLayout.SOUTH);

		control = new CControl( this );
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);

		grid = new CGrid( control );

		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.COMPLETE_TOOLBOX)) {
			projectDashBooard = new ProjectSetupDraw();
			grid.add( -25, 0, 25, 100, projectDashBooard);
			toolBar.noProject();
		}
		// Initialize panels and panel display flags
		panels = new LinkedHashMap<PanelList, DockableMRC2ToolboxPanel>();
		panelShowing = new LinkedHashMap<PanelList, Boolean>();
		
		TableLlayoutManager.loadLayouts();
		
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
		control.getContentArea().deploy( grid );
		add(control.getContentArea(), BorderLayout.CENTER);

		for(PanelList panelType : PanelList.getPanelListForConfiguration(BuildInformation.getStartupConfiguration()))
			panelShowing.put(panelType, panels.get(panelType).isVisible());

		mainMenuBar.refreshPanelsMenu(panelShowing);

		initProgressDialog();

		preferencesDialog = new PreferencesDialog();
		preferencesDialog.loadPreferences(MRC2ToolBoxConfiguration.getPreferences());
		msToolsFrame = new MSToolsFrame();

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

	private void openProject() {

		if (currentProject != null) {

			int selectedValue = JOptionPane.showInternalConfirmDialog(this.getContentPane(),
					"You are going to close current project, do you want to save the results (Yes - save, No - discard)?",
					"Save active project", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			if (selectedValue == JOptionPane.YES_OPTION) {

				runSaveProjectTask();
				clearGui(true);
				this.setTitle(BuildInformation.getProgramName());
				currentProject = null;
				showOpenProjectDialog = true;
			}
			if (selectedValue == JOptionPane.NO_OPTION) {

				clearGui(true);
				this.setTitle(BuildInformation.getProgramName());
				currentProject = null;
				initProjectLoadTask();
			}
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;
		}
		else
			initProjectLoadTask();
	}

	public void reloadDesign() {

		for (Entry<PanelList, DockableMRC2ToolboxPanel> entry : panels.entrySet())
			entry.getValue().reloadDesign();

		projectDashBooard.switchDataPipeline(currentProject, activeDataPipeline);
	}

	private void saveProject() {

		if (currentProject != null)
			runSaveProjectTask();
	}

	private void saveProjectCopy() {

		if (currentProject != null) {

			runSaveProjectTask();
			savingAsCopy = true;
		}
	}

	private File selectProjectCopyDirectory() {

		JFileChooser chooser = new ImprovedFileChooser();
		File inputFile = null;

		chooser.setCurrentDirectory(new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()));
		chooser.setDialogTitle("Select the name and destination for the project copy");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			inputFile = chooser.getSelectedFile();

		return inputFile;
	}

	private File selectProjectFile() {

		JFileChooser chooser = new ImprovedFileChooser();
		File inputFile = null;

		chooser.setCurrentDirectory(new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()));
		chooser.setDialogTitle("Select project file");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);

		FileNameExtensionFilter projectFileFilter = new FileNameExtensionFilter("Project files",
				MRC2ToolBoxConfiguration.PROJECT_FILE_EXTENSION);
		chooser.setFileFilter(projectFileFilter);

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
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

	private void showIntegratedReportDialog() {

		if(integratedReportDialog == null)
			integratedReportDialog = new IntegratedReportDialog();

		currentProject = MRC2ToolBoxCore.getCurrentProject();

		if (currentProject != null) {

			integratedReportDialog.setLocationRelativeTo(this);
			integratedReportDialog.setVisible(true);
		}
	}

	public void showNewProjectDialog(ProjectType type, ExperimentDesign design) {

		if(newProjectFrame == null)
			newProjectFrame = new NewProjectDialog(this);

		newProjectFrame.setDesign(design);
		newProjectFrame.setLimsExperiment(null);
		newProjectFrame.setProjectType(type);

		int selectedValue = JOptionPane.YES_OPTION;

		if (currentProject != null)
			selectedValue = MessageDialog.showChoiceMsg("Current project will be saved and closed, proceed?");

		if (selectedValue == JOptionPane.YES_OPTION) {

			if (currentProject != null) {

				runSaveProjectTask();
				clearGui(true);
				currentProject = null;
				showNewProjectDialog = true;
			}
			else {
				newProjectFrame.setLocationRelativeTo(this);
				newProjectFrame.setVisible(true);
			}
		}
	}

	public void showNewProjectFromLimsExperimentDialogue(ProjectType type, LIMSExperiment activeExperiment) {

		if(newProjectFrame == null)
			newProjectFrame = new NewProjectDialog(this);

		newProjectFrame.setDesign(null);
		newProjectFrame.setLimsExperiment(activeExperiment);
		newProjectFrame.setProjectType(type);

		int selectedValue = JOptionPane.YES_OPTION;
		if (currentProject != null)
			selectedValue = MessageDialog.showChoiceMsg("Current project will be saved and closed, proceed?");

		if (selectedValue == JOptionPane.YES_OPTION) {

			if (currentProject != null) {
				runSaveProjectTask();
				showNewProjectDialog = true;
			}
			else {
				newProjectFrame.setLocationRelativeTo(this);
				newProjectFrame.setVisible(true);
			}
		}
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

		currentProject = new DataAnalysisProject(name, projectDescription, projectDirectory, projectType);
		if(design != null)
			currentProject.getExperimentDesign().replaceDesign(design);

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

	public void shutDown() {

		RawDataManager.releaseAllDataSources();
		try {
			JCS.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		saveApplicationLayout();
		savePreferences();
		this.dispose();
		System.gc();
		System.exit(0);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			// Load project
			if (e.getSource().getClass().equals(LoadProjectTask.class))
				finalizeProjectLoad((LoadProjectTask) e.getSource());

			//	Save project
			if (e.getSource().getClass().equals(SaveProjectTask.class))
				finalizeProjectSave();
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			hideProgressDialog();
	}

	private void finalizeProjectLoad(LoadProjectTask eTask) {

		MRC2ToolBoxCore.setCurrentProject(eTask.getNewProject());
		setGuiFromActiveProject();
	}

	private void finalizeProjectSave() {

		if(showNewProjectDialog) {

			showNewProjectDialog = false;
			clearGuiAfterProjectClosed();

			if(newProjectFrame == null)
				newProjectFrame = new NewProjectDialog(this);

			newProjectFrame.setLocationRelativeTo(this);
			newProjectFrame.setVisible(true);
		}
		if(saveOnCloseRequested) {

			saveOnCloseRequested = false;
			clearGuiAfterProjectClosed();
		}
		if(saveOnExitRequested) {

			saveOnExitRequested = false;
			int selectedValue = JOptionPane.showInternalConfirmDialog(this.getContentPane(),
					"Are you sure you want to exit?", "Exiting...", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);

			if (selectedValue == JOptionPane.YES_OPTION)
				shutDown();
		}
		if(showOpenProjectDialog) {

			clearGuiAfterProjectClosed();
			showOpenProjectDialog = false;
			initProjectLoadTask();
		}
		if(savingAsCopy) {

			savingAsCopy = false;
			File projectCopy = selectProjectCopyDirectory();

			if(projectCopy != null) {
				
				//	TODO update project name and project file name
				try {
					FileUtils.copyDirectory(currentProject.getProjectDirectory(), projectCopy);
					currentProject.getProjectFile();				
					File oldProjectFile = 
							Paths.get(projectCopy.getAbsolutePath(), currentProject.getProjectFile().getName()).toFile();				
					File newProjectFile = 
							Paths.get(projectCopy.getAbsolutePath(), projectCopy.getName() + "."
							+ MRC2ToolBoxConfiguration.PROJECT_FILE_EXTENSION).toFile();
					
					oldProjectFile.renameTo(newProjectFile);
					
					MessageDialog.showInfoMsg("Project " + currentProject.getName() + 
							" copied to\n" + projectCopy.getAbsolutePath());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void clearGuiAfterProjectClosed() {
		
		MRC2ToolBoxCore.setCurrentProject(null);
		switchDataPipeline(null,  null);
		setTitle(BuildInformation.getProgramName());
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
		
		toolBar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
		
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
		currentProject = MRC2ToolBoxCore.getCurrentProject();
		activeDataPipeline = null;
		if(currentProject != null) {
			activeDataPipeline = currentProject.getActiveDataPipeline();
			// Set window title
			this.setTitle(BuildInformation.getProgramName() + " - " + currentProject.getName());
		}
		// Update menu
		mainMenuBar.updateMenuFromProject(currentProject, activeDataPipeline);

		// Update main toolbar
		toolBar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);

		// Update project information
		projectDashBooard.switchDataPipeline(currentProject, activeDataPipeline);

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
	public void clearPanel() {

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
	}

	private void saveApplicationLayout() {

		for(int i=0; i<control.getCDockableCount(); i++) {

			CDockable uiObject = control.getCDockable(i);

			if(uiObject instanceof PersistentLayout) {

				File layoutFile = ((PersistentLayout)uiObject).getLayoutFile();
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
		toolBar.setIdTrackerUser(user);
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
	}
}
























