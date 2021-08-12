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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.Renamable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class RenameDialog extends JDialog implements ActionListener {
	
	private JTextField textField;
	private JButton cancelButton;
	private JButton saveButton;
	private Renamable objectToRename;
	private Collection<Renamable>existingNames;
	
	public static final String SAVE_RENAMED = "Save renamed";

	public RenameDialog(String title, Renamable objectToRename, Collection<Renamable>existingNames) {

		super(MRC2ToolBoxCore.getMainWindow(), title);
		
		this.objectToRename = objectToRename;
		this.existingNames = existingNames;

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(400, 130));
		setPreferredSize(new Dimension(400, 130));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{138, 115, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(100, 25));
		textField.setMinimumSize(new Dimension(100, 25));
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 3;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		panel.add(textField, gbc_textField);
		textField.setColumns(10);
		textField.setText(objectToRename.getName().trim());
		
		JLabel label = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 1;
		panel.add(label, gbc_label);
		
		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.WEST;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.gridx = 1;
		gbc_btnNewButton_1.gridy = 2;
		panel.add(cancelButton, gbc_btnNewButton_1);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);
		
		saveButton = new JButton("Save changes");
		saveButton.setActionCommand(MainActionCommands.RENAME_GENERIC_COMMAND.getName());
		saveButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 2;
		panel.add(saveButton, gbc_btnNewButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		
		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(MainActionCommands.RENAME_GENERIC_COMMAND.getName())) {
			
			String newName = textField.getText().trim();
			
			if(newName.isEmpty()) {
				
				MessageDialog.showErrorMsg("Name can not be empty!", this);
				objectToRename.setNameValid(false);
				return;
			}			
			if(existingNames != null) {
				
				for(Renamable object : existingNames) {
					
					if(object.getName().equals(newName) && !object.equals(objectToRename)) {
						
						MessageDialog.showErrorMsg("Name is already taken! Please choose a different one.", this);
						objectToRename.setNameValid(false);
						return;
					}
				}
			}			
			objectToRename.setName(newName);
			objectToRename.setNameValid(true);
			dispose();
		}
	}
}






























