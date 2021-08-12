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

package edu.umich.med.mrc2.datoolbox.gui.adducts.chemmod;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class ModificationDataEditorDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -4572738647156347802L;

	public static final Icon editIcon = GuiUtils.getIcon("editModification", 32);
	public static final Icon newIcon = GuiUtils.getIcon("newModification", 32);

	private JTextField nameTextField;
	private JTextField descriptionTextField;
	private JComboBox modTypeComboBox;
	private JSpinner chargeSpinner, oligoSpinner;
	private JTextField addedGroupTextField;
	private JTextField removedGroupTextField;
	private JButton cancelButton, saveButton;
	private JLabel lblCefNotation;
	private JTextField cefNotationTextField;

	public ModificationDataEditorDialog(ActionListener listener) {

		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Modification data editor");
		setIconImage(((ImageIcon) editIcon).getImage());
		setSize(new Dimension(600, 250));
		setPreferredSize(new Dimension(600, 250));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 81, 186, 92, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel categoryLabel = new JLabel("Category");
		GridBagConstraints gbc_categoryLabel = new GridBagConstraints();
		gbc_categoryLabel.anchor = GridBagConstraints.EAST;
		gbc_categoryLabel.insets = new Insets(0, 0, 5, 5);
		gbc_categoryLabel.gridx = 2;
		gbc_categoryLabel.gridy = 0;
		panel.add(categoryLabel, gbc_categoryLabel);

		modTypeComboBox = new JComboBox<ModificationType>();
		SortedComboBoxModel<ModificationType> modTypeModel = new SortedComboBoxModel<ModificationType>(
				ModificationType.values());
		modTypeComboBox.setModel(modTypeModel);
		modTypeComboBox.setSelectedIndex(-1);

		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 3;
		gbc_comboBox.gridy = 0;
		panel.add(modTypeComboBox, gbc_comboBox);

		JLabel nameLabel = new JLabel("Name");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nameLabel.anchor = GridBagConstraints.EAST;
		gbc_nameLabel.gridx = 0;
		gbc_nameLabel.gridy = 0;
		panel.add(nameLabel, gbc_nameLabel);

		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		panel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);

		lblCefNotation = new JLabel("CEF notation");
		GridBagConstraints gbc_lblCefNotation = new GridBagConstraints();
		gbc_lblCefNotation.anchor = GridBagConstraints.EAST;
		gbc_lblCefNotation.insets = new Insets(0, 0, 5, 5);
		gbc_lblCefNotation.gridx = 0;
		gbc_lblCefNotation.gridy = 1;
		panel.add(lblCefNotation, gbc_lblCefNotation);

		cefNotationTextField = new JTextField();
		GridBagConstraints gbc_cefNotationTextField = new GridBagConstraints();
		gbc_cefNotationTextField.insets = new Insets(0, 0, 5, 5);
		gbc_cefNotationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_cefNotationTextField.gridx = 1;
		gbc_cefNotationTextField.gridy = 1;
		panel.add(cefNotationTextField, gbc_cefNotationTextField);
		cefNotationTextField.setColumns(10);

		JLabel descriptionLabel = new JLabel("Description");
		GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
		gbc_descriptionLabel.anchor = GridBagConstraints.EAST;
		gbc_descriptionLabel.insets = new Insets(0, 0, 5, 5);
		gbc_descriptionLabel.gridx = 0;
		gbc_descriptionLabel.gridy = 2;
		panel.add(descriptionLabel, gbc_descriptionLabel);

		descriptionTextField = new JTextField();
		GridBagConstraints gbc_descriptionTextField = new GridBagConstraints();
		gbc_descriptionTextField.gridwidth = 3;
		gbc_descriptionTextField.insets = new Insets(0, 0, 5, 0);
		gbc_descriptionTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_descriptionTextField.gridx = 1;
		gbc_descriptionTextField.gridy = 2;
		panel.add(descriptionTextField, gbc_descriptionTextField);
		descriptionTextField.setColumns(10);

		JLabel chargeLabel = new JLabel("Charge");
		GridBagConstraints gbc_chargeLabel = new GridBagConstraints();
		gbc_chargeLabel.anchor = GridBagConstraints.EAST;
		gbc_chargeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_chargeLabel.gridx = 0;
		gbc_chargeLabel.gridy = 3;
		panel.add(chargeLabel, gbc_chargeLabel);

		chargeSpinner = new JSpinner();
		chargeSpinner.setModel(new SpinnerNumberModel(0, -3, 3, 1));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 3;
		panel.add(chargeSpinner, gbc_spinner);

		JLabel lblOligomer = new JLabel("Oligomer");
		GridBagConstraints gbc_lblOligomer = new GridBagConstraints();
		gbc_lblOligomer.anchor = GridBagConstraints.EAST;
		gbc_lblOligomer.insets = new Insets(0, 0, 5, 5);
		gbc_lblOligomer.gridx = 2;
		gbc_lblOligomer.gridy = 3;
		panel.add(lblOligomer, gbc_lblOligomer);

		oligoSpinner = new JSpinner();
		oligoSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_1.anchor = GridBagConstraints.WEST;
		gbc_spinner_1.gridx = 3;
		gbc_spinner_1.gridy = 3;
		panel.add(oligoSpinner, gbc_spinner_1);

		JLabel lblAddedGroup = new JLabel("Added group");
		GridBagConstraints gbc_lblAddedGroup = new GridBagConstraints();
		gbc_lblAddedGroup.anchor = GridBagConstraints.EAST;
		gbc_lblAddedGroup.insets = new Insets(0, 0, 5, 5);
		gbc_lblAddedGroup.gridx = 0;
		gbc_lblAddedGroup.gridy = 4;
		panel.add(lblAddedGroup, gbc_lblAddedGroup);

		addedGroupTextField = new JTextField();
		GridBagConstraints gbc_addedGroupTextField = new GridBagConstraints();
		gbc_addedGroupTextField.insets = new Insets(0, 0, 5, 5);
		gbc_addedGroupTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_addedGroupTextField.gridx = 1;
		gbc_addedGroupTextField.gridy = 4;
		panel.add(addedGroupTextField, gbc_addedGroupTextField);
		addedGroupTextField.setColumns(10);

		JLabel lblRemovedGroup = new JLabel("Removed group");
		GridBagConstraints gbc_lblRemovedGroup = new GridBagConstraints();
		gbc_lblRemovedGroup.anchor = GridBagConstraints.EAST;
		gbc_lblRemovedGroup.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemovedGroup.gridx = 2;
		gbc_lblRemovedGroup.gridy = 4;
		panel.add(lblRemovedGroup, gbc_lblRemovedGroup);

		removedGroupTextField = new JTextField();
		GridBagConstraints gbc_removedGroupTextField = new GridBagConstraints();
		gbc_removedGroupTextField.insets = new Insets(0, 0, 5, 0);
		gbc_removedGroupTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_removedGroupTextField.gridx = 3;
		gbc_removedGroupTextField.gridy = 4;
		panel.add(removedGroupTextField, gbc_removedGroupTextField);
		removedGroupTextField.setColumns(10);

		saveButton = new JButton("Save");
		saveButton.setActionCommand(MainActionCommands.SAVE_SIMPLE_MODIFICATION_DATA_COMMAND.getName());
		saveButton.addActionListener(listener);

		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 6;
		panel.add(cancelButton, gbc_btnCancel);
		GridBagConstraints gbc_saveButton = new GridBagConstraints();
		gbc_saveButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_saveButton.gridx = 3;
		gbc_saveButton.gridy = 6;
		panel.add(saveButton, gbc_saveButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);

		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(false);
	}

	public void clear() {

		nameTextField.setText("");
		cefNotationTextField.setText("");
		descriptionTextField.setText("");
		modTypeComboBox.setSelectedIndex(-1);
		addedGroupTextField.setText("");
		removedGroupTextField.setText("");
		chargeSpinner.setValue(0);
		oligoSpinner.setValue(1);
	}

	public String getAddedGroup() {

		return addedGroupTextField.getText().trim();
	}

	public String getCefNotation() {

		return cefNotationTextField.getText().trim();
	}

	public int getCharge() {

		return (int) chargeSpinner.getValue();
	}

	public String getModDescription() {

		return descriptionTextField.getText().trim();
	}

	public String getModName() {

		return nameTextField.getText().trim();
	}

	public ModificationType getModType() {

		return (ModificationType) modTypeComboBox.getSelectedItem();
	}

	public int getOligomericState() {

		return (int) oligoSpinner.getValue();
	}

	public String getRemovedGroup() {

		return removedGroupTextField.getText().trim();
	}

	public void loadModificationData(Adduct modification) {

		nameTextField.setText(modification.getName());
		cefNotationTextField.setText(modification.getCefNotation());
		descriptionTextField.setText(modification.getDescription());
		modTypeComboBox.setSelectedItem(modification.getModificationType());
		addedGroupTextField.setText(modification.getAddedGroup());
		removedGroupTextField.setText(modification.getRemovedGroup());
		chargeSpinner.setValue(modification.getCharge());
		oligoSpinner.setValue(modification.getOligomericState());
	}
}
