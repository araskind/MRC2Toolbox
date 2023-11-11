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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.lookup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableLookupFeatureTable extends DefaultSingleCDockable implements ActionListener {
	
	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	
	private MSMSClusterDataSet activeMSMSClusterDataSet;
	private LookupFeatureListTable featureTable;
	private ListSelectionListener lsl;
	private JTextField dataSetNameTextField;
	private JTextArea descriptionTextArea;
	private JButton btnToggleList;
	
	public DockableLookupFeatureTable(
			String id, 
			String title, 
			ActionListener actionListener, 
			ListSelectionListener lsl) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));

		featureTable = new LookupFeatureListTable();
		featureTable.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		this.lsl = lsl;
		featureTable.getSelectionModel().addListSelectionListener(lsl);
		add(new JScrollPane(featureTable), BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
				new CompoundBorder(new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Feature set properties", TitledBorder.LEADING, 
						TitledBorder.TOP, null, new Color(0, 0, 0)), 
						new EmptyBorder(10, 10, 10, 10))));
		add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Name ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		dataSetNameTextField = new JTextField();
		dataSetNameTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(dataSetNameTextField, gbc_textField);
		dataSetNameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Description");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setRows(2);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setEditable(false);
		descriptionTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 2;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 2;
		panel.add(descriptionTextArea, gbc_textArea);
		
		JPanel bpanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bpanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(bpanel, BorderLayout.SOUTH);
		btnToggleList = new JButton(
				MainActionCommands.SHOW_COMPLETE_LOOKUP_FEATURE_LIST_COMMAND.getName());
		btnToggleList.setActionCommand(
				MainActionCommands.SHOW_COMPLETE_LOOKUP_FEATURE_LIST_COMMAND.getName());
		btnToggleList.addActionListener(this);
		bpanel.add(btnToggleList);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();	
		if(command.equals(MainActionCommands.SHOW_COMPLETE_LOOKUP_FEATURE_LIST_COMMAND.getName())) {
			showCompleteList();
		}
		if(command.equals(MainActionCommands.SHOW_ONLY_MATCHED_LOOKUP_FEATURES_COMMAND.getName())) {
			showMatchedOnly();
		}
	}
	
	private void showCompleteList() {
		
		if(activeMSMSClusterDataSet == null 
				|| activeMSMSClusterDataSet.getFeatureLookupDataSet() == null)
			return;
		
		featureTable.getSelectionModel().removeListSelectionListener(lsl);
		featureTable.setTableModelFromFeatureCollection(
				activeMSMSClusterDataSet.getFeatureLookupDataSet().getFeatures());
		featureTable.getSelectionModel().addListSelectionListener(lsl);
		
		btnToggleList.setText(
				MainActionCommands.SHOW_ONLY_MATCHED_LOOKUP_FEATURES_COMMAND.getName());
		btnToggleList.setActionCommand(
				MainActionCommands.SHOW_ONLY_MATCHED_LOOKUP_FEATURES_COMMAND.getName());
	}

	private void showMatchedOnly() {
		
		if(activeMSMSClusterDataSet == null 
				|| activeMSMSClusterDataSet.getFeatureLookupDataSet() == null)
			return;
		
		featureTable.getSelectionModel().removeListSelectionListener(lsl);
		featureTable.setTableModelFromFeatureCollection(
				activeMSMSClusterDataSet.getMatchedLookupFeatures());
		featureTable.getSelectionModel().addListSelectionListener(lsl);
		
		btnToggleList.setText(
				MainActionCommands.SHOW_COMPLETE_LOOKUP_FEATURE_LIST_COMMAND.getName());
		btnToggleList.setActionCommand(
				MainActionCommands.SHOW_COMPLETE_LOOKUP_FEATURE_LIST_COMMAND.getName());
		
	}

	public MinimalMSOneFeature getSelectedFeature(){
		return featureTable.getSelectedFeature();
	}

	public void loadDataSet(MSMSClusterDataSet activeMSMSClusterDataSet) {
		
		this.activeMSMSClusterDataSet = activeMSMSClusterDataSet;
		FeatureLookupDataSet dataSet = activeMSMSClusterDataSet.getFeatureLookupDataSet();
		if(dataSet == null) {
			clearPanel();
			return;
		}
		dataSetNameTextField.setText(dataSet.getName());
		descriptionTextArea.setText(dataSet.getDescription());		
		Collection<MinimalMSOneFeature>featureList = 
				activeMSMSClusterDataSet.getMatchedLookupFeatures();
		featureTable.getSelectionModel().removeListSelectionListener(lsl);
		featureTable.setTableModelFromFeatureCollection(featureList);
		featureTable.getSelectionModel().addListSelectionListener(lsl);
	}
	
	public void loadLookupFeatures(Collection<MinimalMSOneFeature>features) {
		
		featureTable.getSelectionModel().removeListSelectionListener(lsl);
		featureTable.setTableModelFromFeatureCollection(features);
		featureTable.getSelectionModel().addListSelectionListener(lsl);
	}
	
	public void clearPanel() {
		
		dataSetNameTextField.setText("");
		descriptionTextArea.setText("");		
		featureTable.clearTable();
		activeMSMSClusterDataSet = null;
	}

	public LookupFeatureListTable getTable() {
		return featureTable;
	}
}





