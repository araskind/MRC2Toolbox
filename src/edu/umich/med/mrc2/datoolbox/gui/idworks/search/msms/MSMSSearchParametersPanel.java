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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.msms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSSearchParameterSet;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;

public class MSMSSearchParametersPanel extends JPanel implements ItemListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7644003624497989531L;
	private Preferences preferences;	
	private static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.gui.idworks.MSMSSearchParametersPanel";
	
	private static final String MZ_ERROR_VALUE = "MZ_ERROR_VALUE";
	private static final String MZ_ERROR_TYPE = "MZ_ERROR_TYPE";
	private static final String RT_ERROR_VALUE = "RT_ERROR_VALUE";
	private static final String IGNORE_RT = "IGNORE_RT";
	private static final String MSMS_SIMILARITY_CUTOFF = "MSMS_SIMILARITY_CUTOFF";
	private static final String ENTROPY_SCORE_MASS_ERROR_VALUE = "ENTROPY_SCORE_MASS_ERROR_VALUE";
	private static final String ENTROPY_SCORE_MASS_ERROR_TYPE = "ENTROPY_SCORE_MASS_ERROR_TYPE";
	private static final String ENTROPY_SCORE_NOISE_CUTOFF = "ENTROPY_SCORE_NOISE_CUTOFF";
	private static final String USE_TABLE_ROW_SUBSET_SET = "USE_TABLE_ROW_SUBSET_SET";	
	private static final String IGNORE_PARENT_ION = "IGNORE_PARENT_ION";
	
	private MSMSSearchParameterSet parameters;
	
	private JFormattedTextField mzErrorValueTextField;
	private JComboBox massErrorTypeComboBox;
	private JFormattedTextField rtWindowTextField;
	private JFormattedTextField msmsSimilarityCutoffTextField;
	private JCheckBox ignoreRtCheckBox;
	private JFormattedTextField esMassErrorTextField;
	private JComboBox esMassErrorTypeComboBox;
	private JFormattedTextField esNoiseCutoffTextField;
	private JRadioButton useFeaturesFromTableRadioButton;
	private JComboBox<TableRowSubset> featureSubsetComboBox;
	private JRadioButton useCompleteSetRadioButton;
	private JCheckBox ignoreParentIonCheckBox;
			
	public MSMSSearchParametersPanel() {
		
		super();
		setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), 
			new CompoundBorder(
				new TitledBorder(
					new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
							new Color(160, 160, 160)), "MSMS search parameters", 
					TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
				new EmptyBorder(10, 10, 10, 10))));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		int rowCount = 0;
		
		JLabel lblNewLabel = new JLabel("M/Z window ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = rowCount;
		add(lblNewLabel, gbc_lblNewLabel);
		
		mzErrorValueTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		mzErrorValueTextField.setColumns(10);
		GridBagConstraints gbc_mzErrorValueTextField = new GridBagConstraints();
		gbc_mzErrorValueTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzErrorValueTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzErrorValueTextField.gridx = 1;
		gbc_mzErrorValueTextField.gridy = rowCount;
		add(mzErrorValueTextField, gbc_mzErrorValueTextField);
		
		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setPreferredSize(new Dimension(60, 22));
		massErrorTypeComboBox.setMinimumSize(new Dimension(60, 22));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = rowCount;
		add(massErrorTypeComboBox, gbc_comboBox);
		
		ignoreParentIonCheckBox = new JCheckBox("Ignore parent ion");
		GridBagConstraints gbc_ignoreParentIonCheckBox = new GridBagConstraints();
		gbc_ignoreParentIonCheckBox.anchor = GridBagConstraints.WEST;
		gbc_ignoreParentIonCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_ignoreParentIonCheckBox.gridx = 3;
		gbc_ignoreParentIonCheckBox.gridy = rowCount;
		add(ignoreParentIonCheckBox, gbc_ignoreParentIonCheckBox);
		
		rowCount++;
		
		JLabel lblNewLabel_1 = new JLabel("RT window ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = rowCount;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		rtWindowTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtWindowTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = rowCount;
		add(rtWindowTextField, gbc_formattedTextField);
		
		JLabel lblNewLabel_2 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = rowCount;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		ignoreRtCheckBox = new JCheckBox("Ignore retention time");
		GridBagConstraints gbc_ignoreRtCheckBox = new GridBagConstraints();
		gbc_ignoreRtCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_ignoreRtCheckBox.gridx = 3;
		gbc_ignoreRtCheckBox.gridy = rowCount;
		add(ignoreRtCheckBox, gbc_ignoreRtCheckBox);
		ignoreRtCheckBox.addItemListener(this);
		
		rowCount++;
		
		JLabel lblNewLabel_3 = new JLabel("MSMS similarity cutoff ");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = rowCount;
		add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		msmsSimilarityCutoffTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		msmsSimilarityCutoffTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 1;
		gbc_formattedTextField_1.gridy = rowCount;
		add(msmsSimilarityCutoffTextField, gbc_formattedTextField_1);
		
		JLabel lblNewLabel_4 = new JLabel("(0.0 - 1.0)");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = rowCount;
		add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		rowCount++;
		
		JPanel entropyScoreParamsPanel = createEntropyScoreParametersBlock();
		GridBagConstraints gbc_entropyScoreParamsPanel = new GridBagConstraints();
		gbc_entropyScoreParamsPanel.anchor = GridBagConstraints.NORTH;
		gbc_entropyScoreParamsPanel.insets = new Insets(0, 0, 0, 5);
		gbc_entropyScoreParamsPanel.gridwidth = 3;
		gbc_entropyScoreParamsPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_entropyScoreParamsPanel.gridx = 0;
		gbc_entropyScoreParamsPanel.gridy = rowCount;
		add(entropyScoreParamsPanel, gbc_entropyScoreParamsPanel);
		
		//`rowCount++;
			
		JPanel featureSubsetPanel = createFeatureSubsetBlock(); 
		GridBagConstraints gbc_featureSubsetPanel = new GridBagConstraints();
		gbc_featureSubsetPanel.anchor = GridBagConstraints.NORTH;
		gbc_featureSubsetPanel.gridwidth = 2;
		gbc_featureSubsetPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureSubsetPanel.gridx = 3;
		gbc_featureSubsetPanel.gridy = rowCount;
		add(featureSubsetPanel, gbc_featureSubsetPanel);

		gridBagLayout.rowWeights = new double[rowCount + 2];
		Arrays.fill(gridBagLayout.rowWeights, 0.0d);
		gridBagLayout.rowWeights[rowCount + 1] = Double.MIN_VALUE;
			
		loadPreferences();
		
		useCompleteSetRadioButton.addItemListener(this);
		useFeaturesFromTableRadioButton.addItemListener(this);
	}
	
	private JPanel createEntropyScoreParametersBlock() {
		
		JPanel entropyScoreParamsPanel = new JPanel();
		entropyScoreParamsPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), 
				new CompoundBorder(
						new TitledBorder(
							new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
									new Color(160, 160, 160)), "Entropy score calculation parameters", 
							TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
						new EmptyBorder(10, 10, 10, 10))));
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		entropyScoreParamsPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("Mass window");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		entropyScoreParamsPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		esMassErrorTextField = new JFormattedTextField(new DecimalFormat("###.###"));
		esMassErrorTextField.setColumns(10);
		GridBagConstraints gbc_massErrorTextField = new GridBagConstraints();
		gbc_massErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_massErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTextField.gridx = 1;
		gbc_massErrorTextField.gridy = 0;
		entropyScoreParamsPanel.add(esMassErrorTextField, gbc_massErrorTextField);
		
		esMassErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		esMassErrorTypeComboBox.setMinimumSize(new Dimension(80, 22));
		esMassErrorTypeComboBox.setPreferredSize(new Dimension(80, 22));
		GridBagConstraints gbc_massErrorTypeComboBox = new GridBagConstraints();
		gbc_massErrorTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_massErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTypeComboBox.gridx = 2;
		gbc_massErrorTypeComboBox.gridy = 0;
		entropyScoreParamsPanel.add(esMassErrorTypeComboBox, gbc_massErrorTypeComboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Noise cutoff");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		entropyScoreParamsPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		esNoiseCutoffTextField = new JFormattedTextField(new DecimalFormat("###.#"));
		esNoiseCutoffTextField.setColumns(10);
		GridBagConstraints gbc_noiseCutoffTextField = new GridBagConstraints();
		gbc_noiseCutoffTextField.insets = new Insets(0, 0, 0, 5);
		gbc_noiseCutoffTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_noiseCutoffTextField.gridx = 1;
		gbc_noiseCutoffTextField.gridy = 1;
		entropyScoreParamsPanel.add(esNoiseCutoffTextField, gbc_noiseCutoffTextField);
		
		JLabel lblNewLabel_2 = new JLabel("% (0.0 - 100.0)");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 1;
		entropyScoreParamsPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		return entropyScoreParamsPanel;
	}
	
	private JPanel createFeatureSubsetBlock() {
		
		JPanel featureSubsetPanel = new JPanel();
		featureSubsetPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), 
				new CompoundBorder(
						new TitledBorder(
							new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
									new Color(160, 160, 160)), "Input MSMS features", 
							TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
						new EmptyBorder(10, 10, 10, 10))));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};		
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		featureSubsetPanel.setLayout(gridBagLayout);
		
		int rowCount = 0;
		
		ButtonGroup bg = new ButtonGroup();
		
		useCompleteSetRadioButton = 
				new JRadioButton("Use complete active feature set");
		useCompleteSetRadioButton.addItemListener(this);
		bg.add(useCompleteSetRadioButton);
		GridBagConstraints gbc_useCompleteSetRadioButton = new GridBagConstraints();
		gbc_useCompleteSetRadioButton.anchor = GridBagConstraints.WEST;
		gbc_useCompleteSetRadioButton.gridx = 0;
		gbc_useCompleteSetRadioButton.gridy = rowCount;
		featureSubsetPanel.add(useCompleteSetRadioButton, gbc_useCompleteSetRadioButton);
		//	useCompleteSetRadioButton.addItemListener(this);
		
		rowCount++;
		
		useFeaturesFromTableRadioButton = 
				new JRadioButton("Use features from the table");
		//	useFeaturesFromTableRadioButton.addItemListener(this);
		bg.add(useFeaturesFromTableRadioButton);
		GridBagConstraints gbc_useFeaturesFromTableRadioButton = new GridBagConstraints();
		gbc_useFeaturesFromTableRadioButton.anchor = GridBagConstraints.WEST;
		gbc_useFeaturesFromTableRadioButton.insets = new Insets(0, 0, 5, 0);
		gbc_useFeaturesFromTableRadioButton.gridx = 0;
		gbc_useFeaturesFromTableRadioButton.gridy = rowCount;
		featureSubsetPanel.add(useFeaturesFromTableRadioButton, gbc_useFeaturesFromTableRadioButton);
		useFeaturesFromTableRadioButton.addItemListener(this);
		
		rowCount++;
		
		featureSubsetComboBox = new JComboBox<TableRowSubset>(
				new DefaultComboBoxModel<TableRowSubset>(TableRowSubset.values()));
		GridBagConstraints gbc_featureSubsetComboBox = new GridBagConstraints();
		gbc_featureSubsetComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_featureSubsetComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureSubsetComboBox.gridx = 0;
		gbc_featureSubsetComboBox.gridy = rowCount;
		featureSubsetPanel.add(featureSubsetComboBox, gbc_featureSubsetComboBox);		
		
		gridBagLayout.rowWeights = new double[rowCount + 2];
		Arrays.fill(gridBagLayout.rowWeights, 0.0d);
		gridBagLayout.rowWeights[rowCount + 1] = Double.MIN_VALUE;
		
		return featureSubsetPanel;
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		
		mzErrorValueTextField.setText(
				Double.toString(preferences.getDouble(MZ_ERROR_VALUE, 20.0d)));
		esMassErrorTypeComboBox.setSelectedItem(
				MassErrorType.getTypeByName(
						preferences.get(MZ_ERROR_TYPE, MassErrorType.ppm.name())));
		ignoreParentIonCheckBox.setSelected(
				preferences.getBoolean(IGNORE_PARENT_ION, false));
		
		rtWindowTextField.setText(
				Double.toString(preferences.getDouble(RT_ERROR_VALUE, 0.1d)));
		ignoreRtCheckBox.setSelected(preferences.getBoolean(IGNORE_RT, true));
		msmsSimilarityCutoffTextField.setText(
				Double.toString(preferences.getDouble(MSMS_SIMILARITY_CUTOFF, 0.5d)));
		
		esMassErrorTextField.setText(
				Double.toString(preferences.getDouble(
						ENTROPY_SCORE_MASS_ERROR_VALUE, 
						MRC2ToolBoxConfiguration.SPECTRUM_ENTROPY_MASS_ERROR_DEFAULT)));
		esMassErrorTypeComboBox.setSelectedItem(
				MassErrorType.getTypeByName(preferences.get(
						ENTROPY_SCORE_MASS_ERROR_TYPE, 
						MRC2ToolBoxConfiguration.SPECTRUM_ENTROPY_MASS_ERROR_TYPE_DEFAULT.name())));
		double esNoiseCutoff = preferences.getDouble(
				ENTROPY_SCORE_NOISE_CUTOFF, 
				MRC2ToolBoxConfiguration.SPECTRUM_ENTROPY_NOISE_CUTOFF_DEFAULT);
		esNoiseCutoffTextField.setText(Double.toString(esNoiseCutoff * 100.0d));

		TableRowSubset trs = TableRowSubset.getOptionByName(
				preferences.get(USE_TABLE_ROW_SUBSET_SET, ""));
		if(trs == null) {			
			useCompleteSetRadioButton.setSelected(true);
			useFeaturesFromTableRadioButton.setSelected(false);
			featureSubsetComboBox.setSelectedIndex(-1);
			featureSubsetComboBox.setEnabled(false);
		}
		else {
			useFeaturesFromTableRadioButton.setSelected(true);
			useCompleteSetRadioButton.setSelected(false);			
			featureSubsetComboBox.setEnabled(true);
			featureSubsetComboBox.setSelectedItem(trs);
		}
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		
		if(!validateParameters().isEmpty())
			return;
	
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.putDouble(MZ_ERROR_VALUE, getMzError());
		if(getMassErrorType() != null)
			preferences.put(MZ_ERROR_TYPE, getMassErrorType().name());
		
		preferences.putBoolean(IGNORE_PARENT_ION, ignoreParentIon());
		
		preferences.putDouble(RT_ERROR_VALUE, getRTError());
		preferences.putBoolean(IGNORE_RT, ignoreRt());
		preferences.putDouble(MSMS_SIMILARITY_CUTOFF, getMsmsSimilarityCutoff());
		
		if(getEntropyScoreMassErrorType() != null)
			preferences.put(MZ_ERROR_TYPE, getMassErrorType().name());
		
		preferences.putDouble(ENTROPY_SCORE_MASS_ERROR_VALUE, getEntropyScoreMassError());
		preferences.putDouble(ENTROPY_SCORE_NOISE_CUTOFF, getEntropyScoreNoizeCutoff());
		
		String trsName = "";
		TableRowSubset trs = getFeaturesTableRowSubset();
		if(trs != null)
			trsName = trs.name();
			
		preferences.put(USE_TABLE_ROW_SUBSET_SET, trsName);		
	}

	public MSMSSearchParameterSet getMSMSSearchParameters() {
		
		if(!validateParameters().isEmpty())
			return null;
		else {
			String name = 
					"MZ " + MRC2ToolBoxConfiguration.getMzFormat().format(getMzError()) 
					+ getMassErrorType().name();
			if(ignoreParentIon())
				name += " | IgnorePrent";
			
			if(ignoreRt())
				name += " | Ignore RT";
			else
				name += " | RT " + MRC2ToolBoxConfiguration.getRtFormat().format(getRTError());
			
			name += " | SCORE " + MRC2ToolBoxConfiguration.getPpmFormat().format(getMsmsSimilarityCutoff());			
			name += " EMZ " + MRC2ToolBoxConfiguration.getMzFormat().format(getEntropyScoreMassError()) 
					+ getEntropyScoreMassErrorType().name();
			name += " | ENC " + MRC2ToolBoxConfiguration.getPpmFormat().format(getEntropyScoreNoizeCutoff());
			
			MSMSSearchParameterSet newParams = new MSMSSearchParameterSet();
			newParams.setName(name);
			newParams.setMzErrorValue(getMzError());
			newParams.setIgnoreParentIon(ignoreParentIon());
			newParams.setMassErrorType(getMassErrorType());
			newParams.setRtErrorValue(getRTError());
			newParams.setIgnoreRt(ignoreRt());
			newParams.setMsmsSimilarityCutoff(getMsmsSimilarityCutoff());
			newParams.setEntropyScoreMassError(getEntropyScoreMassError());
			newParams.setEntropyScoreMassErrorType(getEntropyScoreMassErrorType());
			newParams.setEntropyScoreNoiseCutoff(getEntropyScoreNoizeCutoff());
			String md5 = MSMSClusteringUtils.calculateMSMSSearchParametersMd5(newParams);
			newParams.setMd5(md5);
			
			MSMSClusteringParameterSet existing = 
					MSMSClusterDataSetManager.getMsmsClusteringParameterSetByMd5(md5);
			if(existing == null) {
				try {
					MSMSClusteringDBUtils.addMSMSClusteringParameterSet(newParams);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MSMSClusterDataSetManager.getMsmsClusteringParameters().add(newParams);
				parameters = newParams;
			}
			else {
				parameters = (MSMSSearchParameterSet) existing;
			}
			return parameters;
		}		
	}

	public void setParameters(MSMSSearchParameterSet parameters) {
		
		this.parameters = parameters;
		if(parameters == null)
			return;
		
		mzErrorValueTextField.setText(Double.toString(parameters.getMzErrorValue()));
		esMassErrorTypeComboBox.setSelectedItem(parameters.getMassErrorType());
		rtWindowTextField.setText(Double.toString(parameters.getRtErrorValue()));
		ignoreRtCheckBox.setSelected(parameters.ignoreRt());	
		msmsSimilarityCutoffTextField.setText(Double.toString(parameters.getMsmsSimilarityCutoff()));		
		esMassErrorTextField.setText(Double.toString(parameters.getEntropyScoreMassError()));
		esMassErrorTypeComboBox.setSelectedItem(parameters.getEntropyScoreMassErrorType());
		esNoiseCutoffTextField.setText(Double.toString(parameters.getEntropyScoreNoiseCutoff()));
	}
	
	public double getMzError() {	
		return Double.parseDouble(mzErrorValueTextField.getText().trim());
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)esMassErrorTypeComboBox.getSelectedItem();
	}	
	
	public boolean ignoreParentIon() {
		return ignoreParentIonCheckBox.isSelected();
	}
	
	public double getRTError() {	
		return Double.parseDouble(rtWindowTextField.getText().trim());
	}
	
	public boolean ignoreRt() {
		return ignoreRtCheckBox.isSelected();
	}
	
	public double getMsmsSimilarityCutoff() {	
		return Double.parseDouble(msmsSimilarityCutoffTextField.getText().trim());
	}
	
	public double getEntropyScoreMassError() {
		
		double massError = 0.0d;
		if(esMassErrorTextField.getText() != null 
				&& !esMassErrorTextField.getText().trim().isEmpty())
			massError = Double.parseDouble(esMassErrorTextField.getText().trim());
		
		return massError;
	}
	
	public MassErrorType getEntropyScoreMassErrorType() {
		return (MassErrorType)esMassErrorTypeComboBox.getSelectedItem();
	}	
	
	public double getEntropyScoreNoizeCutoff() {
		
		double noiseCutoff = 0.0d;
		if(esNoiseCutoffTextField.getText() != null 
				&& !esNoiseCutoffTextField.getText().trim().isEmpty())
			noiseCutoff = Double.parseDouble(esNoiseCutoffTextField.getText().trim());
		
		return noiseCutoff / 100.0d;
	}
	
	public TableRowSubset getFeaturesTableRowSubset() {
		return (TableRowSubset)featureSubsetComboBox.getSelectedItem();
	}
	
	public Collection<String>validateParameters(){
		
		Collection<String>errors = new ArrayList<String>();
		
		if(getMzError() == 0.0d)
			errors.add("M/Z error must be > 0");
		
		if(getMassErrorType() == null)
			errors.add("M/Z error type must be specified");
			
		if(getRTError() == 0.0d && !ignoreRt())
			errors.add("RT error must be > 0");
		
		if(getMsmsSimilarityCutoff() < 0.0d || getMsmsSimilarityCutoff() > 1.0d)
			errors.add("MSMS similarity cutoff value must be between 0 and 1");
		
		if(getEntropyScoreMassError() == 0)
			errors.add("Mass error for entropy scoring must be > 0");
		
		if(getEntropyScoreMassErrorType() == null)
			errors.add("Mass error type for entropy scoring must be specified");
		
		if(getEntropyScoreNoizeCutoff() < 0 || getEntropyScoreNoizeCutoff() > 1)
			errors.add("Noise cutoff value for entropy scoring must be between 0 and 100");
				
		return errors;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if(e.getSource().equals(ignoreRtCheckBox)) {
			
			rtWindowTextField.setEditable(!ignoreRtCheckBox.isSelected());
			rtWindowTextField.setEnabled(!ignoreRtCheckBox.isSelected());
		}
		if(e.getSource().equals(useFeaturesFromTableRadioButton) 
				|| e.getSource().equals(useCompleteSetRadioButton) ) {
			
			if(useFeaturesFromTableRadioButton.isSelected()) {
				
				featureSubsetComboBox.setEnabled(true);
				if(featureSubsetComboBox.getSelectedIndex() == -1)
					featureSubsetComboBox.setSelectedItem(TableRowSubset.FILTERED);
			}
			if(useCompleteSetRadioButton.isSelected()) {
				
				featureSubsetComboBox.setSelectedIndex(-1);			
				featureSubsetComboBox.setEnabled(false);
			}	
		}
	}
}













