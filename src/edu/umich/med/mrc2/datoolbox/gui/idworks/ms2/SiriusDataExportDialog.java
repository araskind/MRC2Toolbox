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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2;

import java.awt.BorderLayout;
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
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class SiriusDataExportDialog extends JDialog implements ActionListener, BackedByPreferences{


	/**
	 * 
	 */
	private static final long serialVersionUID = -676595225766284200L;

	private static final Icon siriusIcon = GuiUtils.getIcon("sirius", 32);

	private static final String BROWSE_FOR_OUTPUT_DIR = "BROWSE_FOR_OUTPUT_DIR";

	private Preferences preferences;
	public static final String PREFS_NODE = SiriusDataExportDialog.class.getName();
	public static final String OUTPUT_DIR = "OUTPUT_DIR";
	public static final String MASS_WINDOW_PPM = "MASS_WINDOW_PPM";
	public static final String RT_WINDOW_MIN = "RT_WINDOW_MIN";
	public static final String TABLE_ROW_SUBSET = "TABLE_ROW_SUBSET";
	public static final String FEATURE_SUBSET_BY_ID = "FEATURE_SUBSET_BY_ID";
	
	private File outputDir;
	private JFormattedTextField mzErrorTextField;
	private JFormattedTextField rtErrorTextField;

	private JTextField outputFilleTextField;
	private JComboBox<TableRowSubset> featureSubsetComboBox;
	private JComboBox<FeatureSubsetByIdentification> idFilterComboBox;

	public SiriusDataExportDialog(ActionListener listener) {

		super();
		setIconImage(((ImageIcon) siriusIcon).getImage());

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(600, 200));
		setPreferredSize(new Dimension(600, 200));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel1 = new JPanel();
		panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel1, BorderLayout.CENTER);
		GridBagLayout gbl_panel1 = new GridBagLayout();
		gbl_panel1.columnWidths = new int[]{0, 168, 78, 67, 0, 0, 0};
		gbl_panel1.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel1.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0};
		panel1.setLayout(gbl_panel1);
		
		JLabel lblNewLabel = new JLabel("Group by precurson within m/z window of  ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel1.add(lblNewLabel, gbc_lblNewLabel);
		
		mzErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		mzErrorTextField.setColumns(10);
		GridBagConstraints gbc_mzErrorTextField = new GridBagConstraints();
		gbc_mzErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzErrorTextField.gridx = 2;
		gbc_mzErrorTextField.gridy = 0;
		panel1.add(mzErrorTextField, gbc_mzErrorTextField);
		
		JLabel lblNewLabel_1 = new JLabel("ppm");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 3;
		gbc_lblNewLabel_1.gridy = 0;
		panel1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Group by precurson within RT window of  ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.gridwidth = 2;
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel1.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		rtErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtErrorTextField.setColumns(10);
		GridBagConstraints gbc_rtErrorTextField = new GridBagConstraints();
		gbc_rtErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtErrorTextField.gridx = 2;
		gbc_rtErrorTextField.gridy = 1;
		panel1.add(rtErrorTextField, gbc_rtErrorTextField);
		
		JLabel lblNewLabel_3 = new JLabel("min");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.gridx = 3;
		gbc_lblNewLabel_3.gridy = 1;
		panel1.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		JLabel lblNewLabel_5 = new JLabel("Feature subset");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 2;
		panel1.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		featureSubsetComboBox = new JComboBox<TableRowSubset>(
				new DefaultComboBoxModel<TableRowSubset>(TableRowSubset.values()));
		featureSubsetComboBox.setMinimumSize(new Dimension(200, 25));
		featureSubsetComboBox.setPreferredSize(new Dimension(200, 25));
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 2;
		panel1.add(featureSubsetComboBox, gbc_comboBox_1);
		
		JLabel lblNewLabel_6 = new JLabel("Filter by ID");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 2;
		gbc_lblNewLabel_6.gridy = 2;
		panel1.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		idFilterComboBox = new JComboBox<FeatureSubsetByIdentification>(
				new DefaultComboBoxModel<FeatureSubsetByIdentification>(FeatureSubsetByIdentification.values()));
		idFilterComboBox.setPreferredSize(new Dimension(80, 25));
		idFilterComboBox.setMinimumSize(new Dimension(80, 25));
		GridBagConstraints gbc_idFilterComboBox = new GridBagConstraints();
		gbc_idFilterComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_idFilterComboBox.gridwidth = 3;
		gbc_idFilterComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_idFilterComboBox.gridx = 3;
		gbc_idFilterComboBox.gridy = 2;
		panel1.add(idFilterComboBox, gbc_idFilterComboBox);
		
		JLabel lblNewLabel_4 = new JLabel("Save to");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 3;
		panel1.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		JButton outputFileBrowseButton = new JButton("Browse ...");
		outputFileBrowseButton.setActionCommand(BROWSE_FOR_OUTPUT_DIR);
		outputFileBrowseButton.addActionListener(this);
		
		outputFilleTextField = new JTextField();
		outputFilleTextField.setEditable(false);
		GridBagConstraints gbc_outputFilleTextField = new GridBagConstraints();
		gbc_outputFilleTextField.gridwidth = 4;
		gbc_outputFilleTextField.insets = new Insets(0, 0, 0, 5);
		gbc_outputFilleTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputFilleTextField.gridx = 1;
		gbc_outputFilleTextField.gridy = 3;
		panel1.add(outputFilleTextField, gbc_outputFilleTextField);
		outputFilleTextField.setColumns(10);
		GridBagConstraints gbc_outputFileBrowseButton = new GridBagConstraints();
		gbc_outputFileBrowseButton.anchor = GridBagConstraints.BELOW_BASELINE;
		gbc_outputFileBrowseButton.gridx = 5;
		gbc_outputFileBrowseButton.gridy = 3;
		panel1.add(outputFileBrowseButton, gbc_outputFileBrowseButton);	
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		JButton exportButton = new JButton("Export data");
		exportButton.setActionCommand(
				MainActionCommands.EXPORT_FEATURES_TO_SIRIUS_MS_COMMAND.getName());
		exportButton.addActionListener(listener);
		panel.add(exportButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(exportButton);
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(exportButton);
		
		loadPreferences();
		pack();	
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_FOR_OUTPUT_DIR)) {
			selectOutputFile();
		}
	}	
	
	private void selectOutputFile() {
		
		JnaFileChooser fc = new JnaFileChooser(outputDir);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter(MsLibraryFormat.SIRIUS_MS.getName(), 
				MsLibraryFormat.SIRIUS_MS.getFileExtension());
		fc.setTitle("Export IDTracker data to text file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Set output file");
		String defaultFileName = "MSMS_FEATURES_FOR_SIRIUS_INTERPRETATION_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) 
				+ "." + MsLibraryFormat.SIRIUS_MS.getFileExtension();
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File exportFile  = fc.getSelectedFile();
			outputFilleTextField.setText(exportFile.getAbsolutePath());
			outputDir = exportFile.getParentFile();
			savePreferences();
		}
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	public double getMassWindow() {
		return Double.parseDouble(mzErrorTextField.getText());
	}

	public double getRetentionWindow() {
		return Double.parseDouble(rtErrorTextField.getText());
	}
	
	public TableRowSubset getFeatureSubset() {
		return (TableRowSubset)featureSubsetComboBox.getSelectedItem();
	}
	
	public FeatureSubsetByIdentification getFeatureSubsetByIdentification(){
		 return (FeatureSubsetByIdentification)idFilterComboBox.getSelectedItem();
	}
	
	public File getOutputFile() {
		
		if(outputFilleTextField.getText().trim().isEmpty())
			return null;
		
		return Paths.get(outputFilleTextField.getText()).toFile();
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;

		double massError = preferences.getDouble(MASS_WINDOW_PPM, 20.0d);
		mzErrorTextField.setText(Double.toString(massError));

		double rtWindow = preferences.getDouble(RT_WINDOW_MIN, 0.05d);
		rtErrorTextField.setText(Double.toString(rtWindow));
		
		TableRowSubset subset = TableRowSubset.valueOf(preferences.get(TABLE_ROW_SUBSET, TableRowSubset.ALL.name()));
		featureSubsetComboBox.setSelectedItem(subset);
		
		FeatureSubsetByIdentification idSubset = 
				FeatureSubsetByIdentification.valueOf(preferences.get(FEATURE_SUBSET_BY_ID, 
						FeatureSubsetByIdentification.ALL.name()));		
		idFilterComboBox.setSelectedItem(idSubset);
		
		outputDir = Paths.get(preferences.get(OUTPUT_DIR, MRC2ToolBoxCore.dataDir + File.separator + "Sirius")).toFile();
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());

		if(!mzErrorTextField.getText().isEmpty())
			preferences.putDouble(MASS_WINDOW_PPM, Double.parseDouble(mzErrorTextField.getText()));

		if(!rtErrorTextField.getText().isEmpty())
			preferences.putDouble(RT_WINDOW_MIN, Double.parseDouble(rtErrorTextField.getText()));

		TableRowSubset subset = (TableRowSubset)featureSubsetComboBox.getSelectedItem();
		featureSubsetComboBox.setSelectedItem(subset);
		preferences.put(TABLE_ROW_SUBSET, subset.name());
		
		FeatureSubsetByIdentification idSubset = 
				(FeatureSubsetByIdentification)idFilterComboBox.getSelectedItem();
		preferences.put(FEATURE_SUBSET_BY_ID, idSubset.name());
		
		if(outputDir != null)
			preferences.put(OUTPUT_DIR, outputDir.getAbsolutePath());
	}
}
