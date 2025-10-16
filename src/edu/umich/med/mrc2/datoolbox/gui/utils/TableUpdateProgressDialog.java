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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class TableUpdateProgressDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 5382501259608911620L;
	private JPanel panel;
	private JLabel messageLabel;

	private static final Icon loadingIcon = GuiUtils.getIcon("clock", 32);

	public TableUpdateProgressDialog(String message, Component parent, LongTableUpdateTask task) {

		super();
		setTitle("Operation in progress ...");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setIconImage(((ImageIcon) loadingIcon).getImage());
		setSize(new Dimension(500, 150));
		setPreferredSize(new Dimension(500, 150));
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 30, 30, 30));		
		panel.setLayout(new GridBagLayout());
		getContentPane().add(panel, BorderLayout.CENTER);

		Icon loader = GuiUtils.getLoaderIcon("orange_circles", 64);
		messageLabel = new JLabel(message, loader, SwingConstants.LEFT);	
		messageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(messageLabel);

		pack();
		
		Runnable swingCode = new Runnable() {

			public void run() {
				task.setProgressDialog(TableUpdateProgressDialog.this);
				task.execute();
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeAndWait(swingCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setMessage(String message) {
		messageLabel.setText(message);
	}
}






















