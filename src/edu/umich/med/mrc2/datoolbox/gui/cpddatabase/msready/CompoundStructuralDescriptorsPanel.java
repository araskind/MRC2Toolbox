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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class CompoundStructuralDescriptorsPanel extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8891901509906390809L;
	
	private JTextField formulaTextField;
	private JTextField inchiKeyTextField;
	private JTextArea smilesTextArea;
	private JFormattedTextField massTextField;
	private JSpinner chargeSpinner;
	private JButton copySmilesButton;
	private JButton pasteSmilesButton;
	
	public CompoundStructuralDescriptorsPanel(boolean enablePaste) {
		
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{87, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Formula");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		formulaTextField = new JTextField();
		GridBagConstraints gbc_formulaTextField = new GridBagConstraints();
		gbc_formulaTextField.gridwidth = 4;
		gbc_formulaTextField.insets = new Insets(0, 0, 5, 0);
		gbc_formulaTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formulaTextField.gridx = 1;
		gbc_formulaTextField.gridy = 0;
		add(formulaTextField, gbc_formulaTextField);
		formulaTextField.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Mass");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		massTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		GridBagConstraints gbc_massTextField = new GridBagConstraints();
		gbc_massTextField.gridwidth = 2;
		gbc_massTextField.insets = new Insets(0, 0, 5, 5);
		gbc_massTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_massTextField.gridx = 1;
		gbc_massTextField.gridy = 1;
		add(massTextField, gbc_massTextField);
		
		JLabel lblNewLabel_3 = new JLabel("Charge");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 3;
		gbc_lblNewLabel_3.gridy = 1;
		add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		chargeSpinner = new JSpinner();
		chargeSpinner.setModel(new SpinnerNumberModel(0, -10, 10, 1));
		GridBagConstraints gbc_chargeTextField = new GridBagConstraints();
		gbc_chargeTextField.insets = new Insets(0, 0, 5, 0);
		gbc_chargeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_chargeTextField.gridx = 4;
		gbc_chargeTextField.gridy = 1;
		add(chargeSpinner, gbc_chargeTextField);
		
		JLabel lblNewLabel_1 = new JLabel("InChi key");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		inchiKeyTextField = new JTextField();
		GridBagConstraints gbc_inchiKeyTextField = new GridBagConstraints();
		gbc_inchiKeyTextField.gridwidth = 4;
		gbc_inchiKeyTextField.insets = new Insets(0, 0, 5, 0);
		gbc_inchiKeyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_inchiKeyTextField.gridx = 1;
		gbc_inchiKeyTextField.gridy = 2;
		add(inchiKeyTextField, gbc_inchiKeyTextField);
		inchiKeyTextField.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("SMILES");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 3;
		add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		smilesTextArea = new JTextArea();
		smilesTextArea.setLineWrap(true);
		smilesTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.gridwidth = 5;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 4;
		add(smilesTextArea, gbc_textArea);
		
		copySmilesButton = new JButton(MainActionCommands.COPY_SMILES_COMMAND.getName());
		copySmilesButton.setActionCommand(MainActionCommands.COPY_SMILES_COMMAND.getName());
		copySmilesButton.addActionListener(this);
		GridBagConstraints gbc_copySmilesButton = new GridBagConstraints();
		gbc_copySmilesButton.insets = new Insets(0, 0, 0, 5);
		gbc_copySmilesButton.gridx = 0;
		gbc_copySmilesButton.gridy = 5;
		add(copySmilesButton, gbc_copySmilesButton);
		
		if(enablePaste) {
			pasteSmilesButton = new JButton(MainActionCommands.PASTE_SMILES_COMMAND.getName());
			pasteSmilesButton.setActionCommand(MainActionCommands.PASTE_SMILES_COMMAND.getName());
			pasteSmilesButton.addActionListener(this);
			GridBagConstraints gbc_pasteSmilesButton = new GridBagConstraints();
			gbc_pasteSmilesButton.insets = new Insets(0, 0, 0, 5);
			gbc_pasteSmilesButton.fill = GridBagConstraints.HORIZONTAL;
			gbc_pasteSmilesButton.gridx = 1;
			gbc_pasteSmilesButton.gridy = 5;
			add(pasteSmilesButton, gbc_pasteSmilesButton);
		}
	}
	
	public String getFormula() {
		return formulaTextField.getText().trim();
	}
	
	public String getInchiKey() {
		return inchiKeyTextField.getText().trim();
	}
	
	public String getSmiles() {
		return smilesTextArea.getText().trim();
	}
	
	public int getCharge() {
		return (int)chargeSpinner.getValue();
	}
	
	public double getMass() {
		
		String massString = massTextField.getText().trim();
		if(massString.isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(massString);
	}

	public void setFormula(String formula) {
		formulaTextField.setText(formula);
	}
	
	public void setInchiKey(String inchiKey) {
		inchiKeyTextField.setText(inchiKey);
	}
	
	public void setSmiles(String smiles) {
		smilesTextArea.setText(smiles);
	}
	
	public void setCharge(int charge) {
		chargeSpinner.setValue(charge);
	}
	
	public void setMass(double mass) {		
		massTextField.setText(MRC2ToolBoxConfiguration.getMzFormat().format(mass));	
	}
	
	public void loadCompoundIdentity(CompoundIdentity cid) {
		
		formulaTextField.setText(cid.getFormula());
		inchiKeyTextField.setText(cid.getInChiKey());
		smilesTextArea.setText(cid.getSmiles());
		massTextField.setText(Double.toString(cid.getExactMass()));
		chargeSpinner.setValue(cid.getCharge());
	}
	
	public void lockEditing(boolean includeSmiles) {
		
		formulaTextField.setEditable(false);
		inchiKeyTextField.setEditable(false);		
		massTextField.setEditable(false);
		chargeSpinner.setEnabled(false);
		
		if(includeSmiles)
			smilesTextArea.setEditable(false);
	}
	
	public void unlockEditing() {
		
		formulaTextField.setEditable(true);
		inchiKeyTextField.setEditable(true);		
		massTextField.setEditable(true);
		chargeSpinner.setEnabled(true);
		smilesTextArea.setEditable(true);
	}

	public void clearPanel() {

		formulaTextField.setText("");
		inchiKeyTextField.setText("");	
		massTextField.setText("");
		chargeSpinner.setValue(0);
		smilesTextArea.setText("");
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.COPY_SMILES_COMMAND.getName()))
			copySmiles();
			
		if(command.equals(MainActionCommands.PASTE_SMILES_COMMAND.getName()))
			pasteSmiles();			
	}

	private void copySmiles() {
		
		StringSelection stringSelection = new StringSelection(getSmiles());
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}

	private void pasteSmiles() {

		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		Object contents = clpbrd.getContents(null);
		if(contents != null) {
			
			String smiles = null;
			try {
				smiles = (String) clpbrd.getData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(smiles != null && !smiles.isEmpty())
				setSmiles(smiles);
		}
	}
}














