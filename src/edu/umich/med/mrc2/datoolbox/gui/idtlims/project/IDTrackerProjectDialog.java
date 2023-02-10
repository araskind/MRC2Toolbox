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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class IDTrackerProjectDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 8046770417831235265L;
	private static final Icon newCdpIdProjectIcon = GuiUtils.getIcon("newIdProject", 32);
	private static final Icon editCdpIdProjectIcon = GuiUtils.getIcon("edit", 32);

	private LIMSProject project;
	private JButton saveButton;
	private JLabel projectIdValueLabel;
	private JLabel startDateLabel;
	private JTextArea descriptionTextArea;
	private JTextArea notesTextArea;
	private JLabel clientDataLabel;
	private JTextField projectNameTextField;

	public IDTrackerProjectDialog(LIMSProject project, ActionListener actionListener) {

		super();

		setPreferredSize(new Dimension(400, 400));
		setSize(new Dimension(400, 400));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.project = project;

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_dataPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_dataPanel.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_dataPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblExperimentId = new JLabel("Project ID");
		lblExperimentId.setForeground(Color.BLUE);
		lblExperimentId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExperimentId = new GridBagConstraints();
		gbc_lblExperimentId.anchor = GridBagConstraints.EAST;
		gbc_lblExperimentId.insets = new Insets(0, 0, 5, 5);
		gbc_lblExperimentId.gridx = 0;
		gbc_lblExperimentId.gridy = 0;
		dataPanel.add(lblExperimentId, gbc_lblExperimentId);

		projectIdValueLabel = new JLabel("");
		projectIdValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_expIdValueLabel = new GridBagConstraints();
		gbc_expIdValueLabel.anchor = GridBagConstraints.WEST;
		gbc_expIdValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_expIdValueLabel.gridx = 1;
		gbc_expIdValueLabel.gridy = 0;
		dataPanel.add(projectIdValueLabel, gbc_expIdValueLabel);

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

		JLabel lblProject = new JLabel("Name");
		lblProject.setForeground(Color.BLUE);
		lblProject.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblProject = new GridBagConstraints();
		gbc_lblProject.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblProject.insets = new Insets(0, 0, 5, 5);
		gbc_lblProject.gridx = 0;
		gbc_lblProject.gridy = 1;
		dataPanel.add(lblProject, gbc_lblProject);

		projectNameTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 3;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 1;
		dataPanel.add(projectNameTextField, gbc_textField);
		projectNameTextField.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		lblDescription.setForeground(Color.BLUE);
		lblDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		dataPanel.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setMaximumSize(new Dimension(600, 400));
		descriptionTextArea.setRows(10);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);

		JScrollPane scrollPane = new JScrollPane(descriptionTextArea);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 3;
		dataPanel.add(scrollPane, gbc_scrollPane);

		JLabel lblNotes = new JLabel("Notes");
		lblNotes.setForeground(Color.BLUE);
		lblNotes.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNotes.insets = new Insets(0, 0, 5, 5);
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 4;
		dataPanel.add(lblNotes, gbc_lblNotes);

		notesTextArea = new JTextArea();
		notesTextArea.setMaximumSize(new Dimension(600, 400));
		notesTextArea.setRows(10);
		notesTextArea.setWrapStyleWord(true);
		notesTextArea.setLineWrap(true);

		JScrollPane scrollPane_1 = new JScrollPane(notesTextArea);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 4;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 5;
		dataPanel.add(scrollPane_1, gbc_scrollPane_1);

		JLabel lblClient = new JLabel("Laboratory");
		lblClient.setForeground(Color.BLUE);
		lblClient.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblClient = new GridBagConstraints();
		gbc_lblClient.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblClient.insets = new Insets(0, 0, 0, 5);
		gbc_lblClient.gridx = 0;
		gbc_lblClient.gridy = 6;
		dataPanel.add(lblClient, gbc_lblClient);

		clientDataLabel = new JLabel("");
		GridBagConstraints gbc_clientDataLabel = new GridBagConstraints();
		gbc_clientDataLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_clientDataLabel.gridwidth = 3;
		gbc_clientDataLabel.gridx = 1;
		gbc_clientDataLabel.gridy = 6;
		dataPanel.add(clientDataLabel, gbc_clientDataLabel);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);

		saveButton = new JButton("Save project details");
		saveButton.addActionListener(actionListener);
		panel_1.add(saveButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);

		loadProjectData();
	}

	private void loadProjectData() {

		if(project == null) {
			setTitle("Create new ID tracker project");
			saveButton.setActionCommand(MainActionCommands.NEW_IDTRACKER_PROJECT_COMMAND.getName());
			setIconImage(((ImageIcon) newCdpIdProjectIcon).getImage());
		}
		else {
			setTitle("Edit project \"" + project.getName() + "\"");
			setIconImage(((ImageIcon) editCdpIdProjectIcon).getImage());
			saveButton.setActionCommand(MainActionCommands.SAVE_IDTRACKER_PROJECT_COMMAND.getName());

			projectIdValueLabel.setText(project.getId());
			if (project.getStartDate() != null)
				startDateLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(project.getStartDate()));

			projectNameTextField.setText(project.getName());
			descriptionTextArea.setText(project.getDescription());
			notesTextArea.setText(project.getNotes());
			clientDataLabel.setText(project.getOrganization().getOrganizationInfo());
		}
		pack();
	}

	public LIMSProject getProject() {
		return project;
	}

	public String getProjectName() {
		return projectNameTextField.getText().trim();
	}

	public String getProjectDescription() {
		return descriptionTextArea.getText().trim();
	}

	public String getProjectNotes() {
		return notesTextArea.getText().trim();
	}
}





















