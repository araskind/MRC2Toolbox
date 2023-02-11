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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTRawDataUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
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
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.msc.RawDataConversionSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.msms.MSMSFeatureExtractionSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.RawDataAnalysisExperimentSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.edl.ExistingDataListingDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RDEMetadataWizard;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.scan.DockableScanPanel;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.tree.RawDataTree;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.DockableMsTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.OpenStoredRawDataAnalysisExperimentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.RawDataAnalysisExperimentDatabaseUploadTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveStoredRawDataAnalysisExperimentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ChromatogramExtractionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ExperimentRawDataFileOpenTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.MassSpectraAveragingTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.MsMsfeatureBatchExtractionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataBatchCoversionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataFileOpenTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataRepositoryIndexingTask;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;
import umich.ms.fileio.exceptions.FileParsingException;

public class RawDataExaminerPanel extends DockableMRC2ToolboxPanel 
	implements TreeSelectionListener, BackedByPreferences {

	private DockableChromatogramPlot chromatogramPanel;
	private DockableSpectumPlot msPlotPanel;
	private DockableMsTable msTable;
	private DockableSpectumPlot msmsPlotPanel;
	private DockableMsMsTable msmsTable;
	private DockableDataTreePanel dataFileTreePanel;
	private DockableXICSetupPanel xicSetupPanel;
	private DockableMsExtractorPanel msExtractorPanel;
	private DockableRawDataFilePropertiesTable rawDataFilePropertiesTable;
	private DockableScanPanel scanNavigationPanel;
	private IndeterminateProgressDialog idp;
	private CloseRawDataFilesDialog closeRawDataFilesDialog;
	private RawDataConversionSetupDialog rawDataConversionSetupDialog;
	
	private RawDataAnalysisExperimentSetupDialog rawDataAnalysisExperimentSetupDialog;
	private boolean showOpenExperimentDialog;	
	private boolean saveOnCloseRequested;
	private boolean saveOnExitRequested;
	private boolean showNewExperimentDialog;
	
	private MSMSFeatureExtractionSetupDialog msmsFeatureExtractionSetupDialog;	
	private RDEMetadataWizard rawDataExperimentMetadataWizard;
	private RawDataAnalysisExperimentDatabaseUploadDialog rawDataAnalysisExperimentDatabaseUploadDialog;

	private static final Icon componentIcon = GuiUtils.getIcon("chromatogram", 16);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "RawDataPanel.layout");
	
	private Preferences preferences;
	private static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory; 

	private static final Icon newRdaExperimentIcon = GuiUtils.getIcon("newRawDataAnalysisProject", 24);
	private static final Icon editRdaExperimentIcon = GuiUtils.getIcon("editRawDataAnalysisProject", 24);
	private static final Icon openExperimentIcon = GuiUtils.getIcon("openRawDataAnalysisProject", 24);
	private static final Icon closeExperimentIcon = GuiUtils.getIcon("closeRawDataAnalysisProject", 24);
	private static final Icon saveExperimentIcon = GuiUtils.getIcon("saveRawDataAnalysisProject", 24);	
	private static final Icon extractMSMSFeaturesIcon = GuiUtils.getIcon("findMSMSFeatures", 24);	
	private static final Icon sendToIDTrackerIcon = GuiUtils.getIcon("sendToIDTracker", 24);	
	private static final Icon openDataFileIcon = GuiUtils.getIcon("openDataFile", 24);
	private static final Icon closeDataFileIcon = GuiUtils.getIcon("closeDataFile", 24);
	private static final Icon msConvertIcon = GuiUtils.getIcon("msConvert", 24);
	private static final Icon indexRawFilesIcon = GuiUtils.getIcon("indexRawFiles", 24);
	private static final Icon dataFileToolsIcon = GuiUtils.getIcon("dataFileTools", 24);
	private static final Icon addMetaDataIcon = GuiUtils.getIcon("addMetadata", 24);
	private static final Icon sendExperimentToDatabaseIcon = GuiUtils.getIcon("xml2Database", 24);
	
	public RawDataExaminerPanel(){
		
		super("RawDataExaminerPanel", PanelList.RAW_DATA_EXAMINER.getName(), componentIcon);;

		menuBar = new RawDataExaminerMenuBar(this);	
		add(menuBar, BorderLayout.NORTH);
		
		dataFileTreePanel = new DockableDataTreePanel(this);
		dataFileTreePanel.addTreeSelectionListener(this);
		chromatogramPanel =  new DockableChromatogramPlot(
				"RawDataExaminerPanelDockableChromatogramPlot", "Chromatograms");
		msPlotPanel = new DockableSpectumPlot(
				"RawDataExaminerPanelDockableSpectumPlotMS1", "MS1 spectra");	
		msPlotPanel.setRawDataExaminerPanel(this);
		msTable = new DockableMsTable("RawDataExaminerPanelDockableMsTableMS1", "MS1 table");
		msmsPlotPanel = new DockableSpectumPlot(
				"RawDataExaminerPanelDockableSpectumPlotMS2", "MS2 spectra");
		msPlotPanel.setRawDataExaminerPanel(this);
		msmsTable = new DockableMsMsTable(
				"RawDataExaminerPanelDockableMsMsTable", "MS2 table");
		rawDataFilePropertiesTable = new DockableRawDataFilePropertiesTable();
		
		scanNavigationPanel = new DockableScanPanel();
		scanNavigationPanel.addScanSelectionListener(this);
		
		xicSetupPanel = new DockableXICSetupPanel(this);
		msExtractorPanel = new DockableMsExtractorPanel(this);

		grid.add( 0, 0, 25, 100, dataFileTreePanel, 
				scanNavigationPanel, rawDataFilePropertiesTable );
		grid.add( 25, 0, 75, 50, chromatogramPanel );
		grid.add( 25, 50, 50, 50, msPlotPanel, msTable, msmsPlotPanel, msmsTable);
		grid.add( 75, 50, 25, 50, xicSetupPanel, msExtractorPanel);
				
//		station.dropTree( grid.toTree() );
//		add(station, BorderLayout.CENTER);	
		
		control.getContentArea().deploy( grid );
		add(control.getContentArea(), BorderLayout.CENTER);	
		
		initActions();
		loadLayout(layoutConfigFile);
		loadPreferences();
		populatePanelsMenu();
	}
	
	@Override
	protected void initActions() {
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.NEW_RAW_DATA_EXPERIMENT_SETUP_COMMAND.getName(),
				MainActionCommands.NEW_RAW_DATA_EXPERIMENT_SETUP_COMMAND.getName(), 
				newRdaExperimentIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.OPEN_RAW_DATA_EXPERIMENT_COMMAND.getName(),
				MainActionCommands.OPEN_RAW_DATA_EXPERIMENT_COMMAND.getName(), 
				openExperimentIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLOSE_RAW_DATA_EXPERIMENT_COMMAND.getName(),
				MainActionCommands.CLOSE_RAW_DATA_EXPERIMENT_COMMAND.getName(), 
				closeExperimentIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SAVE_RAW_DATA_EXPERIMENT_COMMAND.getName(),
				MainActionCommands.SAVE_RAW_DATA_EXPERIMENT_COMMAND.getName(), 
				saveExperimentIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_RAW_DATA_EXPERIMENT_SETUP_COMMAND.getName(),
				MainActionCommands.EDIT_RAW_DATA_EXPERIMENT_SETUP_COMMAND.getName(), 
				editRdaExperimentIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.MSMS_FEATURE_EXTRACTION_SETUP_COMMAND.getName(),
				MainActionCommands.MSMS_FEATURE_EXTRACTION_SETUP_COMMAND.getName(), 
				extractMSMSFeaturesIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SEND_MSMS_FEATURES_TO_IDTRACKER_WORKBENCH.getName(),
				MainActionCommands.SEND_MSMS_FEATURES_TO_IDTRACKER_WORKBENCH.getName(), 
				sendToIDTrackerIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_EXPERIMENT_METADATA_COMMAND.getName(),
				MainActionCommands.ADD_EXPERIMENT_METADATA_COMMAND.getName(), 
				addMetaDataIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SET_EXPERIMENT_DATA_UPLOAD_PARAMETERS_COMMAND.getName(),
				MainActionCommands.SET_EXPERIMENT_DATA_UPLOAD_PARAMETERS_COMMAND.getName(), 
				sendExperimentToDatabaseIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.OPEN_RAW_DATA_FILE_COMMAND.getName(),
				MainActionCommands.OPEN_RAW_DATA_FILE_COMMAND.getName(), 
				openDataFileIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLOSE_RAW_DATA_FILE_COMMAND.getName(),
				MainActionCommands.CLOSE_RAW_DATA_FILE_COMMAND.getName(), 
				closeDataFileIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SETUP_RAW_DATA_CONVERSION_COMMAND.getName(),
				MainActionCommands.SETUP_RAW_DATA_CONVERSION_COMMAND.getName(), 
				msConvertIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND.getName(),
				MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND.getName(), 
				indexRawFilesIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND.getName(),
				MainActionCommands.SHOW_RAW_DATA_FILE_TOOLS_COMMAND.getName(), 
				dataFileToolsIcon, this));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		super.actionPerformed(event);
		
		String command = event.getActionCommand();
			
		if (command.equals(MainActionCommands.OPEN_RAW_DATA_EXPERIMENT_COMMAND.getName())) 
			openRawDataAnalysisExperiment();
		
		if (command.equals(MainActionCommands.CLOSE_RAW_DATA_EXPERIMENT_COMMAND.getName())) 
			closeRawDataAnalysisExperiment(false);
		
		if (command.equals(MainActionCommands.SAVE_RAW_DATA_EXPERIMENT_COMMAND.getName())) 
			saveRawDataAnalysisExperiment();
		
		if (command.equals(MainActionCommands.EDIT_RAW_DATA_EXPERIMENT_SETUP_COMMAND.getName()))
			showNewRawDataAnalysisExperimentEditor();
			
		if (command.equals(MainActionCommands.EDIT_RAW_DATA_EXPERIMENT_COMMAND.getName()))
			saveChangesToExperiment();
		
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
			MRC2ToolBoxCore.getMainWindow().showFileToolsDialog();
		
		if(command.equals(MainActionCommands.ADD_EXPERIMENT_METADATA_COMMAND.getName()))
			showExperimentMetadataWizard();	
		
		if(command.equals(MainActionCommands.SET_EXPERIMENT_DATA_UPLOAD_PARAMETERS_COMMAND.getName()))
			setExperimentDataUploadParameters();
		
		if(command.equals(MainActionCommands.SEND_EXPERIMENT_DATA_TO_DATABASE_COMMAND.getName()))
			saveExperimentToDatabaseAsNewExperiment();		
	}
	
	private void setExperimentDataUploadParameters() {
		
		RawDataAnalysisProject activeExperiment = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment();
		if(activeExperiment == null)
			return;
		
		if (activeExperiment.getMSMSDataFiles().isEmpty() 
				|| activeExperiment.getMsMsFeatureBundles().isEmpty()) {
			MessageDialog.showErrorMsg("No data to upload in the current experiment");
			return;
		}		
		if(activeExperiment.getIdTrackerExperiment() != null 
				&& activeExperiment.getIdTrackerExperiment().getId() != null) {
			
			LIMSExperiment existingExperiment = IDTDataCash.getExperimentById(
					activeExperiment.getIdTrackerExperiment().getId());
			if(existingExperiment != null) {
				MessageDialog.showErrorMsg("Current experiment already uploaded to database as\n"
						+ "experiment " + existingExperiment.toString());
				return;
			}
		}		
		Map<LIMSExperiment, Collection<DataFile>> existingDataFiles = 
				checkForExistingDataFiles();
		if(!existingDataFiles.isEmpty()) {
			
			ExistingDataListingDialog fListDialog = 
					new ExistingDataListingDialog(existingDataFiles);
			fListDialog.setLocationRelativeTo(this.getContentPane());
			fListDialog.setVisible(true);
			return;
		}		
		Collection<String>errors = verifyExperimentMetadata();
		if(!errors.isEmpty()) {
			 showExperimentMetadataWizard();
			 MessageDialog.showErrorMsg(
					 StringUtils.join(errors, "\n"), 
					 rawDataExperimentMetadataWizard);
			 return;
		}
		Collection<DataFile>filesWithMissingMSMSData = new ArrayList<DataFile>();
		for(DataFile df : activeExperiment.getMSMSDataFiles()) {
			if(activeExperiment.getMsFeaturesForDataFile(df) == null 
					|| activeExperiment.getMsFeaturesForDataFile(df).isEmpty())
				filesWithMissingMSMSData.add(df);
		}
		if(!filesWithMissingMSMSData.isEmpty()) {
			List<String> fileNames = filesWithMissingMSMSData.stream().
					map(f -> f.getBaseName()).collect(Collectors.toList());
			
			 MessageDialog.showErrorMsg("The following files have no extracted MSMS features:\n" + 
					 StringUtils.join(fileNames, "\n"), 
					 this.getContentPane());
			 return;
		}
		MSMSExtractionParameterSet deMethod = 
				activeExperiment.getMsmsExtractionParameterSet();
		if(deMethod == null) {
			 MessageDialog.showErrorMsg("MSMS extraction method not defined for the experiment", 
					 this.getContentPane());
			 return;
		}	
		String methodMd5 = deMethod.getParameterSetHash();
		DataExtractionMethod existingDeMethod = 
				 IDTDataCash.getDataExtractionMethodByMd5(methodMd5);
		
		boolean allowEdit = false;
			 
	    if(existingDeMethod == null) {  //	Upload new method
	    	
			allowEdit = true;
			try {
				existingDeMethod = 
						IDTUtils.insertNewTrackerDataExtractionMethod(deMethod);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MessageDialog.showErrorMsg("Failed to upload data analysis method for the experiment", 
						 this.getContentPane());
				return;
			}
	    }	
	    IDTDataCash.getDataExtractionMethods().add(existingDeMethod);
		rawDataAnalysisExperimentDatabaseUploadDialog = 
				new RawDataAnalysisExperimentDatabaseUploadDialog(this);
		rawDataAnalysisExperimentDatabaseUploadDialog.setDataExtractionMethod(existingDeMethod, allowEdit);
		rawDataAnalysisExperimentDatabaseUploadDialog.setLocationRelativeTo(this.getContentPane());
		rawDataAnalysisExperimentDatabaseUploadDialog.setVisible(true);
	}
	
	private void saveExperimentToDatabaseAsNewExperiment() {
		
		double msOneMZWindow = 
				rawDataAnalysisExperimentDatabaseUploadDialog.getMsOneMZWindow();
		if(msOneMZWindow <= 0.0d) {
			MessageDialog.showErrorMsg(
					"MS1 M/Z window must be > 0", 
					rawDataAnalysisExperimentDatabaseUploadDialog);
			return;
		}	
		RawDataAnalysisExperimentDatabaseUploadTask task = 
				new RawDataAnalysisExperimentDatabaseUploadTask(
						MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment(),
						msOneMZWindow);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		rawDataAnalysisExperimentDatabaseUploadDialog.dispose();
	}
	
	private Collection<String> verifyExperimentMetadata() {
				
		rawDataExperimentMetadataWizard = 
				new RDEMetadataWizard(this, 
						MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment());
		Collection<String>errors = 
				rawDataExperimentMetadataWizard.silentlyVerifyExperimentMetadata();
		rawDataExperimentMetadataWizard.dispose();
		
		return errors;
	}

	private void showExperimentMetadataWizard() {
		
		RawDataAnalysisProject experiment = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment();
		
		if (experiment == null)
			return;
		
		if(experiment.getIdTrackerExperiment() != null 
				&& experiment.getIdTrackerExperiment().getId() != null) {
			
			LIMSExperiment existingExperiment = IDTDataCash.getExperimentById(
					experiment.getIdTrackerExperiment().getId());
			if(existingExperiment != null) {
				MessageDialog.showErrorMsg("Current project already uploaded to database as\n"
						+ "experiment " + existingExperiment.toString());
				return;
			}
		}	
		if (experiment.getMSMSDataFiles().isEmpty() 
				|| experiment.getMsMsFeatureBundles().isEmpty()) {
			MessageDialog.showErrorMsg("No data to upload in the current project");
			return;
		}		
		Map<LIMSExperiment, Collection<DataFile>> existingDataFiles = 
				checkForExistingDataFiles();
		if(!existingDataFiles.isEmpty()) {
			
			ExistingDataListingDialog fListDialog = 
					new ExistingDataListingDialog(existingDataFiles);
			fListDialog.setLocationRelativeTo(this.getContentPane());
			fListDialog.setVisible(true);
			return;
		}		
		LIMSExperiment idtExperiment = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getIdTrackerExperiment();

		if(idtExperiment != null && idtExperiment.getId() != null) {
			
			LIMSExperiment exiting = IDTDataCash.getExperimentById(idtExperiment.getId());
			if(exiting != null) {
				
				MessageDialog.showWarningMsg(
						"The current experiment was already uploaded "
						+ "to the database as experiment " + exiting.getId() 
						+ "\n\"" + exiting.getName() + "\"\n"
						+ "Its metadata can not be edited through the wizard now.", 
						this.getContentPane());
				return;
			}
		}				
		rawDataExperimentMetadataWizard = 
				new RDEMetadataWizard(this, 
						MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment());
		rawDataExperimentMetadataWizard.setLocationRelativeTo(this.getContentPane());
		rawDataExperimentMetadataWizard.setVisible(true);
	}
	
	private Map<LIMSExperiment, Collection<DataFile>> checkForExistingDataFiles(){
		
		Map<LIMSExperiment, Collection<DataFile>> existingDataFiles = 
				new TreeMap<LIMSExperiment, Collection<DataFile>>();
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() == null)
			return existingDataFiles;
		
		try {
			existingDataFiles = 
				IDTRawDataUtils.getExistingDataFiles(
					MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMSMSDataFiles());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return existingDataFiles;
	}

	private void sendMSMSFeaturesToIDTrackerWorkbench() {
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() == null)
			return;
		
		Collection<DataFile> files = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMSMSDataFiles();
		
		int fCount = 0;
		for(DataFile df : files) {
			 Collection<MSFeatureInfoBundle> fileFeatures = 
					 MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMsFeaturesForDataFile(df);
			 if(fileFeatures != null && !fileFeatures.isEmpty())
				 fCount += fileFeatures.size();
		}
		if(fCount == 0)
			return;
		
		IDWorkbenchPanel workbench  = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
		workbench.loadFeaturesFromRawDataExperiment(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment());
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.ID_WORKBENCH);
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMsmsClusterDataSets().size() == 1) {
			workbench.loadMSMSClusterDataSet(
					MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMsmsClusterDataSets().iterator().next());
		}
	}
	
	private void setupMSMSFeatureExtraction() {
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() == null)
			return;
		
		msmsFeatureExtractionSetupDialog = new MSMSFeatureExtractionSetupDialog(this);		
		MSMSExtractionParameterSet ps = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMsmsExtractionParameterSet();
		if(ps != null) {
			DataExtractionMethod deMethod = 
					IDTDataCash.getDataExtractionMethodById(ps.getId());
			msmsFeatureExtractionSetupDialog.loadParameters(ps, deMethod);
		}		
		msmsFeatureExtractionSetupDialog.setLocationRelativeTo(this.getContentPane());
		msmsFeatureExtractionSetupDialog.setVisible(true);
	}

	private void extractMSMSFeatures() {
		
		if(!MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMsMsFeatureBundles().isEmpty()) {
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to discard previously extracted "
					+ "MSMS data for the current experiment?", 
					msmsFeatureExtractionSetupDialog);
			if(res != JOptionPane.YES_OPTION) 
				return;
		}
		MSMSExtractionParameterSet ps = 
				msmsFeatureExtractionSetupDialog.getMSMSExtractionParameterSet();
		if(ps == null)
			return;
		
		cleanupForReanalysis();

		//	Get existing or create new data extraction 
		//	method based on MSMSExtractionParameterSet
		DataExtractionMethod experimentDataExtractionMethod = null;		
		String methodMd5 = ps.getParameterSetHash();
		
		//	Check if existing method was changed
		if(msmsFeatureExtractionSetupDialog.getInitialParameterSet() != null 
				&& msmsFeatureExtractionSetupDialog.getDataExtractionMethod() != null
				&& msmsFeatureExtractionSetupDialog.getInitialParameterSet().getParameterSetHash().equals(methodMd5)) {
			experimentDataExtractionMethod = msmsFeatureExtractionSetupDialog.getDataExtractionMethod();
			ps = msmsFeatureExtractionSetupDialog.getInitialParameterSet();
		}
		else {
	    	DataExtractionMethod sameNameDeMethod = 
					 IDTDataCash.getDataExtractionMethodByName(ps.getName());
	    	if(sameNameDeMethod != null) {
	    		
	    		String version = " V-" + ExperimentUtils.dateTimeFormat.format(new Date());
	    		String newName = ps.getName() + version;
	    		String newDescription = ps.getDescription() + "\n" + version + 
	    				"("+ MRC2ToolBoxCore.getIdTrackerUser().getFullName() + ")";	    		
	    		ps.setName(newName);
	    		ps.setDescription(newDescription);
	    	}	
	    	//	Upload new method
			try {
				experimentDataExtractionMethod = 
						IDTUtils.insertNewTrackerDataExtractionMethod(ps);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MessageDialog.showErrorMsg("Failed to upload data analysis method for the experiment", 
						 this.getContentPane());
				return;
			}
			if(experimentDataExtractionMethod != null) {
				IDTDataCash.getDataExtractionMethods().add(experimentDataExtractionMethod);
				ps.setId(experimentDataExtractionMethod.getId());
				MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().
					setMsmsExtractionParameterSet(ps);
			}
		}		
		MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().setMsmsExtractionParameterSet(ps);
		MsMsfeatureBatchExtractionTask task = 
				new MsMsfeatureBatchExtractionTask(
						ps, 
						experimentDataExtractionMethod,
						MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMSMSDataFiles(), 
						MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMSOneDataFiles());			
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
		msmsFeatureExtractionSetupDialog.dispose();
	}
	
	private void cleanupForReanalysis() {
		
		MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().clearMSMSFeatures();
		dataFileTreePanel.clearPanel();
		chromatogramPanel.clearPanel();
		msPlotPanel.clearPanel();
		msTable.clearTable();
		msmsPlotPanel.clearPanel();
		msmsTable.clearTable();
		rawDataFilePropertiesTable.clearTable();
		scanNavigationPanel.clearPanel();
		
		IDWorkbenchPanel workbench  = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
		workbench.clearPanel();		
		dataFileTreePanel.loadData(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getDataFiles(), true);	
		dataFileTreePanel.toggleTreeExpanded(dataFileTreePanel.isTreeExpanded());
	}	

	public void showNewRawDataAnalysisExperimentDialog() {
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() != null) {
			MessageDialog.showWarningMsg(
					"Please close active raw data analysis experiment \"" + 
					MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getName() + 
					"\" first.", 
					this.getContentPane());
			return;
		}		
		rawDataAnalysisExperimentSetupDialog = new RawDataAnalysisExperimentSetupDialog(this);
		rawDataAnalysisExperimentSetupDialog.setLocationRelativeTo(this.getContentPane());
		rawDataAnalysisExperimentSetupDialog.setVisible(true);
	}
	
	private void showNewRawDataAnalysisExperimentEditor() {
		//	TODO
	}
	
	private void saveChangesToExperiment() {
		//	TODO
	}
	
	private void openRawDataAnalysisExperiment() {		

		if (MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() != null) {

			int selectedValue = MessageDialog.showChooseOrCancelMsg(
					"You are going to close current experiment, do you want "
					+ "to save the results (Yes - save, No - discard)?", 
					this.getContentPane());

			if (selectedValue == JOptionPane.YES_OPTION) {

				runSaveExperimentTask();
				clearGuiAfterExperimentClosed();
				//	TODO clear tracker workbench if used
				MRC2ToolBoxCore.getMainWindow().setTitle(BuildInformation.getProgramName());
				MRC2ToolBoxCore.setActiveRawDataAnalysisExperiment(null);
				showOpenExperimentDialog = true;
			}
			if (selectedValue == JOptionPane.NO_OPTION) {

				clearGuiAfterExperimentClosed();
				//	TODO clear tracker workbench if used
				MRC2ToolBoxCore.getMainWindow().setTitle(BuildInformation.getProgramName());
				MRC2ToolBoxCore.setActiveRawDataAnalysisExperiment(null);
				initRawDataAnalysisExperimentLoadTask();
			}
			if (selectedValue == JOptionPane.CANCEL_OPTION)
				return;
		}
		else {
			initRawDataAnalysisExperimentLoadTask();
		}
	}	
	
	private void saveRawDataAnalysisExperiment() {

		if (MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() != null)
			runSaveExperimentTask();
	}
	
	public void runSaveExperimentTask() {

		if(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() == null)
			return;
		
		SaveStoredRawDataAnalysisExperimentTask task = 
				new SaveStoredRawDataAnalysisExperimentTask(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void initRawDataAnalysisExperimentLoadTask() {

		JFileChooser chooser = new ImprovedFileChooser();
		chooser.setCurrentDirectory(baseDirectory);
		chooser.setPreferredSize(new Dimension(800, 640));
		chooser.setDialogTitle("Select raw data analysis project file");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
		FileNameExtensionFilter experimentFileFilter = 
				new FileNameExtensionFilter("Raw data experiment files",
				MRC2ToolBoxConfiguration.RAW_DATA_EXPERIMENT_FILE_EXTENSION);
		chooser.setFileFilter(experimentFileFilter);	
		RawDataExperimentOpenComponent acc = 
				new RawDataExperimentOpenComponent(chooser);
		chooser.setAccessory(acc);
		chooser.setSize(800, 640);
		if (chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
			
			File experimentFile = null;
			boolean loadResults = acc.loadResults();
			File selectedFile = chooser.getSelectedFile();
			if(selectedFile.isDirectory()) {
				List<String> pfList = FIOUtils.findFilesByExtension(
						Paths.get(selectedFile.getAbsolutePath()), 
						MRC2ToolBoxConfiguration.RAW_DATA_EXPERIMENT_FILE_EXTENSION);
				if(pfList == null || pfList.isEmpty()) {
					MessageDialog.showWarningMsg(selectedFile.getName() + 
							" is not a valid experiment", chooser);
					return;
				}
				experimentFile = new File(pfList.get(0));
				baseDirectory = selectedFile.getParentFile();
			}
			else {
				experimentFile = selectedFile;
				baseDirectory = experimentFile.getParentFile().getParentFile();
			}
			savePreferences();
			OpenStoredRawDataAnalysisExperimentTask ltp = 
					new OpenStoredRawDataAnalysisExperimentTask(experimentFile, loadResults);
			ltp.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(ltp);
		}
	}

	public void closeRawDataAnalysisExperiment(boolean exitProgram) {

		if (MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() == null) {
			
			if(exitProgram) {
				if (MessageDialog.showChoiceWithWarningMsg(
						"Are you sure you want to exit?", this.getContentPane()) == JOptionPane.YES_OPTION)
					MRC2ToolBoxCore.shutDown();
			}
			else {
				return;
			}
		}
		String yesNoQuestion = "You are going to close current experiment,"
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
			
			runSaveExperimentTask();
			return;
		}
		if (selectedValue == JOptionPane.NO_OPTION) {
			
			if(exitProgram) {
				selectedValue = 
						MessageDialog.showChoiceWithWarningMsg("Are you sure you want to exit?", 
								this.getContentPane());
				if (selectedValue == JOptionPane.YES_OPTION)
					MRC2ToolBoxCore.shutDown();
			}
			else {
				clearGuiAfterExperimentClosed();
			}
		}			
	}
	
	public void clearGuiAfterExperimentClosed() {		
		MRC2ToolBoxCore.setActiveRawDataAnalysisExperiment(null);
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
	
//	private void initChooser() {
//
//		chooser = new ImprovedFileChooser();
//		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setMultiSelectionEnabled(true);
//		File rawDataRepository = new File(MRC2ToolBoxConfiguration.getRawDataRepository());
//		if(rawDataRepository.exists() && rawDataRepository.isDirectory())
//			chooser.setCurrentDirectory(rawDataRepository);
//
//		chooser.setFileFilter(
//				new FileNameExtensionFilter("Raw MS files", "mzml", "mzML", "mzXML", "mzxml"));
//	}

	private void openRawDataFiles() {
		
		JnaFileChooser fc = new JnaFileChooser(MRC2ToolBoxConfiguration.getRawDataRepository());
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Raw MS files", "mzml", "mzML", "mzXML", "mzxml");
		fc.setTitle("Select raw data files");
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File[] selectedFiles = fc.getSelectedFiles();
			if(selectedFiles.length == 0)
				return;
			
			baseDirectory = selectedFiles[0].getParentFile();
			savePreferences();
			ArrayList<File>filesToOpen = new ArrayList<File>();
			for(File rf : selectedFiles) {
				
				if(RawDataManager.getRawData(rf) == null)
					filesToOpen.add(rf);
			}
			if(filesToOpen.isEmpty()) {
				MessageDialog.showWarningMsg("All selected files already opened.", this.getContentPane());
				return;
			}
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
			for(DataFile df : newRawFiles) {
				if(df.getInjectionTime() == null) {
					LCMSData rd = RawDataManager.getRawData(df);
					df.setInjectionTime(rd.getSource().getRunInfo().getRunStartTime());
				}				
			}
			return null;
		}
	}
	
	class ShowRawDataFilesTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private List<DataFile>files;

		public ShowRawDataFilesTask(List<DataFile>files2show) {
			this.files = files2show;
		}
		
		@Override
		public Void doInBackground() {

			try {
				xicSetupPanel.selectFiles(files);
				msExtractorPanel.selectFiles(files);
				
				LCMSData data = RawDataManager.getRawData(files.get(0));
				rawDataFilePropertiesTable.showDataFileProperties(data);
				
				scanNavigationPanel.setModelFromDataFile(files.get(0));
				
				List<ExtractedChromatogram> chromList = files.stream().
						flatMap(f -> f.getChromatograms().stream()).
						filter(c -> c.getChromatogramDefinition().getMode().equals(ChromatogramPlotMode.TIC)).
						collect(Collectors.toList());
				chromatogramPanel.showExtractedChromatogramCollection(chromList);		
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
		msmsPlotPanel.clearPanel();
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
			
			if(e.getSource().getClass().equals(ExperimentRawDataFileOpenTask.class))
				finalizeExperimentRawDataLoad((ExperimentRawDataFileOpenTask)e.getSource());
			
			if (e.getSource().getClass().equals(SaveStoredRawDataAnalysisExperimentTask.class))
				finalizeRawDataAnalysisExperimentSave();
			
			if (e.getSource().getClass().equals(OpenStoredRawDataAnalysisExperimentTask.class))
				finalizeStoredRawDataAnalysisExperimentOpen(
						(OpenStoredRawDataAnalysisExperimentTask)e.getSource());			
			
			if (e.getSource().getClass().equals(MsMsfeatureBatchExtractionTask.class))
				finalizeRawMSMSBatchExtractionTask(
						(MsMsfeatureBatchExtractionTask)e.getSource());
			
			if (e.getSource().getClass().equals(RawDataAnalysisExperimentDatabaseUploadTask.class))
				finalizeRawDataAnalysisExperimentDatabaseUploadTask(
						(RawDataAnalysisExperimentDatabaseUploadTask)e.getSource());		
		}
	}
	
	private void finalizeRawDataAnalysisExperimentDatabaseUploadTask(
			RawDataAnalysisExperimentDatabaseUploadTask task) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.ID_TRACKER_LIMS);
		IDTrackerLimsManagerPanel limsPanel = 
				(IDTrackerLimsManagerPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_TRACKER_LIMS);
		limsPanel.refreshIdTrackerdata();

		//	Force project save
		showNewExperimentDialog = false;
		saveOnCloseRequested = false;
		saveOnExitRequested = false;
		showOpenExperimentDialog = false;
		SaveStoredRawDataAnalysisExperimentTask saveTask = 
				new SaveStoredRawDataAnalysisExperimentTask(
						MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment());
		saveTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(saveTask);
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
		Map<DataFile, Collection<MSFeatureInfoBundle>> featureMap = task.getMsFeatureMap();	
		Collection<String>log = new ArrayList<String>();
		for(Entry<DataFile, Collection<MSFeatureInfoBundle>>e : featureMap.entrySet()) {
			MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().setMsFeaturesForDataFile(e.getKey(), e.getValue());
			log.add(e.getKey().getName() + " -> " + Integer.toString(e.getValue().size()) + " features");
		}
		MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().setChromatogramMap(task.getChromatogramMap());
		MessageDialog.showInfoMsg(StringUtils.join(log, "\n"), this.getContentPane());
		OpenRawDataFilesTask ordTask = new OpenRawDataFilesTask(
				MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getMSMSDataFiles(), false);
		idp = new IndeterminateProgressDialog("Loading raw data tree ...", this.getContentPane(), ordTask);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	private void finalizeStoredRawDataAnalysisExperimentOpen(OpenStoredRawDataAnalysisExperimentTask task) {

		if(!task.getErrors().isEmpty()) {
			
			MessageDialog.showErrorMsg(
					StringUtils.join(task.getErrors(), "\n"), this.getContentPane());
			return;
		}
		MRC2ToolBoxCore.setActiveRawDataAnalysisExperiment(task.getExperiment());	
		OpenRawDataFilesTask ordTask = new OpenRawDataFilesTask(
				MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getDataFiles());
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		StatusBar.clearExperimentData();
		StatusBar.setExperimentName(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getName());
		idp = new IndeterminateProgressDialog("Loading raw data tree ...", this.getContentPane(), ordTask);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);		
		sendMSMSFeaturesToIDTrackerWorkbench();
	}

	private void finalizeRawDataAnalysisExperimentSave() {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();		
		if(showNewExperimentDialog) {

			showNewExperimentDialog = false;
			clearGuiAfterExperimentClosed();
			rawDataAnalysisExperimentSetupDialog = new RawDataAnalysisExperimentSetupDialog(this);
			rawDataAnalysisExperimentSetupDialog.setLocationRelativeTo(this.getContentPane());
			rawDataAnalysisExperimentSetupDialog.setVisible(true);
		}
		if(saveOnCloseRequested) {

			saveOnCloseRequested = false;
			clearGuiAfterExperimentClosed();
		}
		if(saveOnExitRequested) {

			saveOnExitRequested = false;
			int selectedValue = 
					MessageDialog.showChoiceWithWarningMsg("Are you sure you want to exit?", 
							this.getContentPane());
			if (selectedValue == JOptionPane.YES_OPTION)
				MRC2ToolBoxCore.shutDown();
		}
		if(showOpenExperimentDialog) {

			clearGuiAfterExperimentClosed();
			showOpenExperimentDialog = false;
			initRawDataAnalysisExperimentLoadTask();
		}
	}
	
	public void finalizeExperimentRawDataLoad(ExperimentRawDataFileOpenTask task) {
		
		Collection<String> errors = task.getErrors();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this.getContentPane());
			ExperimentUtils.saveExperimentFile(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment());
		}
		MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().updateExperimentLocation(
				MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getExperimentFile());
		Collection<DataFile> filesToLoad = new ArrayList<DataFile>();
		filesToLoad.addAll(task.getExperiment().getMSMSDataFiles());
		filesToLoad.addAll(task.getExperiment().getMSOneDataFiles());		
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
		msmsPlotPanel.clearPanel();
		msmsTable.clearTable();
		xicSetupPanel.clearPanel();
		msExtractorPanel.clearPanel();
		rawDataFilePropertiesTable.clearTable();
		scanNavigationPanel.clearPanel();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		if(e.getValueIsAdjusting() || e.getSource() == null)
			return;

		for(ListSelectionListener listener : ((DefaultListSelectionModel)e.getSource()).getListSelectionListeners()) {

			if(listener.equals(scanNavigationPanel.getTable())) {
				
				IScan scan = scanNavigationPanel.getSelectedScan();
				if(scan != null)
					showScan(scan);
			}
		}
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
		if(selected.stream().findFirst().get() instanceof MSFeatureInfoBundle)
			showMsFeatureInfoBundle(((MSFeatureInfoBundle)selected.stream().findFirst().get()));
		
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

	private void showMsFeatureInfoBundle(MSFeatureInfoBundle msFeatureInfoBundle) {

		MsFeature msFeature = msFeatureInfoBundle.getMsFeature();
		msPlotPanel.showMsForFeature(msFeature, false);
		msTable.setTableModelFromMsFeature(msFeature);
		msmsPlotPanel.clearPanel();
		msmsTable.clearTable();
	
		if(msFeature.getSpectrum() != null) {
			TandemMassSpectrum msms = 
					msFeature.getSpectrum().getExperimentalTandemSpectrum();
			if(msms != null) {
				msmsPlotPanel.showTandemMs(msms);;
				msmsTable.setTableModelFromTandemMs(msms);
			}
		}
		DataFile df = msFeatureInfoBundle.getDataFile();
		if(df != null) {
			xicSetupPanel.selectFiles(Collections.singleton(df));
			msExtractorPanel.selectFiles(Collections.singleton(df));
		}
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment() != null && 
				!MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getChromatogramMap().isEmpty()) {
			
			MsFeatureChromatogramBundle msfCb = 
					MRC2ToolBoxCore.getActiveRawDataAnalysisExperiment().getChromatogramMap().get(msFeature.getId());			
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
		
		ShowRawDataFilesTask task = new ShowRawDataFilesTask(files);
		idp = new IndeterminateProgressDialog("Loading raw data display ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	private void showAverageMassSpectrum(AverageMassSpectrum averageMassSpectrum) {
		
		if(averageMassSpectrum.getMsLlevel() == 1) {
			msPlotPanel.showMsDataSet(new MsDataSet(averageMassSpectrum));
			msTable.setTableModelFromSpectrum(averageMassSpectrum.getMasSpectrum());
		}		
		if(averageMassSpectrum.getMsLlevel() > 1) {
			msmsPlotPanel.showMsDataSet(new MsDataSet(averageMassSpectrum));
			msmsTable.setTableModelFromDataPoints(
					averageMassSpectrum.getMasSpectrum().getMsPoints(), null);
		}
		DataFile df = averageMassSpectrum.getDataFile();
		if(df != null) {
			xicSetupPanel.selectFiles(Collections.singleton(df));
			msExtractorPanel.selectFiles(Collections.singleton(df));
		}
	}

	private void showScan(IScan s) {
		
		if(s.getSpectrum() == null) {
			try {
				s.fetchSpectrum();
			} catch (FileParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(s.getMsLevel() == 1) {
			msPlotPanel.showScan(s);
			msTable.setTableModelFromScan(s);
			msmsPlotPanel.clearPanel();
			msmsTable.clearTable();
			
			IScan next = s.getScanCollection().getNextScan(s.getNum());
			if(next.getMsLevel() == 2) {
				msmsPlotPanel.showScan(next);
				msmsTable.setTableModelFromScan(next);
			}
		}
		else {
			msmsPlotPanel.showScan(s);
			msmsTable.setTableModelFromScan(s);
			
			IScan parent = 
					s.getScanCollection().getPrevScanAtMsLevel(s.getNum(), 1);
//			IScan parent = getParentScan(s);
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
		
		if(s.getMsLevel() == 1)
			return null;
		
		if(s.getPrecursor() != null) {
			
			int parentScanNumber = s.getPrecursor().getParentScanNum();			
			return s.getScanCollection().getScanByNum(parentScanNumber);
			
//			DataFile df = dataFileTreePanel.getDataFileForScan(s);
//			if(df != null) {
//				LCMSData data = RawDataManager.getRawData(df);
//				if(data != null) {
//					IScanCollection scans = data.getScans();
//					return scans.getScanByNum(parentScanNumber);
//				}
//			}
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
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =
			new File(preferences.get(BASE_DIRECTORY,
				MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(RawDataExaminerPanel.class.getName()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(RawDataExaminerPanel.class.getName());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	@Override
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}
}








































