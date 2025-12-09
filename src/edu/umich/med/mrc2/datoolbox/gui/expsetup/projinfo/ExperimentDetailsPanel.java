/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.projinfo;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.expsetup.dpl.DataPipelineDefinitionDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.StatusBar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.BuildInformation;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;

public class ExperimentDetailsPanel extends DockableMRC2ToolboxPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("project", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "ProjectDetailsPanel.layout");
	
	private DataPipelinesTable dataPipelinesTable;
	private JLabel lastModifiedValueLabel;
	private JLabel createdOnValueLabel;
	private JButton deleteAssayButtonButton;
	private JButton changeAssayButton;
	private DataPipelineDefinitionDialog dataPipelineDefinitionDialog;
	private JButton nameEditButton;
	private JButton descEditButton;
	private JTextArea nameTextArea;
	private JTextArea descriptionTextArea;
	private JLabel acqMethodNameLabel;
	private JLabel daMethodNameLabel;

	public ExperimentDetailsPanel() {

		super("ProjectDetailsPanel", "Project details", componentIcon);
		setCloseable(false);
		initPanelGui();
		initActions();
		populatePanelsMenu();
	}

	private void initPanelGui() {

		JPanel contents = new JPanel();
		contents.setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 };
		gridBagLayout.columnWidths = new int[] { 0, 78, 0, 0, 70, 0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		contents.setLayout(gridBagLayout);

		JLabel lblName = new JLabel("Name: ");
		lblName.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		contents.add(lblName, gbc_lblName);

		nameTextArea = new JTextArea();
		nameTextArea.setRows(2);
		nameTextArea.setLineWrap(true);
		nameTextArea.setWrapStyleWord(true);
		nameTextArea.setEditable(false);
		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
		gbc_nameTextField.gridwidth = 4;
		gbc_nameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_nameTextField.fill = GridBagConstraints.BOTH;
		gbc_nameTextField.gridx = 1;
		gbc_nameTextField.gridy = 0;
		contents.add(nameTextArea, gbc_nameTextField);

		nameEditButton = new JButton("Edit");
		nameEditButton.addActionListener(this);
		nameEditButton.setActionCommand(MainActionCommands.EDIT_EXPERIMENT_NAME_COMMAND.getName());
		GridBagConstraints gbc_nameEditButton = new GridBagConstraints();
		gbc_nameEditButton.anchor = GridBagConstraints.NORTH;
		gbc_nameEditButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameEditButton.insets = new Insets(0, 0, 5, 0);
		gbc_nameEditButton.gridx = 5;
		gbc_nameEditButton.gridy = 0;
		contents.add(nameEditButton, gbc_nameEditButton);

		JLabel lblDescription = new JLabel("Description: ");
		lblDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 1;
		contents.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setRows(3);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setEditable(false);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 6;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 2;
		contents.add(new JScrollPane(descriptionTextArea), gbc_scrollPane_1);

		descEditButton = new JButton("Edit");
		descEditButton.setActionCommand(MainActionCommands.EDIT_EXPERIMENT_DESCRIPTION_COMMAND.getName());
		descEditButton.addActionListener(this);
		descEditButton.setSize(new Dimension(50, 25));
		descEditButton.setPreferredSize(new Dimension(50, 25));
		GridBagConstraints gbc_descEditButton = new GridBagConstraints();
		gbc_descEditButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_descEditButton.anchor = GridBagConstraints.NORTH;
		gbc_descEditButton.insets = new Insets(0, 0, 5, 0);
		gbc_descEditButton.gridx = 5;
		gbc_descEditButton.gridy = 3;
		contents.add(descEditButton, gbc_descEditButton);

		JLabel lblCreatedOn = new JLabel("Created on: ");
		lblCreatedOn.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblCreatedOn = new GridBagConstraints();
		gbc_lblCreatedOn.gridwidth = 3;
		gbc_lblCreatedOn.anchor = GridBagConstraints.EAST;
		gbc_lblCreatedOn.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatedOn.gridx = 0;
		gbc_lblCreatedOn.gridy = 4;
		contents.add(lblCreatedOn, gbc_lblCreatedOn);

		createdOnValueLabel = new JLabel("");
		GridBagConstraints gbc_createdOnValueLabel = new GridBagConstraints();
		gbc_createdOnValueLabel.gridwidth = 2;
		gbc_createdOnValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_createdOnValueLabel.anchor = GridBagConstraints.WEST;
		gbc_createdOnValueLabel.gridx = 3;
		gbc_createdOnValueLabel.gridy = 4;
		contents.add(createdOnValueLabel, gbc_createdOnValueLabel);

		JLabel lblLastModified = new JLabel("Last modified: ");
		lblLastModified.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblLastModified = new GridBagConstraints();
		gbc_lblLastModified.gridwidth = 3;
		gbc_lblLastModified.anchor = GridBagConstraints.EAST;
		gbc_lblLastModified.insets = new Insets(0, 0, 5, 5);
		gbc_lblLastModified.gridx = 0;
		gbc_lblLastModified.gridy = 5;
		contents.add(lblLastModified, gbc_lblLastModified);

		lastModifiedValueLabel = new JLabel("");
		GridBagConstraints gbc_lastModifiedValueLabel = new GridBagConstraints();
		gbc_lastModifiedValueLabel.gridwidth = 2;
		gbc_lastModifiedValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lastModifiedValueLabel.anchor = GridBagConstraints.WEST;
		gbc_lastModifiedValueLabel.gridx = 3;
		gbc_lastModifiedValueLabel.gridy = 5;
		contents.add(lastModifiedValueLabel, gbc_lastModifiedValueLabel);

		
		dataPipelinesTable = new DataPipelinesTable();
		dataPipelinesTable.getSelectionModel().addListSelectionListener(this);
		dataPipelinesTable.addMouseListener(

		        new MouseAdapter(){

		          public void mouseClicked(MouseEvent e){

		            if (e.getClickCount() == 2)
		            	activateSelectedDataPipeline();
		          }
	        });
		dataPipelinesTable.addTablePopupMenu(new DataPipelineTablePopupMenu(this));	
		JScrollPane scrollPane = new JScrollPane(dataPipelinesTable);
//		scrollPane.add(dataPipelinesTable);
//		scrollPane.setViewportView(dataPipelinesTable);
//		scrollPane.setPreferredSize(dataPipelinesTable.getPreferredScrollableViewportSize());

		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 6;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 6;
		contents.add(scrollPane, gbc_scrollPane);

		changeAssayButton = new JButton(MainActionCommands.EDIT_DATA_PIPELINE_COMMAND.getName());
		changeAssayButton.setActionCommand(MainActionCommands.EDIT_DATA_PIPELINE_COMMAND.getName());
		changeAssayButton.addActionListener(this);
		GridBagConstraints gbc_changeAssayButton = new GridBagConstraints();
		gbc_changeAssayButton.gridwidth = 3;
		gbc_changeAssayButton.insets = new Insets(0, 0, 5, 5);
		gbc_changeAssayButton.gridx = 1;
		gbc_changeAssayButton.gridy = 7;
		contents.add(changeAssayButton, gbc_changeAssayButton);

		deleteAssayButtonButton = new JButton(MainActionCommands.DELETE_DATA_PIPELINE_COMMAND.getName());
		deleteAssayButtonButton.setActionCommand(MainActionCommands.DELETE_DATA_PIPELINE_COMMAND.getName());
		deleteAssayButtonButton.addActionListener(this);
		GridBagConstraints gbc_deleteAssayButtonButton = new GridBagConstraints();
		gbc_deleteAssayButtonButton.insets = new Insets(0, 0, 5, 0);
		gbc_deleteAssayButtonButton.gridwidth = 2;
		gbc_deleteAssayButtonButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_deleteAssayButtonButton.gridx = 4;
		gbc_deleteAssayButtonButton.gridy = 7;
		contents.add(deleteAssayButtonButton, gbc_deleteAssayButtonButton);

		JLabel label = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 8;
		contents.add(label, gbc_label);

		add(contents);
		
		JLabel lblNewLabel = new JLabel("Acquisition method: ");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 9;
		contents.add(lblNewLabel, gbc_lblNewLabel);
		
		acqMethodNameLabel = new JLabel("");
		GridBagConstraints gbc_acqMethodNameLabel = new GridBagConstraints();
		gbc_acqMethodNameLabel.anchor = GridBagConstraints.WEST;
		gbc_acqMethodNameLabel.gridwidth = 6;
		gbc_acqMethodNameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_acqMethodNameLabel.gridx = 0;
		gbc_acqMethodNameLabel.gridy = 10;
		contents.add(acqMethodNameLabel, gbc_acqMethodNameLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Data analysis method: ");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 11;
		contents.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		daMethodNameLabel = new JLabel("");
		GridBagConstraints gbc_daMethodNameLabel = new GridBagConstraints();
		gbc_daMethodNameLabel.insets = new Insets(0, 0, 0, 5);
		gbc_daMethodNameLabel.anchor = GridBagConstraints.WEST;
		gbc_daMethodNameLabel.gridwidth = 6;
		gbc_daMethodNameLabel.gridx = 0;
		gbc_daMethodNameLabel.gridy = 12;
		contents.add(daMethodNameLabel, gbc_daMethodNameLabel);
	}
	
	private void activateSelectedDataPipeline() {
		
		DataPipeline pl = dataPipelinesTable.getSelectedDataPipeline();
		if(pl == null || currentExperiment == null)
			return;
		
		if(activeDataPipeline != null && activeDataPipeline.equals(pl))
			return;
		
		MRC2ToolBoxCore.getMainWindow().switchDataPipeline(currentExperiment, pl);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		super.actionPerformed(e);
		
		if (currentExperiment == null)
			return;

		String command = e.getActionCommand();
		
		if (command.equals(MainActionCommands.ACTIVATE_DATA_PIPELINE_COMMAND.getName()))
			activateSelectedDataPipeline();
		
		if (command.equals(MainActionCommands.DELETE_DATA_PIPELINE_COMMAND.getName()))
			deleteDataPipeline();

		if (command.equals(MainActionCommands.EDIT_DATA_PIPELINE_COMMAND.getName()))
			showAsayTypeDialog();

		if (command.equals(MainActionCommands.SAVE_DATA_PIPELINE_COMMAND.getName()))
			changeDataPipelineType();

		if (command.equals(MainActionCommands.EDIT_EXPERIMENT_NAME_COMMAND.getName()))
			editProjectName();

		if (command.equals(MainActionCommands.EDIT_EXPERIMENT_DESCRIPTION_COMMAND.getName()))
			editProjectDescription();

		if (command.equals(MainActionCommands.SAVE_NEW_EXPERIMENT_NAME_COMMAND.getName()))
			saveProjectName();

		if (command.equals(MainActionCommands.SAVE_NEW_EXPERIMENT_DESCRIPTION_COMMAND.getName()))
			saveProjectDescription();		
	}

	private void editProjectName() {

		nameTextArea.setEditable(true);
		nameEditButton.setText("Save");
		nameEditButton.setActionCommand(MainActionCommands.SAVE_NEW_EXPERIMENT_NAME_COMMAND.getName());
	}

	private void saveProjectName() {

		currentExperiment.setName(nameTextArea.getText());
		MRC2ToolBoxCore.getMainWindow().setTitle(BuildInformation.getProgramName() + " - " + currentExperiment.getName());
		StatusBar.setExperimentName(currentExperiment.getName());
		nameTextArea.setEditable(false);
		nameEditButton.setText("Edit");
		nameEditButton.setActionCommand(MainActionCommands.EDIT_EXPERIMENT_NAME_COMMAND.getName());
	}

	private void editProjectDescription() {

		descriptionTextArea.setEditable(true);
		descEditButton.setText("Save");
		descEditButton.setActionCommand(MainActionCommands.SAVE_NEW_EXPERIMENT_DESCRIPTION_COMMAND.getName());
	}

	private void saveProjectDescription() {

		currentExperiment.setDescription(descriptionTextArea.getText());
		descriptionTextArea.setEditable(false);
		descEditButton.setText("Edit");
		descEditButton.setActionCommand(MainActionCommands.EDIT_EXPERIMENT_DESCRIPTION_COMMAND.getName());
	}

	private void changeDataPipelineType() {
		
		DataPipeline selectedPipeline = dataPipelinesTable.getSelectedDataPipeline();
		DataPipeline modifiedPipeline = dataPipelineDefinitionDialog.getDataPipeline();
		
		Set<DataPipeline> otherPipelines = currentExperiment.getDataPipelines().stream().
				filter(p -> !p.equals(selectedPipeline)).
				collect(Collectors.toSet());
		
		if(!otherPipelines.isEmpty()) {
			
			DataPipeline conflictingPipeline = otherPipelines.stream().
				filter(p -> p.getAcquisitionMethod().equals(modifiedPipeline.getAcquisitionMethod())).
				filter(p -> p.getDataExtractionMethod().equals(modifiedPipeline.getDataExtractionMethod())).
				findFirst().orElse(null);
			if(conflictingPipeline != null) {
				MessageDialog.showErrorMsg(
						"Current project contains a different data pipeline \"" + conflictingPipeline.getName() + "\"\n" + 
						"with acquisition method \"" + conflictingPipeline.getAcquisitionMethod().getName() + "\"\n" + 
						"and data extraction method \"" + conflictingPipeline.getDataExtractionMethod().getName() + "\"", 
				this.getContentPane());
				return;
			}
		}
		selectedPipeline.setName(modifiedPipeline.getName());
		selectedPipeline.setDescription(modifiedPipeline.getDescription());
		selectedPipeline.setAcquisitionMethod(modifiedPipeline.getAcquisitionMethod());
		selectedPipeline.setDataExtractionMethod(modifiedPipeline.getDataExtractionMethod());
		selectedPipeline.setMotrpacAssay(modifiedPipeline.getMotrpacAssay());
		selectedPipeline.setAssay(modifiedPipeline.getAssay());
		dataPipelinesTable.setTableModelFromProject(currentExperiment);
		dataPipelineDefinitionDialog.dispose();
	}

	private void showAsayTypeDialog() {

		DataPipeline selectedPipeline = dataPipelinesTable.getSelectedDataPipeline();
		if(selectedPipeline == null)
			return;
		
		dataPipelineDefinitionDialog = new DataPipelineDefinitionDialog(this, selectedPipeline);
		dataPipelineDefinitionDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		dataPipelineDefinitionDialog.setVisible(true);	
	}

	private void deleteDataPipeline() {

		DataPipeline selectedPipeline = dataPipelinesTable.getSelectedDataPipeline();
		if(selectedPipeline == null)
			return;

		String yesNoQuestion = 
				"You are going to delete \"" + selectedPipeline.getName()
				+ "\" data pipeline from current project.\n"
				+ "This operation is not reversible!\n"
				+ "Proceed?";

		if (MessageDialog.showChoiceWithWarningMsg(yesNoQuestion,
				this.getContentPane()) == JOptionPane.OK_OPTION) {

			RemoveDataPipelineTask task = new RemoveDataPipelineTask(selectedPipeline);
			IndeterminateProgressDialog idp = 
					new IndeterminateProgressDialog(
							"Removing data pipeline \"" + selectedPipeline.getName() + "\"", 
							MRC2ToolBoxCore.getMainWindow(), task);
			idp.setLocationRelativeTo(this.getContentPane());
			idp.setVisible(true);
			
//			currentExperiment.removeDataPipeline(selectedPipeline);
//			activeDataPipeline = currentExperiment.getActiveDataPipeline();
//			MRC2ToolBoxCore.getMainWindow().switchDataPipeline(currentExperiment, activeDataPipeline);
		}		
	}
	
	class RemoveDataPipelineTask extends LongUpdateTask {
		/*
			* Main task. Executed in background thread.
			*/
		private DataPipeline toRemove;

		public RemoveDataPipelineTask(DataPipeline toRemove) {
			this.toRemove = toRemove;
		}

		@Override
		public Void doInBackground() {

			try {
				currentExperiment.removeDataPipeline(toRemove);
				activeDataPipeline = currentExperiment.getActiveDataPipeline();
				MRC2ToolBoxCore.getMainWindow().switchDataPipeline(currentExperiment, activeDataPipeline);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		public void done() {
			super.done();
			MRC2ToolBoxCore.getMainWindow().saveExperimentAndContinue();
		}
	}

	public synchronized void clearPanel() {

		nameTextArea.setText("");
		descriptionTextArea.setText("");
		createdOnValueLabel.setText("");
		lastModifiedValueLabel.setText("");
		dataPipelinesTable.clearTable();		
		acqMethodNameLabel.setText("");
		daMethodNameLabel.setText("");
	}
	
	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		
		super.switchDataPipeline(project, newDataPipeline);
		clearPanel();
		if(currentExperiment == null)
			return;
		
		nameTextArea.setText(currentExperiment.getName());
		descriptionTextArea.setText(currentExperiment.getDescription());
		descriptionTextArea.setCaretPosition(0);

		String created = "";
		String modified = "";

		if (currentExperiment.getDateCreated() != null)
			created = MRC2ToolBoxConfiguration.getDateTimeFormat().
				format(currentExperiment.getDateCreated());

		createdOnValueLabel.setText(created);

		if (currentExperiment.getLastModified() != null)
			modified = MRC2ToolBoxConfiguration.getDateTimeFormat().
				format(currentExperiment.getLastModified());

		lastModifiedValueLabel.setText(modified);
		dataPipelinesTable.setTableModelFromProject(currentExperiment);
		dataPipelinesTable.selectPipeline(newDataPipeline);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {
			DataPipeline pl = dataPipelinesTable.getSelectedDataPipeline();
			if(pl == null)
				return;
			
			acqMethodNameLabel.setText(pl.getAcquisitionMethod().getName() + 
					" (" + pl.getAcquisitionMethod().getId() + ")");			
			daMethodNameLabel.setText(pl.getDataExtractionMethod().getName() + 
					" (" + pl.getDataExtractionMethod().getId() + ")");
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateGuiWithRecentData() {
		// TODO Auto-generated method stub
		
	}
}

















