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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public abstract class BasicDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JButton primaryActionButton;
	protected JButton btnCancel;
	protected JPanel mainPanel;
	protected JPanel buttonPanel;
	protected ActionListener cancelListener;
	
	public BasicDialog(
			String title,
			String iconId,
			Dimension preferredSize,
			ActionListener actionListener) {
		
		super();
		setTitle(title);
		setIconImage(((ImageIcon) GuiUtils.getIcon(iconId, 32)).getImage());
		setPreferredSize(preferredSize);
		setSize(preferredSize);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		createCancelListener();
		btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		btnCancel.addActionListener(cancelListener);


		primaryActionButton = new JButton("Save");
		primaryActionButton.addActionListener(actionListener);
		buttonPanel.add(primaryActionButton);
		JRootPane rootPane = SwingUtilities.getRootPane(primaryActionButton);
		rootPane.registerKeyboardAction(
				cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(primaryActionButton);
	}
	
	protected void createCancelListener() {
		
		cancelListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};		
	}
}
