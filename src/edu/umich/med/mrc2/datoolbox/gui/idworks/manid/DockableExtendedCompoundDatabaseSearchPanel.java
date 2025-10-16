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

package edu.umich.med.mrc2.datoolbox.gui.idworks.manid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundNameScope;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.enums.StringMatchFidelity;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableExtendedCompoundDatabaseSearchPanel 	
		extends DefaultSingleCDockable implements ActionListener, BackedByPreferences{

	private static final Icon componentIcon = GuiUtils.getIcon("dbLookup", 16);
	
	private Preferences preferences;
	public static final String PREFS_NODE = DockableExtendedCompoundDatabaseSearchPanel.class.getName();
	public static final String MZ_ERROR = "MZ_ERROR";
	public static final String MZ_ERROR_TYPE = "MZ_ERROR_TYPE";

	private JTextField nameTextField;
	private JTextField formulaTextField;
	private JTextField idTextField;
	private JTextField inChiTextField;
	private JButton clearButton;
	private JButton searchButton;
	private JComboBox<MassErrorType> massErrorTypeComboBox;
	private JFormattedTextField massErrorTextField;
	private JFormattedTextField massTextField;

	public static final String CLEAR_FORM = "Clear form";
	private JRadioButton primaryNamesOnlyRadioButton;
	private JRadioButton synonymsRadioButton;
	private JRadioButton chckbxAllowSpellingErrors;
	private JRadioButton partialMatchRadioButton;
	private JRadioButton rdbtnExactMatchOnly;
	private JScrollPane scrollPane;
	private AdductChooserTable adductChooserTable;
	private Polarity polarity;

	public DockableExtendedCompoundDatabaseSearchPanel(ActionListener listener) {

		super("DockableExtendedCompoundDatabaseSearchPanel", componentIcon, "Search compound database", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(panel, BorderLayout.CENTER);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 151, 37, 0, 82, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		panel.add(lblName, gbc_lblName);

		nameTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 4;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(nameTextField, gbc_textField);
		nameTextField.setColumns(10);

		ButtonGroup nameScopeButtonGroup = new ButtonGroup();

		primaryNamesOnlyRadioButton = new JRadioButton("Search primary names only");
		primaryNamesOnlyRadioButton.setSelected(true);
		GridBagConstraints gbc_primaryNamesOnlyRadioButton = new GridBagConstraints();
		gbc_primaryNamesOnlyRadioButton.anchor = GridBagConstraints.WEST;
		gbc_primaryNamesOnlyRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_primaryNamesOnlyRadioButton.gridx = 1;
		gbc_primaryNamesOnlyRadioButton.gridy = 1;
		panel.add(primaryNamesOnlyRadioButton, gbc_primaryNamesOnlyRadioButton);
		nameScopeButtonGroup.add(primaryNamesOnlyRadioButton);

		synonymsRadioButton = new JRadioButton("Search synonyms");
		GridBagConstraints gbc_synonymsCheckBox = new GridBagConstraints();
		gbc_synonymsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_synonymsCheckBox.gridwidth = 3;
		gbc_synonymsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_synonymsCheckBox.gridx = 2;
		gbc_synonymsCheckBox.gridy = 1;
		panel.add(synonymsRadioButton, gbc_synonymsCheckBox);
		nameScopeButtonGroup.add(synonymsRadioButton);

		ButtonGroup nameFidelityButtonGroup = new ButtonGroup();

		rdbtnExactMatchOnly = new JRadioButton("Exact match only");
		rdbtnExactMatchOnly.setSelected(true);
		GridBagConstraints gbc_rdbtnExactMatchOnly = new GridBagConstraints();
		gbc_rdbtnExactMatchOnly.anchor = GridBagConstraints.WEST;
		gbc_rdbtnExactMatchOnly.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnExactMatchOnly.gridx = 1;
		gbc_rdbtnExactMatchOnly.gridy = 2;
		panel.add(rdbtnExactMatchOnly, gbc_rdbtnExactMatchOnly);
		nameFidelityButtonGroup.add(rdbtnExactMatchOnly);

		chckbxAllowSpellingErrors = new JRadioButton("Allow spelling errors");
		GridBagConstraints gbc_chckbxAllowSpellingErrors = new GridBagConstraints();
		gbc_chckbxAllowSpellingErrors.gridwidth = 2;
		gbc_chckbxAllowSpellingErrors.anchor = GridBagConstraints.WEST;
		gbc_chckbxAllowSpellingErrors.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxAllowSpellingErrors.gridx = 2;
		gbc_chckbxAllowSpellingErrors.gridy = 2;
		panel.add(chckbxAllowSpellingErrors, gbc_chckbxAllowSpellingErrors);
		nameFidelityButtonGroup.add(chckbxAllowSpellingErrors);

		partialMatchRadioButton = new JRadioButton("Partial match");
		GridBagConstraints gbc_partialMatchRadioButton = new GridBagConstraints();
		gbc_partialMatchRadioButton.anchor = GridBagConstraints.WEST;
		gbc_partialMatchRadioButton.insets = new Insets(0, 0, 5, 0);
		gbc_partialMatchRadioButton.gridx = 4;
		gbc_partialMatchRadioButton.gridy = 2;
		panel.add(partialMatchRadioButton, gbc_partialMatchRadioButton);
		nameFidelityButtonGroup.add(partialMatchRadioButton);

		JLabel lblFormula = new JLabel("Formula");
		GridBagConstraints gbc_lblFormula = new GridBagConstraints();
		gbc_lblFormula.anchor = GridBagConstraints.EAST;
		gbc_lblFormula.insets = new Insets(0, 0, 5, 5);
		gbc_lblFormula.gridx = 0;
		gbc_lblFormula.gridy = 3;
		panel.add(lblFormula, gbc_lblFormula);

		formulaTextField = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 3;
		panel.add(formulaTextField, gbc_textField_1);
		formulaTextField.setColumns(10);

		JLabel lblId = new JLabel("ID");
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 2;
		gbc_lblId.gridy = 3;
		panel.add(lblId, gbc_lblId);

		idTextField = new JTextField();
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.gridwidth = 2;
		gbc_textField_2.insets = new Insets(0, 0, 5, 0);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 3;
		gbc_textField_2.gridy = 3;
		panel.add(idTextField, gbc_textField_2);
		idTextField.setColumns(10);
		
		JLabel lblInchyKey = new JLabel("InChi key");
		GridBagConstraints gbc_lblInchyKey = new GridBagConstraints();
		gbc_lblInchyKey.anchor = GridBagConstraints.EAST;
		gbc_lblInchyKey.insets = new Insets(0, 0, 5, 5);
		gbc_lblInchyKey.gridx = 0;
		gbc_lblInchyKey.gridy = 4;
		panel.add(lblInchyKey, gbc_lblInchyKey);
		
		inChiTextField = new JTextField();
		GridBagConstraints gbc_textField_3 = new GridBagConstraints();
		gbc_textField_3.insets = new Insets(0, 0, 5, 0);
		gbc_textField_3.gridwidth = 4;
		gbc_textField_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_3.gridx = 1;
		gbc_textField_3.gridy = 4;
		panel.add(inChiTextField, gbc_textField_3);
		inChiTextField.setColumns(10);

		JLabel lblMass = new JLabel("Mass");
		GridBagConstraints gbc_lblMass = new GridBagConstraints();
		gbc_lblMass.insets = new Insets(0, 0, 5, 5);
		gbc_lblMass.anchor = GridBagConstraints.EAST;
		gbc_lblMass.gridx = 0;
		gbc_lblMass.gridy = 5;
		panel.add(lblMass, gbc_lblMass);

		massTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		massTextField.setColumns(12);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 5;
		panel.add(massTextField, gbc_formattedTextField);

		JLabel lblError = new JLabel("Error");
		GridBagConstraints gbc_lblErrorType = new GridBagConstraints();
		gbc_lblErrorType.anchor = GridBagConstraints.EAST;
		gbc_lblErrorType.insets = new Insets(0, 0, 5, 5);
		gbc_lblErrorType.gridx = 2;
		gbc_lblErrorType.gridy = 5;
		panel.add(lblError, gbc_lblErrorType);

		massErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massErrorTextField.setColumns(6);
		massErrorTextField.setText(Double.toString(MRC2ToolBoxConfiguration.getMassAccuracy()));
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 3;
		gbc_formattedTextField_1.gridy = 5;
		panel.add(massErrorTextField, gbc_formattedTextField_1);

		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setSelectedItem(MassErrorType.ppm);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 4;
		gbc_comboBox.gridy = 5;
		panel.add(massErrorTypeComboBox, gbc_comboBox);

		clearButton = new JButton("Clear form");
		clearButton.setActionCommand(CLEAR_FORM);
		clearButton.addActionListener(this);
		
		JLabel lblNewLabel = new JLabel("Adduct");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 6;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		adductChooserTable = new AdductChooserTable();
		scrollPane = new JScrollPane(adductChooserTable);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 6;
		panel.add(scrollPane, gbc_scrollPane);
		clearButton.setPreferredSize(new Dimension(83, 25));
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.gridwidth = 2;
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.gridx = 2;
		gbc_btnNewButton_1.gridy = 7;
		panel.add(clearButton, gbc_btnNewButton_1);

		searchButton = new JButton("Search");
		searchButton.setActionCommand(MainActionCommands.SEARCH_DATABASE_COMMAND.getName());
		searchButton.addActionListener(listener);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 7;
		panel.add(searchButton, gbc_btnNewButton);
	}

	public void setActiveFeature(MsFeature activeFeature) {
		
		clearForm();
		MassSpectrum spectrum = activeFeature.getSpectrum();
		if(spectrum == null)	
			return;
		
		adductChooserTable.loadAdductsForFeature(activeFeature);
		polarity = activeFeature.getPolarity();
		TandemMassSpectrum msms = spectrum.getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
		if(msms != null)
			setMz(msms.getParent().getMz());	
		else
			setMz(spectrum.getPrimaryAdductBasePeakMz());
	}
	
	public void setMz(double mz) {
		massTextField.setText(Double.toString(mz));
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(CLEAR_FORM))
			clearForm();
	}

	public void clearForm() {

		nameTextField.setText("");
		formulaTextField.setText("");
		massTextField.setText("");
		idTextField.setText("");
		inChiTextField.setText("");
		
		adductChooserTable.clearSelection();
		if(polarity != null)
			adductChooserTable.selectAdduct(AdductManager.getDefaultAdductForPolarity(polarity));
	}

	public String getCompoundName() {
		return nameTextField.getText().trim();
	}

	public String getFormula() {
		return formulaTextField.getText().trim();
	}

	public void setFormula(String newFormula) {
		formulaTextField.setText(newFormula.trim());
	}

	public String getId() {
		return idTextField.getText().trim();
	}

	public void setId(String accession) {
		idTextField.setText(accession.trim());
	}

	public String getInChi() {
		return inChiTextField.getText().trim();
	}

	public boolean getExactMatch() {
		return primaryNamesOnlyRadioButton.isSelected();
	}

	public boolean searchSynonyms() {
		return synonymsRadioButton.isSelected();
	}

	public boolean allowSpellingErrors() {
		return chckbxAllowSpellingErrors.isSelected();
	}

	public void setLookupMass(double mass) {
		massTextField.setText(Double.toString(mass));
	}

	public void setMassError(double error, MassErrorType type) {
		massErrorTextField.setText(Double.toString(error));
		massErrorTypeComboBox.setSelectedItem(type);
	}
	
	public Range getMassRangeOld() {

		String massString = massTextField.getText().trim();
		Range massRange = null;
		double accuracy = 0.0d;
		double mass = 0.0d;

		if(!massString.isEmpty()) {

			String accuracyString = massErrorTextField.getText().trim();

			if(!accuracyString.isEmpty())
				accuracy = Double.valueOf(accuracyString);

			mass = Double.valueOf(massString);
			massRange = MsUtils.createMassRange(mass, accuracy, (MassErrorType) massErrorTypeComboBox.getSelectedItem());
		}
		return massRange;
	}
	
	public Range getMassRange() {
		
		String massString = massTextField.getText().trim();
		if(massString.isEmpty())
			return null;
		
		String accuracyString = massErrorTextField.getText().trim();
		if(accuracyString.isEmpty())
			return null;
		
		double mass = Double.valueOf(massString);
		double accuracy = Double.valueOf(accuracyString);
		MassErrorType massErrorType = (MassErrorType) massErrorTypeComboBox.getSelectedItem();
		Adduct selectedAdduct = adductChooserTable.getSelectedAdduct();

		//	Calculate range without considering adduct 
		if(selectedAdduct == null || selectedAdduct.equals(AdductManager.getDefaultAdductForCharge(0))) {
			return MsUtils.createMassRange(mass, accuracy, massErrorType);
		}
		else {
			double adjustedMass = MsUtils.getNeutralMassForAdduct(mass, adductChooserTable.getSelectedAdduct());
			return MsUtils.createMassRange(adjustedMass, accuracy, massErrorType);
		}
	}
	
	public Adduct getSelectedAdduct() {
		return adductChooserTable.getSelectedAdduct();
	}

	public CompoundNameScope getNameScope() {

		if(primaryNamesOnlyRadioButton.isSelected())
			return CompoundNameScope.PRIMARY_ONLY;
		else
			return CompoundNameScope.ALL_SYNONYMS;
	}

	public StringMatchFidelity getNameMatchFidelity() {

		if(rdbtnExactMatchOnly.isSelected())
			return StringMatchFidelity.EXACT_MATCH;
		else if(chckbxAllowSpellingErrors.isSelected())
			return StringMatchFidelity.UTL_MATCH;
		else
			return StringMatchFidelity.LIKE_MATCH;
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();
	}
	
	public double getMassErrorValue() {
		return Double.parseDouble(massErrorTextField.getText());
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		
		massErrorTextField.setText(Double.toString(preferences.getDouble(MZ_ERROR, 20.0d)));
		MassErrorType met = MassErrorType.getTypeByName(
				preferences.get(MZ_ERROR_TYPE, MassErrorType.ppm.name()));
		massErrorTypeComboBox.setSelectedItem(met);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userRoot().node(PREFS_NODE);
		Double mzError = getMassErrorValue();
		if(mzError != null)
			preferences.putDouble(MZ_ERROR, mzError);

		preferences.put(MZ_ERROR_TYPE, getMassErrorType().name());	
	}
}




















