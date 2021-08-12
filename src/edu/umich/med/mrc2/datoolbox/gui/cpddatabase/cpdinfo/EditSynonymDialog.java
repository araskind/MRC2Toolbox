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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class EditSynonymDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 4479358488451256003L;

	private static final Icon editSynonymIcon = GuiUtils.getIcon("editSynonym", 32);
	private static final Icon addSynonymIcon = GuiUtils.getIcon("addSynonym", 32);

	private String originalName;
	private JButton btnSave;
	private JCheckBox chckbxSetAsPrimary;
	private JTextArea textArea;

	public EditSynonymDialog(
			String originalName,
			boolean isPrimary,
			ActionListener actionListener) {
		super();
		this.originalName = originalName;

		setPreferredSize(new Dimension(400, 150));
		setSize(new Dimension(400, 150));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new BorderLayout(0, 0));

		chckbxSetAsPrimary = new JCheckBox("Set as primary name");
		chckbxSetAsPrimary.setSelected(isPrimary);
		dataPanel.add(chckbxSetAsPrimary, BorderLayout.SOUTH);

		textArea = new JTextArea();
		textArea.setBorder(new LineBorder(new Color(0, 0, 0)));
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		dataPanel.add(textArea, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		finalizeDialog();
	}

	private void finalizeDialog() {

		if(originalName == null) {

			setTitle("Add new synonym");
			setIconImage(((ImageIcon) addSynonymIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_SYNONYM_COMMAND.getName());
		}
		else {
			setTitle("Edit synonym");
			setIconImage(((ImageIcon) editSynonymIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_SYNONYM_COMMAND.getName());
			textArea.setText(originalName);
		}
		pack();
	}

	/**
	 * @return the originalName
	 */
	public String getOriginalName() {
		return originalName;
	}

	public String getEditedName() {
		return textArea.getText().replace("\n", "").replace("\r", "").trim();
	}

	/**
	 * @return the isPrimary
	 */
	public boolean isNamePrimary() {
		return chckbxSetAsPrimary.isSelected();
	}
}
