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

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonMenuBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureDataPanelMenuBar extends CommonMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -6288875491040382193L;

	// Icons
	private static final Icon loadLibraryIcon = GuiUtils.getIcon("loadLibrary", 24);
	private static final Icon loadPlainDataFileIcon = GuiUtils.getIcon("importTextfile", 24);
	private static final Icon loadPlainDataFileIconSmall = GuiUtils.getIcon("importTextfile", 16);
	private static final Icon loadMultiFileIcon = GuiUtils.getIcon("importMultifile", 24);	
	private static final Icon importFromBinnerIcon = GuiUtils.getIcon("importFromBinner", 24);
	private static final Icon clearBinnerAnnotationsIcon = GuiUtils.getIcon("clearBinnerAnnotations", 24);
	private static final Icon addMultiFileIcon = GuiUtils.getIcon("addMultifile", 24);
	private static final Icon addPeakQualityDataIcon = GuiUtils.getIcon("addStandardSample", 24);	
	private static final Icon loadFromExcelIcon = GuiUtils.getIcon("excelImport", 24);
	private static final Icon calcStatsIcon = GuiUtils.getIcon("calcStats", 24);
	private static final Icon calcStatsIconSmall = GuiUtils.getIcon("calcStats", 16);
	private static final Icon cleanEmptyFeaturesIcon = GuiUtils.getIcon("cleanEmpty", 24);
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 24);
	private static final Icon filterIconSmall = GuiUtils.getIcon("filter", 16);
	private static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 24);
	private static final Icon knownIcon = GuiUtils.getIcon("showKnowns", 24);
	private static final Icon qcIcon = GuiUtils.getIcon("qc", 24);
	private static final Icon unknownIcon = GuiUtils.getIcon("showUnknowns", 24);
	private static final Icon filterBinnerIcon = GuiUtils.getIcon("filterBinner", 24);
	private static final Icon primaryBinnerIcon = GuiUtils.getIcon("primaryBinner", 24);	
	private static final Icon inClusterIcon = GuiUtils.getIcon("inCluster", 24);
	private static final Icon notInClusterIcon = GuiUtils.getIcon("notInCluster", 24);
	private static final Icon searchLibraryIcon = GuiUtils.getIcon("searchLibrary", 24);
	private static final Icon searchLibraryIconSmall = GuiUtils.getIcon("searchLibrary", 16);
	private static final Icon searchDatabaseIcon = GuiUtils.getIcon("searchDatabase", 24);
	private static final Icon showMissingIdentificationsIcon = GuiUtils.getIcon("missingIdentifications", 24);
	private static final Icon clearIdentificationsIcon = GuiUtils.getIcon("clearIdentifications", 24);
	private static final Icon imputeDataIcon = GuiUtils.getIcon("impute", 24);
	private static final Icon bubblePlotIcon = GuiUtils.getIcon("bubble", 24);
	private static final Icon bubblePlotIconSmall = GuiUtils.getIcon("bubble", 16);
	private static final Icon checkDuplicateNamesIcon = GuiUtils.getIcon("checkDuplicateNames", 24);	
	private static final Icon exportResultsIcon = GuiUtils.getIcon("export", 24);
	private static final Icon exportResults4MPPIcon = GuiUtils.getIcon("export4MPP", 24);
	private static final Icon exportResults4BinnerIcon = GuiUtils.getIcon("export4Binner", 24);
	private static final Icon exportResults4RIcon = GuiUtils.getIcon("export4R", 24);		
	private static final Icon exportMetabCombinerIcon = GuiUtils.getIcon("exportMetabCombiner", 24);	
	private static final Icon exportResultsIconSmall = GuiUtils.getIcon("export", 16);
	private static final Icon exportExcelIcon = GuiUtils.getIcon("excel", 24);
	private static final Icon exportMwTabIcon = GuiUtils.getIcon("mwTabReport", 24);
	private static final Icon dataFileToolsIcon = GuiUtils.getIcon("dataFileTools", 24);	
	private static final Icon exportMzRtStatsIcon = GuiUtils.getIcon("exportMZRT", 24);	
	private static final Icon exportPeakWidthStatsIcon = GuiUtils.getIcon("smoothChromatogram", 24);	
	private static final Icon featureCleanupIcon = GuiUtils.getIcon("clearAnnotation", 24);
	private static final Icon mzFrequencyIcon = GuiUtils.getIcon("mzFrequency", 24);
	private static final Icon exportStatsIcon = GuiUtils.getIcon("exportStats", 24);
	private static final Icon multiSpectraIcon = GuiUtils.getIcon("multiSpectra", 24);
	private static final Icon exportAuxDataMatrixIcon = GuiUtils.getIcon("exportAuxDataMatrix", 24);	
	private static final Icon analysisIconSmall = GuiUtils.getIcon("script", 16);
	private static final Icon averageFeaturesIcon = GuiUtils.getIcon("avgSpectrum", 24);
	private static final Icon openAvgFeatureLibraryIcon = GuiUtils.getIcon("openAvgFeatureLibrary", 24);
	

	// Menus
	private JMenu
		loadDataMenu,
		statsMenu,
		searchMenu,
		identificationMenu,
		exportMenu,
		utilsMenu,
		graphicsMenu;

	// Load data items
	private JMenuItem
		loadMultiFileMenuItem,
		addFromMultiFileMenuItem,
		addPeakQualityDataMenuItem,
		loadFromTextMenuItem,
		loadFromExcelMenuItem,
		loadLibraryDataMenuItem,
		importBinnerAnnotationsMenuItem,
		clearBinnerAnnotationsMenuItem;

	// Statistics items
	private JMenuItem
		calculateStatisticsMenuItem,
		mzFrequencyMenuItem,
		exportFeatureStatsMenuItem,
		averageFeaturesMenuItem,
		openAverageFeatureLibraryMenuItem;

	// Search items
	private JMenuItem
		featureFilterMenuItem,
		resetFilterMenuItem,
		showOnlyKnownsMenuItem,
		showOnlyUnknownsMenuItem,
		showQcStandardsMenuItem,		
		showBinnerAnnotatedMenuItem,
		showPrimaryBinnerAnnotatedMenuItem,		
		dataCleanupMenuItem;
	
	// Identification items
	private JMenuItem
		searchLibraryMenuItem,
		searchDatabaseMenuItem,
		showMissingIdsMenuItem,
		clearIdentificationsMenuItem,
		binnerAnalysisMenuItem;
	
	// Export items
	private JMenuItem
		exportForBinnerAnalysisMenuItem,
		exportForMPPAnalysisMenuItem,
		exportForRAnalysisMenuItem,
		exportMzRtStatsMenuItem,
		exportPeakWidthStatsMenuItem,
		exportAllStatsMenuItem,
		exportDialogMenuItem,
		exportIntegratedReportMenuItem,
		exportMWTabReportMenuItem,
		exportMetabCombinerMenuItem;
		
	// Utils items
	private JMenuItem
		sendProjectToTrackerMenuItem,
		checkForDuplicateNamesMenuItem,
		cleanEmptyFeaturesMenuItem;
	
	// Graphics items
	private JMenuItem
		bubblePlotMenuItem,
		multiMsPlotMenuItem;

	public FeatureDataPanelMenuBar(ActionListener listener) {

		super(listener);

		// Load data items
		loadDataMenu = new JMenu("Load data");
		loadDataMenu.setIcon(loadPlainDataFileIconSmall);
		
		loadMultiFileMenuItem = addItem(loadDataMenu, 
				MainActionCommands.LOAD_DATA_FROM_MULTIFILES_COMMAND, 
				loadMultiFileIcon);
		addFromMultiFileMenuItem = addItem(loadDataMenu, 
				MainActionCommands.ADD_DATA_FROM_MULTIFILES_COMMAND, 
				addMultiFileIcon);		
		addPeakQualityDataMenuItem = addItem(loadDataMenu, 
				MainActionCommands.ADD_PEAK_QUALITY_DATA_FROM_MULTIFILES_COMMAND, 
				addPeakQualityDataIcon);
		
		loadDataMenu.addSeparator();
		
		loadFromTextMenuItem = addItem(loadDataMenu, 
				MainActionCommands.LOAD_DATA_COMMAND, 
				loadPlainDataFileIcon);
		loadLibraryDataMenuItem = addItem(loadDataMenu, 
				MainActionCommands.LOAD_LIBRARY_COMMAND, 
				loadLibraryIcon);
		
		loadDataMenu.addSeparator();
		
		loadFromExcelMenuItem = addItem(loadDataMenu,
				MainActionCommands.LOAD_DATA_FROM_EXCEL_FILE_COMMAND, 
				loadFromExcelIcon);
		
		add(loadDataMenu);

		// Statistics items
		statsMenu = new JMenu("Statistics");
		statsMenu.setIcon(calcStatsIconSmall);
		
		calculateStatisticsMenuItem = addItem(statsMenu, 
				MainActionCommands.CALC_FEATURES_STATS_COMMAND, 
				calcStatsIcon);
				
		mzFrequencyMenuItem = addItem(statsMenu, 
				MainActionCommands.SET_UP_MZ_FREQUENCY_ANALYSIS_COMMAND, 
				mzFrequencyIcon);
		
		exportFeatureStatsMenuItem = addItem(statsMenu, 
				MainActionCommands.EXPORT_FEATURE_STATISTICS_COMMAND, 
				exportStatsIcon);
		
		statsMenu.addSeparator();
		
		averageFeaturesMenuItem = addItem(statsMenu, 
				MainActionCommands.AVERAGE_FEATURES_LIBRARY_SETUP_COMMAND, 
				averageFeaturesIcon);
		
		openAverageFeatureLibraryMenuItem = addItem(statsMenu, 
				MainActionCommands.OPEN_AVERAGE_FEATURES_LIBRARY_COMMAND, 
				openAvgFeatureLibraryIcon);
		
		add(statsMenu);
		
		// Search items
		searchMenu = new JMenu("Search/Filter");
		searchMenu.setIcon(filterIconSmall);
		
		featureFilterMenuItem = addItem(searchMenu, 
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND, 
				filterIcon);
		showOnlyKnownsMenuItem = addItem(searchMenu, 
				MainActionCommands.SHOW_KNOWN_FEATURES_COMMAND, 
				knownIcon);
		showOnlyUnknownsMenuItem = addItem(searchMenu, 
				MainActionCommands.SHOW_UNKNOWN_FEATURES_COMMAND, 
				unknownIcon);
		showQcStandardsMenuItem = addItem(searchMenu, 
				MainActionCommands.SHOW_QC_FEATURES_COMMAND, 
				qcIcon);
		
		searchMenu.addSeparator();
		
		showBinnerAnnotatedMenuItem = addItem(searchMenu, 
				MainActionCommands.SHOW_BINNER_ANNOTATED_FEATURES_COMMAND, 
				filterBinnerIcon);
		showPrimaryBinnerAnnotatedMenuItem = addItem(searchMenu, 
				MainActionCommands.SHOW_PRIMARY_BINNER_ANNOTATED_FEATURES_COMMAND, 
				primaryBinnerIcon);
		
		searchMenu.addSeparator();
		
		resetFilterMenuItem = addItem(searchMenu, 
				MainActionCommands.RESET_FEATURE_FILTERS_COMMAND, 
				resetFilterIcon);
		
		searchMenu.addSeparator();
		
		dataCleanupMenuItem = addItem(searchMenu, 
				MainActionCommands.SHOW_DATA_CLEANUP_DIALOG_COMMAND, 
				featureCleanupIcon);
		
		add(searchMenu);
		
		// Identification items
		identificationMenu = new JMenu("Identification");
		identificationMenu.setIcon(searchLibraryIconSmall);
		
		searchLibraryMenuItem = addItem(identificationMenu, 
				MainActionCommands.MS_RT_LIBRARY_SEARCH_SETUP_COMMAND, 
				searchLibraryIcon);
		searchDatabaseMenuItem = addItem(identificationMenu, 
				MainActionCommands.COMPOUND_DATABASE_SEARCH_SETUP_COMMAND, 
				searchDatabaseIcon);
		
		identificationMenu.addSeparator();
		
		showMissingIdsMenuItem = addItem(identificationMenu, 
				MainActionCommands.SHOW_MISSING_IDENTIFICATIONS_COMMAND, 
				showMissingIdentificationsIcon);
		clearIdentificationsMenuItem = addItem(identificationMenu,
				MainActionCommands.CLEAR_IDENTIFICATIONS_COMMAND, 
				clearIdentificationsIcon);
		
		identificationMenu.addSeparator();
	
		importBinnerAnnotationsMenuItem = addItem(identificationMenu,
				MainActionCommands.IMPORT_BINNER_ANNOTATIONS_COMMAND, 
				importFromBinnerIcon);
		clearBinnerAnnotationsMenuItem = addItem(identificationMenu,
				MainActionCommands.CLEAR_BINNER_ANNOTATIONS_COMMAND, 
				clearBinnerAnnotationsIcon);
		
		add(identificationMenu);
		
		// Export items
		exportMenu = new JMenu("Export");
		exportMenu.setIcon(exportResultsIconSmall);
		
		exportForBinnerAnalysisMenuItem = addItem(exportMenu,
				MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND, 
				exportResults4BinnerIcon);
		exportForMPPAnalysisMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND, 
				exportResults4MPPIcon);
		exportForRAnalysisMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_RESULTS_4R_COMMAND, 
				exportResults4RIcon);
		exportMetabCombinerMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_RESULTS_4METAB_COMBINER_COMMAND, 
				exportMetabCombinerIcon);
		
		exportMenu.addSeparator();
		
//		exportMzRtStatsMenuItem = addItem(exportMenu, 
//				MainActionCommands.EXPORT_MZRT_STATISTICS_COMMAND, 
//				exportMzRtStatsIcon);
//		
//		exportPeakWidthStatsMenuItem = addItem(exportMenu, 
//				MainActionCommands.EXPORT_PEAK_WIDTH_STATISTICS_COMMAND, 
//				exportPeakWidthStatsIcon);
		
		exportAllStatsMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_ALL_FEATURE_STATISTICS_COMMAND, 
				exportAuxDataMatrixIcon);
		
		exportMenu.addSeparator();
		
		exportDialogMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_RESULTS_COMMAND, 
				exportResultsIcon);
		
		exportMenu.addSeparator();
		
		exportIntegratedReportMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND, 
				exportExcelIcon);
		exportMWTabReportMenuItem = addItem(exportMenu, 
				MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND, 
				exportMwTabIcon);
		
//		addItem(exportMenu, 
//				MainActionCommands.GET_DATA_MATRIX_FOR_FEATURE_SET_AND_DESIGN, 
//				null);
		
		add(exportMenu);
		
		// Utils items
		utilsMenu = new JMenu("Utilities");
		utilsMenu.setIcon(dataFileToolsIcon);
		
		checkForDuplicateNamesMenuItem = addItem(utilsMenu, 
				MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND, 
				checkDuplicateNamesIcon);
		cleanEmptyFeaturesMenuItem = addItem(utilsMenu, 
				MainActionCommands.CLEAN_EMPTY_FEATURES_COMMAND, 
				cleanEmptyFeaturesIcon);
		
//		utilsMenu.addSeparator();
//		
//		sendProjectToTrackerMenuItem = addItem(utilsMenu, sendProjectToTrackerMenuItem, 
//				MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND, 
//				exportMwTabIcon);
		
		add(utilsMenu);
		
		// Graphics items
		graphicsMenu = new JMenu("Visualization");
		graphicsMenu.setIcon(bubblePlotIconSmall);
		
		bubblePlotMenuItem = addItem(graphicsMenu,  
				MainActionCommands.SHOW_DATA_EXPLORER_FRAME, 
				bubblePlotIcon);		
		multiMsPlotMenuItem = addItem(graphicsMenu,  
				MainActionCommands.SHOW_MS_MULTIPLOT_FRAME, 
				multiSpectraIcon);
		
		add(graphicsMenu);
		
		adjustEnabledButtonsForConfiguration();
	}

	public void updateMenuFromExperiment(DataAnalysisProject currentProject, DataPipeline activePipeline) {
		// TODO Auto-generated method stub

	}
	
	//	TODO
	private void adjustEnabledButtonsForConfiguration() {
				
		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.IDTRACKER)) {			

		}
	}
}
