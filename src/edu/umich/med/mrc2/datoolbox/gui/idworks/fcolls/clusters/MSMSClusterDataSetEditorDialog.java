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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.clusters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MSMSClusterDataSetEditorDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3062896101017122798L;
	
	private static final Icon addFeatureCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("editCollection", 32);
	
	private MSMSClusterDataSet dataSet;
	private JButton btnSave;
	private JLabel dateCreatedLabel, lastModifiedLabel;
	private JTextArea descriptionTextArea;	
	private JTextField nameTextField;
	private JLabel idValueLabel;
	private JLabel ownerLabel;
	private Collection<MsFeatureInfoBundleCluster> clustersToAdd;
	private JCheckBox loadMSMSClusterDataSetCheckBox;

	private MSMSClusteringParameterSet msmsExtractionParameters;
	
	public MSMSClusterDataSetEditorDialog(
			MSMSClusterDataSet dataSet, 
			Collection<MsFeatureInfoBundleCluster> clustersToAdd,
			ActionListener actionListener) {
		super();
		setPreferredSize(new Dimension(700, 300));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.dataSet = dataSet;
		this.clustersToAdd = clustersToAdd;
		if(dataSet!= null)
			this.msmsExtractionParameters = dataSet.getParameters();
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 83, 114, 77, 100, 78, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		dataPanel.add(lblId, gbc_lblId);

		idValueLabel = new JLabel("");
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.anchor = GridBagConstraints.WEST;
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);
		
		JLabel lblCreated = new JLabel("Created on");
		GridBagConstraints gbc_lblCreated = new GridBagConstraints();
		gbc_lblCreated.anchor = GridBagConstraints.EAST;
		gbc_lblCreated.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreated.gridx = 2;
		gbc_lblCreated.gridy = 0;
		dataPanel.add(lblCreated, gbc_lblCreated);
		
		dateCreatedLabel = new JLabel("");
		GridBagConstraints gbc_dateCreatedLabel = new GridBagConstraints();
		gbc_dateCreatedLabel.anchor = GridBagConstraints.WEST;
		gbc_dateCreatedLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dateCreatedLabel.gridx = 3;
		gbc_dateCreatedLabel.gridy = 0;
		dataPanel.add(dateCreatedLabel, gbc_dateCreatedLabel);
		
		JLabel lblNewLabel = new JLabel("Last modified");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 4;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		lastModifiedLabel = new JLabel("");
		GridBagConstraints gbc_lastModifiedLabel = new GridBagConstraints();
		gbc_lastModifiedLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lastModifiedLabel.gridx = 5;
		gbc_lastModifiedLabel.gridy = 0;
		dataPanel.add(lastModifiedLabel, gbc_lastModifiedLabel);

		JLabel lblCreatedBy = new JLabel("Created by");
		GridBagConstraints gbc_lblCreatedBy = new GridBagConstraints();
		gbc_lblCreatedBy.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblCreatedBy.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatedBy.gridx = 0;
		gbc_lblCreatedBy.gridy = 1;
		dataPanel.add(lblCreatedBy, gbc_lblCreatedBy);

		ownerLabel = new JLabel("");
		GridBagConstraints gbc_methodAuthorLabel = new GridBagConstraints();
		gbc_methodAuthorLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodAuthorLabel.gridwidth = 4;
		gbc_methodAuthorLabel.insets = new Insets(0, 0, 5, 5);
		gbc_methodAuthorLabel.gridx = 1;
		gbc_methodAuthorLabel.gridy = 1;
		dataPanel.add(ownerLabel, gbc_methodAuthorLabel);

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 2;
		dataPanel.add(lblName, gbc_lblName);

		nameTextField = new JTextField();
		GridBagConstraints gbc_methodNameTextField = new GridBagConstraints();
		gbc_methodNameTextField.gridwidth = 6;
		gbc_methodNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_methodNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodNameTextField.gridx = 1;
		gbc_methodNameTextField.gridy = 2;
		dataPanel.add(nameTextField, gbc_methodNameTextField);
		nameTextField.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTH;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 3;
		dataPanel.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new LineBorder(new Color(0, 0, 0)));
		descriptionTextArea.setRows(3);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.gridwidth = 6;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 3;
		dataPanel.add(descriptionTextArea, gbc_textArea);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		loadMSMSClusterDataSetCheckBox = 
				new JCheckBox("Load MSMS cluster data set in the workbench");
		panel.add(loadMSMSClusterDataSetCheckBox);
		loadMSMSClusterDataSetCheckBox.setSelected(true);
		loadMSMSClusterDataSetCheckBox.setEnabled(false);
		
		Component horizontalStrut = Box.createHorizontalStrut(50);
		panel.add(horizontalStrut);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadClusterDataSetParameters();
	}

	private void loadClusterDataSetParameters() {

		if(dataSet == null) {

			setTitle("Create new MSMS cluster data set");
			setIconImage(((ImageIcon) addFeatureCollectionIcon).getImage());
			
//			if(clustersToAdd == null) 
				btnSave.setActionCommand(MainActionCommands.ADD_MSMS_CLUSTER_DATASET_COMMAND.getName());
//			else 
//				btnSave.setActionCommand(MainActionCommands.ADD_MSMS_CLUSTER_DATASET_WITH_CLUSTERS_COMMAND.getName());
			
			dateCreatedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));
			lastModifiedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));			
			ownerLabel.setText(MRC2ToolBoxCore.getIdTrackerUser().getInfo());
		}
		else {
			setTitle("Edit information for " + dataSet.getName());
			setIconImage(((ImageIcon) editFeatureCollectionIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_MSMS_CLUSTER_DATASET_COMMAND.getName());
			idValueLabel.setText(dataSet.getId());

			if (dataSet.getDateCreated() != null)
				dateCreatedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(
						dataSet.getDateCreated()));

			if (dataSet.getLastModified() != null)
				lastModifiedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(
						dataSet.getLastModified()));
			
			nameTextField.setText(dataSet.getName());
			descriptionTextArea.setText(dataSet.getDescription());

			if(dataSet.getCreatedBy() != null)
				ownerLabel.setText(dataSet.getCreatedBy().getInfo());
		}
		pack();
	}
	
	public Collection<String>validateCollectionData() {
		
		Collection<String>errors = new ArrayList<String>();
		String newName = getMSMSClusterDataSetName();
		if(newName.isEmpty())
			errors.add("Name can not be empty.");
		
		String nameError = null;
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			nameError = validateNameAgainstDatabase(newName);
		else
			nameError = validateNameAgainstProject(newName);
		
		if(nameError != null)
			errors.add(nameError);
		
		return errors;
	}

	private String validateNameAgainstDatabase(String newName) {
		
		MSMSClusterDataSet existing = null;
		if(this.dataSet == null) {
			existing = MSMSClusterDataSetManager.getMSMSClusterDataSetByName(newName);
		}
		else {
			String id = dataSet.getId();
			existing = MSMSClusterDataSetManager.getMSMSClusterDataSets().stream().
					filter(f -> !f.getId().equals(id)).
					filter(f -> f.getName().equalsIgnoreCase(newName)).
					findFirst().orElse(null);
		}
		if(existing != null)
			return "Data set \"" + newName + "\" already exists.";
		else
			return null;
	}

	private String validateNameAgainstProject(String newName) {
		
		Collection<MSMSClusterDataSet> dataSets = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisProject().getMsmsClusterDataSets();
		MSMSClusterDataSet existing = null;

		if(this.dataSet == null) {
			existing = dataSets.stream().
					filter(s -> s.getName().equalsIgnoreCase(newName)).
					findFirst().orElse(null);
		}
		else {
			String id = dataSet.getId();
			existing = dataSets.stream().
					filter(s -> !s.getId().equals(id)).
					filter(s -> s.getName().equalsIgnoreCase(newName)).
					findFirst().orElse(null);
		}
		if(existing != null)
			return "Data set \"" + newName + "\" already exists.";
		else
			return null;
	}

	public MSMSClusterDataSet getMSMSClusterDataSet() {
		return dataSet;
	}

	public String getMSMSClusterDataSetName() {
		return nameTextField.getText().trim();
	}

	public String getMSMSClusterDataSetDescription() {
		return descriptionTextArea.getText().trim();
	}

	public Collection<MsFeatureInfoBundleCluster>getClustersToAdd() {
		return clustersToAdd;
	}
	
	public boolean loadMSMSClusterDataSetIntoWorkBench() {
		return loadMSMSClusterDataSetCheckBox.isSelected();
	}

	public MSMSClusteringParameterSet getMsmsExtractionParameters() {
		return msmsExtractionParameters;
	}

	public void setMsmsExtractionParameters(MSMSClusteringParameterSet msmsExtractionParameters) {
		this.msmsExtractionParameters = msmsExtractionParameters;
	}
}











