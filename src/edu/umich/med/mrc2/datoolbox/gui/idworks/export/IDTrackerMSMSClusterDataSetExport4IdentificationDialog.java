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

package edu.umich.med.mrc2.datoolbox.gui.idworks.export;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.IDTrackerDataExportParameters;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMSMSClusterProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.SummaryIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.FormUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class IDTrackerMSMSClusterDataSetExport4IdentificationDialog extends JDialog 
		implements ActionListener, BackedByPreferences { 

	private static final long serialVersionUID = 1L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("clustersSummary", 32);
	protected Preferences preferences;
		
	private static final String PREFS_NODE = 
			"edu.umich.med.mrc2.IDTrackerMSMSClusterDataSetExport4IdentificationDialog";
	
	private static final String BASE_DIR = "BASE_DIR";
	
	private static final String MSMS_CLUSTER_PROPERTIES = 
			"MSMS_CLUSTER_PROPERTIES";
	private static final String MSMS_CLUSTER_IDENTIFICATION_PROPERTIES = 
			"MSMS_CLUSTER_IDENTIFICATION_PROPERTIES";	
	private static final String SUMMARY_IDENTIFICATION_PROPERTIES = 
			"SUMMARY_IDENTIFICATION_PROPERTIES";
	private static final String PARENT_BINNING_MASS_ERROR = "PARENT_BINNING_MASS_ERROR";
	private static final String PARENT_BINNING_MASS_ERROR_TYPE = "PARENT_BINNING_MASS_ERROR_TYPE";
	
	private static final String PROPERTIES_DELIMITER = "@";
	
	private static final IDTrackerMSMSClusterProperties[] defaultMSMSClusterProperties 	
		= new IDTrackerMSMSClusterProperties[] {				
				IDTrackerMSMSClusterProperties.CLUSTER_ID,
				IDTrackerMSMSClusterProperties.MEDIAN_RETENTION_TIME,
				IDTrackerMSMSClusterProperties.MEDIAN_MZ,
				IDTrackerMSMSClusterProperties.LOOKUP_FEATURE_MZ,
				IDTrackerMSMSClusterProperties.LOOKUP_FEATURE_RT,
				IDTrackerMSMSClusterProperties.LOOKUP_FEATURE_NAME,
				IDTrackerMSMSClusterProperties.RANK,
		};
	
	private static final IDTrackerFeatureIdentificationProperties[] defaultMSMSClusterIdentificationProperties 
		= new IDTrackerFeatureIdentificationProperties[] {				
				IDTrackerFeatureIdentificationProperties.COMPOUND_NAME,
				IDTrackerFeatureIdentificationProperties.DATABASE_ID,
				IDTrackerFeatureIdentificationProperties.SOURCE_DATABASE,
				IDTrackerFeatureIdentificationProperties.MSMS_LIBRARY,
				IDTrackerFeatureIdentificationProperties.MSMS_ENTROPY_SCORE,
				IDTrackerFeatureIdentificationProperties.ID_SCORE,
				IDTrackerFeatureIdentificationProperties.MATCH_TYPE,
				IDTrackerFeatureIdentificationProperties.ID_LEVEL,				
		};	
	
	private static final SummaryIdentificationProperties[] summaryIdentificationProperties 	
		= new SummaryIdentificationProperties[] {				
				SummaryIdentificationProperties.NUMBER_OF_HITS,
				SummaryIdentificationProperties.MEDIAN_TOP_ENTROPY_SCORE,
				SummaryIdentificationProperties.RT_RANGE,
				SummaryIdentificationProperties.FRAGMENTATION_ENERGIES_NUMBER,
				SummaryIdentificationProperties.FRAGMENTATION_ENERGIES,
				SummaryIdentificationProperties.PARENT_IONS_NUMBER,
				SummaryIdentificationProperties.PARENT_IONS,
				SummaryIdentificationProperties.LIB_ADDUCTS_NUMBER,
				SummaryIdentificationProperties.LIB_ADDUCTS,
				SummaryIdentificationProperties.MATCH_TYPES,
		};

	private IDTrackerMSMSClusterProperties[] selectedMSMSClusterProperties;	
	private IDTrackerFeatureIdentificationProperties[] selectedMSMSClusterIdentificationProperties;
	private SummaryIdentificationProperties[] selectedSummaryIdentificationProperties;

	private JButton exportButton;
	private File baseDirectory;
	private JTextField outputFilleTextField;
	private JList<IDTrackerMSMSClusterProperties> clusterPropertyList;	
	private JList<IDTrackerFeatureIdentificationProperties> identificationDetailsList;	
	private JList<SummaryIdentificationProperties> summaryIdentificationPropertyList;
	private JFormattedTextField redundantIonMassErrorField;
	private JComboBox<MassErrorType> redundantIonMassErrorComboBox;
	
	private final String BROWSE_COMMAND = "BROWSE_FOR_OUTPUT";
	
	private IMSMSClusterDataSet activeMSMSClusterDataSet;

	public IDTrackerMSMSClusterDataSetExport4IdentificationDialog(
			ActionListener listener, 
			IMSMSClusterDataSet activeMSMSClusterDataSet) {
		super();
		setTitle("Export identification summary data for the active MSMS cluster data set \"" + 
				activeMSMSClusterDataSet.getName() +"\"");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(700, 640));
		setPreferredSize(new Dimension(1000, 800));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.activeMSMSClusterDataSet = activeMSMSClusterDataSet;
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Save to");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		outputFilleTextField = new JTextField();
		outputFilleTextField.setEditable(false);
		GridBagConstraints gbc_outputFilleTextField = new GridBagConstraints();
		gbc_outputFilleTextField.insets = new Insets(0, 0, 0, 5);
		gbc_outputFilleTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputFilleTextField.gridx = 1;
		gbc_outputFilleTextField.gridy = 0;
		panel_1.add(outputFilleTextField, gbc_outputFilleTextField);
		outputFilleTextField.setColumns(10);
		
		JButton outputFileBrowseButton = new JButton("Browse ...");
		outputFileBrowseButton.setActionCommand(BROWSE_COMMAND);
		outputFileBrowseButton.addActionListener(this);
		GridBagConstraints gbc_outputFileBrowseButton = new GridBagConstraints();
		gbc_outputFileBrowseButton.anchor = GridBagConstraints.BELOW_BASELINE;
		gbc_outputFileBrowseButton.gridx = 2;
		gbc_outputFileBrowseButton.gridy = 0;
		panel_1.add(outputFileBrowseButton, gbc_outputFileBrowseButton);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(panel_3, BorderLayout.CENTER);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_3.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		clusterPropertyList = new JList<>();
		JScrollPane scrollPane = new JScrollPane(clusterPropertyList);
		scrollPane.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Cluster properties", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_3.add(scrollPane, gbc_scrollPane);
		
		identificationDetailsList = new JList<>();
		JScrollPane scrollPane_1 = new JScrollPane(identificationDetailsList);
		scrollPane_1.setBorder(new TitledBorder(null, "Identification details", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridwidth = 3;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 0;
		panel_3.add(scrollPane_1, gbc_scrollPane_1);
				
		summaryIdentificationPropertyList = new JList<>();
		JScrollPane scrollPane_2 = new JScrollPane(summaryIdentificationPropertyList);
		scrollPane_2.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Identification summarization", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 4;
		gbc_scrollPane_2.gridy = 0;
		panel_3.add(scrollPane_2, gbc_scrollPane_2);
		
		JButton resetFieldsButton = 
				new JButton(MainActionCommands.RESET_IDTRACKER_DATA_EXPORT_FIELDS_COMMAND.getName());
		resetFieldsButton.setActionCommand(
				MainActionCommands.RESET_IDTRACKER_DATA_EXPORT_FIELDS_COMMAND.getName());
		resetFieldsButton.addActionListener(this);
		GridBagConstraints gbc_resetFieldsButton = new GridBagConstraints();		
		gbc_resetFieldsButton.insets = new Insets(0, 0, 5, 0);
		gbc_resetFieldsButton.anchor = GridBagConstraints.EAST;
		gbc_resetFieldsButton.gridx = 4;
		gbc_resetFieldsButton.gridy = 1;
		panel_3.add(resetFieldsButton, gbc_resetFieldsButton);
		
		JLabel lblNewLabel = 
				new JLabel("Group parent ions using the following mass window:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 2;
		panel_3.add(lblNewLabel, gbc_lblNewLabel);
		
		redundantIonMassErrorField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		redundantIonMassErrorField.setPreferredSize(new Dimension(80, 20));
		redundantIonMassErrorField.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_redundantIonMAssErrorField = new GridBagConstraints();
		gbc_redundantIonMAssErrorField.insets = new Insets(0, 0, 0, 5);
		gbc_redundantIonMAssErrorField.fill = GridBagConstraints.HORIZONTAL;
		gbc_redundantIonMAssErrorField.gridx = 1;
		gbc_redundantIonMAssErrorField.gridy = 2;
		panel_3.add(redundantIonMassErrorField, gbc_redundantIonMAssErrorField);
		
		redundantIonMassErrorComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(MassErrorType.values()));
		redundantIonMassErrorComboBox.setMinimumSize(new Dimension(80, 22));
		redundantIonMassErrorComboBox.setPreferredSize(new Dimension(80, 22));
		GridBagConstraints gbc_redundantIonMAssErrorComboBox = new GridBagConstraints();
		gbc_redundantIonMAssErrorComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_redundantIonMAssErrorComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_redundantIonMAssErrorComboBox.gridx = 2;
		gbc_redundantIonMAssErrorComboBox.gridy = 2;
		panel_3.add(redundantIonMassErrorComboBox, gbc_redundantIonMAssErrorComboBox);
			
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		btnCancel.addActionListener(al);

		exportButton = new JButton(MainActionCommands.EXPORT_MSMS_CLUSTER_ID_SUMMARY_COMMAND.getName());
		buttonPanel.add(exportButton);
		exportButton.setActionCommand(MainActionCommands.EXPORT_MSMS_CLUSTER_ID_SUMMARY_COMMAND.getName());
		exportButton.addActionListener(listener);
		JRootPane rootPane = SwingUtilities.getRootPane(exportButton);
		rootPane.setDefaultButton(exportButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
			
		populateExportPropertyLists();
		loadPreferences();
		
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		String fileName = activeMSMSClusterDataSet.getName().replaceAll("\\W+", "-") 
				+ "MSMSClusterExport_" + timestamp + ".txt";		
		String filePath = Paths.get(baseDirectory.getAbsolutePath(), fileName).toString();
		outputFilleTextField.setText(filePath);
		pack();
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}
	
	private void populateExportPropertyLists() {
		
		//	Cluster properties
		DefaultListModel<IDTrackerMSMSClusterProperties> clustPropsModel = new DefaultListModel<>();
		clustPropsModel.addAll(Arrays.asList(IDTrackerMSMSClusterProperties.values()));
		clusterPropertyList.setModel(clustPropsModel);
			
		//	ID properties
		DefaultListModel<IDTrackerFeatureIdentificationProperties> idPropsModel = new DefaultListModel<>();
		idPropsModel.addAll(Arrays.asList(IDTrackerFeatureIdentificationProperties.values()));
		identificationDetailsList.setModel(idPropsModel);
		
		//	Summary identification properties
		DefaultListModel<SummaryIdentificationProperties> summaryIdPropsModel = new DefaultListModel<>();
		summaryIdPropsModel.addAll(Arrays.asList(SummaryIdentificationProperties.values()));
		summaryIdentificationPropertyList.setModel(summaryIdPropsModel);
		
		if(selectedMSMSClusterProperties != null)
			selectClusterPropertiesListItems(selectedMSMSClusterProperties);
		else
			selectClusterPropertiesListItems(defaultMSMSClusterProperties);
		
		if(selectedSummaryIdentificationProperties != null)
			selectSummaryIdentificationPropertiesListItems(selectedSummaryIdentificationProperties);
		else
			selectSummaryIdentificationPropertiesListItems(summaryIdentificationProperties);
		
		if(selectedMSMSClusterIdentificationProperties != null)
			selectIdentificationPropertiesListItems(selectedMSMSClusterIdentificationProperties);
		else
			selectIdentificationPropertiesListItems(defaultMSMSClusterIdentificationProperties);		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectOutputFile();

		if(e.getActionCommand().equals(MainActionCommands.RESET_IDTRACKER_DATA_EXPORT_FIELDS_COMMAND.getName()))
			resetExportFields();
	}
	
	private void selectOutputFile() {
				
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Export data for \"" + activeMSMSClusterDataSet.getName() 
			+ "\" data set to text file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Set output file");
		String defaultFileName = 
				activeMSMSClusterDataSet.getName().replaceAll("\\W+", "-") 
				+ "MSMSClusterExport_"  + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt";
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File exportFile  = fc.getSelectedFile();
			outputFilleTextField.setText(exportFile.getAbsolutePath());
			baseDirectory = exportFile.getParentFile();
		}
	}
	
	private void resetExportFields() {
	
		selectedMSMSClusterProperties = defaultMSMSClusterProperties;
		selectedMSMSClusterIdentificationProperties = defaultMSMSClusterIdentificationProperties;
		selectClusterPropertiesListItems(selectedMSMSClusterProperties);
		selectIdentificationPropertiesListItems(selectedMSMSClusterIdentificationProperties);
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory =
				new File(preferences.get(BASE_DIR, MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory())).
				getAbsoluteFile();
		
		//	Select fields for export
		ArrayList<IDTrackerMSMSClusterProperties> storedClusterProperties = new ArrayList<>();		
		ArrayList<IDTrackerFeatureIdentificationProperties> storedIdentificationProperties = new ArrayList<>();
		ArrayList<SummaryIdentificationProperties> storedSummaryIdentificationProperties = new ArrayList<>();
		
		String msTwoClusterPropertiesString = preferences.get(MSMS_CLUSTER_PROPERTIES, "");
		selectedMSMSClusterProperties = defaultMSMSClusterProperties;
		if(!msTwoClusterPropertiesString.isEmpty()) {
			
			 String[] selectedMsTwoFeaturePropertiesNames =  
					 msTwoClusterPropertiesString.split(PROPERTIES_DELIMITER);

			 for(String name : selectedMsTwoFeaturePropertiesNames) {
				 
				 IDTrackerMSMSClusterProperties fProperty = 					
						 IDTrackerMSMSClusterProperties.getOptionByName(name);
				if(fProperty != null)
					storedClusterProperties.add(fProperty);
			 }
			 selectedMSMSClusterProperties = 
					 storedClusterProperties.toArray(
							 new IDTrackerMSMSClusterProperties[storedClusterProperties.size()]);
		}
		String summaryIdentificationPropertiesString = preferences.get(SUMMARY_IDENTIFICATION_PROPERTIES, "");
		selectedSummaryIdentificationProperties = summaryIdentificationProperties;
		if(!summaryIdentificationPropertiesString.isEmpty()) {
			
			 String[] selectedSummaryIdentificationPropertiesNames =  
					 summaryIdentificationPropertiesString.split(PROPERTIES_DELIMITER);

			 for(String name : selectedSummaryIdentificationPropertiesNames) {
				 
				 SummaryIdentificationProperties fProperty = 					
						 SummaryIdentificationProperties.getOptionByName(name);
				if(fProperty != null)
					storedSummaryIdentificationProperties.add(fProperty);
			 }
			 selectedSummaryIdentificationProperties = 
					 storedSummaryIdentificationProperties.toArray(
							 new SummaryIdentificationProperties[storedSummaryIdentificationProperties.size()]);
		}
		String msTwoIdentificationPropertiesString = preferences.get(MSMS_CLUSTER_IDENTIFICATION_PROPERTIES, "");
		selectedMSMSClusterIdentificationProperties = defaultMSMSClusterIdentificationProperties;
		if(!msTwoIdentificationPropertiesString.isEmpty()) {
			
			 String[] selectedMsTwoIdentificationPropertiesNames =  
					 msTwoIdentificationPropertiesString.split(PROPERTIES_DELIMITER);

			 for(String name : selectedMsTwoIdentificationPropertiesNames) {
				 
				 IDTrackerFeatureIdentificationProperties idProperty = 
						 IDTrackerFeatureIdentificationProperties.getOptionByName(name);
				if(idProperty != null)
					storedIdentificationProperties.add(idProperty);
			 }
			 selectedMSMSClusterIdentificationProperties = 
				 storedIdentificationProperties.toArray(
					 new IDTrackerFeatureIdentificationProperties[storedIdentificationProperties.size()]);
		}
		selectClusterPropertiesListItems(selectedMSMSClusterProperties);		
		selectIdentificationPropertiesListItems(selectedMSMSClusterIdentificationProperties);
		selectSummaryIdentificationPropertiesListItems(selectedSummaryIdentificationProperties);
		
		redundantIonMassErrorField.setText(
				Double.toString(preferences.getDouble(PARENT_BINNING_MASS_ERROR, 5.0d)));
		
		String errName = preferences.get(PARENT_BINNING_MASS_ERROR_TYPE, MassErrorType.mDa.name());
		MassErrorType redundantIonMassErrorType = MassErrorType.getTypeByName(errName);
		if(redundantIonMassErrorType == null)
			redundantIonMassErrorType = MassErrorType.mDa;

		redundantIonMassErrorComboBox.setSelectedItem(redundantIonMassErrorType);
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIR, baseDirectory.getAbsolutePath());

		List<String> msTwoClusterProperties = clusterPropertyList.getSelectedValuesList().stream().
				map(v -> v.name()).collect(Collectors.toList());
		List<String> msTwoFeatureProperties = summaryIdentificationPropertyList.getSelectedValuesList().stream().
				map(v -> v.name()).collect(Collectors.toList());
		List<String> msTwoIdentificationProperties = 
				identificationDetailsList.getSelectedValuesList().stream().
				map(v -> v.name()).collect(Collectors.toList());

		preferences.put(MSMS_CLUSTER_PROPERTIES, 
				StringUtils.join(msTwoClusterProperties, PROPERTIES_DELIMITER));
		preferences.put(SUMMARY_IDENTIFICATION_PROPERTIES, 
				StringUtils.join(msTwoFeatureProperties, PROPERTIES_DELIMITER));
		preferences.put(MSMS_CLUSTER_IDENTIFICATION_PROPERTIES, 
				StringUtils.join(msTwoIdentificationProperties, PROPERTIES_DELIMITER));
		
		preferences.putDouble(PARENT_BINNING_MASS_ERROR, getRedundantIonMassError());
		preferences.put(PARENT_BINNING_MASS_ERROR_TYPE, getRedundantIonMassErrorType().name());
	}
	
	private void selectClusterPropertiesListItems(
			IDTrackerMSMSClusterProperties[] selectedProperties) {
			
		clusterPropertyList.clearSelection();
		ArrayList<Integer>indices = new ArrayList<>();
		DefaultListModel<IDTrackerMSMSClusterProperties> model = 
				(DefaultListModel<IDTrackerMSMSClusterProperties>) clusterPropertyList.getModel();
		for(IDTrackerMSMSClusterProperties item : selectedProperties) {
			int idx = model.indexOf(item);
			if(idx > -1)
				indices.add(idx);
		}
		int[] toSelect = indices.stream().mapToInt(i->i).toArray();				
		clusterPropertyList.setSelectedIndices(toSelect);
	}
	
	private void selectSummaryIdentificationPropertiesListItems(
			SummaryIdentificationProperties[] selectedProperties) {
			
		summaryIdentificationPropertyList.clearSelection();
		ArrayList<Integer>indices = new ArrayList<>();
		DefaultListModel<SummaryIdentificationProperties> model = 
				(DefaultListModel<SummaryIdentificationProperties>) summaryIdentificationPropertyList.getModel();
		for(SummaryIdentificationProperties item : selectedProperties) {
			int idx = model.indexOf(item);
			if(idx > -1)
				indices.add(idx);
		}
		int[] toSelect = indices.stream().mapToInt(i->i).toArray();				
		summaryIdentificationPropertyList.setSelectedIndices(toSelect);
	}
	
	private void selectIdentificationPropertiesListItems(
			IDTrackerFeatureIdentificationProperties[] selectedProperties) {
			
		identificationDetailsList.clearSelection();
		ArrayList<Integer>indices = new ArrayList<>();
		DefaultListModel<IDTrackerFeatureIdentificationProperties> model = 
				(DefaultListModel<IDTrackerFeatureIdentificationProperties>) identificationDetailsList.getModel();
		for(IDTrackerFeatureIdentificationProperties item : selectedProperties) {
			int idx = model.indexOf(item);
			if(idx > -1)
				indices.add(idx);
		}
		int[] toSelect = indices.stream().mapToInt(i->i).toArray();				
		identificationDetailsList.setSelectedIndices(toSelect);
	}

	public Collection<IDTrackerMSMSClusterProperties> getSelectedMSMSClusterProperties() {
		return clusterPropertyList.getSelectedValuesList();
	}
	
	public Collection<SummaryIdentificationProperties> getSelectedSummaryIdentificationProperties() {
		return summaryIdentificationPropertyList.getSelectedValuesList();
	}

	public Collection<IDTrackerFeatureIdentificationProperties> getSelectedIdentificationProperties() {
		return identificationDetailsList.getSelectedValuesList();
	}
	
	public File getOutputFile() {
		
		if(outputFilleTextField.getText().trim().isEmpty())
			return null;
		
		return Paths.get(outputFilleTextField.getText()).toFile();
	}
	
	public double getRedundantIonMassError() {
		return FormUtils.getDoubleValueFromTextField(redundantIonMassErrorField);
	}
	
	public MassErrorType getRedundantIonMassErrorType() {
		return (MassErrorType)redundantIonMassErrorComboBox.getSelectedItem();
	}
	
	public Collection<String>validateFormParameters(){
		
		Collection<String>errors = new ArrayList<>();
		if(getOutputFile() == null)
			errors.add("Output file not specified");
		
		if(getSelectedMSMSClusterProperties().isEmpty())
			errors.add("No data fields for MSMS clusters selected");
		
		if(getSelectedSummaryIdentificationProperties().isEmpty())
			errors.add("No data fields for feature selected");
		
		if(getSelectedIdentificationProperties().isEmpty())
			errors.add("No data fields for compound identification selected");
		
		if(getRedundantIonMassError() <= 0.0)
			errors.add("Mass error for grouping parent ions must be > 0");
		
		return errors;
	}
	
	public IDTrackerDataExportParameters getIDTrackerDataExportParameters() {

		IDTrackerDataExportParameters params = new IDTrackerDataExportParameters(
				getSelectedMSMSClusterProperties(),			
				getSelectedIdentificationProperties(),
				getSelectedSummaryIdentificationProperties());
		params.setRedundantMzWindow(getRedundantIonMassError());
		params.setRedMzErrorType(getRedundantIonMassErrorType());
		
		return params;
	}
}












