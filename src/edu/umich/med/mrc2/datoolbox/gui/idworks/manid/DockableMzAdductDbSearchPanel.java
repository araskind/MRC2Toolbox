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

package edu.umich.med.mrc2.datoolbox.gui.idworks.manid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableMzAdductDbSearchPanel extends DefaultSingleCDockable implements ActionListener, BackedByPreferences {
	
	private static final Icon componentIcon = GuiUtils.getIcon("chemModListSearch", 16);
	private Preferences preferences;
	public static final String PREFS_NODE = DockableMzAdductDbSearchPanel.class.getName();
	public static final String MZ_ERROR = "MZ_ERROR";
	public static final String MZ_ERROR_TYPE = "MZ_ERROR_TYPE";
	
	public static final String CLEAR_FORM = "Clear form";
	private JButton clearButton;
	private JButton searchButton;
	private JComboBox<MassErrorType> massErrorTypeComboBox;
	private JFormattedTextField massErrorTextField;
	private JFormattedTextField mzTextField;
	private JScrollPane scrollPane;
	private JLabel lblSelectAdduct;
	private AdductChooserTable adductChooserTable;
	
	public DockableMzAdductDbSearchPanel(ActionListener listener) {

		super("DockableMzAdductDbSearchPanel", componentIcon, "Search database by MZ/Adduct", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(panel, BorderLayout.CENTER);
		
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 151, 37, 0, 82, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblMass = new JLabel("M/Z");
		GridBagConstraints gbc_lblMass = new GridBagConstraints();
		gbc_lblMass.insets = new Insets(0, 0, 5, 5);
		gbc_lblMass.anchor = GridBagConstraints.EAST;
		gbc_lblMass.gridx = 0;
		gbc_lblMass.gridy = 0;
		panel.add(lblMass, gbc_lblMass);

		mzTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		mzTextField.setColumns(12);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 0;
		panel.add(mzTextField, gbc_formattedTextField);
		
		JLabel lblError = new JLabel("Error");
		GridBagConstraints gbc_lblErrorType = new GridBagConstraints();
		gbc_lblErrorType.anchor = GridBagConstraints.EAST;
		gbc_lblErrorType.insets = new Insets(0, 0, 5, 5);
		gbc_lblErrorType.gridx = 2;
		gbc_lblErrorType.gridy = 0;
		panel.add(lblError, gbc_lblErrorType);
		
		massErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massErrorTextField.setColumns(6);
		massErrorTextField.setText(Double.toString(MRC2ToolBoxConfiguration.getMassAccuracy()));
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 3;
		gbc_formattedTextField_1.gridy = 0;
		panel.add(massErrorTextField, gbc_formattedTextField_1);

		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setSelectedItem(MassErrorType.ppm);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 4;
		gbc_comboBox.gridy = 0;
		panel.add(massErrorTypeComboBox, gbc_comboBox);
		
		clearButton = new JButton("Clear form");
		clearButton.setActionCommand(CLEAR_FORM);
		clearButton.addActionListener(this);
		
		lblSelectAdduct = new JLabel("Select adduct");
		GridBagConstraints gbc_lblSelectAdduct = new GridBagConstraints();
		gbc_lblSelectAdduct.anchor = GridBagConstraints.WEST;
		gbc_lblSelectAdduct.gridwidth = 2;
		gbc_lblSelectAdduct.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectAdduct.gridx = 0;
		gbc_lblSelectAdduct.gridy = 1;
		panel.add(lblSelectAdduct, gbc_lblSelectAdduct);
		
		adductChooserTable = new AdductChooserTable();
		scrollPane = new JScrollPane(adductChooserTable);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		panel.add(scrollPane, gbc_scrollPane);
		clearButton.setPreferredSize(new Dimension(83, 25));
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.gridwidth = 2;
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.gridx = 2;
		gbc_btnNewButton_1.gridy = 3;
		panel.add(clearButton, gbc_btnNewButton_1);

		searchButton = new JButton("Search");
		searchButton.setActionCommand(MainActionCommands.SEARCH_DATABASE_BY_MZ_ADDUCT_COMMAND.getName());
		searchButton.addActionListener(listener);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 3;
		panel.add(searchButton, gbc_btnNewButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();

		if (command.equals(CLEAR_FORM))
			clearForm();
	}
	
	public void setActiveFeature(MsFeature activeFeature) {
		
		clearForm();
		MassSpectrum spectrum = activeFeature.getSpectrum();
		if(spectrum == null)	
			return;
		
		adductChooserTable.loadAdductsForFeature(activeFeature);
		TandemMassSpectrum msms = spectrum.getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
		if(msms != null)
			setMz(msms.getParent().getMz());	
		else
			setMz(spectrum.getPrimaryAdductBasePeakMz());
	}
	
	public void clearForm() {

		mzTextField.setText("");
		adductChooserTable.clearTable();
	}
	
	public void setMz(double mz) {
		mzTextField.setText(Double.toString(mz));
	}
	
	public void setMassError(double error, MassErrorType type) {
		massErrorTextField.setText(Double.toString(error));
		massErrorTypeComboBox.setSelectedItem(type);
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();
	}
	
	public double getMassErrorValue() {
		return Double.parseDouble(massErrorTextField.getText());
	}
	
	public Range getMassRange() {
		
		if(adductChooserTable.getSelectedAdduct() == null) {
			MessageDialog.showErrorMsg("No adduct selected.", this.getContentPane());
			return null;
		}
		String massString = mzTextField.getText().trim();
		Range massRange = null;
		double accuracy = 0.0d;
		double mass = 0.0d;
		double adjustedMass = 0.0d;

		if(!massString.isEmpty()) {

			String accuracyString = massErrorTextField.getText().trim();

			if(!accuracyString.isEmpty())
				accuracy = Double.valueOf(accuracyString);

			mass = Double.valueOf(massString);
			adjustedMass = MsUtils.getNeutralMassForAdduct(mass, adductChooserTable.getSelectedAdduct());
			return MsUtils.createMassRange(adjustedMass, accuracy, (MassErrorType) massErrorTypeComboBox.getSelectedItem());
		}
		else {
			MessageDialog.showErrorMsg("M/Z not specified.", this.getContentPane());
			return null;
		}
	}
	
	public Adduct getSelectedAdduct() {
		return adductChooserTable.getSelectedAdduct();
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


