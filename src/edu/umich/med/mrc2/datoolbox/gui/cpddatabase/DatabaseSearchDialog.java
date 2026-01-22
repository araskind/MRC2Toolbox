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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DatabaseSearchDialog extends JDialog implements ActionListener{

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
	private JCheckBox exactMatchCheckBox;
	private JCheckBox synonymsCheckBox;
	private JButton btnCancel;

	private static final Icon dbLookupIcon = GuiUtils.getIcon("dbLookup", 32);

	public DatabaseSearchDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Search compound database");
		setIconImage(((ImageIcon) dbLookupIcon).getImage());

		setModalityType(ModalityType.MODELESS);
		setSize(new Dimension(450, 220));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 190, 37, 0, 82, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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

		exactMatchCheckBox = new JCheckBox("Exact match");
		GridBagConstraints gbc_exactMatchCheckBox = new GridBagConstraints();
		gbc_exactMatchCheckBox.anchor = GridBagConstraints.WEST;
		gbc_exactMatchCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_exactMatchCheckBox.gridx = 1;
		gbc_exactMatchCheckBox.gridy = 1;
		panel.add(exactMatchCheckBox, gbc_exactMatchCheckBox);

		synonymsCheckBox = new JCheckBox("Search synonyms");
		GridBagConstraints gbc_synonymsCheckBox = new GridBagConstraints();
		gbc_synonymsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_synonymsCheckBox.gridwidth = 3;
		gbc_synonymsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_synonymsCheckBox.gridx = 2;
		gbc_synonymsCheckBox.gridy = 1;
		panel.add(synonymsCheckBox, gbc_synonymsCheckBox);

		JLabel lblFormula = new JLabel("Formula");
		GridBagConstraints gbc_lblFormula = new GridBagConstraints();
		gbc_lblFormula.anchor = GridBagConstraints.EAST;
		gbc_lblFormula.insets = new Insets(0, 0, 5, 5);
		gbc_lblFormula.gridx = 0;
		gbc_lblFormula.gridy = 2;
		panel.add(lblFormula, gbc_lblFormula);

		formulaTextField = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 2;
		panel.add(formulaTextField, gbc_textField_1);
		formulaTextField.setColumns(10);

		JLabel lblId = new JLabel("ID");
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 2;
		gbc_lblId.gridy = 2;
		panel.add(lblId, gbc_lblId);

		idTextField = new JTextField();
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.gridwidth = 2;
		gbc_textField_2.insets = new Insets(0, 0, 5, 0);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 3;
		gbc_textField_2.gridy = 2;
		panel.add(idTextField, gbc_textField_2);
		idTextField.setColumns(10);

		JLabel lblMass = new JLabel("Mass");
		GridBagConstraints gbc_lblMass = new GridBagConstraints();
		gbc_lblMass.insets = new Insets(0, 0, 5, 5);
		gbc_lblMass.anchor = GridBagConstraints.EAST;
		gbc_lblMass.gridx = 0;
		gbc_lblMass.gridy = 3;
		panel.add(lblMass, gbc_lblMass);

		massTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		massTextField.setColumns(12);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 3;
		panel.add(massTextField, gbc_formattedTextField);

		JLabel lblError = new JLabel("Error");
		GridBagConstraints gbc_lblErrorType = new GridBagConstraints();
		gbc_lblErrorType.anchor = GridBagConstraints.EAST;
		gbc_lblErrorType.insets = new Insets(0, 0, 5, 5);
		gbc_lblErrorType.gridx = 2;
		gbc_lblErrorType.gridy = 3;
		panel.add(lblError, gbc_lblErrorType);

		massErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massErrorTextField.setColumns(6);
		massErrorTextField.setText(Double.toString(MRC2ToolBoxConfiguration.getMassAccuracy()));
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 3;
		gbc_formattedTextField_1.gridy = 3;
		panel.add(massErrorTextField, gbc_formattedTextField_1);

		massErrorTypeComboBox = new JComboBox<MassErrorType>(new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setSelectedItem(MassErrorType.ppm);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 4;
		gbc_comboBox.gridy = 3;
		panel.add(massErrorTypeComboBox, gbc_comboBox);

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

		searchButton = new JButton("Search");
		searchButton.setActionCommand(MainActionCommands.SEARCH_DATABASE_COMMAND.getName());
		searchButton.addActionListener(listener);

		clearButton = new JButton("Clear form");
		clearButton.setActionCommand(CLEAR_FORM);
		clearButton.addActionListener(this);

		btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 5;
		panel.add(btnCancel, gbc_btnCancel);
		clearButton.setPreferredSize(new Dimension(83, 25));
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.gridx = 3;
		gbc_btnNewButton_1.gridy = 5;
		panel.add(clearButton, gbc_btnNewButton_1);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 5;
		panel.add(searchButton, gbc_btnNewButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(searchButton);
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
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

		return exactMatchCheckBox.isSelected();
	}

	public boolean searchSynonyms() {

		return synonymsCheckBox.isSelected();
	}

	public void setLookupMass(double mass) {

		massTextField.setText(Double.toString(mass));
	}

	public Range getMassRange() {

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
}



























