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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.IDTrackerDataExportParameters;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIDSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMsFeatureProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class IDTrackerDataExportDialog extends JDialog 
		implements ActionListener, ItemListener, BackedByPreferences { 

	/**
	 * 
	 */
	private static final long serialVersionUID = 412209008488860214L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("saveList", 32);
	
	protected Preferences preferences;
	public static final String BASE_DIR = "BASE_DIR";
	public static final String MS_LEVEL = "MS_LEVEL";	
	public static final String TABLE_ROW_SUBSET = "TABLE_ROW_SUBSET";
	public static final String FEATURE_SUBSET_BY_ID = "FEATURE_SUBSET_BY_ID";
	public static final String MSTWO_FEATURE_PROPERTIES = " MSTWO_FEATURE_PROPERTIES";
	public static final String MSTWO_IDENTIFICATION_PROPERTIES = "MSTWO_IDENTIFICATION_PROPERTIES";	
	public static final String REMOVE_REDUNDANT = "REMOVE_REDUNDANT";
	public static final String REDUNDANT_MZ_WINDOW = "REDUNDANT_MZ_WINDOW";
	public static final String REDUNDANT_MZ_ERROR_TYPE = "REDUNDANT_MZ_ERROR_TYPE";
	public static final String REDUNDANT_RT_WINDOW = "REDUNDANT_RT_WINDOW";		
	public static final String MSONE_FEATURE_PROPERTIES = "MSONE_FEATURE_PROPERTIES";
	public static final String MSONE_IDENTIFICATION_PROPERTIES = "MSONE_IDENTIFICATION_PROPERTIES";
	public static final String PROPERTIES_DELIMITER = "@";
	
	private final static IDTrackerMsFeatureProperties[] defaultMsTwoFeatureProperties 	
		= new IDTrackerMsFeatureProperties[] {				
				IDTrackerMsFeatureProperties.FEATURE_ID,
				IDTrackerMsFeatureProperties.RETENTION_TIME,
				IDTrackerMsFeatureProperties.PRECURSOR_MZ,
				IDTrackerMsFeatureProperties.ACQ_METHOD,
				IDTrackerMsFeatureProperties.SAMPLE_TYPE,
				IDTrackerMsFeatureProperties.SPECTRUM_ENTROPY,
				IDTrackerMsFeatureProperties.TOTAL_INTENSITY,
				IDTrackerMsFeatureProperties.ANNOTATIONS,
				IDTrackerMsFeatureProperties.FOLLOWUPS,
		};
	private final static IDTrackerFeatureIdentificationProperties[] defaultMsTwoIdentificationProperties 
		= new IDTrackerFeatureIdentificationProperties[] {				
				IDTrackerFeatureIdentificationProperties.COMPOUND_NAME,
				IDTrackerFeatureIdentificationProperties.DATABASE_ID,
				IDTrackerFeatureIdentificationProperties.SOURCE_DATABASE,
				IDTrackerFeatureIdentificationProperties.MSMS_LIBRARY,
				IDTrackerFeatureIdentificationProperties.FWD_SCORE,
				IDTrackerFeatureIdentificationProperties.REVERSE_SCORE,
				IDTrackerFeatureIdentificationProperties.MATCH_TYPE,
				IDTrackerFeatureIdentificationProperties.ID_LEVEL,				
		};	
	private final static IDTrackerMsFeatureProperties[] defaultMsOneFeatureProperties 
		= new IDTrackerMsFeatureProperties[] {
				IDTrackerMsFeatureProperties.FEATURE_ID,
				IDTrackerMsFeatureProperties.RETENTION_TIME,
				IDTrackerMsFeatureProperties.BASE_PEAK_MZ,
				IDTrackerMsFeatureProperties.ACQ_METHOD,
				IDTrackerMsFeatureProperties.SAMPLE_TYPE,
				IDTrackerMsFeatureProperties.ANNOTATIONS,
				IDTrackerMsFeatureProperties.FOLLOWUPS,
		};
	private final static IDTrackerFeatureIdentificationProperties[] defaultMsOneIdentificationProperties 
		= new IDTrackerFeatureIdentificationProperties[] {
				IDTrackerFeatureIdentificationProperties.COMPOUND_NAME,
				IDTrackerFeatureIdentificationProperties.DATABASE_ID,
				IDTrackerFeatureIdentificationProperties.SOURCE_DATABASE,
				IDTrackerFeatureIdentificationProperties.MSRT_LIB,
				IDTrackerFeatureIdentificationProperties.MASS_ERROR,
				IDTrackerFeatureIdentificationProperties.RETENTION_ERROR,
				IDTrackerFeatureIdentificationProperties.ID_LEVEL,
		};
	
	private IDTrackerMsFeatureProperties[] selectedMsOneFeatureProperties;
	private IDTrackerMsFeatureProperties[] selectedMsTwoFeatureProperties;
	private IDTrackerFeatureIdentificationProperties[] selectedMsOneIdentificationProperties;
	private IDTrackerFeatureIdentificationProperties[] selectedMsTwoIdentificationProperties;

	private JButton exportButton;
	private File baseDirectory;
	private JTextField outputFilleTextField;
	private JComboBox<MsDepth> msDepthComboBox;
	private JComboBox<TableRowSubset> featureSubsetComboBox;
	private JList<IDTrackerMsFeatureProperties> featurePropertyList;
	private JList<IDTrackerFeatureIdentificationProperties> identificationDetailsList;
	
	private final String BROWSE_COMMAND = "BROWSE_FOR_OUTPUT";
	private JComboBox idFilterComboBox;
	private JCheckBox removeRedundantCheckBox;
	private JFormattedTextField redundantMzErrorTextField;
	private JFormattedTextField rtWindowTextField;
	private JComboBox massErrorTypeComboBox;	
	private IdentificationExportSettingsPanel identificationExportSettingsPanel;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IDTrackerDataExportDialog(ActionListener listener) {
		super();
		setTitle("Export data from IDTracker");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 800));
		setPreferredSize(new Dimension(800, 800));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		identificationExportSettingsPanel = new IdentificationExportSettingsPanel();
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.gridwidth = 3;
		gbc_panel_4.insets = new Insets(0, 0, 5, 5);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 0;
		panel_1.add(identificationExportSettingsPanel, gbc_panel_4);

		JLabel lblNewLabel_3 = new JLabel("Save to");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 1;
		panel_1.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		outputFilleTextField = new JTextField();
		outputFilleTextField.setEditable(false);
		GridBagConstraints gbc_outputFilleTextField = new GridBagConstraints();
		gbc_outputFilleTextField.insets = new Insets(0, 0, 0, 5);
		gbc_outputFilleTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputFilleTextField.gridx = 1;
		gbc_outputFilleTextField.gridy = 1;
		panel_1.add(outputFilleTextField, gbc_outputFilleTextField);
		outputFilleTextField.setColumns(10);
		
		JButton outputFileBrowseButton = new JButton("Browse ...");
		outputFileBrowseButton.setActionCommand(BROWSE_COMMAND);
		outputFileBrowseButton.addActionListener(this);
		GridBagConstraints gbc_outputFileBrowseButton = new GridBagConstraints();
		gbc_outputFileBrowseButton.anchor = GridBagConstraints.BELOW_BASELINE;
		gbc_outputFileBrowseButton.gridx = 2;
		gbc_outputFileBrowseButton.gridy = 1;
		panel_1.add(outputFileBrowseButton, gbc_outputFileBrowseButton);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(panel_2, BorderLayout.NORTH);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		JLabel lblNewLabel = new JLabel("MS level ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_2.add(lblNewLabel, gbc_lblNewLabel);
		
		msDepthComboBox = new JComboBox<MsDepth>(
				new DefaultComboBoxModel<MsDepth>(new MsDepth[] {MsDepth.MS1, MsDepth.MS2}));
		msDepthComboBox.setPreferredSize(new Dimension(100, 25));
		msDepthComboBox.setSize(new Dimension(100, 25));
		msDepthComboBox.setMinimumSize(new Dimension(100, 25));
		msDepthComboBox.addItemListener(this);
		GridBagConstraints gbc_msDepthComboBox = new GridBagConstraints();
		gbc_msDepthComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_msDepthComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_msDepthComboBox.gridx = 1;
		gbc_msDepthComboBox.gridy = 0;
		panel_2.add(msDepthComboBox, gbc_msDepthComboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Feature subset");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel_2.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		featureSubsetComboBox = new JComboBox<TableRowSubset>(
				new DefaultComboBoxModel<TableRowSubset>(TableRowSubset.values()));
		featureSubsetComboBox.setMinimumSize(new Dimension(200, 25));
		featureSubsetComboBox.setPreferredSize(new Dimension(200, 25));
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.gridwidth = 2;
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 3;
		gbc_comboBox_1.gridy = 0;
		panel_2.add(featureSubsetComboBox, gbc_comboBox_1);
		
		JLabel lblNewLabel_31 = new JLabel("Filter by ID");
		GridBagConstraints gbc_lblNewLabel_31 = new GridBagConstraints();
		gbc_lblNewLabel_31.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_31.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_31.gridx = 5;
		gbc_lblNewLabel_31.gridy = 0;
		panel_2.add(lblNewLabel_31, gbc_lblNewLabel_31);
		
		idFilterComboBox = new JComboBox<FeatureSubsetByIdentification>(
				new DefaultComboBoxModel<FeatureSubsetByIdentification>(
						FeatureSubsetByIdentification.values()));
		idFilterComboBox.setPreferredSize(new Dimension(80, 25));
		idFilterComboBox.setMinimumSize(new Dimension(80, 25));
		GridBagConstraints gbc_idFilterComboBox = new GridBagConstraints();
		gbc_idFilterComboBox.gridwidth = 2;
		gbc_idFilterComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_idFilterComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_idFilterComboBox.gridx = 6;
		gbc_idFilterComboBox.gridy = 0;
		panel_2.add(idFilterComboBox, gbc_idFilterComboBox);
		
		removeRedundantCheckBox = new JCheckBox("Remove redundant features");
		GridBagConstraints gbc_removeRedundantCheckBox = new GridBagConstraints();
		gbc_removeRedundantCheckBox.anchor = GridBagConstraints.WEST;
		gbc_removeRedundantCheckBox.gridwidth = 2;
		gbc_removeRedundantCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_removeRedundantCheckBox.gridx = 0;
		gbc_removeRedundantCheckBox.gridy = 1;
		panel_2.add(removeRedundantCheckBox, gbc_removeRedundantCheckBox);
		
		JLabel lblNewLabel_4 = new JLabel("M/Z window");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 1;
		panel_2.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		redundantMzErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		redundantMzErrorTextField.setColumns(10);
		GridBagConstraints gbc_redundantMzErrorTextField = new GridBagConstraints();
		gbc_redundantMzErrorTextField.insets = new Insets(0, 0, 0, 5);
		gbc_redundantMzErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_redundantMzErrorTextField.gridx = 3;
		gbc_redundantMzErrorTextField.gridy = 1;
		panel_2.add(redundantMzErrorTextField, gbc_redundantMzErrorTextField);
		
		massErrorTypeComboBox = new JComboBox(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		GridBagConstraints gbc_massErrorTypeComboBox = new GridBagConstraints();
		gbc_massErrorTypeComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_massErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTypeComboBox.gridx = 4;
		gbc_massErrorTypeComboBox.gridy = 1;
		panel_2.add(massErrorTypeComboBox, gbc_massErrorTypeComboBox);
		
		JLabel lblNewLabel_6 = new JLabel("RT window");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_6.gridx = 5;
		gbc_lblNewLabel_6.gridy = 1;
		panel_2.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		rtWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtWindowTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 6;
		gbc_formattedTextField.gridy = 1;
		panel_2.add(rtWindowTextField, gbc_formattedTextField);
		
		JLabel lblNewLabel_7 = new JLabel("min");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_7.gridx = 7;
		gbc_lblNewLabel_7.gridy = 1;
		panel_2.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new EmptyBorder(0, 10, 0, 10));
		panel.add(panel_3, BorderLayout.CENTER);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0, 0};
		gbl_panel_3.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		featurePropertyList = new JList<IDTrackerMsFeatureProperties>();
		JScrollPane scrollPane = new JScrollPane(featurePropertyList);
		scrollPane.setBorder(new TitledBorder(null, "Feature properties", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
			
		loadPreferences();
		
		String filePath = Paths.get(baseDirectory.getAbsolutePath(), 
				"IDTracker_export_" + 
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

	@Override
	public void itemStateChanged(ItemEvent event) {

		if (event.getStateChange() == ItemEvent.SELECTED) {

			MsDepth depth = (MsDepth)msDepthComboBox.getSelectedItem();
			populateExportPropertyLists(depth);
			//	TODO toggle MSMS results filters
		}
	}
	
	private void populateExportPropertyLists(MsDepth msDepth) {
		
		//	Feature properties
		DefaultListModel<IDTrackerMsFeatureProperties> fPropsModel = 
				new DefaultListModel<IDTrackerMsFeatureProperties>();
		fPropsModel.addAll(Arrays.asList(IDTrackerMsFeatureProperties.values()));
		featurePropertyList.setModel(fPropsModel);

		//	ID properties
		DefaultListModel<IDTrackerFeatureIdentificationProperties> idPropsModel = 
				new DefaultListModel<IDTrackerFeatureIdentificationProperties>();
		idPropsModel.addAll(Arrays.asList(IDTrackerFeatureIdentificationProperties.values()));
		identificationDetailsList.setModel(idPropsModel);
		
		if(msDepth.equals(MsDepth.MS1)) {
			
			if(selectedMsOneFeatureProperties != null)
				selectFeaturePropertiesListItems(selectedMsOneFeatureProperties);
			else
				selectFeaturePropertiesListItems(defaultMsOneFeatureProperties);
			
			if(selectedMsOneIdentificationProperties != null)
				selectIdentificationPropertiesListItems(selectedMsOneIdentificationProperties);
			else
				selectIdentificationPropertiesListItems(defaultMsOneIdentificationProperties);
		}
		if(msDepth.equals(MsDepth.MS2)) {
			
			if(selectedMsTwoFeatureProperties != null)
				selectFeaturePropertiesListItems(selectedMsTwoFeatureProperties);
			else
				selectFeaturePropertiesListItems(defaultMsTwoFeatureProperties);
			
			if(selectedMsTwoIdentificationProperties != null)
				selectIdentificationPropertiesListItems(selectedMsTwoIdentificationProperties);
			else
				selectIdentificationPropertiesListItems(defaultMsTwoIdentificationProperties);
		}
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
		fc.setTitle("Export IDTracker data to text file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Set output file");
		String defaultFileName = "IDTracker_export_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt";
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File exportFile  = fc.getSelectedFile();
			outputFilleTextField.setText(exportFile.getAbsolutePath());
			baseDirectory = exportFile.getParentFile();
		}
	}
	
	private void resetExportFields() {
		
		selectedMsOneFeatureProperties = defaultMsOneFeatureProperties;
		selectedMsOneIdentificationProperties = defaultMsOneIdentificationProperties;		
		selectedMsTwoFeatureProperties = defaultMsTwoFeatureProperties;
		selectedMsTwoIdentificationProperties = defaultMsTwoIdentificationProperties;
		if(msDepthComboBox.getSelectedItem().equals(MsDepth.MS1)) {
			selectFeaturePropertiesListItems(selectedMsOneFeatureProperties);
			selectIdentificationPropertiesListItems(selectedMsOneIdentificationProperties);
		}
		if(msDepthComboBox.getSelectedItem().equals(MsDepth.MS2)) {
			selectFeaturePropertiesListItems(selectedMsTwoFeatureProperties);
			selectIdentificationPropertiesListItems(selectedMsTwoIdentificationProperties);
		}
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory =
				new File(preferences.get(BASE_DIR, MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory())).
				getAbsoluteFile();
		
		msDepthComboBox.removeItemListener(this);
		MsDepth depth = MsDepth.getMsDepthByName(preferences.get(MS_LEVEL, MsDepth.MS2.name()));
		msDepthComboBox.setSelectedItem(depth);
		msDepthComboBox.addItemListener(this);		
		populateExportPropertyLists(depth);
		
		TableRowSubset subset = TableRowSubset.getSubsetByName(
				preferences.get(TABLE_ROW_SUBSET, TableRowSubset.ALL.name()));
		featureSubsetComboBox.setSelectedItem(subset);
		
		FeatureSubsetByIdentification idSubset = 
				FeatureSubsetByIdentification.getOptionByName(preferences.get(FEATURE_SUBSET_BY_ID, 
						FeatureSubsetByIdentification.ALL.name()));
		idFilterComboBox.setSelectedItem(idSubset);
		
		//	Redundant feature filtering
		removeRedundantCheckBox.setSelected(preferences.getBoolean(REMOVE_REDUNDANT, false));
		redundantMzErrorTextField.setText(Double.toString(preferences.getDouble(REDUNDANT_MZ_WINDOW, 20.0d)));
		massErrorTypeComboBox.setSelectedItem(
				MassErrorType.getTypeByName(preferences.get(REDUNDANT_MZ_ERROR_TYPE, MassErrorType.ppm.name())));
		rtWindowTextField.setText(Double.toString(preferences.getDouble(REDUNDANT_RT_WINDOW, 0.1d)));
		
		//	Select fields for export
		ArrayList<IDTrackerMsFeatureProperties> storedFeatureProperties = 
				 new ArrayList<IDTrackerMsFeatureProperties>();
		ArrayList<IDTrackerFeatureIdentificationProperties> storedIdentificationProperties = 
				 new ArrayList<IDTrackerFeatureIdentificationProperties>();

		String msOneFeaturePropertiesString = preferences.get(MSONE_FEATURE_PROPERTIES, "");
		selectedMsOneFeatureProperties = defaultMsOneFeatureProperties;
		if(!msOneFeaturePropertiesString.isEmpty()) {
			 String[] selectedMsOneFeaturePropertiesNames =  
					 msOneFeaturePropertiesString.split(PROPERTIES_DELIMITER);

			 for(String name : selectedMsOneFeaturePropertiesNames) {
				 
				IDTrackerMsFeatureProperties fProperty = 					
						IDTrackerMsFeatureProperties.getPropertyByName(name);
				if(fProperty != null)
					storedFeatureProperties.add(fProperty);
			 }
			 selectedMsOneFeatureProperties = 
					 storedFeatureProperties.toArray(
							 new IDTrackerMsFeatureProperties[storedFeatureProperties.size()]);
		}	
		String msOneIdentificationPropertiesString = preferences.get(MSONE_IDENTIFICATION_PROPERTIES, "");
		selectedMsOneIdentificationProperties = defaultMsOneIdentificationProperties;
		if(!msOneIdentificationPropertiesString.isEmpty()) {
			
			 String[] selectedMsOneIdentificationPropertiesNames =  
					 msOneIdentificationPropertiesString.split(PROPERTIES_DELIMITER);

			 for(String name : selectedMsOneIdentificationPropertiesNames) {
				 
				 IDTrackerFeatureIdentificationProperties idProperty = 
						 IDTrackerFeatureIdentificationProperties.getPropertyByName(name);
				if(idProperty != null)
					storedIdentificationProperties.add(idProperty);
			 }
			 selectedMsOneIdentificationProperties = 
					 storedIdentificationProperties.toArray(
							 new IDTrackerFeatureIdentificationProperties[storedIdentificationProperties.size()]);
		}			
		storedFeatureProperties = new ArrayList<IDTrackerMsFeatureProperties>();
		storedIdentificationProperties = new ArrayList<IDTrackerFeatureIdentificationProperties>();
		String msTwoFeaturePropertiesString = preferences.get(MSTWO_FEATURE_PROPERTIES, "");
		selectedMsTwoFeatureProperties = defaultMsTwoFeatureProperties;
		if(!msTwoFeaturePropertiesString.isEmpty()) {
			
			 String[] selectedMsTwoFeaturePropertiesNames =  
					 msTwoFeaturePropertiesString.split(PROPERTIES_DELIMITER);

			 for(String name : selectedMsTwoFeaturePropertiesNames) {
				 
				IDTrackerMsFeatureProperties fProperty = 					
						IDTrackerMsFeatureProperties.getPropertyByName(name);
				if(fProperty != null)
					storedFeatureProperties.add(fProperty);
			 }
			 selectedMsTwoFeatureProperties = 
					 storedFeatureProperties.toArray(
							 new IDTrackerMsFeatureProperties[storedFeatureProperties.size()]);
		}		
		String msTwoIdentificationPropertiesString = preferences.get(MSTWO_IDENTIFICATION_PROPERTIES, "");
		selectedMsTwoIdentificationProperties = defaultMsTwoIdentificationProperties;
		if(!msTwoIdentificationPropertiesString.isEmpty()) {
			
			 String[] selectedMsTwoIdentificationPropertiesNames =  
					 msTwoIdentificationPropertiesString.split(PROPERTIES_DELIMITER);

			 for(String name : selectedMsTwoIdentificationPropertiesNames) {
				 
				 IDTrackerFeatureIdentificationProperties idProperty = 
						 IDTrackerFeatureIdentificationProperties.getPropertyByName(name);
				if(idProperty != null)
					storedIdentificationProperties.add(idProperty);
			 }
			 selectedMsTwoIdentificationProperties = 
					 storedIdentificationProperties.toArray(
							 new IDTrackerFeatureIdentificationProperties[storedIdentificationProperties.size()]);
		}		
		if(msDepthComboBox.getSelectedItem().equals(MsDepth.MS1)) {
			selectFeaturePropertiesListItems(selectedMsOneFeatureProperties);
			selectIdentificationPropertiesListItems(selectedMsOneIdentificationProperties);
		}
		if(msDepthComboBox.getSelectedItem().equals(MsDepth.MS2)) {
			selectFeaturePropertiesListItems(selectedMsTwoFeatureProperties);
			selectIdentificationPropertiesListItems(selectedMsTwoIdentificationProperties);
		}
		identificationExportSettingsPanel.loadPreferences(preferences);
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIR, baseDirectory.getAbsolutePath());
		MsDepth depth = (MsDepth)msDepthComboBox.getSelectedItem();
		preferences.put(MS_LEVEL, depth.name());
		
		TableRowSubset subset = (TableRowSubset)featureSubsetComboBox.getSelectedItem();
		preferences.put(TABLE_ROW_SUBSET, subset.name());
		
		FeatureSubsetByIdentification idSubset = 
				(FeatureSubsetByIdentification)idFilterComboBox.getSelectedItem();
		preferences.put(FEATURE_SUBSET_BY_ID, idSubset.name());		

		preferences.putBoolean(REMOVE_REDUNDANT, removeRedundantCheckBox.isSelected());
		
		if(!redundantMzErrorTextField.getText().trim().isEmpty())
			preferences.putDouble(REDUNDANT_MZ_WINDOW, Double.parseDouble(redundantMzErrorTextField.getText()));
		
		if(massErrorTypeComboBox.getSelectedItem() != null)
			preferences.put(REDUNDANT_MZ_ERROR_TYPE, ((MassErrorType)massErrorTypeComboBox.getSelectedItem()).name());
		
		if(!rtWindowTextField.getText().trim().isEmpty())
			preferences.putDouble(REDUNDANT_RT_WINDOW, Double.parseDouble(rtWindowTextField.getText()));
		
		Collection<String> msOneFeatureProperties = 
				Arrays.asList(selectedMsOneFeatureProperties).stream().
				map(v -> v.name()).collect(Collectors.toList());
		Collection<String> msTwoFeatureProperties = 
				Arrays.asList(selectedMsTwoFeatureProperties).stream().
				map(v -> v.name()).collect(Collectors.toList());
		Collection<String> msOneIdentificationProperties = 
				Arrays.asList(selectedMsOneIdentificationProperties).stream().
				map(v -> v.name()).collect(Collectors.toList());
		Collection<String> msTwoIdentificationProperties = 
				Arrays.asList(selectedMsTwoIdentificationProperties).stream().
				map(v -> v.name()).collect(Collectors.toList());
		
		if(depth.equals(MsDepth.MS1)) {
			msOneFeatureProperties = featurePropertyList.getSelectedValuesList().stream().
					map(v -> v.name()).collect(Collectors.toList());
			msOneIdentificationProperties = identificationDetailsList.getSelectedValuesList().stream().
					map(v -> v.name()).collect(Collectors.toList());
		}
		if(depth.equals(MsDepth.MS2)) {
			msTwoFeatureProperties = featurePropertyList.getSelectedValuesList().stream().
					map(v -> v.name()).collect(Collectors.toList());
			msTwoIdentificationProperties = identificationDetailsList.getSelectedValuesList().stream().
					map(v -> v.name()).collect(Collectors.toList());
		}
		preferences.put(MSONE_FEATURE_PROPERTIES, 
				StringUtils.join(msOneFeatureProperties, PROPERTIES_DELIMITER));
		preferences.put(MSONE_IDENTIFICATION_PROPERTIES, 
				StringUtils.join(msOneIdentificationProperties, PROPERTIES_DELIMITER));
		preferences.put(MSTWO_FEATURE_PROPERTIES, 
				StringUtils.join(msTwoFeatureProperties, PROPERTIES_DELIMITER));
		preferences.put(MSTWO_IDENTIFICATION_PROPERTIES, 
				StringUtils.join(msTwoIdentificationProperties, PROPERTIES_DELIMITER));
		
		Collection<MSMSMatchType>selectedMatchTypes = getMSMSSearchTypes();
		preferences.putBoolean(
				IdentificationExportSettingsPanel.INCLUDE_NORMAL_MATCH, 
				selectedMatchTypes.contains(MSMSMatchType.Regular));
		preferences.putBoolean(
				IdentificationExportSettingsPanel.INCLUDE_IN_SOURCE_MATCH, 
				selectedMatchTypes.contains(MSMSMatchType.InSource));
		preferences.putBoolean(
				IdentificationExportSettingsPanel.INCLUDE_HYBRID_MATCH, 
				selectedMatchTypes.contains(MSMSMatchType.Hybrid));
		preferences.putDouble(IdentificationExportSettingsPanel.MIN_SCORE, getMinimalMSMSScore());		
		preferences.put(IdentificationExportSettingsPanel.SCORING_PARAMETER, getMSMSScoringParameter().name());
		preferences.put(IdentificationExportSettingsPanel.IDS_PER_FEATURE, getFeatureIDSubset().name());		
		preferences.putBoolean(IdentificationExportSettingsPanel.EXCLUDE_IF_NO_IDS, excludeIfNoIdsLeft());
	}
	
	private void selectFeaturePropertiesListItems(
			IDTrackerMsFeatureProperties[] selectedProperties) {
			
		featurePropertyList.clearSelection();
		ArrayList<Integer>indices = new ArrayList<Integer>();
		DefaultListModel<IDTrackerMsFeatureProperties> model = 
				(DefaultListModel<IDTrackerMsFeatureProperties>) featurePropertyList.getModel();
		for(IDTrackerMsFeatureProperties item : selectedProperties) {
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

	public Collection<IDTrackerMsFeatureProperties> getSelectedFeatureProperties() {
		return featurePropertyList.getSelectedValuesList();
	}

	public Collection<IDTrackerFeatureIdentificationProperties> getSelectedIdentificationProperties() {
		return identificationDetailsList.getSelectedValuesList();
	}
	
	public MsDepth getMsLevel() {		
		return (MsDepth)msDepthComboBox.getSelectedItem();
	}
	
	public TableRowSubset getFeatureSubset() {
		return (TableRowSubset)featureSubsetComboBox.getSelectedItem();
	}
	
	public FeatureSubsetByIdentification getFeatureSubsetByIdentification(){
		 return (FeatureSubsetByIdentification)idFilterComboBox.getSelectedItem();
	}
	
	public boolean removeRedundant() {
		return removeRedundantCheckBox.isSelected();
	}
	
	public double getRedundantMzWindow() {
		
		if(!redundantMzErrorTextField.getText().trim().isEmpty())
			return Double.parseDouble(redundantMzErrorTextField.getText());
		else 
			return 0.0;
	}
	
	public MassErrorType getRedundantMzErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();
	}
	
	public double getRedundantRTWindow() {
		
		if(!rtWindowTextField.getText().trim().isEmpty())
			return Double.parseDouble(rtWindowTextField.getText());
		else 
			return 0.0;
	}

	public File getOutputFile() {
		
		if(outputFilleTextField.getText().trim().isEmpty())
			return null;
		
		return Paths.get(outputFilleTextField.getText()).toFile();
	}
	
	public MSMSScoringParameter getMSMSScoringParameter() {
		return identificationExportSettingsPanel.getMSMSScoringParameter();
	}
	
	public double getMinimalMSMSScore() {	
		return identificationExportSettingsPanel.getMinimalMSMSScore();
	}
	
	public FeatureIDSubset getFeatureIDSubset() {		
		return identificationExportSettingsPanel.getFeatureIDSubset();
	}
	
	public Collection<MSMSMatchType>getMSMSSearchTypes(){
		return identificationExportSettingsPanel.getMSMSSearchTypes();
	}
	
	public boolean excludeIfNoIdsLeft() {
		return identificationExportSettingsPanel.excludeIfNoIdsLeft();
	}
	
	public IDTrackerDataExportParameters getIDTrackerDataExportParameters() {
		
		IDTrackerDataExportParameters params = new IDTrackerDataExportParameters(
				getMsLevel(), 
				getSelectedFeatureProperties(),
				getSelectedIdentificationProperties(), 
				removeRedundant(),
				getRedundantMzWindow(), 
				getRedundantMzErrorType(), 
				getRedundantRTWindow(),
				getMSMSScoringParameter(), 
				getMinimalMSMSScore(), 
				getFeatureIDSubset(),
				getMSMSSearchTypes(),
				excludeIfNoIdsLeft());
		return params;
	}	
	
	public Collection<String>validateFormParameters(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getOutputFile() == null)
			errors.add("Output file not specified");
		
		if(getSelectedIdentificationProperties().isEmpty())
			errors.add("No data fields for compound identification selected");
		
		if(getSelectedFeatureProperties().isEmpty())
			errors.add("No data fields for feature selected");
		
		if(removeRedundant()) {
			
			if(getRedundantMzWindow() == 0.0)
				errors.add("MZ window value not valid");
			
			if(getRedundantMzErrorType() == null)
				errors.add("MZ error type not specified");
			
			if(getRedundantRTWindow() == 0.0)
				errors.add("RT window value not valid");
		}		
		return errors;
	}
}












