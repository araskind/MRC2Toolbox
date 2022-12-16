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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.experiment;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.instrument.InstrumentSelectionDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class IDTExperimentDefinitionPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4115441450930108518L;
	
	private LIMSExperiment experiment;
	private LIMSInstrument instrument;
	private JLabel expIdValueLabel;
	private JLabel startDateLabel;
	private JTextArea descriptionTextArea;
	private JTextArea notesTextArea;
	private JLabel organizationDataLabel;
	private JLabel contactDataLabel;
	private JTextField experimentNameTextField;
	private JComboBox projectComboBox;
	private JTextField instrumentTextField;	
	private InstrumentSelectionDialog instrumentSelectionDialog;

	@SuppressWarnings("unchecked")
	public IDTExperimentDefinitionPanel(LIMSExperiment experiment) {
		super();

		this.experiment = experiment;
		
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_dataPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_dataPanel.columnWeights = new double[] { 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_dataPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0,
				Double.MIN_VALUE };
		setLayout(gbl_dataPanel);

		JLabel lblExperimentId = new JLabel("Experiment ID");
		lblExperimentId.setForeground(Color.BLUE);
		lblExperimentId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExperimentId = new GridBagConstraints();
		gbc_lblExperimentId.anchor = GridBagConstraints.EAST;
		gbc_lblExperimentId.insets = new Insets(0, 0, 5, 5);
		gbc_lblExperimentId.gridx = 0;
		gbc_lblExperimentId.gridy = 0;
		add(lblExperimentId, gbc_lblExperimentId);

		expIdValueLabel = new JLabel("");
		expIdValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_expIdValueLabel = new GridBagConstraints();
		gbc_expIdValueLabel.anchor = GridBagConstraints.WEST;
		gbc_expIdValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_expIdValueLabel.gridx = 1;
		gbc_expIdValueLabel.gridy = 0;
		add(expIdValueLabel, gbc_expIdValueLabel);

		JLabel lblStartDate = new JLabel("Start date");
		lblStartDate.setForeground(Color.BLUE);
		lblStartDate.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblStartDate = new GridBagConstraints();
		gbc_lblStartDate.anchor = GridBagConstraints.EAST;
		gbc_lblStartDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblStartDate.gridx = 2;
		gbc_lblStartDate.gridy = 0;
		add(lblStartDate, gbc_lblStartDate);

		startDateLabel = new JLabel("");
		startDateLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_startDateLabel = new GridBagConstraints();
		gbc_startDateLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_startDateLabel.insets = new Insets(0, 0, 5, 0);
		gbc_startDateLabel.gridx = 3;
		gbc_startDateLabel.gridy = 0;
		add(startDateLabel, gbc_startDateLabel);

		experimentNameTextField = new JTextField();
		GridBagConstraints gbc_experimentNameTextField = new GridBagConstraints();
		gbc_experimentNameTextField.gridwidth = 3;
		gbc_experimentNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_experimentNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_experimentNameTextField.gridx = 1;
		gbc_experimentNameTextField.gridy = 1;
		add(experimentNameTextField, gbc_experimentNameTextField);
		experimentNameTextField.setColumns(10);

		JLabel lblProject = new JLabel("Project");
		lblProject.setForeground(Color.BLUE);
		lblProject.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblProject = new GridBagConstraints();
		gbc_lblProject.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblProject.insets = new Insets(0, 0, 5, 5);
		gbc_lblProject.gridx = 0;
		gbc_lblProject.gridy = 2;
		add(lblProject, gbc_lblProject);

		JLabel lblName = new JLabel("Experiment name");
		lblName.setForeground(Color.BLUE);
		lblName.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		add(lblName, gbc_lblName);

		projectComboBox = new JComboBox(
				new SortedComboBoxModel<LIMSProject>(IDTDataCash.getProjects()));
		GridBagConstraints gbc_projectComboBox = new GridBagConstraints();
		gbc_projectComboBox.gridwidth = 3;
		gbc_projectComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_projectComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_projectComboBox.gridx = 1;
		gbc_projectComboBox.gridy = 2;
		add(projectComboBox, gbc_projectComboBox);
		
		JLabel lblInstrument = new JLabel("Instrument");
		lblInstrument.setForeground(Color.BLUE);
		lblInstrument.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblInstrument = new GridBagConstraints();
		gbc_lblInstrument.anchor = GridBagConstraints.WEST;
		gbc_lblInstrument.insets = new Insets(0, 0, 5, 5);
		gbc_lblInstrument.gridx = 0;
		gbc_lblInstrument.gridy = 3;
		add(lblInstrument, gbc_lblInstrument);
		
		JButton instrumentSelectButton = new JButton("Select instrument");
		instrumentSelectButton.setActionCommand(MainActionCommands.SELECT_INSTRUMENT_DIALOG_COMMAND.getName());
		instrumentSelectButton.addActionListener(this);
		
		instrumentTextField = new JTextField();
		instrumentTextField.setEditable(false);
		GridBagConstraints gbc_instrumentTextField = new GridBagConstraints();
		gbc_instrumentTextField.gridwidth = 3;
		gbc_instrumentTextField.insets = new Insets(0, 0, 5, 5);
		gbc_instrumentTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_instrumentTextField.gridx = 0;
		gbc_instrumentTextField.gridy = 4;
		add(instrumentTextField, gbc_instrumentTextField);
		instrumentTextField.setColumns(10);
		GridBagConstraints gbc_instrumentSelectButton = new GridBagConstraints();
		gbc_instrumentSelectButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_instrumentSelectButton.insets = new Insets(0, 0, 5, 0);
		gbc_instrumentSelectButton.gridx = 3;
		gbc_instrumentSelectButton.gridy = 4;
		add(instrumentSelectButton, gbc_instrumentSelectButton);

		JLabel lblDescription = new JLabel("Description");
		lblDescription.setForeground(Color.BLUE);
		lblDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 5;
		add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		//descriptionTextArea.setMaximumSize(new Dimension(600, 400));
		descriptionTextArea.setRows(10);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);

		JScrollPane scrollPane = new JScrollPane(descriptionTextArea);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 6;
		add(scrollPane, gbc_scrollPane);

		JLabel lblNotes = new JLabel("Notes");
		lblNotes.setForeground(Color.BLUE);
		lblNotes.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNotes.insets = new Insets(0, 0, 5, 5);
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 7;
		add(lblNotes, gbc_lblNotes);

		notesTextArea = new JTextArea();
		//notesTextArea.setMaximumSize(new Dimension(600, 400));
		notesTextArea.setRows(10);
		notesTextArea.setWrapStyleWord(true);
		notesTextArea.setLineWrap(true);

		JScrollPane scrollPane_1 = new JScrollPane(notesTextArea);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 4;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 8;
		add(scrollPane_1, gbc_scrollPane_1);

		JLabel lblClient = new JLabel("Organization");
		lblClient.setForeground(Color.BLUE);
		lblClient.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblClient = new GridBagConstraints();
		gbc_lblClient.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblClient.insets = new Insets(0, 0, 5, 5);
		gbc_lblClient.gridx = 0;
		gbc_lblClient.gridy = 9;
		add(lblClient, gbc_lblClient);

		organizationDataLabel = new JLabel("");
		GridBagConstraints gbc_clientDataLabel = new GridBagConstraints();
		gbc_clientDataLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_clientDataLabel.gridwidth = 3;
		gbc_clientDataLabel.insets = new Insets(0, 0, 5, 0);
		gbc_clientDataLabel.gridx = 1;
		gbc_clientDataLabel.gridy = 9;
		add(organizationDataLabel, gbc_clientDataLabel);

		JLabel lblContactPerson = new JLabel("Contact person");
		lblContactPerson.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblContactPerson.setForeground(Color.BLUE);
		GridBagConstraints gbc_lblContactPerson = new GridBagConstraints();
		gbc_lblContactPerson.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblContactPerson.insets = new Insets(0, 0, 0, 5);
		gbc_lblContactPerson.gridx = 0;
		gbc_lblContactPerson.gridy = 10;
		add(lblContactPerson, gbc_lblContactPerson);

		contactDataLabel = new JLabel("");
		GridBagConstraints gbc_contactDataLabel = new GridBagConstraints();
		gbc_contactDataLabel.gridwidth = 3;
		gbc_contactDataLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_contactDataLabel.gridx = 1;
		gbc_contactDataLabel.gridy = 10;
		add(contactDataLabel, gbc_contactDataLabel);
		
		loadExperimentData();
	}
	
	public void loadExperimentData() {

		if(experiment == null) 
			return;

		expIdValueLabel.setText(experiment.getId());
		if (experiment.getStartDate() != null)
			startDateLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(experiment.getStartDate()));

		experimentNameTextField.setText(experiment.getName());
		projectComboBox.setSelectedItem(experiment.getProject());
		descriptionTextArea.setText(experiment.getDescription());
		notesTextArea.setText(experiment.getNotes());
		
		instrument = IDTDataCash.getInstrumentForExperiment(experiment);
		if(instrument != null)
			instrumentTextField.setText(instrument.toString());
		
		LIMSOrganization organization = null;
		if(experiment.getProject() != null && experiment.getProject().getOrganization() != null)
			organization = experiment.getProject().getOrganization();
		
		if(organization == null && experiment.getCreator() != null)
			organization = IDTDataCash.getOrganizationForUser(experiment.getCreator());
				
		if(organization != null)
			organizationDataLabel.setText(organization.getOrganizationInfo());
		
		if(experiment.getCreator() != null)
			contactDataLabel.setText(experiment.getCreator().getInfo());	
	}
	
	public void loadExperiment(LIMSExperiment newExperiment) {
		
		experiment = newExperiment;
		loadExperimentData();
	}
	
	public void setProject(LIMSProject project) {
		projectComboBox.setSelectedItem(project);
	}

	public LIMSExperiment getExperiment() {
		return experiment;
	}

	public LIMSProject getExperimentProject() {
		return (LIMSProject) projectComboBox.getSelectedItem();
	}

	public String getExperimentName() {
		return experimentNameTextField.getText().trim();
	}

	public String getExperimentDescription() {
		return descriptionTextArea.getText().trim();
	}

	public String getExperimentNotes() {
		return notesTextArea.getText().trim();
	}
	
	public Collection<String>validateExperimentDefinition(){
		
		Collection<String> errors = new ArrayList<String>();
		if (getExperimentName().isEmpty())
			errors.add("Experiment name can not be empty.");

		if (getExperimentProject() == null)
			errors.add("Experiment parent project should be specified.");
		
		if (getInstrument() == null)
			errors.add("Instrument should be specified.");
		
		return errors;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.SELECT_INSTRUMENT_DIALOG_COMMAND.getName())) {
			instrumentSelectionDialog = new InstrumentSelectionDialog(this);
			instrumentSelectionDialog.setLocationRelativeTo(this);
			instrumentSelectionDialog.setVisible(true);
		}
		if(e.getActionCommand().equals(MainActionCommands.SELECT_INSTRUMENT_COMMAND.getName())) {
			
			instrument = instrumentSelectionDialog.getSelectedInstrument();
			if(instrument != null) {
				instrumentTextField.setText(instrument.toString() + "; " + 
						instrument.getManufacturer() + " " + instrument.getModel());
				instrumentSelectionDialog.dispose();
			}
		}
	}

	public LIMSInstrument getInstrument() {
		return instrument;
	}

	public void setInstrument(LIMSInstrument instrument) {
		this.instrument = instrument;
		if(instrument != null)
			instrumentTextField.setText(instrument.toString() + "; " + 
					instrument.getManufacturer() + " " + instrument.getModel());
	}
	
	public void clearPanel() {
		
		experiment = null;
		instrument = null;
		expIdValueLabel.setText("");
		startDateLabel.setText("");
		descriptionTextArea.setText("");
		notesTextArea.setText("");
		organizationDataLabel.setText("");
		contactDataLabel.setText("");
		experimentNameTextField.setText("");
		projectComboBox.setSelectedIndex(-1);
		instrumentTextField.setText("");
	}
}




