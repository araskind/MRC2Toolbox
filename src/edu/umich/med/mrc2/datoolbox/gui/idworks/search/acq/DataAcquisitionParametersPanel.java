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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.acq;

import java.awt.Dimension;
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
import javax.swing.ListSelectionModel;

import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.AcquisitionMethodTable;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dextr.DataExtractionMethodTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.IDTrackerDataSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class DataAcquisitionParametersPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7633915974561498973L;
	
	private ChromatographicSeparationTypeTable chromatographicSeparationTypeTable;
	private MsTypeTable msTypeTable;
	private ChromatographicColumnListTable chromatographicColumnListTable;
	private AcquisitionMethodTable acquisitionMethodTable;
	private DataExtractionMethodTable dataExtractionMethodTable;
	
	public DataAcquisitionParametersPanel() {
		super();
		setMinimumSize(new Dimension(600, 600));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Chromatographic separation types");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		JLabel lblNewLabel_2 = new JLabel("MS types");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 0;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		chromatographicSeparationTypeTable = new ChromatographicSeparationTypeTable();
		JScrollPane scrollPane_1 = new JScrollPane(chromatographicSeparationTypeTable);
//		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		add(scrollPane_1, gbc_scrollPane_1);
		
		msTypeTable = new MsTypeTable();
		JScrollPane scrollPane_2 = new JScrollPane(msTypeTable);
//		JScrollPane scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_2.gridwidth = 3;
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 1;
		gbc_scrollPane_2.gridy = 1;
		add(scrollPane_2, gbc_scrollPane_2);
		
		JLabel lblNewLabel_1 = new JLabel("Chromatographic columns");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
			
		chromatographicColumnListTable = new ChromatographicColumnListTable();
		JScrollPane scrollPane = new JScrollPane(chromatographicColumnListTable);
//		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 3;
		add(scrollPane, gbc_scrollPane);
		
		JLabel lblNewLabel_1_1 = new JLabel("Acquisition methods");
		lblNewLabel_1_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1_1 = new GridBagConstraints();
		gbc_lblNewLabel_1_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1_1.gridx = 0;
		gbc_lblNewLabel_1_1.gridy = 4;
		add(lblNewLabel_1_1, gbc_lblNewLabel_1_1);
		
		acquisitionMethodTable = new AcquisitionMethodTable();
		acquisitionMethodTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane_3 = new JScrollPane(acquisitionMethodTable);
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.gridwidth = 3;
		gbc_scrollPane_3.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.gridx = 0;
		gbc_scrollPane_3.gridy = 5;
		add(scrollPane_3, gbc_scrollPane_3);
		
		JLabel lblNewLabel_1_1_1 = new JLabel("Data analyzis methods");
		lblNewLabel_1_1_1.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1_1_1 = new GridBagConstraints();
		gbc_lblNewLabel_1_1_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1_1_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1_1_1.gridx = 0;
		gbc_lblNewLabel_1_1_1.gridy = 6;
		add(lblNewLabel_1_1_1, gbc_lblNewLabel_1_1_1);
		
		dataExtractionMethodTable = new DataExtractionMethodTable();
		dataExtractionMethodTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane_4 = new JScrollPane(dataExtractionMethodTable);
		GridBagConstraints gbc_scrollPane_4 = new GridBagConstraints();
		gbc_scrollPane_4.gridwidth = 3;
		gbc_scrollPane_4.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_4.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_4.gridx = 0;
		gbc_scrollPane_4.gridy = 7;
		add(scrollPane_4, gbc_scrollPane_4);
			
		JButton resetButton = new JButton(
				"Reset to default values", IDTrackerDataSearchDialog.resetIcon);
		resetButton.setActionCommand(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName());
		resetButton.addActionListener(this);
		
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 8;
		add(resetButton, gbc_btnNewButton);
	}
	
	public void populateTablesFromDatabase() {
		
		chromatographicSeparationTypeTable.setTableModelFromChromatographicSeparationTypeList(
					IDTDataCash.getChromatographicSeparationTypes());
		msTypeTable.setTableModelFromMsTypeList(IDTDataCash.getMsTypes());
		chromatographicColumnListTable.setTableModelFromColumns(
					IDTDataCash.getChromatographicColumns());	
		
		acquisitionMethodTable.setTableModelFromAcquisitionMethods(IDTDataCash.getAcquisitionMethods());
		dataExtractionMethodTable.setTableModelFromMethods(IDTDataCash.getDataExtractionMethods());
	}
	
	public void setSelectedDataAcquisitionMethods(Collection<DataAcquisitionMethod>methodsToSelect) {
		acquisitionMethodTable.setSelectedAcquisitionMethods(methodsToSelect);
		if(acquisitionMethodTable.getSelectedRow() > -1)
			acquisitionMethodTable.scrollToSelected();
	}
	
	public Collection<DataAcquisitionMethod> getSelectedAcquisitionMethods() {
		return acquisitionMethodTable.getSelectedAcquisitionMethods();
	}
	
	public void setSelectedDataExtractionMethods(Collection<DataExtractionMethod>methodsToSelect) {
		dataExtractionMethodTable.setSelectedDataExtractionMethods(methodsToSelect);
		if(dataExtractionMethodTable.getSelectedRow() > -1)
			dataExtractionMethodTable.scrollToSelected();
	}
	
	public Collection<DataExtractionMethod> getSelectedDataExtractionMethods() {
		return dataExtractionMethodTable.getSelectedDataExtractionMethods();
	}
	
	public Collection<ChromatographicSeparationType>getSelectedChromatographicSeparationTypes(){
		return chromatographicSeparationTypeTable.getSelectedTypes();
	}
	
	public void setSelectedChromatographicSeparationTypes(Collection<ChromatographicSeparationType>selectedTypes){
		chromatographicSeparationTypeTable.selectTypeList(selectedTypes);
		if(chromatographicSeparationTypeTable.getSelectedRow() > -1)
			chromatographicSeparationTypeTable.scrollToSelected();
	}
	
	public Collection<LIMSChromatographicColumn>getSelectedChromatographicColumns(){
		return chromatographicColumnListTable.getSelectedChromatographicColumns();
	}
	
	public void setSelectedChromatographicColumns(Collection<LIMSChromatographicColumn>selectedColumns){
		chromatographicColumnListTable.selectColumns(selectedColumns);
		if(chromatographicColumnListTable.getSelectedRow() > -1)
			chromatographicColumnListTable.scrollToSelected();
	}
	
	public Collection<MsType>getSelectedMsTypes(){
		return msTypeTable.getSelectedTypes();
	}
	
	public void setSelectedMsTypes(Collection<MsType>selectedTypes){
		msTypeTable.selectTypeList(selectedTypes);
		if(msTypeTable.getSelectedRow() > -1)
			msTypeTable.scrollToSelected();
	}
	
	public void resetPanel() {		
		chromatographicSeparationTypeTable.clearSelection();
		chromatographicColumnListTable.clearSelection();
		msTypeTable.clearSelection();		
		acquisitionMethodTable.clearSelection();		
		dataExtractionMethodTable.clearSelection();
	}
	
	public Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
		
		
		return errors;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName()))
			resetPanel();
	}
}














