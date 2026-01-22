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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureSubsetDialog extends JDialog implements ItemListener, ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 1633596808735466315L;
	
	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final String BROWSE_COMMAND = "BROWSE_COMMAND";
	private File baseDirectory;
	private File featureListFile;

	private static final Icon newSubsetIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon editSubsetIcon = GuiUtils.getIcon("editCollection", 32);

	private JTextField subsetNameTextField;
	private MsFeatureSet activeSet;
	// private SubsetFeaturesTable featuresTable;
	private JComboBox<MsFeatureSet> featureSetComboBox;
	private JComboBox<TableRowSubset> tableRowChoiceComboBox;
	private JLabel numFeaturesLabel;
	private JRadioButton copyFromExistingSubsetRadioButton;
	private JRadioButton addFromFeatureTableRadioButton;
	private JRadioButton createFromFileRadioButton;
	private JTextField featureListFileTextField;

	private JButton browseButton;

	public FeatureSubsetDialog(MsFeatureSet msFeatureSet, ActionListener listener) {

		super();
		this.activeSet = msFeatureSet;
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(new Dimension(500, 300));
		setPreferredSize(new Dimension(500, 300));

		JPanel subsetInfoPanel = new JPanel();
		subsetInfoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(subsetInfoPanel, BorderLayout.NORTH);

		GridBagLayout gbl_subsetInfoPanel = new GridBagLayout();
		gbl_subsetInfoPanel.columnWidths = new int[] { 0, 134, 0, 0 };
		gbl_subsetInfoPanel.rowHeights = new int[] { 30, 0, 0, 30, 0, 0, 0, 0 };
		gbl_subsetInfoPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0 };
		gbl_subsetInfoPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		subsetInfoPanel.setLayout(gbl_subsetInfoPanel);

		JLabel lblName = new JLabel("Name ");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		subsetInfoPanel.add(lblName, gbc_lblName);

		JLabel lblOfFeatures = new JLabel("# of features");
		GridBagConstraints gbc_lblOfFeatures = new GridBagConstraints();
		gbc_lblOfFeatures.anchor = GridBagConstraints.EAST;
		gbc_lblOfFeatures.insets = new Insets(0, 0, 5, 5);
		gbc_lblOfFeatures.gridx = 2;
		gbc_lblOfFeatures.gridy = 0;
		subsetInfoPanel.add(lblOfFeatures, gbc_lblOfFeatures);

		numFeaturesLabel = new JLabel("");
		numFeaturesLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_numFeaturesLabel = new GridBagConstraints();
		gbc_numFeaturesLabel.insets = new Insets(0, 0, 5, 0);
		gbc_numFeaturesLabel.anchor = GridBagConstraints.WEST;
		gbc_numFeaturesLabel.gridx = 3;
		gbc_numFeaturesLabel.gridy = 0;
		subsetInfoPanel.add(numFeaturesLabel, gbc_numFeaturesLabel);

		subsetNameTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 4;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		subsetInfoPanel.add(subsetNameTextField, gbc_textField);
		subsetNameTextField.setColumns(10);
		
		ButtonGroup bg = new ButtonGroup();

		addFromFeatureTableRadioButton = 
				new JRadioButton("Add from features table: ");
		bg.add(addFromFeatureTableRadioButton);
		GridBagConstraints gbc_rdbtnNewRadioButton = new GridBagConstraints();
		gbc_rdbtnNewRadioButton.anchor = GridBagConstraints.WEST;
		gbc_rdbtnNewRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnNewRadioButton.gridx = 0;
		gbc_rdbtnNewRadioButton.gridy = 2;
		subsetInfoPanel.add(addFromFeatureTableRadioButton, gbc_rdbtnNewRadioButton);

		tableRowChoiceComboBox = new JComboBox<TableRowSubset>();
		tableRowChoiceComboBox.setModel(
				new DefaultComboBoxModel<TableRowSubset>(TableRowSubset.values()));
		tableRowChoiceComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_tableRowChoiceComboBox = new GridBagConstraints();
		gbc_tableRowChoiceComboBox.gridwidth = 3;
		gbc_tableRowChoiceComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_tableRowChoiceComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_tableRowChoiceComboBox.gridx = 1;
		gbc_tableRowChoiceComboBox.gridy = 2;
		subsetInfoPanel.add(tableRowChoiceComboBox, gbc_tableRowChoiceComboBox);

		copyFromExistingSubsetRadioButton = 
				new JRadioButton("Copy all from existing feature subset: ");
		bg.add(copyFromExistingSubsetRadioButton);
		GridBagConstraints gbc_rdbtnNewRadioButton_1 = new GridBagConstraints();
		gbc_rdbtnNewRadioButton_1.anchor = GridBagConstraints.WEST;
		gbc_rdbtnNewRadioButton_1.gridwidth = 2;
		gbc_rdbtnNewRadioButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnNewRadioButton_1.gridx = 0;
		gbc_rdbtnNewRadioButton_1.gridy = 3;
		subsetInfoPanel.add(copyFromExistingSubsetRadioButton, gbc_rdbtnNewRadioButton_1);

		featureSetComboBox = new JComboBox<MsFeatureSet>();
		GridBagConstraints gbc_featureSetComboBox = new GridBagConstraints();
		gbc_featureSetComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_featureSetComboBox.gridwidth = 4;
		gbc_featureSetComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureSetComboBox.gridx = 0;
		gbc_featureSetComboBox.gridy = 4;
		subsetInfoPanel.add(featureSetComboBox, gbc_featureSetComboBox);
		
		createFromFileRadioButton = new JRadioButton("Import feature list from file:");
		bg.add(createFromFileRadioButton);
		GridBagConstraints gbc_rdbtnNewRadioButton_2 = new GridBagConstraints();
		gbc_rdbtnNewRadioButton_2.gridwidth = 2;
		gbc_rdbtnNewRadioButton_2.anchor = GridBagConstraints.WEST;
		gbc_rdbtnNewRadioButton_2.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnNewRadioButton_2.gridx = 0;
		gbc_rdbtnNewRadioButton_2.gridy = 5;
		subsetInfoPanel.add(createFromFileRadioButton, gbc_rdbtnNewRadioButton_2);
		
		featureListFileTextField = new JTextField();
		featureListFileTextField.setEditable(false);
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.gridwidth = 3;
		gbc_textField_2.insets = new Insets(0, 0, 0, 5);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 0;
		gbc_textField_2.gridy = 6;
		subsetInfoPanel.add(featureListFileTextField, gbc_textField_2);
		featureListFileTextField.setColumns(10);
		
		browseButton = new JButton("Browse");
		browseButton.setActionCommand(BROWSE_COMMAND);
		browseButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 6;
		subsetInfoPanel.add(browseButton, gbc_btnNewButton);

//		featuresTable = new SubsetFeaturesTable();
//		getContentPane().add(new JScrollPane(featuresTable), BorderLayout.CENTER);

		String dsCommand = "";
		if (msFeatureSet == null) {
			setTitle("Create new feature subset");
			setIconImage(((ImageIcon) newSubsetIcon).getImage());
			populateMsFeatureSetSelector();
			featureSetComboBox.setSelectedItem(null);	
			addFromFeatureTableRadioButton.setSelected(true);
			featureSetComboBox.setEnabled(false);
			copyFromExistingSubsetRadioButton.addItemListener(this);
			addFromFeatureTableRadioButton.addItemListener(this);
			createFromFileRadioButton.addItemListener(this);
			dsCommand = MainActionCommands.CREATE_NEW_FEATURE_SUBSET_COMMAND.getName();
		} else {
			setTitle("Edit feature subset");
			setIconImage(((ImageIcon) editSubsetIcon).getImage());
			loadFeatureSubset(msFeatureSet);
			featureSetComboBox.setSelectedItem(activeSet);
			featureSetComboBox.setEnabled(false);
			tableRowChoiceComboBox.setEnabled(false);
			dsCommand = MainActionCommands.SAVE_CHANGES_TO_FEATURE_SUBSET_COMMAND.getName();
		}		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(dsCommand);
		btnSave.setActionCommand(dsCommand);
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectFeatureListFile();
	}
	
	private void selectFeatureListFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select feature list file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			featureListFile = fc.getSelectedFile();
			baseDirectory = featureListFile.getParentFile();
			featureListFileTextField.setText(featureListFile.getAbsolutePath());
			savePreferences();
		}
	}

	public MsFeatureSet getActiveSet() {
		return activeSet;
	}

	public TableRowSubset getFeaturesSelectionToAdd() {
		return (TableRowSubset) tableRowChoiceComboBox.getSelectedItem();
	}

//	public Collection<MsFeature>getSelectedFeatures() {
//		return  featuresTable.getSelectedFeatures();
//	}

	public MsFeatureSet getSelectedSourceSubset() {
		return (MsFeatureSet) featureSetComboBox.getSelectedItem();
	}

	public String getSubsetName() {
		return subsetNameTextField.getText().trim();
	}

	private void loadFeatureSubset(MsFeatureSet setToLoad) {

		activeSet = setToLoad;
		subsetNameTextField.setText(activeSet.getName());
		numFeaturesLabel.setText(Integer.toString(activeSet.getFeatures().size()));
//		featuresTable.setTableModelFromFeatureSet(setToLoad);
//		featureSetComboBox.setSelectedIndex(-1);
//		tableRowChoiceComboBox.setSelectedIndex(-1);
	}

	private void populateMsFeatureSetSelector() {

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if (currentProject == null)
			return;

		DataPipeline activePipeline = currentProject.getActiveDataPipeline();
		if (activePipeline == null)
			return;

		List<MsFeatureSet> sets = currentProject.getMsFeatureSetsForDataPipeline(activePipeline).stream()
				.filter(s -> !s.equals(activeSet)).sorted().collect(Collectors.toList());

		DefaultComboBoxModel<MsFeatureSet> model = new DefaultComboBoxModel<MsFeatureSet>(
				sets.toArray(new MsFeatureSet[sets.size()]));

		featureSetComboBox.setModel(model);
		featureSetComboBox.setSelectedIndex(-1);
	}

	public Collection<String> validateFeatureSubsetData() {

		Collection<String> errors = new ArrayList<String>();

		String newName = getSubsetName();
		if (newName.isEmpty())
			errors.add("Feature subset name cannot be empty.");

		DataAnalysisProject experiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Set<MsFeatureSet> allSubsets = 
				experiment.getMsFeatureSetsForDataPipeline(experiment.getActiveDataPipeline());
		MsFeatureSet existing = null;
		if (activeSet == null) {
			existing = allSubsets.stream().filter(s -> s.getName().equalsIgnoreCase(newName)).findFirst().orElse(null);
		} else {
			existing = allSubsets.stream().filter(s -> !s.equals(activeSet))
					.filter(s -> s.getName().equalsIgnoreCase(newName)).findFirst().orElse(null);
		}
		if (existing != null)
			errors.add("Feature subset \"" + newName + "\" already exists.");
		
		if(createFromFileRadioButton.isSelected() && featureListFile == null) 
			errors.add("Feature list file not selected.");

		return errors;
	}

	
	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if(activeSet != null)
			return;
		
		tableRowChoiceComboBox.setSelectedIndex(-1);
		featureSetComboBox.setSelectedIndex(-1);
		if(copyFromExistingSubsetRadioButton.isSelected()) {
			
			featureSetComboBox.setEnabled(true);			
			tableRowChoiceComboBox.setEnabled(false);
			browseButton.setEnabled(false);
		}
		if(addFromFeatureTableRadioButton.isSelected()) {
			
			featureSetComboBox.setEnabled(false);
			tableRowChoiceComboBox.setEnabled(true);
			browseButton.setEnabled(false);
		}
		if(createFromFileRadioButton.isSelected()) {
			
			featureSetComboBox.setEnabled(false);
			tableRowChoiceComboBox.setEnabled(false);
			browseButton.setEnabled(true);
		}
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =
			new File(preferences.get(BASE_DIRECTORY,
				MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}


	public File getFeatureListFile() {
		return featureListFile;
	}
}
