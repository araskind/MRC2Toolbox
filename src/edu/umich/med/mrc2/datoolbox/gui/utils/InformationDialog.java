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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class InformationDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 4820644205036160130L;

	private static final Icon errorIcon = GuiUtils.getIcon("error", 128);
	private static final Icon infoIcon = GuiUtils.getIcon("info", 128);

	private static final String CLOSE_COMMAND = "CLOSE";
	private static final String COPY_COMMAND = "COPY";

	private JTextArea textArea;
	private JLabel messageLabel;
	
	public InformationDialog(
			String title, 
			String message, 
			String details, 
			Component parent,
			InfoDialogType dialogType) {

		super(MRC2ToolBoxCore.getMainWindow(), title, true);

		initGui(dialogType);

		messageLabel.setText(message);
		textArea.setText(details);

		if(parent == null)
			setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		else
			setLocationRelativeTo(parent);

		setVisible(true);
	}

	public InformationDialog(
			String title, 
			String message, 
			String details, 
			Component parent) {

		this(title, message, details, parent, InfoDialogType.INFO);
	}

	public InformationDialog(String message, Throwable exception, Component parent) {

		super(MRC2ToolBoxCore.getMainWindow(), "Error!", true);

		initGui(InfoDialogType.ERROR);

		messageLabel.setText(message);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		textArea.setText(sw.toString());
		exception.printStackTrace();

		if(parent == null)
			setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		else
			setLocationRelativeTo(parent);

		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(CLOSE_COMMAND))
			dispose();

		if (command.equals(COPY_COMMAND)) {

			String myString = textArea.getText();
			StringSelection stringSelection = new StringSelection(myString);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}
	}

	private void initGui(InfoDialogType type) {

		setMaximumSize(new Dimension(1000, 800));
		setPreferredSize(new Dimension(500, 300));
		setSize(new Dimension(600, 400));
		setResizable(false);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(20, 20, 20, 20));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 123, 327, 327, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 31, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		messageLabel = new JLabel("");
		messageLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_messageLabel = new GridBagConstraints();
		gbc_messageLabel.anchor = GridBagConstraints.WEST;
		gbc_messageLabel.gridwidth = 3;
		gbc_messageLabel.insets = new Insets(0, 0, 5, 0);
		gbc_messageLabel.gridx = 0;
		gbc_messageLabel.gridy = 0;
		panel.add(messageLabel, gbc_messageLabel);

		JLabel errorIconLabel = new JLabel("");
		errorIconLabel.setMinimumSize(new Dimension(140, 140));
		errorIconLabel.setSize(new Dimension(140, 140));

		if (type.equals(InfoDialogType.ERROR))
			errorIconLabel.setIcon(errorIcon);

		if (type.equals(InfoDialogType.INFO))
			errorIconLabel.setIcon(infoIcon);

		GridBagConstraints gbc_errorIconLabel = new GridBagConstraints();
		gbc_errorIconLabel.fill = GridBagConstraints.BOTH;
		gbc_errorIconLabel.insets = new Insets(0, 0, 5, 5);
		gbc_errorIconLabel.gridx = 0;
		gbc_errorIconLabel.gridy = 2;
		panel.add(errorIconLabel, gbc_errorIconLabel);

		textArea = new JTextArea();
		textArea.setBorder(new LineBorder(new Color(0, 0, 0)));
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 2;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 2;
		panel.add(new JScrollPane(textArea), gbc_textArea);

		JButton copyErrorButton = new JButton("Copy to clipboard");
		copyErrorButton.addActionListener(this);
		copyErrorButton.setActionCommand(COPY_COMMAND);
		GridBagConstraints gbc_copyErrorButton = new GridBagConstraints();
		gbc_copyErrorButton.fill = GridBagConstraints.BOTH;
		gbc_copyErrorButton.insets = new Insets(0, 0, 0, 5);
		gbc_copyErrorButton.gridx = 1;
		gbc_copyErrorButton.gridy = 3;
		panel.add(copyErrorButton, gbc_copyErrorButton);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButton.setActionCommand(CLOSE_COMMAND);
		GridBagConstraints gbc_closeButton = new GridBagConstraints();
		gbc_closeButton.fill = GridBagConstraints.VERTICAL;
		gbc_closeButton.anchor = GridBagConstraints.EAST;
		gbc_closeButton.gridx = 2;
		gbc_closeButton.gridy = 3;
		panel.add(closeButton, gbc_closeButton);
	}
}
