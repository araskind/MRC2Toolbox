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

package edu.umich.med.mrc2.datoolbox.gui.io;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import edu.umich.med.mrc2.datoolbox.data.enums.DataTypeForImport;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MultiFileImportToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 3815759313572710153L;
	
	private static final Icon selectLibraryIcon = GuiUtils.getIcon("loadLibrary", 24);
	private static final Icon addDataFilesIcon = GuiUtils.getIcon("addMultifile", 24);
	private static final Icon pcdlLibraryIcon = GuiUtils.getIcon("newPCDLfromBase", 24);
	private static final Icon csvIcon = GuiUtils.getIcon("csv", 24);
	private static final Icon detailedCsvIcon = GuiUtils.getIcon("detailedCsv", 24);
	private static final Icon normalizedDataIcon = GuiUtils.getIcon("loess", 24);	
	private static final Icon adductIcon = GuiUtils.getIcon("editModification", 24);
	private static final Icon loadPfaFileIcon = GuiUtils.getIcon("importFromProFinderPaf", 24);
	private static final Icon removeDataFilesIcon = GuiUtils.getIcon("removeMultifile", 24);
	private static final Icon clearDataIcon = GuiUtils.getIcon("clearWorklist", 24);
	private static final Icon editReferenceSamplesIcon = GuiUtils.getIcon("standardSample", 24);
	private static final Icon loadDesignIcon = GuiUtils.getIcon("loadDesign", 24);
	private static final Icon addAcqMethodIcon = GuiUtils.getIcon("addDataAcquisitionMethod", 24);
	private static final Icon addDextrMethodIcon = GuiUtils.getIcon("addDataProcessingMethod", 24);
	private JButton
		//	dataAnalysisPipelineButton,
		selectLibraryButton,
		addDataFilesButton,
		selectPCDLLibraryButton,
		selectAdductsButton,
		selectSimpleProFinderCSVButton,
		selectDetailedProFinderCSVButton,
		loadFromProFinderPfaButton,
		selectNormalizedTargetedDataButton,
		removeDataFilesButton,
		clearDataButton,
		editReferenceSamplesButton,
		loadDesignButton,
		addAcqMethodButton,
		addDextrMethodButton;

	private JSpinner taskNumberSpinner;
	private JComboBox<DataTypeForImport>importTypeComboBox;

	public MultiFileImportToolbar(ActionListener commandListener) {
		
		super(commandListener);

		importTypeComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(DataTypeForImport.values()));
		importTypeComboBox.addItemListener((ItemListener)commandListener);
		importTypeComboBox.setMaximumSize(new Dimension(300,80));
		add(importTypeComboBox);
		
		addSeparator(buttonDimension);
		
		selectLibraryButton = GuiUtils.addButton(this, null, selectLibraryIcon, commandListener,
				MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName(),
				MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName(), buttonDimension);
		
		addDataFilesButton = GuiUtils.addButton(this, null, addDataFilesIcon, commandListener,
				MainActionCommands.ADD_DATA_FILES_COMMAND.getName(),
				MainActionCommands.ADD_DATA_FILES_COMMAND.getName(), buttonDimension);
		
//		selectPCDLLibraryButton = GuiUtils.addButton(this, null, pcdlLibraryIcon, commandListener,
//				MainActionCommands.SELECT_PCDL_LIBRARY_COMMAND.getName(),
//				MainActionCommands.SELECT_PCDL_LIBRARY_COMMAND.getName(), buttonDimension);

		addSeparator(buttonDimension);

		selectAdductsButton = GuiUtils.addButton(this, null, adductIcon, commandListener,
				MainActionCommands.SHOW_ADDUCT_SELECTOR.getName(),
				MainActionCommands.SHOW_ADDUCT_SELECTOR.getName(), buttonDimension);
		
		loadFromProFinderPfaButton = GuiUtils.addButton(this, null, loadPfaFileIcon, commandListener,
				MainActionCommands.LOAD_DATA_FROM_PROFINDER_PFA_COMMAND.getName(),
				MainActionCommands.LOAD_DATA_FROM_PROFINDER_PFA_COMMAND.getName(),
				buttonDimension);
		
		selectSimpleProFinderCSVButton = GuiUtils.addButton(this, null, csvIcon, commandListener,
				MainActionCommands.SELECT_PROFINDER_SIMPLE_CSV_COMMAND.getName(),
				MainActionCommands.SELECT_PROFINDER_SIMPLE_CSV_COMMAND.getName(),
				buttonDimension);
		
		selectDetailedProFinderCSVButton = GuiUtils.addButton(this, null, detailedCsvIcon, commandListener,
				MainActionCommands.SELECT_PROFINDER_DETAILED_CSV_COMMAND.getName(),
				MainActionCommands.SELECT_PROFINDER_DETAILED_CSV_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		selectNormalizedTargetedDataButton = GuiUtils.addButton(this, null, normalizedDataIcon, commandListener,
				MainActionCommands.SELECT_TARGETED_DATA_FILE_COMMAND.getName(),
				MainActionCommands.SELECT_TARGETED_DATA_FILE_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		removeDataFilesButton = GuiUtils.addButton(this, null, removeDataFilesIcon, commandListener,
				MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName(),
				MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName(), buttonDimension);

		clearDataButton = GuiUtils.addButton(this, null, clearDataIcon, commandListener,
				MainActionCommands.CLEAR_DATA_COMMAND.getName(),
				MainActionCommands.CLEAR_DATA_COMMAND.getName(), buttonDimension);
		
		addSeparator(buttonDimension);

		editReferenceSamplesButton = GuiUtils.addButton(this, null, editReferenceSamplesIcon, commandListener,
				MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		addAcqMethodButton = GuiUtils.addButton(this, null, addAcqMethodIcon, commandListener,
				MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		addDextrMethodButton = GuiUtils.addButton(this, null, addDextrMethodIcon, commandListener,
				MainActionCommands.ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		loadDesignButton = GuiUtils.addButton(this, null, loadDesignIcon, commandListener,
				MainActionCommands.LOAD_DATA_FILE_SAMPLE_MAP_COMMAND.getName(),
				MainActionCommands.LOAD_DATA_FILE_SAMPLE_MAP_COMMAND.getName(),
				buttonDimension);
		
		addSeparator(buttonDimension);
		
		add(new JLabel("# of tasks: "));
		
		taskNumberSpinner = new JSpinner();
		taskNumberSpinner.setModel(new SpinnerNumberModel(
				MRC2ToolBoxConfiguration.getMaxThreadNumber(), 1, null, 1));
		taskNumberSpinner.setPreferredSize(new Dimension(30, 22));
		taskNumberSpinner.setMinimumSize(new Dimension(30, 22));
		taskNumberSpinner.setMaximumSize(new Dimension(80, 22));
		
		JComponent comp = taskNumberSpinner.getEditor();
	    JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
	    DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
	    formatter.setCommitsOnValidEdit(true);
	    taskNumberSpinner.addChangeListener(new ChangeListener() {

	        @Override
	        public void stateChanged(ChangeEvent e) {
	        	MRC2ToolBoxConfiguration.setMaxThreadNumber((int) taskNumberSpinner.getValue());
	        }
	    });	    
		add(taskNumberSpinner);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		boolean active = true;
		if(project == null)
			active = false;

		//	dataAnalysisPipelineButton.setEnabled(active);
		selectLibraryButton.setEnabled(active);
		addDataFilesButton.setEnabled(active);
		loadFromProFinderPfaButton.setEnabled(active);
		removeDataFilesButton.setEnabled(active);
		clearDataButton.setEnabled(active);
		editReferenceSamplesButton.setEnabled(active);
		loadDesignButton.setEnabled(active);
		addAcqMethodButton.setEnabled(active);
		addDextrMethodButton.setEnabled(active);
	}
	
	public void setDataTypeForImport(DataTypeForImport newType) {
		importTypeComboBox.setSelectedItem(newType);
	}
	
	public DataTypeForImport getDataTypeForImport() {
		return (DataTypeForImport)importTypeComboBox.getSelectedItem();
	}
	
	public void updateInterfaceForImportType(DataTypeForImport importType) {
		
		if(importType.equals(DataTypeForImport.AGILENT_UNTARGETED))
			updateInterfaceForAgilentUntargetedImport();
		
		if(importType.equals(DataTypeForImport.AGILENT_PROFINDER_TARGETED))
			updateInterfaceForProFinderImport();
		
		if(importType.equals(DataTypeForImport.GENERIC_TARGETED))
			updateInterfaceForGenericTargetedImport();	
	}

	private void updateInterfaceForAgilentUntargetedImport() {

		selectLibraryButton.setEnabled(true);
		addDataFilesButton.setEnabled(true);
		loadFromProFinderPfaButton.setEnabled(true);
		
		selectNormalizedTargetedDataButton.setEnabled(false);
		
		selectAdductsButton.setEnabled(false);
		selectSimpleProFinderCSVButton.setEnabled(false);
		selectDetailedProFinderCSVButton.setEnabled(false);
		loadFromProFinderPfaButton.setEnabled(false);
	}

	private void updateInterfaceForProFinderImport() {
		
		selectLibraryButton.setEnabled(false);
		addDataFilesButton.setEnabled(false);
		loadFromProFinderPfaButton.setEnabled(false);
		
		selectNormalizedTargetedDataButton.setEnabled(false);
		
		selectAdductsButton.setEnabled(true);
		selectSimpleProFinderCSVButton.setEnabled(true);
		selectDetailedProFinderCSVButton.setEnabled(true);
		loadFromProFinderPfaButton.setEnabled(true);
	}

	private void updateInterfaceForGenericTargetedImport() {
		
		selectLibraryButton.setEnabled(false);
		addDataFilesButton.setEnabled(false);
		loadFromProFinderPfaButton.setEnabled(false);
		
		selectNormalizedTargetedDataButton.setEnabled(true);
		
		selectAdductsButton.setEnabled(false);
		selectSimpleProFinderCSVButton.setEnabled(false);
		selectDetailedProFinderCSVButton.setEnabled(false);
		loadFromProFinderPfaButton.setEnabled(false);		
	}
}



