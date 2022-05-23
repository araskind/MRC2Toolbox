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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.id;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.IdentifierSearchOptions;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.IdFollowupStepTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.IDTrackerDataSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class IdAnnotationSearchParametersPanel extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -412061840550532436L;

	private JComboBox idOptionComboBox;
	private JComboBox idFilterComboBox;
	private JTextField nameIdTextField;
	private JTextField formulaTextField;
	private JTextField inChIKeyTextField;
	private IdFollowupStepTable idFollowupStepTable;
	private IdLevelListTable idLevelListTable;
	private JCheckBox annotationsOnlyCheckBox;
	private JCheckBox searchAllIdsCheckBox;

	public IdAnnotationSearchParametersPanel() {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Filter by identification status ");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		idFilterComboBox = new JComboBox<FeatureSubsetByIdentification>(
				new DefaultComboBoxModel<FeatureSubsetByIdentification>(FeatureSubsetByIdentification.values()));
		idFilterComboBox.setPreferredSize(new Dimension(80, 25));
		idFilterComboBox.setMinimumSize(new Dimension(80, 25));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 2;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		add(idFilterComboBox, gbc_comboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Filter by name or ID");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		idOptionComboBox = new JComboBox<IdentifierSearchOptions>(
				new DefaultComboBoxModel<IdentifierSearchOptions>(IdentifierSearchOptions.values()));
		idOptionComboBox.setPreferredSize(new Dimension(80, 25));
		idOptionComboBox.setMinimumSize(new Dimension(80, 25));
		GridBagConstraints gbc_idOptionComboBox = new GridBagConstraints();
		gbc_idOptionComboBox.gridwidth = 2;
		gbc_idOptionComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_idOptionComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_idOptionComboBox.gridx = 1;
		gbc_idOptionComboBox.gridy = 1;
		add(idOptionComboBox, gbc_idOptionComboBox);
		
		nameIdTextField = new JTextField();
		GridBagConstraints gbc_nameIdTextField = new GridBagConstraints();
		gbc_nameIdTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameIdTextField.gridwidth = 3;
		gbc_nameIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameIdTextField.gridx = 0;
		gbc_nameIdTextField.gridy = 2;
		add(nameIdTextField, gbc_nameIdTextField);
		nameIdTextField.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Filter by formula");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 3;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		formulaTextField = new JTextField();
		GridBagConstraints gbc_formulaTextField = new GridBagConstraints();
		gbc_formulaTextField.gridwidth = 2;
		gbc_formulaTextField.insets = new Insets(0, 0, 5, 0);
		gbc_formulaTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formulaTextField.gridx = 1;
		gbc_formulaTextField.gridy = 3;
		add(formulaTextField, gbc_formulaTextField);
		formulaTextField.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Filter by InChIKey");
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 4;
		add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		inChIKeyTextField = new JTextField();
		GridBagConstraints gbc_InChIKeyTextField = new GridBagConstraints();
		gbc_InChIKeyTextField.gridwidth = 2;
		gbc_InChIKeyTextField.insets = new Insets(0, 0, 5, 0);
		gbc_InChIKeyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_InChIKeyTextField.gridx = 1;
		gbc_InChIKeyTextField.gridy = 4;
		add(inChIKeyTextField, gbc_InChIKeyTextField);
		inChIKeyTextField.setColumns(10);
		
		searchAllIdsCheckBox = new JCheckBox("Search all identifications");
		searchAllIdsCheckBox.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 5;
		add(searchAllIdsCheckBox, gbc_chckbxNewCheckBox);
		
		JLabel lblNewLabel_4 = new JLabel("If not checked, only primary IDs are searched");
		lblNewLabel_4.setForeground(Color.RED);
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.gridwidth = 2;
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_4.gridx = 1;
		gbc_lblNewLabel_4.gridy = 5;
		add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		annotationsOnlyCheckBox = new JCheckBox("Only features with annotations");
		annotationsOnlyCheckBox.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_annotationsOnlyCheckBox = new GridBagConstraints();
		gbc_annotationsOnlyCheckBox.anchor = GridBagConstraints.WEST;
		gbc_annotationsOnlyCheckBox.gridwidth = 3;
		gbc_annotationsOnlyCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_annotationsOnlyCheckBox.gridx = 0;
		gbc_annotationsOnlyCheckBox.gridy = 6;
		add(annotationsOnlyCheckBox, gbc_annotationsOnlyCheckBox);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridwidth = 3;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 7;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		idLevelListTable = new IdLevelListTable();
		idLevelListTable.setTableModelFromLevelList(
				IDTDataCash.getMsFeatureIdentificationLevelList());
		JScrollPane scrollPane = new JScrollPane(idLevelListTable);
//		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new CompoundBorder(new EmptyBorder(10, 0, 0, 5), 
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Identification levels", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 12), new Color(0, 0, 0))));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel.add(scrollPane, gbc_scrollPane);
		
		idFollowupStepTable = new IdFollowupStepTable();
		idFollowupStepTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		idFollowupStepTable.setTableModelFromFollowupStepList(
				IDTDataCash.getMsFeatureIdentificationFollowupStepList());
		JScrollPane scrollPane_1 = new JScrollPane(idFollowupStepTable);
//		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBorder(new CompoundBorder(new EmptyBorder(10, 5, 0, 0), 
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Followup steps", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 12), new Color(0, 0, 0))));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 0;
		panel.add(scrollPane_1, gbc_scrollPane_1);
		
		JButton resetButton = new JButton(
				"Reset to default values", IDTrackerDataSearchDialog.resetIcon);
		resetButton.setActionCommand(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName());
		resetButton.addActionListener(this);
		GridBagConstraints gbc_resetButton = new GridBagConstraints();
		gbc_resetButton.anchor = GridBagConstraints.SOUTHWEST;
		gbc_resetButton.gridx = 0;
		gbc_resetButton.gridy = 8;
		add(resetButton, gbc_resetButton);
	}
	
	public FeatureSubsetByIdentification getFeatureSubsetByIdentification() {
		return (FeatureSubsetByIdentification)idFilterComboBox.getSelectedItem();
	}
	
	public void setFeatureSubsetByIdentification(FeatureSubsetByIdentification subset) {
		idFilterComboBox.setSelectedItem(subset);
	}
	
	public IdentifierSearchOptions getIdentifierSearchOption() {
		return (IdentifierSearchOptions)idOptionComboBox.getSelectedItem();
	}
	
	public void setIdentifierSearchOption(IdentifierSearchOptions option) {
		idOptionComboBox.setSelectedItem(option);
	}
	
	public String getNameIdString() {
		return nameIdTextField.getText().trim();
	}
	
	public void setNameIdString(String nameOrId) {
		nameIdTextField.setText(nameOrId);
	}
	
	public String getFormula() {
		return formulaTextField.getText().trim();
	}
	
	public void setFormula(String formula) {
		formulaTextField.setText(formula);
	}
	
	public String getInChIKey() {
		return inChIKeyTextField.getText().trim().toUpperCase();
	}
	
	public void setInChIKey(String inChIKey) {
		inChIKeyTextField.setText(inChIKey);
	}
	
	public boolean getAnnotatedOnly() {
		return annotationsOnlyCheckBox.isSelected();
	}
	
	public void setAnnotatedOnly(boolean annotatedOnly) {
		annotationsOnlyCheckBox.setSelected(annotatedOnly);
	}
	
	public Collection<MSFeatureIdentificationLevel>getSelectedIdLevels(){
		return idLevelListTable.getSelectedLevels();
	}
	
	public void setSelectedIdLevels(Collection<MSFeatureIdentificationLevel>levelsToSelect) {
		idLevelListTable.selectLevelList(levelsToSelect);
		if(idLevelListTable.getSelectedRow() > -1)
			idLevelListTable.scrollToSelected();
	}
	
	public Collection<MSFeatureIdentificationFollowupStep>getSelectedFollowupSteps(){
		return idFollowupStepTable.getSelectedFollowupSteps();
	}
	
	public void setSelectedFollowupSteps(Collection<MSFeatureIdentificationFollowupStep>stepsToSelect) {
		idFollowupStepTable.selectFollowupSteps(stepsToSelect);
		if(idFollowupStepTable.getSelectedRow() > -1)
			idFollowupStepTable.scrollToSelected();
	}
	
	public boolean searchAllIds() {
		return searchAllIdsCheckBox.isSelected();
	}
	
	public void setSearchAllIds(boolean b) {
		searchAllIdsCheckBox.setSelected(b);
	}

	public void resetPanel() {
		
		idFilterComboBox.setSelectedItem(FeatureSubsetByIdentification.ALL);
		idOptionComboBox.setSelectedIndex(-1);
		nameIdTextField.setText("");
		formulaTextField.setText("");
		inChIKeyTextField.setText("");
		annotationsOnlyCheckBox.setSelected(false);
		idLevelListTable.clearSelection();
		idFollowupStepTable.clearSelection();
		searchAllIdsCheckBox.setSelected(false);
	}	
	
	public Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
		if(!getNameIdString().isEmpty() && getIdentifierSearchOption() == null)
			errors.add("Please select the ID search option from the \"Filter by name or ID\" dropdown \n"
					+ "on \"ID / Annotations\" panel or clear Name/ID search field");

		if(!getFormula().isEmpty() && !fomulaValid(getFormula()))
			errors.add("Formula invalid on \"ID / Annotations\" panel.");
		
		if(!getInChIKey().isEmpty() && !inchiKeyIsValid())
			errors.add("InChiKey invalid on \"ID / Annotations\" panel.");
		
		return errors;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName()))
			resetPanel();
	}

	public boolean hasLimitingInput() {

//		if(!getNameIdString().isEmpty() && getIdentifierSearchOption() != null)
//			return true;
//		
//		if(!getFormula().isEmpty() && fomulaValid(getFormula()))
//			return true;
//		
//		if(!getInChIKey().isEmpty() && inchiKeyIsValid())
//			return true;
		
		if(!getNameIdString().isEmpty() || !getFormula().isEmpty() || !getInChIKey().isEmpty())
			return true;
		else	
			return false;
	}
	
	private boolean inchiKeyIsValid() {
		
		if(getInChIKey().length() == 27 || getInChIKey().length() == 14)
			return true;
		else
			return false;
	}

	private boolean fomulaValid(String formulaString) {

		IMolecularFormula queryFormula = null;
		try {
			queryFormula = MolecularFormulaManipulator.getMolecularFormula(formulaString,
					DefaultChemObjectBuilder.getInstance());
		} catch (Exception e) {
			return false;
		}
		return queryFormula != null;
	}
}












