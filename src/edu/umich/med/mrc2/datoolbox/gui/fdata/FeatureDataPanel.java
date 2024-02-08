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

package edu.umich.med.mrc2.datoolbox.gui.fdata;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import org.apache.commons.lang3.StringUtils;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSRTSearchParametersObject;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataImputationType;
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
import edu.umich.med.mrc2.datoolbox.gui.clustertree.FilterTreeDialog;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.DatabaseSearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.datexp.DataExplorerPlotFrame;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates.DuplicateMergeDialog;
import edu.umich.med.mrc2.datoolbox.gui.expdesign.pools.ExperimentPooledSampleManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.fdata.cleanup.FeatureCleanupParameters;
import edu.umich.med.mrc2.datoolbox.gui.fdata.cleanup.FeatureDataCleanupDialog;
import edu.umich.med.mrc2.datoolbox.gui.fdata.corr.DockableCorrelationDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.fdata.noid.MissingIdentificationsDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtable.DockableUniversalIdentificationResultsTable;
import edu.umich.med.mrc2.datoolbox.gui.idtable.MetabolomicsIdentificationTableModelListener;
import edu.umich.med.mrc2.datoolbox.gui.io.DataExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.IntegratedReportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.MultiFileDataImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.excel.ExcelImportWizard;
import edu.umich.med.mrc2.datoolbox.gui.io.mwtab.MWTabExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.txt.TextDataImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.library.search.LibrarySearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.mzfreq.MzFrequencyAnalysisResultsDialog;
import edu.umich.med.mrc2.datoolbox.gui.mzfreq.MzFrequencyAnalysisSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.plot.spectrum.DockableSpectumPlot;
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
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.DataExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.MultiCefDataAddTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.MultiCefImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.QuantMatrixImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.ClearIdentificationsTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LibrarySearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LoadDatabaseLibraryTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.CalculateStatisticsTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.ImputeMissingDataTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.MzFrequencyAnalysisTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.MzFrequencyType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.RemoveEmptyFeaturesTask;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.MetabolomicsProjectUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class FeatureDataPanel extends DockableMRC2ToolboxPanel implements ListSelectionListener {

	private DockableFeatureDataTable featureDataTable;
	private DockableDataPlot dataPlot;
	private DockableFeatureIntensitiesTable featureIntensitiesTable;
	private DockableSpectumPlot spectrumPlot;
	private DockableMsTable spectrumTable;
	//	private DockableIdentificationResultsTable idTable;
	private DockableMolStructurePanel molStructurePanel;
	private DockableObjectAnnotationPanel featureAnnotationPanel;
	private DockableCorrelationDataPanel correlationPanel;
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
	
	private boolean cleanEmtyFeatures;
	private MzFrequencyAnalysisSetupDialog mzFrequencyAnalysisSetupDialog;
	private ExperimentPooledSampleManagerDialog experimentPooledSampleManagerDialog;

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
		grid.add(0, 30, 100, 20, identificationsTable);
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
		selectedFeaturesMap = new TreeMap<DataPipeline, Collection<MsFeature>>();
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
		
		if (currentExperiment == null)
			return;

		if (currentExperiment.getExperimentDesign() == null || 
				currentExperiment.getExperimentDesign().getSamples().isEmpty())
			return;

		String command = event.getActionCommand();
		
		if (command.equals(MainActionCommands.LOAD_DATA_COMMAND.getName()))
			showDataLoader();

		if (command.equals(MainActionCommands.LOAD_DATA_FROM_MULTIFILES_COMMAND.getName()))
			showMultifileDataLoader(null);
		
		if (command.equals(MainActionCommands.ADD_DATA_FROM_MULTIFILES_COMMAND.getName()))
			showMultifileDataLoader(activeDataPipeline);

		if (command.equals(MainActionCommands.LOAD_DATA_FROM_EXCEL_FILE_COMMAND.getName()))
			showExcelDataLoader();

		if (command.equals(MainActionCommands.LOAD_LIBRARY_COMMAND.getName()))
			loadLibrary();
		
		if(activeDataPipeline == null) {
			return;
		}
		else {
			if (command.equals(MainActionCommands.CALC_FEATURES_STATS_COMMAND.getName()))
				calculateDataStats();
			
			if (command.equals(MainActionCommands.RECALCULATE_STATISTICS_WITH_SELECTED_POOLS_COMMAND.getName()))
				recalculateDataStatsWithSelectedPools();
			
			if (command.equals(MainActionCommands.CLEAN_EMPTY_FEATURES_COMMAND.getName()))
				cleanEmptyFeatures();
			
			if (command.equals(MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName()))
				showFeatureFilter();
				
			if (command.equals(MainActionCommands.FILTER_FEATURES_COMMAND.getName()))
				filterFeatureTable();

			if (command.equals(MainActionCommands.RESET_FEATURE_FILTERS_COMMAND.getName()))
				resetFeatureTable();

			if (command.equals(MainActionCommands.SHOW_KNOWN_FEATURES_COMMAND.getName()))
				showKnownsOnly();

			if (command.equals(MainActionCommands.SHOW_UNKNOWN_FEATURES_COMMAND.getName()))
				showUnknownsOnly();

			if (command.equals(MainActionCommands.SHOW_QC_FEATURES_COMMAND.getName()))
				showQcOnly();

			// Feature popup
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
			
			if (command.equals(MainActionCommands.GET_DATA_MATRIX_FOR_FEATURE_SET_AND_DESIGN.getName()))
				createDataMatrixForActiveFeatureSet();
		}	
	}

	private void createDataMatrixForActiveFeatureSet() {

		if(activeMsFeatureSet == null)
			return;
				
		Matrix fm = currentExperiment.getDataMatrixForFeatureSetAndDesign(
				activeMsFeatureSet, 
				currentExperiment.getExperimentDesign().getActiveDesignSubset(), 
				activeDataPipeline);
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

			Collection<MsFeature> cleaned = new HashSet<MsFeature>();	
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
	    		return;
	    	}
	    	else {
	    		cleanMsFeatureSet.setProperty(FeatureSetProperties.FILTERING_PARAMETERS, fcp);
	    		MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
	    			addFeatureSetForDataPipeline(cleanMsFeatureSet, activeDataPipeline);
	    		
	    		MainWindow.getExperimentSetupDraw().getFeatureSubsetPanel().addSetListeners(cleanMsFeatureSet);
	    		
	    		 MetabolomicsProjectUtils.switchActiveMsFeatureSet(cleanMsFeatureSet);
				//	setTableModelFromFeatureSet(cleanMsFeatureSet);
	    	}
	    }
	    
	    private Collection<MsFeature>filterByPooledRepresentation(Collection<MsFeature> input){
	    	
	    	Collection<MsFeature>filtered = new HashSet<MsFeature>();
			HashMap<DataFile,Long>fileCoordinates = new HashMap<DataFile,Long>();
			Matrix assayData = currentExperiment.getDataMatrixForDataPipeline(activeDataPipeline);
			final DataAcquisitionMethod method = activeDataPipeline.getAcquisitionMethod();
			Set<DataFile> pooledDataFiles = fcp.getSelectedPooledSamples().stream().
					flatMap(s -> s.getDataFilesForMethod(method).stream()).
					collect(Collectors.toSet());
			
			int totalPoolCount = pooledDataFiles.size();
			double repCutoff = (double)fcp.getPooledFrequencyCutoff() / 100.0d;
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
		
		currentExperiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
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
		
		if(exportType.equals(MainActionCommands.EXPORT_MZRT_STATISTICS_COMMAND)) {
			
			if(currentExperiment.getFeatureMatrixFileNameForDataPipeline(activeDataPipeline) == null) {
				MessageDialog.showWarningMsg(
						"M/Z and RT data for features from individual samples not available", 
						this.getContentPane());
				return;
			}
			File featureMatrixFile = Paths.get(currentExperiment.getExperimentDirectory().getAbsolutePath(), 
					currentExperiment.getFeatureMatrixFileNameForDataPipeline(activeDataPipeline)).toFile();
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
			dataExplorerPlotDialog.loadMzRtFromFeatureCollection(activeMsFeatureSet);
		}
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
			return;
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

			Collection<CompoundLibrary> libraries = new HashSet<CompoundLibrary>();
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
			libsToLoad = new HashSet<CompoundLibrary>();
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

	private void showMultifileDataLoader(DataPipeline pipeline) {

		multiFileDataImportDialog = new MultiFileDataImportDialog(this);
		if(pipeline != null)
			multiFileDataImportDialog.setExistingDataPipeline(pipeline);
			
		multiFileDataImportDialog.setLocationRelativeTo(this.getContentPane());
		multiFileDataImportDialog.setVisible(true);
	}

	private void showDataLoader() {

		if(currentExperiment == null)
			return;
		
		textDataImportDialog = new TextDataImportDialog();
		textDataImportDialog.setLocationRelativeTo(this.getContentPane());
		textDataImportDialog.setVisible(true);
	}

	private void addSelectedFeaturesToActiveSubset() {
		// TODO Auto-generated method stub

	}

	private void calculateDataStats() {

		if(currentExperiment == null || activeDataPipeline == null)
			return;

		experimentPooledSampleManagerDialog = 
				new ExperimentPooledSampleManagerDialog(this);
		experimentPooledSampleManagerDialog.setLocationRelativeTo(this.getContentPane());
		experimentPooledSampleManagerDialog.setVisible(true);
	}
	
	private void recalculateDataStatsWithSelectedPools() {
		
		if(currentExperiment == null || activeDataPipeline == null)
			return;

		Collection<ExperimentalSample>selectedPools = 
					experimentPooledSampleManagerDialog.getSelectedSamples();
		currentExperiment.getPooledSamples().clear();
		currentExperiment.getPooledSamples().addAll(selectedPools);
		experimentPooledSampleManagerDialog.dispose();
		
		// Check if design assigned to data files and pooled/sample are specified
		// TODO If no pooled present and required, calculate for the whole set as samples
		if (ExperimentUtils.designValidForStats(currentExperiment, activeDataPipeline, false)) {

			CalculateStatisticsTask cst = 
					new CalculateStatisticsTask(currentExperiment, activeDataPipeline);
			cst.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(cst);
		} else {
			MessageDialog.showWarningMsg(
					"Experiment design not valid for calculating the statistics (no samples / pools defined	)", 
					this.getContentPane());
		}
	}
	
	private void cleanEmptyFeatures() {
	
		RemoveEmptyFeaturesTask task = 
				new RemoveEmptyFeaturesTask(currentExperiment, activeDataPipeline);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
	
	public synchronized void clearPanel() {

		clearFeatureData();
		featureDataTable.clearTable();
	}

	public void clearFeatureData() {

		dataPlot.clearPlotPanel();
		correlationPanel.clearPanel();
		featureIntensitiesTable.clearTable();
		spectrumPlot.removeAllDataSets();
		spectrumTable.clearTable();
		identificationsTable.clearTable();
		molStructurePanel.clearPanel();
		featureAnnotationPanel.clearPanel();
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
		HashSet<MsFeature> filtered = new HashSet<MsFeature>();
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

		if (selected.size() > 0) {

			int approve = MessageDialog.showChoiceMsg(
					"Remove selected feature(s) from subset?\n" + "(NO UNDO!)", 
					this.getContentPane());

			if (approve == JOptionPane.YES_OPTION)
				activeMsFeatureSet.removeFeatures(selected);
		}
	}

	private void resetFeatureTable() {
		
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
		
		if(activeFeatureSetForMethod.equals(activeMsFeatureSet))
			return;
		
		featureDataTable.clearTable();
		if(activeMsFeatureSet != null)
			activeMsFeatureSet.removeListener(this);

		activeMsFeatureSet = activeFeatureSetForMethod;
		activeMsFeatureSet.addListener(this);
		setTableModelFromFeatureMap(Collections.singletonMap(
				activeDataPipeline, activeMsFeatureSet.getFeatures()));
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
		
			if(activeMsFeatureSet != null) {
				if(activeMsFeatureSet.equals(source)) {
					clearPanel();
					setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, source.getFeatures()));
				}
			}
		}
		if (status.equals(ParameterSetStatus.CREATED)) {

			clearPanel();
			activeMsFeatureSet = source;
			setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, source.getFeatures()));
		}
		if (status.equals(ParameterSetStatus.DELETED)) {

			if(activeMsFeatureSet != null) {
				if(activeMsFeatureSet.equals(source))
					clearPanel();
			}			
		}
		if (status.equals(ParameterSetStatus.ENABLED)) {
		
			if(!activeMsFeatureSet.equals(source)) {
				
				clearPanel();
				activeMsFeatureSet = source;
				setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, source.getFeatures()));
			}

		}
		if (status.equals(ParameterSetStatus.DISABLED)) {
			
			if(activeMsFeatureSet != null) {
				if(activeMsFeatureSet.equals(source))
					clearPanel();
			}	
		}
		if(MainWindow.getDataExplorerPlotDialog().isVisible()) {
			
			if(activeMsFeatureSet == null || status.equals(ParameterSetStatus.DISABLED)) {
				MainWindow.getDataExplorerPlotDialog().clearPanels();
			}
			else {
				MainWindow.getDataExplorerPlotDialog().loadMzRtFromFeatureCollection(activeMsFeatureSet);
			}
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(CalculateStatisticsTask.class))
				updateStatisticsResults((CalculateStatisticsTask) e.getSource());

			if (e.getSource().getClass().equals(ImputeMissingDataTask.class)) {

				String mName = ((ImputeMissingDataTask) e.getSource()).getImputationMethod().getName();
				MessageDialog.showInfoMsg("Data were imputed using " + mName + " method");
			}
			if (e.getSource().getClass().equals(LibrarySearchTask.class))
				reviewLibrarySearchResults((LibrarySearchTask) e.getSource());

			if (e.getSource().getClass().equals(ClearIdentificationsTask.class))
				resetFeatureTable();

			if (e.getSource().getClass().equals(MergeDuplicateFeaturesTask.class)) {

				setTableModelFromFeatureSet(currentExperiment.getActiveFeatureSetForDataPipeline(activeDataPipeline));
				MRC2ToolBoxCore.getMainWindow().getPreferencesDraw().switchDataPipeline(currentExperiment, activeDataPipeline);
			}
			if (e.getSource().getClass().equals(LoadDatabaseLibraryTask.class))
				finalizeDatabaseLibraryLoad((LoadDatabaseLibraryTask) e.getSource());
		
			//	Load data
			if (e.getSource().getClass().equals(QuantMatrixImportTask.class))
				finalizeQuantDataLoad((QuantMatrixImportTask) e.getSource());

			//	Load data from multiple CEF files
			if (e.getSource().getClass().equals(MultiCefImportTask.class))
				finalizeMultiCefDataLoad((MultiCefImportTask) e.getSource());
			
			if (e.getSource().getClass().equals(MultiCefDataAddTask.class))
				finalizeMultiCefDataAddition((MultiCefDataAddTask) e.getSource());
			
			if (e.getSource().getClass().equals(RemoveEmptyFeaturesTask.class))
				finalizeEmptyFeatureCleanup((RemoveEmptyFeaturesTask) e.getSource());
			
			if (e.getSource().getClass().equals(FindDuplicateNamesTask.class))
				finalizeDuplicateNameSearch((FindDuplicateNamesTask) e.getSource());			
		
			if (e.getSource().getClass().equals(MzFrequencyAnalysisTask.class))
				finalizeMzFrequencyAnalysisTask((MzFrequencyAnalysisTask)e.getSource());	
		}
		if (e.getStatus() == TaskStatus.CANCELED || e.getStatus() == TaskStatus.ERROR) {
			MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
			MainWindow.hideProgressDialog();
		}
	}
	
	private void finalizeMzFrequencyAnalysisTask(MzFrequencyAnalysisTask task) {
	
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

	private void finalizeDuplicateNameSearch(FindDuplicateNamesTask task) {

		if(task.getDuplicateNameList().isEmpty()) {
			MessageDialog.showInfoMsg(
					"No duplicate feature names found.", 
					this.getContentPane());
			return;
		}
		Collection<String>dupNames = new TreeSet<String>();
		for(MsFeatureCluster cluster : task.getDuplicateNameList())		
			dupNames.add(cluster.getPrimaryFeature().getName());

		InformationDialog info = new InformationDialog(
				"Duplicate feature names", 
				"Found the following duplicate feature names",
				StringUtils.join(dupNames, "\n"),
				this.getContentPane());
	}
	
	private void finalizeQuantDataLoad(QuantMatrixImportTask quantMatrixImportTask) {

		DataPipeline dataPipeline = quantMatrixImportTask.getDataPipeline();
		currentExperiment.addDataPipeline(dataPipeline);
		currentExperiment.setDataMatrixForDataPipeline(
				dataPipeline, quantMatrixImportTask.getDataMatrix());
		currentExperiment.setFeaturesForDataPipeline(dataPipeline, 
				new HashSet<MsFeature>(quantMatrixImportTask.getFeatureList()));
		currentExperiment.setDataFilesForAcquisitionMethod(dataPipeline.getAcquisitionMethod(), 
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

	private void finalizeMultiCefDataLoad(MultiCefImportTask multiCefImportTask) {
		
		DataPipeline dataPipeline = multiCefImportTask.getDataPipeline();
		MRC2ToolBoxCore.getMainWindow().
			switchDataPipeline(currentExperiment, dataPipeline);
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();

		//	Rerun statistics
		cleanEmtyFeatures = true;
		CalculateStatisticsTask statsTask = 
				new CalculateStatisticsTask(currentExperiment, activeDataPipeline);
		statsTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(statsTask);
	}
	
	private void finalizeMultiCefDataAddition(MultiCefDataAddTask task) {

		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
	
		//	Attach data
		currentExperiment.setDataMatrixForDataPipeline(activeDataPipeline, task.getDataMatrix());
		currentExperiment.addDataFilesForAcquisitionMethod(
				activeDataPipeline.getAcquisitionMethod(), task.getDataFiles());
		
		MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.DESIGN).reloadDesign();

		//	Rerun statistics
		cleanEmtyFeatures = true;
		CalculateStatisticsTask statsTask = 
				new CalculateStatisticsTask(currentExperiment, activeDataPipeline);
		statsTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(statsTask);
	}

	private void finalizeEmptyFeatureCleanup(RemoveEmptyFeaturesTask source) {

		MRC2ToolBoxCore.getMainWindow().getPreferencesDraw().
				switchDataPipeline(currentExperiment, activeDataPipeline);
		resetFeatureTable();
	}

//	private void finalizeLibraryLoad(CefLibraryImportTask lit) {
//
//		setTableModelFromFeatureSet(
//				currentProject.getActiveFeatureSetForDataPipeline(activeDataPipeline));
//
//		// Show unassigned features
//		if (!lit.getUnassigned().isEmpty()) {
//
//			ArrayList<String> flist = new ArrayList<String>();
//
//			for (MsFeature msf : lit.getUnassigned())
//				flist.add(msf.getName());
//
//			@SuppressWarnings("unused")
//			InformationDialog id = new InformationDialog(
//					"Unmatched features",
//					"Not all features were matched to the library.\n"
//					+ "Below is the list of unmatched features.",
//					StringUtils.join(flist, "\n"),
//					this.getContentPane());
//		}
//		// Show unassigned adducts
//		if (!lit.getUnmatchedAdducts().isEmpty()) {
//
//			@SuppressWarnings("unused")
//			InformationDialog id = new InformationDialog(
//					"Unmatched adducts",
//					"Not all adducts were matched to the database.\n"
//					+ "Below is the list of unmatched adducts.",
//					StringUtils.join(lit.getUnmatchedAdducts(), "\n"),
//					this.getContentPane());
//		}
//	}
	
	private void finalizeDatabaseLibraryLoad(LoadDatabaseLibraryTask task) {

		MRC2ToolBoxCore.getActiveMsLibraries().add(task.getLibrary());
		loadedLibsCount = loadedLibsCount + 1;

		if (loadedLibsCount == libsToLoad.size()) {
			populateMissingIdDialog(identifiedFeatures, missingLookupLibs);
			return;
		}
	}

	private void updateStatisticsResults(CalculateStatisticsTask csTask) {
		
		MRC2ToolBoxCore.getMainWindow().
			switchPanelForDataPipeline(activeDataPipeline, PanelList.FEATURE_DATA);
		currentExperiment.setStatisticsStatusForDataPipeline(activeDataPipeline, true);
		MRC2ToolBoxCore.getMainWindow().getPreferencesDraw().switchDataPipeline(currentExperiment, activeDataPipeline);
		currentExperiment.setStatisticsStatusForDataPipeline(activeDataPipeline, true);
		resetFeatureTable();
		
		if(cleanEmtyFeatures) {			
			cleanEmtyFeatures = false;
			cleanEmptyFeatures();
		}
	}

	public void findFeaturesByAdductMasses(
			Collection<Double> monoisotopicAdductMasses, double massAccuracyPpm,
			Range rtRange) {

		if (currentExperiment == null || activeDataPipeline == null)
			return;
		
		Collection<MsFeature> filtered = new TreeSet<MsFeature>(new MsFeatureComparator(SortProperty.RT));
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

	private void reviewLibrarySearchResults(LibrarySearchTask lst) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();

		if (lst.getIdentifiedFeatures().isEmpty()) {

			MessageDialog.showWarningMsg(
					"No identifications found in addition to existing ones", 
					this.getContentPane());
			featureDataTable.getTable().getSelectionModel().addListSelectionListener(this);
			return;
		}
		setTableModelFromFeatureMap(Collections.singletonMap(activeDataPipeline, lst.getIdentifiedFeatures()));
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		//	TODO handle identification table selection separately from feature table
//		if (event.getSource() instanceof DefaultListSelectionModel 
//				&& !event.getValueIsAdjusting()) {
//
//			clearFeatureData();
//			switchSelectedFeatures();
//		}
		
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
//		if(firstSelected.getPrimaryIdentity() != null 
//				&& firstSelected.getPrimaryIdentity().getCompoundIdentity() != null) {
//			try {
//				molStructurePanel.showStructure(firstSelected.getPrimaryIdentity().
//						getCompoundIdentity().getSmiles());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		showCompoundStructure(firstSelected.getPrimaryIdentity());
		featureAnnotationPanel.loadFeatureData(firstSelected);
		if (allSelected.size() > 1) {

			MsFeature[] msf = allSelected.toArray(new MsFeature[allSelected.size()]);
			//	TODO maybe will need an update to handle multiple pipelines
			correlationPanel.createCorrelationPlot(
					msf[0], activeDataPipeline, 
					msf[1], activeDataPipeline);
		}
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
	}

	@Override
	public void closeExperiment() {
		// TODO Auto-generated method stub
		super.closeExperiment();
		clearPanel();
		menuBar.updateMenuFromExperiment(null, null);
	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {

		if (e.getStatus().equals(ParameterSetStatus.CHANGED)) {

			if (currentExperiment != null)
				dataPlot.setActiveDesign(currentExperiment.getExperimentDesign().getActiveDesignSubset());
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
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

	public void refreshIdentificationsTable() {

		MsFeature selectedFeature = getSelectedFeature();
		if(selectedFeature == null)
			identificationsTable.clearTable();
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
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment(),
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline(),
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
}
