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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.filetools.FileToolsDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.DockableMsMsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.main.StatusBar;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.DockableChromatogramPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.MsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.msc.RawDataConversionSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.msms.MSMSFeatureExtractionSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.RawDataAnalysisProjectSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.tree.RawDataTree;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.DockableMsTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.OpenStoredRawDataAnalysisProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveStoredRawDataAnalysisProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ChromatogramExtractionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.MSMSExtractionParameterSet;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.MassSpectraAveragingTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.MsMsfeatureBatchExtractionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ProjectRawDataFileOpenTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataBatchCoversionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataFileOpenTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataRepositoryIndexingTask;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scancollection.IScanCollection;

public class RawDataExaminerPanel extends DockableMRC2ToolboxPanel 
	implements TreeSelectionListener{
	
	private RawDataExaminerToolbar toolbar;
	private DockableChromatogramPlot chromatogramPanel;
	private DockableSpectumPlot msPlotPanel;
	private DockableMsTable msTable;
	private DockableSpectumPlot msmsPlotPane;
	private DockableMsMsTable msmsTable;
	private DockableDataTreePanel dataFileTreePanel;
	private DockableXICSetupPanel xicSetupPanel;
	private DockableMsExtractorPanel msExtractorPanel;
	private DockableRawDataFilePropertiesTable rawDataFilePropertiesTable;
	private ImprovedFileChooser chooser;
	private IndeterminateProgressDialog idp;
	private CloseRawDataFilesDialog closeRawDataFilesDialog;
	private RawDataConversionSetupDialog rawDataConversionSetupDialog;
	
	private boolean showOpenProjectDialog;
	private RawDataAnalysisProject activeRawDataAnalysisProject;
	private RawDataAnalysisProjectSetupDialog rawDataAnalysisProjectSetupDialog;
	private boolean saveOnCloseRequested;
	private boolean saveOnExitRequested;
	private boolean showNewProjectDialog;
	private MSMSFeatureExtractionSetupDialog msmsFeatureExtractionSetupDialog;
	private FileToolsDialog fileToolsDialog;

	private static final Icon componentIcon = GuiUtils.getIcon("chromatogram", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "RawDataPanel.layout");
	
	public RawDataExaminerPanel(){
		
		super("RawDataExaminerPanel", PanelList.RAW_DATA_EXAMINER.getName(), componentIcon);;

		toolbar = new RawDataExaminerToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
		dataFileTreePanel = new DockableDataTreePanel(this);
		dataFileTreePanel.addTreeSelectionListener(this);
		chromatogramPanel =  new DockableChromatogramPlot(
				"RawDataExaminerPanelDockableChromatogramPlot", "Chromatograms");
		msPlotPanel = new DockableSpectumPlot(
				"RawDataExaminerPanelDockableSpectumPlotMS1", "MS1 spectra");	
		msPlotPanel.setRawDataExaminerPanel(this);
		msTable = new DockableMsTable("RawDataExaminerPanelDockableMsTableMS1", "MS1 table");
		msmsPlotPane = new DockableSpectumPlot(
				"RawDataExaminerPanelDockableSpectumPlotMS2", "MS2 spectra");
		msPlotPanel.setRawDataExaminerPanel(this);
		msmsTable = new DockableMsMsTable(
				"RawDataExaminerPanelDockableMsMsTable", "MS2 table");
		rawDataFilePropertiesTable = new DockableRawDataFilePropertiesTable();
		
		xicSetupPanel = new DockableXICSetupPanel(this);
		msExtractorPanel = new DockableMsExtractorPanel(this);
		
		grid.add( 0, 0, 25, 100, dataFileTreePanel, rawDataFilePropertiesTable );
		grid.add( 25, 0, 75, 50, chromatogramPanel );
		grid.add( 25, 50, 50, 50, msPlotPanel, msTable, msmsPlotPane, msmsTable);
		grid.add( 75, 50, 25, 50, xicSetupPanel, msExtractorPanel);
		control.getContentArea().deploy( grid );

		add(control.getContentArea(), BorderLayout.CENTER);		
		loadLayout(layoutConfigFile);
		
		initChooser();
	}
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		File rawDataRepository = new File(MRC2ToolBoxConfiguration.getRawDataRepository());
		if(rawDataRepository.exists() && rawDataRepository.isDirectory())
			chooser.setCurrentDirectory(rawDataRepository);

		chooser.setFileFilter(
				new FileNameExtensionFilter("Raw MS files", "mzml", "mzML", "mzXML", "mzxml"));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		String command = event.getActionCommand();
		
		if (command.equals(MainActionCommands.NEW_RAW_DATA_PROJECT_SETUP_COMMAND.getName())) 
			showNewRawDataAnalysisProjectDialog();
		
		if (command.equals(MainActionCommands.NEW_RAW_DATA_PROJECT_COMMAND.getName())) 
			createNewRawDataAnalysisProject();
			
		if (command.equals(MainActionCommands.OPEN_RAW_DATA_PROJECT_COMMAND.getName())) 
			openRawDataAnalysisProject();
		
		if (command.equals(MainActionCommands.CLOSE_RAW_DATA_PROJECT_COMMAND.getName())) 
			closeRawDataAnalysisProject(false);
		
		if (command.equals(MainActionCommands.SAVE_RAW_DATA_PROJECT_COMMAND.getName())) 
			saveRawDataAnalysisProject();
		
		if (command.equals(MainActionCommands.EDIT_RAW_DATA_PROJECT_SETUP_COMMAND.getName()))
			showNewRawDataAnalysisProjectEditor();
			
		if (command.equals(MainActionCommands.EDIT_RAW_DATA_PROJECT_COMMAND.getName()))
			saveChangesToProject();
		
		if (command.equals(MainActionCommands.MSMS_FEATURE_EXTRACTION_SETUP_COMMAND.getName()))
			setupMSMSFeatureExtraction();

		if (command.equals(MainActionCommands.MSMS_FEATURE_EXTRACTION_COMMAND.getName()))
			extractMSMSFeatures();
		
		if (command.equals(MainActionCommands.SEND_MSMS_FEATURES_TO_IDTRACKER_WORKBENCH.getName()))
			sendMSMSFeaturesToIDTrackerWorkbench();
		
		if (command.equals(MainActionCommands.OPEN_RAW_DATA_FILE_COMMAND.getName())) 
			openRawDataFiles();
		
		if (command.equals(MainActionCommands.CLOSE_RAW_DATA_FILE_COMMAND.getName())) 
			showCloseRawDataFilesDialog();
		
		if (command.equals(MainActionCommands.FINALIZE_CLOSE_RAW_DATA_FILE_COMMAND.getName())) 
			closeRawDataFiles();		

		if (command.equals(MainActionCommands.SETUP_RAW_DATA_CONVERSION_COMMAND.getName()))	
			setupRawDataConversion();	
		
		if (command.equals(MainActionCommands.CONVERT_RAW_DATA_COMMAND.getName()))	
			convertRawData();
		
		if (command.equals(MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND.getName()))	
			indexRawDataRepository();	
		
		if (command.equals(MainActionCommands.EXTRACT_CHROMATOGRAM.getName()))	
			extractChromatogramms();	
				
		if(command.equals(MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND.getName()))
			showFileToolsDialog();
		
		if(command.equals(MainActionCommands.ADD_PROJECT_METADATA_COMMAND.getName())) {
			MessageDialog.showWarningMsg("TODO: " + command, this.getContentPane());
		}
		if(command.equals(MainActionCommands.SEND_PROJECT_DATA_TO_DATABASE_COMMAND.getName())) {
			MessageDialog.showWarningMsg("TODO: " + command, this.getContentPane());
		}
	}
		
	private void showFileToolsDialog() {
		
		fileToolsDialog = new FileToolsDialog();
		fileToolsDialog.setLocationRelativeTo(this.getContentPane());
		fileToolsDialog.setVisible(true);
	}
	
	private void sendMSMSFeaturesToIDTrackerWorkbench() {
		
		if(activeRawDataAnalysisProject == null)
			return;
		
		Collection<DataFile> files = 
				activeRawDataAnalysisProject.getMSMSDataFiles();
		
		int fCount = 0;
		for(DataFile df : files) {
			 Collection<MsFeatureInfoBundle> fileFeatures = 
					 activeRawDataAnalysisProject.getMsFeaturesForDataFile(df);
			 if(fileFeatures != null && !fileFeatures.isEmpty())
				 fCount += fileFeatures.size();
		}
		if(fCount == 0)
			return;
		
		IDWorkbenchPanel workbench  = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
		workbench.loadFeaturesFromRawDataProject(activeRawDataAnalysisProject);
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.ID_WORKBENCH);
	}
	
	private void setupMSMSFeatureExtraction() {
		
		if(activeRawDataAnalysisProject == null)
			return;
		
		msmsFeatureExtractionSetupDialog = new MSMSFeatureExtractionSetupDialog(this);
		msmsFeatureExtractionSetupDialog.setLocationRelativeTo(this.getContentPane());
		msmsFeatureExtractionSetupDialog.setVisible(true);
	}

	private void extractMSMSFeatures() {
		
		if(!activeRawDataAnalysisProject.getMsMsFeatureBundles().isEmpty()) {
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to discard previously extracted "
					+ "MSMS data for the current project?", 
					msmsFeatureExtractionSetupDialog);
			if(res != JOptionPane.YES_OPTION) 
				return;
		}
		cleanupForReanalysis();
		MSMSExtractionParameterSet ps = 
				msmsFeatureExtractionSetupDialog.getMSMSExtractionParameterSet();
		if(ps == null)
			return;

		MsMsfeatureBatchExtractionTask task = 
				new MsMsfeatureBatchExtractionTask(
						ps, 
						activeRawDataAnalysisProject.getMSMSDataFiles(), 
						activeRawDataAnalysisProject.getMSOneDataFiles());	
		
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
		msmsFeatureExtractionSetupDialog.dispose();
	}
	
	private void cleanupForReanalysis() {
		activeRawDataAnalysisProject.clearMSMSFeatures();
		dataFileTreePanel.clearPanel();
		chromatogramPanel.clearPanel();
		msPlotPanel.clearPanel();
		msTable.clearTable();
		msmsPlotPane.clearPanel();
		msmsTable.clearTable();
		rawDataFilePropertiesTable.clearTable();
		IDWorkbenchPanel workbench  = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
		workbench.clearPanel();		
		dataFileTreePanel.loadData(activeRawDataAnalysisProject.getDataFiles(), true);	
		dataFileTreePanel.toggleTreeExpanded(dataFileTreePanel.isTreeExpanded());
	}	

	public void showNewRawDataAnalysisProjectDialog() {
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() != null) {
			MessageDialog.showWarningMsg(
					"Please close active raw data analysis project \"" + 
					MRC2ToolBoxCore.getActiveRawDataAnalysisProject().getName() + 
					"\" first.", 
					this.getContentPane());
			return;
		}		
		rawDataAnalysisProjectSetupDialog = new RawDataAnalysisProjectSetupDialog(this);
		rawDataAnalysisProjectSetupDialog.setLocationRelativeTo(this.getContentPane());
		rawDataAnalysisProjectSetupDialog.setVisible(true);
	}

	private void createNewRawDataAnalysisProject() {
		
		Collection<String>errors = validateRawDataAnalysisProjectSetup();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), 
					rawDataAnalysisProjectSetupDialog);
			return;
		}				
		File parentDirectory = 
				new File(rawDataAnalysisProjectSetupDialog.getProjectLocationPath());		
		RawDataAnalysisProject newProject = new RawDataAnalysisProject(
				rawDataAnalysisProjectSetupDialog.getProjectName(), 
				rawDataAnalysisProjectSetupDialog.getProjectDescription(), 
				parentDirectory);
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
		activeRawDataAnalysisProject = newProject;
		StatusBar.setProjectName(activeRawDataAnalysisProject.getName());
		
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
		
		return errors;
	}
	
	private void showNewRawDataAnalysisProjectEditor() {
		//	TODO
	}
	
	private void saveChangesToProject() {
		//	TODO
	}
	
	private void openRawDataAnalysisProject() {		

		if (activeRawDataAnalysisProject != null) {

			int selectedValue = MessageDialog.showChooseOrCancelMsg(
					"You are going to close current project, do you want "
					+ "to save the results (Yes - save, No - discard)?", 
					this.getContentPane());

			if (selectedValue == JOptionPane.YES_OPTION) {

				runSaveProjectTask();
				clearGuiAfterProjectClosed();
				//	TODO clear tracker workbench if used
				MRC2ToolBoxCore.getMainWindow().setTitle(BuildInformation.getProgramName());
				activeRawDataAnalysisProject = null;
				showOpenProjectDialog = true;
			}
			if (selectedValue == JOptionPane.NO_OPTION) {

				clearGuiAfterProjectClosed();
				//	TODO clear tracker workbench if used
				MRC2ToolBoxCore.getMainWindow().setTitle(BuildInformation.getProgramName());
				activeRawDataAnalysisProject = null;
				initRawDataAnalysisProjectLoadTask();
			}
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;
		}
		else {
			//	clearGuiAfterProjectClosed();
			initRawDataAnalysisProjectLoadTask();
		}
	}	
	
	private void saveRawDataAnalysisProject() {

		if (activeRawDataAnalysisProject != null)
			runSaveProjectTask();
	}
	
	public void runSaveProjectTask() {

		if(activeRawDataAnalysisProject == null)
			return;
		
		SaveStoredRawDataAnalysisProjectTask task = 
				new SaveStoredRawDataAnalysisProjectTask(activeRawDataAnalysisProject);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void initRawDataAnalysisProjectLoadTask() {

		JFileChooser chooser = new ImprovedFileChooser();
		chooser.setCurrentDirectory(
				new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()));
		chooser.setDialogTitle("Select raw data analysis project file");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
		FileNameExtensionFilter projectFileFilter = new FileNameExtensionFilter("Raw data project files",
				MRC2ToolBoxConfiguration.RAW_DATA_PROJECT_FILE_EXTENSION);
		chooser.setFileFilter(projectFileFilter);

		if (chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
			
			File projectFile = chooser.getSelectedFile();
			//	LoadRawDataAnalysisProjectTask ltp = 
			//		new LoadRawDataAnalysisProjectTask(projectFile);
			OpenStoredRawDataAnalysisProjectTask ltp = 
					new OpenStoredRawDataAnalysisProjectTask(projectFile);
			ltp.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(ltp);
		}
	}

	public void closeRawDataAnalysisProject(boolean exitProgram) {

		if (activeRawDataAnalysisProject == null) {
			
			if(exitProgram) {
				if (MessageDialog.showChoiceWithWarningMsg(
						"Are you sure you want to exit?", this.getContentPane()) == JOptionPane.YES_OPTION)
					MRC2ToolBoxCore.getMainWindow().shutDown();
			}
			else {
				return;
			}
		}
		String yesNoQuestion = "You are going to close current project,"
				+ " do you want to save the results (Yes - save, No - discard)?";
		int selectedValue = MessageDialog.showChooseOrCancelMsg(
				yesNoQuestion, this.getContentPane());
		if (selectedValue == JOptionPane.CANCEL_OPTION)
			return;

		if (selectedValue == JOptionPane.YES_OPTION) {
			
			if(exitProgram)
				saveOnExitRequested = true;
			else
				saveOnCloseRequested = true;
			
			runSaveProjectTask();
			return;
		}
		if (selectedValue == JOptionPane.NO_OPTION) {
			
			if(exitProgram) {
				selectedValue = 
						MessageDialog.showChoiceWithWarningMsg("Are you sure you want to exit?", 
								this.getContentPane());
				if (selectedValue == JOptionPane.YES_OPTION)
					MRC2ToolBoxCore.getMainWindow().shutDown();
			}
			else {
				clearGuiAfterProjectClosed();
			}
		}			
	}
	
	private void clearGuiAfterProjectClosed() {		
		activeRawDataAnalysisProject = null;
		MRC2ToolBoxCore.setActiveRawDataAnalysisProject(null);
		clearPanel();
		IDWorkbenchPanel workbench  = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
		workbench.clearPanel();
		RawDataManager.releaseAllDataSources();
		System.gc();
	}

	private void extractChromatogramms() {
		
		Collection<String>errors = xicSetupPanel.veryfyParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this.getContentPane());
			return;
		}
		ChromatogramExtractionTask task = xicSetupPanel.createChromatogramExtractionTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
	
	private void indexRawDataRepository() {
		
		String repositoryPath = MRC2ToolBoxConfiguration.getRawDataRepository();
		if(repositoryPath == null) {
			MessageDialog.showErrorMsg(
					"Raw data repository has to be specified in program preferences.", 
					this.getContentPane());
			return;
		}
		File rawDataRepository = new File(repositoryPath);
		if(!rawDataRepository.exists()) {
			MessageDialog.showErrorMsg(
					"Selected raw data repository at \"" + 
						MRC2ToolBoxConfiguration.getRawDataRepository() + "\" does not exist.", 
					this.getContentPane());
			return;
		}
		RawDataRepositoryIndexingTask task = new RawDataRepositoryIndexingTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void setupRawDataConversion() {
		
		String msConvertPath = MRC2ToolBoxConfiguration.getMsConvertExecutableFile();
		if(msConvertPath.isEmpty()) {
			MessageDialog.showErrorMsg("msconvert executable file location not specified.", this.getContentPane());
			return;
		}
		File msConvertExe = new File(msConvertPath);
		if(!msConvertExe.exists()) {
			MessageDialog.showErrorMsg("msconvert executable file can not be found at \n\"" + 
					msConvertPath + "\"" , this.getContentPane());
			return;
		}		
		rawDataConversionSetupDialog = new RawDataConversionSetupDialog(this);
		rawDataConversionSetupDialog.setLocationRelativeTo(this.getContentPane());
		rawDataConversionSetupDialog.setVisible(true);
	}

	private void convertRawData() {

		File outputDir = rawDataConversionSetupDialog.getOutputFolder();
		if(outputDir == null) {
			MessageDialog.showErrorMsg("Output folder not specified.", rawDataConversionSetupDialog);
			return;
		}
		Collection<File>filesToConvert = rawDataConversionSetupDialog.getFiles();
		if(filesToConvert.isEmpty()) {
			MessageDialog.showErrorMsg("No files selected for conversion.", rawDataConversionSetupDialog);
			return;
		}
		RawDataBatchCoversionTask task = 
				new RawDataBatchCoversionTask(outputDir, filesToConvert);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
		rawDataConversionSetupDialog.dispose();
	}

	private void openRawDataFiles() {
		
		if(chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
			
			File[] selectedFiles = chooser.getSelectedFiles();
			if(selectedFiles.length == 0)
				return;
			
			ArrayList<File>filesToOpen = new ArrayList<File>();
			for(File rf : selectedFiles) {
				
				if(RawDataManager.getRawData(rf) == null)
					filesToOpen.add(rf);
			}
			if(filesToOpen.isEmpty()) {
				MessageDialog.showWarningMsg("All selected files already opened.", this.getContentPane());
				return;
			}			
			chooser.setCurrentDirectory(chooser.getSelectedFile().getParentFile());
			RawDataFileOpenTask task = new RawDataFileOpenTask(filesToOpen);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	class OpenRawDataFilesTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private Collection<DataFile>newRawFiles;
		private boolean append;

		public OpenRawDataFilesTask(Collection<DataFile> newRawFiles, boolean append) {
			super();
			this.newRawFiles = newRawFiles;
			this.append = append;
		}

		public OpenRawDataFilesTask(Collection<DataFile>newRawFiles) {
			this.newRawFiles = newRawFiles;
			this.append = true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void doInBackground() {

			try {
				Collection<DataFile> existingFiles = dataFileTreePanel.getDataFiles();
				existingFiles.addAll(newRawFiles);
				dataFileTreePanel.loadData(existingFiles, true);	
				dataFileTreePanel.toggleTreeExpanded(dataFileTreePanel.isTreeExpanded());				
				xicSetupPanel.loadData(newRawFiles, append);
				msExtractorPanel.loadData(newRawFiles, append);				
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	private void showCloseRawDataFilesDialog() {
		
		closeRawDataFilesDialog = new CloseRawDataFilesDialog(this);
		closeRawDataFilesDialog.setLocationRelativeTo(this.getContentPane());
		closeRawDataFilesDialog.setVisible(true);
	}
	
	private void closeRawDataFiles() {
		
		Collection<DataFile> files = closeRawDataFilesDialog.getSelectedFiles();
		if(files.isEmpty()) {
			MessageDialog.showErrorMsg("No files selected.", closeRawDataFilesDialog);
			return;
		}
		dataFileTreePanel.removeDataFiles(files);
		chromatogramPanel.removeChromatogramsForFiles(files);
		files.stream().forEach(f -> RawDataManager.removeDataSource(f));
		clearMsData(files);
		
		xicSetupPanel.removeDataFiles(files);
		msExtractorPanel.removeDataFiles(files);
		
		// TODO clear only if data related to selected files
		rawDataFilePropertiesTable.clearTable();
		
		closeRawDataFilesDialog.dispose();
	}

	private void clearMsData(Collection<DataFile> files) {
		
		// TODO clear only if data related to selected files
		msPlotPanel.clearPanel();
		msTable.clearTable();
		msmsPlotPane.clearPanel();
		msmsTable.clearTable();
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(ChromatogramExtractionTask.class))		
				finalizeChromatogramExtraction((ChromatogramExtractionTask)e.getSource());						
			
			if (e.getSource().getClass().equals(MassSpectraAveragingTask.class))		
				finalizeMassSpectraAveraging((MassSpectraAveragingTask)e.getSource());		
			
			if(e.getSource().getClass().equals(RawDataFileOpenTask.class))				
				finalizeRawDataFileOpenTask((RawDataFileOpenTask)e.getSource());		
			
			if(e.getSource().getClass().equals(RawDataBatchCoversionTask.class))
				finalizeRawDataFileConversionTask();
			
			if(e.getSource().getClass().equals(RawDataRepositoryIndexingTask.class)) 
				MessageDialog.showInfoMsg("Raw data indexing completed", this.getContentPane());
			
			if(e.getSource().getClass().equals(ProjectRawDataFileOpenTask.class))
				finalizeProjectRawDataLoad((ProjectRawDataFileOpenTask)e.getSource());
			
			if (e.getSource().getClass().equals(SaveStoredRawDataAnalysisProjectTask.class))
				finalizeRawDataAnalysisProjectSave();
			
			if (e.getSource().getClass().equals(OpenStoredRawDataAnalysisProjectTask.class))
				finalizeStoredRawDataAnalysisProjectOpen((OpenStoredRawDataAnalysisProjectTask)e.getSource());			
			
			if (e.getSource().getClass().equals(MsMsfeatureBatchExtractionTask.class))
				finalizeRawMSMSBatchExtractionTask((MsMsfeatureBatchExtractionTask)e.getSource());
		}
	}
	
	private void finalizeRawDataFileConversionTask() {
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		MessageDialog.showInfoMsg("Data conversion completed", this.getContentPane());
	}
	
	private void finalizeRawDataFileOpenTask(RawDataFileOpenTask rdoTask) {
		OpenRawDataFilesTask task = new OpenRawDataFilesTask(rdoTask.getOpenedFiles());
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		idp = new IndeterminateProgressDialog("Loading raw data tree ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	private void finalizeRawMSMSBatchExtractionTask(MsMsfeatureBatchExtractionTask task) {

		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		Map<DataFile, Collection<MsFeatureInfoBundle>> featureMap = task.getMsFeatureMap();	
		Collection<String>log = new ArrayList<String>();
		for(Entry<DataFile, Collection<MsFeatureInfoBundle>>e : featureMap.entrySet()) {
			activeRawDataAnalysisProject.setMsFeaturesForDataFile(e.getKey(), e.getValue());
			log.add(e.getKey().getName() + " -> " + Integer.toString(e.getValue().size()) + " features");
		}
		activeRawDataAnalysisProject.setChromatogramMap(task.getChromatogramMap());
		MessageDialog.showInfoMsg(StringUtils.join(log, "\n"), this.getContentPane());
		OpenRawDataFilesTask ordTask = new OpenRawDataFilesTask(
				activeRawDataAnalysisProject.getMSMSDataFiles(), false);
		idp = new IndeterminateProgressDialog("Loading raw data tree ...", this.getContentPane(), ordTask);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	private void finalizeStoredRawDataAnalysisProjectOpen(OpenStoredRawDataAnalysisProjectTask task) {

		if(!task.getErrors().isEmpty()) {
			
			MessageDialog.showErrorMsg(
					StringUtils.join(task.getErrors(), "\n"), this.getContentPane());
			return;
		}
		MRC2ToolBoxCore.setActiveRawDataAnalysisProject(task.getProject());
		activeRawDataAnalysisProject = task.getProject();
		OpenRawDataFilesTask ordTask = new OpenRawDataFilesTask(
				activeRawDataAnalysisProject.getDataFiles());
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		StatusBar.clearProjectData();
		StatusBar.setProjectName(activeRawDataAnalysisProject.getName());
		idp = new IndeterminateProgressDialog("Loading raw data tree ...", this.getContentPane(), ordTask);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);		
		sendMSMSFeaturesToIDTrackerWorkbench();
	}

	private void finalizeRawDataAnalysisProjectSave() {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();		
		if(showNewProjectDialog) {

			showNewProjectDialog = false;
			clearGuiAfterProjectClosed();
			rawDataAnalysisProjectSetupDialog = new RawDataAnalysisProjectSetupDialog(this);
			rawDataAnalysisProjectSetupDialog.setLocationRelativeTo(this.getContentPane());
			rawDataAnalysisProjectSetupDialog.setVisible(true);
		}
		if(saveOnCloseRequested) {

			saveOnCloseRequested = false;
			clearGuiAfterProjectClosed();
		}
		if(saveOnExitRequested) {

			saveOnExitRequested = false;
			int selectedValue = 
					MessageDialog.showChoiceWithWarningMsg("Are you sure you want to exit?", 
							this.getContentPane());
			if (selectedValue == JOptionPane.YES_OPTION)
				MRC2ToolBoxCore.getMainWindow().shutDown();
		}
		if(showOpenProjectDialog) {

			clearGuiAfterProjectClosed();
			showOpenProjectDialog = false;
			initRawDataAnalysisProjectLoadTask();
		}
	}
	
	private void finalizeProjectRawDataLoad(ProjectRawDataFileOpenTask task) {
		
		Collection<String> errors = task.getErrors();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this.getContentPane());
			ProjectUtils.saveProjectFile(activeRawDataAnalysisProject);
		}
		activeRawDataAnalysisProject.updateProjectLocation(activeRawDataAnalysisProject.getProjectFile());
		Collection<DataFile> filesToLoad = new ArrayList<DataFile>();
		filesToLoad.addAll(task.getProject().getMSMSDataFiles());
		filesToLoad.addAll(task.getProject().getMSOneDataFiles());
		
		OpenRawDataFilesTask ordTask = new OpenRawDataFilesTask(filesToLoad);
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		idp = new IndeterminateProgressDialog("Loading raw data tree ...", this.getContentPane(), ordTask);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	public void finalizeMassSpectraAveraging(MassSpectraAveragingTask task) {
		
		Collection<AverageMassSpectrum> spectra = task.getExtractedSpectra();
		for (AverageMassSpectrum ms : spectra)
			dataFileTreePanel.addObject(ms);
		
		if(!spectra.isEmpty()) { 
			AverageMassSpectrum avgMs = spectra.iterator().next();
			showAverageMassSpectrum(avgMs);
			//	dataFileTreePanel.selectNodeForObject(avgMs);
		}
	}
	
	public void finalizeChromatogramExtraction(ChromatogramExtractionTask task) {
		
		Collection<ExtractedChromatogram> chroms = task.getExtractedChromatograms();
		for (ExtractedChromatogram ec : chroms)
			dataFileTreePanel.addObject(ec);

		chromatogramPanel.showExtractedChromatogramCollection(chroms);			
		Set<DataFile> chromFiles = chroms.stream().
				map(c -> c.getDataFile()).distinct().
				collect(Collectors.toSet());
		xicSetupPanel.selectFiles(chromFiles);
		msExtractorPanel.selectFiles(chromFiles);
	}
	
	public void loadRawData(Collection<DataFile>dataFiles) {
		
		OpenRawDataFilesTask task = new OpenRawDataFilesTask(dataFiles);
		idp = new IndeterminateProgressDialog("Loading raw data tree ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	@Override
	public synchronized void clearPanel() {

		dataFileTreePanel.clearPanel();
		chromatogramPanel.clearPanel();
		msPlotPanel.clearPanel();
		msTable.clearTable();
		msmsPlotPane.clearPanel();
		msmsTable.clearTable();
		xicSetupPanel.clearPanel();
		msExtractorPanel.clearPanel();
		rawDataFilePropertiesTable.clearTable();
	}
	
	

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
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
	}

	@Override
	public void saveLayout(File layoutFile) {

		try {
			control.writeXML(layoutFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {

		RawDataTree pt = (RawDataTree) e.getSource();
		Collection<Object> selected = pt.getSelectedObjects();
		if(selected.isEmpty())
			return;
		
		//	Show first selected scan
		if(selected.stream().findFirst().get() instanceof IScan)
			showScan((IScan)selected.stream().findFirst().get());
				
		//	Show first selected user spectrum
		if(selected.stream().findFirst().get() instanceof AverageMassSpectrum)
			showAverageMassSpectrum((AverageMassSpectrum)selected.stream().findFirst().get());
		
		//	Show first selected MSMS feature
		if(selected.stream().findFirst().get() instanceof MsFeatureInfoBundle)
			showMsFeatureInfoBundle(((MsFeatureInfoBundle)selected.stream().findFirst().get()));
		
		//	Show selected chromatograms
		List<ExtractedChromatogram> chroms = 
				selected.stream().filter(o -> (o instanceof ExtractedChromatogram)).
				map(ExtractedChromatogram.class::cast).collect(Collectors.toList());
		
		if(!chroms.isEmpty())
			showChromatograms(chroms);
		
		List<DataFile> files = 
				selected.stream().filter(o -> (o instanceof DataFile)).
				map(DataFile.class::cast).collect(Collectors.toList());
		
		if(!files.isEmpty())
			showFiles(files);
	}

	private void showMsFeatureInfoBundle(MsFeatureInfoBundle msFeatureInfoBundle) {

		MsFeature msFeature = msFeatureInfoBundle.getMsFeature();
		msPlotPanel.showMsForFeature(msFeature, false);
		msTable.setTableModelFromSpectrum(msFeature);
		msmsPlotPane.clearPanel();
		msmsTable.clearTable();
	
		if(msFeature.getSpectrum() != null) {
			TandemMassSpectrum msms = 
					msFeature.getSpectrum().getExperimentalTandemSpectrum();
			if(msms != null) {
				msmsPlotPane.showTandemMs(msms);;
				msmsTable.setTableModelFromTandemMs(msms);
			}
		}
		DataFile df = msFeatureInfoBundle.getDataFile();
		if(df != null) {
			xicSetupPanel.selectFiles(Collections.singleton(df));
			msExtractorPanel.selectFiles(Collections.singleton(df));
		}
		if(activeRawDataAnalysisProject != null && 
				!activeRawDataAnalysisProject.getChromatogramMap().isEmpty()) {
			
			MsFeatureChromatogramBundle msfCb = 
					activeRawDataAnalysisProject.getChromatogramMap().get(msFeature.getId());			
			if(msfCb != null) {
				Collection<Double>markers = 
						RawDataUtils.getMSMSScanRtMarkersForFeature(
								msFeature, msFeatureInfoBundle.getDataFile());
				chromatogramPanel.showMsFeatureChromatogramBundle(msfCb, markers);
			}
		}
	}
	
//	private Collection<Double>getMsFeatureRtMarkers(MsFeature msFeature, DataFile df){
//		
//		Collection<Double>markers = new TreeSet<Double>();
//		if(msFeature.getSpectrum() != null
//				&& msFeature.getSpectrum().getExperimentalTandemSpectrum() != null) {
//			
//			Set<Integer> msmsScanNums = msFeature.getSpectrum().
//					getExperimentalTandemSpectrum().getAveragedScanNumbers().keySet();
//			if(!msmsScanNums.isEmpty()) {
//				
//				LCMSData rawData = RawDataManager.getRawData(df);	
//				TreeMap<Integer, IScan> num2scan = 
//						rawData.getScans().getMapMsLevel2index().get(2).getNum2scan();
//
//				for(int scanNum : msmsScanNums) {
//					IScan scan = num2scan.get(scanNum);
//					if(scan != null)
//						markers.add(scan.getRt());
//				}
//			}
//		}	
//		if(markers.isEmpty())
//			markers.add(msFeature.getRetentionTime());
//		
//		return markers;
//	}

	private void showFiles(List<DataFile> files) {

		xicSetupPanel.selectFiles(files);
		msExtractorPanel.selectFiles(files);
		
		LCMSData data = RawDataManager.getRawData(files.get(0));
		rawDataFilePropertiesTable.showDataFileProperties(data);
		
		List<ExtractedChromatogram> chromList = files.stream().
				flatMap(f -> f.getChromatograms().stream()).
				filter(c -> c.getChromatogramDefinition().getMode().equals(ChromatogramPlotMode.TIC)).
				collect(Collectors.toList());
		chromatogramPanel.showExtractedChromatogramCollection(chromList);
	}

	private void showAverageMassSpectrum(AverageMassSpectrum averageMassSpectrum) {
		msPlotPanel.showMsDataSet(new MsDataSet(averageMassSpectrum));
		DataFile df = averageMassSpectrum.getDataFile();
		if(df != null) {
			xicSetupPanel.selectFiles(Collections.singleton(df));
			msExtractorPanel.selectFiles(Collections.singleton(df));
		}
	}

	private void showScan(IScan s) {
		
		if(s.getMsLevel() == 1) {
			msPlotPanel.showScan(s);
			msTable.setTableModelFromScan(s);
			msmsPlotPane.clearPanel();
			msmsTable.clearTable();
		}
		else {
			msmsPlotPane.showScan(s);
			msmsTable.setTableModelFromScan(s);
			IScan parent = getParentScan(s);
			if(parent != null) {
				msPlotPanel.showScan(parent);
				msTable.setTableModelFromScan(parent);
			}
		}
		DataFile df = dataFileTreePanel.getDataFileForScan(s);
		if(df != null) {
			xicSetupPanel.selectFiles(Collections.singleton(df));
			msExtractorPanel.selectFiles(Collections.singleton(df));
		}
	}
	
	public IScan getParentScan(IScan s) {
		
		if(s.getPrecursor() != null) {
			int parentScanNumber = s.getPrecursor().getParentScanNum();
			DataFile df = dataFileTreePanel.getDataFileForScan(s);
			if(df != null) {
				LCMSData data = RawDataManager.getRawData(df);
				if(data != null) {
					IScanCollection scans = data.getScans();
					return scans.getScanByNum(parentScanNumber);
				}
			}
		}
		return null;
	}
	
	public DataFile getDataFileForScan(IScan s) {
		return dataFileTreePanel.getDataFileForScan(s);
	}		

	private void showChromatograms(Collection<ExtractedChromatogram> chroms) {

		chromatogramPanel.clearPanel();
		if(!chroms.isEmpty()) {			
			chromatogramPanel.showExtractedChromatogramCollection(chroms);	
			TreeSet<DataFile>files = chroms.stream().map(c -> c.getDataFile()).
					collect(Collectors.toCollection(TreeSet::new));
			xicSetupPanel.selectFiles(files);
			msExtractorPanel.selectFiles(files);			
		}
	}
	
	public void removeDataFiles(Collection<DataFile> selectedFiles) {
		dataFileTreePanel.removeDataFiles(selectedFiles);		
	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {

	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public Range getSelectedRTRange() {
		return chromatogramPanel.getSelectedRTRange();
	}
	
	public void clearChromatogramPanel() {
		chromatogramPanel.clearPanel();
	}
	
	public void clearSpectraPanel() {
		msPlotPanel.clearPanel();
	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub
		
	}
}








































