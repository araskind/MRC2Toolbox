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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.projinfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

//	TODO delete when Data pipeline editor implemented
public class AssayMethodDialog extends JDialog {

	private Assay oldMethod;
	private JLabel methodNameLabel;
	private JComboBox assayComboBox;
	private JButton cancelButton;
	private JButton confirmButton;

	public AssayMethodDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Change method for selected assay", true);

		setSize(new Dimension(500, 150));
		setPreferredSize(new Dimension(500, 150));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblCurrentMethod = new JLabel("Current method:");
		GridBagConstraints gbc_lblCurrentMethod = new GridBagConstraints();
		gbc_lblCurrentMethod.anchor = GridBagConstraints.EAST;
		gbc_lblCurrentMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentMethod.gridx = 0;
		gbc_lblCurrentMethod.gridy = 0;
		panel.add(lblCurrentMethod, gbc_lblCurrentMethod);

		methodNameLabel = new JLabel("");
		GridBagConstraints gbc_methodNameLabel = new GridBagConstraints();
		gbc_methodNameLabel.gridwidth = 2;
		gbc_methodNameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_methodNameLabel.anchor = GridBagConstraints.WEST;
		gbc_methodNameLabel.gridx = 1;
		gbc_methodNameLabel.gridy = 0;
		panel.add(methodNameLabel, gbc_methodNameLabel);

		JLabel lblSelectNewMethod = new JLabel("Select new method:");
		GridBagConstraints gbc_lblSelectNewMethod = new GridBagConstraints();
		gbc_lblSelectNewMethod.anchor = GridBagConstraints.EAST;
		gbc_lblSelectNewMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectNewMethod.gridx = 0;
		gbc_lblSelectNewMethod.gridy = 1;
		panel.add(lblSelectNewMethod, gbc_lblSelectNewMethod);

		assayComboBox = new JComboBox();
		resetAssaySelector();
		GridBagConstraints gbc_assayComboBox = new GridBagConstraints();
		gbc_assayComboBox.gridwidth = 2;
		gbc_assayComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_assayComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_assayComboBox.gridx = 1;
		gbc_assayComboBox.gridy = 1;
		panel.add(assayComboBox, gbc_assayComboBox);

		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.EAST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 1;
		gbc_cancelButton.gridy = 2;
		panel.add(cancelButton, gbc_cancelButton);

		confirmButton = new JButton("Confirm change");
		confirmButton.setActionCommand(MainActionCommands.CONFIRM_ASSAY_CHANGE_COMMAND.getName());
		confirmButton.addActionListener(listener);
		GridBagConstraints gbc_confirmButton = new GridBagConstraints();
		gbc_confirmButton.anchor = GridBagConstraints.EAST;
		gbc_confirmButton.gridx = 2;
		gbc_confirmButton.gridy = 2;
		panel.add(confirmButton, gbc_confirmButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(confirmButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(confirmButton);

		pack();
	}

	@SuppressWarnings("unchecked")
	public void resetAssaySelector() {

		Collection<Assay> availableAssays = new ArrayList<Assay>();
//		try {
//			availableAssays = AssayDatabaseUtils.getAssays(true);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(MRC2ToolBoxCore.getCurrentProject() != null) {
//
//			for (Assay am : MRC2ToolBoxCore.getCurrentProject().getDataPipelines())
//				availableAssays.remove(am);
//		}
		assayComboBox.setModel(new SortedComboBoxModel<Assay>(availableAssays));
	}

	public void loadAssay(Assay toLoad) {

		oldMethod = toLoad;
		methodNameLabel.setText(oldMethod.getName());
		resetAssaySelector();
		assayComboBox.setSelectedIndex(-1);
	}

	public Assay getOldMethod() {
		return oldMethod;
	}

	public Assay getNewMethod() {
		return (Assay) assayComboBox.getSelectedItem();
	}
}
