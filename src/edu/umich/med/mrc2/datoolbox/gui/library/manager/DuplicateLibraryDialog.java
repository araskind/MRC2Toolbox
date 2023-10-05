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

package edu.umich.med.mrc2.datoolbox.gui.library.manager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DuplicateLibraryDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 4172902312202222510L;
	private JTextField textField;
	private JButton cancelButton;
	private JButton copyButton;
	private JCheckBox clearRtCheckBox;
	private JCheckBox clearSpectraCheckBox;
	private CompoundLibrary library;
	private JCheckBox clearAnnotationsCheckBox;

	private static final Icon duplicateLibraryIcon = GuiUtils.getIcon("duplicateLibrary", 32);

	public DuplicateLibraryDialog(JDialog parent, ActionListener listener) {

		super(parent, "Duplicate library", true);
		setIconImage(((ImageIcon) duplicateLibraryIcon).getImage());

		setSize(new Dimension(400, 200));
		setPreferredSize(new Dimension(400, 200));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		panel_1.add(lblName, gbc_lblName);

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel_1.add(textField, gbc_textField);
		textField.setColumns(10);

		clearRtCheckBox = new JCheckBox("Clear retention times");
		GridBagConstraints gbc_clearRetentionCheckBox = new GridBagConstraints();
		gbc_clearRetentionCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_clearRetentionCheckBox.anchor = GridBagConstraints.WEST;
		gbc_clearRetentionCheckBox.gridwidth = 2;
		gbc_clearRetentionCheckBox.gridx = 0;
		gbc_clearRetentionCheckBox.gridy = 1;
		panel_1.add(clearRtCheckBox, gbc_clearRetentionCheckBox);

		clearSpectraCheckBox = new JCheckBox("Clear adducts");
		GridBagConstraints gbc_chckbxNewCheckBox_1 = new GridBagConstraints();
		gbc_chckbxNewCheckBox_1.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxNewCheckBox_1.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox_1.gridwidth = 2;
		gbc_chckbxNewCheckBox_1.gridx = 0;
		gbc_chckbxNewCheckBox_1.gridy = 2;
		panel_1.add(clearSpectraCheckBox, gbc_chckbxNewCheckBox_1);

		clearAnnotationsCheckBox = new JCheckBox("Clear annotations");
		GridBagConstraints gbc_clearAnnotationsCheckBox = new GridBagConstraints();
		gbc_clearAnnotationsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_clearAnnotationsCheckBox.gridwidth = 2;
		gbc_clearAnnotationsCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_clearAnnotationsCheckBox.gridx = 0;
		gbc_clearAnnotationsCheckBox.gridy = 3;
		panel_1.add(clearAnnotationsCheckBox, gbc_clearAnnotationsCheckBox);

		cancelButton = new JButton("Cancel");
		panel.add(cancelButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);

		copyButton = new JButton("Create new library");
		copyButton.setActionCommand(MainActionCommands.DUPLICATE_LIBRARY_COMMAND.getName());
		copyButton.addActionListener(listener);
		panel.add(copyButton);

		JRootPane rootPane = SwingUtilities.getRootPane(copyButton);
		rootPane.setDefaultButton(copyButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		pack();
	}

	public void loadLibrary(CompoundLibrary library) {

		this.library =  library;
		textField.setText(this.library.getLibraryName() + " copy");
		clearRtCheckBox.setSelected(true);
		clearSpectraCheckBox.setSelected(false);
		clearAnnotationsCheckBox.setSelected(false);
	}

	public String getLibraryName() {

		return textField.getText().trim();
	}

	public boolean clearRetention() {

		return clearRtCheckBox.isSelected();
	}

	public boolean clearSpectra() {

		return clearSpectraCheckBox.isSelected();
	}

	public boolean clearAnnotations() {

		return clearAnnotationsCheckBox.isSelected();
	}

	public CompoundLibrary getLibrary() {
		return library;
	}
}


























