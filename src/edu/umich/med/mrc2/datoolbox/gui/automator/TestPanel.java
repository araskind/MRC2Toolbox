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

package edu.umich.med.mrc2.datoolbox.gui.automator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class TestPanel extends JPanel implements ActionListener {
	
	private static final Icon openIcon = GuiUtils.getIcon("open", 24);
	
	private JTextField positiveModeFolderTextField;
	private JTextField negativeModeFolderTextField;

	public TestPanel() {
		super();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 10, 0 };
		gridBagLayout.rowHeights = new int[] { 10, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblNewLabel = new JLabel("POS mode");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);
		
		JButton openPosModeFolderButton = new JButton(openIcon);
		openPosModeFolderButton.setActionCommand(
				MainActionCommands.OPEN_POSITIVE_MODE_RAW_DATA_FOLDER.getName());
		openPosModeFolderButton.addActionListener(this);
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 1;
		add(openPosModeFolderButton, gbc_lblNewLabel_1);

		positiveModeFolderTextField = new JTextField();
		GridBagConstraints gbc_txtSelectDataFile = new GridBagConstraints();
		gbc_txtSelectDataFile.insets = new Insets(0, 0, 5, 5);
		gbc_txtSelectDataFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSelectDataFile.gridx = 2;
		gbc_txtSelectDataFile.gridy = 1;
		add(positiveModeFolderTextField, gbc_txtSelectDataFile);
		positiveModeFolderTextField.setColumns(10);

		JButton recentPosFoldersButton = new JButton("Recent ...");
		recentPosFoldersButton.setActionCommand(
				MainActionCommands.SELECT_RECENT_POSITIVE_MODE_RAW_DATA_FOLDER.getName());
		recentPosFoldersButton.addActionListener(this);
		GridBagConstraints gbc_recentPosFoldersButton = new GridBagConstraints();
		gbc_recentPosFoldersButton.insets = new Insets(0, 0, 5, 5);
		gbc_recentPosFoldersButton.gridx = 3;
		gbc_recentPosFoldersButton.gridy = 1;
		add(recentPosFoldersButton, gbc_recentPosFoldersButton);
		
		JButton browseForPosFolderButton = new JButton("Browse...");
		browseForPosFolderButton.setActionCommand(
				MainActionCommands.BROWSE_FOR_POSITIVE_MODE_RAW_DATA_FOLDER.getName());
		browseForPosFolderButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 1;
		add(browseForPosFolderButton, gbc_btnNewButton);

		JLabel lblExperimentDesign = new JLabel("NEG mode");
		GridBagConstraints gbc_lblExperimentDesign = new GridBagConstraints();
		gbc_lblExperimentDesign.anchor = GridBagConstraints.WEST;
		gbc_lblExperimentDesign.insets = new Insets(0, 0, 0, 5);
		gbc_lblExperimentDesign.gridx = 0;
		gbc_lblExperimentDesign.gridy = 2;
		add(lblExperimentDesign, gbc_lblExperimentDesign);
		
		JButton openNegModeFolderButton = new JButton(openIcon);
		openNegModeFolderButton.setActionCommand(
				MainActionCommands.OPEN_NEGATIVE_MODE_RAW_DATA_FOLDER.getName());
		openNegModeFolderButton.addActionListener(this);
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 2;
		add(openNegModeFolderButton, gbc_lblNewLabel_2);

		negativeModeFolderTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 2;
		gbc_textField.gridy = 2;
		add(negativeModeFolderTextField, gbc_textField);
		negativeModeFolderTextField.setColumns(10);

		JButton recentNegFoldersButton = new JButton("Recent ...");
		recentNegFoldersButton.setActionCommand(
				MainActionCommands.SELECT_RECENT_NEGATIVE_MODE_RAW_DATA_FOLDER.getName());
		recentNegFoldersButton.addActionListener(this);
		GridBagConstraints gbc_recentNegFoldersButton = new GridBagConstraints();
		gbc_recentNegFoldersButton.insets = new Insets(0, 0, 0, 5);
		gbc_recentNegFoldersButton.gridx = 3;
		gbc_recentNegFoldersButton.gridy = 2;
		add(recentNegFoldersButton, gbc_recentNegFoldersButton);
		
		JButton browseForNegFolderButton = new JButton("Browse...");
		browseForNegFolderButton.setActionCommand(
				MainActionCommands.BROWSE_FOR_NEGATIVE_MODE_RAW_DATA_FOLDER.getName());
		browseForNegFolderButton.addActionListener(this);
		GridBagConstraints gbc_negModeButton = new GridBagConstraints();
		gbc_negModeButton.gridx = 4;
		gbc_negModeButton.gridy = 2;
		add(browseForNegFolderButton, gbc_negModeButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	
}
