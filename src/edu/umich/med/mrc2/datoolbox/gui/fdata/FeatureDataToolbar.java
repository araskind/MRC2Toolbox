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
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.StartupConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureDataToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -1934124108412393204L;

	private static final Icon loadLibraryIcon = GuiUtils.getIcon("loadLibrary", 32);
	private static final Icon loadPlainDataFileIcon = GuiUtils.getIcon("importTextfile", 32);
	private static final Icon loadMultiFileIcon = GuiUtils.getIcon("importMultifile", 32);	
	private static final Icon addMultiFileIcon = GuiUtils.getIcon("addMultifile", 32);
	private static final Icon loadFromExcelIcon = GuiUtils.getIcon("excelImport", 32);
	private static final Icon calcStatsIcon = GuiUtils.getIcon("calcStats", 32);
	private static final Icon cleanEmptyFeaturesIcon = GuiUtils.getIcon("cleanEmpty", 32);
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 32);
	private static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 32);
	private static final Icon knownIcon = GuiUtils.getIcon("showKnowns", 32);
	private static final Icon qcIcon = GuiUtils.getIcon("qc", 32);
	private static final Icon unknownIcon = GuiUtils.getIcon("showUnknowns", 32);
	private static final Icon inClusterIcon = GuiUtils.getIcon("inCluster", 32);
	private static final Icon notInClusterIcon = GuiUtils.getIcon("notInCluster", 32);
	private static final Icon searchLibraryIcon = GuiUtils.getIcon("searchLibrary", 32);
	private static final Icon searchDatabaseIcon = GuiUtils.getIcon("searchDatabase", 32);
	private static final Icon showMissingIdentificationsIcon = GuiUtils.getIcon("missingIdentifications", 32);
	private static final Icon clearIdentificationsIcon = GuiUtils.getIcon("clearIdentifications", 32);
	private static final Icon imputeDataIcon = GuiUtils.getIcon("impute", 32);
	private static final Icon bubblePlotIcon = GuiUtils.getIcon("bubble", 32);
	private static final Icon checkDuplicateNamesIcon = GuiUtils.getIcon("checkDuplicateNames", 32);	
	private static final Icon exportResultsIcon = GuiUtils.getIcon("export", 32);
	private static final Icon exportExcelIcon = GuiUtils.getIcon("excel", 32);
	private static final Icon exportMwTabIcon = GuiUtils.getIcon("mwTabReport", 32);
	
	@SuppressWarnings("unused")
	private JButton
		loadPlainDataFileButton,
		loadMultiFileButton,
		addMultiFileButton,
		loadFromExcelButton,
		loadLibraryButton,
		calcStatsButton,
		cleanEmptyFeaturesButton,
		filterFeaturesButton,
		resetFilterButton,
		showKnownOnlyButton,
		showUnknownOnlyButton,
		showQCOnlyButton,
		inClusterButton,
		notInClusterButton,
		searchLibraryButton,
		searchDatabaseButton,
		showMissingIdentificationsButton,
		clearIdentificationsButon,
		imputeMissingDataButton,
		bubblePlotButton,
		checkDuplicateNamesButton,
		exportResultsButton,
		exportExcelButton,
		exportMwTabButton;
	
//	private JComboBox dataPipelineComboBox;
//	private ItemListener iListener;

	@SuppressWarnings("unchecked")
	public FeatureDataToolbar(ActionListener commandListener) {

		super(commandListener);		

		loadMultiFileButton = GuiUtils.addButton(this, null, loadMultiFileIcon, commandListener,
				MainActionCommands.LOAD_DATA_FROM_MULTIFILES_COMMAND.getName(),
				MainActionCommands.LOAD_DATA_FROM_MULTIFILES_COMMAND.getName(),
				buttonDimension);
		
		addMultiFileButton = GuiUtils.addButton(this, null, addMultiFileIcon, commandListener,
				MainActionCommands.ADD_DATA_FROM_MULTIFILES_COMMAND.getName(),
				MainActionCommands.ADD_DATA_FROM_MULTIFILES_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);

		loadFromExcelButton = GuiUtils.addButton(this, null, loadFromExcelIcon, commandListener,
				MainActionCommands.LOAD_DATA_FROM_EXCEL_FILE_COMMAND.getName(),
				MainActionCommands.LOAD_DATA_FROM_EXCEL_FILE_COMMAND.getName(),
				buttonDimension);

		loadPlainDataFileButton = GuiUtils.addButton(this, null, loadPlainDataFileIcon, commandListener,
				MainActionCommands.LOAD_DATA_COMMAND.getName(),
				MainActionCommands.LOAD_DATA_COMMAND.getName(),
				buttonDimension);

		loadLibraryButton = GuiUtils.addButton(this, null, loadLibraryIcon, commandListener,
				MainActionCommands.LOAD_LIBRARY_COMMAND.getName(),
				MainActionCommands.LOAD_LIBRARY_COMMAND.getName(),
				buttonDimension);

		addSeparator(buttonDimension);

		calcStatsButton = GuiUtils.addButton(this, null, calcStatsIcon, commandListener,
				MainActionCommands.CALC_FEATURES_STATS_COMMAND.getName(),
				MainActionCommands.CALC_FEATURES_STATS_COMMAND.getName(), buttonDimension);
		
		cleanEmptyFeaturesButton = GuiUtils.addButton(this, null, cleanEmptyFeaturesIcon, commandListener,
				MainActionCommands.CLEAN_EMPTY_FEATURES_COMMAND.getName(),
				MainActionCommands.CLEAN_EMPTY_FEATURES_COMMAND.getName(), buttonDimension);
		
		checkDuplicateNamesButton = GuiUtils.addButton(this, null, checkDuplicateNamesIcon, commandListener,
				MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND.getName(),
				MainActionCommands.CHECK_FOR_DUPLICATE_NAMES_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		showKnownOnlyButton = GuiUtils.addButton(this, null, knownIcon, commandListener,
				MainActionCommands.SHOW_KNOWN_FEATURES_COMMAND.getName(),
				MainActionCommands.SHOW_KNOWN_FEATURES_COMMAND.getName(), buttonDimension);

		showUnknownOnlyButton = GuiUtils.addButton(this, null, unknownIcon, commandListener,
				MainActionCommands.SHOW_UNKNOWN_FEATURES_COMMAND.getName(),
				MainActionCommands.SHOW_UNKNOWN_FEATURES_COMMAND.getName(), buttonDimension);

		showQCOnlyButton = GuiUtils.addButton(this, null, qcIcon, commandListener,
				MainActionCommands.SHOW_QC_FEATURES_COMMAND.getName(),
				MainActionCommands.SHOW_QC_FEATURES_COMMAND.getName(), buttonDimension);

		filterFeaturesButton = GuiUtils.addButton(this, null, filterIcon, commandListener,
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName(),
				MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName(), buttonDimension);

		resetFilterButton = GuiUtils.addButton(this, null, resetFilterIcon, commandListener,
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(),
				MainActionCommands.RESET_FILTER_CLUSTERS_COMMAND.getName(), buttonDimension);

//		addSeparator(buttonDimension);
//
//		inClusterButton = GuiUtils.addButton(this, null, inClusterIcon, commandListener,
//				MainActionCommands.SHOW_FEATURES_ASSIGNED_TO_CLUSTERS_COMMAND.getName(),
//				MainActionCommands.SHOW_FEATURES_ASSIGNED_TO_CLUSTERS_COMMAND.getName(), buttonDimension);
//
//		notInClusterButton = GuiUtils.addButton(this, null, notInClusterIcon, commandListener,
//				MainActionCommands.SHOW_FEATURES_NOT_ASSIGNED_TO_CLUSTERS_COMMAND.getName(),
//				MainActionCommands.SHOW_FEATURES_NOT_ASSIGNED_TO_CLUSTERS_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		searchLibraryButton = GuiUtils.addButton(this, null, searchLibraryIcon, commandListener,
				MainActionCommands.MS_RT_LIBRARY_SEARCH_SETUP_COMMAND.getName(),
				MainActionCommands.MS_RT_LIBRARY_SEARCH_SETUP_COMMAND.getName(), buttonDimension);

		searchDatabaseButton = GuiUtils.addButton(this, null, searchDatabaseIcon, commandListener,
				MainActionCommands.COMPOUND_DATABASE_SEARCH_SETUP_COMMAND.getName(),
				MainActionCommands.COMPOUND_DATABASE_SEARCH_SETUP_COMMAND.getName(), buttonDimension);

		showMissingIdentificationsButton = GuiUtils.addButton(this, null, showMissingIdentificationsIcon, commandListener,
				MainActionCommands.SHOW_MISSING_IDENTIFICATIONS_COMMAND.getName(),
				MainActionCommands.SHOW_MISSING_IDENTIFICATIONS_COMMAND.getName(), buttonDimension);

		clearIdentificationsButon = GuiUtils.addButton(this, null, clearIdentificationsIcon, commandListener,
				MainActionCommands.CLEAR_IDENTIFICATIONS_COMMAND.getName(),
				MainActionCommands.CLEAR_IDENTIFICATIONS_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);
		
		bubblePlotButton = GuiUtils.addButton(this, null, bubblePlotIcon, commandListener,
				MainActionCommands.SHOW_DATA_EXPLORER_FRAME.getName(),
				MainActionCommands.SHOW_DATA_EXPLORER_FRAME.getName(), buttonDimension);
//
//		imputeMissingDataButton = GuiUtils.addButton(this, null, imputeDataIcon, commandListener,
//				MainActionCommands.SHOW_IMPUTE_DIALOG_COMMAND.getName(),
//				MainActionCommands.SHOW_IMPUTE_DIALOG_COMMAND.getName(), buttonDimension);
		
		addSeparator(buttonDimension);

		exportResultsButton = GuiUtils.addButton(this, null, exportResultsIcon, null,
				MainActionCommands.EXPORT_RESULTS_COMMAND.getName(),
				MainActionCommands.EXPORT_RESULTS_COMMAND.getName(), buttonDimension);

		exportExcelButton = GuiUtils.addButton(this, null, exportExcelIcon, null,
				MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND.getName(),
				MainActionCommands.EXPORT_RESULTS_TO_EXCEL_COMMAND.getName(), buttonDimension);

		exportMwTabButton = GuiUtils.addButton(this, null, exportMwTabIcon, null,
				MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND.getName(),
				MainActionCommands.EXPORT_RESULTS_TO_MWTAB_COMMAND.getName(), buttonDimension);

//		addSeparator(buttonDimension);
//		Component horizontalGlue = Box.createHorizontalGlue();
//		add(horizontalGlue);
//
//		JLabel lblNewLabel = new JLabel("Data pipline: ");
//		add(lblNewLabel);

//		dataPipelineComboBox = new JComboBox<DataPipeline>();
//		dataPipelineComboBox.setFont(new Font("Tahoma", Font.BOLD, 14));
//		dataPipelineComboBox.setModel(
//				new SortedComboBoxModel<DataPipeline>(new DataPipeline[0]));
//		dataPipelineComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
//		dataPipelineComboBox.setMaximumSize(new Dimension(200, 30));
//		dataPipelineComboBox.setMinimumSize(new Dimension(200, 30));
//		add(dataPipelineComboBox);
		
		adjustEnabledButtonsForConfiguration();
	}
	
//	public void setProjectActionListener(ActionListener listener) {
//		
//		exportResultsButton.addActionListener(listener);
//		exportExcelButton.addActionListener(listener);
//		exportMwTabButton.addActionListener(listener);
//		iListener = (ItemListener) listener;
//		dataPipelineComboBox.addItemListener(iListener);
//	}
	
	private void adjustEnabledButtonsForConfiguration() {
		
		if(BuildInformation.getStartupConfiguration().equals(StartupConfiguration.IDTRACKER)) {			
//			dataPipelineComboBox.setEnabled(false);
			exportResultsButton.setEnabled(false);
			exportExcelButton.setEnabled(false);
			exportMwTabButton.setEnabled(false);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		
		DataAnalysisProject currentProject = project;
		if(currentProject == null) {
			noProject();
			return;
		}
		projectActive();
		if (newDataPipeline != null) {
			exportResultsButton.setEnabled(
					currentProject.dataPipelineHasData(newDataPipeline));
			exportExcelButton.setEnabled(
					currentProject.dataPipelineHasData(newDataPipeline));
		}
		else {
			exportResultsButton.setEnabled(false);
			exportExcelButton.setEnabled(false);
		}
		// Assay selector
//		dataPipelineComboBox.removeItemListener(iListener);
//		DataPipeline[] projectAssays = currentProject.getDataPipelines().
//				toArray(new DataPipeline[currentProject.getDataPipelines().size()]);
//		dataPipelineComboBox.setModel(
//				new SortedComboBoxModel<DataPipeline>(projectAssays));
//		dataPipelineComboBox.setEnabled(true);
//
//		if (newDataPipeline != null)
//			dataPipelineComboBox.setSelectedItem(newDataPipeline);
//		else
//			dataPipelineComboBox.setSelectedIndex(-1);
//
//		if (projectAssays.length == 0)
//			dataPipelineComboBox.setEnabled(false);
//
//		dataPipelineComboBox.addItemListener(iListener);
	}
	
	@SuppressWarnings("unchecked")
	public void noProject() {
		calcStatsButton.setEnabled(true);
		showKnownOnlyButton.setEnabled(true);
		showUnknownOnlyButton.setEnabled(true);
		showQCOnlyButton.setEnabled(true);
//		inClusterButton.setEnabled(true);
//		notInClusterButton.setEnabled(true);
		filterFeaturesButton.setEnabled(true);
		resetFilterButton.setEnabled(true);
		//imputeMissingDataButton.setEnabled(true);
		exportResultsButton.setEnabled(false);
		exportExcelButton.setEnabled(false);
		exportMwTabButton.setEnabled(false);
//		dataPipelineComboBox.removeItemListener(iListener);
//		dataPipelineComboBox.setModel(
//				new SortedComboBoxModel<DataPipeline>(new DataPipeline[0]));
//		dataPipelineComboBox.addItemListener(iListener);
//		dataPipelineComboBox.setEnabled(false);
	}

	public void projectActive() {
		calcStatsButton.setEnabled(false);
		showKnownOnlyButton.setEnabled(false);
		showUnknownOnlyButton.setEnabled(false);
		showQCOnlyButton.setEnabled(false);
//		inClusterButton.setEnabled(false);
//		notInClusterButton.setEnabled(false);
		filterFeaturesButton.setEnabled(false);
		resetFilterButton.setEnabled(false);
		//imputeMissingDataButton.setEnabled(false);
		exportResultsButton.setEnabled(true);
		exportExcelButton.setEnabled(true);
		exportMwTabButton.setEnabled(true);
//		dataPipelineComboBox.setEnabled(true);
	}
}
