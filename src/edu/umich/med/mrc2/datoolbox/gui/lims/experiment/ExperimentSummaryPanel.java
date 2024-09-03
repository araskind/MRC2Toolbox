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

package edu.umich.med.mrc2.datoolbox.gui.lims.experiment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.TextAreaLabel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ExperimentSummaryPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LIMSExperiment experiment;

	private TextAreaLabel expNameLabel;
	private JLabel expIdValueLabel;
	private JLabel startDateLabel;
	private JTextArea descriptionTextArea;
	private JTextArea notesTextArea;
	private JButton editButton;
	private JButton saveButton;
	private JButton btnCancel;
	private TextAreaLabel projectNameLabel;
	private JLabel clientDataLabel;
	private JLabel contactPersonDataLabel;
	private JLabel piDataLabel;

	private static final String EDIT_COMMAND = "Unlock data to edit";
	private static final String CANCEL_EDIT_COMMAND = "Cancel data editing";

	public ExperimentSummaryPanel(ActionListener aListener) {

		super();
		setLayout(new BorderLayout(0, 0));

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_dataPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_dataPanel.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_dataPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblExperimentId = new JLabel("Experiment ID");
		lblExperimentId.setForeground(Color.BLUE);
		lblExperimentId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExperimentId = new GridBagConstraints();
		gbc_lblExperimentId.anchor = GridBagConstraints.EAST;
		gbc_lblExperimentId.insets = new Insets(0, 0, 5, 5);
		gbc_lblExperimentId.gridx = 0;
		gbc_lblExperimentId.gridy = 0;
		dataPanel.add(lblExperimentId, gbc_lblExperimentId);

		expIdValueLabel = new JLabel("");
		expIdValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_expIdValueLabel = new GridBagConstraints();
		gbc_expIdValueLabel.anchor = GridBagConstraints.WEST;
		gbc_expIdValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_expIdValueLabel.gridx = 1;
		gbc_expIdValueLabel.gridy = 0;
		dataPanel.add(expIdValueLabel, gbc_expIdValueLabel);

		JLabel lblStartDate = new JLabel("Start date");
		lblStartDate.setForeground(Color.BLUE);
		lblStartDate.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblStartDate = new GridBagConstraints();
		gbc_lblStartDate.anchor = GridBagConstraints.EAST;
		gbc_lblStartDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblStartDate.gridx = 2;
		gbc_lblStartDate.gridy = 0;
		dataPanel.add(lblStartDate, gbc_lblStartDate);

		startDateLabel = new JLabel("");
		startDateLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_startDateLabel = new GridBagConstraints();
		gbc_startDateLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_startDateLabel.insets = new Insets(0, 0, 5, 0);
		gbc_startDateLabel.gridx = 3;
		gbc_startDateLabel.gridy = 0;
		dataPanel.add(startDateLabel, gbc_startDateLabel);

		JLabel lblProject = new JLabel("Project");
		lblProject.setForeground(Color.BLUE);
		lblProject.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblProject = new GridBagConstraints();
		gbc_lblProject.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblProject.insets = new Insets(0, 0, 5, 5);
		gbc_lblProject.gridx = 0;
		gbc_lblProject.gridy = 2;
		dataPanel.add(lblProject, gbc_lblProject);

		projectNameLabel = new TextAreaLabel();
		projectNameLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_projectNameLabel = new GridBagConstraints();
		gbc_projectNameLabel.fill = GridBagConstraints.BOTH;
		gbc_projectNameLabel.gridwidth = 3;
		gbc_projectNameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_projectNameLabel.gridx = 1;
		gbc_projectNameLabel.gridy = 2;
		dataPanel.add(projectNameLabel, gbc_projectNameLabel);

		JLabel lblName = new JLabel("Experiment name");
		lblName.setForeground(Color.BLUE);
		lblName.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);

		expNameLabel = new TextAreaLabel();
		expNameLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_expNameTextField = new GridBagConstraints();
		gbc_expNameTextField.gridwidth = 3;
		gbc_expNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_expNameTextField.fill = GridBagConstraints.BOTH;
		gbc_expNameTextField.gridx = 1;
		gbc_expNameTextField.gridy = 1;
		dataPanel.add(expNameLabel, gbc_expNameTextField);

		JLabel lblDescription = new JLabel("Description");
		lblDescription.setForeground(Color.BLUE);
		lblDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 3;
		dataPanel.add(lblDescription, gbc_lblDescription);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		dataPanel.add(scrollPane, gbc_scrollPane);

		descriptionTextArea = new JTextArea();
		scrollPane.setViewportView(descriptionTextArea);
		descriptionTextArea.setMaximumSize(new Dimension(600, 400));
		descriptionTextArea.setRows(10);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);

		JLabel lblNotes = new JLabel("Notes");
		lblNotes.setForeground(Color.BLUE);
		lblNotes.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNotes.insets = new Insets(0, 0, 5, 5);
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 5;
		dataPanel.add(lblNotes, gbc_lblNotes);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 4;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 6;
		dataPanel.add(scrollPane_1, gbc_scrollPane_1);

		notesTextArea = new JTextArea();
		scrollPane_1.setViewportView(notesTextArea);
		notesTextArea.setMaximumSize(new Dimension(600, 400));
		notesTextArea.setRows(10);
		notesTextArea.setWrapStyleWord(true);
		notesTextArea.setLineWrap(true);

		JLabel lblClient = new JLabel("Client");
		lblClient.setForeground(Color.BLUE);
		lblClient.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblClient = new GridBagConstraints();
		gbc_lblClient.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblClient.insets = new Insets(0, 0, 5, 5);
		gbc_lblClient.gridx = 0;
		gbc_lblClient.gridy = 7;
		dataPanel.add(lblClient, gbc_lblClient);

		clientDataLabel = new JLabel("");
		GridBagConstraints gbc_clientDataLabel = new GridBagConstraints();
		gbc_clientDataLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_clientDataLabel.gridwidth = 3;
		gbc_clientDataLabel.insets = new Insets(0, 0, 5, 0);
		gbc_clientDataLabel.gridx = 1;
		gbc_clientDataLabel.gridy = 7;
		dataPanel.add(clientDataLabel, gbc_clientDataLabel);

		JLabel lblPi = new JLabel("PI");
		lblPi.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPi.setForeground(Color.BLUE);
		GridBagConstraints gbc_lblPi = new GridBagConstraints();
		gbc_lblPi.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblPi.insets = new Insets(0, 0, 5, 5);
		gbc_lblPi.gridx = 0;
		gbc_lblPi.gridy = 8;
		dataPanel.add(lblPi, gbc_lblPi);

		piDataLabel = new JLabel("");
		GridBagConstraints gbc_piDataLabel = new GridBagConstraints();
		gbc_piDataLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_piDataLabel.insets = new Insets(0, 0, 5, 5);
		gbc_piDataLabel.gridx = 1;
		gbc_piDataLabel.gridy = 8;
		dataPanel.add(piDataLabel, gbc_piDataLabel);

		JLabel lblContact = new JLabel("Contact");
		lblContact.setForeground(Color.BLUE);
		lblContact.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblContact = new GridBagConstraints();
		gbc_lblContact.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblContact.insets = new Insets(0, 0, 5, 5);
		gbc_lblContact.gridx = 2;
		gbc_lblContact.gridy = 8;
		dataPanel.add(lblContact, gbc_lblContact);

		editButton = new JButton("Edit ...");
		editButton.setActionCommand(EDIT_COMMAND);
		editButton.addActionListener(this);

		contactPersonDataLabel = new JLabel("");
		GridBagConstraints gbc_contactPersonDataLabel = new GridBagConstraints();
		gbc_contactPersonDataLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_contactPersonDataLabel.insets = new Insets(0, 0, 5, 0);
		gbc_contactPersonDataLabel.gridx = 3;
		gbc_contactPersonDataLabel.gridy = 8;
		dataPanel.add(contactPersonDataLabel, gbc_contactPersonDataLabel);

		editButton.setToolTipText(EDIT_COMMAND);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 10;
		dataPanel.add(editButton, gbc_btnNewButton);

		btnCancel = new JButton("Cancel");
		btnCancel.setActionCommand(CANCEL_EDIT_COMMAND);
		btnCancel.addActionListener(this);
		btnCancel.setToolTipText(CANCEL_EDIT_COMMAND);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 10;
		dataPanel.add(btnCancel, gbc_btnCancel);

		saveButton = new JButton("Save changes");
		saveButton.setActionCommand(MainActionCommands.SAVE_EXPERIMENT_SUMMARY_COMMAND.getName());
		saveButton.addActionListener(aListener);
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.gridx = 3;
		gbc_btnNewButton_1.gridy = 10;
		dataPanel.add(saveButton, gbc_btnNewButton_1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals(EDIT_COMMAND))
			unlockDataForEditing();

		if (e.getActionCommand().equals(CANCEL_EDIT_COMMAND)) {

		}
	}

	private void unlockDataForEditing() {
		// TODO Auto-generated method stub

	}

	public void showExperimentSummary(LIMSExperiment newExperiment) {

		clearPanel();
		this.experiment = newExperiment;

		expIdValueLabel.setText(experiment.getId());
		expNameLabel.setText(experiment.getName());

		if (experiment.getStartDate() != null)
			startDateLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(experiment.getStartDate()));

		descriptionTextArea.setText(experiment.getDescription());
		descriptionTextArea.setCaretPosition(0);
		notesTextArea.setText(experiment.getNotes());
		notesTextArea.setCaretPosition(0);
		projectNameLabel.setText(experiment.getProject().getName());
		clientDataLabel.setText(experiment.getProject().getOrganization().getOrganizationInfo());

		if (experiment.getProject().getContactPerson() != null)
			contactPersonDataLabel.setText(experiment.getProject().getContactPerson().getInfo());
		else {
			if (experiment.getProject().getClient().getContactPerson() != null)
				contactPersonDataLabel.setText(experiment.getProject().getClient().getContactPerson().getInfo());
		}
		if (experiment.getProject().getClient().getPrincipalInvestigator() != null)
			piDataLabel.setText(experiment.getProject().getClient().getPrincipalInvestigator().getInfo());
	}

	public synchronized void clearPanel() {
	
		expIdValueLabel.setText("");
		expNameLabel.setText("");
		startDateLabel.setText("");
		descriptionTextArea.setText("");
		notesTextArea.setText("");
		projectNameLabel.setText("");
		clientDataLabel.setText("");
		contactPersonDataLabel.setText("");
		piDataLabel.setText("");
	}
}