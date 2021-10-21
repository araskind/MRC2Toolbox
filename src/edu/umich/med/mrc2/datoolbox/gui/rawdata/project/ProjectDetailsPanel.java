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

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class ProjectDetailsPanel extends JPanel  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2509734060186732096L;
	private JTextField projectNameTextField;
	private JTextArea descriptionTextArea;
	private JTextField projectLocationTextField;
	
	public ProjectDetailsPanel(ActionListener listener) {
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		projectNameTextField = new JTextField();
		GridBagConstraints gbc_projectNameTextField = new GridBagConstraints();
		gbc_projectNameTextField.gridwidth = 2;
		gbc_projectNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_projectNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_projectNameTextField.gridx = 0;
		gbc_projectNameTextField.gridy = 1;
		add(projectNameTextField, gbc_projectNameTextField);
		projectNameTextField.setColumns(10);
		
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
		
		JLabel locLabel = new JLabel("Location");
		GridBagConstraints gbc_locLabel = new GridBagConstraints();
		gbc_locLabel.gridwidth = 2;
		gbc_locLabel.insets = new Insets(0, 0, 5, 0);
		gbc_locLabel.anchor = GridBagConstraints.WEST;
		gbc_locLabel.gridx = 0;
		gbc_locLabel.gridy = 4;
		add(locLabel, gbc_locLabel);
		
		projectLocationTextField = new JTextField();
		GridBagConstraints gbc_projectLocationTextField = new GridBagConstraints();
		gbc_projectLocationTextField.insets = new Insets(0, 0, 0, 5);
		gbc_projectLocationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_projectLocationTextField.gridx = 0;
		gbc_projectLocationTextField.gridy = 5;
		add(projectLocationTextField, gbc_projectLocationTextField);
		projectLocationTextField.setColumns(10);
		
		JButton btnNewButton = new JButton("Browse ...");
		btnNewButton.setActionCommand(MainActionCommands.SELECT_PROJECT_LOCATION_COMMAND.getName());
		btnNewButton.addActionListener(listener);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 5;
		add(btnNewButton, gbc_btnNewButton);
	}
	
	public String getProjectName() {
		return projectNameTextField.getText().trim();
	}

	public String getProjectDescription() {
		return descriptionTextArea.getText().trim();
	}
	
	public void setProjectLocation(File location) {
		projectLocationTextField.setText(location.getAbsolutePath());
	}
	
	public String getProjectLocationPath() {
		return projectLocationTextField.getText().trim();
	}
}











