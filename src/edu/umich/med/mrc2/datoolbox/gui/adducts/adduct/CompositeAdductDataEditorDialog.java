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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.structure.MolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class CompositeAdductDataEditorDialog extends JDialog 
		implements ActionListener, ListSelectionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -4572738647156347802L;

	public static final Icon editIcon = GuiUtils.getIcon("editModification", 32);
	public static final Icon newIcon = GuiUtils.getIcon("newModification", 32);

	private JTextField nameTextField;
	private JTextField descriptionTextField;
	private JButton cancelButton, saveButton;
	private JTextField chargeCarrierTextField;
	private CompositeAdduct compositeAdduct;
	private SimpleAdduct chargeCarrier;

	private JButton 
		selectChargeCarrierButton,
		addNeutralLossButton,
		addNeutralAdductButton,
		removeSelectedButton;
	
	private CompositeAdductComponentsTable compositAdductComponentsTable;
	private ChargeCarrierSelectorDialog chargeCarrierSelectorDialog;
	private SimpleModificationSelectorDialog simpleModificationSelectorDialog;
	private MolStructurePanel structurePanel;
	
	public CompositeAdductDataEditorDialog(ActionListener listener) {

		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Composite adduct data editor");
		setIconImage(((ImageIcon) editIcon).getImage());
		setPreferredSize(new Dimension(800, 450));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 292, 104, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		nameTextField = new JTextField();
		nameTextField.setEditable(false);
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 3;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		panel.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Description");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		descriptionTextField = new JTextField();
		GridBagConstraints gbc_descriptionTextField = new GridBagConstraints();
		gbc_descriptionTextField.gridwidth = 3;
		gbc_descriptionTextField.insets = new Insets(0, 0, 5, 0);
		gbc_descriptionTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_descriptionTextField.gridx = 1;
		gbc_descriptionTextField.gridy = 1;
		panel.add(descriptionTextField, gbc_descriptionTextField);
		descriptionTextField.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Charge carrier");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		chargeCarrierTextField = new JTextField();
		chargeCarrierTextField.setEditable(false);
		GridBagConstraints gbc_chargeCarrierTextField = new GridBagConstraints();
		gbc_chargeCarrierTextField.insets = new Insets(0, 0, 5, 5);
		gbc_chargeCarrierTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_chargeCarrierTextField.gridx = 1;
		gbc_chargeCarrierTextField.gridy = 2;
		panel.add(chargeCarrierTextField, gbc_chargeCarrierTextField);
		chargeCarrierTextField.setColumns(10);
		
		selectChargeCarrierButton = new JButton(
				MainActionCommands.SELECT_CHARGE_CARRIER_COMMAND.getName());
		selectChargeCarrierButton.setActionCommand(
				MainActionCommands.SELECT_CHARGE_CARRIER_DIALOG_COMMAND.getName());
		selectChargeCarrierButton.addActionListener(this);
		GridBagConstraints gbc_selectChargeCarrierButton = new GridBagConstraints();
		gbc_selectChargeCarrierButton.insets = new Insets(0, 0, 5, 5);
		gbc_selectChargeCarrierButton.gridx = 2;
		gbc_selectChargeCarrierButton.gridy = 2;
		panel.add(selectChargeCarrierButton, gbc_selectChargeCarrierButton);
		
		compositAdductComponentsTable = new CompositeAdductComponentsTable();
		JScrollPane scrollPane = new JScrollPane(compositAdductComponentsTable);
		compositAdductComponentsTable.getSelectionModel().addListSelectionListener(this);
//		JScrollPane scrollPane = new JScrollPane();

		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 3;
		panel.add(scrollPane, gbc_scrollPane);		
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 3;
		gbc_panel_1.gridy = 3;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		addNeutralLossButton = new JButton(
				MainActionCommands.ADD_NEUTRAL_LOSS_COMMAND.getName());
		addNeutralLossButton.setActionCommand(
				MainActionCommands.ADD_NEUTRAL_LOSS_DIALOG_COMMAND.getName());
		addNeutralLossButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 0;
		panel_1.add(addNeutralLossButton, gbc_btnNewButton);
		
		addNeutralAdductButton = new JButton(
				MainActionCommands.ADD_NEUTRAL_ADDUCT_COMMAND.getName());
		addNeutralAdductButton.setActionCommand(
				MainActionCommands.ADD_NEUTRAL_ADDUCT_DIALOG_COMMAND.getName());
		addNeutralAdductButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.gridx = 0;
		gbc_btnNewButton_1.gridy = 1;
		panel_1.add(addNeutralAdductButton, gbc_btnNewButton_1);
		
		removeSelectedButton = new JButton(
				MainActionCommands.REMOVE_SELECTED_ADDUCT_COMMAND.getName());
		removeSelectedButton.setActionCommand(
				MainActionCommands.REMOVE_SELECTED_ADDUCT_COMMAND.getName());		
		removeSelectedButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
		gbc_btnNewButton_2.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_2.anchor = GridBagConstraints.SOUTH;
		gbc_btnNewButton_2.gridx = 0;
		gbc_btnNewButton_2.gridy = 2;
		panel_1.add(removeSelectedButton, gbc_btnNewButton_2);
		
		structurePanel = new MolStructurePanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 3;
		panel_1.add(structurePanel, gbc_panel_2);
		
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
				MainActionCommands.SAVE_COMPOSITE_MODIFICATION_DATA_COMMAND.getName());
		saveButton.addActionListener(listener);
		buttonPanel.add(saveButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		
		pack();
	}

	public synchronized void clearPanel() {

		nameTextField.setText("");
		descriptionTextField.setText("");
		chargeCarrierTextField.setText("");
		compositAdductComponentsTable.clearTable();
		chargeCarrier = null;
		compositeAdduct = null;
	}

	public String getModDescription() {
		return descriptionTextField.getText().trim();
	}

	public String getModName() {
		return nameTextField.getText().trim();
	}

	public void loadCompositeAdduct(CompositeAdduct composite) {
		
		this.compositeAdduct = composite;
		this.chargeCarrier = compositeAdduct.getChargeCarrier();
		chargeCarrierTextField.setText(chargeCarrier.getName());
		nameTextField.setText(compositeAdduct.getName());
		descriptionTextField.setText(compositeAdduct.getDescription());
		compositAdductComponentsTable.setTableModelFromCompositAdduct(compositeAdduct);
	}

	public void loadBinnerMassDiffData(BinnerNeutralMassDifference mdiff) {

		this.compositeAdduct = null;
		this.chargeCarrier = null;
		compositAdductComponentsTable.setTableModelFromBinnerNeutralMassDifference(mdiff);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SELECT_CHARGE_CARRIER_DIALOG_COMMAND.getName()))
			showChargeCarrierSelector();
		
		if(command.equals(MainActionCommands.SELECT_CHARGE_CARRIER_COMMAND.getName()))
			selectChargeCarrier();
		
		if(command.equals(MainActionCommands.ADD_NEUTRAL_LOSS_DIALOG_COMMAND.getName()))
			showComponentAdductSelector(ModificationType.LOSS);
		
		if(command.equals(MainActionCommands.ADD_NEUTRAL_ADDUCT_DIALOG_COMMAND.getName()))
			showComponentAdductSelector(ModificationType.REPEAT);
		
		if(command.equals(MainActionCommands.ADD_NEUTRAL_LOSS_COMMAND.getName()) || 
				command.equals(MainActionCommands.ADD_NEUTRAL_ADDUCT_COMMAND.getName())) {
			addComponentModification();
		}
		if(command.equals(MainActionCommands.REMOVE_SELECTED_ADDUCT_COMMAND.getName()))
			removeComponentModification();			
	}
	
	private void showChargeCarrierSelector() {
		
		chargeCarrierSelectorDialog = new ChargeCarrierSelectorDialog(this);
		chargeCarrierSelectorDialog.setLocationRelativeTo(this);
		chargeCarrierSelectorDialog.setVisible(true);
	}
	
	private void selectChargeCarrier() {
		
		SimpleAdduct newChargeCarrier = 
				chargeCarrierSelectorDialog.getSelectedChargeCarrier();
		if(newChargeCarrier == null)
			return;
		
		chargeCarrier = newChargeCarrier;
		chargeCarrierTextField.setText(chargeCarrier.getName());
		updateModificationName();
		chargeCarrierSelectorDialog.dispose();
	}
	
	private void showComponentAdductSelector(ModificationType type) {
		
		if(chargeCarrier == null) {
			MessageDialog.showErrorMsg("Please select the charge carrier first", this);
			return;
		}		
		simpleModificationSelectorDialog = 
				new SimpleModificationSelectorDialog(this, type);
		simpleModificationSelectorDialog.setLocationRelativeTo(this);
		simpleModificationSelectorDialog.setVisible(true);
	}
	
	private void addComponentModification() {
		
		SimpleAdduct mod = 
				simpleModificationSelectorDialog.getSelectedNeutralModification();
		if(mod == null)
			return;
		
		for(int i=0; i< simpleModificationSelectorDialog.getNumberOfUnits(); i++)
			compositAdductComponentsTable.addModification(mod);
				
		updateModificationName();
		simpleModificationSelectorDialog.dispose();
	}
	
	private void removeComponentModification() {
		
		SimpleAdduct mod = 
				compositAdductComponentsTable.getSelectedModification();
		if(mod == null)
			return;
		
		compositAdductComponentsTable.removeModification(mod);
		updateModificationName();
	}
	
	private void updateModificationName() {
		
		if(chargeCarrier == null)
			return;
		
		CompositeAdduct tmp = new CompositeAdduct(chargeCarrier);
		for(SimpleAdduct mod : compositAdductComponentsTable.getAllAdducts())
			tmp.addModification(mod);
		
		nameTextField.setText(tmp.getName());
	}
	
	public Collection<String>validateModification(){
		
		Collection<String>errors = new ArrayList<String>();
		if(chargeCarrier == null)
			errors.add("Charge carrier not specified.");
		
		if(compositAdductComponentsTable.getAllAdducts().isEmpty())
			errors.add("No neutral losses or adducts specified.");
		
		if(!errors.isEmpty())
			return errors;
		
		CompositeAdduct tmp = getEditedCompositeAdduct();		
		CompositeAdduct sameAdduct = AdductManager.getCompositeAdducts().
				stream().filter(c -> c.equals(tmp)).findFirst().orElse(null);
		
		boolean sameDescription = false;
		if(sameAdduct != null) {
			
			if(sameAdduct.getDescription() != null && tmp.getDescription() != null)
				sameDescription = sameAdduct.getDescription().equals(tmp.getDescription());
		}		
		if(sameAdduct != null && sameDescription)
			errors.add("Composite adduct with the same combination\n"
					+ "of charge carrier/losses/additions already exists.");
			
		return errors;
	}
	
	public CompositeAdduct getCompositeAdduct() {
		return compositeAdduct;
	}

	public CompositeAdduct getEditedCompositeAdduct() {
		
		CompositeAdduct tmp = new CompositeAdduct(chargeCarrier);
		for(SimpleAdduct mod : compositAdductComponentsTable.getAllAdducts())
			tmp.addModification(mod);
		
		String description = descriptionTextField.getText().trim();
		if(description.isEmpty())
			description = tmp.getName();
		
		tmp.setDescription(description);	
		return tmp;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		if(!e.getValueIsAdjusting()) {
			
			structurePanel.clearPanel();
			Adduct mod = compositAdductComponentsTable.getSelectedModification();
			if(mod == null || mod.getSmiles() == null)
				return;
			
			
			
			structurePanel.showStructure(mod.getSmiles());
		}
	}
}














