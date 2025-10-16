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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.std;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import ambit2.tautomers.zwitterion.ZwitterionManager;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ZwitterIonSettingsDialog extends JDialog implements BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = 5041872153346492046L;
	private static final Icon dialogIcon = GuiUtils.getIcon("gln-zwitter", 32);

	private Preferences preferences;
	
	public static final String USE_CARBOXYLIC_GROUPS = "USE_CARBOXYLIC_GROUPS";
	public static final String USE_SULFONIC_AND_SULFINIC_GROUPS = "USE_SULFONIC_AND_SULFINIC_GROUPS";
	public static final String USE_PHOSPHORIC_GROUPS = "USE_PHOSPHORIC_GROUPS";
	public static final String USE_PRIMARY_AMINES = "USE_PRIMARY_AMINES";
	public static final String USE_SECONDARY_AMINES = "USE_SECONDARY_AMINES";
	public static final String USE_TERTIARY_AMINES = "CLEAR_ISOTOPES";
	public static final String FILTER_DUPLICATES = "FILTER_DUPLICATES";
	public static final String MAX_NUMBER_OF_ZWITTERIONIC_PAIRS = "MAX_NUMBER_OF_ZWITTERIONIC_PAIRS";
	public static final String MAX_NUMBER_OF_REGISTERED_ZWITTERIONS = "MAX_NUMBER_OF_REGISTERED_ZWITTERIONS";
	
	private JCheckBox useCarboxylicGroupsCheckBox;
	private JCheckBox useSulfonicAndSulfinicGroupsCheckBox;
	private JCheckBox usePhosphoricGroupsCheckBox;
	private JCheckBox usePrimaryAminesCheckBox;
	private JCheckBox useSecondaryAminesCheckBox;
	private JCheckBox useTertiaryAminesCheckBox;
	private JCheckBox filterDuplicatesCheckBox;
	private JSpinner maxNumberOfZwitterionicPairsSpinner;
	private JSpinner maxNumberOfRegisteredZwitterionsSpinner;

	public ZwitterIonSettingsDialog(ActionListener listener) {
		super();
		setTitle("Zwitter-ion Generator Settings");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(350, 400));
		setSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		useCarboxylicGroupsCheckBox = new JCheckBox("Use Carboxylic Groups");
		GridBagConstraints gbc_useAromaticSymbolsCheckBox = new GridBagConstraints();
		gbc_useAromaticSymbolsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_useAromaticSymbolsCheckBox.gridwidth = 2;
		gbc_useAromaticSymbolsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_useAromaticSymbolsCheckBox.gridx = 0;
		gbc_useAromaticSymbolsCheckBox.gridy = 0;
		panel_1.add(useCarboxylicGroupsCheckBox, gbc_useAromaticSymbolsCheckBox);
		
		useSulfonicAndSulfinicGroupsCheckBox = new JCheckBox("Use Sulfonic And Sulfinic Groups");
		GridBagConstraints gbc_generateInChICheckBox = new GridBagConstraints();
		gbc_generateInChICheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_generateInChICheckBox.gridwidth = 2;
		gbc_generateInChICheckBox.anchor = GridBagConstraints.WEST;
		gbc_generateInChICheckBox.gridx = 0;
		gbc_generateInChICheckBox.gridy = 1;
		panel_1.add(useSulfonicAndSulfinicGroupsCheckBox, gbc_generateInChICheckBox);
		
		usePhosphoricGroupsCheckBox = new JCheckBox("Use Phosphoric Groups");
		GridBagConstraints gbc_splitFragmentsCheckBox = new GridBagConstraints();
		gbc_splitFragmentsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_splitFragmentsCheckBox.gridwidth = 2;
		gbc_splitFragmentsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_splitFragmentsCheckBox.gridx = 0;
		gbc_splitFragmentsCheckBox.gridy = 2;
		panel_1.add(usePhosphoricGroupsCheckBox, gbc_splitFragmentsCheckBox);
		
		usePrimaryAminesCheckBox = new JCheckBox("Use Primary Amines");
		GridBagConstraints gbc_generateTautomersCheckBox = new GridBagConstraints();
		gbc_generateTautomersCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_generateTautomersCheckBox.gridwidth = 2;
		gbc_generateTautomersCheckBox.anchor = GridBagConstraints.WEST;
		gbc_generateTautomersCheckBox.gridx = 0;
		gbc_generateTautomersCheckBox.gridy = 3;
		panel_1.add(usePrimaryAminesCheckBox, gbc_generateTautomersCheckBox);
		
		useSecondaryAminesCheckBox = new JCheckBox("Use Secondary Amines");
		GridBagConstraints gbc_neutraliseCheckBox = new GridBagConstraints();
		gbc_neutraliseCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_neutraliseCheckBox.gridwidth = 2;
		gbc_neutraliseCheckBox.anchor = GridBagConstraints.WEST;
		gbc_neutraliseCheckBox.gridx = 0;
		gbc_neutraliseCheckBox.gridy = 4;
		panel_1.add(useSecondaryAminesCheckBox, gbc_neutraliseCheckBox);
		
		useTertiaryAminesCheckBox = new JCheckBox("Use Tertiary Amines");
		GridBagConstraints gbc_clearIsotopesCheckBox = new GridBagConstraints();
		gbc_clearIsotopesCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_clearIsotopesCheckBox.anchor = GridBagConstraints.WEST;
		gbc_clearIsotopesCheckBox.gridwidth = 2;
		gbc_clearIsotopesCheckBox.gridx = 0;
		gbc_clearIsotopesCheckBox.gridy = 5;
		panel_1.add(useTertiaryAminesCheckBox, gbc_clearIsotopesCheckBox);
		
		filterDuplicatesCheckBox = new JCheckBox("Filter Duplicates");
		GridBagConstraints gbc_implicitHydrogensCheckBox = new GridBagConstraints();
		gbc_implicitHydrogensCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_implicitHydrogensCheckBox.anchor = GridBagConstraints.WEST;
		gbc_implicitHydrogensCheckBox.gridwidth = 2;
		gbc_implicitHydrogensCheckBox.gridx = 0;
		gbc_implicitHydrogensCheckBox.gridy = 6;
		panel_1.add(filterDuplicatesCheckBox, gbc_implicitHydrogensCheckBox);
		
		JLabel lblNewLabel = new JLabel("Max Number Of Zwitterionic Pairs");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 7;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		maxNumberOfZwitterionicPairsSpinner = new JSpinner();
		maxNumberOfZwitterionicPairsSpinner.setModel(new SpinnerNumberModel(100, 1, 200, 1));
		maxNumberOfZwitterionicPairsSpinner.setPreferredSize(new Dimension(80, 20));
		maxNumberOfZwitterionicPairsSpinner.setMinimumSize(new Dimension(50, 20));
		GridBagConstraints gbc_maxNumberOfZwitterionicPairsSpinner = new GridBagConstraints();
		gbc_maxNumberOfZwitterionicPairsSpinner.anchor = GridBagConstraints.WEST;
		gbc_maxNumberOfZwitterionicPairsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_maxNumberOfZwitterionicPairsSpinner.gridx = 1;
		gbc_maxNumberOfZwitterionicPairsSpinner.gridy = 7;
		panel_1.add(maxNumberOfZwitterionicPairsSpinner, gbc_maxNumberOfZwitterionicPairsSpinner);
		
		JLabel lblNewLabel_1 = new JLabel("Max Number Of Registered Zwitterions");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 8;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		maxNumberOfRegisteredZwitterionsSpinner = new JSpinner();
		maxNumberOfRegisteredZwitterionsSpinner.setModel(new SpinnerNumberModel(10000, 1, 20000, 1));
		maxNumberOfRegisteredZwitterionsSpinner.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_maxNumberOfRegisteredZwitterionsSpinner = new GridBagConstraints();
		gbc_maxNumberOfRegisteredZwitterionsSpinner.anchor = GridBagConstraints.WEST;
		gbc_maxNumberOfRegisteredZwitterionsSpinner.gridx = 1;
		gbc_maxNumberOfRegisteredZwitterionsSpinner.gridy = 8;
		panel_1.add(maxNumberOfRegisteredZwitterionsSpinner, gbc_maxNumberOfRegisteredZwitterionsSpinner);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.SAVE_ZWITTER_ION_GENERATOR_SETTINGS_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.SAVE_ZWITTER_ION_GENERATOR_SETTINGS_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		pack();
	}	
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		
		useCarboxylicGroupsCheckBox.setSelected(
				preferences.getBoolean(USE_CARBOXYLIC_GROUPS,true));
		useSulfonicAndSulfinicGroupsCheckBox.setSelected(
				preferences.getBoolean(USE_SULFONIC_AND_SULFINIC_GROUPS,true));
		usePhosphoricGroupsCheckBox.setSelected(
				preferences.getBoolean(USE_PHOSPHORIC_GROUPS,true));
		usePrimaryAminesCheckBox.setSelected(
				preferences.getBoolean(USE_PRIMARY_AMINES,true));
		useSecondaryAminesCheckBox.setSelected(
				preferences.getBoolean(USE_SECONDARY_AMINES,true));
		useTertiaryAminesCheckBox.setSelected(
				preferences.getBoolean(USE_TERTIARY_AMINES,true));
		filterDuplicatesCheckBox.setSelected(
				preferences.getBoolean(FILTER_DUPLICATES,false));
		maxNumberOfZwitterionicPairsSpinner.setValue(
				preferences.getInt(MAX_NUMBER_OF_ZWITTERIONIC_PAIRS, 100));
		maxNumberOfRegisteredZwitterionsSpinner.setValue(
				preferences.getInt(MAX_NUMBER_OF_REGISTERED_ZWITTERIONS, 10000));
	}

	@Override
	public void loadPreferences() {
				    
		loadPreferences(Preferences.userRoot().node(
				MRC2ToolBoxConfiguration.ZWITTER_ION_GENERATOR_PREFERENCES_NODE));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(
				MRC2ToolBoxConfiguration.ZWITTER_ION_GENERATOR_PREFERENCES_NODE);

		preferences.putBoolean(USE_CARBOXYLIC_GROUPS,
				useCarboxylicGroupsCheckBox.isSelected());
		preferences.putBoolean(USE_SULFONIC_AND_SULFINIC_GROUPS,
				useSulfonicAndSulfinicGroupsCheckBox.isSelected());
		preferences.putBoolean(USE_PHOSPHORIC_GROUPS,
				usePhosphoricGroupsCheckBox.isSelected());
		preferences.putBoolean(USE_PRIMARY_AMINES,
				usePrimaryAminesCheckBox.isSelected());
		preferences.putBoolean(USE_SECONDARY_AMINES,
				useSecondaryAminesCheckBox.isSelected());
		preferences.putBoolean(USE_TERTIARY_AMINES,
				useTertiaryAminesCheckBox.isSelected());
		preferences.putBoolean(FILTER_DUPLICATES,
				filterDuplicatesCheckBox.isSelected());
		preferences.putInt(MAX_NUMBER_OF_ZWITTERIONIC_PAIRS,
				(int)maxNumberOfZwitterionicPairsSpinner.getValue());
		preferences.putInt(MAX_NUMBER_OF_REGISTERED_ZWITTERIONS,
				(int)maxNumberOfRegisteredZwitterionsSpinner.getValue());
	}
	
	public ZwitterionManager getConfiguredZwitterionManager() {
		
		ZwitterionManager zwitterionManager = new ZwitterionManager();
		zwitterionManager.FlagUseCarboxylicGroups = 
				useCarboxylicGroupsCheckBox.isSelected();
		zwitterionManager.FlagUseSulfonicAndSulfinicGroups =
				useSulfonicAndSulfinicGroupsCheckBox.isSelected();
		zwitterionManager.FlagUsePhosphoricGroups = 
				usePhosphoricGroupsCheckBox.isSelected();
		zwitterionManager.FlagUsePrimaryAmines = 
				usePrimaryAminesCheckBox.isSelected();
		zwitterionManager.FlagUseSecondaryAmines = 
				useSecondaryAminesCheckBox.isSelected();
		zwitterionManager.FlagUseTertiaryAmines = 
				useTertiaryAminesCheckBox.isSelected();
		zwitterionManager.FlagFilterDuplicates = 
				filterDuplicatesCheckBox.isSelected();		
		zwitterionManager.MaxNumberOfZwitterionicPairs = 
				(int)maxNumberOfZwitterionicPairsSpinner.getValue();
		zwitterionManager.MaxNumberOfRegisteredZwitterions = 
				(int)maxNumberOfRegisteredZwitterionsSpinner.getValue();
				
		return zwitterionManager;
	}
}


















