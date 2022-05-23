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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.query;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class SaveQueryDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4355294477862968508L;
	private static final Icon dialogIcon = GuiUtils.getIcon("findList", 32);
	private JTextArea textArea;

	public SaveQueryDialog(ActionListener listener) {
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Provide query description");
		setIconImage(((ImageIcon)dialogIcon).getImage());
		
		setSize(new Dimension(400, 200));
		setPreferredSize(new Dimension(400, 200));	
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		getContentPane().add(textArea, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);
		btnCancel.addActionListener(al);
		btnCancel.addActionListener(al);

		JButton searchButton = new JButton(MainActionCommands.ID_TRACKER_SAVE_QUERY_COMMAND.getName());
		searchButton.addActionListener(listener);
		searchButton.setActionCommand(MainActionCommands.ID_TRACKER_SAVE_QUERY_COMMAND.getName());
		panel_1.add(searchButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(searchButton);
		
		pack();
	}
	
	public String getQueryDescription() {
		return textArea.getText().trim();
	}
}










