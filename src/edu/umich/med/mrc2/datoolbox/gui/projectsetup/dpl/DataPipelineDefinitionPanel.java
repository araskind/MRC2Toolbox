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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.dpl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.gui.communication.DataPipelineEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.DataPipelineEventListener;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class DataPipelineDefinitionPanel extends JPanel 
		implements ActionListener, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7138471968941298758L;
	
	private static final Icon assayIcon = GuiUtils.getIcon("assay", 32);
	private static final Icon acquisitionMethodIcon = GuiUtils.getIcon("dataAcquisitionMethod", 32);
	private static final Icon dataAnalysisMethodIcon = GuiUtils.getIcon("dataProcessing", 32);
	
	private JTextField acqMethodTextField;
	private JTextField daMethodTextField;
	private JTextField nameTextField;
	private JComboBox assayComboBox;
	private JTextArea descriptionTextArea;
	private DataAcquisitionMethod acquisitionMethod;
	private DataExtractionMethod dataExtractionMethod;	
	private AcquisitionMethodSelectorDialog acquisitionMethodSelectorDialog;
	private DataExtractionMethodSelectorDialog dataExtractionMethodSelectorDialog;
	private JComboBox motrpacAssayCodeComboBox;
	private JButton showDataAcqSelectorButton;
	private JButton showDataExtrSelectorButton;
	private Set<DataPipelineEventListener> eventListeners;

	@SuppressWarnings("rawtypes")
	public DataPipelineDefinitionPanel() {
		super();
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gbl_this = new GridBagLayout();
		gbl_this.columnWidths = new int[]{0, 49, 0, 0, 0};
		gbl_this.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_this.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_this.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		this.setLayout(gbl_this);
		
		JLabel lblNewLabel_3 = new JLabel("Name");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 0;
		this.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		nameTextField = new JTextField();
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 3;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		this.add(nameTextField, gbc_nameTextField);
		nameTextField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Assay");
		lblNewLabel.setIcon(assayIcon);		
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		this.add(lblNewLabel, gbc_lblNewLabel);
		
		assayComboBox = new JComboBox();
		GridBagConstraints gbc_assayComboBox = new GridBagConstraints();
		gbc_assayComboBox.gridwidth = 3;
		gbc_assayComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_assayComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_assayComboBox.gridx = 1;
		gbc_assayComboBox.gridy = 1;
		this.add(assayComboBox, gbc_assayComboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Acquisition");
		lblNewLabel_1.setIcon(acquisitionMethodIcon);
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		this.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		acqMethodTextField = new JTextField();
		acqMethodTextField.setEditable(false);
		GridBagConstraints gbc_acqMethodTextField = new GridBagConstraints();
		gbc_acqMethodTextField.gridwidth = 2;
		gbc_acqMethodTextField.insets = new Insets(0, 0, 5, 5);
		gbc_acqMethodTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_acqMethodTextField.gridx = 1;
		gbc_acqMethodTextField.gridy = 2;
		this.add(acqMethodTextField, gbc_acqMethodTextField);
		acqMethodTextField.setColumns(10);
		
		showDataAcqSelectorButton = new JButton("Select method ...");
		showDataAcqSelectorButton.setActionCommand(
				MainActionCommands.SHOW_DATA_ACQUISITION_SELECTOR_COMMAND.getName());
		showDataAcqSelectorButton.addActionListener(this);		
		GridBagConstraints gbc_showDataAcqSelectorButton = new GridBagConstraints();
		gbc_showDataAcqSelectorButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_showDataAcqSelectorButton.insets = new Insets(0, 0, 5, 0);
		gbc_showDataAcqSelectorButton.gridx = 3;
		gbc_showDataAcqSelectorButton.gridy = 2;
		this.add(showDataAcqSelectorButton, gbc_showDataAcqSelectorButton);
		
		JLabel lblNewLabel_2 = new JLabel("Data analysis");
		lblNewLabel_2.setIcon(dataAnalysisMethodIcon);
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 3;
		this.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		daMethodTextField = new JTextField();
		daMethodTextField.setEditable(false);
		GridBagConstraints gbc_daMethodTextField = new GridBagConstraints();
		gbc_daMethodTextField.gridwidth = 2;
		gbc_daMethodTextField.insets = new Insets(0, 0, 5, 5);
		gbc_daMethodTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_daMethodTextField.gridx = 1;
		gbc_daMethodTextField.gridy = 3;
		this.add(daMethodTextField, gbc_daMethodTextField);
		daMethodTextField.setColumns(10);
		
		showDataExtrSelectorButton = new JButton("Select method ...");
		showDataExtrSelectorButton.setActionCommand(
				MainActionCommands.SHOW_DATA_EXTRACTION_SELECTOR_COMMAND.getName());
		showDataExtrSelectorButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 3;
		this.add(showDataExtrSelectorButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_4 = new JLabel("Description");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 4;
		this.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setRows(3);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.gridwidth = 4;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 5;
		this.add(descriptionTextArea, gbc_textArea);
		
		JLabel lblNewLabel_5 = new JLabel("MoTrPAC assay code");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_5.gridwidth = 2;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 6;
		add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		motrpacAssayCodeComboBox = new JComboBox();
		GridBagConstraints gbc_motrpacAssayCodeComboBox = new GridBagConstraints();
		gbc_motrpacAssayCodeComboBox.gridwidth = 2;
		gbc_motrpacAssayCodeComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_motrpacAssayCodeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_motrpacAssayCodeComboBox.gridx = 2;
		gbc_motrpacAssayCodeComboBox.gridy = 6;
		add(motrpacAssayCodeComboBox, gbc_motrpacAssayCodeComboBox);
		
		eventListeners = ConcurrentHashMap.newKeySet();
		populateAssaySelector();
		assayComboBox.addItemListener(this);
		populateMotrpacAssayComboBox();
		motrpacAssayCodeComboBox.addItemListener(this);
	}
	
	public void addListener(DataPipelineEventListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.add(listener);
	}

	public void removeListener(DataPipelineEventListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.remove(listener);
	}
	
	@SuppressWarnings("unchecked")
	private void populateAssaySelector() {
		Collection<Assay> assays = new TreeSet<Assay>();		
		try {
			assays = LIMSUtils.getLIMSAssayList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		assayComboBox.setModel(new SortedComboBoxModel<Assay>(assays));
		assayComboBox.setSelectedIndex(-1);
	}
	
	@SuppressWarnings("unchecked")
	private void populateMotrpacAssayComboBox() {
		
		Collection<MoTrPACAssay> mpAssays = MoTrPACDatabaseCash.getMotrpacAssayList();
		DefaultComboBoxModel<MoTrPACAssay>model = 
				new DefaultComboBoxModel<MoTrPACAssay>(mpAssays.toArray(new MoTrPACAssay[mpAssays.size()]));
		motrpacAssayCodeComboBox.setModel(model);
		motrpacAssayCodeComboBox.setSelectedIndex(-1);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SHOW_DATA_ACQUISITION_SELECTOR_COMMAND.getName())) {
			acquisitionMethodSelectorDialog = new AcquisitionMethodSelectorDialog(this);
			acquisitionMethodSelectorDialog.setLocationRelativeTo(this);
			acquisitionMethodSelectorDialog.setVisible(true);
		}
		if(command.equals(MainActionCommands.SELECT_DATA_ACQUISITION_METHOD_COMMAND.getName())) {
			
			acquisitionMethod = acquisitionMethodSelectorDialog.getSelectedMethod();
			if(acquisitionMethod == null)
				return;
			
			acqMethodTextField.setText(acquisitionMethod.getName());
			acquisitionMethodSelectorDialog.dispose();
			fireDataPipelineEvent();
		}
		if(command.equals(MainActionCommands.SHOW_DATA_EXTRACTION_SELECTOR_COMMAND.getName())) {
			dataExtractionMethodSelectorDialog = new DataExtractionMethodSelectorDialog(this);
			dataExtractionMethodSelectorDialog.setLocationRelativeTo(this);
			dataExtractionMethodSelectorDialog.setVisible(true);
		}
		if(command.equals(MainActionCommands.SELECT_DATA_EXTRACTION_METHOD_COMMAND.getName())) {
			
			dataExtractionMethod = dataExtractionMethodSelectorDialog.getSelectedMethod();
			if(dataExtractionMethod == null)
				return;
			
			daMethodTextField.setText(dataExtractionMethod.getName());
			dataExtractionMethodSelectorDialog.dispose();
			fireDataPipelineEvent();
		}
	}
	
	public void fireDataPipelineEvent() {
		
		if(eventListeners == null){
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		DataPipelineEvent event = new DataPipelineEvent(getUnverifiedDataPipeline());
		eventListeners.stream().forEach(l -> l.dataPipelineChanged(event));		
	}
	
	public String getName() {
		return nameTextField.getText().trim();
	}
	
	public String getDescription() {
		return descriptionTextArea.getText().trim();
	}
	
	
	public Assay getAssay() {
		return (Assay)assayComboBox.getSelectedItem();
	}

	public DataAcquisitionMethod getAcquisitionMethod() {
		return acquisitionMethod;
	}

	public DataExtractionMethod getDataExtractionMethod() {
		return dataExtractionMethod;
	}
	
	public MoTrPACAssay getMotrpacAssay() {
		return (MoTrPACAssay)motrpacAssayCodeComboBox.getSelectedItem();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {
			fireDataPipelineEvent();
		}
	}
	
	public DataPipeline getDataPipeline() {
		
		Collection<String>errors = validatePipelineDefinition();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return null;
		}
		DataPipeline dpl = new DataPipeline(
				getName(), getAssay(), acquisitionMethod, dataExtractionMethod);
		dpl.setDescription(getDescription());
		dpl.setMotrpacAssay(getMotrpacAssay());
		return dpl;
	}
	
	public DataPipeline getUnverifiedDataPipeline() {
		
		DataPipeline dpl = new DataPipeline(
				getName(), getAssay(), acquisitionMethod, dataExtractionMethod);
		dpl.setDescription(getDescription());
		dpl.setMotrpacAssay(getMotrpacAssay());
		return dpl;
	}
		
	public void setDataPipeline(DataPipeline pipeline) {
		
		nameTextField.setText(pipeline.getName());
		descriptionTextArea.setText(pipeline.getDescription());
		assayComboBox.setSelectedItem(pipeline.getAssay());
		acquisitionMethod = pipeline.getAcquisitionMethod();
		acqMethodTextField.setText(acquisitionMethod.getName());
		dataExtractionMethod = pipeline.getDataExtractionMethod();
		daMethodTextField.setText(dataExtractionMethod.getName());
	}
	
	public void lockPanel() {
		
		nameTextField.setEditable(false);
		descriptionTextArea.setEditable(false);
		assayComboBox.setEnabled(false);		
		showDataAcqSelectorButton.setEnabled(false);		
		showDataExtrSelectorButton.setEnabled(false);
	}
	
	public Collection<String>validatePipelineDefinition(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getName().isEmpty())
			errors.add("Name can not be empty");
		
		if(getAssay() == null)
			errors.add("Assay not defined");
		
		if(acquisitionMethod == null)
			errors.add("Data acquisition method not defined");
		
		if(dataExtractionMethod == null)
			errors.add("Data analysis method not defined");
		
		return errors;
	}
}

















