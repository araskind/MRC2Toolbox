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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.std;

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
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

import ambit2.tautomers.TautomerConst.GAT;
import ambit2.tautomers.TautomerManager;
import ambit2.tautomers.ranking.EnergyRanking;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class TautomerGeneratorSettingsDialog extends JDialog implements BackedByPreferences, ItemListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 5041872153346492046L;
	private static final Icon dialogIcon = GuiUtils.getIcon("tautomerSettings", 32);
	//
	private Preferences preferences;
	
	public static final String TAUTOMER_GENERATION_ALGORITHM = "TAUTOMER_GENERATION_ALGORITHM";
	public static final String USE_13_SHIFTS_ONLY = "USE_13_SHIFTS_ONLY";
	public static final String USE_15_SHIFTS = "USE_15_SHIFTS";
	public static final String USE_17_SHIFTS = "USE_17_SHIFTS";
	public static final String USE_19_SHIFTS = "USE_19_SHIFTS";
	public static final String USE_RING_CHAIN_RULES = "USE_RING_CHAIN_RULES";
	public static final String USE_CHLORINE_RULES = "USE_CHLORINE_RULES";
	public static final String MAX_NUM_OF_BACKTRACKS = "MAX_NUM_OF_BACKTRACKS";
	public static final String MAX_NUM_OF_TAUTOMER_REGISTRATIONS = "MAX_NUM_OF_TAUTOMER_REGISTRATIONS";
	public static final String MAX_NUM_OF_SUBCOMBINATIONS = "MAX_NUM_OF_SUBCOMBINATIONS";
	public static final String CALCULATE_CACTVS_ENERGY_RANK = "CALCULATE_CACTVS_ENERGY_RANK";
		
	private JComboBox<TautomerGenerationAlgorithm> tautomerGenerationAlgorithmComboBox;
	private JCheckBox use13ruleOnlyCheckBox;
	private JCheckBox use15ruleCheckBox;
	private JCheckBox use17ruleCheckBox;
	private JCheckBox use19ruleCheckBox;
	private JCheckBox useRingChainRulesCheckBox;
	private JCheckBox useChlorineRulesCheckBox;
	private JCheckBox calculateCACTVSEnergyRankCheckBox;
	private JSpinner maxNumOfBackTracksSpinner;
	private JSpinner maxNumOfTautomerRegistrationsSpinner;
	private JSpinner maxNumOfSubCombinationsSpinner;

	public TautomerGeneratorSettingsDialog(ActionListener listener) {
		super();
		setTitle("Tautomer Generator Settings");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(400, 400));
		setSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Tautomer generation algorithm");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		tautomerGenerationAlgorithmComboBox = 
				new JComboBox<TautomerGenerationAlgorithm>(
						new DefaultComboBoxModel<TautomerGenerationAlgorithm>(
								TautomerGenerationAlgorithm.values()));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel_1.add(tautomerGenerationAlgorithmComboBox, gbc_comboBox);
		
		use13ruleOnlyCheckBox = new JCheckBox("Use only 1->3 shifts rule");
		use13ruleOnlyCheckBox.addItemListener(this);
		GridBagConstraints gbc_use13ruleOnlyCheckBox = new GridBagConstraints();
		gbc_use13ruleOnlyCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_use13ruleOnlyCheckBox.gridwidth = 2;
		gbc_use13ruleOnlyCheckBox.anchor = GridBagConstraints.WEST;
		gbc_use13ruleOnlyCheckBox.gridx = 0;
		gbc_use13ruleOnlyCheckBox.gridy = 1;
		panel_1.add(use13ruleOnlyCheckBox, gbc_use13ruleOnlyCheckBox);
		
		use15ruleCheckBox = new JCheckBox("Use 1->5 shifts rule");
		GridBagConstraints gbc_use15ruleCheckBox = new GridBagConstraints();
		gbc_use15ruleCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_use15ruleCheckBox.gridwidth = 2;
		gbc_use15ruleCheckBox.anchor = GridBagConstraints.WEST;
		gbc_use15ruleCheckBox.gridx = 0;
		gbc_use15ruleCheckBox.gridy = 2;
		panel_1.add(use15ruleCheckBox, gbc_use15ruleCheckBox);
		
		use17ruleCheckBox = new JCheckBox("Use 1->7 shifts rule");
		GridBagConstraints gbc_use17ruleCheckBox = new GridBagConstraints();
		gbc_use17ruleCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_use17ruleCheckBox.gridwidth = 2;
		gbc_use17ruleCheckBox.anchor = GridBagConstraints.WEST;
		gbc_use17ruleCheckBox.gridx = 0;
		gbc_use17ruleCheckBox.gridy = 3;
		panel_1.add(use17ruleCheckBox, gbc_use17ruleCheckBox);
		
		use19ruleCheckBox = new JCheckBox("Use 1->9 shifts rule");
		GridBagConstraints gbc_use19ruleCheckBox = new GridBagConstraints();
		gbc_use19ruleCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_use19ruleCheckBox.gridwidth = 2;
		gbc_use19ruleCheckBox.anchor = GridBagConstraints.WEST;
		gbc_use19ruleCheckBox.gridx = 0;
		gbc_use19ruleCheckBox.gridy = 4;
		panel_1.add(use19ruleCheckBox, gbc_use19ruleCheckBox);
		
		useRingChainRulesCheckBox = new JCheckBox("Use ring chain rules");
		GridBagConstraints gbc_useRingChainRulesCheckBox = new GridBagConstraints();
		gbc_useRingChainRulesCheckBox.anchor = GridBagConstraints.WEST;
		gbc_useRingChainRulesCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_useRingChainRulesCheckBox.gridx = 0;
		gbc_useRingChainRulesCheckBox.gridy = 5;
		panel_1.add(useRingChainRulesCheckBox, gbc_useRingChainRulesCheckBox);
		
		useChlorineRulesCheckBox = new JCheckBox("Use chlorine rules");
		GridBagConstraints gbc_useChlorineRulesCheckBox = new GridBagConstraints();
		gbc_useChlorineRulesCheckBox.anchor = GridBagConstraints.WEST;
		gbc_useChlorineRulesCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_useChlorineRulesCheckBox.gridx = 0;
		gbc_useChlorineRulesCheckBox.gridy = 6;
		panel_1.add(useChlorineRulesCheckBox, gbc_useChlorineRulesCheckBox);
		
		JLabel lblNewLabel_0 = new JLabel("Max Number Of Backtracks");
		GridBagConstraints gbc_lblNewLabel_0 = new GridBagConstraints();
		gbc_lblNewLabel_0.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_0.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_0.gridx = 0;
		gbc_lblNewLabel_0.gridy = 7;
		panel_1.add(lblNewLabel_0, gbc_lblNewLabel_0);
		
		maxNumOfBackTracksSpinner = new JSpinner();
		maxNumOfBackTracksSpinner.setModel(new SpinnerNumberModel(5000, 1, 10000, 1));
		maxNumOfBackTracksSpinner.setPreferredSize(new Dimension(80, 20));
		maxNumOfBackTracksSpinner.setMinimumSize(new Dimension(50, 20));
		GridBagConstraints gbc_maxNumOfBackTracksSpinner = new GridBagConstraints();
		gbc_maxNumOfBackTracksSpinner.anchor = GridBagConstraints.WEST;
		gbc_maxNumOfBackTracksSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_maxNumOfBackTracksSpinner.gridx = 1;
		gbc_maxNumOfBackTracksSpinner.gridy = 7;
		panel_1.add(maxNumOfBackTracksSpinner, gbc_maxNumOfBackTracksSpinner);
		
		JLabel lblNewLabel_1 = new JLabel("Max Number Of Tautomer Registrations");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 8;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		maxNumOfTautomerRegistrationsSpinner = new JSpinner();
		maxNumOfTautomerRegistrationsSpinner.setModel(new SpinnerNumberModel(1000, 1, 5000, 1));
		maxNumOfTautomerRegistrationsSpinner.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_maxNumOfTautomerRegistrationsSpinner = new GridBagConstraints();
		gbc_maxNumOfTautomerRegistrationsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_maxNumOfTautomerRegistrationsSpinner.anchor = GridBagConstraints.WEST;
		gbc_maxNumOfTautomerRegistrationsSpinner.gridx = 1;
		gbc_maxNumOfTautomerRegistrationsSpinner.gridy = 8;
		panel_1.add(maxNumOfTautomerRegistrationsSpinner, gbc_maxNumOfTautomerRegistrationsSpinner);
		
		JLabel lblNewLabel_2 = new JLabel("Max number of sub-combinations");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 9;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		maxNumOfSubCombinationsSpinner = new JSpinner();
		maxNumOfSubCombinationsSpinner.setModel(new SpinnerNumberModel(10000, 1, 20000, 1));
		maxNumOfSubCombinationsSpinner.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_maxNumOfSubCombinationsSpinner = new GridBagConstraints();
		gbc_maxNumOfSubCombinationsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_maxNumOfSubCombinationsSpinner.anchor = GridBagConstraints.WEST;
		gbc_maxNumOfSubCombinationsSpinner.gridx = 1;
		gbc_maxNumOfSubCombinationsSpinner.gridy = 9;
		panel_1.add(maxNumOfSubCombinationsSpinner, gbc_maxNumOfSubCombinationsSpinner);
		
		calculateCACTVSEnergyRankCheckBox = new JCheckBox("Calculate CACTVS Energy Rank");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 10;
		panel_1.add(calculateCACTVSEnergyRankCheckBox, gbc_chckbxNewCheckBox);
		
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
				MainActionCommands.SAVE_TAUTOMER_GENERATOR_SETTINGS_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.SAVE_TAUTOMER_GENERATOR_SETTINGS_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadPreferences();
		pack();
	}
	
	public int getMaxNumOfBackTracks() {
		return (int)maxNumOfBackTracksSpinner.getValue();
	}
	
	public int getMaxNumOfTautomerRegistrations() {
		return (int)maxNumOfTautomerRegistrationsSpinner.getValue();
	}
	
	public int getMaxNumOfSubCombinations() {
		return (int)maxNumOfSubCombinationsSpinner.getValue();
	}
	
	public TautomerGenerationAlgorithm getTga() {
		return (TautomerGenerationAlgorithm)tautomerGenerationAlgorithmComboBox.getSelectedItem();
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;

		TautomerGenerationAlgorithm tga = 
				TautomerGenerationAlgorithm.getTautomerGenerationAlgorithmByGATName(
						preferences.get(TAUTOMER_GENERATION_ALGORITHM, GAT.Incremental.name()));
		if(tga == null)
			tga= TautomerGenerationAlgorithm.Incremental;
		
		tautomerGenerationAlgorithmComboBox.setSelectedItem(tga);
				
		use15ruleCheckBox.setSelected(preferences.getBoolean(USE_15_SHIFTS, true));
		use17ruleCheckBox.setSelected(preferences.getBoolean(USE_17_SHIFTS, true));
		use19ruleCheckBox.setSelected(preferences.getBoolean(USE_19_SHIFTS, false));
		useRingChainRulesCheckBox.setSelected(
				preferences.getBoolean(USE_RING_CHAIN_RULES, false));
		useChlorineRulesCheckBox.setSelected(
				preferences.getBoolean(USE_CHLORINE_RULES, false));
		use13ruleOnlyCheckBox.setSelected(
				preferences.getBoolean(USE_13_SHIFTS_ONLY, false));
		
		maxNumOfBackTracksSpinner.setValue(
				preferences.getInt(MAX_NUM_OF_BACKTRACKS, 5000));
		maxNumOfTautomerRegistrationsSpinner.setValue(
				preferences.getInt(MAX_NUM_OF_TAUTOMER_REGISTRATIONS, 1000));
		maxNumOfSubCombinationsSpinner.setValue(
				preferences.getInt(MAX_NUM_OF_SUBCOMBINATIONS, 10000));	
		calculateCACTVSEnergyRankCheckBox.setSelected(
				preferences.getBoolean(CALCULATE_CACTVS_ENERGY_RANK, false));
	}

	@Override
	public void loadPreferences(){
		
		loadPreferences(Preferences.userRoot().node(
				MRC2ToolBoxConfiguration.TAUTOMER_GENERATOR_PREFERENCES_NODE));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(
				MRC2ToolBoxConfiguration.TAUTOMER_GENERATOR_PREFERENCES_NODE);
		
		preferences.put(TAUTOMER_GENERATION_ALGORITHM, getAlgorithm().name());
		preferences.putBoolean(USE_13_SHIFTS_ONLY, use13ruleOnlyCheckBox.isSelected());

		preferences.putBoolean(USE_15_SHIFTS, use15ruleCheckBox.isSelected());
		preferences.putBoolean(USE_17_SHIFTS, use17ruleCheckBox.isSelected());
		preferences.putBoolean(USE_19_SHIFTS, use19ruleCheckBox.isSelected());
		preferences.putBoolean(USE_RING_CHAIN_RULES, useRingChainRulesCheckBox.isSelected());
		preferences.putBoolean(USE_CHLORINE_RULES, useChlorineRulesCheckBox.isSelected());
		preferences.putBoolean(CALCULATE_CACTVS_ENERGY_RANK, calculateCACTVSEnergyRankCheckBox.isSelected());

		preferences.putInt(MAX_NUM_OF_BACKTRACKS, getMaxNumOfBackTracks());
		preferences.putInt(MAX_NUM_OF_TAUTOMER_REGISTRATIONS, getMaxNumOfTautomerRegistrations());
		preferences.putInt(MAX_NUM_OF_SUBCOMBINATIONS, getMaxNumOfSubCombinations());
	}
	
	public TautomerManager getConfiguredTautomerManager() {
		
		TautomerManager tautomerManager = new TautomerManager();
		
		tautomerManager.maxNumOfBackTracks = getMaxNumOfBackTracks();
		tautomerManager.maxNumOfTautomerRegistrations = getMaxNumOfTautomerRegistrations();
		tautomerManager.maxNumOfSubCombinations = getMaxNumOfSubCombinations();
		tautomerManager.FlagCalculateCACTVSEnergyRank = calculateCACTVSEnergyRankCheckBox.isSelected();
		
		tautomerManager.getKnowledgeBase().activateChlorineRules(useChlorineRulesCheckBox.isSelected());
		tautomerManager.getKnowledgeBase().activateRingChainRules(useRingChainRulesCheckBox.isSelected());
		tautomerManager.getKnowledgeBase().use15ShiftRules(use15ruleCheckBox.isSelected());
		tautomerManager.getKnowledgeBase().use17ShiftRules(use17ruleCheckBox.isSelected());
		tautomerManager.getKnowledgeBase().use19ShiftRules(use19ruleCheckBox.isSelected());
		tautomerManager.getKnowledgeBase().use13ShiftRulesOnly(use13ruleOnlyCheckBox.isSelected());
			
		try {
			tautomerManager.setEnergyRanking(new EnergyRanking());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		tman.tautomerFilter.setFlagApplyWarningFilter(true);
//		tman.tautomerFilter.setFlagApplyExcludeFilter(true);
//		tman.tautomerFilter.setFlagApplyDuplicationFilter(true);
//		tman.tautomerFilter.setFlagApplyDuplicationCheckIsomorphism(false);
//		tman.tautomerFilter.setFlagApplyDuplicationCheckInChI(true);
//		tman.tautomerFilter.setFlagFilterIncorrectValencySumStructures(true);
//		tman.tautomerFilter.FlagApplySimpleAromaticityRankCorrection = true;
//		tman.FlagSetStereoElementsOnTautomerProcess = true;
//		tman.FlagNewRuleInstanceSearchOnEnergyRanking = false;
//		tman.FlagCheckDuplicationOnRegistering = true;
//		tman.FlagRecurseBackResultTautomers = false;
//		tman.FlagPrintTargetMoleculeInfo = true;
//		tman.FlagPrintExtendedRuleInstances = true;
//		tman.FlagPrintIcrementalStepDebugInfo = false;
//		tman.FlagProcessRemainingStackIncSteps = true;
//		tman.FlagCalculateCACTVSEnergyRank = true;
//		tman.FlagRegisterOnlyBestRankTautomers = false;
//		tman.FlagNewRuleInstanceSearchOnEnergyRanking = false;
//		tman.FlagRegisterOnlyBestRankTautomers = false;
//		tman.FlagAddImplicitHAtomsOnTautomerProcess = false;
		
		return tautomerManager;
	}
	
	public GAT getAlgorithm() {
		
		TautomerGenerationAlgorithm tga = getTga();
		if(tga != null)
			return tga.getAlgorithm();
		else
			return GAT.Incremental;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if(e.getSource().equals(use13ruleOnlyCheckBox))
			adjustRuleSelection(use13ruleOnlyCheckBox.isSelected());		
	}
	
	private void adjustRuleSelection(boolean use13ruleOnly) {
		
		if(use13ruleOnly) {
			use15ruleCheckBox.setSelected(false);
			use17ruleCheckBox.setSelected(false);
			use19ruleCheckBox.setSelected(false);
		}		
		use15ruleCheckBox.setEnabled(!use13ruleOnly);
		use17ruleCheckBox.setEnabled(!use13ruleOnly);
		use19ruleCheckBox.setEnabled(!use13ruleOnly);
	}
}














