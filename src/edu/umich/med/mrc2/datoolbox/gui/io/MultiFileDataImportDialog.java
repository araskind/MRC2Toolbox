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
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.ResultsFile;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
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
import edu.umich.med.mrc2.datoolbox.utils.DataImportUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class MultiFileDataImportDialog extends JDialog
	implements ActionListener, ItemListener, DataPipelineEventListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -1605053468969470610L;

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
				dataFileDirectory, 
				baseDesignDirectory,
				pfaTempDir;
	private CompoundLibrary currentLibrary;
	private JTextField libraryTextField;
	private MultiFileImportToolbar toolBar;
	
	private JLabel libFileLabel;
	private JComboBox featureSubsetcomboBox;
	private JCheckBox removeAbnormalIsoPatternsCheckBox;
	private JCheckBox skipCompoundMatchingCheckbox;
	
	private DataFileSampleMatchPanel matchPanel;
	private TaskListener dataLoadTaskListener;
	private DataPipelineDefinitionPanel dataPipelineDefinitionPanel;
	private IndeterminateProgressDialog idp;
	private ReferenceSampleDialog rsd;	
	private DataAcquisitionMethod activeDataAcquisitionMethod;
	private Set<DataFile>dataFilesForMethod;
	private boolean pfaLoaded = false;
	private DataAnalysisProject currentProject;
	private AcquisitionMethodExtendedEditorDialog acquisitionMethodEditorDialog;
	private DataExtractionMethodEditorDialog dataExtractionMethodEditorDialog;
	
	private boolean areDataFromProFinder;
	
	private static final Icon importMultifileIcon = GuiUtils.getIcon("importMultifile", 32);
	private JLabel dataFileLabel;
	private JTextField dataFileTextField;

	public MultiFileDataImportDialog(TaskListener dataLoadTaskListener) {

		super();
		setTitle("Import data from multiple files");
		setIconImage(((ImageIcon) importMultifileIcon).getImage());
		setPreferredSize(new Dimension(800, 640));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 640));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.dataLoadTaskListener = dataLoadTaskListener;
		existingDataPipeline = null;
		areDataFromProFinder = false;

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

		/**
		JLabel lblNewLabel = new JLabel("Features to align ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);

		featureSubsetcomboBox = new JComboBox<FeatureAlignmentType>(
				new DefaultComboBoxModel<FeatureAlignmentType>(FeatureAlignmentType.values()));
		featureSubsetcomboBox.setEnabled(false);
		featureSubsetcomboBox.setPreferredSize(new Dimension(28, 25));
		featureSubsetcomboBox.setMinimumSize(new Dimension(28, 25));
		GridBagConstraints gbc_fscomboBox = new GridBagConstraints();
		gbc_fscomboBox.insets = new Insets(0, 0, 5, 5);
		gbc_fscomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_fscomboBox.gridx = 1;
		gbc_fscomboBox.gridy = 0;
		panel_1.add(featureSubsetcomboBox, gbc_fscomboBox);

		JLabel lblNewLabel_1 = new JLabel("Only selected subset will be imported!");
		lblNewLabel_1.setForeground(Color.RED);
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		*/
		
		JPanel panel_2 = new JPanel();
		main.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		matchPanel = new DataFileSampleMatchPanel();
		panel_2.add(matchPanel, BorderLayout.CENTER);

		JPanel libChooserPanel = new JPanel();
		libChooserPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_libChooserPanel = new GridBagLayout();
		gbl_libChooserPanel.columnWidths = new int[]{0, 0, 0};
		gbl_libChooserPanel.rowHeights = new int[]{0, 0, 0};
		gbl_libChooserPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_libChooserPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		libChooserPanel.setLayout(gbl_libChooserPanel);

		libFileLabel = new JLabel("Library: ");
		GridBagConstraints gbc_libFileLabel = new GridBagConstraints();
		gbc_libFileLabel.anchor = GridBagConstraints.EAST;
		gbc_libFileLabel.insets = new Insets(0, 0, 5, 5);
		gbc_libFileLabel.gridx = 0;
		gbc_libFileLabel.gridy = 0;
		libChooserPanel.add(libFileLabel, gbc_libFileLabel);

		libraryTextField = new JTextField();
		libraryTextField.setEditable(false);
		GridBagConstraints gbc_libraryTextField = new GridBagConstraints();
		gbc_libraryTextField.insets = new Insets(0, 0, 5, 0);
		gbc_libraryTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_libraryTextField.gridx = 1;
		gbc_libraryTextField.gridy = 0;
		libChooserPanel.add(libraryTextField, gbc_libraryTextField);
		libraryTextField.setColumns(10);

		panel_2.add(libChooserPanel, BorderLayout.NORTH);
		
		dataFileLabel = new JLabel("Data file: ");
		GridBagConstraints gbc_dataFileLabel = new GridBagConstraints();
		gbc_dataFileLabel.anchor = GridBagConstraints.EAST;
		gbc_dataFileLabel.insets = new Insets(0, 0, 0, 5);
		gbc_dataFileLabel.gridx = 0;
		gbc_dataFileLabel.gridy = 1;
		libChooserPanel.add(dataFileLabel, gbc_dataFileLabel);
		
		dataFileTextField = new JTextField();
		dataFileTextField.setEditable(false);
		GridBagConstraints gbc_dataFileTextField = new GridBagConstraints();
		gbc_dataFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_dataFileTextField.gridx = 1;
		gbc_dataFileTextField.gridy = 1;
		libChooserPanel.add(dataFileTextField, gbc_dataFileTextField);
		dataFileTextField.setColumns(10);

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
//		initChooser();
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

		if (command.equals(MainActionCommands.ADD_DATA_FILES_COMMAND.getName()))
			selectDataFiles();
		
		if (command.equals(MainActionCommands.LOAD_DATA_FROM_PROFINDER_PFA_COMMAND.getName()))
			selectPfaFile();

		if (command.equals(MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName()))
			removeDataFiles();

		if (event.getActionCommand().equals(MainActionCommands.IMPORT_DATA_COMMAND.getName()))
			importData();

		if (command.equals(MainActionCommands.CLEAR_DATA_COMMAND.getName())) {

			if (MessageDialog.showChoiceMsg("Clear input data?", this) == JOptionPane.YES_OPTION)
				clearPanel();
		}
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

	private void addResultsToNewPipeline(DataPipeline pipeline, File[] dataFiles) {

		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Set<SampleDataResultObject>sampleDataResultObjects = new TreeSet<SampleDataResultObject>(
				new SampleDataResultObjectComparator(SortProperty.resultFile));
		
		//	Get already added data
		Set<SampleDataResultObject> addedData = matchPanel.getSampleDataResultObjects(false);
		sampleDataResultObjects.addAll(addedData);
		
		DataAcquisitionMethod acqMethod = pipeline.getAcquisitionMethod();
		DataExtractionMethod daMethod = pipeline.getDataExtractionMethod();
		
		for (File f : dataFiles) {

			String fileBaseName = FilenameUtils.getBaseName(f.getName());
			SampleDataResultObject existingObject = addedData.stream().
					filter(s -> s.getDataFile().getName().equals(fileBaseName)).
					findFirst().orElse(null);
			if(existingObject == null) {
				DataFile df = new DataFile(fileBaseName, acqMethod);
				df.setFullPath(f.getAbsolutePath());
				ResultsFile resultFile = new ResultsFile(fileBaseName, daMethod, new Date(), df);
				resultFile.setFullPath(f.getAbsolutePath());
				df.addResultFile(resultFile);					
				ExperimentalSample matchedSample = 
						DataImportUtils.getSampleFromFileName(fileBaseName, project);
				SampleDataResultObject sdro = 
						new SampleDataResultObject(matchedSample, df, resultFile);
				sampleDataResultObjects.add(sdro);
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
				//	df.addResultFile(resultFile);					
				ExperimentalSample matchedSample = 
						DataImportUtils.getSampleFromFileName(fileBaseName, project);
				SampleDataResultObject sdro = 
						new SampleDataResultObject(matchedSample, df, resultFile);
				sampleDataResultObjects.add(sdro);
			}
		}	
		matchPanel.loadSampleDataResultObject(sampleDataResultObjects);
	}
	
//	private void addResultsProFinderPfaFileToNewPipeline() {
//		
//		newDataPipeline = dataPipelineDefinitionPanel.getDataPipeline();
//		if(newDataPipeline == null)
//			return;
//
//		if(MRC2ToolBoxCore.getCurrentProject().getDataPipelines().contains(newDataPipeline)) {
//			MessageDialog.showErrorMsg("The experiment already contains data pipeline \n"
//					+ "with selected combination of assay, data acquisition and data analysis methods.\n"
//					+ "Please adjust you selection.\n"
//					+ "If you want to replace the existing data\n"
//					+ "please delete them first and then re-upload.", 
//					this);
//			return;
//		}
//		File pfaFile = chooser.getSelectedFile();		
//		ProFinderArchiveExtractionTask task = 
//				new ProFinderArchiveExtractionTask(pfaFile, newDataPipeline);
//		idp = new IndeterminateProgressDialog("Loading document preview ...", this, task);
//		idp.setLocationRelativeTo(this.getContentPane());
//		idp.setVisible(true);	
//	}
	
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
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(cefFiles != null && !cefFiles.isEmpty()) {
				
				File[] dataFiles = cefFiles.toArray(new File[cefFiles.size()]);
				addResultsToNewPipeline(pipeline, dataFiles);
				pfaLoaded = true;
			}
			else {
				MessageDialog.showErrorMsg("Unable to extract data from " + proFinderArchiveFile.getName(), 
						MultiFileDataImportDialog.this);
				try {
					FileUtils.deleteDirectory(pfaTempDir);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}	
			return null;
		}
	}
	
    private Collection<File> extractFile(File zipFile, File destinationDir) throws Exception {
    	
    	Collection<File>cefFiles = new TreeSet<File>();
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

//	private void addSelectedLibraryFile() {
//		
//		libraryFile = chooser.getSelectedFile();
//		libraryTextField.setText(libraryFile.getPath());
//		baseLibraryDirectory = libraryFile.getParentFile();
//		savePreferences();	
//	}

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
		if(existingDataPipeline != null) {

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
			dispose();
		}		
	}
	
	private Collection<String>validateInputData(){
		
		ArrayList<String>errors = new ArrayList<String>();
		
		//	Validate new pipeline data
		if(newDataPipeline != null) {
			
			errors.addAll(dataPipelineDefinitionPanel.validatePipelineDefinition());

			if(libraryFile == null && areDataFromProFinder == false)
				errors.add("Library file not specified.");
	
			if(!libraryFile.exists() || !libraryFile.canRead())
				errors.add("Can not read library file..");
		}
		//	Check if all files matched to samples
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
		
		return errors;
	}

	private void removeDataFiles() {	
		matchPanel.removeSelected();
	}

	private void selectDataFiles() {

		JnaFileChooser fc = new JnaFileChooser(dataFileDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.setTitle("Select CEF files for the samples");
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(this)) {
			
			File[] dataFiles = fc.getSelectedFiles();
			if(dataFiles.length == 0)
				return;
			
			if(pfaLoaded && !matchPanel.getSampleDataResultObjects(false).isEmpty()) {
				
				MessageDialog.showErrorMsg("You've selected ProFinder archive as data source.\n"
						+ "Adding individual CEF files to this data analysis pipeline is not supported.", 
						this);
				return;
			}		
			dataFileDirectory = dataFiles[0].getParentFile();
			savePreferences();
			//	Add data to existing pipeline
			if(existingDataPipeline != null) {
				addResultsToExistingPipeline(dataFiles);
				return;
			}
			//	Add extra data to new pipeline
			if(newDataPipeline != null) {
				addResultsToNewPipeline(newDataPipeline, dataFiles);
				return;
			}
			//	Create new pipeline and add data to it		
			newDataPipeline = dataPipelineDefinitionPanel.getDataPipeline();
			if(existingDataPipeline == null 
					&& newDataPipeline != null
					&& MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getDataPipelines().contains(newDataPipeline)) {
				MessageDialog.showErrorMsg("The experiment already contains data pipeline \n"
						+ "with selected combination of assay, data acquisition and data analysis methods.\n"
						+ "Please adjust you selection.\n"
						+ "If you want to replace the existing data\n"
						+ "please delete them first and then re-upload.", 
						this);
				newDataPipeline = null;
				return;
			}	
			addResultsToNewPipeline(newDataPipeline, dataFiles);
			areDataFromProFinder = false;
		}
	}
	
	private void selectPfaFile() {
		
		if(existingDataPipeline != null) {
			MessageDialog.showWarningMsg("Adding results from ProFinder archive\n"
					+ "to existing data analysis pipeline i not supported\n"
					+ "Please create new data pipeline to import the data from ProFinder.", this);
			return;
		}	
		JnaFileChooser fc = new JnaFileChooser(dataFileDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("ProFinder archive files", "pfa", "PFA");
		fc.setTitle("Select ProFinder archive file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			newDataPipeline = dataPipelineDefinitionPanel.getDataPipeline();
			if(newDataPipeline == null)
				return;

			if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getDataPipelines().contains(newDataPipeline)) {
				MessageDialog.showErrorMsg("The experiment already contains data pipeline \n"
						+ "with selected combination of assay, data acquisition and data analysis methods.\n"
						+ "Please adjust you selection.\n"
						+ "If you want to replace the existing data\n"
						+ "please delete them first and then re-upload.", 
						this);
				return;
			}
			File pfaFile = fc.getSelectedFile();
			dataFileDirectory = pfaFile.getParentFile();
			savePreferences();	
			
			areDataFromProFinder = true;
			ProFinderArchiveExtractionTask task = 
					new ProFinderArchiveExtractionTask(pfaFile, newDataPipeline);
			idp = new IndeterminateProgressDialog("Loading document preview ...", this, task);
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

	private void updateInterfaceForImportType(DataTypeForImport importType) {
		
		if(importType.equals(DataTypeForImport.AGILENT_UNTARGETED))
			updateInterfaceForAgilentUntargetedImport();
		
		if(importType.equals(DataTypeForImport.AGILENT_PROFINDER_TARGETED))
			updateInterfaceForProFinderImport();
		
		if(importType.equals(DataTypeForImport.GENERIC_TARGETED))
			updateInterfaceForGenericTargetedImport();	
	}

	private void updateInterfaceForAgilentUntargetedImport() {
		// TODO Auto-generated method stub
		
	}

	private void updateInterfaceForProFinderImport() {
		// TODO Auto-generated method stub
		
	}

	private void updateInterfaceForGenericTargetedImport() {
		// TODO Auto-generated method stub
		
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
	
	//	TODO
	//	Collection<DataFile> dataFiles = matchPanel.getAllDataFiles();
	//	if(dataFiles.isEmpty())
	//		return;
	//			
	//	File designFile = chooser.getSelectedFile();
	//	baseLibraryDirectory = designFile.getParentFile();
	//	savePreferences();
	//	
	//	//	Read design file
	//	String[][] designData = null;
	//	try {
	//		designData = DelimitedTextParser.parseTextFileWithEncoding(designFile, MRC2ToolBoxCore.getDataDelimiter());
	//	} catch (IOException e) {
	//		// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
	//	if (designData == null) {
	//		MessageDialog.showErrorMsg("Couldn't read design file!", this);
	//		return;
	//	}
	//	String[] header = designData[0];
	//	int dataFileColumn = -1;
	//	int nameColumn = -1;
	//	int idColumn = -1;
	//	for (int i = 0; i < header.length; i++) {
	//
	//		if (header[i].equals(ExperimentDesignFields.SAMPLE_NAME.getName()))
	//			nameColumn = i;
	//
	//		if (header[i].equals(ExperimentDesignFields.SAMPLE_ID.getName()))
	//			idColumn = i;
	//
	//		if (header[i].equals(ExperimentDesignFields.DATA_FILE.getName()))
	//			dataFileColumn = i;
	//	}
	//	if (dataFileColumn == -1 && (nameColumn == -1 || idColumn == -1)) {
	//		MessageDialog.showErrorMsg("One or more obligatory columns missing!\n"
	//				+ "Obligatory columns:\n"
	//				+ "1. " + ExperimentDesignFields.DATA_FILE.getName() + "\n"
	//				+ "2. " + ExperimentDesignFields.SAMPLE_ID.getName() 
	//				+ " or " + ExperimentDesignFields.SAMPLE_NAME.getName(), this);
	//		return;
	//	}
	//	Map<DataFile,ExperimentalSample>fileSampleMap = new TreeMap<DataFile,ExperimentalSample>();
	//	ExperimentDesign design = MRC2ToolBoxCore.getCurrentProject().getExperimentDesign();
	//	for (int i = 1; i < designData.length; i++) {
	//		
	//		String dataFileName = FilenameUtils.getBaseName(designData[i][dataFileColumn]);
	//		String sampleId = null;
	//		String sampleName = null;
	//		ExperimentalSample sample = null;
	//		if(idColumn > -1) {
	//			sampleId = designData[i][idColumn];
	//			sample = design.getSampleById(sampleId);
	//		}			
	//		if(sample == null && nameColumn > -1) {
	//			sampleName = designData[i][nameColumn];
	//			sample = design.getSampleByName(sampleName);
	//		}	
	//		DataFile dataFile = dataFiles.stream().
	//				filter(f -> FilenameUtils.getBaseName(f.getName()).equals(dataFileName)).
	//				findFirst().orElse(null);
	//		
	//		if(sample != null && dataFile != null)
	//			fileSampleMap.put(dataFile, sample);
	//	}
	//	//	Assign samples in the table
	//	matchPanel.assignSamples(fileSampleMap);
	//	
	//	//	Find unmapped files
	//	Set<DataFile> mappedFiles = fileSampleMap.keySet();
	//	List<String> unmappedFileNames = 
	//			dataFiles.stream().filter(f -> !mappedFiles.contains(f)).
	//			sorted().map(f -> f.getName()).collect(Collectors.toList());
	//	
	//	if (!unmappedFileNames.isEmpty()) {
	//		MessageDialog.showWarningMsg("The following data files were not mapped to samples:\n" + 
	//				StringUtils.join(unmappedFileNames, "\n"), this);
	//
	//	}
	//	return;
	}
}





















