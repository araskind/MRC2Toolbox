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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.instrument.InstrumentSelectionDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class ExperimentDetailsPanel extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2509734060186732096L;
	private JTextField experimentNameTextField;
	private JTextArea descriptionTextArea;
	private JTextField experimentLocationTextField;
	private JTextField instrumentTextField;
	private InstrumentSelectionDialog instrumentSelectionDialog;
	private LIMSInstrument instrument;
	
	public ExperimentDetailsPanel(ActionListener listener) {
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		experimentNameTextField = new JTextField();
		GridBagConstraints gbc_experimentNameTextField = new GridBagConstraints();
		gbc_experimentNameTextField.gridwidth = 2;
		gbc_experimentNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_experimentNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_experimentNameTextField.gridx = 0;
		gbc_experimentNameTextField.gridy = 1;
		add(experimentNameTextField, gbc_experimentNameTextField);
		experimentNameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Description");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new LineBorder(Color.LIGHT_GRAY));
		descriptionTextArea.setRows(2);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
		gbc_descriptionTextArea.gridwidth = 2;
		gbc_descriptionTextArea.insets = new Insets(0, 0, 5, 0);
		gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_descriptionTextArea.gridx = 0;
		gbc_descriptionTextArea.gridy = 3;
		add(descriptionTextArea, gbc_descriptionTextArea);
		
		JLabel lblNewLabel_2 = new JLabel("Instrument");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 4;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		instrumentTextField = new JTextField();
		instrumentTextField.setEditable(false);
		GridBagConstraints gbc_instrumentTextField = new GridBagConstraints();
		gbc_instrumentTextField.insets = new Insets(0, 0, 5, 5);
		gbc_instrumentTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_instrumentTextField.gridx = 0;
		gbc_instrumentTextField.gridy = 5;
		add(instrumentTextField, gbc_instrumentTextField);
		instrumentTextField.setColumns(10);
		
		JButton selectInstrumentButton = new JButton("Select");
		selectInstrumentButton.setActionCommand(MainActionCommands.SELECT_INSTRUMENT_DIALOG_COMMAND.getName());		
		selectInstrumentButton.addActionListener(this);
		GridBagConstraints gbc_selectInstrumentButton = new GridBagConstraints();
		gbc_selectInstrumentButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectInstrumentButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectInstrumentButton.gridx = 1;
		gbc_selectInstrumentButton.gridy = 5;
		add(selectInstrumentButton, gbc_selectInstrumentButton);
		
		JLabel locLabel = new JLabel("Location");
		GridBagConstraints gbc_locLabel = new GridBagConstraints();
		gbc_locLabel.gridwidth = 2;
		gbc_locLabel.insets = new Insets(0, 0, 5, 0);
		gbc_locLabel.anchor = GridBagConstraints.WEST;
		gbc_locLabel.gridx = 0;
		gbc_locLabel.gridy = 6;
		add(locLabel, gbc_locLabel);
		
		experimentLocationTextField = new JTextField();
		GridBagConstraints gbc_experimentLocationTextField = new GridBagConstraints();
		gbc_experimentLocationTextField.insets = new Insets(0, 0, 0, 5);
		gbc_experimentLocationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_experimentLocationTextField.gridx = 0;
		gbc_experimentLocationTextField.gridy = 7;
		add(experimentLocationTextField, gbc_experimentLocationTextField);
		experimentLocationTextField.setColumns(10);
		
		JButton btnNewButton = new JButton("Browse ...");
		btnNewButton.setActionCommand(
				MainActionCommands.SELECT_EXPERIMENT_LOCATION_COMMAND.getName());
		btnNewButton.addActionListener(listener);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 7;
		add(btnNewButton, gbc_btnNewButton);
	}
	
	public String getExperimentName() {
		return experimentNameTextField.getText().trim();
	}

	public String getExperimentDescription() {
		return descriptionTextArea.getText().trim();
	}
	
	public void setExperimentLocation(File location) {
		experimentLocationTextField.setText(location.getAbsolutePath());
	}
	
	public String getExperimentLocationPath() {
		return experimentLocationTextField.getText().trim();
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
}











