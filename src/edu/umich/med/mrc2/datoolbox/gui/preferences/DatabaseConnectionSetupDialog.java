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

package edu.umich.med.mrc2.datoolbox.gui.preferences;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DatabaseConnectionSetupDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2797048267276292340L;
	private static final Icon preferencesIcon = GuiUtils.getIcon("database", 32);
	private DatabasePreferencesPanel databasePreferencesPanel;
	
	public DatabaseConnectionSetupDialog() {
		super();
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		setTitle("Database Connection Setup");
		setIconImage(((ImageIcon) preferencesIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 480));
		setPreferredSize(new Dimension(640, 480));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		databasePreferencesPanel = 
				new DatabasePreferencesPanel();
		getContentPane().add(databasePreferencesPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton saveButton = new JButton(MainActionCommands.CONTINUE_PROGRAM_STARTUP_COMMAND.getName());
		saveButton.setActionCommand(MainActionCommands.CONTINUE_PROGRAM_STARTUP_COMMAND.getName());
		saveButton.addActionListener(this);
		buttonPanel.add(saveButton);

		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.setDefaultButton(saveButton);

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.CONTINUE_PROGRAM_STARTUP_COMMAND.getName())) {
			
			if(databasePreferencesPanel.testConnection(true)) {				
				databasePreferencesPanel.savePreferences();
				dispose();	
			}
		}
	}
}
