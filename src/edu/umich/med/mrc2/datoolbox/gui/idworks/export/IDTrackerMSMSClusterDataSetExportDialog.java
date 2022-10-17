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
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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

import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMSMSClusterProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMsFeatureProperties;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class IDTrackerMSMSClusterDataSetExportDialog extends JDialog 
		implements ActionListener, BackedByPreferences { 

	/**
	 * 
	 */
	private static final long serialVersionUID = -3120164101290035794L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("exportClusterSet", 32);
	protected Preferences preferences;
		
	private static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.IDTrackerMSMSClusterDataSetExportDialog";
	public static final String BASE_DIR = "BASE_DIR";
	public static final String MSMS_CLUSTER_PROPERTIES = 
			"MSMS_CLUSTER_PROPERTIES";
	public static final String MSMS_CLUSTER_IDENTIFICATION_PROPERTIES = 
			"MSMS_CLUSTER_IDENTIFICATION_PROPERTIES";	
	public static final String PROPERTIES_DELIMITER = "@";
	
	private final static IDTrackerMSMSClusterProperties[] defaultMSMSClusterProperties 	
		= new IDTrackerMSMSClusterProperties[] {				
				IDTrackerMSMSClusterProperties.CLUSTER_ID,
				IDTrackerMSMSClusterProperties.MEDIAN_RETENTION_TIME,
				IDTrackerMSMSClusterProperties.MEDIAN_MZ,
				IDTrackerMSMSClusterProperties.LOOKUP_FEATURE_MZ,
				IDTrackerMSMSClusterProperties.LOOKUP_FEATURE_RT,
				IDTrackerMSMSClusterProperties.LOOKUP_FEATURE_NAME,				
		};
	private final static IDTrackerFeatureIdentificationProperties[] defaultMSMSClusterIdentificationProperties 
		= new IDTrackerFeatureIdentificationProperties[] {				
				IDTrackerFeatureIdentificationProperties.COMPOUND_NAME,
				IDTrackerFeatureIdentificationProperties.DATABASE_ID,
				IDTrackerFeatureIdentificationProperties.SOURCE_DATABASE,
				IDTrackerFeatureIdentificationProperties.MSMS_LIBRARY,
				IDTrackerFeatureIdentificationProperties.FWD_SCORE,
				IDTrackerFeatureIdentificationProperties.REVERSE_SCORE,
				IDTrackerFeatureIdentificationProperties.MATCH_TYPE,
				IDTrackerFeatureIdentificationProperties.ANNOTATIONS,
				IDTrackerFeatureIdentificationProperties.FOLLOWUPS,
				IDTrackerFeatureIdentificationProperties.ID_LEVEL,				
		};		
	private IDTrackerMSMSClusterProperties[] selectedMSMSClusterProperties;
	private IDTrackerFeatureIdentificationProperties[] selectedMSMSClusterIdentificationProperties;

	private JButton exportButton;
	private ImprovedFileChooser outputFileChooser;
	private File baseDirectory;
	private JTextField outputFilleTextField;
	private JList<IDTrackerMSMSClusterProperties> featurePropertyList;
	private JList<IDTrackerFeatureIdentificationProperties> identificationDetailsList;
	
	private final String BROWSE_COMMAND = "BROWSE_FOR_OUTPUT";
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IDTrackerMSMSClusterDataSetExportDialog(ActionListener listener) {
		super();
		setTitle("Export data from IDTracker");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(700, 640));
		setPreferredSize(new Dimension(700, 640));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
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
		gbl_panel_3.columnWidths = new int[]{0, 0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0, 0};
		gbl_panel_3.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		featurePropertyList = new JList<IDTrackerMSMSClusterProperties>();
		JScrollPane scrollPane = new JScrollPane(featurePropertyList);
		scrollPane.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Cluster properties", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_3.add(scrollPane, gbc_scrollPane);
		
		identificationDetailsList = new JList<IDTrackerFeatureIdentificationProperties>();
		JScrollPane scrollPane_1 = new JScrollPane(identificationDetailsList);
		scrollPane_1.setBorder(new TitledBorder(null, "Identification details", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 0;
		panel_3.add(scrollPane_1, gbc_scrollPane_1);
		
		JButton resetFieldsButton = 
				new JButton(MainActionCommands.RESET_IDTRACKER_DATA_EXPORT_FIELDS_COMMAND.getName());
		resetFieldsButton.setActionCommand(
				MainActionCommands.RESET_IDTRACKER_DATA_EXPORT_FIELDS_COMMAND.getName());
		resetFieldsButton.addActionListener(this);		
		GridBagConstraints gbc_resetFieldsButton = new GridBagConstraints();		
		gbc_resetFieldsButton.anchor = GridBagConstraints.EAST;
		gbc_resetFieldsButton.gridx = 1;
		gbc_resetFieldsButton.gridy = 1;
		panel_3.add(resetFieldsButton, gbc_resetFieldsButton);
			
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

		exportButton = new JButton(MainActionCommands.EXPORT_IDTRACKER_DATA_COMMAND.getName());
		buttonPanel.add(exportButton);
		exportButton.setActionCommand(MainActionCommands.EXPORT_IDTRACKER_DATA_COMMAND.getName());
		exportButton.addActionListener(listener);
		JRootPane rootPane = SwingUtilities.getRootPane(exportButton);
		rootPane.setDefaultButton(exportButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
			
		populateExportPropertyLists();
		loadPreferences();
		initFileChoosers();
		
		String filePath = Paths.get(baseDirectory.getAbsolutePath(), 
				"IDTracker_export" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt").
				toString();
		outputFilleTextField.setText(filePath);
		pack();
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}
	
	private void populateExportPropertyLists() {
		
		DefaultListModel<IDTrackerFeatureIdentificationProperties> idPropsModel = 
				new DefaultListModel<IDTrackerFeatureIdentificationProperties>();
		idPropsModel.addAll(Arrays.asList(IDTrackerFeatureIdentificationProperties.values()));
		identificationDetailsList.setModel(idPropsModel);
		
		DefaultListModel<IDTrackerMSMSClusterProperties> fPropsModel = 
				new DefaultListModel<IDTrackerMSMSClusterProperties>();
		fPropsModel.addAll(Arrays.asList(IDTrackerMSMSClusterProperties.values()));
		featurePropertyList.setModel(fPropsModel);
			
		if(selectedMSMSClusterProperties != null)
			selectFeaturePropertiesListItems(selectedMSMSClusterProperties);
		else
			selectFeaturePropertiesListItems(defaultMSMSClusterProperties);
		
		if(selectedMSMSClusterIdentificationProperties != null)
			selectIdentificationPropertiesListItems(selectedMSMSClusterIdentificationProperties);
		else
			selectIdentificationPropertiesListItems(defaultMSMSClusterIdentificationProperties);		
	}
	
	protected void initFileChoosers() {

		outputFileChooser = new ImprovedFileChooser();
		outputFileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		outputFileChooser.addActionListener(this);
		outputFileChooser.setAcceptAllFileFilterUsed(true);
		outputFileChooser.setMultiSelectionEnabled(false);
		outputFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		outputFileChooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
		outputFileChooser.setCurrentDirectory(baseDirectory);
		outputFileChooser.setApproveButtonText("Set output file");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectOutputFile();

		if(e.getActionCommand().equals(MainActionCommands.RESET_IDTRACKER_DATA_EXPORT_FIELDS_COMMAND.getName()))
			resetExportFields();
	}
	
	private void selectOutputFile() {
		
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		String fileName = "IDTrackerDataExport_" + timestamp + ".txt";
		outputFileChooser.setSelectedFile(new File(fileName));			
		if(outputFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			outputFilleTextField.setText(outputFileChooser.getSelectedFile().getAbsolutePath());
			baseDirectory = outputFileChooser.getSelectedFile().getParentFile();
		}
	}
	
	private void resetExportFields() {
	
		selectedMSMSClusterProperties = defaultMSMSClusterProperties;
		selectedMSMSClusterIdentificationProperties = defaultMSMSClusterIdentificationProperties;
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory =
				new File(preferences.get(BASE_DIR, MRC2ToolBoxConfiguration.getDefaultProjectsDirectory())).
				getAbsoluteFile();
		
		//	Select fields for export
		ArrayList<IDTrackerMsFeatureProperties> storedFeatureProperties = 
				 new ArrayList<IDTrackerMsFeatureProperties>();
		ArrayList<IDTrackerFeatureIdentificationProperties> storedIdentificationProperties = 
				 new ArrayList<IDTrackerFeatureIdentificationProperties>();
		
		storedFeatureProperties = new ArrayList<IDTrackerMsFeatureProperties>();
		storedIdentificationProperties = new ArrayList<IDTrackerFeatureIdentificationProperties>();
		String msTwoFeaturePropertiesString = preferences.get(MSMS_CLUSTER_PROPERTIES, "");
		selectedMSMSClusterProperties = defaultMSMSClusterProperties;
		if(!msTwoFeaturePropertiesString.isEmpty()) {
			
			 String[] selectedMsTwoFeaturePropertiesNames =  
					 msTwoFeaturePropertiesString.split(PROPERTIES_DELIMITER);

			 for(String name : selectedMsTwoFeaturePropertiesNames) {
				 
				IDTrackerMsFeatureProperties fProperty = 					
						IDTrackerMsFeatureProperties.getPropertyByName(name);
				if(fProperty != null)
					storedFeatureProperties.add(fProperty);
			 }
			 selectedMSMSClusterProperties = 
					 storedFeatureProperties.toArray(
							 new IDTrackerMSMSClusterProperties[storedFeatureProperties.size()]);
		}		
		String msTwoIdentificationPropertiesString = preferences.get(MSMS_CLUSTER_IDENTIFICATION_PROPERTIES, "");
		selectedMSMSClusterIdentificationProperties = defaultMSMSClusterIdentificationProperties;
		if(!msTwoIdentificationPropertiesString.isEmpty()) {
			
			 String[] selectedMsTwoIdentificationPropertiesNames =  
					 msTwoIdentificationPropertiesString.split(PROPERTIES_DELIMITER);

			 for(String name : selectedMsTwoIdentificationPropertiesNames) {
				 
				 IDTrackerFeatureIdentificationProperties idProperty = 
						 IDTrackerFeatureIdentificationProperties.getPropertyByName(name);
				if(idProperty != null)
					storedIdentificationProperties.add(idProperty);
			 }
			 selectedMSMSClusterIdentificationProperties = 
					 storedIdentificationProperties.toArray(
							 new IDTrackerFeatureIdentificationProperties[storedIdentificationProperties.size()]);
		}
		selectFeaturePropertiesListItems(selectedMSMSClusterProperties);
		selectIdentificationPropertiesListItems(selectedMSMSClusterIdentificationProperties);
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIR, baseDirectory.getAbsolutePath());
		
		Collection<String> msTwoFeatureProperties = 
				Arrays.asList(selectedMSMSClusterProperties).stream().
				map(v -> v.name()).collect(Collectors.toList());
		Collection<String> msTwoIdentificationProperties = 
				Arrays.asList(selectedMSMSClusterIdentificationProperties).stream().
				map(v -> v.name()).collect(Collectors.toList());		

		msTwoFeatureProperties = featurePropertyList.getSelectedValuesList().stream().
				map(v -> v.name()).collect(Collectors.toList());;
		msTwoIdentificationProperties = identificationDetailsList.getSelectedValuesList().stream().
				map(v -> v.name()).collect(Collectors.toList());

		preferences.put(MSMS_CLUSTER_PROPERTIES, 
				StringUtils.join(msTwoFeatureProperties, PROPERTIES_DELIMITER));
		preferences.put(MSMS_CLUSTER_IDENTIFICATION_PROPERTIES, 
				StringUtils.join(msTwoIdentificationProperties, PROPERTIES_DELIMITER));
	}
	
	private void selectFeaturePropertiesListItems(
			IDTrackerMSMSClusterProperties[] selectedProperties) {
			
		featurePropertyList.clearSelection();
		ArrayList<Integer>indices = new ArrayList<Integer>();
		DefaultListModel<IDTrackerMSMSClusterProperties> model = 
				(DefaultListModel<IDTrackerMSMSClusterProperties>) featurePropertyList.getModel();
		for(IDTrackerMSMSClusterProperties item : selectedProperties) {
			int idx = model.indexOf(item);
			if(idx > -1)
				indices.add(idx);
		}
		int[] toSelect = indices.stream().mapToInt(i->i).toArray();				
		featurePropertyList.setSelectedIndices(toSelect);
	}
	
	private void selectIdentificationPropertiesListItems(
			IDTrackerFeatureIdentificationProperties[] selectedProperties) {
			
		identificationDetailsList.clearSelection();
		ArrayList<Integer>indices = new ArrayList<Integer>();
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
		return featurePropertyList.getSelectedValuesList();
	}

	public Collection<IDTrackerFeatureIdentificationProperties> getSelectedIdentificationProperties() {
		return identificationDetailsList.getSelectedValuesList();
	}
	
	public File getOutputFile() {
		
		if(outputFilleTextField.getText().trim().isEmpty())
			return null;
		
		return Paths.get(outputFilleTextField.getText()).toFile();
	}
	
	public Collection<String>validateFormParameters(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getOutputFile() == null)
			errors.add("Output file not specified");
		
		if(getSelectedMSMSClusterProperties().isEmpty())
			errors.add("No data fields for MSMS clusters selected");
		
		if(getSelectedIdentificationProperties().isEmpty())
			errors.add("No data fields for compound identification selected");
		
		return errors;
	}
}












