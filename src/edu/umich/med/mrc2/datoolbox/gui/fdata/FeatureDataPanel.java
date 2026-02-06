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

package edu.umich.med.mrc2.datoolbox.gui.fdata;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MSRTSearchParametersObject;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.data.MzRtFilter;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataImputationType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataTypeForImport;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureFilter;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSetProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MissingExportType;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.annotation.DockableObjectAnnotationPanel;
import edu.umich.med.mrc2.datoolbox.gui.binner.display.DockableBinnerAnnotationDetailsPanel;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.FilterTreeDialog;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.DatabaseSearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.datexp.DataExplorerPlotFrame;
import edu.umich.med.mrc2.datoolbox.gui.datexp.msone.MultiMSFeatureQCPlotFrame;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates.DuplicateMergeDialog;
import edu.umich.med.mrc2.datoolbox.gui.expdesign.pools.ExperimentPooledSampleManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.fdata.cleanup.FeatureCleanupParameters;
import edu.umich.med.mrc2.datoolbox.gui.fdata.cleanup.FeatureDataCleanupDialog;
import edu.umich.med.mrc2.datoolbox.gui.fdata.corr.DockableCorrelationDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.fdata.noid.MissingIdentificationsDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtable.DockableUniversalIdentificationResultsTable;
import edu.umich.med.mrc2.datoolbox.gui.idtable.MetabolomicsIdentificationTableModelListener;
import edu.umich.med.mrc2.datoolbox.gui.integration.DataIntegratorPanel;
import edu.umich.med.mrc2.datoolbox.gui.io.DataExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.IntegratedReportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.MultiFileDataImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.PeakQualityImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.TargetedDataFileSelectionDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.excel.ExcelImportWizard;
import edu.umich.med.mrc2.datoolbox.gui.io.mwtab.MWTabExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.txt.TextDataImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.library.MsLibraryPanel;
import edu.umich.med.mrc2.datoolbox.gui.library.search.LibrarySearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.mzdelta.MZDeltaAnalysisDialog;
import edu.umich.med.mrc2.datoolbox.gui.mzdelta.MZDeltaAnalysisParametersObject;
import edu.umich.med.mrc2.datoolbox.gui.mzfreq.MzFrequencyAnalysisResultsDialog;
import edu.umich.med.mrc2.datoolbox.gui.mzfreq.MzFrequencyAnalysisSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.DockableDataPlot;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.DockableMsTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.FindDuplicateNamesTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.MergeDuplicateFeaturesTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration.MsFeatureAveragingTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.DataExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ImportBinnerAnnotationsForUntargetedDataTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.MiltiCefPeakQualityImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.MultiCefDataAddTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.MultiCefImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ProFinderResultsImportTaskTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.QuantMatrixImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.TargetedDataMatrixImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.ClearIdentificationsTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LibrarySearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LoadDatabaseLibraryTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveMetabolomicsProjectTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SavePipelineDataTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.CalculateStatisticsTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.ImputeMissingDataTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.MZDeltaAnalysisTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.MzFrequencyAnalysisTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.MzFrequencyType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.RemoveEmptyFeaturesTask;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.MetabolomicsProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.ProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class FeatureDataPanel extends DockableMRC2ToolboxPanel implements ListSelectionListener {

	private DockableFeatureDataTable featureDataTable;
	private DockableDataPlot dataPlot;
	private DockableFeatureIntensitiesTable featureIntensitiesTable;
	private DockableSpectumPlot spectrumPlot;
	private DockableMsTable spectrumTable;
	private DockableMolStructurePanel molStructurePanel;
	private DockableObjectAnnotationPanel featureAnnotationPanel;
	private DockableCorrelationDataPanel correlationPanel;
	private DockableBinnerAnnotationDetailsPanel binnerAnnotationDetailsPanel;
	private DockableUniversalIdentificationResultsTable identificationsTable;
	private LibrarySearchSetupDialog librarySearchSetupDialog;
	private DatabaseSearchSetupDialog databaseSearchSetupDialog;
	private DataImputationSetupDialog dataImputationSetupDialog;
	private FilterTreeDialog filterFeaturesDialog;
	private DuplicateMergeDialog duplicateMergeDialog;
	private MissingIdentificationsDialog missingIdentificationsDialog;

	private MsFeatureSet activeMsFeatureSet;
	private Map<DataPipeline, Collection<MsFeature>> selectedFeaturesMap;
	private Collection<CompoundLibrary> libsToLoad;
	private Collection<CompoundLibrary> missingLookupLibs;
	private int loadedLibsCount;
	private Set<MsFeature> identifiedFeatures;
	private FeatureFilter activeFeatureFilter;
	
	private MultiFileDataImportDialog multiFileDataImportDialog;
	private ExcelImportWizard excelImportWizard;
	private TextDataImportDialog textDataImportDialog;
	private DataExportDialog exportDialog;
	private MWTabExportDialog mwTabExportDialog;
	private IntegratedReportDialog integratedReportDialog;
	private FeatureDataCleanupDialog featureDataCleanupDialog;
	private PeakQualityImportDialog peakQualityImportDialog;
	
	private MzFrequencyAnalysisSetupDialog mzFrequencyAnalysisSetupDialog;
	private ExperimentPooledSampleManagerDialog experimentPooledSampleManagerDialog;
	private MultiMSFeatureQCPlotFrame multiSpectraPlotFrame;
	private FeatureAveragingSetupDialog featureAveragingSetupDialog;
	private MZDeltaAnalysisDialog mzDeltaAnalysisDialog;
	private FilterFeaturesByMzRtListDialog filterFeaturesByMzRtListDialog;

	private static final Icon componentIcon = GuiUtils.getIcon("barChart", 16);
	private static final Icon loadLibraryIcon = GuiUtils.getIcon("loadLibrary", 24);
	private static final Icon loadPlainDataFileIcon = GuiUtils.getIcon("importTextfile", 24);
	private static final Icon loadMultiFileIcon = GuiUtils.getIcon("importMultifile", 24);	
	private static final Icon addMultiFileIcon = GuiUtils.getIcon("addMultifile", 24);
	private static final Icon loadFromExcelIcon = GuiUtils.getIcon("excelImport", 24);
	private static final Icon calcStatsIcon = GuiUtils.getIcon("calcStats", 24);
	private static final Icon cleanEmptyFeaturesIcon = GuiUtils.getIcon("cleanEmpty", 24);
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 24);
	private static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 24);
	private static final Icon knownIcon = GuiUtils.getIcon("showKnowns", 24);
	private static final Icon qcIcon = GuiUtils.getIcon("qc", 24);
	private static final Icon unknownIcon = GuiUtils.getIcon("showUnknowns", 24);
	private static final Icon inClusterIcon = GuiUtils.getIcon("inCluster", 24);
	private static final Icon notInClusterIcon = GuiUtils.getIcon("notInCluster", 24);
	private static final Icon searchLibraryIcon = GuiUtils.getIcon("searchLibrary", 24);
	private static final Icon searchDatabaseIcon = GuiUtils.getIcon("searchDatabase", 24);
	private static final Icon showMissingIdentificationsIcon = GuiUtils.getIcon("missingIdentifications", 24);
	private static final Icon clearIdentificationsIcon = GuiUtils.getIcon("clearIdentifications", 24);
	private static final Icon imputeDataIcon = GuiUtils.getIcon("impute", 24);
	private static final Icon bubblePlotIcon = GuiUtils.getIcon("bubble", 24);
	private static final Icon checkDuplicateNamesIcon = GuiUtils.getIcon("checkDuplicateNames", 24);	
	private static final Icon exportResultsIcon = GuiUtils.getIcon("export", 24);
	private static final Icon exportExcelIcon = GuiUtils.getIcon("excel", 24);
	private static final Icon exportMwTabIcon = GuiUtils.getIcon("mwTabReport", 24);

	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "FeatureStatisticsPanel.layout");

	public FeatureDataPanel() {

		super("FeatureDataPanel", PanelList.FEATURE_DATA.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		menuBar = new FeatureDataPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		featureDataTable = new DockableFeatureDataTable(
				"FeatureDataPanelDockableFeatureDataTable", "Feature listing");
		featureDataTable.getTable().addTablePopupMenu(
				new FeaturePopupMenu(this, featureDataTable.getTable()));
		featureDataTable.getTable().getSelectionModel().addListSelectionListener(this);

		dataPlot = new DockableDataPlot(
				"FeatureDataPanelDockableDataPlot", "Data plots");
		featureIntensitiesTable = new DockableFeatureIntensitiesTable(
				"FeatureDataPanelDockableFeatureIntensitiesTable", "Signal intensity table");
		spectrumPlot = new DockableSpectumPlot(
				"FeatureDataPanelDockableSpectumPlot", "Spectrum plot");
		correlationPanel = new DockableCorrelationDataPanel(
				"FeatureDataPanelCorrelationDataPanel", "Feature correlation");
		spectrumTable = new DockableMsTable(
				"FeatureDataPanelDockableMsTableMS1", "MS1 table");
		binnerAnnotationDetailsPanel = new DockableBinnerAnnotationDetailsPanel();
		identificationsTable = new DockableUniversalIdentificationResultsTable(
				"FeatureDataPanelUniversalIdentificationResultsTable", "Identifications");
		identificationsTable.getTable().getSelectionModel().addListSelectionListener(this);
		
		identificationsTable.getTable().setIdentificationTableModelListener(
				new MetabolomicsIdentificationTableModelListener(this));
		
		molStructurePanel = new DockableMolStructurePanel(
				"FeatureDataPanelDockableMolStructurePanel");
		featureAnnotationPanel = new DockableObjectAnnotationPanel(
				"FeatureDataPanelAnnotations", "Annotations", 80);

		grid.add(0, 0, 80, 30, featureDataTable);
		grid.add(80, 0, 20, 30, molStructurePanel);
		grid.add(0, 30, 100, 20, identificationsTable, binnerAnnotationDetailsPanel);
		grid.add(0, 50, 50, 50, dataPlot, featureIntensitiesTable, correlationPanel);
		grid.add(50, 50, 50, 50, spectrumPlot, spectrumTable, featureAnnotationPanel);
		grid.select(0, 50, 50, 50, dataPlot);
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
		
		dataImputationSetupDialog = new DataImputationSetupDialog(this);
		duplicateMergeDialog = new DuplicateMergeDialog(this);
		selectedFeaturesMap = new TreeMap<>();
		activeMsFeatureSet = null;
		activeFeatureFilter = FeatureFilter.ALL_FEATURES;
	}

	@Override
	protected void initActions() {
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_DATA_FROM_MULTIFILES_COMMAND.getName(),
				MainActionCommands.LOAD_DATA_FROM_MULTIFILES_COMMAND.getName(), 
				loadMultiFileIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_DATA_FROM_MULTIFILES_COMMAND.getName(),
				MainActionCommands.ADD_DATA_FROM_MULTIFILES_COMMAND.getName(), 
				addMultiFileIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_DATA_FROM_EXCEL_FILE_COMMAND.getName(),
				MainActionCommands.LOAD_DATA_FROM_EXCEL_FILE_COMMAND.getName(), 
				loadFromExcelIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_DATA_COMMAND.getName(),
				MainActionCommands.LOAD_DATA_COMMAND.getName(), 
				loadPlainDataFileIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_LIBRARY_COMMAND.getName(),
				MainActionCommands.LOAD_LIBRARY_COMMAND.getName(), 
				loadLibraryIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CALC_FEATURES_STATS_COMMAND.getName(),
				MainActionCommands.CALC_FEATURES_STATS_COMMAND.getName(), 
				calcStatsIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLEAN_EMPTY_FEATURES_COMMAND.getName(),
				MainActionCommands.CLEAN_EMPTY_FEATURES_COMMAND.getName(), 
				cleanEmptyFeaturesIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND.getName(),
				MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND.getName(), 
				checkDuplicateNamesIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_KNOWN_FEATURES_COMMAND.getName(),
				MainActionCommands.SHOW_KNOWN_FEATURES_COMMAND.getName(), 
				knownIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_UNKNOWN_FEATURES_COMMAND.getName(),
				MainActionCommands.SHOW_UNKNOWN_FEATURES_COMMAND.getName(), 
				unknownIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_QC_FEATURES_COMMAND.getName(),
				MainActionCommands.SHOW_QC_FEATURES_COMMAND.getName(), 
				qcIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName(),
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName(), 
				filterIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(),
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(), 
				resetFilterIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.MS_RT_LIBRARY_SEARCH_SETUP_COMMAND.getName(),
				MainActionCommands.MS_RT_LIBRARY_SEARCH_SETUP_COMMAND.getName(), 
				searchLibraryIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.COMPOUND_DATABASE_SEARCH_SETUP_COMMAND.getName(),
				MainActionCommands.COMPOUND_DATABASE_SEARCH_SETUP_COMMAND.getName(), 
				searchDatabaseIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_MISSING_IDENTIFICATIONS_COMMAND.getName(),
				MainActionCommands.SHOW_MISSING_IDENTIFICATIONS_COMMAND.getName(), 
				showMissingIdentificationsIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLEAR_IDENTIFICATIONS_COMMAND.getName(),
				MainActionCommands.CLEAR_IDENTIFICATIONS_COMMAND.getName(), 
				clearIdentificationsIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_DATA_EXPLORER_FRAME.getName(),
				MainActionCommands.SHOW_DATA_EXPLORER_FRAME.getName(), 
				bubblePlotIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_RESULTS_COMMAND.getName(),
				MainActionCommands.EXPORT_RESULTS_COMMAND.getName(), 
				exportResultsIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND.getName(),
				MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND.getName(), 
				exportExcelIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND.getName(),
				MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND.getName(), 
				exportMwTabIcon, this));
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

		if (currentExperiment == null || currentExperiment.getExperimentDesign() == null || 
				currentExperiment.getExperimentDesign().getSamples().isEmpty())
			return;

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.LOAD_DATA_FROM_MULTIFILES_COMMAND.getName()))
			showMultifileDataLoader(null, DataTypeForImport.AGILENT_UNTARGETED);
		
		if (command.equals(MainActionCommands.ADD_DATA_FROM_MULTIFILES_COMMAND.getName()))
			showMultifileDataLoader(activeDataPipeline, DataTypeForImport.AGILENT_UNTARGETED);
		
		if (command.equals(MainActionCommands.SETUP_COMPOUND_NAME_VERIFICATION_COMMAND.getName()))
			setupCompoundNameVerification();
		
		if (command.equals(MainActionCommands.LOAD_DATA_FROM_PROFINDER_PFA_COMMAND.getName()))
			showMultifileDataLoader(null, DataTypeForImport.AGILENT_PROFINDER_TARGETED);

		if (command.equals(MainActionCommands.LOAD_TARGETED_DATA_FROM_PLAIN_TEXT_COMMAND.getName()))
			showMultifileDataLoader(null, DataTypeForImport.GENERIC_TARGETED);
		
		if (command.equals(MainActionCommands.LOAD_DATA_FROM_EXCEL_FILE_COMMAND.getName()))
			showExcelDataLoader();

		if (command.equals(MainActionCommands.LOAD_LIBRARY_COMMAND.getName()))
			loadLibrary();
		
		if(activeDataPipeline != null) {
			
			if (command.equals(MainActionCommands.ADD_PEAK_QUALITY_DATA_FROM_MULTIFILES_COMMAND.getName()))
				setUpPeakQualityDataImport();
			
			if (command.equals(MainActionCommands.START_PEAK_QUALITY_DATA_IMPORT_COMMAND.getName()))
				addPeakQualityData();
				
			if (command.equals(MainActionCommands.CALC_FEATURES_STATS_COMMAND.getName()))
				setupDataStatsCalculation();
			
			if (command.equals(MainActionCommands.RECALCULATE_STATISTICS_WITH_SELECTED_POOLS_COMMAND.getName()))
				recalculateDataStatsWithSelectedPools();
			
			if (command.equals(MainActionCommands.CLEAN_EMPTY_FEATURES_COMMAND.getName()))
				cleanEmptyFeatures();
			
			if (command.equals(MainActionCommands.AVERAGE_FEATURES_LIBRARY_SETUP_COMMAND.getName()))
				setupAverageFeaturesLibraryGeneration();
			
			if (command.equals(MainActionCommands.CREATE_AVERAGE_FEATURES_LIBRARY_COMMAND.getName()))
				createAverageFeaturesLibrary();
			
			if (command.equals(MainActionCommands.OPEN_AVERAGE_FEATURES_LIBRARY_COMMAND.getName()))
				loadAverageFeaturesLibrary();
			
			if (command.equals(MainActionCommands.DELETE_AVERAGE_FEATURES_LIBRARY_COMMAND.getName()))
				deleteAverageFeaturesLibrary();
			
			if (command.equals(MainActionCommands.IMPORT_BINNER_ANNOTATIONS_COMMAND.getName()))
				importBinnerAnnotations();
			
			if (command.equals(MainActionCommands.CLEAR_BINNER_ANNOTATIONS_COMMAND.getName()))
				clearBinnerAnnotations();
			
			if (command.equals(MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName()))
				showFeatureFilter();
				
			if (command.equals(MainActionCommands.FILTER_FEATURES_COMMAND.getName()))
				filterFeatureTable();
			
			if (command.equals(MainActionCommands.SHOW_FEATURE_MZ_RT_LIST_FILTER_COMMAND.getName()))
				showFeatureMZRTListFilter();
				
			if (command.equals(MainActionCommands.FILTER_FEATURES_BY_MZ_RT_LIST_COMMAND.getName()))
				filterFeatureTableByMZRTList();
			
			if (command.equals(MainActionCommands.RESET_FEATURE_FILTERS_COMMAND.getName()))
				resetFeatureTable();

			if (command.equals(MainActionCommands.SHOW_KNOWN_FEATURES_COMMAND.getName()))
				showKnownsOnly();

			if (command.equals(MainActionCommands.SHOW_UNKNOWN_FEATURES_COMMAND.getName()))
				showUnknownsOnly();

			if (command.equals(MainActionCommands.SHOW_QC_FEATURES_COMMAND.getName()))
				showQcOnly();
			
			if (command.equals(MainActionCommands.SHOW_BINNER_ANNOTATED_FEATURES_COMMAND.getName()))
				showBinnerAnnotated(false);
			
			if (command.equals(MainActionCommands.SHOW_PRIMARY_BINNER_ANNOTATED_FEATURES_COMMAND.getName()))
				showBinnerAnnotated(true);

			if (command.equals(MainActionCommands.EDIT_FEATURE_METADATA_COMMAND.getName()))
				editFeatureMetaData();

			// if(command.equals(MainActionCommands.ADD_SELECTED_FEATURES_TO_SUBSET_COMMAND.getName()))
			// addSelectedFeaturesToActiveSubset();

			if (command.equals(MainActionCommands.REMOVE_SELECTED_FEATURES_FROM_SUBSET_COMMAND.getName()))
				removeSelectedFeaturesFromActiveSubset();

			if (command.equals(MainActionCommands.SHOW_IMPUTE_DIALOG_COMMAND.getName()))
				showDataImputationDialog();

			if (command.equals(MainActionCommands.IMPUTE_DATA_COMMAND.getName()))
				imputeMissingData();

			if (command.equals(MainActionCommands.SHOW_DATA_EXPLORER_FRAME.getName()))
				showDataExplorerFrame();
			
			if (command.equals(MainActionCommands.SHOW_MS_MULTIPLOT_FRAME.getName()))
				showMsMultiplotFrame();
			
			if (command.equals(MainActionCommands.MS_RT_LIBRARY_SEARCH_SETUP_FOR_SELECTED_COMMAND.getName()))
				showLibrarySearchSetup(TableRowSubset.SELECTED);

			if (command.equals(MainActionCommands.MS_RT_LIBRARY_SEARCH_SETUP_COMMAND.getName()))
				showLibrarySearchSetup(TableRowSubset.ALL);
			
			if (command.equals(MainActionCommands.COMPOUND_DATABASE_SEARCH_SETUP_FOR_SELECTED_COMMAND.getName()))
				showDatabaseSearchSetup(TableRowSubset.SELECTED);
				
			if (command.equals(MainActionCommands.COMPOUND_DATABASE_SEARCH_SETUP_COMMAND.getName()))
				showDatabaseSearchSetup(TableRowSubset.ALL);

			if (command.equals(MainActionCommands.SEARCH_FEATURE_SET_AGAINST_LIBRARIES_COMMAND.getName()))
				runLibrarySearch();

			if (command.equals(MainActionCommands.SEARCH_FEATURE_SET_AGAINST_COMPOUND_DATABASE_COMMAND.getName()))
				runDatabaseSearch();

			if (command.equals(MainActionCommands.SHOW_MISSING_IDENTIFICATIONS_COMMAND.getName()))
				showMissingIdentifications();

			if (command.equals(MainActionCommands.CLEAR_IDENTIFICATIONS_COMMAND.getName()))
				clearAllFeatureIdentifications();

			if (command.equals(MainActionCommands.MERGE_SELECTED_FEATURES_COMMAND.getName()))
				showMergeSelectedFeaturesDialog();

			if (command.equals(MainActionCommands.MERGE_DUPLICATES_COMMAND.getName()))
				mergeSelectedFeatures();

			if (command.equals(MainActionCommands.CLEAR_SELECTED_FEATURE_IDENTIFICATION_COMMAND.getName()))
				clearSelectedFeatureIdentifications();
			
			if (command.equals(MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND.getName()))
				checkForDuplicateNames();
						
			if(DataExportDialog.getExportTypes().contains(command) 
					|| command.equals(MainActionCommands.EXPORT_RESULTS_COMMAND.getName()))
				exportAnalysisResults(command);
			
			if (command.equals(MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND.getName()))
				showMwTabReportDialog();
			
			if (command.equals(MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND.getName()))
				showIntegratedReportDialog();
			
			if (command.equals(MainActionCommands.SHOW_DATA_CLEANUP_DIALOG_COMMAND.getName()))
				showFeatureSetCleanupDialog();
			
			if (command.equals(MainActionCommands.CLEANUP_FEATURE_DATA_COMMAND.getName()))
				cleanUpFeatureSet();	
			
			if (command.equals(MainActionCommands.SET_UP_MZ_FREQUENCY_ANALYSIS_COMMAND.getName()))
				setUpMSMSParentIonFrequencyAnalysis();
					
			if (command.equals(MainActionCommands.RUN_MZ_FREQUENCY_ANALYSIS_COMMAND.getName()))
				runMSMSParentIonFrequencyAnalysis();
			
			if (command.equals(MainActionCommands.EXPORT_FEATURE_STATISTICS_COMMAND.getName()))
				exportStatisticsForActiveFeatureSet();
			
			if (command.equals(MainActionCommands.SET_UP_MZ_DIFFERENCE_ANALYSIS_COMMAND.getName()))
				setUpMZDifferenceAnalysis();
					
			if (command.equals(MainActionCommands.RUN_MZ_DIFFERENCE_ANALYSIS_COMMAND.getName()))
				runMZDifferenceAnalysis();
		}	
	}

	private void setUpMZDifferenceAnalysis() {
		
		mzDeltaAnalysisDialog = new MZDeltaAnalysisDialog(this);
		mzDeltaAnalysisDialog.setLocationRelativeTo(this.getContentPane());
		mzDeltaAnalysisDialog.setVisible(true);
	}

	private void runMZDifferenceAnalysis() {

		Collection<String>errors = mzDeltaAnalysisDialog.validateFormData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(
		            StringUtils.join(errors, "\n"), mzDeltaAnalysisDialog);
		    return;
		}	
		MZDeltaAnalysisParametersObject analysisParameters = new MZDeltaAnalysisParametersObject(
				currentExperiment, 
				activeDataPipeline,
				currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline), 
				mzDeltaAnalysisDialog.getAnchorMassSet(),
				mzDeltaAnalysisDialog.getAnchorMassError(),
				mzDeltaAnalysisDialog.getAnchorMassErrorType(), 
				mzDeltaAnalysisDialog.getAnchorRTError(), 
				mzDeltaAnalysisDialog.getRTSeriesMassSet(),
				mzDeltaAnalysisDialog.getRTSeriesMassError(), 
				mzDeltaAnalysisDialog.getRTSeriesMassErrorType(),
				mzDeltaAnalysisDialog.getRTSeriesMinStep());
		mzDeltaAnalysisDialog.dispose();
		MZDeltaAnalysisTask task = new MZDeltaAnalysisTask(analysisParameters);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
	}

	private void setUpMSMSParentIonFrequencyAnalysis() {
		
		if(activeMsFeatureSet == null || activeMsFeatureSet.getFeatures().isEmpty() 
				|| activeDataPipeline == null)
			return;

		mzFrequencyAnalysisSetupDialog = 
				new MzFrequencyAnalysisSetupDialog(this, "Analyze MSMS parent ion frequency");
		mzFrequencyAnalysisSetupDialog.setLocationRelativeTo(this.getContentPane());
		mzFrequencyAnalysisSetupDialog.setVisible(true);
	}

	private void runMSMSParentIonFrequencyAnalysis() {

		double massWindowSize = 
				mzFrequencyAnalysisSetupDialog.getMZWindow();
		MassErrorType massWindowType = 
				mzFrequencyAnalysisSetupDialog.getMassErrorType();
		if(massWindowType == null || massWindowSize == 0) {
			MessageDialog.showErrorMsg("Invalid parameters!", mzFrequencyAnalysisSetupDialog);
			return;
		}
		mzFrequencyAnalysisSetupDialog.dispose();
		MzFrequencyAnalysisTask task = new MzFrequencyAnalysisTask(
				activeMsFeatureSet.getFeatures(), 
				MzFrequencyType.MS1_BASEPEAK_FREQUENCY,
				massWindowSize, 
				massWindowType);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
	}
	
	private void showFeatureSetCleanupDialog() {

		if(activeMsFeatureSet == null || activeMsFeatureSet.getFeatures().isEmpty() 
				|| activeDataPipeline == null)
			return;
		
		featureDataCleanupDialog = new FeatureDataCleanupDialog(this, activeMsFeatureSet);
		featureDataCleanupDialog.setLocationRelativeTo(this.getContentPane());
		featureDataCleanupDialog.setVisible(true);
	}
	
	private void cleanUpFeatureSet() {

		Collection<String> errors = featureDataCleanupDialog.validateParameters();
		if(!errors.isEmpty()) {
			
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), featureDataCleanupDialog);
			return;
		}			
		CleanUpFeatureSetTask task = new CleanUpFeatureSetTask(
				featureDataCleanupDialog.getFeatureCleanupParameters(),
				featureDataCleanupDialog.getActiveMsFeatureSet(),
				featureDataCleanupDialog.getFilteredFeatureSetName());
		
		featureDataCleanupDialog.dispose();
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Cleaning up active feature set set ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class CleanUpFeatureSetTask extends LongUpdateTask {
		
		private FeatureCleanupParameters fcp;
		private MsFeatureSet inputMsFeatureSet;
		private MsFeatureSet cleanMsFeatureSet;
		private String newFeatureSetName;
				
		public CleanUpFeatureSetTask(
				FeatureCleanupParameters fcp, 
				MsFeatureSet inputMsFeatureSet,
				String newFeatureSetName) {
			super();
			this.fcp = fcp;
			this.inputMsFeatureSet = inputMsFeatureSet;
			this.newFeatureSetName = newFeatureSetName;
		}
		
		@Override
		public Void doInBackground() {

			Collection<MsFeature> cleaned = new HashSet<>();	
			cleaned.addAll(inputMsFeatureSet.getFeatures());
			// Filter features
			try {
				//	By pooled frequency
				if(fcp.isFilterByPooledFrequency())					
					cleaned = filterByPooledRepresentation(cleaned);
													
				//	By mass defect
				if(fcp.isFilterByMassDefect()) {
					
					final double mdRtCutof = fcp.getMassDefectFilterRTCutoff();
					final double mdMassCutof = fcp.getMdFilterMassDefectValue();
					Collection<MsFeature>hm = 
							cleaned.stream().
							filter(f -> f.getRetentionTime() < mdRtCutof).
							filter(f -> f.getFractionalMassDefect() > mdMassCutof).
							collect(Collectors.toSet());					
					if(!hm.isEmpty())
						cleaned.removeAll(hm);
				}				
				//	By high mass
				if(fcp.isFilterHighMassBelowRT()) {
					
					final double hmRtCutof = fcp.getHighMassFilterRTCutoff();
					final double hmMassCutof = fcp.getHighMassFilterMassValue();
					Collection<MsFeature>hm = 
							cleaned.stream().
							filter(f -> f.getRetentionTime() < hmRtCutof).
							filter(f -> f.getSpectrum().getPrimaryAdductBasePeakMz() > hmMassCutof).
							collect(Collectors.toSet());					
					if(!hm.isEmpty())
						cleaned.removeAll(hm);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			cleanMsFeatureSet = new MsFeatureSet(newFeatureSetName, cleaned);
			return null;
		}
		
	    @Override
	    public void done() {
	    	
	    	super.done();
	    	if(cleanMsFeatureSet.getFeatures().isEmpty()) {
	    		MessageDialog.showWarningMsg("All features were removd by filtering!");
	    	}
	    	else {
	    		cleanMsFeatureSet.setProperty(
	    				FeatureSetProperties.FILTERING_PARAMETERS, fcp);
	    		currentExperiment.addFeatureSetForDataPipeline(
	    				cleanMsFeatureSet, activeDataPipeline);
	    		
	    		MRC2ToolBoxCore.getMainWindow().getExperimentSetupDraw().
	    			getFeatureSubsetPanel().addSetListeners(cleanMsFeatureSet);
	    		
	    		MetabolomicsProjectUtils.switchActiveMsFeatureSet(cleanMsFeatureSet);
	    	}
	    }
	    
	    private Collection<MsFeature>filterByPooledRepresentation(Collection<MsFeature> input){
	    	
	    	Collection<MsFeature>filtered = new HashSet<>();
			HashMap<DataFile,Long>fileCoordinates = new HashMap<>();
			Matrix assayData = currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline);
			final DataAcquisitionMethod method = activeDataPipeline.getAcquisitionMethod();
			Set<DataFile> pooledDataFiles = fcp.getSelectedPooledSamples().stream().
					flatMap(s -> s.getDataFilesForMethod(method).stream()).
					collect(Collectors.toSet());
			
			int totalPoolCount = pooledDataFiles.size();
			double repCutoff = fcp.getPooledFrequencyCutoff() / 100.0d;
			long[] dataCoordinates = new long[2];
			for (DataFile df : pooledDataFiles) 
				fileCoordinates.put(df, assayData.getRowForLabel(df));
						
			for (MsFeature cf : input) {

				int hitCount = 0;
				dataCoordinates[1] = assayData.getColumnForLabel(cf);
				for (DataFile df : pooledDataFiles) {

					dataCoordinates[0] = fileCoordinates.get(df);
					if(assayData.getAsDouble(dataCoordinates) > 0)
						hitCount++;					
				}
				if((double)hitCount / (double)totalPoolCount > repCutoff)
					filtered.add(cf);
			}			
			return filtered;			
	    }
	}

	private void showIntegratedReportDialog() {

		if (currentExperiment == null)
			return;

		integratedReportDialog = new IntegratedReportDialog(currentExperiment);
		integratedReportDialog.setLocationRelativeTo(this.getContentPane());
		integratedReportDialog.setVisible(true);
	}
	
	private void showMwTabReportDialog() {

		if(currentExperiment == null)
			return;

		mwTabExportDialog = new MWTabExportDialog();
		mwTabExportDialog.setLocationRelativeTo(this.getContentPane());
		mwTabExportDialog.setVisible(true);
	}
	
	private void exportAnalysisResults(String command) {
		
		if(command.equals(MainActionCommands.EXPORT_RESULTS_COMMAND.getName())) {
			exportDialog = new DataExportDialog();
			exportDialog.setLocationRelativeTo(this.getContentPane());
			exportDialog.setVisible(true);
			return;
		}			
		MainActionCommands exportType = 
				DataExportDialog.getExportTypeByName(command);
		if(exportType == null)
			return;
		
		if(exportType.equals(MainActionCommands.EXPORT_MZRT_STATISTICS_COMMAND)
				|| exportType.equals(MainActionCommands.EXPORT_PEAK_WIDTH_STATISTICS_COMMAND)
				|| exportType.equals(MainActionCommands.EXPORT_ALL_FEATURE_STATISTICS_COMMAND)) {
			
			File featureMatrixFile = 
					ProjectUtils.getFeatureMatrixFilePath(
							currentExperiment, activeDataPipeline, false).toFile();
			
			if (!featureMatrixFile.exists()) {
				MessageDialog.showWarningMsg(
						"M/Z and RT data for features from individual samples not available", 
						this.getContentPane());
				return;
			}
		}		
		exportDialog = new DataExportDialog(exportType);
		exportDialog.setLocationRelativeTo(this.getContentPane());
		exportDialog.setVisible(true);		
	}

	private void checkForDuplicateNames() {
		
		if(currentExperiment == null || activeDataPipeline == null)
			return;
			
		FindDuplicateNamesTask task = 
			new FindDuplicateNamesTask(currentExperiment, activeDataPipeline);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);;
	}

	private void showFeatureFilter() {
		
		filterFeaturesDialog = new FilterTreeDialog(this);
		filterFeaturesDialog.setLocationRelativeTo(this.getContentPane());
		filterFeaturesDialog.setVisible(true);
	}

	private void clearSelectedFeatureIdentifications() {

		selectedFeaturesMap = featureDataTable.getSelectedFeaturesMap();		
		if(selectedFeaturesMap.isEmpty())
			return;

		int confirm = MessageDialog.showChoiceWithWarningMsg(
			"Do you want to remove compound identification data from selected features?",
			this.getContentPane());
		if (confirm != JOptionPane.YES_OPTION)
			return;
			
		List<MsFeature>selectedFeatures = selectedFeaturesMap.values().
				stream().flatMap(c -> c.stream()).collect(Collectors.toList());
		selectedFeatures.stream().forEach(f -> f.clearIdentification());


		//	If knowns only shown remove row from view and scroll to nearest selected
		if(activeFeatureFilter.equals(FeatureFilter.IDENTIFIED_ONLY)) {

			List<MsFeature> sorted =
					currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline).getFeatures().stream().
					filter(f -> f.isIdentified()).sorted(new MsFeatureComparator(SortProperty.Name)).
					collect(Collectors.toList());

			setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, sorted));
		}
		else {			
			featureDataTable.updateMultipleFeatureData(selectedFeatures);
		}
	}

	private void showExcelDataLoader() {

		if(currentExperiment == null)
			return;

		excelImportWizard = new ExcelImportWizard();
		excelImportWizard.setLocationRelativeTo(this.getContentPane());
		excelImportWizard.setVisible(true);
	}

	public void showDataExplorerFrame() {

		if (currentExperiment == null || activeDataPipeline == null)
			return;
		
		DataExplorerPlotFrame dataExplorerPlotDialog = MainWindow.getDataExplorerPlotDialog();
		dataExplorerPlotDialog.setParentPanel(this);
		
		if(dataExplorerPlotDialog.isVisible())			
			dataExplorerPlotDialog.toFront();
		else {
			dataExplorerPlotDialog.setVisible(true);
			dataExplorerPlotDialog.loadMzRtFromFeatureCollection(
					currentExperiment, activeMsFeatureSet);
		}
	}
	
	public void showMsMultiplotFrame() {
		
		if (currentExperiment == null || activeDataPipeline == null)
			return;
		
		if(multiSpectraPlotFrame != null 
				&& multiSpectraPlotFrame.isVisible()) {
			multiSpectraPlotFrame.toFront();
			return;
		}
		
		multiSpectraPlotFrame = new MultiMSFeatureQCPlotFrame(
				currentExperiment,activeDataPipeline);
		multiSpectraPlotFrame.setLocationRelativeTo(this.getContentPane());
		multiSpectraPlotFrame.setVisible(true);
		
		MsFeature firstSelected = featureDataTable.getSelectedFeature();
		if(firstSelected != null)			
			multiSpectraPlotFrame.loadFeatureData(firstSelected);
	}

	private void showMissingIdentifications() {

		if (currentExperiment == null || activeDataPipeline == null)
			return;

		// Check if any IDs present
		identifiedFeatures = 
				currentExperiment.getMsFeaturesForDataPipeline(activeDataPipeline).
				stream().filter(f -> f.isIdentified()).
				collect(Collectors.toSet());

		if (identifiedFeatures.isEmpty()) {

			MessageDialog.showErrorMsg(
					"No identified features found, please run library search first!",
					this.getContentPane());
		} else {
			Set<CompoundIdSource> idSources = 
					identifiedFeatures.stream().
					map(f -> f.getPrimaryIdentity().getIdSource()).
					distinct().collect(Collectors.toSet());

			if (!idSources.contains(CompoundIdSource.LIBRARY)) {

				MessageDialog.showErrorMsg(
						"No library-identified features found, please run library search first!",
						this.getContentPane());
				return;
			}
			// Find and load libraries used to run identifications
			Collection<String> targetIds = identifiedFeatures.stream().
					filter(f -> Objects.nonNull(f.getPrimaryIdentity().getMsRtLibraryMatch())).
					map(f -> f.getPrimaryIdentity().getMsRtLibraryMatch().getLibraryTargetId()).
					distinct().collect(Collectors.toSet());

			Collection<CompoundLibrary> libraries = new HashSet<>();
			try {
				libraries = MSRTLibraryUtils.getLibrariesForTargets(targetIds);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (libraries.isEmpty()) {
				MessageDialog.showErrorMsg(
						"Libraries used for identification are not available!",
						this.getContentPane());
				return;
			}
			libsToLoad = new HashSet<>();
			for (CompoundLibrary l : libraries) {

				boolean loaded = false;
				for (CompoundLibrary loadedLib : MRC2ToolBoxCore.getActiveMsLibraries()) {

					if (l.getLibraryId().equals(loadedLib.getLibraryId())) {
						loaded = true;
						break;
					}
				}
				if (!loaded)
					libsToLoad.add(l);
			}
			if (libsToLoad.isEmpty()) {
				populateMissingIdDialog(identifiedFeatures, libraries);
			} else {
				missingLookupLibs = libraries;
				loadedLibsCount = 0;
				for (CompoundLibrary dbLibrary : libsToLoad) {
					LoadDatabaseLibraryTask ldbltask = new LoadDatabaseLibraryTask(dbLibrary.getLibraryId());
					ldbltask.addTaskListener(this);
					MRC2ToolBoxCore.getTaskController().addTask(ldbltask);
				}
			}
		}
	}

	private void populateMissingIdDialog(Set<MsFeature> identified, 
			Collection<CompoundLibrary> libraries) {

		if (missingIdentificationsDialog == null)
			missingIdentificationsDialog = new MissingIdentificationsDialog();

		missingIdentificationsDialog.populateMissingIdsTable(identified, libraries);

		missingIdentificationsDialog.setLocationRelativeTo(this.getContentPane());
		missingIdentificationsDialog.setVisible(true);
	}

	private void mergeSelectedFeatures() {

		MergeDuplicateFeaturesTask ddt = 
				new MergeDuplicateFeaturesTask(featureDataTable.getSelectedFeatures(),
				currentExperiment, 
				activeDataPipeline, 
				duplicateMergeDialog.getMergeOption());
		ddt.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(ddt);
		duplicateMergeDialog.setVisible(false);
	}

	private void showMergeSelectedFeaturesDialog() {

		Collection<MsFeature> selected = featureDataTable.getSelectedFeatures();
		if (selected.size() < 2)
			return;

		List<String> featureNames = selected.stream().			
				map(f -> f.getName()).collect(Collectors.toList());
		String yesNoQuestion = "Do you want yo merge:\n " + 
				StringUtils.join(featureNames, "\n")
				+ "\ninto a single feature?";
		if (MessageDialog.showChoiceMsg(yesNoQuestion, 
				this.getContentPane()) == JOptionPane.YES_OPTION) {

			duplicateMergeDialog.setLocationRelativeTo(this.getContentPane());
			duplicateMergeDialog.setVisible(true);
		}
	}

	private void clearAllFeatureIdentifications() {

		if (currentExperiment == null || activeDataPipeline == null)
			return;

		int confirm = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to remove compound identification data from all features?", 
				this.getContentPane());

		if (confirm == JOptionPane.YES_OPTION) {
			ClearIdentificationsTask ciTask = new ClearIdentificationsTask();
			ciTask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(ciTask);
		}
	}
	
	private Collection<MsFeature> getFeaturesToSearch(TableRowSubset rowSubset){
		
		if(rowSubset.equals(TableRowSubset.SELECTED)){
			
			if (featureDataTable.getSelectedFeatures().isEmpty())
				return null;
			else
				return featureDataTable.getSelectedFeatures();
		}
		if(rowSubset.equals(TableRowSubset.ALL)){
			
			if (featureDataTable.hasHiddenRows()) {
			
				int confirm = MessageDialog.showChooseOrCancelMsg(
						"Some features in the table are hidden by filtering.\n"
						+ "Click \"Yes\" to submit only visible features to the search.\n"
						+ "Click \"No\" to submit all features to the search.\n",
						this.getContentPane());
	
				if (confirm == JOptionPane.YES_OPTION)
					return featureDataTable.getVisibleFeatures();
				else if(confirm == JOptionPane.NO_OPTION)
					return featureDataTable.getAllFeatures();
				else
					return null;			
			} 
			else {
				return featureDataTable.getAllFeatures();	
			}		
		}
		return null;
	}
	
	public Collection<MsFeature>getFeatures(TableRowSubset subset){
			return featureDataTable.getFeatures(subset);
	}

	private void showLibrarySearchSetup(TableRowSubset rowSubset) {
				
		Collection<MsFeature> featuresToSearch = 
				getFeaturesToSearch(rowSubset);
		if(featuresToSearch == null 
				|| featuresToSearch.isEmpty())
			return;

		librarySearchSetupDialog = 
				new LibrarySearchSetupDialog(this, featuresToSearch);
		librarySearchSetupDialog.setLocationRelativeTo(this.getContentPane());
		librarySearchSetupDialog.setVisible(true);
	}

	private void showDatabaseSearchSetup(TableRowSubset rowSubset) {
		
		Collection<MsFeature> featuresToSearch = 
				getFeaturesToSearch(rowSubset);
		if(featuresToSearch == null 
				|| featuresToSearch.isEmpty())
			return;

		//	TODO
		//		if (databaseSearchSetupDialog == null)
		//			databaseSearchSetupDialog = new DatabaseSearchSetupDialog(this);
		//
		//		databaseSearchSetupDialog.updateData();
		//		databaseSearchSetupDialog.setLocationRelativeTo(this.getContentPane());
		//		databaseSearchSetupDialog.setVisible(true);
	}

	private void runLibrarySearch() {
		
		Collection<String> errors = 
				librarySearchSetupDialog.verifySearchParameters();
		if (!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this.getContentPane());
			return;
		} 
		if(librarySearchSetupDialog.clearPreviousResults()) {
			
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to clear previous library search results?", 
					librarySearchSetupDialog);
			if(res != JOptionPane.YES_OPTION)
				return;
		}
		MSRTSearchParametersObject spo = 
				librarySearchSetupDialog.getMSRTSearchParameters();
		Collection<MsFeature> featuresToSearch = 
				librarySearchSetupDialog.getFeaturesToSearch();
		LibrarySearchTask lst = new LibrarySearchTask(featuresToSearch, spo);
		lst.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(lst);
		librarySearchSetupDialog.dispose();
	}

	private void runDatabaseSearch() {
		// TODO Auto-generated method stub

	}

	private void showDataImputationDialog() {

		dataImputationSetupDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		dataImputationSetupDialog.setVisible(true);
	}
	
	private void setupCompoundNameVerification() {
		
		TargetedDataFileSelectionDialog ntdDialog = 
				new TargetedDataFileSelectionDialog(
						this, currentExperiment.getLibraryDirectory(), true);
		ntdDialog.setLocationRelativeTo(this.getContentPane());
		ntdDialog.setVisible(true);
	}

	private void showMultifileDataLoader(DataPipeline pipeline, DataTypeForImport importType) {

		multiFileDataImportDialog = new MultiFileDataImportDialog(this);
		multiFileDataImportDialog.setDataTypeForImport(importType);
		if(pipeline != null)
			multiFileDataImportDialog.setExistingDataPipeline(pipeline);
			
		multiFileDataImportDialog.setLocationRelativeTo(this.getContentPane());
		multiFileDataImportDialog.setVisible(true);
	}
	
	private void setUpPeakQualityDataImport() {
		
		// If no data imported yet
		if(currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline) == null)
			return;
		
		peakQualityImportDialog = 
				new PeakQualityImportDialog(currentExperiment,activeDataPipeline,this);
		peakQualityImportDialog.setLocationRelativeTo(this.getContentPane());
		peakQualityImportDialog.setVisible(true);	
	}
	
	private void addPeakQualityData() {

		Collection<File> cefFiles = peakQualityImportDialog.getCefFiles();
		if(cefFiles.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No input CEF files selected", dataImputationSetupDialog);
			return;
		}		
		MiltiCefPeakQualityImportTask task = new MiltiCefPeakQualityImportTask(
				currentExperiment, activeDataPipeline, cefFiles);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
		peakQualityImportDialog.dispose();
	}

	private void addSelectedFeaturesToActiveSubset() {
		// TODO Auto-generated method stub

	}

	private void setupDataStatsCalculation() {

		if(currentExperiment == null || activeDataPipeline == null)
			return;

		experimentPooledSampleManagerDialog = 
				new ExperimentPooledSampleManagerDialog(this);
		experimentPooledSampleManagerDialog.setLocationRelativeTo(this.getContentPane());
		experimentPooledSampleManagerDialog.setVisible(true);
	}
	
	public void recalculateDataStatsWithSelectedPools() {
		
		if(currentExperiment == null || activeDataPipeline == null)
			return;

		currentExperiment.setPooledSamples(
				experimentPooledSampleManagerDialog.getSelectedSamples());
		experimentPooledSampleManagerDialog.dispose();
		
		// Check if design assigned to data files and pooled/sample are specified
		// TODO If no pooled present and required, calculate for the whole set as samples
		if (DataSetUtils.designValidForStats(currentExperiment, activeDataPipeline, false)) {

			CalculateStatisticsTask cst = 
					new CalculateStatisticsTask(currentExperiment, activeDataPipeline, false);
			cst.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(cst);
		} else {
			MessageDialog.showWarningMsg(
					"Experiment design not valid for calculating the statistics (no samples / pools defined	)", 
					this.getContentPane());
		}
	}
	
	private void cleanEmptyFeatures() {
		
		String message = "This operation may change the current feature set and can not be reversed, proceed?";
		int res = MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
		if(res == JOptionPane.YES_OPTION) {
			
			RemoveEmptyFeaturesTask task = 
					new RemoveEmptyFeaturesTask(currentExperiment, activeDataPipeline);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	private void setupAverageFeaturesLibraryGeneration(){
	
		CompoundLibrary existing = 
				currentExperiment.getAveragedFeatureLibraryForDataPipeline(activeDataPipeline);
		if(existing != null) {
			
			int res = MessageDialog.showChoiceWithWarningMsg(
					"The averaged feature library for the active data pipeline already exists.\n"
					+ "Do you want to recalculate it?", 
					this.getContentPane());
			if(res != JOptionPane.YES_OPTION)
				return;
		}
		featureAveragingSetupDialog = new FeatureAveragingSetupDialog(this);
		featureAveragingSetupDialog.loadPipelineData(currentExperiment, activeDataPipeline);
		featureAveragingSetupDialog.setLocationRelativeTo(this.getContentPane());
		featureAveragingSetupDialog.setVisible(true);
	}
	
	private void createAverageFeaturesLibrary(){
		
		Collection<DataFile> dataFiles = featureAveragingSetupDialog.getSelectedFiles();
		if(dataFiles.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No data files selected for feature averaging", 
					featureAveragingSetupDialog);
			return;
		}
		MsFeatureAveragingTask task = 
				new MsFeatureAveragingTask(activeDataPipeline, dataFiles);
		task.addTaskListener(this);		
		MRC2ToolBoxCore.getTaskController().addTask(task);
		featureAveragingSetupDialog.dispose();
	}
	
	private void loadAverageFeaturesLibrary(){
		
		CompoundLibrary averagedLibrary = 
				currentExperiment.getAveragedFeatureLibraryForDataPipeline(activeDataPipeline);
		if(averagedLibrary == null) {
			MessageDialog.showWarningMsg(
					"Averaged feature library not available for data pipeline \""
					+ activeDataPipeline.getName() + "\"\n"
					+ "Please run the feature averaging routine first.", this.getContentPane());
			return;
		}
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.MS_LIBRARY);
		MsLibraryPanel libPanel = 
				(MsLibraryPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.MS_LIBRARY);
		if(libPanel.getCurrentLibrary() != null 
				&& libPanel.getCurrentLibrary().equals(averagedLibrary))
			return;
		else {
			MRC2ToolBoxCore.getActiveMsLibraries().add(averagedLibrary);
			libPanel.reloadLibraryData(averagedLibrary);
		}
	}	
	
	private void deleteAverageFeaturesLibrary() {
		
		CompoundLibrary averagedLibrary = 
				currentExperiment.getAveragedFeatureLibraryForDataPipeline(activeDataPipeline);
		if(averagedLibrary == null)
			return;
		
		String message = "Do you want to delet averaged feature library "
				+ "for data pipeline \"" + activeDataPipeline.getName() +"\"?";
		int res = MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
		if(res == JOptionPane.YES_OPTION) {
			
			currentExperiment.getAveragedFeatureMap().remove(activeDataPipeline);
			MRC2ToolBoxCore.getActiveMsLibraries().remove(averagedLibrary);
			MsLibraryPanel libPanel = 
					(MsLibraryPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.MS_LIBRARY);
			if(libPanel.getCurrentLibrary() != null 
					&& libPanel.getCurrentLibrary().equals(averagedLibrary)) {
				libPanel.clearPanel();
			}
			FIOUtils.safeDeleteFile(
					ProjectUtils.getAveragedFeaturesFilePath(currentExperiment, activeDataPipeline));
		}		
	}
	
	private void importBinnerAnnotations() {
		
		if(hasBinnerAnnotations()) {
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to clear existing Binner annotations?", 
					this.getContentPane());
			if(res != JOptionPane.YES_OPTION) 
				return;
		}

		JnaFileChooser fc = new JnaFileChooser(currentExperiment.getDataDirectory());
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.setMultiSelectionEnabled(false);
		fc.addFilter("Excel files", "xlsx", "XLSX");
		fc.setTitle("Import binner annotations from file:");
		fc.setMultiSelectionEnabled(false);
		fc.setOpenButtonText("Select Binner output file");

		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {

			File binnerResultsFile = fc.getSelectedFile();
			if(binnerResultsFile!= null) {
				
				ImportBinnerAnnotationsForUntargetedDataTask task = 
						new ImportBinnerAnnotationsForUntargetedDataTask(
								binnerResultsFile, currentExperiment, activeDataPipeline);

				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}	
	}
	
	private void clearBinnerAnnotations() {
		
		if(hasBinnerAnnotations()) {
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to clear existing Binner annotations?", 
					this.getContentPane());
			if(res == JOptionPane.YES_OPTION) {
				
				currentExperiment.getMsFeaturesForDataPipeline(activeDataPipeline).
					stream().forEach(f -> f.setBinnerAnnotation(null));
				resetFeatureTable();
			}
		}
	}
	
	private boolean hasBinnerAnnotations() {
		
		if(currentExperiment == null || activeDataPipeline == null)
			return false;
		
		MsFeature annotatted = 
				currentExperiment.getMsFeaturesForDataPipeline(activeDataPipeline).
				stream().filter(f -> Objects.nonNull(f.getBinnerAnnotation())).
				findFirst().orElse(null);
		return (annotatted != null);
	}
	
	public synchronized void clearPanel() {

		clearFeatureData();
		clearFeatureTable();
	}

	public void clearFeatureData() {

		dataPlot.clearPlotPanel();
		correlationPanel.clearPanel();
		featureIntensitiesTable.clearTable();
		spectrumPlot.removeAllDataSets();
		spectrumTable.clearTable();
		clearIdentificationTable();
		molStructurePanel.clearPanel();
		featureAnnotationPanel.clearPanel();
		binnerAnnotationDetailsPanel.clearPanel();
	}
	
	private void clearIdentificationTable() {
		
		identificationsTable.getTable().getSelectionModel().removeListSelectionListener(this);		
		identificationsTable.clearTable();
		identificationsTable.getTable().getSelectionModel().addListSelectionListener(this);
	}

	private void editFeatureMetaData() {

		MsFeature feature = featureDataTable.getFeatureAtPopup();
		feature.addListener(this);

		//	TODO write new functionality
		//		MainWindow.getFeatureDataEditor().loadFeatureData(feature);
		//		MainWindow.getFeatureDataEditor().setVisible(true);
	}

	private void filterFeatureTable() {
		
		Collection<MsFeature> features = 
				currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline)
				.getFeatures();
		HashSet<MsFeature> filtered = new HashSet<>();
		boolean append;

		String featureName = filterFeaturesDialog.getFeatureNameSubstring();
		Pattern nameMatch = null;

		if (featureName != null)
			nameMatch = Pattern.compile(Pattern.quote(featureName), Pattern.CASE_INSENSITIVE);

		Range rtRange = filterFeaturesDialog.getRtRange();
		Range mzRange = filterFeaturesDialog.getMzRange();

		for (MsFeature feature : features) {

			append = true;
			if (nameMatch != null && !nameMatch.matcher(feature.getName()).find())
				append = false;

			if (rtRange != null && !rtRange.contains(feature.getRetentionTime()))
				append = false;

			if (mzRange != null && !mzRange.contains(feature.getMonoisotopicMz()))
				append = false;

			if (append)
				filtered.add(feature);
		}
		if (filtered.isEmpty()) {
			MessageDialog.showWarningMsg("No clusters found matching all the criteria.");
		} else {
			clearPanel();
			Collection<MsFeature> sorted = 
					filtered.stream().sorted(new MsFeatureComparator(SortProperty.Name)).
					collect(Collectors.toList());
			setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, sorted));
		}
		filterFeaturesDialog.dispose();
		activeFeatureFilter = FeatureFilter.CUSTOM_FILTER;
	}
	
	private void showFeatureMZRTListFilter() {
		
		if(activeMsFeatureSet == null 
				|| activeMsFeatureSet.getFeatures().isEmpty())
			return;
		
		filterFeaturesByMzRtListDialog = new FilterFeaturesByMzRtListDialog(this);
		filterFeaturesByMzRtListDialog.setLocationRelativeTo(this.getContentPane());
		filterFeaturesByMzRtListDialog.setVisible(true);
	}
	
	private void filterFeatureTableByMZRTList() {

		Collection<String>errors = filterFeaturesByMzRtListDialog.validateFormData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(
		            StringUtils.join(errors, "\n"), filterFeaturesByMzRtListDialog);
		    return;
		}
		String[][]dataArray = filterFeaturesByMzRtListDialog.getInputDataArray();
		int mzColumnIndex = filterFeaturesByMzRtListDialog.getMzColumnIndex();
		int rtColumnIndex = filterFeaturesByMzRtListDialog.getRtColumnIndex();
		double massWindow = filterFeaturesByMzRtListDialog.getMassWindow();
		MassErrorType massErrorType = filterFeaturesByMzRtListDialog.getMassErrorType();
		double rtWindow = filterFeaturesByMzRtListDialog.getRTWindow();
		String dataSetName = filterFeaturesByMzRtListDialog.getFilteredFeatureSetName();
		
		filterFeaturesByMzRtListDialog.savePreferences();
		filterFeaturesByMzRtListDialog.dispose();
		
		double[][]mzRtArray = new double[dataArray.length-1][2];
		for(int i=1; i<dataArray.length; i++) {
			mzRtArray[i-1][0] = NumberUtils.createDouble(dataArray[i][mzColumnIndex]);
			mzRtArray[i-1][1] = NumberUtils.createDouble(dataArray[i][rtColumnIndex]);
		}
		Set<MsFeature> filtered = new HashSet<>();
		for(int i=0; i<mzRtArray.length; i++) {
			
			MzRtFilter mzRtFilter = new MzRtFilter(
					mzRtArray[i][0], massWindow, massErrorType, mzRtArray[i][1], rtWindow);			
			Set<MsFeature> matches = activeMsFeatureSet.getFeatures().stream().
				filter(f -> mzRtFilter.matches(f.getMonoisotopicMz(), f.getRetentionTime())).
				collect(Collectors.toSet());
			if(!matches.isEmpty())
				filtered.addAll(matches);
		}
		if(filtered.isEmpty()){
		    MessageDialog.showErrorMsg(
		    		"No features found using MZ/RT list", 
		    		this.getContentPane());
		}
		else {
			MsFeatureSet filteredDataSet = new MsFeatureSet(dataSetName);
			filteredDataSet.addFeatures(filtered);
			currentExperiment.addFeatureSetForDataPipeline(filteredDataSet, activeDataPipeline);			
    		MRC2ToolBoxCore.getMainWindow().getExperimentSetupDraw().
				getFeatureSubsetPanel().addSetListeners(filteredDataSet);		
    		MetabolomicsProjectUtils.switchActiveMsFeatureSet(filteredDataSet);
		}
	}

	public MsFeature[] getFilteredFeatures() {

		Set<MsFeature> filtered = featureDataTable.getVisibleFeatures();
		return filtered.toArray(new MsFeature[filtered.size()]);
	}

	public MsFeature[] getSelectedFeaturesArray() {

		Collection<MsFeature> selected = featureDataTable.getSelectedFeatures();
		return selected.toArray(new MsFeature[selected.size()]);
	}
	
	public MsFeature getSelectedFeature() {
		return featureDataTable.getSelectedFeature();
	}

	private void imputeMissingData() {

		dataImputationSetupDialog.setVisible(false);

		DataImputationType method = dataImputationSetupDialog.getSelectedImputationMethod();
		Object[] parameters = new Object[] { dataImputationSetupDialog.getKnnParameter() };
		ImputeMissingDataTask task = new ImputeMissingDataTask(method, parameters);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	//	TODO Force lib with text or Excel data?
	private void loadLibrary() {
		
//		if (currentProject.getCompoundLibraryForDataPipeline(activeDataPipeline) != null) {
//
//			String message = 
//					"Library for the active data pipeline is already loaded.\n" + 
//					"Do you want to replace it?";
//
//			int selectedValue = JOptionPane.showInternalConfirmDialog(
//					MRC2ToolBoxCore.getMainWindow().getContentPane(), message, "Replace library",
//					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//			
//			int selectedValue = 
//					MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
//			if (selectedValue == JOptionPane.YES_OPTION)
//				MRC2ToolBoxCore.getMainWindow().showDataLoader(
//						MainActionCommands.LOAD_LIBRARY_COMMAND.getName());
//		} else {
//			MRC2ToolBoxCore.getMainWindow().showDataLoader(
//					MainActionCommands.LOAD_LIBRARY_COMMAND.getName());
//		}
	}

	private void removeSelectedFeaturesFromActiveSubset() {

		if (activeMsFeatureSet.isLocked()) {

			MessageDialog.showWarningMsg("Data set \"" + activeMsFeatureSet.getName() + 
							"\" is locked and can not be modified", 
							this.getContentPane());
			return;
		}
		Collection<MsFeature> selected = featureDataTable.getSelectedFeatures();

		if (!selected.isEmpty()) {

			int approve = MessageDialog.showChoiceMsg(
					"Remove selected feature(s) from subset?\n" + "(NO UNDO!)", 
					this.getContentPane());

			if (approve == JOptionPane.YES_OPTION)
				activeMsFeatureSet.removeFeatures(selected);
		}
	}

	private synchronized void resetFeatureTable() {
		
		if(currentExperiment == null || activeDataPipeline == null) {
			clearPanel();
			return;
		}
		activeFeatureFilter = FeatureFilter.ALL_FEATURES;
		setTableModelFromFeatureMap(Collections.singletonMap(
				activeDataPipeline, activeMsFeatureSet.getFeatures()));
	}
	
	public void setTableModelFromFeatureMap(
			Map<DataPipeline, Collection<MsFeature>> featureMap) {

		featureDataTable.getTable().getSelectionModel().removeListSelectionListener(this);
		featureDataTable.setTableModelFromFeatureMap(featureMap);
		featureDataTable.getTable().getSelectionModel().addListSelectionListener(this);
	}

	public void setTableModelFromFeatureSet(
			MsFeatureSet activeFeatureSetForMethod) {
		
//		if(activeFeatureSetForMethod.equals(activeMsFeatureSet)
//				&& featureDataTable.getTable().getRowCount() == activeMsFeatureSet.getFeatures().size())
//			return;
		
		clearFeatureTable();
		if(activeMsFeatureSet != null)
			activeMsFeatureSet.removeListener(this);

		activeMsFeatureSet = activeFeatureSetForMethod;
		activeMsFeatureSet.addListener(this);
		setTableModelFromFeatureMap(Collections.singletonMap(
				activeDataPipeline, activeMsFeatureSet.getFeatures()));
	}
	
	private void clearFeatureTable() {
		featureDataTable.getTable().getSelectionModel().removeListSelectionListener(this);
		featureDataTable.clearTable();
		featureDataTable.getTable().getSelectionModel().addListSelectionListener(this);
	}

	public void showFeatureData(MsFeature selectedFeature) {

		int featureRow = featureDataTable.getFeatureRow(selectedFeature);
		if (featureRow == -1)
			MessageDialog.showWarningMsg("Feature " + selectedFeature.getName() + " not found");
		else {
			featureDataTable.selectFeatureRow(featureRow);
			MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.FEATURE_DATA);
		}
	}

	private void showKnownsOnly() {
		clearPanel();
		List<MsFeature> sorted = 
				currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline).
				getFeatures().stream().filter(f -> f.isIdentified()).
				sorted(new MsFeatureComparator(SortProperty.Name)).collect(Collectors.toList());

		setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, sorted));
		activeFeatureFilter = FeatureFilter.IDENTIFIED_ONLY;
	}

	private void showUnknownsOnly() {
		clearPanel();
		List<MsFeature> sorted = 
				currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline).
				getFeatures().stream().filter(f -> !f.isIdentified()).
				sorted(new MsFeatureComparator(SortProperty.Name)).collect(Collectors.toList());

		setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, sorted));
		activeFeatureFilter = FeatureFilter.UNKNOWN_ONLY;
	}

	private void showQcOnly() {
		clearPanel();
		List<MsFeature> sorted = 
				currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline).
				getFeatures().stream().filter(f -> f.isQcStandard()).
				sorted(new MsFeatureComparator(SortProperty.Name)).collect(Collectors.toList());

		setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, sorted));
		activeFeatureFilter = FeatureFilter.QC_ONLY;
	}
	
	private void showBinnerAnnotated(boolean primaryOnly) {
		
		clearPanel();
		List<MsFeature> binnerAnnotated = 
				currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline).
				getFeatures().stream().filter(f -> Objects.nonNull(f.getBinnerAnnotation())).
				collect(Collectors.toList());
		if(primaryOnly)
			binnerAnnotated = binnerAnnotated.stream().
				filter(f -> f.getBinnerAnnotation().isPrimary()).
				collect(Collectors.toList());
			
		setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, binnerAnnotated));
		activeFeatureFilter = FeatureFilter.UNKNOWN_ONLY;		
	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {

		// TODO implement the functions
		ExperimentDesignSubset edSubset = e.getSource();

		ParameterSetStatus status = e.getStatus();

		if (status.equals(ParameterSetStatus.CHANGED)) {

			// clearPanel();

		}
		if (status.equals(ParameterSetStatus.CREATED)) {

			// clearPanel();

		}
		if (status.equals(ParameterSetStatus.DELETED)) {

			// clearPanel();
		}
		if (status.equals(ParameterSetStatus.ENABLED)) {

			// clearPanel();

		}
		if (status.equals(ParameterSetStatus.DISABLED)) {

			// clearPanel();
		}
	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {

		MsFeature source = e.getSource();
		ParameterSetStatus status = e.getStatus();

		if (status.equals(ParameterSetStatus.CHANGED)) {

			featureDataTable.updateFeatureData(source);
		}
		if (status.equals(ParameterSetStatus.CREATED)) {

			// clearPanel();

		}
		if (status.equals(ParameterSetStatus.DELETED)) {

			// clearPanel();
		}
		if (status.equals(ParameterSetStatus.ENABLED)) {

			// clearPanel();

		}
		if (status.equals(ParameterSetStatus.DISABLED)) {

			// clearPanel();
		}
	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {

		MsFeatureSet source = (MsFeatureSet) e.getSource();
		ParameterSetStatus status = e.getStatus();
		if (status.equals(ParameterSetStatus.CHANGED)) {
		
			if(activeMsFeatureSet != null && activeMsFeatureSet.equals(source)) {
					clearPanel();
					setTableModelFromFeatureMap(
							Collections.singletonMap(activeDataPipeline, source.getFeatures()));
			}			
		}
		if (status.equals(ParameterSetStatus.CREATED)) {

			clearPanel();
			activeMsFeatureSet = source;
			setTableModelFromFeatureMap(
					Collections.singletonMap(activeDataPipeline, source.getFeatures()));
		}
		if (status.equals(ParameterSetStatus.DELETED)) {

			if(activeMsFeatureSet != null && activeMsFeatureSet.equals(source))
				clearPanel();
		}			
		
		if (status.equals(ParameterSetStatus.ENABLED) && !activeMsFeatureSet.equals(source)) {
				
			clearPanel();
			activeMsFeatureSet = source;
			setTableModelFromFeatureMap(
					Collections.singletonMap(activeDataPipeline, source.getFeatures()));
		}
		if (status.equals(ParameterSetStatus.DISABLED)) {
			
			if(activeMsFeatureSet != null && activeMsFeatureSet.equals(source))
				clearPanel();
			
		}
		if(MainWindow.getDataExplorerPlotDialog().isVisible()) {
			
			if(activeMsFeatureSet == null || status.equals(ParameterSetStatus.DISABLED)) 
				MainWindow.getDataExplorerPlotDialog().clearPanels();			
			else 
				MainWindow.getDataExplorerPlotDialog().
					loadMzRtFromFeatureCollection(currentExperiment,activeMsFeatureSet);		
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(CalculateStatisticsTask.class)) {
				//	updateStatisticsResults((CalculateStatisticsTask) e.getSource());
//				MRC2ToolBoxCore.getMainWindow().switchPanelForDataPipeline(
//						activeDataPipeline, PanelList.FEATURE_DATA);
				setTableModelFromFeatureMap(Collections.singletonMap(
						activeDataPipeline, activeMsFeatureSet.getFeatures()));
			}
			if (e.getSource().getClass().equals(ImputeMissingDataTask.class))
				finalizeImputeMissingDataTask((ImputeMissingDataTask) e.getSource());
			
			if (e.getSource().getClass().equals(LibrarySearchTask.class))
				reviewLibrarySearchResults((LibrarySearchTask) e.getSource());

			if (e.getSource().getClass().equals(ClearIdentificationsTask.class))
				resetFeatureTable();

			if (e.getSource().getClass().equals(MergeDuplicateFeaturesTask.class)) {
				//	finalizeMergeDuplicateFeaturesTask((MergeDuplicateFeaturesTask)e.getSource());
				MRC2ToolBoxCore.getMainWindow().switchPanelForDataPipeline(
						activeDataPipeline, PanelList.FEATURE_DATA);
			}
			
			if (e.getSource().getClass().equals(LoadDatabaseLibraryTask.class))
				finalizeDatabaseLibraryLoad((LoadDatabaseLibraryTask) e.getSource());
		
			//	Load data
			if (e.getSource().getClass().equals(QuantMatrixImportTask.class))
				finalizeQuantDataLoad((QuantMatrixImportTask) e.getSource());

			//	Load data from multiple CEF files
			if (e.getSource().getClass().equals(MultiCefImportTask.class))
				finalizeMultiCefDataLoad((MultiCefImportTask) e.getSource());
			
			//	Load targeted data from text file
			if (e.getSource().getClass().equals(TargetedDataMatrixImportTask.class))
				finalizeTargetedDataMatrixImportTask((TargetedDataMatrixImportTask) e.getSource());
			
			if (e.getSource().getClass().equals(MultiCefDataAddTask.class))
				finalizeMultiCefDataAddition((MultiCefDataAddTask) e.getSource());
			
			if (e.getSource().getClass().equals(RemoveEmptyFeaturesTask.class))
				finalizeEmptyFeatureCleanup((RemoveEmptyFeaturesTask) e.getSource());
			
			if (e.getSource().getClass().equals(FindDuplicateNamesTask.class))
				finalizeDuplicateNameSearch((FindDuplicateNamesTask) e.getSource());			
		
			if (e.getSource().getClass().equals(MzFrequencyAnalysisTask.class))
				finalizeMzFrequencyAnalysisTask((MzFrequencyAnalysisTask)e.getSource());
			
			if (e.getSource().getClass().equals(MiltiCefPeakQualityImportTask.class))
				finalizePeakQualityImportTask((MiltiCefPeakQualityImportTask)e.getSource());
			
			if (e.getSource().getClass().equals(ImportBinnerAnnotationsForUntargetedDataTask.class))
				finalizeBinnerAnnotationsImportTask((ImportBinnerAnnotationsForUntargetedDataTask)e.getSource());	
			
			if (e.getSource().getClass().equals(ProFinderResultsImportTaskTask.class))
				finalizeProFinderResultsImportTaskTask((ProFinderResultsImportTaskTask)e.getSource());
			
			if (e.getSource().getClass().equals(MsFeatureAveragingTask.class))
				finalizeMsFeatureAveragingTask((MsFeatureAveragingTask)e.getSource());		
			
			if (e.getSource().getClass().equals(MZDeltaAnalysisTask.class))
				finalizeMZDeltaAnalysisTask((MZDeltaAnalysisTask)e.getSource());	
		}
		if (e.getStatus() == TaskStatus.CANCELED || e.getStatus() == TaskStatus.ERROR) {
			MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
			MainWindow.hideProgressDialog();
		}
	}

	private synchronized void finalizeMZDeltaAnalysisTask(MZDeltaAnalysisTask task) {

		MsFeatureClusterSet mzDeltaAnalysisClusterDataSet = 
				new MsFeatureClusterSet("M/Z delta analysis results - " 
						+ MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(new Date()), 
				task.getFeatureClusters());
		mzDeltaAnalysisClusterDataSet.setActive(true);
		currentExperiment.addFeatureClusterSet(mzDeltaAnalysisClusterDataSet);
				
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.INTEGRATION);
		DataIntegratorPanel dip = (DataIntegratorPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.INTEGRATION);		
		dip.loadFeatureClusterSet(mzDeltaAnalysisClusterDataSet);
	}

	private synchronized void finalizeMsFeatureAveragingTask(MsFeatureAveragingTask task) {

		CompoundLibrary averagedLibrary = task.getAveragedFeaturesLibrary();
		if(averagedLibrary != null) {
			currentExperiment.setAveragedFeatureLibraryForDataPipeline(
					activeDataPipeline, averagedLibrary);
			
			MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.MS_LIBRARY);
			MsLibraryPanel libPanel = 
					(MsLibraryPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.MS_LIBRARY);
			MRC2ToolBoxCore.getActiveMsLibraries().add(averagedLibrary);
			libPanel.reloadLibraryData(averagedLibrary);
		}
	}

	//	TODO Calculate statistics and save the data
	private synchronized void finalizeProFinderResultsImportTaskTask(ProFinderResultsImportTaskTask task) {

		DataPipeline dataPipeline = task.getDataPipeline();
		List<DataFile> filesWithoutTimestamp = 
				currentExperiment.getDataFilesForPipeline(dataPipeline, false).
				stream().filter(f -> Objects.isNull(f.getInjectionTime())).
				collect(Collectors.toList());
		if(!filesWithoutTimestamp.isEmpty()) {
			
			String message = "The following data files have no injection time assigned:\n";
			for(DataFile df : filesWithoutTimestamp)
				message += df.getBaseName() + "\n";
			
			message += "\nMissing injection time will interfere with some types of data presentatioh,\n"
					+ "Please add the missing data";
			
			MessageDialog.showWarningMsg(message, this.getContentPane());
		}
		MRC2ToolBoxCore.getMainWindow().
			switchDataPipeline(currentExperiment, dataPipeline);
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();

		//	Rerun statistics
		CalculateStatisticsTask statsTask = 
				new CalculateStatisticsTask(currentExperiment, activeDataPipeline, true);
		statsTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(statsTask);
	}

	private synchronized void finalizeImputeMissingDataTask(ImputeMissingDataTask task) {

		String mName = task.getImputationMethod().getName();
		MessageDialog.showInfoMsg("Data were imputed using " + mName + " method");
	}

	private synchronized void finalizeBinnerAnnotationsImportTask(ImportBinnerAnnotationsForUntargetedDataTask task) {
		
		Collection<BinnerAnnotation>unassignedAnnotations = task.getUnassignedAnnotations();
		if(unassignedAnnotations.isEmpty()) {
			MessageDialog.showInfoMsg(
					"Binner Annotations Import finished.", 
					this.getContentPane());
		}
		else {
			List<String>parts = unassignedAnnotations.stream().
				map(a -> (a.getFeatureName() + "\t" + a.getAnnotation())).
				collect(Collectors.toList());
			String details = StringUtils.join(parts, "\n");
			InformationDialog id = new InformationDialog(
					"Unassigned Binner Annotations", 
					"The following Binner annotations could not be matched to any of the features:", 
					details);
			id.setLocationRelativeTo(this.getContentPane());
			id.setVisible(true);
		}
		resetFeatureTable();
	}

	private synchronized void finalizePeakQualityImportTask(MiltiCefPeakQualityImportTask source) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		MessageDialog.showInfoMsg("Peak quality data import completed", this.getContentPane());
	}

	private synchronized void finalizeMzFrequencyAnalysisTask(MzFrequencyAnalysisTask task) {
	
		Collection<MzFrequencyObject>mzFrequencyObjects = task.getMzFrequencyObjects();
		String binningParameter = 
				MRC2ToolBoxConfiguration.getPpmFormat().format(task.getMassWindowSize()) 
				+ " " + task.getMassWindowType().name();
		
		MzFrequencyAnalysisResultsDialog resultsDialog = 
				new MzFrequencyAnalysisResultsDialog(
						this, mzFrequencyObjects, binningParameter);
		resultsDialog.setLocationRelativeTo(this.getContentPane());
		resultsDialog.setVisible(true);
	}

	private synchronized void finalizeDuplicateNameSearch(FindDuplicateNamesTask task) {

		if(task.getDuplicateNameList().isEmpty()) {
			MessageDialog.showInfoMsg(
					"No duplicate feature names found.", 
					this.getContentPane());
			return;
		}
		Collection<String>dupNames = new TreeSet<>();
		for(MsFeatureCluster cluster : task.getDuplicateNameList())		
			dupNames.add(cluster.getPrimaryFeature().getName());

		InformationDialog id = new InformationDialog(
				"Duplicate feature names", 
				"Found the following duplicate feature names",
				StringUtils.join(dupNames, "\n"));
		id.setLocationRelativeTo(this.getContentPane());
		id.setVisible(true);
	}
	
	private synchronized void finalizeQuantDataLoad(QuantMatrixImportTask quantMatrixImportTask) {

		DataPipeline dataPipeline = quantMatrixImportTask.getDataPipeline();
		currentExperiment.addDataPipeline(dataPipeline);
		currentExperiment.setDataMatrixForDataPipeline(
				dataPipeline, quantMatrixImportTask.getDataMatrix());
		currentExperiment.setFeaturesForDataPipeline(dataPipeline, 
				new HashSet<>(quantMatrixImportTask.getFeatureList()));
		currentExperiment.addDataFilesForAcquisitionMethod(dataPipeline.getAcquisitionMethod(), 
				quantMatrixImportTask.getDataFiles());

		MsFeatureSet allFeatures = new MsFeatureSet(GlobalDefaults.ALL_FEATURES.getName(),
				quantMatrixImportTask.getFeatureList());
		allFeatures.setActive(true);
		allFeatures.setLocked(true);
		currentExperiment.addFeatureSetForDataPipeline(allFeatures, dataPipeline);

		if (quantMatrixImportTask.hasUnmatchedSamples()) {

			MRC2ToolBoxCore.getMainWindow().
				switchPanelForDataPipeline(dataPipeline, PanelList.DESIGN);

			//TODO switch to Assay design tab

			MessageDialog.showWarningMsg(
					"Some files were not properly matched to the samples.\n"
					+ "Please correct this manually!");
		} else {
			MRC2ToolBoxCore.getMainWindow().
				switchPanelForDataPipeline(dataPipeline, PanelList.FEATURE_DATA);
		}
	}

	private synchronized void finalizeMultiCefDataLoad(MultiCefImportTask multiCefImportTask) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		
		MRC2ToolBoxCore.getMainWindow().switchDataPipeline(
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment(), 
				multiCefImportTask.getDataPipeline());
		
		SaveMetabolomicsProjectTask task = 
				new SaveMetabolomicsProjectTask(currentExperiment, true);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
		
	private void finalizeTargetedDataMatrixImportTask(TargetedDataMatrixImportTask tdmTask) {

		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		
		MRC2ToolBoxCore.getMainWindow().switchDataPipeline(
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment(), 
				tdmTask.getDataPipeline());
		
		SaveMetabolomicsProjectTask task = 
				new SaveMetabolomicsProjectTask(currentExperiment, true);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
	
	private synchronized void finalizeMultiCefDataAddition(MultiCefDataAddTask task) {

//		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
//		MainWindow.hideProgressDialog();
//	
//		//	Attach data
//		currentExperiment.setDataMatrixForDataPipeline(activeDataPipeline, task.getDataMatrix());
//		currentExperiment.addDataFilesForAcquisitionMethod(
//				activeDataPipeline.getAcquisitionMethod(), task.getDataFiles());
//		
//		MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.DESIGN).reloadDesign();
//
//		//	Rerun statistics
//		cleanEmptyFeatures = true;
//		CalculateStatisticsTask statsTask = 
//				new CalculateStatisticsTask(currentExperiment, activeDataPipeline, true);
//		statsTask.addTaskListener(this);
//		MRC2ToolBoxCore.getTaskController().addTask(statsTask);
	}

	private synchronized void finalizeEmptyFeatureCleanup(RemoveEmptyFeaturesTask task) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		
		MRC2ToolBoxCore.getMainWindow().switchPanelForDataPipeline(
				task.getDataPipeline(), PanelList.FEATURE_DATA);
		resetFeatureTable();
		SavePipelineDataTask spTask = 
				new SavePipelineDataTask(currentExperiment, activeDataPipeline);
		MRC2ToolBoxCore.getTaskController().addTask(spTask);
	}
	
	private synchronized void finalizeDatabaseLibraryLoad(LoadDatabaseLibraryTask task) {

		MRC2ToolBoxCore.getActiveMsLibraries().add(task.getLibrary());
		loadedLibsCount = loadedLibsCount + 1;

		if (loadedLibsCount == libsToLoad.size())
			populateMissingIdDialog(identifiedFeatures, missingLookupLibs);		
	}

//	private void updateStatisticsResults(CalculateStatisticsTask csTask) {
//		
//		MRC2ToolBoxCore.getMainWindow().switchPanelForDataPipeline(
//				csTask.getDataPipeline(), PanelList.FEATURE_DATA);
//	}

	public void findFeaturesByAdductMasses(
			Collection<Double> monoisotopicAdductMasses, double massAccuracyPpm,
			Range rtRange) {

		if (currentExperiment == null || activeDataPipeline == null)
			return;
		
		Collection<MsFeature> filtered = new TreeSet<>(new MsFeatureComparator(SortProperty.RT));
		for (double mz : monoisotopicAdductMasses) {

			Range mzRange = MsUtils.createPpmMassRange(mz, massAccuracyPpm);
			Set<MsFeature> adductFiltered = 
					currentExperiment.getMsFeaturesForDataPipeline(activeDataPipeline).stream().
					filter(f -> rtRange.contains(f.getRetentionTime())).
					filter(f -> mzRange.contains(f.getBasePeakMz())).
					collect(Collectors.toSet());

			if (!adductFiltered.isEmpty())
				filtered.addAll(adductFiltered);
		}
		if (filtered.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No matching features found!", this.getContentPane());
		} else {
			clearFeatureData();
			setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, filtered));
		}
	}

	private synchronized void reviewLibrarySearchResults(LibrarySearchTask lst) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();

		if (lst.getIdentifiedFeatures().isEmpty()) {

			MessageDialog.showWarningMsg(
					"No identifications found in addition to existing ones", 
					this.getContentPane());
			featureDataTable.getTable().getSelectionModel().addListSelectionListener(this);
			return;
		}
		setTableModelFromFeatureMap(
				Collections.singletonMap(activeDataPipeline, lst.getIdentifiedFeatures()));
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(e.getValueIsAdjusting() || e.getSource() == null)
			return;

		for(ListSelectionListener listener : ((DefaultListSelectionModel)e.getSource()).getListSelectionListeners()) {

			if(listener.equals(featureDataTable.getTable())) {
				
				clearFeatureData();
				switchSelectedFeatures();
				return;
			}
			if(listener.equals(identificationsTable.getTable())){
				
				showCompoundStructure(identificationsTable.getSelectedIdentity());
				return;
			}
		}
	}
	
	private void showCompoundStructure(MsFeatureIdentity fid) {
		
		if(fid == null 
				|| fid.getCompoundIdentity() == null
				|| fid.getCompoundIdentity().getSmiles() == null)
			return;
		
		try {
			molStructurePanel.showStructure(fid.getCompoundIdentity().getSmiles());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private void switchSelectedFeatures() {

		selectedFeaturesMap.values().stream().
				flatMap(c -> c.stream()).forEach(f -> f.removeListener(this));
		selectedFeaturesMap = featureDataTable.getSelectedFeaturesMap();
		if(selectedFeaturesMap.isEmpty())
			return;
		
		List<MsFeature> allSelected = selectedFeaturesMap.values().stream().
			flatMap(c -> c.stream()).collect(Collectors.toList());

		allSelected.stream().forEach(f -> f.addListener(this));
		MsFeature firstSelected = allSelected.get(0);	
		dataPlot.loadMultipleFeatureData(selectedFeaturesMap);	
		featureIntensitiesTable.setTableModelFromFeatureMap(selectedFeaturesMap);
		if(firstSelected.getSpectrum() != null) {

			spectrumPlot.showMsForPointCollection(
					Arrays.asList(firstSelected.getSpectrum().getCompletePattern()), "Observed MS");
			spectrumTable.setTableModelFromMsFeature(firstSelected);
		}
		identificationsTable.setModelFromMsFeature(firstSelected);
		
		Collection<BinnerAnnotation> binnerAnnotations = allSelected.stream().
				filter(f -> Objects.nonNull(f.getBinnerAnnotation())).
				map(f -> f.getBinnerAnnotation()).
				collect(Collectors.toList());
		binnerAnnotationDetailsPanel.setTableModelFromBinnerAnnotations(binnerAnnotations);
				
		showCompoundStructure(firstSelected.getPrimaryIdentity());
		featureAnnotationPanel.loadFeatureData(firstSelected);
		if (allSelected.size() > 1) {

			MsFeature[] msf = allSelected.toArray(new MsFeature[allSelected.size()]);
			//	TODO maybe will need an update to handle multiple pipelines
			correlationPanel.createCorrelationPlot(
					msf[0], activeDataPipeline, 
					msf[1], activeDataPipeline);
		}
		if(multiSpectraPlotFrame != null && multiSpectraPlotFrame.isVisible())
			multiSpectraPlotFrame.loadFeatureData(firstSelected);	
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newDataPipeline);
		menuBar.updateMenuFromExperiment(currentExperiment, activeDataPipeline);
		if (currentExperiment != null) {

			dataPlot.setActiveDesign(currentExperiment.getExperimentDesign().getActiveDesignSubset());
			if(activeDataPipeline != null)
				setTableModelFromFeatureSet(currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline));
		}		
		if(multiSpectraPlotFrame != null) {
			
			multiSpectraPlotFrame.setCurrentExperiment(project);
			multiSpectraPlotFrame.setDataPipeline(newDataPipeline);		
		}		
	}

	@Override
	public void closeExperiment() {

		super.closeExperiment();
		clearPanel();
		menuBar.updateMenuFromExperiment(null, null);
	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {

		if (e.getStatus().equals(ParameterSetStatus.CHANGED) 
				&& currentExperiment != null)
			dataPlot.setActiveDesign(currentExperiment.getExperimentDesign().getActiveDesignSubset());		
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void populatePanelsMenu() {
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}

	public void refreshIdentificationsTable() {

		MsFeature selectedFeature = getSelectedFeature();
		if(selectedFeature == null)
			clearIdentificationTable();
		else
			identificationsTable.setModelFromMsFeature(selectedFeature);
	}
	
	public void updateFeatureData(MsFeature toUpdate) {
		
		FeatureUpdateTask task = new FeatureUpdateTask(toUpdate);
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Updating feature ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class FeatureUpdateTask extends LongUpdateTask {
		
		private MsFeature toUpdate;
			
		public FeatureUpdateTask(MsFeature toUpdate) {
			super();
			this.toUpdate = toUpdate;
		}

		@Override
		public Void doInBackground() {
			
			featureDataTable.updateFeatureData(toUpdate);
			return null;
		}
	}
	
	private void exportStatisticsForActiveFeatureSet() {
		
		if(activeMsFeatureSet == null || activeMsFeatureSet.getFeatures().isEmpty() 
				|| activeDataPipeline == null)
			return;
		
		currentExperiment.getExportsDirectory();
		
		File exportFile = null;
		JnaFileChooser fc = new JnaFileChooser(currentExperiment.getExportsDirectory());
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Export metabolomics experiment data to text file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Export data");
		String defaultFileName = FIOUtils.createFileNameForDataExportType(
				MainActionCommands.EXPORT_FEATURE_STATISTICS_COMMAND);
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
						
			exportFile = fc.getSelectedFile();
		}	
		if(exportFile == null)
			return;
			
		DataExportTask det = new DataExportTask(
				currentExperiment,
				activeDataPipeline,
				exportFile,
				MainActionCommands.EXPORT_FEATURE_STATISTICS_COMMAND,
				MissingExportType.AS_MISSING,
				false,
				1000.0d,
				0.0d,
				DataExportFields.DATA_FILE,
				false,
				false);
		det.setMsFeatureSet4export(activeMsFeatureSet.getFeatures());
		det.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(det);		
	}
	
	public void loadCompleteDataSetForActivedataPipeline() {
		
		if(currentExperiment == null || activeDataPipeline == null)
			return;
		
		MsFeatureSet completeSet = 
				currentExperiment.getAllFeaturesSetFordataPipeline(activeDataPipeline);
		if(!completeSet.equals(activeMsFeatureSet)) {
			activeMsFeatureSet = completeSet;
			currentExperiment.setActiveFeatureSetForDataPipeline(activeMsFeatureSet, activeDataPipeline);
		}
		else {
			setTableModelFromFeatureMap(Collections.singletonMap(
					activeDataPipeline, activeMsFeatureSet.getFeatures()));
		}
	}

	@Override
	public void updateGuiWithRecentData() {
		// TODO Auto-generated method stub
		
	}	
}
