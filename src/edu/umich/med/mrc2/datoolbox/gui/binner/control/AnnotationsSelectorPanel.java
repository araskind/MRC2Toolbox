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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.BorderLayout;
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

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.BinnerAdductList;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.adducts.bindif.BinnerAnnotationsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.ValidatableForm;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.main.config.NumberFormatStore;

public class AnnotationsSelectorPanel extends JPanel implements ValidatableForm, ActionListener{

	private static final long serialVersionUID = 1L;
	private BinnerAnnotationsTable binnerAnnotationsTable;
	private JFormattedTextField annotRTToleranceField;
	private JFormattedTextField annotMassToleranceField;
	private JCheckBox neutMasForChargeCarrierCheckBox;
	private JCheckBox varChargeCheckBox;
	private BinnerAnnotationSelectorToolbar toolbar;
	private JLabel nameLabel;
	private JLabel descriptionLabel;
	private JLabel ownerLabel;
	private JLabel dateCreatedLabel;
	private JLabel lastModifiedLabel;
	private BinnerAdductList binnerAdductList;
	private AnnotationListEditorDialog annotationListEditorDialog;

	public AnnotationsSelectorPanel() {
		super(new BorderLayout(0, 0));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Annotation assignment parameters", TitledBorder.LEADING, TitledBorder.TOP, null, 
				new Color(0, 0, 0)), new EmptyBorder(10, 10, 10, 10)));
		add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Mass tolerance");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		annotMassToleranceField = 
				new JFormattedTextField(NumberFormatStore.getDefaultMZformat());
		annotMassToleranceField.setColumns(10);
		annotMassToleranceField.setPreferredSize(new Dimension(80, 20));
		annotMassToleranceField.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_annotMassToleranceField = new GridBagConstraints();
		gbc_annotMassToleranceField.insets = new Insets(0, 0, 5, 5);
		gbc_annotMassToleranceField.fill = GridBagConstraints.HORIZONTAL;
		gbc_annotMassToleranceField.gridx = 1;
		gbc_annotMassToleranceField.gridy = 0;
		panel.add(annotMassToleranceField, gbc_annotMassToleranceField);
		
		JLabel lblNewLabel_3 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_3.gridx = 2;
		gbc_lblNewLabel_3.gridy = 0;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		JLabel lblNewLabel_1 = new JLabel("RT tolerance");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		annotRTToleranceField = 
				new JFormattedTextField(NumberFormatStore.getDefaultRTformat());
		annotRTToleranceField.setPreferredSize(new Dimension(80, 20));
		annotRTToleranceField.setMinimumSize(new Dimension(80, 20));
		annotRTToleranceField.setColumns(10);
		GridBagConstraints gbc_annotRTToleranceField = new GridBagConstraints();
		gbc_annotRTToleranceField.insets = new Insets(0, 0, 5, 5);
		gbc_annotRTToleranceField.fill = GridBagConstraints.HORIZONTAL;
		gbc_annotRTToleranceField.gridx = 1;
		gbc_annotRTToleranceField.gridy = 1;
		panel.add(annotRTToleranceField, gbc_annotRTToleranceField);
		
		JLabel lblNewLabel_2 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 1;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		neutMasForChargeCarrierCheckBox = 
				new JCheckBox("Use neutral masses to help determine best charge carrier");
		GridBagConstraints gbc_neutMasForChargeCarrierCheckBox = new GridBagConstraints();
		gbc_neutMasForChargeCarrierCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_neutMasForChargeCarrierCheckBox.gridwidth = 3;
		gbc_neutMasForChargeCarrierCheckBox.anchor = GridBagConstraints.WEST;
		gbc_neutMasForChargeCarrierCheckBox.gridx = 0;
		gbc_neutMasForChargeCarrierCheckBox.gridy = 2;
		panel.add(neutMasForChargeCarrierCheckBox, gbc_neutMasForChargeCarrierCheckBox);
		
		varChargeCheckBox = 
				new JCheckBox("Allow variable charge without isotope information");
		GridBagConstraints gbc_varChargeCheckBox = new GridBagConstraints();
		gbc_varChargeCheckBox.anchor = GridBagConstraints.WEST;
		gbc_varChargeCheckBox.gridwidth = 3;
		gbc_varChargeCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_varChargeCheckBox.gridx = 0;
		gbc_varChargeCheckBox.gridy = 3;
		panel.add(varChargeCheckBox, gbc_varChargeCheckBox);
		
		JPanel tableWrap = new JPanel(new BorderLayout(0, 0));		
		tableWrap.setBorder(new CompoundBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Available annotations list", 
				TitledBorder.LEADING, TitledBorder.TOP, null, 
				new Color(0, 0, 0)), new EmptyBorder(10, 0, 0, 0)));
		
		toolbar = new BinnerAnnotationSelectorToolbar(this);
		tableWrap.add(toolbar, BorderLayout.NORTH);
		
		binnerAnnotationsTable = new BinnerAnnotationsTable();
		tableWrap.add(new JScrollPane(binnerAnnotationsTable), BorderLayout.CENTER);
		add(tableWrap, BorderLayout.CENTER);
		
		JPanel listInfoPanel = createInfoPanel();
		tableWrap.add(listInfoPanel, BorderLayout.SOUTH);
	}
	
	private JPanel createInfoPanel() {
		
		JPanel listInfoPanel = new JPanel();
		listInfoPanel.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Annotation list details", TitledBorder.LEADING, 
				TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 5, 5, 5)));
		
		GridBagLayout gbl_listInfoPanel = new GridBagLayout();
		gbl_listInfoPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_listInfoPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_listInfoPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_listInfoPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		listInfoPanel.setLayout(gbl_listInfoPanel);
		
		JLabel lblNewLabel_4 = new JLabel("Name: ");
		lblNewLabel_4.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 0;
		listInfoPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		nameLabel = new JLabel("");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.anchor = GridBagConstraints.WEST;
		gbc_nameLabel.gridwidth = 4;
		gbc_nameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_nameLabel.gridx = 1;
		gbc_nameLabel.gridy = 0;
		listInfoPanel.add(nameLabel, gbc_nameLabel);
		
		JLabel lblNewLabel_5 = new JLabel("Description: ");
		lblNewLabel_5.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 1;
		listInfoPanel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		descriptionLabel = new JLabel("");
		GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
		gbc_descriptionLabel.anchor = GridBagConstraints.WEST;
		gbc_descriptionLabel.gridwidth = 4;
		gbc_descriptionLabel.insets = new Insets(0, 0, 5, 0);
		gbc_descriptionLabel.gridx = 1;
		gbc_descriptionLabel.gridy = 1;
		listInfoPanel.add(descriptionLabel, gbc_descriptionLabel);
		
		JLabel lblNewLabel_6 = new JLabel("Owner: ");
		lblNewLabel_6.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = 2;
		listInfoPanel.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		ownerLabel = new JLabel("");
		GridBagConstraints gbc_ownerLabel = new GridBagConstraints();
		gbc_ownerLabel.anchor = GridBagConstraints.WEST;
		gbc_ownerLabel.gridwidth = 4;
		gbc_ownerLabel.insets = new Insets(0, 0, 5, 0);
		gbc_ownerLabel.gridx = 1;
		gbc_ownerLabel.gridy = 2;
		listInfoPanel.add(ownerLabel, gbc_ownerLabel);
		
		JLabel lblNewLabel_7 = new JLabel("Date created: ");
		lblNewLabel_7.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_7.gridx = 0;
		gbc_lblNewLabel_7.gridy = 3;
		listInfoPanel.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		dateCreatedLabel = new JLabel("          ");
		dateCreatedLabel.setMinimumSize(new Dimension(80, 14));
		dateCreatedLabel.setPreferredSize(new Dimension(80, 14));
		GridBagConstraints gbc_dateCreatedLabel = new GridBagConstraints();
		gbc_dateCreatedLabel.insets = new Insets(0, 0, 0, 5);
		gbc_dateCreatedLabel.gridx = 1;
		gbc_dateCreatedLabel.gridy = 3;
		listInfoPanel.add(dateCreatedLabel, gbc_dateCreatedLabel);
		
		JLabel lblNewLabel_8 = new JLabel("Last modified: ");
		lblNewLabel_8.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_8.gridx = 2;
		gbc_lblNewLabel_8.gridy = 3;
		listInfoPanel.add(lblNewLabel_8, gbc_lblNewLabel_8);
		
		lastModifiedLabel = new JLabel("");
		lastModifiedLabel.setPreferredSize(new Dimension(80, 14));
		GridBagConstraints gbc_lastModifiedLabel = new GridBagConstraints();
		gbc_lastModifiedLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lastModifiedLabel.gridx = 3;
		gbc_lastModifiedLabel.gridy = 3;
		listInfoPanel.add(lastModifiedLabel, gbc_lastModifiedLabel);
		
		return listInfoPanel;
	}
	
	public void clearAnnotationList() {
		
		binnerAdductList = null;
		binnerAnnotationsTable.clearTable();
		nameLabel.setText("");
		descriptionLabel.setText("");
		ownerLabel.setText("");
		dateCreatedLabel.setText("");
		lastModifiedLabel.setText("");
	}

	@Override
	public Collection<String> validateFormData() {

		Collection<String>errors = new ArrayList<String>();
		
		
		return errors;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.OPEN_BINNER_ANNOTATION_LIST_COMMAND.getName())) 
			showAnnotationListSelector();
		
		if(command.equals(MainActionCommands.NEW_BINNER_ANNOTATION_LIST_COMMAND.getName()))
			showAnnotationListEditor(null);
		
		if(command.equals(MainActionCommands.EDIT_BINNER_ANNOTATION_LIST_COMMAND.getName()) 
				&& binnerAdductList != null)
					showAnnotationListEditor(binnerAdductList);
		
		if(command.equals(MainActionCommands.SAVE_BINNER_ANNOTATION_LIST_COMMAND.getName()))
			saveAndReloadAnnotationList();
			
		if(command.equals(MainActionCommands.DELETE_BINNER_ANNOTATION_LIST_COMMAND.getName()))
			deleteActiveAnnotationList();		
	}

	private void deleteActiveAnnotationList() {

		if(binnerAdductList == null)
			return;
		
		if(!IDTUtils.isSuperUser(this))
			return;
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to delete Binner "
				+ "annotation list \"" + binnerAdductList.getName() +"\" ", this);
		if(res == JOptionPane.YES_OPTION) {
			
			clearAnnotationList();
			try {
				BinnerUtils.deleteBinnerAdductList(binnerAdductList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void showAnnotationListSelector() {
		// TODO Auto-generated method stub
		
	}

	private void showAnnotationListEditor(BinnerAdductList binnerAdductList) {

		if(binnerAdductList != null && !MRC2ToolBoxCore.getIdTrackerUser().equals(binnerAdductList.getOwner())) {
			
			MessageDialog.showWarningMsg("Binner annotation list \"" +
					binnerAdductList.getName() + "\" may only be edited by its owner", this);
			return;
		}
		annotationListEditorDialog = new AnnotationListEditorDialog(binnerAdductList, this);
		annotationListEditorDialog.setLocationRelativeTo(this);
		annotationListEditorDialog.setVisible(true);
	}
	
	private void saveAndReloadAnnotationList() {
		// TODO Auto-generated method stub
		
		
		
		annotationListEditorDialog.dispose();
	}
	
	public void loadBinnerAdductList(BinnerAdductList binnerAdductList) {
		
		this.binnerAdductList = binnerAdductList;
		clearAnnotationList();
		if(binnerAdductList == null)
			return;
		
		binnerAnnotationsTable.setTableModelFromBinnerAdductTierMap(binnerAdductList.getComponents());
		
		nameLabel.setText(binnerAdductList.getName());
		descriptionLabel.setText(binnerAdductList.getDescription());
		ownerLabel.setText(binnerAdductList.getOwner().toString());
		dateCreatedLabel.setText(
				MRC2ToolBoxConfiguration.getDateTimeFormat().format(binnerAdductList.getDateCreated()));
		lastModifiedLabel.setText(
				MRC2ToolBoxConfiguration.getDateTimeFormat().format(binnerAdductList.getLastModified()));		
	}

	public BinnerAdductList getBinnerAdductList() {
		return binnerAdductList;
	}
}
