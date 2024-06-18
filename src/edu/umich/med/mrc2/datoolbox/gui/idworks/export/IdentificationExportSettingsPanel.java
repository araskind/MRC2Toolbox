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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdFilter;
import edu.umich.med.mrc2.datoolbox.data.enums.DecoyExportHandling;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIDSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;
import edu.umich.med.mrc2.datoolbox.gui.idworks.export.cpdfilter.CompoundFilterDefinitionDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class IdentificationExportSettingsPanel extends JPanel implements ItemListener, ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1779207710366698893L;
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 16);
	private static final NumberFormat twoDecFormat = new DecimalFormat("###.##");

	public static final String MIN_SCORE = "MIN_SCORE";
	public static final String SCORING_PARAMETER = "SCORING_PARAMETER";
	public static final String INCLUDE_NORMAL_MATCH = "INCLUDE_NORMAL_MATCH";
	public static final String INCLUDE_IN_SOURCE_MATCH = "INCLUDE_IN_SOURCE_MATCH";
	public static final String INCLUDE_HYBRID_MATCH = "INCLUDE_HYBRID_MATCH";
	public static final String IDS_PER_FEATURE = "IDS_PER_FEATURE";
	public static final String EXCLUDE_IF_NO_IDS = "EXCLUDE_IF_NO_IDS";
	public static final String DECOY_EXPORT_HANDLING = "DECOY_EXPORT_HANDLING";
	
	private JComboBox<FeatureIDSubset> featureIdSubsetComboBox;
	private JCheckBox regularMatchCheckBox;
	private JCheckBox inSourceCheckBox;
	private JCheckBox hybridMatchCheckBox;
	private JComboBox<MSMSScoringParameter> scoringParameterComboBox;
	private JFormattedTextField minScoreTextField;
	private JCheckBox excludeIfNoIdsLeftCheckBox;
	private JLabel lblNewLabel;
	private JComboBox decoyHitsHadlingComboBox;
	private JButton compoundFilterButton;
	private CompoundFilterDefinitionDialog compoundFilterDefinitionDialog;
	private CompoundIdFilter compoundFilter;
	private JButton clearCompoundFilterButton;
	
	public IdentificationExportSettingsPanel() {
		super();
		setBorder(
				new CompoundBorder(
					new TitledBorder(
							new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255),
									new Color(160, 160, 160)),
							"Identification export settings", TitledBorder.LEADING, TitledBorder.TOP, null,
							new Color(0, 0, 0)),
					new EmptyBorder(10, 10, 10, 10)));
			
			GridBagLayout gbl_panel_4 = new GridBagLayout();
			gbl_panel_4.columnWidths = new int[] { 0, 189, 189, 0, 0, 0 };
			gbl_panel_4.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
			gbl_panel_4.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
			gbl_panel_4.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			setLayout(gbl_panel_4);
			
			JLabel lblNewLabel_9 = new JLabel("IDs to export for each feature: ");
			GridBagConstraints gbc_lblNewLabel_9 = new GridBagConstraints();
			gbc_lblNewLabel_9.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_9.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_9.gridx = 0;
			gbc_lblNewLabel_9.gridy = 0;
			add(lblNewLabel_9, gbc_lblNewLabel_9);
			
			featureIdSubsetComboBox = 
					new JComboBox<FeatureIDSubset>(
							new DefaultComboBoxModel<FeatureIDSubset>(FeatureIDSubset.values()));
			GridBagConstraints gbc_featureIdSubsetComboBox = new GridBagConstraints();
			gbc_featureIdSubsetComboBox.gridwidth = 4;
			gbc_featureIdSubsetComboBox.insets = new Insets(0, 0, 5, 0);
			gbc_featureIdSubsetComboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_featureIdSubsetComboBox.gridx = 1;
			gbc_featureIdSubsetComboBox.gridy = 0;
			add(featureIdSubsetComboBox, gbc_featureIdSubsetComboBox);
			
			JLabel lblNewLabel_5 = new JLabel("Top MSMS library hit must be: ");
			GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
			gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_5.gridx = 0;
			gbc_lblNewLabel_5.gridy = 1;
			add(lblNewLabel_5, gbc_lblNewLabel_5);

			regularMatchCheckBox = new JCheckBox("Regular match");
			GridBagConstraints gbc_regularMatchCheckBox = new GridBagConstraints();
			gbc_regularMatchCheckBox.anchor = GridBagConstraints.WEST;
			gbc_regularMatchCheckBox.insets = new Insets(0, 0, 5, 5);
			gbc_regularMatchCheckBox.gridx = 1;
			gbc_regularMatchCheckBox.gridy = 1;
			add(regularMatchCheckBox, gbc_regularMatchCheckBox);
			
			inSourceCheckBox = new JCheckBox("In-source match");
			GridBagConstraints gbc_inSourceCheckBox = new GridBagConstraints();
			gbc_inSourceCheckBox.insets = new Insets(0, 0, 5, 5);
			gbc_inSourceCheckBox.anchor = GridBagConstraints.WEST;
			gbc_inSourceCheckBox.gridx = 2;
			gbc_inSourceCheckBox.gridy = 1;
			add(inSourceCheckBox, gbc_inSourceCheckBox);

			hybridMatchCheckBox = new JCheckBox("Hybrid match");
			GridBagConstraints gbc_hybridMatchCheckBox = new GridBagConstraints();
			gbc_hybridMatchCheckBox.insets = new Insets(0, 0, 5, 5);
			gbc_hybridMatchCheckBox.anchor = GridBagConstraints.WEST;
			gbc_hybridMatchCheckBox.gridx = 3;
			gbc_hybridMatchCheckBox.gridy = 1;
			add(hybridMatchCheckBox, gbc_hybridMatchCheckBox);
			
			lblNewLabel = new JLabel("Decoy hits handling");
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 2;
			add(lblNewLabel, gbc_lblNewLabel);
			
			decoyHitsHadlingComboBox = new JComboBox<DecoyExportHandling>(
					new DefaultComboBoxModel<DecoyExportHandling>(DecoyExportHandling.values()));
			GridBagConstraints gbc_decoyHitsHadlingComboBox = new GridBagConstraints();
			gbc_decoyHitsHadlingComboBox.gridwidth = 3;
			gbc_decoyHitsHadlingComboBox.insets = new Insets(0, 0, 5, 5);
			gbc_decoyHitsHadlingComboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_decoyHitsHadlingComboBox.gridx = 1;
			gbc_decoyHitsHadlingComboBox.gridy = 2;
			add(decoyHitsHadlingComboBox, gbc_decoyHitsHadlingComboBox);
			
			JLabel lblNewLabel_1 = new JLabel("(for primary ID only)");
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
			gbc_lblNewLabel_1.gridx = 4;
			gbc_lblNewLabel_1.gridy = 2;
			add(lblNewLabel_1, gbc_lblNewLabel_1);

			JLabel lblNewLabel_8 = new JLabel("Scoring parameter");
			GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
			gbc_lblNewLabel_8.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_8.gridx = 0;
			gbc_lblNewLabel_8.gridy = 3;
			add(lblNewLabel_8, gbc_lblNewLabel_8);

			scoringParameterComboBox = new JComboBox<MSMSScoringParameter>(
					new DefaultComboBoxModel<MSMSScoringParameter>(MSMSScoringParameter.values()));	
			GridBagConstraints gbc_scoringParameterComboBox = new GridBagConstraints();
			gbc_scoringParameterComboBox.gridwidth = 2;
			gbc_scoringParameterComboBox.insets = new Insets(0, 0, 5, 5);
			gbc_scoringParameterComboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_scoringParameterComboBox.gridx = 1;
			gbc_scoringParameterComboBox.gridy = 3;
			add(scoringParameterComboBox, gbc_scoringParameterComboBox);
			
			JLabel lblNewLabel_2 = new JLabel("Minimal score");
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_2.gridx = 3;
			gbc_lblNewLabel_2.gridy = 3;
			add(lblNewLabel_2, gbc_lblNewLabel_2);

			minScoreTextField = new JFormattedTextField(twoDecFormat);
			minScoreTextField.setColumns(10);
			GridBagConstraints gbc_minScoreTextField = new GridBagConstraints();
			gbc_minScoreTextField.insets = new Insets(0, 0, 5, 0);
			gbc_minScoreTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_minScoreTextField.gridx = 4;
			gbc_minScoreTextField.gridy = 3;
			add(minScoreTextField, gbc_minScoreTextField);
			
			excludeIfNoIdsLeftCheckBox = new JCheckBox(
					"Exclude features from export if all IDs were filtered out");
			GridBagConstraints gbc_excludeIfNoIdsLeftCheckBox = new GridBagConstraints();
			gbc_excludeIfNoIdsLeftCheckBox.insets = new Insets(0, 0, 0, 5);
			gbc_excludeIfNoIdsLeftCheckBox.anchor = GridBagConstraints.WEST;
			gbc_excludeIfNoIdsLeftCheckBox.gridwidth = 3;
			gbc_excludeIfNoIdsLeftCheckBox.gridx = 0;
			gbc_excludeIfNoIdsLeftCheckBox.gridy = 4;
			add(excludeIfNoIdsLeftCheckBox, gbc_excludeIfNoIdsLeftCheckBox);
			
			compoundFilterButton = new JButton(
					MainActionCommands.DEFINE_COMPOUND_FILTER_FOR_EXPORT_COMMAND.getName());
			compoundFilterButton.setActionCommand(
					MainActionCommands.DEFINE_COMPOUND_FILTER_FOR_EXPORT_COMMAND.getName());
			compoundFilterButton.addActionListener(this);
			GridBagConstraints gbc_compoundFilterButton = new GridBagConstraints();
			gbc_compoundFilterButton.fill = GridBagConstraints.HORIZONTAL;
			gbc_compoundFilterButton.insets = new Insets(0, 0, 0, 5);
			gbc_compoundFilterButton.gridx = 3;
			gbc_compoundFilterButton.gridy = 4;
			add(compoundFilterButton, gbc_compoundFilterButton);
			
			clearCompoundFilterButton = new JButton("Clear filter");
			clearCompoundFilterButton.setActionCommand(
					MainActionCommands.CLEAR_COMPOUND_FILTER_FOR_EXPORT_COMMAND.getName());
			clearCompoundFilterButton.addActionListener(this);
			GridBagConstraints gbc_clearCompoundFilterButton = new GridBagConstraints();
			gbc_clearCompoundFilterButton.gridx = 4;
			gbc_clearCompoundFilterButton.gridy = 4;
			add(clearCompoundFilterButton, gbc_clearCompoundFilterButton);
	}
	
	public MSMSScoringParameter getMSMSScoringParameter() {
		return (MSMSScoringParameter)scoringParameterComboBox.getSelectedItem();
	}
	
	public double getMinimalMSMSScore() {
		
		if(minScoreTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(minScoreTextField.getText());
	}
	
	public FeatureIDSubset getFeatureIDSubset() {		
		return (FeatureIDSubset)featureIdSubsetComboBox.getSelectedItem();
	}
	
	public Collection<MSMSMatchType>getMSMSSearchTypes(){
		
		Collection<MSMSMatchType>searchTypes = new ArrayList<MSMSMatchType>();
		if(regularMatchCheckBox.isSelected())
			searchTypes.add(MSMSMatchType.Regular);
		
		if(inSourceCheckBox.isSelected())
			searchTypes.add(MSMSMatchType.InSource);
		
		if(hybridMatchCheckBox.isSelected())
			searchTypes.add(MSMSMatchType.Hybrid);
		
		return searchTypes;
	}
	
	public boolean excludeIfNoIdsLeft() {
		return excludeIfNoIdsLeftCheckBox.isSelected();
	}
	
	public DecoyExportHandling getDecoyExportHandling() {		
		return (DecoyExportHandling)decoyHitsHadlingComboBox.getSelectedItem();
	}

	public void loadPreferences(Preferences preferences) {
		
		FeatureIDSubset idsPerFeature = FeatureIDSubset.getOptionByName(
				preferences.get(IDS_PER_FEATURE, FeatureIDSubset.PRIMARY_ONLY.name()));		
		featureIdSubsetComboBox.setSelectedItem(idsPerFeature);
		
		regularMatchCheckBox.setSelected(
				preferences.getBoolean(INCLUDE_NORMAL_MATCH, false));
		inSourceCheckBox.setSelected(
				preferences.getBoolean(INCLUDE_IN_SOURCE_MATCH, false));
		hybridMatchCheckBox.setSelected(
				preferences.getBoolean(INCLUDE_HYBRID_MATCH, false));
		double minScore = preferences.getDouble(MIN_SCORE, 0.0d);
		if(minScore > 0)
			minScoreTextField.setText(Double.toString(minScore));
		
		MSMSScoringParameter msmsScoringParameter = 
				MSMSScoringParameter.getOptionByName(
				preferences.get(SCORING_PARAMETER, MSMSScoringParameter.ENTROPY_SCORE.name()));
		scoringParameterComboBox.setSelectedItem(msmsScoringParameter);
		
		excludeIfNoIdsLeftCheckBox.setSelected(
				preferences.getBoolean(EXCLUDE_IF_NO_IDS, false));
		
		DecoyExportHandling decoyExportHandling = 
				DecoyExportHandling.getOptionByName(
						preferences.get(DECOY_EXPORT_HANDLING, DecoyExportHandling.NORMAL_ONLY.name()));		
		decoyHitsHadlingComboBox.setSelectedItem(decoyExportHandling);
	}

	public void toggleFormStatus(boolean enabled) {
		
		featureIdSubsetComboBox.setEnabled(enabled);
		regularMatchCheckBox.setEnabled(enabled);
		inSourceCheckBox.setEnabled(enabled);
		hybridMatchCheckBox.setEnabled(enabled);
		scoringParameterComboBox.setEnabled(enabled);
		minScoreTextField.setEnabled(enabled);
		excludeIfNoIdsLeftCheckBox.setEnabled(enabled);
		decoyHitsHadlingComboBox.setEnabled(enabled);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		//	In case "Best hit for each compound" do not allow decoys
		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof FeatureIDSubset) {
			
			FeatureIDSubset selected = (FeatureIDSubset)featureIdSubsetComboBox.getSelectedItem();
			if(selected.equals(FeatureIDSubset.BEST_FOR_EACH_COMPOUND)){
					decoyHitsHadlingComboBox.setSelectedItem(DecoyExportHandling.NORMAL_ONLY);
					decoyHitsHadlingComboBox.setEnabled(false);
			}
			else {
				decoyHitsHadlingComboBox.setEnabled(true);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(
				MainActionCommands.DEFINE_COMPOUND_FILTER_FOR_EXPORT_COMMAND.getName())) {
			showCompoundFilterDefinitionDialog();
		}
		if(e.getActionCommand().equals(
				MainActionCommands.ADD_COMPOUND_FILTER_FOR_EXPORT_COMMAND.getName())) {
			addCompoundFilterDefinition();
		}
		if(e.getActionCommand().equals(
				MainActionCommands.CLEAR_COMPOUND_FILTER_FOR_EXPORT_COMMAND.getName())) {
			clearCompoundFilter();
		}		
	}

	private void clearCompoundFilter() {
		compoundFilter = null;
		compoundFilterButton.setIcon(null);
	}

	private void addCompoundFilterDefinition() {
		
		compoundFilter  = compoundFilterDefinitionDialog.getCompoundIdFilter();
		if(compoundFilter != null)
			compoundFilterButton.setIcon(filterIcon);
		
		compoundFilterDefinitionDialog.dispose();
	}

	private void showCompoundFilterDefinitionDialog() {

		compoundFilterDefinitionDialog = new CompoundFilterDefinitionDialog(this);
		if(compoundFilter != null)
			compoundFilterDefinitionDialog.loadFilter(compoundFilter);
		
		compoundFilterDefinitionDialog.setLocationRelativeTo(this);
		compoundFilterDefinitionDialog.setVisible(true);
	}

	public CompoundIdFilter getCompoundFilter() {
		return compoundFilter;
	}
}














