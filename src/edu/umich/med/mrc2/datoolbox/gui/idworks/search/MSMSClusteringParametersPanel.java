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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;

public class MSMSClusteringParametersPanel extends JPanel implements BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7644003624497989531L;
	private Preferences preferences;	
	private static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.gui.idworks.MSMSClusteringParametersPanel";
	
	private static final String MZ_ERROR_VALUE = "MZ_ERROR_VALUE";
	private static final String MZ_ERROR_TYPE = "MZ_ERROR_TYPE";
	private static final String RT_ERROR_VALUE = "RT_ERROR_VALUE";
	private static final String MSMS_SIMILARITY_CUTOFF = "MSMS_SIMILARITY_CUTOFF";
	
	private MSMSClusteringParameterSet parameters;
	
	private JFormattedTextField mzErrorValueTextField;
	private JComboBox massErrorTypeComboBox;
	private JFormattedTextField rtWindowTextField;
	private JFormattedTextField msmsSimilarityCutoffTextField;
			
	public MSMSClusteringParametersPanel() {
		super();
		setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), 
			new CompoundBorder(
				new TitledBorder(
					new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
							new Color(160, 160, 160)), "MSMS clustering parameters", 
					TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
				new EmptyBorder(10, 10, 10, 10))));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("M/Z window ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		mzErrorValueTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		mzErrorValueTextField.setColumns(10);
		GridBagConstraints gbc_mzErrorValueTextField = new GridBagConstraints();
		gbc_mzErrorValueTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzErrorValueTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzErrorValueTextField.gridx = 1;
		gbc_mzErrorValueTextField.gridy = 0;
		add(mzErrorValueTextField, gbc_mzErrorValueTextField);
		
		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setPreferredSize(new Dimension(60, 22));
		massErrorTypeComboBox.setMinimumSize(new Dimension(60, 22));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 0;
		add(massErrorTypeComboBox, gbc_comboBox);
		
		JLabel lblNewLabel_1 = new JLabel("RT window ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		rtWindowTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtWindowTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 1;
		add(rtWindowTextField, gbc_formattedTextField);
		
		JLabel lblNewLabel_2 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 1;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("MSMS similarity cutoff ");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 2;
		add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		msmsSimilarityCutoffTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		msmsSimilarityCutoffTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 1;
		gbc_formattedTextField_1.gridy = 2;
		add(msmsSimilarityCutoffTextField, gbc_formattedTextField_1);
		
		JLabel lblNewLabel_4 = new JLabel("(0.0 - 1.0)");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 2;
		add(lblNewLabel_4, gbc_lblNewLabel_4);

		loadPreferences();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		
		mzErrorValueTextField.setText(
				Double.toString(preferences.getDouble(MZ_ERROR_VALUE, 20.0d)));
		massErrorTypeComboBox.setSelectedItem(
				MassErrorType.getTypeByName(preferences.get(MZ_ERROR_TYPE, MassErrorType.ppm.name())));
		rtWindowTextField.setText(
				Double.toString(preferences.getDouble(RT_ERROR_VALUE, 0.1d)));
		msmsSimilarityCutoffTextField.setText(
				Double.toString(preferences.getDouble(MSMS_SIMILARITY_CUTOFF, 0.5d)));
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
		
		preferences.putDouble(RT_ERROR_VALUE, getRTError());
		preferences.putDouble(MSMS_SIMILARITY_CUTOFF, getMsmsSimilarityCutoff());
	}

	public MSMSClusteringParameterSet getParameters() {
		
		if(!validateParameters().isEmpty())
			return null;
		else {
			String name = 
					"MZ " + MRC2ToolBoxConfiguration.getMzFormat().format(getMzError()) + 
					" " + getMassErrorType().name() + 
					" | RT " + MRC2ToolBoxConfiguration.getRtFormat().format(getRTError()) +
					" | SCORE " + MRC2ToolBoxConfiguration.getPpmFormat().format(getMsmsSimilarityCutoff());
			MSMSClusteringParameterSet newParams = 
					new MSMSClusteringParameterSet(
							name, 
							getMzError(), 
							getMassErrorType(),
							getRTError(), 
							getMsmsSimilarityCutoff());
			String md5 = MSMSClusteringUtils.calculateCLusteringParametersMd5(newParams);
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
				parameters = existing;
			}
		}
		return parameters;
	}

	public void setParameters(MSMSClusteringParameterSet parameters) {
		
		this.parameters = parameters;
		if(parameters == null)
			return;
		
		mzErrorValueTextField.setText(Double.toString(parameters.getMzErrorValue()));
		massErrorTypeComboBox.setSelectedItem(parameters.getMassErrorType());
		rtWindowTextField.setText(Double.toString(parameters.getRtErrorValue()));
		msmsSimilarityCutoffTextField.setText(Double.toString(parameters.getMsmsSimilarityCutoff()));
	}
	
	public Collection<String>validateParameters(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getMzError() == 0.0d)
			errors.add("M/Z error must be > 0");
		
		if(getMassErrorType() == null)
			errors.add("M/Z error type must be specified");
			
		if(getRTError() == 0.0d)
			errors.add("RT error must be > 0");
		
		if(getMsmsSimilarityCutoff() < 0.0d || getMsmsSimilarityCutoff() > 1.0d)
			errors.add("MSMS similarity cutoff value must be between 0 and 1");
				
		return errors;
	}
	
	public double getMzError() {	
		return Double.parseDouble(mzErrorValueTextField.getText().trim());
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();
	}
	
	public double getRTError() {	
		return Double.parseDouble(rtWindowTextField.getText().trim());
	}
	
	public double getMsmsSimilarityCutoff() {	
		return Double.parseDouble(msmsSimilarityCutoffTextField.getText().trim());
	}
}













