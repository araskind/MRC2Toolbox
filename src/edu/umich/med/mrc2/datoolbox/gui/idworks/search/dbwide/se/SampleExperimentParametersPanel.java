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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.se;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.lookup.SampleTypeTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.IDTrackerDataSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class SampleExperimentParametersPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3528612529019257044L;
	private IDTrackerExperimentListingTable experimentListingTable;
	private SampleTypeTable sampleTypeTable;

	public SampleExperimentParametersPanel() {
		super();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Sample type ");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		sampleTypeTable = new SampleTypeTable();
		JScrollPane scrollPane_1 = new JScrollPane(sampleTypeTable);
//		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridwidth = 3;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		add(scrollPane_1, gbc_scrollPane_1);
		
		JLabel lblNewLabel_1 = new JLabel("Experiment");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();		
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		experimentListingTable = new IDTrackerExperimentListingTable();
		JScrollPane scrollPane = new JScrollPane(experimentListingTable);
//		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 3;
		add(scrollPane, gbc_scrollPane);

		JButton resetButton = new JButton(
				"Reset to default values", IDTrackerDataSearchDialog.resetIcon);
		resetButton.setActionCommand(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName());
		resetButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 4;
		add(resetButton, gbc_btnNewButton);
	}
	
	public void populateTablesFromDatabase() {
		
		experimentListingTable.setTableModelFromExperimentList(
					IDTDataCash.getExperiments());
		sampleTypeTable.setModelFromSampleTypeList(
					IDTDataCash.getSampleTypeList());
	}
	
	public void setTableModelFromExperimentList(Collection<LIMSExperiment>experimentList) {		
		experimentListingTable.setTableModelFromExperimentList(experimentList);
	}
	
	public Collection<LIMSExperiment>getSelectedExperiments(){
		 return experimentListingTable.getSelectedExperiments();
	}
	
	public void setSelectedExperiments(Collection<LIMSExperiment>experimentList) {
		experimentListingTable.setSelectedExperiments(experimentList);
		if(experimentListingTable.getSelectedRow() > -1)
			experimentListingTable.scrollToSelected();
	}
	
	public void setModelFromSampleTypeList(Collection<LIMSSampleType>typeList) {
		sampleTypeTable.setModelFromSampleTypeList(typeList);
	}

	public Collection<LIMSSampleType> getSelectedSampleTypes() {
		return sampleTypeTable.getSelectedSampleTypes();
	}
	
	public void setSelectedSampleTypes(Collection<LIMSSampleType>typeList) {
		sampleTypeTable.setSelectedSampleTypes(typeList);
		if(sampleTypeTable.getSelectedRow() > -1)
			sampleTypeTable.scrollToSelected();
	}

	public void resetPanel() {

		experimentListingTable.clearSelection();
		sampleTypeTable.clearSelection();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName()))
			resetPanel();
	}
	
	public Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
		
		
		return errors;
	}
}
