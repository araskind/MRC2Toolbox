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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.ResultsFile;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.data.TargetedDataMatrixImportSettingsObject;
import edu.umich.med.mrc2.datoolbox.data.compare.SampleDataResultObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataTypeForImport;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureAlignmentType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.DataPipelineEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.DataPipelineEventListener;
import edu.umich.med.mrc2.datoolbox.gui.expdesign.editor.ReferenceSampleDialog;
import edu.umich.med.mrc2.datoolbox.gui.expsetup.dpl.DataPipelineDefinitionPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.AcquisitionMethodExtendedEditorDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.DockableAcquisitionMethodDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dextr.DataExtractionMethodEditorDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.matcher.DataFileSampleMatchPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.MultiCefDataAddTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.MultiCefImportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ProFinderResultsImportTaskTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.TargetedDataMatrixImportTask;
import edu.umich.med.mrc2.datoolbox.utils.DataImportUtils;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.mslib.CompoundNameMatchingTask;

public class MultiFileDataImportDialog extends JDialog
	implements ActionListener, ItemListener, DataPipelineEventListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -1605053468969470610L;

	private static final Icon importMultifileIcon = GuiUtils.getIcon("importMultifile", 32);
	private static final Icon openIcon = GuiUtils.getIcon("open", 16);
	//detailedCsv
	private Preferences preferences;
	public static final String PREFS_NODE = MultiFileDataImportDialog.class.getName();
	public static final String BASE_LIBRARY_DIRECTORY = "BASE_LIBRARY_DIRECTORY";
	public static final String BASE_DATA_FILES_DIRECTORY = "BASE_DATA_FILES_DIRECTORY";
	public static final String BASE_DESIGN_DIRECTORY = "BASE_DESIGN_DIRECTORY";
	public static final String REMOVE_ABNORMAL_ISO_PATTERNS = "REMOVE_ABNORMAL_ISO_PATTERNS";

	private DataPipeline existingDataPipeline = null;
	private DataPipeline newDataPipeline = null;
	
	private File baseLibraryDirectory, 
				libraryFile, 
				detailedProFinderFile,
				dataFileDirectory, 
				baseDesignDirectory,
				pfaTempDir;

	private MultiFileImportToolbar toolBar;

	private JLabel libraryTextFieldLabel;
	private JTextField libraryTextField;
	private JButton selectCefLibraryButton;
	private JButton selectPFdetailedCsvButton;
	private JTextField detailedProFinderFileTextField;
	private JCheckBox removeAbnormalIsoPatternsCheckBox;
	private JCheckBox skipCompoundMatchingCheckbox;
	
	private DataFileSampleMatchPanel matchPanel;
	private TaskListener dataLoadTaskListener;
	private DataPipelineDefinitionPanel dataPipelineDefinitionPanel;
	private ReferenceSampleDialog rsd;	
	private DataAcquisitionMethod activeDataAcquisitionMethod;
	private Set<DataFile>dataFilesForMethod;
	private boolean pfaLoaded = false;
	private DataAnalysisProject currentProject;
	private AcquisitionMethodExtendedEditorDialog acquisitionMethodEditorDialog;
	private DataExtractionMethodEditorDialog dataExtractionMethodEditorDialog;
	private AdductSelectionDialog adductSelectionDialog;
	private NormalizedTargetedDataSelectionDialog normalizedTargetedDataSelectionDialog;

	//	private JTextField dataFileTextField;
	private Collection<Adduct> selectedAdducts;
	private String[]compoundNames;
	private int linesToSkipAfterHeader;
	private String featureColumn;
	private CompoundLibrary referenceLibrary;
	private Map<String,LibraryMsFeature>nameFeatureMap;
	
	public MultiFileDataImportDialog(TaskListener dataLoadTaskListener) {

		super();
		setTitle("Import data from multiple files");
		setIconImage(((ImageIcon) importMultifileIcon).getImage());
		setPreferredSize(new Dimension(1200, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(1200, 800));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.dataLoadTaskListener = dataLoadTaskListener;
		existingDataPipeline = null;

		JPanel main = new JPanel(new BorderLayout(0, 0));
		main.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(main, BorderLayout.CENTER);

		toolBar = new MultiFileImportToolbar(this);
		main.add(toolBar, BorderLayout.NORTH);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		main.add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{81, 290, 65, 89, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 23, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JPanel panel_2 = new JPanel();
		main.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		matchPanel = new DataFileSampleMatchPanel();
		panel_2.add(matchPanel, BorderLayout.CENTER);

		JPanel libChooserPanel = new JPanel();
		libChooserPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_libChooserPanel = new GridBagLayout();
		gbl_libChooserPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_libChooserPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_libChooserPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_libChooserPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		libChooserPanel.setLayout(gbl_libChooserPanel);

		libraryTextFieldLabel = new JLabel("Library: ");
		GridBagConstraints gbc_libFileLabel = new GridBagConstraints();
		gbc_libFileLabel.anchor = GridBagConstraints.EAST;
		gbc_libFileLabel.insets = new Insets(0, 0, 5, 5);
		gbc_libFileLabel.gridx = 0;
		gbc_libFileLabel.gridy = 0;
		libChooserPanel.add(libraryTextFieldLabel, gbc_libFileLabel);

		libraryTextField = new JTextField();
		libraryTextField.setEditable(false);
		GridBagConstraints gbc_libraryTextField = new GridBagConstraints();
		gbc_libraryTextField.insets = new Insets(0, 0, 5, 5);
		gbc_libraryTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_libraryTextField.gridx = 1;
		gbc_libraryTextField.gridy = 0;
		libChooserPanel.add(libraryTextField, gbc_libraryTextField);
		libraryTextField.setColumns(10);

		panel_2.add(libChooserPanel, BorderLayout.NORTH);
		
		selectCefLibraryButton = new JButton(openIcon);
		selectCefLibraryButton.setActionCommand(
				MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName());
		selectCefLibraryButton.addActionListener(this);	
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.BOTH;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		libChooserPanel.add(selectCefLibraryButton, gbc_btnNewButton);
		
		JLabel lblNewLabel = new JLabel("Detailed data: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		libChooserPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		detailedProFinderFileTextField = new JTextField();
		detailedProFinderFileTextField.setEditable(false);
		GridBagConstraints gbc_detailedDataFileTextField = new GridBagConstraints();
		gbc_detailedDataFileTextField.insets = new Insets(0, 0, 0, 5);
		gbc_detailedDataFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_detailedDataFileTextField.gridx = 1;
		gbc_detailedDataFileTextField.gridy = 1;
		libChooserPanel.add(detailedProFinderFileTextField, gbc_detailedDataFileTextField);
		detailedProFinderFileTextField.setColumns(10);
		
		selectPFdetailedCsvButton = new JButton(openIcon);
		selectPFdetailedCsvButton.setActionCommand(
				MainActionCommands.SELECT_PROFINDER_DETAILED_CSV_COMMAND.getName());
		selectPFdetailedCsvButton.addActionListener(this);		
		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
		gbc_btnNewButton_2.fill = GridBagConstraints.BOTH;
		gbc_btnNewButton_2.gridx = 2;
		gbc_btnNewButton_2.gridy = 1;
		libChooserPanel.add(selectPFdetailedCsvButton, gbc_btnNewButton_2);
		
		/*
		JLabel dataFileLabel = new JLabel("Data file: ");
		GridBagConstraints gbc_dataFileLabel = new GridBagConstraints();
		gbc_dataFileLabel.anchor = GridBagConstraints.EAST;
		gbc_dataFileLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dataFileLabel.gridx = 0;
		gbc_dataFileLabel.gridy = 2;
		libChooserPanel.add(dataFileLabel, gbc_dataFileLabel);
		
		dataFileTextField = new JTextField();
		dataFileTextField.setEditable(false);
		GridBagConstraints gbc_dataFileTextField = new GridBagConstraints();
		gbc_dataFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_dataFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_dataFileTextField.gridx = 1;
		gbc_dataFileTextField.gridy = 2;
		libChooserPanel.add(dataFileTextField, gbc_dataFileTextField);
		dataFileTextField.setColumns(10);
		
		JButton btnNewButton_1 = new JButton(openIcon);
		btnNewButton_1.setActionCommand(
				MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName());
		btnNewButton_1.addActionListener(this);	
		
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_1.gridx = 2;
		gbc_btnNewButton_1.gridy = 2;
		libChooserPanel.add(btnNewButton_1, gbc_btnNewButton_1);
		*/
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};		
		dataPipelineDefinitionPanel = 
				new DataPipelineDefinitionPanel();
		dataPipelineDefinitionPanel.setBorder(
				new CompoundBorder(new EmptyBorder(10, 10, 0, 10), 
						new TitledBorder(UIManager.getBorder("TitledBorder.border"), 
								"Define data pipeline", TitledBorder.LEADING, 
								TitledBorder.TOP, null, new Color(0, 0, 0))));
		
		dataPipelineDefinitionPanel.addListener(this);
		
		removeAbnormalIsoPatternsCheckBox = 
				new JCheckBox("Remove features with abnormal isotopic patterns");
		GridBagConstraints gbc_removeAbnormalIsoPatternsCheckBox = new GridBagConstraints();
		gbc_removeAbnormalIsoPatternsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_removeAbnormalIsoPatternsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_removeAbnormalIsoPatternsCheckBox.gridx = 1;
		gbc_removeAbnormalIsoPatternsCheckBox.gridy = 1;
		panel_1.add(removeAbnormalIsoPatternsCheckBox, gbc_removeAbnormalIsoPatternsCheckBox);
		
		skipCompoundMatchingCheckbox = new JCheckBox("Skip matching compounds  to database");
		GridBagConstraints gbc_skipCompoundMatchingCheckbox = new GridBagConstraints();
		gbc_skipCompoundMatchingCheckbox.gridwidth = 2;
		gbc_skipCompoundMatchingCheckbox.anchor = GridBagConstraints.WEST;
		gbc_skipCompoundMatchingCheckbox.insets = new Insets(0, 0, 5, 0);
		gbc_skipCompoundMatchingCheckbox.gridx = 2;
		gbc_skipCompoundMatchingCheckbox.gridy = 1;
		panel_1.add(skipCompoundMatchingCheckbox, gbc_skipCompoundMatchingCheckbox);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 4;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		panel_1.add(dataPipelineDefinitionPanel, gbc_panel);
		JButton cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 2;
		gbc_cancelButton.gridy = 3;
		panel_1.add(cancelButton, gbc_cancelButton);
		cancelButton.addActionListener(al);

		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		JButton importDataButton = new JButton("Import data");
		importDataButton.setActionCommand(MainActionCommands.IMPORT_DATA_COMMAND.getName());
		importDataButton.addActionListener(this);
		GridBagConstraints gbc_importDataButton = new GridBagConstraints();
		gbc_importDataButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_importDataButton.gridx = 3;
		gbc_importDataButton.gridy = 3;
		panel_1.add(importDataButton, gbc_importDataButton);
		JRootPane rootPane = SwingUtilities.getRootPane(importDataButton);
		rootPane.setDefaultButton(importDataButton);		
		
		currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		loadPreferences();
		updateInterfaceForImportType(toolBar.getDataTypeForImport());
		pack();
	}
	
	@Override
	public void dispose() {
		savePreferences();		
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
				
		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName()))
			selectLibraryFile();
		
		if (command.equals(MainActionCommands.SHOW_ADDUCT_SELECTOR.getName()))
			showAdductSelector();

		if (command.equals(MainActionCommands.SELECT_ADDUCTS.getName()))
			selectAdducts();

		if (command.equals(MainActionCommands.ADD_DATA_FILES_COMMAND.getName()))
			selectCEFDataFiles();
		
		if (command.equals(MainActionCommands.LOAD_DATA_FROM_PROFINDER_PFA_COMMAND.getName()))
			selectPfaFile();
		
		if (command.equals(MainActionCommands.SELECT_PROFINDER_SIMPLE_CSV_COMMAND.getName()))
			selectProFinderCsvFile(false);
		
		if (command.equals(MainActionCommands.SELECT_PROFINDER_DETAILED_CSV_COMMAND.getName()))
			selectProFinderCsvFile(true);
		
		if (command.equals(MainActionCommands.SELECT_NORMALIZED_TARGETED_DATA_COMMAND.getName()))
			selectNormalizedTargetedDataFile();
		
		if (command.equals(MainActionCommands.PARSE_NORMALIZED_TARGETED_DATA_COMMAND.getName()))
			parseNormalizedTargetedDataFile();

		if (command.equals(MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName()))
			removeDataFiles();

		if (event.getActionCommand().equals(MainActionCommands.IMPORT_DATA_COMMAND.getName()))
			importData();

		if (command.equals(MainActionCommands.CLEAR_DATA_COMMAND.getName()))
			clearInputData();
		
		if(command.equals(MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName()))
			showReferenceSamplesEditDialog();
		
		if(command.equals(MainActionCommands.EDIT_REFERENCE_SAMPLES_COMMAND.getName()))
			editReferenceSamples();
		
		if (command.equals(MainActionCommands.LOAD_DATA_FILE_SAMPLE_MAP_COMMAND.getName()))
			selectDesignFile();
		
		if(command.equals(MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName()))
			showAcquisitionMethodEditor();
		
		if(command.equals(MainActionCommands.ADD_ACQUISITION_METHOD_COMMAND.getName()))
			addAcquisitionMethod();
		
		if(command.equals(MainActionCommands.ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName()))
			showDataExtractionMethodEditor();
		
		if(command.equals(MainActionCommands.ADD_DATA_EXTRACTION_METHOD_COMMAND.getName()))
			addDataExtractionMethod();
	}
	
	private void clearInputData() {
		
		if (MessageDialog.showChoiceMsg("Clear input data?", this) == JOptionPane.YES_OPTION)
			clearPanel();
	}

	private void showAdductSelector() {
	
		adductSelectionDialog = new AdductSelectionDialog(this);
		adductSelectionDialog.setLocationRelativeTo(this);
		adductSelectionDialog.setVisible(true);
	}

	private void selectAdducts() {
		
		selectedAdducts = adductSelectionDialog.getSelectedAdducts();
		adductSelectionDialog.dispose();
	}
	
	private void showDataExtractionMethodEditor() {
		
		dataExtractionMethodEditorDialog = new DataExtractionMethodEditorDialog(null, this);
		dataExtractionMethodEditorDialog.setLocationRelativeTo(this.getContentPane());
		dataExtractionMethodEditorDialog.setVisible(true);
	}
	
	private void addDataExtractionMethod() {

		Collection<String>errors = 
				dataExtractionMethodEditorDialog.validateMethodData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), dataExtractionMethodEditorDialog);
			return;
		}
		DataExtractionMethod selectedMethod = new DataExtractionMethod(
					null,
					dataExtractionMethodEditorDialog.getMethodName(),
					dataExtractionMethodEditorDialog.getMethodDescription(),
					MRC2ToolBoxCore.getIdTrackerUser(),
					new Date());
		selectedMethod.setSoftware(dataExtractionMethodEditorDialog.getSoftware());
		try {
			IDTUtils.addNewDataExtractionMethod(
					selectedMethod, dataExtractionMethodEditorDialog.getMethodFile());
			IDTDataCache.getDataExtractionMethods().add(selectedMethod);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataExtractionMethodEditorDialog.dispose();
	}
	
	private void showAcquisitionMethodEditor() {

		acquisitionMethodEditorDialog = new AcquisitionMethodExtendedEditorDialog(null, this);
		acquisitionMethodEditorDialog.setLocationRelativeTo(this.getContentPane());
		acquisitionMethodEditorDialog.setVisible(true);
	}
	
	private void addAcquisitionMethod() {

		Collection<String>errors = 
				acquisitionMethodEditorDialog.validateMethodData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), acquisitionMethodEditorDialog);
			return;
		}
		DockableAcquisitionMethodDataPanel methodData = 
				acquisitionMethodEditorDialog.getDataPanel();
		DataAcquisitionMethod newMethod = new DataAcquisitionMethod(
					null,
					methodData.getMethodName(),
					methodData.getMethodDescription(),
					MRC2ToolBoxCore.getIdTrackerUser(),
					new Date());

		newMethod.setPolarity(methodData.getMethodPolarity());
		newMethod.setMsType(methodData.getMethodMsType());
		newMethod.setColumn(methodData.getColumn());
		newMethod.setIonizationType(methodData.getIonizationType());
		newMethod.setMassAnalyzerType(methodData.getMassAnalyzerType());
		newMethod.setSeparationType(methodData.getChromatographicSeparationType());
		newMethod.setSoftware(methodData.getSoftware());
		try {
			AcquisitionMethodUtils.addNewAcquisitionMethod(
					newMethod, methodData.getMethodFile());
			IDTDataCache.getAcquisitionMethods().add(newMethod);		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		acquisitionMethodEditorDialog.dispose();
	}
	
	private void editReferenceSamples() {
		
		rsd.editReferenceSamples();
		matchPanel.refreshSampleEditor();
		rsd.dispose();
	}
	
	private void showReferenceSamplesEditDialog() {

		if(rsd == null)
			rsd = new ReferenceSampleDialog(this, MRC2ToolBoxCore.getActiveMetabolomicsExperiment());

		rsd.setLocationRelativeTo(this.getContentPane());
		rsd.setVisible(true);
	}

	private void addResultsToNewPipeline(DataPipeline pipeline, File[] resultFiles) {

		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Set<SampleDataResultObject>sampleDataResultObjects = new TreeSet<>(
				new SampleDataResultObjectComparator(SortProperty.resultFile));
		
		//	Get already added data
		Set<SampleDataResultObject> addedData = matchPanel.getSampleDataResultObjects(false);
		sampleDataResultObjects.addAll(addedData);
		
		DataAcquisitionMethod acqMethod = pipeline.getAcquisitionMethod();
		DataExtractionMethod daMethod = pipeline.getDataExtractionMethod();
		Set<DataFile> existingDataFiles = project.getDataFilesForAcquisitionMethod(acqMethod);
		
		for (File f : resultFiles) {

			SampleDataResultObject existingObject = null;
			String fileBaseName = FilenameUtils.getBaseName(f.getName());
			DataFile inProject = null;
			if(existingDataFiles != null && !existingDataFiles.isEmpty()) {
				
				final DataFile existingDataFile = existingDataFiles.stream().
					filter(df -> df.getBaseName().equals(fileBaseName)).
					findFirst().orElse(null);
				
				if(existingDataFile != null) {
					
					inProject = existingDataFile;
					existingObject = addedData.stream().
							filter(s -> s.getDataFile().equals(existingDataFile)).
							findFirst().orElse(null);
				}
			}
			if(existingObject == null) {
				
				existingObject = addedData.stream().
						filter(s -> s.getDataFile().getName().equals(fileBaseName)).
						findFirst().orElse(null);
			}
			if(existingObject == null) {
				
				if(inProject != null) {
					
					ResultsFile resultFile = new ResultsFile(fileBaseName, daMethod, new Date(), inProject);
					resultFile.setFullPath(f.getAbsolutePath());
					SampleDataResultObject sdro = 
							new SampleDataResultObject(
									inProject.getParentSample(), inProject, resultFile);
					sampleDataResultObjects.add(sdro);
				}
				else {	
					DataFile df = new DataFile(fileBaseName, acqMethod);
					df.setFullPath(f.getAbsolutePath());
					ResultsFile resultFile = new ResultsFile(fileBaseName, daMethod, new Date(), df);
					resultFile.setFullPath(f.getAbsolutePath());				
					ExperimentalSample matchedSample = 
							DataImportUtils.getSampleFromFileName(fileBaseName, project);
					SampleDataResultObject sdro = 
							new SampleDataResultObject(matchedSample, df, resultFile);
					sampleDataResultObjects.add(sdro);
				}
			}
		}
		matchPanel.loadSampleDataResultObject(sampleDataResultObjects);
	}

	private void addResultsToExistingPipeline(File[] dataFiles) {
		
		Set<SampleDataResultObject>sampleDataResultObjects = new TreeSet<SampleDataResultObject>(
				new SampleDataResultObjectComparator(SortProperty.resultFile));
		
		//	Get already added data
		Set<SampleDataResultObject> addedData = matchPanel.getSampleDataResultObjects(false);
		sampleDataResultObjects.addAll(addedData);
		
		DataAcquisitionMethod acqMethod = existingDataPipeline.getAcquisitionMethod();
		DataExtractionMethod daMethod = existingDataPipeline.getDataExtractionMethod();
		
		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Set<DataFile> existingDataFiles = project.getDataFilesForAcquisitionMethod(acqMethod);
		
		for (File f : dataFiles) {

			String fileBaseName = FilenameUtils.getBaseName(f.getName());			
			SampleDataResultObject existingObject = addedData.stream().
					filter(s -> s.getDataFile().getName().equals(fileBaseName)).
					findFirst().orElse(null);
			if(existingObject == null) {
				
				//	Check if there is existing data file
				DataFile df = existingDataFiles.stream().
						filter(d -> d.getName().equals(fileBaseName)).
						findFirst().orElse(null);
				if(df == null) {
					df = new DataFile(fileBaseName, acqMethod);
					df.setFullPath(f.getAbsolutePath());
				}
				else {	//	Check if data for this data analysis method already atached to this data file 
					if(df.getResultForDataExtractionMethod(daMethod) != null)
						continue;
				}				
				ResultsFile resultFile = new ResultsFile(fileBaseName, daMethod, new Date(), df);
				resultFile.setFullPath(f.getAbsolutePath());				
				ExperimentalSample matchedSample = 
						DataImportUtils.getSampleFromFileName(fileBaseName, project);
				SampleDataResultObject sdro = 
						new SampleDataResultObject(matchedSample, df, resultFile);
				sampleDataResultObjects.add(sdro);
			}
		}	
		matchPanel.loadSampleDataResultObject(sampleDataResultObjects);
	}
	
	class ProFinderArchiveExtractionTask extends LongUpdateTask {

	    private final File proFinderArchiveFile;
	    private final DataPipeline pipeline;

		public ProFinderArchiveExtractionTask(File proFinderArchiveFile, DataPipeline pipeline) {
	        this.proFinderArchiveFile = proFinderArchiveFile;
	        this.pipeline = pipeline;
	    }

		@Override
		public Void doInBackground() {
			
			//	Unzip PFA file to the temporary directory
			pfaTempDir = null;
			Path pfaTempDirPath = null;
			try {
				pfaTempDirPath = Files.createDirectories(
						Paths.get(proFinderArchiveFile.getParentFile().getAbsolutePath(), 
						FilenameUtils.getBaseName(proFinderArchiveFile.getName() + 
						"_" + FIOUtils.getTimestamp())));			
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if(pfaTempDirPath == null) {
				MessageDialog.showErrorMsg("Unable to create temporary directory.");
				return null;
			}	
			pfaTempDir = pfaTempDirPath.toFile();
			dataFileDirectory = proFinderArchiveFile.getParentFile();
			savePreferences();
			
			Collection<File> cefFiles = null;
			try {
				cefFiles = extractFile(proFinderArchiveFile, pfaTempDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(cefFiles != null && !cefFiles.isEmpty()) {
				
				File[] resultFiles = cefFiles.toArray(new File[cefFiles.size()]);
				addResultsToNewPipeline(pipeline, resultFiles);
				pfaLoaded = true;
			}
			else {
				MessageDialog.showErrorMsg("Unable to extract data from " + proFinderArchiveFile.getName(), 
						MultiFileDataImportDialog.this);
				try {
					FileUtils.deleteDirectory(pfaTempDir);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}	
			return null;
		}
			
	    private Collection<File> extractFile(File zipFile, File destinationDir) throws Exception {
	    	
	    	Collection<File>cefFiles = new TreeSet<>();
	        byte[] buffer = new byte[4096];
	        ZipFile zf = new ZipFile(zipFile);
	        Enumeration<ZipArchiveEntry> entries = zf.getEntries();
	        while(entries.hasMoreElements()) {
	        	
	            ZipArchiveEntry ze = entries.nextElement();
	            String zefilename = ze.getName();
	            if(zefilename.toLowerCase().endsWith(".cef")) {
	            	
	                System.out.println("Extracting file: " + zefilename);
	                File extfile = Paths.get(destinationDir.getAbsolutePath(), zefilename).toFile();
	                InputStream zis = zf.getInputStream(ze);
	                FileOutputStream fos = new FileOutputStream(extfile);
	                try {
	                    int numBytes;
	                    while ((numBytes = zis.read(buffer, 0, buffer.length)) != -1)
	                        fos.write(buffer, 0, numBytes);
	                }
	                finally {
	                    fos.close();
	                    zis.close();
	                    cefFiles.add(extfile);
	                }
	            }
	        }
	        zf.close();
	        return cefFiles;
	    }
	}

	public synchronized void clearPanel() {

		libraryFile = null;
		matchPanel.clearTable();
		libraryTextField.setText("");
	}
	
	public boolean removeAbnormalIsoPatterns() {	
		return removeAbnormalIsoPatternsCheckBox.isSelected();
	}

	private void importData() {
		
		if((existingDataPipeline == null && newDataPipeline == null) 
				|| matchPanel.getSampleDataResultObjects(true).isEmpty())
			return;
		
		Collection<String>errors = validateInputData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"));
			return;		
		}
		DataPipeline importPipeline = existingDataPipeline;	
		if(importPipeline == null)
			importPipeline = newDataPipeline;

		//	Assign data and result files
		Set<SampleDataResultObject> dataToImport = 
				matchPanel.getSampleDataResultObjects(true);
		for(SampleDataResultObject o : dataToImport) {
			
			o.getDataFile().addResultFile(o.getResultFile());
			if(!o.getSample().hasDataFile(o.getDataFile()))
				o.getSample().addDataFile(o.getDataFile());
		}
		if(existingDataPipeline != null 
				&& DataTypeForImport.AGILENT_UNTARGETED.equals(toolBar.getDataTypeForImport())) {

			MultiCefDataAddTask task = new MultiCefDataAddTask(
					dataToImport, 
					importPipeline, 
					FeatureAlignmentType.ALIGN_TO_LIBRARY);
			task.addTaskListener(dataLoadTaskListener);
			MRC2ToolBoxCore.getTaskController().addTask(task);
			dispose();
			return;
		}
		if(newDataPipeline != null) {	
			
			DataTypeForImport dataTypeForImport = toolBar.getDataTypeForImport();
			if(dataTypeForImport.equals(DataTypeForImport.AGILENT_UNTARGETED))
				initiateUntargetedDataImport(dataToImport, importPipeline);
			
			if(dataTypeForImport.equals(DataTypeForImport.AGILENT_PROFINDER_TARGETED))
				initiateProFinderDataImport(dataToImport, importPipeline);
			
			if(dataTypeForImport.equals(DataTypeForImport.GENERIC_TARGETED))
				initiateGenericTargetedDataImport(dataToImport, importPipeline);
			
			dispose();
		}		
	}

	private void initiateProFinderDataImport(
			Set<SampleDataResultObject> dataToImport, 
			DataPipeline importPipeline) {
				
		ProFinderResultsImportTaskTask task = new ProFinderResultsImportTaskTask(
					importPipeline,
					pfaTempDir,
					dataToImport, 			
					libraryFile, 
					detailedProFinderFile,
					selectedAdducts);
		task.addTaskListener(dataLoadTaskListener);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
	
	private void initiateUntargetedDataImport(
			Set<SampleDataResultObject> dataToImport, 
			DataPipeline importPipeline) {
		
		MultiCefImportTask task = new MultiCefImportTask(
				libraryFile, 
				dataToImport, 
				importPipeline, 
				FeatureAlignmentType.ALIGN_TO_LIBRARY,
				pfaTempDir,
				skipCompoundMatchingCheckbox.isSelected());
		task.setRemoveAbnormalIsoPatterns(removeAbnormalIsoPatterns());
		task.addTaskListener(dataLoadTaskListener);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
		
	private void initiateGenericTargetedDataImport(
			Set<SampleDataResultObject> dataToImport,
			DataPipeline importPipeline) {
		
		TargetedDataMatrixImportSettingsObject importSettings = 
				new TargetedDataMatrixImportSettingsObject(
						dataToImport,
						importPipeline,
						libraryFile, 
						linesToSkipAfterHeader,
						featureColumn,
						referenceLibrary, 
						nameFeatureMap);

		TargetedDataMatrixImportTask task = new TargetedDataMatrixImportTask(importSettings);
		task.addTaskListener(dataLoadTaskListener);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
	}
	
	private Collection<String>validateInputData(){
		
		Collection<String>errors = new ArrayList<>();
		if(newDataPipeline != null)
			errors.addAll(dataPipelineDefinitionPanel.validatePipelineDefinition());
		
		checkForUnmatchedSamples(errors);
		
		DataTypeForImport dataTypeForImport = toolBar.getDataTypeForImport();
		
		if(dataTypeForImport.equals(DataTypeForImport.AGILENT_UNTARGETED))
			validateInputDataForAgilentUntargetedImport(errors);

		if(dataTypeForImport.equals(DataTypeForImport.AGILENT_PROFINDER_TARGETED))
			validateInputDataForAgilentProFinderImport(errors);
		
		if(dataTypeForImport.equals(DataTypeForImport.GENERIC_TARGETED))
			validateInputDataForNormalizedTargetedImport(errors);
		
		return errors;
	}

	private void validateInputDataForAgilentProFinderImport(Collection<String>errors) {
		
		if(newDataPipeline != null) {			
			
			if(libraryFile == null || !libraryFile.exists() || !libraryFile.canRead())
				errors.add("ProFinder simple export file not specified or not readable");
			
			if(detailedProFinderFile == null || !detailedProFinderFile.exists() 
					|| !detailedProFinderFile.canRead())
				errors.add("ProFinder detailed export file not specified or not readable");
			

		}
		if(selectedAdducts == null || selectedAdducts.isEmpty())
			errors.add("Adduct list for the library construction is not selected");
	}

	private void validateInputDataForAgilentUntargetedImport(Collection<String>errors) {
		
		if(newDataPipeline != null)	{		
			if(libraryFile == null || !libraryFile.exists() || !libraryFile.canRead())
				errors.add("CEF Library file not specified or not readable");
		}
	}
	
	private void validateInputDataForNormalizedTargetedImport(Collection<String> errors) {
		
		nameFeatureMap = new TreeMap<>();
		CompoundNameMatchingTask task = new CompoundNameMatchingTask(
				compoundNames, 
				referenceLibrary,
				errors, 
				nameFeatureMap, 
				true,
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExportsDirectory());
		IndeterminateProgressDialog idp = 
				new IndeterminateProgressDialog(
						"Fetching reference library and matching compound names ...", this, task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	private void checkForUnmatchedSamples(Collection<String>errors){
		
		Set<SampleDataResultObject> objectsToImport = 			
				matchPanel.getSampleDataResultObjects(true);
		List<String> unmatched = 
				objectsToImport.stream().
				filter(o -> Objects.isNull(o.getSample())).
				map(o -> o.getDataFile().getName()).
				collect(Collectors.toList());

		if(!unmatched.isEmpty())
			errors.add("Not all files matched to samples:\n" +
					StringUtils.join(unmatched, "\n"));
	}

	private void removeDataFiles() {	
		matchPanel.removeSelected();
	}

	private void selectCEFDataFiles() {

		if(pfaLoaded && !matchPanel.getSampleDataResultObjects(false).isEmpty()) {
			
			MessageDialog.showErrorMsg("You've selected ProFinder archive as data source.\n"
					+ "Adding individual CEF files to this data analysis pipeline is not supported.", 
					this);
			return;
		}		
		JnaFileChooser fc = new JnaFileChooser(dataFileDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.setTitle("Select CEF files for the samples");
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(this)) {
			
			File[] dataFiles = fc.getSelectedFiles();
			if(dataFiles.length == 0)
				return;			
		
			dataFileDirectory = dataFiles[0].getParentFile();
			savePreferences();
			
			if(existingDataPipeline != null && newDataPipeline == null) {	//	Add data to existing pipeline
				addResultsToExistingPipeline(dataFiles);
			}			
			else if(newDataPipeline != null && existingDataPipeline == null) {	//	Add extra data to new pipeline
				addResultsToNewPipeline(newDataPipeline, dataFiles);
			}
			else {	//	Create new pipeline and add data to it		
				if(!getNewDataPipeline())
					return;
				else
					addResultsToNewPipeline(newDataPipeline, dataFiles);					
			}				
		}
	}
	
	private boolean getNewDataPipeline() {
		
		newDataPipeline = dataPipelineDefinitionPanel.getDataPipeline();
		if(newDataPipeline == null)
			return false;
		
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getDataPipelines().contains(newDataPipeline)
				|| MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getDataPipelineByName(newDataPipeline.getName()) != null) {
			MessageDialog.showErrorMsg("The experiment already contains data pipeline with the same name \n"
					+ "and/or with selected combination of assay, data acquisition and data analysis methods.\n"
					+ "Please adjust you selection.\n"
					+ "If you want to replace the existing data\n"
					+ "please delete them first and then re-upload.", this);
			
			return false;
		}
		else 
			return true;
	}
	
	private void selectPfaFile() {
		
		if(existingDataPipeline != null) {
			MessageDialog.showWarningMsg("Adding results from ProFinder archive\n"
					+ "to existing data analysis pipeline i not supported\n"
					+ "Please create new data pipeline to import the data from ProFinder.", this);
			return;
		}		
		if(!getNewDataPipeline())
			return;
		
		JnaFileChooser fc = new JnaFileChooser(dataFileDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("ProFinder archive files", "pfa", "PFA");
		fc.setTitle("Select ProFinder archive file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			File pfaFile = fc.getSelectedFile();
			dataFileDirectory = pfaFile.getParentFile();
			savePreferences();	

			ProFinderArchiveExtractionTask task = 
					new ProFinderArchiveExtractionTask(pfaFile, newDataPipeline);
			IndeterminateProgressDialog idp = 
					new IndeterminateProgressDialog("Loading document preview ...", this, task);
			idp.setLocationRelativeTo(this.getContentPane());
			idp.setVisible(true);
		}
	}

	private void selectLibraryFile() {
		
		if(existingDataPipeline != null)
			return;
		
		JnaFileChooser fc = new JnaFileChooser(baseLibraryDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.setTitle("Select library CEF file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			libraryFile = fc.getSelectedFile();
			libraryTextField.setText(libraryFile.getPath());
			baseLibraryDirectory = libraryFile.getParentFile();
			savePreferences();	
		}
	}
	
	private void selectProFinderCsvFile(boolean detailed) {
		
		if(existingDataPipeline != null) {
			MessageDialog.showWarningMsg("Adding results from ProFinder \n"
					+ "to existing data analysis pipeline i not supported\n"
					+ "Please create new data pipeline to import the data from ProFinder.", this);
			return;
		}
		if(!getNewDataPipeline())
			return;
		
		JnaFileChooser fc = new JnaFileChooser(baseLibraryDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CSV files", "csv", "CSV");
		if(detailed)
			fc.setTitle("Select detailed ProFinder CSV export file");
		else
			fc.setTitle("Select simple ProFinder CSV export file");
		
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			if(detailed) {
				detailedProFinderFile = fc.getSelectedFile();
				detailedProFinderFileTextField.setText(detailedProFinderFile.getPath());
				baseLibraryDirectory = detailedProFinderFile.getParentFile();
			}
			else {
				libraryFile = fc.getSelectedFile();
				libraryTextField.setText(libraryFile.getPath());
				baseLibraryDirectory = libraryFile.getParentFile();
			}
			savePreferences();	
		}
	}
	
	private void selectNormalizedTargetedDataFile() {
		
		if(existingDataPipeline != null) {
			MessageDialog.showWarningMsg("Adding normalized targeted results \n"
					+ "to existing data analysis pipeline i not supported\n"
					+ "Please create new data pipeline to import the data from ProFinder.", this);
			return;
		}
		if(!getNewDataPipeline())
			return;
				
		normalizedTargetedDataSelectionDialog = 
				new NormalizedTargetedDataSelectionDialog(this, baseLibraryDirectory, false);
		normalizedTargetedDataSelectionDialog.setLocationRelativeTo(this);
		normalizedTargetedDataSelectionDialog.setVisible(true);
	}
	
	private void parseNormalizedTargetedDataFile() {

		Collection<String>errors = normalizedTargetedDataSelectionDialog.validateFormData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(
		            StringUtils.join(errors, "\n"), normalizedTargetedDataSelectionDialog);
		    return;
		}
		libraryFile = normalizedTargetedDataSelectionDialog.getInputFile();
		libraryTextField.setText(libraryFile.getAbsolutePath());
		baseLibraryDirectory = libraryFile.getParentFile();
		featureColumn = normalizedTargetedDataSelectionDialog.getFeatureColumnName();
		referenceLibrary = normalizedTargetedDataSelectionDialog.getReferenceLibrary();

		extractDataFilesFromNormalizedTargetedData(libraryFile, 
				featureColumn,
				normalizedTargetedDataSelectionDialog.getFileNameMask(), 
				normalizedTargetedDataSelectionDialog.getNumberOfLinesToSkipAfterHeader());
		
		savePreferences();
		normalizedTargetedDataSelectionDialog.savePreferences();
		normalizedTargetedDataSelectionDialog.dispose();
	}
	
	private void extractDataFilesFromNormalizedTargetedData(
			File inputFile, 
			String featureColumn,
			String fileNameMask, 
			int linesToSkipAfterHeader) {
		
		this.linesToSkipAfterHeader = linesToSkipAfterHeader;
		String[][] inputDataArray = 
				DelimitedTextParser.parseTextFile(
						inputFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		Pattern fileNamePattern = Pattern.compile(fileNameMask);

		Set<File>sampleFiles = new TreeSet<>();
		for(String column : inputDataArray[0]) {
			
			if(fileNamePattern.matcher(column).find())
				sampleFiles.add(new File(column));						
		}		
		compoundNames = DataImportUtils.extractNamedColumn(
				inputDataArray, featureColumn, linesToSkipAfterHeader);
		
		long badNameCount = Arrays.asList(compoundNames).stream().
				filter(n -> (Objects.isNull(n) || n.isBlank())).count();
		if(badNameCount > 0) {
			MessageDialog.showErrorMsg("Missing names in \"" + featureColumn + "\" column");
			return;
		}		
		File[]sampleFilesArray = sampleFiles.toArray(new File[sampleFiles.size()]);
		addResultsToNewPipeline(dataPipelineDefinitionPanel.getDataPipeline(), sampleFilesArray);		
	}
		
	public void selectDesignFile() {

		JnaFileChooser fc = new JnaFileChooser(baseDesignDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select experiment design file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			File designFile = fc.getSelectedFile();
			baseDesignDirectory = designFile.getParentFile();
			savePreferences();	
			
			//	TODO read design
		}
	}
	
	public void setExistingDataPipeline(DataPipeline pipeline) {
		
		existingDataPipeline = pipeline;
		dataPipelineDefinitionPanel.setDataPipeline(pipeline);
		dataPipelineDefinitionPanel.lockPanel();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if(e.getStateChange() == ItemEvent.SELECTED) {
			
			if(e.getItem() instanceof DataTypeForImport)			
				updateInterfaceForImportType((DataTypeForImport)e.getItem());			
		}
	}
	
	public void setDataTypeForImport(DataTypeForImport newType) {
		toolBar.setDataTypeForImport(newType);
	}

	public void updateInterfaceForImportType(DataTypeForImport importType) {
		
		toolBar.updateInterfaceForImportType(importType);
		
		if(importType.equals(DataTypeForImport.AGILENT_UNTARGETED))
			updateInterfaceForAgilentUntargetedImport();
		
		if(importType.equals(DataTypeForImport.AGILENT_PROFINDER_TARGETED))
			updateInterfaceForProFinderImport();
		
		if(importType.equals(DataTypeForImport.GENERIC_TARGETED))
			updateInterfaceForGenericTargetedImport();	
	}

	private void updateInterfaceForAgilentUntargetedImport() {

		selectCefLibraryButton.setActionCommand(
				MainActionCommands.SELECT_INPUT_LIBRARY_COMMAND.getName());
		libraryTextFieldLabel.setText("Library: ");
		removeAbnormalIsoPatternsCheckBox.setEnabled(true);
		skipCompoundMatchingCheckbox.setEnabled(true);
		selectPFdetailedCsvButton.setEnabled(false);
	}

	private void updateInterfaceForProFinderImport() {
		
		selectCefLibraryButton.setActionCommand(
				MainActionCommands.SELECT_PROFINDER_SIMPLE_CSV_COMMAND.getName());
		libraryTextFieldLabel.setText("ProFinder simple CSV: ");
		removeAbnormalIsoPatternsCheckBox.setEnabled(false);
		skipCompoundMatchingCheckbox.setEnabled(false);
		selectPFdetailedCsvButton.setEnabled(true);
	}

	private void updateInterfaceForGenericTargetedImport() {
		
		selectCefLibraryButton.setActionCommand(
				MainActionCommands.SELECT_NORMALIZED_TARGETED_DATA_COMMAND.getName());
		libraryTextFieldLabel.setText("Normalized targeted data: ");
		removeAbnormalIsoPatternsCheckBox.setEnabled(false);
		selectPFdetailedCsvButton.setEnabled(false);
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		baseLibraryDirectory =  
				new File(preferences.get(BASE_LIBRARY_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		dataFileDirectory =  
				new File(preferences.get(BASE_DATA_FILES_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		baseDesignDirectory =  
				new File(preferences.get(BASE_DESIGN_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		
		removeAbnormalIsoPatternsCheckBox.setSelected(
				preferences.getBoolean(REMOVE_ABNORMAL_ISO_PATTERNS, true));				
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_LIBRARY_DIRECTORY, baseLibraryDirectory.getAbsolutePath());
		preferences.put(BASE_DATA_FILES_DIRECTORY, dataFileDirectory.getAbsolutePath());
		preferences.put(BASE_DESIGN_DIRECTORY, baseDesignDirectory.getAbsolutePath());
		preferences.putBoolean(REMOVE_ABNORMAL_ISO_PATTERNS, removeAbnormalIsoPatterns());
	}

	@Override
	public void dataPipelineChanged(DataPipelineEvent e) {
		
		DataPipeline pipeline = (DataPipeline)e.getSource();
		if(pipeline == null)
			return;
		
		DataAcquisitionMethod newMethod = pipeline.getAcquisitionMethod();
		boolean methodChanged = false;
		if(newMethod != null) {
			
			if(activeDataAcquisitionMethod == null) {
				activeDataAcquisitionMethod = newMethod;
				methodChanged = true;
			}
			else {
				if(!activeDataAcquisitionMethod.equals(newMethod)) {
					activeDataAcquisitionMethod = newMethod;
					methodChanged = true;
				}
			}
		}
		if(methodChanged) {			
			dataFilesForMethod = 
					currentProject.getDataFilesForAcquisitionMethod(activeDataAcquisitionMethod);
		}
	}
		
	private void loadDesignFromFile() {
		
		MessageDialog.showWarningMsg("Under construction", this);
		return;
	}
}





















