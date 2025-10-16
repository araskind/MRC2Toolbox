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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSClusterFilterDialog extends JDialog implements BackedByPreferences, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2851758377902206542L;
	
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 32);
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.gui.idworks.MSMSClusterFilterDialog";
	private static final String LOOKUP_FEATURE_NAME = "LOOKUP_FEATURE_NAME";
	private static final String MZ_VALUE = "MZ_VALUE";
	private static final String MZ_ERROR = "MZ_ERROR";
	private static final String MZ_ERROR_TYPE = "MZ_ERROR_TYPE";
	private static final String RT_VALUE = "RT_VALUE";
	private static final String RT_ERROR = "RT_ERROR";
	private static final String COMPOUND_NAME = "COMPOUND_NAME";
	private static final String FORMULA = "FORMULA";
	private static final String IDENTIFIED_ONLY = "IDENTIFIED_ONLY";
	
	private static final NumberFormat twoDecFormat = new DecimalFormat("###.##");
	private static final NumberFormat wholeNumDecFormat = new DecimalFormat("###");
	
	private static final String RESET_COMMAND = "Reset";
	private JTextField lookupNameTextField;
	private JFormattedTextField mzTextField;
	private JFormattedTextField mzErrorTextField;
	private JComboBox mzErrorTypeComboBox;
	private JFormattedTextField rtTextField;
	private JFormattedTextField rtErrorTextField;
	private JTextField compoundNameTextField;
	private JTextField formulaTextField;
	private JCheckBox identifiedOnlyCheckBox;
	
	public MSMSClusterFilterDialog(ActionListener listener) {
		super();
		setTitle("Filter MSMS clusters");
		setIconImage(((ImageIcon)filterIcon).getImage());
		setPreferredSize(new Dimension(640, 300));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Lookup feature name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		lookupNameTextField = new JTextField();
		GridBagConstraints gbc_lookupNameTextField = new GridBagConstraints();
		gbc_lookupNameTextField.gridwidth = 4;
		gbc_lookupNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_lookupNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_lookupNameTextField.gridx = 1;
		gbc_lookupNameTextField.gridy = 0;
		panel_1.add(lookupNameTextField, gbc_lookupNameTextField);
		lookupNameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("M/Z");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		mzTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		mzTextField.setColumns(10);
		GridBagConstraints gbc_mzTextField = new GridBagConstraints();
		gbc_mzTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzTextField.anchor = GridBagConstraints.WEST;
		gbc_mzTextField.gridx = 1;
		gbc_mzTextField.gridy = 1;
		panel_1.add(mzTextField, gbc_mzTextField);
		
		JLabel lblNewLabel_2 = new JLabel("+/-");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 1;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		mzErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		mzErrorTextField.setColumns(10);
		GridBagConstraints gbc_mzErrorTextField = new GridBagConstraints();
		gbc_mzErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzErrorTextField.gridx = 3;
		gbc_mzErrorTextField.gridy = 1;
		panel_1.add(mzErrorTextField, gbc_mzErrorTextField);
		
		mzErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		mzErrorTypeComboBox.setPreferredSize(new Dimension(100, 22));
		mzErrorTypeComboBox.setMinimumSize(new Dimension(80, 22));
		GridBagConstraints gbc_mzErrorTypeomboBox = new GridBagConstraints();
		gbc_mzErrorTypeomboBox.insets = new Insets(0, 0, 5, 0);
		gbc_mzErrorTypeomboBox.anchor = GridBagConstraints.WEST;
		gbc_mzErrorTypeomboBox.gridx = 4;
		gbc_mzErrorTypeomboBox.gridy = 1;
		panel_1.add(mzErrorTypeComboBox, gbc_mzErrorTypeomboBox);
		
		JLabel lblNewLabel_3 = new JLabel("RT");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 2;
		panel_1.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		rtTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtTextField.setColumns(10);
		GridBagConstraints gbc_rtTextField = new GridBagConstraints();
		gbc_rtTextField.anchor = GridBagConstraints.WEST;
		gbc_rtTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtTextField.gridx = 1;
		gbc_rtTextField.gridy = 2;
		panel_1.add(rtTextField, gbc_rtTextField);
		
		JLabel lblNewLabel_2_1 = new JLabel("+/-");
		GridBagConstraints gbc_lblNewLabel_2_1 = new GridBagConstraints();
		gbc_lblNewLabel_2_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2_1.gridx = 2;
		gbc_lblNewLabel_2_1.gridy = 2;
		panel_1.add(lblNewLabel_2_1, gbc_lblNewLabel_2_1);
		
		rtErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtErrorTextField.setColumns(10);
		GridBagConstraints gbc_rtErrorTextField = new GridBagConstraints();
		gbc_rtErrorTextField.anchor = GridBagConstraints.WEST;
		gbc_rtErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtErrorTextField.gridx = 3;
		gbc_rtErrorTextField.gridy = 2;
		panel_1.add(rtErrorTextField, gbc_rtErrorTextField);
		
		JLabel lblNewLabel_4 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.gridx = 4;
		gbc_lblNewLabel_4.gridy = 2;
		panel_1.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		JLabel lblNewLabel_5 = new JLabel("Compound name");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 3;
		panel_1.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		compoundNameTextField = new JTextField();
		GridBagConstraints gbc_compoundNameTextField = new GridBagConstraints();
		gbc_compoundNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_compoundNameTextField.gridwidth = 4;
		gbc_compoundNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_compoundNameTextField.gridx = 1;
		gbc_compoundNameTextField.gridy = 3;
		panel_1.add(compoundNameTextField, gbc_compoundNameTextField);
		compoundNameTextField.setColumns(10);
		
		JLabel lblNewLabel_6 = new JLabel("Formula");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = 4;
		panel_1.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		formulaTextField = new JTextField();
		GridBagConstraints gbc_formulaTextField = new GridBagConstraints();
		gbc_formulaTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formulaTextField.gridwidth = 3;
		gbc_formulaTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formulaTextField.gridx = 1;
		gbc_formulaTextField.gridy = 4;
		panel_1.add(formulaTextField, gbc_formulaTextField);
		formulaTextField.setColumns(10);
		
		identifiedOnlyCheckBox = new JCheckBox("Show identified only");
		GridBagConstraints gbc_identifiedOnlyCheckBox = new GridBagConstraints();
		gbc_identifiedOnlyCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_identifiedOnlyCheckBox.gridwidth = 4;
		gbc_identifiedOnlyCheckBox.anchor = GridBagConstraints.WEST;
		gbc_identifiedOnlyCheckBox.gridx = 1;
		gbc_identifiedOnlyCheckBox.gridy = 5;
		panel_1.add(identifiedOnlyCheckBox, gbc_identifiedOnlyCheckBox);
		
		JLabel lblNewLabel_7 = new JLabel("      ");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_7.gridx = 0;
		gbc_lblNewLabel_7.gridy = 7;
		panel_1.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		JButton btnNewButton = new JButton("Reset form");
		btnNewButton.setActionCommand(RESET_COMMAND);
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 7;
		panel_1.add(btnNewButton, gbc_btnNewButton);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		JButton filterButton = new JButton(MainActionCommands.FILTER_MSMS_CLUSTERS_COMMAND.getName());
		filterButton.setActionCommand(MainActionCommands.FILTER_MSMS_CLUSTERS_COMMAND.getName());
		filterButton.addActionListener(listener);
		panel.add(filterButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(filterButton);
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(filterButton);
		loadPreferences();
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(RESET_COMMAND))
			resetForm();
	}
	
	private void resetForm() {

		lookupNameTextField.setText("");
		mzTextField.setText("");
		mzErrorTextField.setText("");
		mzErrorTypeComboBox.setSelectedIndex(-1);
		rtTextField.setText("");
		rtErrorTextField.setText("");
		compoundNameTextField.setText("");
		formulaTextField.setText("");
		identifiedOnlyCheckBox.setSelected(false);
	}

	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	public String getLookupFeatureName() {
		return lookupNameTextField.getText().trim();
	}
	
	public String getCompoundName() {
		return compoundNameTextField.getText().trim();
	}
	
	public String getFormula() {
		return formulaTextField.getText().trim();
	}
	
	public boolean showIdentifiedOnly() {
		return identifiedOnlyCheckBox.isSelected();
	}
	
	public double getMzValue() {
		
		if(mzTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(mzTextField.getText().trim());
	}
	
	public double getMzError() {
		
		if(mzErrorTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(mzErrorTextField.getText().trim());
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)mzErrorTypeComboBox.getSelectedItem();
	}
	
	public double getRTValue() {
		
		if(rtTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(rtTextField.getText().trim());
	}
	
	public double getRTError() {
		
		if(rtErrorTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(rtErrorTextField.getText().trim());
	}
	
	public Range getMzRange() {
		
		double mz = getMzValue();
		double mzError =  getMzError();
		MassErrorType met = getMassErrorType();
		
		if(mz > 0.0d && mzError > 0.0d && met != null)
			return MsUtils.createMassRange(mz, mzError, met);
		else
			return null;
	}
	
	public Range getRTRange() {
		
		double rt = getRTValue();
		double rtError =  getRTError();
		
		if(rt > 0.0d && rtError > 0.0d)
			return new Range(rt - rtError, rt + rtError);
		else
			return null;
	}
	
	public Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getMzValue() > 0.0d) {
			
			if(getMzError() == 0.0d)
				errors.add("M/Z error must be specified");
			
			if(getMassErrorType() == null)
				errors.add("M/Z error type must be specified");
		}
		if(getRTValue() > 0.0d && getRTError() == 0.0d)
			errors.add("RT error must be specified");
		
		if(getMzValue() == 0.0d && getRTValue() == 0.0d 
				&& getLookupFeatureName().isEmpty() && getCompoundName().isEmpty()
				&& getFormula().isEmpty() && !showIdentifiedOnly()) {
			errors.add("No filtering criteria specified.");
		}
		return errors;
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;

		lookupNameTextField.setText(preferences.get(LOOKUP_FEATURE_NAME, ""));
		double mz = preferences.getDouble(MZ_VALUE, 0.0d);
		if(mz > 0.0d)
			mzTextField.setText(Double.toString(mz));
		else
			mzTextField.setText("");
		
		double mzError = preferences.getDouble(MZ_ERROR, 20.0d);
		if(mzError > 0.0d)
			mzErrorTextField.setText(Double.toString(mzError));
		else
			mzErrorTextField.setText("");
		
		MassErrorType met = MassErrorType.getTypeByName(
				preferences.get(MZ_ERROR_TYPE, MassErrorType.ppm.name()));
		mzErrorTypeComboBox.setSelectedItem(met);
		
		double rt = preferences.getDouble(RT_VALUE, 0.0d);
		if(rt > 0.0d)
			rtTextField.setText(Double.toString(rt));
		else
			rtTextField.setText("");
		
		double rtError = preferences.getDouble(RT_ERROR, 0.15d);
		if(rtError > 0.0d)
			rtErrorTextField.setText(Double.toString(rtError));
		else
			rtErrorTextField.setText("");
		
		compoundNameTextField.setText(preferences.get(COMPOUND_NAME, ""));
		formulaTextField.setText(preferences.get(FORMULA, ""));
		identifiedOnlyCheckBox.setSelected(preferences.getBoolean(IDENTIFIED_ONLY, false));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		
		preferences.put(LOOKUP_FEATURE_NAME, getLookupFeatureName());
		preferences.putDouble(MZ_VALUE, getMzValue());
		preferences.putDouble(MZ_ERROR, getMzError());
		MassErrorType met = getMassErrorType();
		if(met == null)
			met = MassErrorType.ppm;
		
		preferences.put(MZ_ERROR_TYPE, met.name());
		
		preferences.putDouble(RT_VALUE, getRTValue());
		preferences.putDouble(RT_ERROR, getRTError());
		
		preferences.put(COMPOUND_NAME, getCompoundName());
		preferences.put(FORMULA, getFormula());
		preferences.putBoolean(IDENTIFIED_ONLY, showIdentifiedOnly());
	}
}
