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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

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
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.border.TitledBorder;

import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.AdductNotationType;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.structure.MolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class SimpleModificationDataEditorDialog extends JDialog 
		implements ActionListener, ItemListener {

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
	private JTextField cefNotationTextField;
	private JButton checkSmilesButton;
	private JTextField smilesTextField;
	private MolStructurePanel molStructurePanel;
	private SimpleAdduct modification;
	private JTextField binnerNotationTextField;
	private JTextField siriusNotationTextField;

	@SuppressWarnings("unchecked")
	public SimpleModificationDataEditorDialog(ActionListener listener) {

		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Modification data editor");
		setIconImage(((ImageIcon) editIcon).getImage());
		setPreferredSize(new Dimension(640, 480));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 73, 174, 83, 113, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel categoryLabel = new JLabel("Category");
		GridBagConstraints gbc_categoryLabel = new GridBagConstraints();
		gbc_categoryLabel.anchor = GridBagConstraints.EAST;
		gbc_categoryLabel.insets = new Insets(0, 0, 5, 5);
		gbc_categoryLabel.gridx = 3;
		gbc_categoryLabel.gridy = 0;
		panel.add(categoryLabel, gbc_categoryLabel);

		modTypeComboBox = new JComboBox<ModificationType>();
		ComboBoxModel<ModificationType> modTypeModel = 
				new DefaultComboBoxModel<ModificationType>(new ModificationType[] {
						ModificationType.ADDUCT,
						ModificationType.LOSS,
						ModificationType.REPEAT
				});
		modTypeComboBox.setModel(modTypeModel);
		modTypeComboBox.setSelectedIndex(-1);
		modTypeComboBox.addItemListener(this);

		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 4;
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
		gbc_nameTextField.gridwidth = 2;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		panel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
				JLabel descriptionLabel = new JLabel("Description");
				GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
				gbc_descriptionLabel.anchor = GridBagConstraints.EAST;
				gbc_descriptionLabel.insets = new Insets(0, 0, 5, 5);
				gbc_descriptionLabel.gridx = 0;
				gbc_descriptionLabel.gridy = 1;
				panel.add(descriptionLabel, gbc_descriptionLabel);
		
				descriptionTextField = new JTextField();
				GridBagConstraints gbc_descriptionTextField = new GridBagConstraints();
				gbc_descriptionTextField.gridwidth = 4;
				gbc_descriptionTextField.insets = new Insets(0, 0, 5, 0);
				gbc_descriptionTextField.fill = GridBagConstraints.HORIZONTAL;
				gbc_descriptionTextField.gridx = 1;
				gbc_descriptionTextField.gridy = 1;
				panel.add(descriptionTextField, gbc_descriptionTextField);
				descriptionTextField.setColumns(10);

		JLabel lblCefNotation = new JLabel("CEF notation");
		GridBagConstraints gbc_lblCefNotation = new GridBagConstraints();
		gbc_lblCefNotation.anchor = GridBagConstraints.EAST;
		gbc_lblCefNotation.insets = new Insets(0, 0, 5, 5);
		gbc_lblCefNotation.gridx = 0;
		gbc_lblCefNotation.gridy = 2;
		panel.add(lblCefNotation, gbc_lblCefNotation);

		cefNotationTextField = new JTextField();
		GridBagConstraints gbc_cefNotationTextField = new GridBagConstraints();
		gbc_cefNotationTextField.insets = new Insets(0, 0, 5, 5);
		gbc_cefNotationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_cefNotationTextField.gridx = 1;
		gbc_cefNotationTextField.gridy = 2;
		panel.add(cefNotationTextField, gbc_cefNotationTextField);
		cefNotationTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Binner notation");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 2;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		binnerNotationTextField = new JTextField();
		GridBagConstraints gbc_binnerNotationTextField = new GridBagConstraints();
		gbc_binnerNotationTextField.gridwidth = 2;
		gbc_binnerNotationTextField.insets = new Insets(0, 0, 5, 0);
		gbc_binnerNotationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_binnerNotationTextField.gridx = 3;
		gbc_binnerNotationTextField.gridy = 2;
		panel.add(binnerNotationTextField, gbc_binnerNotationTextField);
		binnerNotationTextField.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("SIRIUS notation");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 3;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		siriusNotationTextField = new JTextField();
		GridBagConstraints gbc_siriusNotationTextField = new GridBagConstraints();
		gbc_siriusNotationTextField.gridwidth = 2;
		gbc_siriusNotationTextField.insets = new Insets(0, 0, 5, 5);
		gbc_siriusNotationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_siriusNotationTextField.gridx = 3;
		gbc_siriusNotationTextField.gridy = 3;
		panel.add(siriusNotationTextField, gbc_siriusNotationTextField);
		siriusNotationTextField.setColumns(10);

		JLabel chargeLabel = new JLabel("Charge");
		GridBagConstraints gbc_chargeLabel = new GridBagConstraints();
		gbc_chargeLabel.anchor = GridBagConstraints.EAST;
		gbc_chargeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_chargeLabel.gridx = 0;
		gbc_chargeLabel.gridy = 4;
		panel.add(chargeLabel, gbc_chargeLabel);

		chargeSpinner = new JSpinner();
		chargeSpinner.setPreferredSize(new Dimension(50, 25));
		chargeSpinner.setModel(new SpinnerNumberModel(0, -3, 3, 1));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 4;
		panel.add(chargeSpinner, gbc_spinner);

		JLabel lblOligomer = new JLabel("Oligomer");
		GridBagConstraints gbc_lblOligomer = new GridBagConstraints();
		gbc_lblOligomer.anchor = GridBagConstraints.EAST;
		gbc_lblOligomer.insets = new Insets(0, 0, 5, 5);
		gbc_lblOligomer.gridx = 2;
		gbc_lblOligomer.gridy = 4;
		panel.add(lblOligomer, gbc_lblOligomer);

		oligoSpinner = new JSpinner();
		oligoSpinner.setPreferredSize(new Dimension(50, 25));
		oligoSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.anchor = GridBagConstraints.WEST;
		gbc_spinner_1.gridx = 3;
		gbc_spinner_1.gridy = 4;
		panel.add(oligoSpinner, gbc_spinner_1);

		JLabel lblAddedGroup = new JLabel("Added group");
		GridBagConstraints gbc_lblAddedGroup = new GridBagConstraints();
		gbc_lblAddedGroup.anchor = GridBagConstraints.EAST;
		gbc_lblAddedGroup.insets = new Insets(0, 0, 5, 5);
		gbc_lblAddedGroup.gridx = 0;
		gbc_lblAddedGroup.gridy = 5;
		panel.add(lblAddedGroup, gbc_lblAddedGroup);

		addedGroupTextField = new JTextField();
		GridBagConstraints gbc_addedGroupTextField = new GridBagConstraints();
		gbc_addedGroupTextField.insets = new Insets(0, 0, 5, 5);
		gbc_addedGroupTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_addedGroupTextField.gridx = 1;
		gbc_addedGroupTextField.gridy = 5;
		panel.add(addedGroupTextField, gbc_addedGroupTextField);
		addedGroupTextField.setColumns(10);

		JLabel lblRemovedGroup = new JLabel("Removed group");
		GridBagConstraints gbc_lblRemovedGroup = new GridBagConstraints();
		gbc_lblRemovedGroup.anchor = GridBagConstraints.EAST;
		gbc_lblRemovedGroup.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemovedGroup.gridx = 2;
		gbc_lblRemovedGroup.gridy = 5;
		panel.add(lblRemovedGroup, gbc_lblRemovedGroup);

		removedGroupTextField = new JTextField();
		GridBagConstraints gbc_removedGroupTextField = new GridBagConstraints();
		gbc_removedGroupTextField.insets = new Insets(0, 0, 5, 5);
		gbc_removedGroupTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_removedGroupTextField.gridx = 3;
		gbc_removedGroupTextField.gridy = 5;
		panel.add(removedGroupTextField, gbc_removedGroupTextField);
		removedGroupTextField.setColumns(10);

		JLabel lblNewLabel = new JLabel("SMILES");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 6;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		smilesTextField = new JTextField();
		GridBagConstraints gbc_smilesTextField = new GridBagConstraints();
		gbc_smilesTextField.gridwidth = 3;
		gbc_smilesTextField.insets = new Insets(0, 0, 5, 5);
		gbc_smilesTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_smilesTextField.gridx = 1;
		gbc_smilesTextField.gridy = 6;
		panel.add(smilesTextField, gbc_smilesTextField);
		smilesTextField.setColumns(10);
		
		checkSmilesButton = new JButton("Check structure");
		checkSmilesButton.setActionCommand(
				MainActionCommands.VERIFY_MODIFICATION_SMILES_COMMAND.getName());
		checkSmilesButton.addActionListener(this);
		GridBagConstraints gbc_checkSmilesButton = new GridBagConstraints();
		gbc_checkSmilesButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_checkSmilesButton.insets = new Insets(0, 0, 5, 0);
		gbc_checkSmilesButton.gridx = 4;
		gbc_checkSmilesButton.gridy = 6;
		panel.add(checkSmilesButton, gbc_checkSmilesButton);
		
		molStructurePanel = new MolStructurePanel();
		molStructurePanel.setBorder(
				new TitledBorder(null, "Structure", 
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 5;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 7;
		panel.add(molStructurePanel, gbc_panel_1);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(al);	
		buttonPanel.add(cancelButton);
		
		saveButton = new JButton("Save");
		saveButton.setActionCommand(
				MainActionCommands.SAVE_SIMPLE_MODIFICATION_DATA_COMMAND.getName());
		saveButton.addActionListener(listener);
		buttonPanel.add(saveButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		
		modification = null;
		
		pack();
	}

	public synchronized void clearPanel() {

		nameTextField.setText("");
		cefNotationTextField.setText("");
		binnerNotationTextField.setText("");
		siriusNotationTextField.setText("");
		descriptionTextField.setText("");
		modTypeComboBox.setSelectedIndex(-1);
		addedGroupTextField.setText("");
		removedGroupTextField.setText("");
		chargeSpinner.setValue(0);
		oligoSpinner.setValue(1);
		smilesTextField.setText("");
		molStructurePanel.clearPanel();
	}

	public String getAddedGroup() {
		
		if(getModificationType().equals(ModificationType.LOSS))
			return new String("");
		
		return addedGroupTextField.getText().trim();
	}

	public String getCefNotation() {
		return cefNotationTextField.getText().trim();
	}
	
	public String getBinnerNotation() {
		return binnerNotationTextField.getText().trim();
	}
	
	public String getSiriusNotation() {
		return siriusNotationTextField.getText().trim();
	}

	public int getCharge() {
		
		if(getModificationType().equals(ModificationType.LOSS)
				|| getModificationType().equals(ModificationType.REPEAT))
			return 0;
		
		return (int) chargeSpinner.getValue();
	}

	public String getModDescription() {
		return descriptionTextField.getText().trim();
	}

	public String getModificationName() {
		return nameTextField.getText().trim();
	}

	public ModificationType getModificationType() {
		return (ModificationType) modTypeComboBox.getSelectedItem();
	}

	public int getOligomericState() {
		
		if(getModificationType().equals(ModificationType.LOSS)
				|| getModificationType().equals(ModificationType.REPEAT))
			return 1;
		
		return (int) oligoSpinner.getValue();
	}

	public String getRemovedGroup() {
		
		if(getModificationType().equals(ModificationType.REPEAT))
			return new String("");
		
		return removedGroupTextField.getText().trim();
	}
	
	public String getSmiles() {
		
		String smilesString = smilesTextField.getText().trim();
		if(smilesString.isEmpty())
			return null;
		else
			return smilesString;
	}

	public void loadModificationData(SimpleAdduct modification) {

		this.modification = modification;
		nameTextField.setText(modification.getName());
		cefNotationTextField.setText(modification.getNotationForType(AdductNotationType.CEF));
		binnerNotationTextField.setText(modification.getNotationForType(AdductNotationType.BINNER));
		siriusNotationTextField.setText(modification.getNotationForType(AdductNotationType.SIRIUS));
		descriptionTextField.setText(modification.getDescription());
		addedGroupTextField.setText(modification.getAddedGroup());
		removedGroupTextField.setText(modification.getRemovedGroup());
		chargeSpinner.setValue(modification.getCharge());
		oligoSpinner.setValue(modification.getOligomericState());
		modTypeComboBox.setSelectedItem(modification.getModificationType());
		modTypeComboBox.setEnabled(false);
		
		String smiles = modification.getSmiles();
		if(smiles != null && !smiles.isEmpty()) {
			smilesTextField.setText(smiles);
			molStructurePanel.showStructure(smiles);
		}		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof ModificationType)
			updateGuiForModificationType((ModificationType)e.getItem());		
	}
	
	private void updateGuiForModificationType(ModificationType selectedType) {
		
		if(selectedType.equals(ModificationType.ADDUCT)) {
			
			addedGroupTextField.setEditable(true);
			removedGroupTextField.setEditable(true);
			chargeSpinner.setEnabled(true);
			oligoSpinner.setEnabled(true);
		}
		if(selectedType.equals(ModificationType.LOSS)) {
			
			addedGroupTextField.setEditable(false);
			removedGroupTextField.setEditable(true);
			chargeSpinner.setEnabled(false);
			oligoSpinner.setEnabled(false);
		}
		if(selectedType.equals(ModificationType.REPEAT)) {
			
			addedGroupTextField.setEditable(true);
			removedGroupTextField.setEditable(false);
			chargeSpinner.setEnabled(false);
			oligoSpinner.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.VERIFY_MODIFICATION_SMILES_COMMAND.getName()))
			veryfySmiles();		
	}
	
	private void veryfySmiles() {
		
		String smiles = getSmiles();
		if(smiles == null || smiles.isEmpty()) 
			return;
		
		if(!molStructurePanel.smilesStringIsValid(smiles)) {
			
			MessageDialog.showErrorMsg("SMILES not valid.", this);
			return;
		}
		molStructurePanel.showStructure(smiles);		
	}
	
	public Collection<String>validateModification(){
		
		Collection<String>errors = new ArrayList<String>();		
		ModificationType modType = getModificationType();
		if(modType == null) {
			errors.add("Modification type not specified!");
			return errors;
		}		
		if (getModificationName().isEmpty())
			errors.add("Name can not be empty.");
		
		if(modType.equals(ModificationType.ADDUCT)) {
			
			if(getCharge() == 0)
				errors.add("Adduct charge can not be 0.");
			
			if(getAddedGroup().isEmpty() && getRemovedGroup().isEmpty())
				errors.add("At least one group (added or removed) has to be specified.");
			
			if (!getAddedGroup().isEmpty() && !isFormulaValid(getAddedGroup()))
				errors.add("Added group formula invalid!");
			
			if (!getRemovedGroup().isEmpty() && !isFormulaValid(getRemovedGroup()))
				errors.add("Removed group formula invalid!");
		}
		if(modType.equals(ModificationType.LOSS)) {
			
			if(getRemovedGroup().isEmpty())
				errors.add("Removed group has to be specified.");
			
			if (!getRemovedGroup().isEmpty() && !isFormulaValid(getRemovedGroup()))
				errors.add("Removed group formula invalid!");
		}
		if(modType.equals(ModificationType.REPEAT)) {
			
			if(getAddedGroup().isEmpty())
				errors.add("Added group has to be specified.");
			
			if (!getAddedGroup().isEmpty() && !isFormulaValid(getAddedGroup()))
				errors.add("Added group formula invalid.");
		}
		String smiles = getSmiles();
		if(smiles != null && !smiles.isEmpty()
				&& !molStructurePanel.smilesStringIsValid(smiles)) {
			errors.add("SMILES not valid");
		}		
		// Test if same modification already exists
		SimpleAdduct newAdduct = constructModification();		
		SimpleAdduct same = null;
		
		if(modification == null) {
			same = AdductManager.getSimpleAdducts().stream().
				filter(a -> a.isEquivalent(newAdduct)).
				findFirst().orElse(null);
		}
		else {
			same = AdductManager.getSimpleAdducts().stream().
				filter(a -> !a.equals(modification)).
				filter(a -> a.isEquivalent(newAdduct)).
				findFirst().orElse(null);
		}
		if(same != null)
			errors.add("Equivalent modification \"" + same.getName() + "\" already exists.");
		
		return errors;
	}
	
	private boolean isFormulaValid(String formula) {
		
		try {
			IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(formula,
					SilentChemObjectBuilder.getInstance());
			MolecularFormulaManipulator.getMass(
					mf, MolecularFormulaManipulator.MonoIsotopic);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public SimpleAdduct getActiveModification() {
		return modification;
	}
	
	public SimpleAdduct constructModification() {
		
		String description = getModDescription();
		if(description == null || description.isEmpty())
			description = getModificationName();
		
		SimpleAdduct newAdduct = new SimpleAdduct(
				null,
				getModificationName(), 
				description, 
				getAddedGroup(), 
				getRemovedGroup(),
				getSmiles(), 
				getCharge(), 
				getOligomericState(), 
				0.0d,
				getModificationType(), 
				true);
		newAdduct.setMassCorrection(
				MsUtils.calculateMassCorrectionFromAddedRemovedGroups(newAdduct));
		
		String cefNotation = getCefNotation();
		if(!cefNotation.isEmpty())
			newAdduct.setNotationForType(AdductNotationType.CEF, cefNotation);
				
		String binnerNotation = getBinnerNotation();
		if(!binnerNotation.isEmpty())
			newAdduct.setNotationForType(AdductNotationType.BINNER, binnerNotation);
		
		String siriusNotation = getSiriusNotation();
		if(!siriusNotation.isEmpty())
			newAdduct.setNotationForType(AdductNotationType.SIRIUS, siriusNotation);
		
		return newAdduct;
	}
	
	public SimpleAdduct getEditedModification() {
		
		SimpleAdduct newAdduct = constructModification();
		
		if(modification != null)
			newAdduct.setId(modification.getId());
		
		return newAdduct;
	}
}
















