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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class IndeterminateProgressDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 5382501259608911620L;
	private JPanel panel;
	private JLabel messageLabel;

	private static final Icon loadingIcon = GuiUtils.getIcon("clock", 32);

	public IndeterminateProgressDialog(String message, Component parent, LongUpdateTask task) {

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
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 434, 0 };
		gbl_panel.rowHeights = new int[] { 34, 47, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		messageLabel = new JLabel(message);
		messageLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
		GridBagConstraints gbc_messageLabel = new GridBagConstraints();
		gbc_messageLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_messageLabel.insets = new Insets(0, 0, 5, 0);
		gbc_messageLabel.gridx = 0;
		gbc_messageLabel.gridy = 0;
		panel.add(messageLabel, gbc_messageLabel);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setMinimumSize(new Dimension(10, 20));
		progressBar.setIndeterminate(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 1;
		panel.add(progressBar, gbc_progressBar);
		pack();
		task.setProgressDialog(this);
		task.execute();
	
	}

	public void setMessage(String message) {
		messageLabel.setText(message);
	}
}






















