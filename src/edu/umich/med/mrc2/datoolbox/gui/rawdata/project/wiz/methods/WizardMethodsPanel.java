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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.methods;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.AcquisitionMethodExtendedEditorDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.DockableAcquisitionMethodDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.dpl.AcquisitionMethodSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RawDataProjectMetadataWizard;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RawDataProjectMetadataWizardPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.LIMSWorklistAcquisitiomMethodScanTask;

public class WizardMethodsPanel extends RawDataProjectMetadataWizardPanel 
	implements ActionListener, BackedByPreferences, TaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2460123645924507011L;
	
	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	
	private AcquisitionMethodTable acquisitionMethodTable;
	private AcquisitionMethodToolbar acquisitionMethodToolbar;
	private AcquisitionMethodSelectorDialog acquisitionMethodSelectorDialog;
	private AcquisitionMethodExtendedEditorDialog acquisitionMethodEditorDialog;
	
	private File baseDirectory;
	private Collection<DataAcquisitionMethod>dataAcquisitionMethods;
	
//	private MethodTable dataAnalysisMethodTable;
//	private DataAnalysisMethodToolbar dataAnalysisMethodToolbar;
//	private DataAnalysisMethodSelectionDialog dataAnalysisMethodSelectionDialog;
//	private DataExtractionMethodEditorDialog dataExtractionMethodEditorDialog;
//	private Collection<DataExtractionMethod>dataExtractionMethods;
	
	private TreeMap<String, File> missingMethodNameToFileMap;
	
	public WizardMethodsPanel(RawDataProjectMetadataWizard wizard) {
		
		super(wizard);
		JPanel panel  = initPanel();
		add(panel, gbc_panel);
		
//		dataExtractionMethods = new TreeSet<DataExtractionMethod>();
		dataAcquisitionMethods = new TreeSet<DataAcquisitionMethod>();

		completeStageButton.setText(
				MainActionCommands.COMPLETE_ANALYSIS_METHODS_DEFINITION_COMMAND.getName());		
		completeStageButton.setActionCommand(
				MainActionCommands.COMPLETE_ANALYSIS_METHODS_DEFINITION_COMMAND.getName());
		
		missingMethodNameToFileMap = new TreeMap<String, File>();
		loadPreferences();
	}
	
	private JPanel initPanel() {
		
		JPanel panel = new JPanel();		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gridBagLayout);
		
		JPanel panel1 = new JPanel(new BorderLayout(0, 0));
		panel1.setBorder(new CompoundBorder(
			new TitledBorder(
					new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
					"Data acquisition methods", TitledBorder.LEADING, TitledBorder.TOP, 
					new Font("Tahoma", Font.BOLD, 12), new Color(0, 0, 0)), 
			new EmptyBorder(10, 10, 10, 10)));
		acquisitionMethodToolbar = new AcquisitionMethodToolbar(this);
		panel1.add(acquisitionMethodToolbar, BorderLayout.NORTH);		
		acquisitionMethodTable = new AcquisitionMethodTable();
		panel1.add(new JScrollPane(acquisitionMethodTable), BorderLayout.CENTER);
		
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		panel.add(panel1, gbc_panel);
				
//		JPanel panel2 = new JPanel(new BorderLayout(0, 0));		
//		panel2.setBorder(new CompoundBorder(
//				new TitledBorder(
//						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
//						"Data analysis methods", TitledBorder.LEADING, TitledBorder.TOP, 
//						new Font("Tahoma", Font.BOLD, 12), new Color(0, 0, 0)), 
//				new EmptyBorder(10, 10, 10, 10)));		
//		dataAnalysisMethodToolbar = new DataAnalysisMethodToolbar(this);
//		panel2.add(dataAnalysisMethodToolbar, BorderLayout.NORTH);		
//		dataAnalysisMethodTable = new MethodTable();
//		panel2.add(new JScrollPane(dataAnalysisMethodTable), BorderLayout.CENTER);
//		
//		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
//		gbc_panel_1.fill = GridBagConstraints.BOTH;
//		gbc_panel_1.gridx = 0;
//		gbc_panel_1.gridy = 1;
//		panel.add(panel2, gbc_panel_1);
		
		return panel;
	}

	public Collection<String> validateMethodsData() {

		Collection<String>errors = new ArrayList<String>();
		if(acquisitionMethodTable.getRowCount() == 0)
			errors.add("No acquisition methods selected for the experiment.");
		
		if(!acquisitionMethodTable.getMissingMethodNames().isEmpty()) {
			errors.add("The following acquisition methods not in database:");
			for(String name : acquisitionMethodTable.getMissingMethodNames())
				errors.add(name);
			
			errors.add(" ");
		}		
//		if(dataAnalysisMethodTable.getRowCount() == 0)
//			errors.add("No data analysis methods selected for the experiment.");
		
		return errors;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
//		if(command.equals(MainActionCommands.ADD_DATA_EXTRACTION_METHOD_FROM_DATABASE_DIALOG_COMMAND.getName()))
//			showDataExtractionMethodSelectionDialog();
//			
//		if(command.equals(MainActionCommands.SELECT_DA_METHOD_COMMAND.getName()))
//			selectExistingDataExtractionMethod();
//
//		if(command.equals(MainActionCommands.ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName()))
//			showNewDataExtractionMethodEditorDialog();
//		
//		if(command.equals(MainActionCommands.ADD_DATA_EXTRACTION_METHOD_COMMAND.getName()))
//			saveDataExtractionMethod();
//				
//		if(command.equals(MainActionCommands.DELETE_DATA_EXTRACTION_METHOD_COMMAND.getName()))
//			removeSelectedDataExtractionMethod();				
		
		if(command.equals(MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName()))
			try {
				scanDirectoryForAcquisitionMethods();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		if(command.equals(MainActionCommands.SHOW_DATA_ACQUISITION_SELECTOR_COMMAND.getName()))
			showAcquisitionMethodSelector();
		
		if(command.equals(MainActionCommands.SELECT_DATA_ACQUISITION_METHOD_COMMAND.getName())) 
			addExistingAcquisitionMethod();
		
		if(command.equals(MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName()))
			showNewAcquisitionMethodEditorDialog();
		
		if(command.equals(MainActionCommands.ADD_ACQUISITION_METHOD_COMMAND.getName()))
			saveAcquisitionMethodData();
		
		if(command.equals(MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName()))
			removeSelectedAcquisitionMethod();
	}

	
	/**
	 * Data extraction methods
	 * */	
//	private void showDataExtractionMethodSelectionDialog() {
//		
//		dataAnalysisMethodSelectionDialog = new DataAnalysisMethodSelectionDialog(this);
//		dataAnalysisMethodSelectionDialog.setLocationRelativeTo(this);
//		dataAnalysisMethodSelectionDialog.setVisible(true);
//	}
//	
//	private void selectExistingDataExtractionMethod() {
//		
//		DataExtractionMethod damethod = dataAnalysisMethodSelectionDialog.getSelectedMethod();
//		if(damethod == null)
//			return;
//		
//		dataExtractionMethods.add(damethod);
//		reloadDataAnalysisMethods();
//		dataAnalysisMethodSelectionDialog.dispose();
//	}
//	
//	private void showNewDataExtractionMethodEditorDialog() {
//		
//		dataExtractionMethodEditorDialog = new DataExtractionMethodEditorDialog(null, this);
//		dataExtractionMethodEditorDialog.setLocationRelativeTo(this);
//		dataExtractionMethodEditorDialog.setVisible(true);
//	}
//		
//	private void saveDataExtractionMethod() {
//
//		Collection<String>errors = dataExtractionMethodEditorDialog.validateMethodData();
//		if(!errors.isEmpty()) {
//			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), dataExtractionMethodEditorDialog);
//			return;
//		}
//		DataExtractionMethod selectedMethod = new DataExtractionMethod(
//					null,
//					dataExtractionMethodEditorDialog.getMethodName(),
//					dataExtractionMethodEditorDialog.getMethodDescription(),
//					MRC2ToolBoxCore.getIdTrackerUser(),
//					new Date());
//		String methodId = null;
//		try {
//			methodId = IDTUtils.addNewDataExtractionMethod(
//					selectedMethod, dataExtractionMethodEditorDialog.getMethodFile());
//			selectedMethod.setId(methodId);
//			IDTDataCash.getDataExtractionMethods().add(selectedMethod);
//			dataExtractionMethods.add(selectedMethod);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//		reloadDataAnalysisMethods();
//		dataExtractionMethodEditorDialog.dispose();
//	}
//	
//	private void removeSelectedDataExtractionMethod() {
//
//		DataExtractionMethod method =
//				(DataExtractionMethod)dataAnalysisMethodTable.getSelectedAnalysisMethod();
//		if(method == null)
//			return;
//
//		dataExtractionMethods.remove(method);
//		reloadDataAnalysisMethods();
//	}
//	
//	private void reloadDataAnalysisMethods() {
//		dataAnalysisMethodTable.setTableModelFromDataAnalysisMethods(dataExtractionMethods);		
//	}
	
	/**
	 * Data acquisition methods
	 * */	
	private void showNewAcquisitionMethodEditorDialog() {
				
		acquisitionMethodEditorDialog = new AcquisitionMethodExtendedEditorDialog(null, this);
		String methodName = acquisitionMethodTable.getSelectedAnalysisMethodName();
		if(methodName != null && acquisitionMethodTable.getSelectedAnalysisMethod() == null) {
			
			File methodFile = missingMethodNameToFileMap.get(methodName);
			if(methodFile != null && methodFile.exists())
				acquisitionMethodEditorDialog.setMethodFile(methodFile);
		}		
		acquisitionMethodEditorDialog.setLocationRelativeTo(this);
		acquisitionMethodEditorDialog.setVisible(true);
	}
	
	private void scanDirectoryForAcquisitionMethods() throws Exception {

		File dirToScan = selectRawFilesDirectory();
		if(dirToScan == null)
			return;

		LIMSWorklistAcquisitiomMethodScanTask wlit =
			new LIMSWorklistAcquisitiomMethodScanTask(
					dirToScan,
					WorklistImportType.RAW_DATA_DIRECTORY_SCAN);
		wlit.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(wlit);
	}

	private File selectRawFilesDirectory() {

		JFileChooser chooser = new ImprovedFileChooser();
		File inputFile = null;

		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select folder containing raw data files:");
		chooser.setCurrentDirectory(baseDirectory);

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			inputFile = chooser.getSelectedFile();

			if (inputFile.exists()) {
				baseDirectory = inputFile.getParentFile();
				savePreferences();
			}
		}
		return inputFile;
	}	
	
	private void showAcquisitionMethodSelector(){
		
		acquisitionMethodSelectorDialog = new AcquisitionMethodSelectorDialog(this);
		acquisitionMethodSelectorDialog.setLocationRelativeTo(this);
		acquisitionMethodSelectorDialog.setVisible(true);
	}	
	
	private void addExistingAcquisitionMethod() {
		
		DataAcquisitionMethod acquisitionMethod = 
				acquisitionMethodSelectorDialog.getSelectedMethod();
		if(acquisitionMethod == null)
			return;
		
		dataAcquisitionMethods.add(acquisitionMethod);
		reloadAcquisitionMethods();
		acquisitionMethodSelectorDialog.dispose();
	}
	
	private void saveAcquisitionMethodData() {

		Collection<String>errors = acquisitionMethodEditorDialog.validateMethodData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), acquisitionMethodEditorDialog);
			return;
		}
		DockableAcquisitionMethodDataPanel methodData = acquisitionMethodEditorDialog.getDataPanel();
		DataAcquisitionMethod selectedMethod = new DataAcquisitionMethod(
					null,
					methodData.getMethodName(),
					methodData.getMethodDescription(),
					MRC2ToolBoxCore.getIdTrackerUser(),
					new Date());

		selectedMethod.setPolarity(methodData.getMethodPolarity());
		selectedMethod.setMsType(methodData.getMethodMsType());
		selectedMethod.setColumn(methodData.getColumn());
		selectedMethod.setIonizationType(methodData.getIonizationType());
		selectedMethod.setMassAnalyzerType(methodData.getMassAnalyzerType());
		selectedMethod.setSeparationType(methodData.getChromatographicSeparationType());
		try {
			AcquisitionMethodUtils.addNewAcquisitionMethod(selectedMethod, methodData.getMethodFile());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IDTDataCash.getAcquisitionMethods().add(selectedMethod);
		dataAcquisitionMethods.add(selectedMethod);
		reloadAcquisitionMethods();
		acquisitionMethodEditorDialog.dispose();
	}
	
	private void removeSelectedAcquisitionMethod() {
		
		if(acquisitionMethodTable.getSelectedRow() == -1)
			return;
		
		TreeSet<String>methodNames = new TreeSet<String>(acquisitionMethodTable.getMethodNames());
		DataAcquisitionMethod method =
				(DataAcquisitionMethod)acquisitionMethodTable.getSelectedAnalysisMethod();
		acquisitionMethodTable.removeSelectedRow();
		if(method != null) 
			dataAcquisitionMethods.remove(method);	
	}
	
	private void reloadAcquisitionMethods() {
		
		Collection<String> methodNames = dataAcquisitionMethods.stream().map(m -> m.getName()).
				collect(Collectors.toCollection(TreeSet::new));
		
		if(!missingMethodNameToFileMap.isEmpty())
			methodNames.addAll(missingMethodNameToFileMap.keySet());
			
		acquisitionMethodTable.setTableModelFromAcquisitionMethods(methodNames);
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory =
				new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultProjectsDirectory())).
				getAbsoluteFile();
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if(e.getStatus().equals(TaskStatus.FINISHED)) {
			
			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(LIMSWorklistAcquisitiomMethodScanTask.class)) {

				LIMSWorklistAcquisitiomMethodScanTask task  = (LIMSWorklistAcquisitiomMethodScanTask)e.getSource();
				TreeMap<String, File> nameToFileMap = task.getMethodNameToFileMap();			
				TreeSet<String>methodNames = new TreeSet<String>(acquisitionMethodTable.getMethodNames());
				methodNames.addAll(nameToFileMap.keySet());
				acquisitionMethodTable.setTableModelFromAcquisitionMethods(methodNames);			
				dataAcquisitionMethods.addAll(acquisitionMethodTable.getAvailableDataAcquisitionMethods());
				Collection<String>missingNames = acquisitionMethodTable.getMissingMethodNames();
				if(!missingNames.isEmpty()) {

					for(String name : missingNames) {
						
						if(nameToFileMap.containsKey(name))
							missingMethodNameToFileMap.put(name, nameToFileMap.get(name));
					}				
				}
			}
		}
	}
	
//	public Collection<DataExtractionMethod> getDataExtractionMethods() {
//		return dataAnalysisMethodTable.getAvailableDataExtractionMethods();
//	}

	public Collection<DataAcquisitionMethod> getDataAcquisitionMethods() {
		return acquisitionMethodTable.getAvailableDataAcquisitionMethods();
	}
}
















