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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.TextAreaLabel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableLIMSDataPanel extends DefaultSingleCDockable{

	private static final Icon componentIcon = GuiUtils.getIcon("database", 16);

	private TextAreaLabel projectNameValueLabel;
	private JLabel clientValueLabel;
	private JLabel piValueLabel;
	private JLabel contactValueLabel;
	private JTextArea experimentDescriptionTextArea;
	private TextAreaLabel expNameLabel;
	private JLabel startDateLabel;
	private JLabel expIdValueLabel;
	private JTextArea experimentNotesTextArea;
	private JTextArea projectDescriptionTextArea;

	public DockableLIMSDataPanel() {

		super("DockableLIMSDataPanel", componentIcon,"LIMS data",null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 71, 0, 0};
		gbl_dataPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel label = new JLabel("Experiment ID");
		label.setForeground(Color.BLUE);
		label.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		dataPanel.add(label, gbc_label);

		expIdValueLabel = new JLabel("");
		expIdValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_expIdValueLabel = new GridBagConstraints();
		gbc_expIdValueLabel.anchor = GridBagConstraints.WEST;
		gbc_expIdValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_expIdValueLabel.gridx = 1;
		gbc_expIdValueLabel.gridy = 0;
		dataPanel.add(expIdValueLabel, gbc_expIdValueLabel);

		JLabel label_1 = new JLabel("Start date");
		label_1.setForeground(Color.BLUE);
		label_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.anchor = GridBagConstraints.EAST;
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 2;
		gbc_label_1.gridy = 0;
		dataPanel.add(label_1, gbc_label_1);

		startDateLabel = new JLabel("");
		startDateLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_startDateLabel = new GridBagConstraints();
		gbc_startDateLabel.anchor = GridBagConstraints.WEST;
		gbc_startDateLabel.insets = new Insets(0, 0, 5, 0);
		gbc_startDateLabel.gridx = 3;
		gbc_startDateLabel.gridy = 0;
		dataPanel.add(startDateLabel, gbc_startDateLabel);

		JLabel label_2 = new JLabel("Experiment name");
		label_2.setForeground(Color.BLUE);
		label_2.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.anchor = GridBagConstraints.NORTHEAST;
		gbc_label_2.insets = new Insets(0, 0, 5, 5);
		gbc_label_2.gridx = 0;
		gbc_label_2.gridy = 1;
		dataPanel.add(label_2, gbc_label_2);

		expNameLabel = new TextAreaLabel();
		expNameLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_expNameLabel = new GridBagConstraints();
		gbc_expNameLabel.gridwidth = 3;
		gbc_expNameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_expNameLabel.fill = GridBagConstraints.BOTH;
		gbc_expNameLabel.gridx = 1;
		gbc_expNameLabel.gridy = 1;
		dataPanel.add(expNameLabel, gbc_expNameLabel);

		JLabel lblProjectName = new JLabel("Project name");
		lblProjectName.setForeground(Color.BLUE);
		lblProjectName.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblProjectName = new GridBagConstraints();
		gbc_lblProjectName.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblProjectName.insets = new Insets(0, 0, 5, 5);
		gbc_lblProjectName.gridx = 0;
		gbc_lblProjectName.gridy = 2;
		dataPanel.add(lblProjectName, gbc_lblProjectName);

		projectNameValueLabel = new TextAreaLabel();
		projectNameValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_projectNameValueLabel = new GridBagConstraints();
		gbc_projectNameValueLabel.gridwidth = 3;
		gbc_projectNameValueLabel.anchor = GridBagConstraints.NORTH;
		gbc_projectNameValueLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_projectNameValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_projectNameValueLabel.gridx = 1;
		gbc_projectNameValueLabel.gridy = 2;
		dataPanel.add(projectNameValueLabel, gbc_projectNameValueLabel);

		JLabel lblProjectDescription = new JLabel("Project description");
		lblProjectDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblProjectDescription.setForeground(Color.BLUE);
		GridBagConstraints gbc_lblProjectDescription = new GridBagConstraints();
		gbc_lblProjectDescription.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblProjectDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblProjectDescription.gridx = 0;
		gbc_lblProjectDescription.gridy = 3;
		dataPanel.add(lblProjectDescription, gbc_lblProjectDescription);

		projectDescriptionTextArea = new JTextArea();
		projectDescriptionTextArea.setLineWrap(true);
		projectDescriptionTextArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(projectDescriptionTextArea);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		dataPanel.add(scrollPane, gbc_scrollPane);

		JLabel lblExperimentDescription = new JLabel("Experiment description");
		lblExperimentDescription.setForeground(Color.BLUE);
		lblExperimentDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExperimentDescription = new GridBagConstraints();
		gbc_lblExperimentDescription.anchor = GridBagConstraints.WEST;
		gbc_lblExperimentDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblExperimentDescription.gridx = 0;
		gbc_lblExperimentDescription.gridy = 5;
		dataPanel.add(lblExperimentDescription, gbc_lblExperimentDescription);

		experimentDescriptionTextArea = new JTextArea();
		experimentDescriptionTextArea.setWrapStyleWord(true);
		experimentDescriptionTextArea.setLineWrap(true);
		JScrollPane scrollPane_1 = new JScrollPane(experimentDescriptionTextArea);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridwidth = 4;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 6;
		dataPanel.add(scrollPane_1, gbc_scrollPane_1);

		JLabel lblExperimentNotes = new JLabel("Experiment notes");
		lblExperimentNotes.setForeground(Color.BLUE);
		lblExperimentNotes.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExperimentNotes = new GridBagConstraints();
		gbc_lblExperimentNotes.anchor = GridBagConstraints.WEST;
		gbc_lblExperimentNotes.insets = new Insets(0, 0, 5, 5);
		gbc_lblExperimentNotes.gridx = 0;
		gbc_lblExperimentNotes.gridy = 7;
		dataPanel.add(lblExperimentNotes, gbc_lblExperimentNotes);

		experimentNotesTextArea = new JTextArea();
		experimentNotesTextArea.setLineWrap(true);
		experimentNotesTextArea.setWrapStyleWord(true);
		JScrollPane scrollPane_2 = new JScrollPane(experimentNotesTextArea);
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.gridwidth = 4;
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 8;
		dataPanel.add(scrollPane_2, gbc_scrollPane_2);

		JLabel lblClient = new JLabel("Client");
		lblClient.setForeground(Color.BLUE);
		lblClient.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblClient = new GridBagConstraints();
		gbc_lblClient.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblClient.insets = new Insets(0, 0, 5, 5);
		gbc_lblClient.gridx = 0;
		gbc_lblClient.gridy = 9;
		dataPanel.add(lblClient, gbc_lblClient);

		clientValueLabel = new JLabel("");
		GridBagConstraints gbc_clientValueLabel = new GridBagConstraints();
		gbc_clientValueLabel.gridwidth = 3;
		gbc_clientValueLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_clientValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_clientValueLabel.gridx = 1;
		gbc_clientValueLabel.gridy = 9;
		dataPanel.add(clientValueLabel, gbc_clientValueLabel);

		JLabel lblPi = new JLabel("PI");
		lblPi.setForeground(Color.BLUE);
		lblPi.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblPi = new GridBagConstraints();
		gbc_lblPi.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblPi.insets = new Insets(0, 0, 5, 5);
		gbc_lblPi.gridx = 0;
		gbc_lblPi.gridy = 10;
		dataPanel.add(lblPi, gbc_lblPi);

		piValueLabel = new JLabel("");
		GridBagConstraints gbc_piValueLabel = new GridBagConstraints();
		gbc_piValueLabel.gridwidth = 3;
		gbc_piValueLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_piValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_piValueLabel.gridx = 1;
		gbc_piValueLabel.gridy = 10;
		dataPanel.add(piValueLabel, gbc_piValueLabel);

		JLabel lblContactPerson = new JLabel("Contact person");
		lblContactPerson.setForeground(Color.BLUE);
		lblContactPerson.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblContactPerson = new GridBagConstraints();
		gbc_lblContactPerson.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblContactPerson.insets = new Insets(0, 0, 0, 5);
		gbc_lblContactPerson.gridx = 0;
		gbc_lblContactPerson.gridy = 11;
		dataPanel.add(lblContactPerson, gbc_lblContactPerson);

		contactValueLabel = new JLabel("");
		GridBagConstraints gbc_contactValueLabel = new GridBagConstraints();
		gbc_contactValueLabel.gridwidth = 3;
		gbc_contactValueLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_contactValueLabel.gridx = 1;
		gbc_contactValueLabel.gridy = 11;
		dataPanel.add(contactValueLabel, gbc_contactValueLabel);
	}

	public synchronized void clearPanel() {

		expIdValueLabel.setText("");
		startDateLabel.setText("");
		projectNameValueLabel.setText("");
		projectDescriptionTextArea.setText("");
		expNameLabel.setText("");
		experimentDescriptionTextArea.setText("");
		experimentNotesTextArea.setText("");
		clientValueLabel.setText("");
		piValueLabel.setText("");
		contactValueLabel.setText("");
	}

	public void setDataFromLimsObjects(LIMSProject project, LIMSExperiment experiment) {

		clearPanel();

		projectNameValueLabel.setText(project.getName());
		projectDescriptionTextArea.setText(project.getDescription());
		clientValueLabel.setText(project.getOrganization().getOrganizationInfo());

		if (project.getContactPerson() != null)
			contactValueLabel.setText(project.getContactPerson().getInfo());
		else {
			if (project.getClient().getContactPerson() != null)
				contactValueLabel.setText(project.getClient().getContactPerson().getInfo());
		}
		if (project.getClient().getPrincipalInvestigator() != null)
			piValueLabel.setText(project.getClient().getPrincipalInvestigator().getInfo());

		if(experiment != null) {
			expIdValueLabel.setText(experiment.getId());

			if (experiment.getStartDate() != null)
				startDateLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(experiment.getStartDate()));

			expNameLabel.setText(experiment.getName());
			experimentDescriptionTextArea.setText(experiment.getDescription());
			experimentNotesTextArea.setText(experiment.getNotes());
		}
	}
}







































