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

package edu.umich.med.mrc2.datoolbox.gui.assay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class AssayEditorDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -5719300923307257792L;
	private JTextField methodNameTextField;
	private JButton cancelButton;
	private JButton saveButton;
	private Assay activeMethod;
	private boolean createNewMethod;

	private static final Icon editAcqMethodIcon = GuiUtils.getIcon("editAcqMethod", 32);

	public AssayEditorDialog(JDialog parent) {

		super(parent, "Edit assay methods");
		setIconImage(((ImageIcon) editAcqMethodIcon).getImage());
		setPreferredSize(new Dimension(500, 150));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(500, 150));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblMethodName = new JLabel("Assay name");
		GridBagConstraints gbc_lblMethodName = new GridBagConstraints();
		gbc_lblMethodName.insets = new Insets(0, 0, 5, 5);
		gbc_lblMethodName.anchor = GridBagConstraints.EAST;
		gbc_lblMethodName.gridx = 0;
		gbc_lblMethodName.gridy = 0;
		panel.add(lblMethodName, gbc_lblMethodName);

		methodNameTextField = new JTextField();
		GridBagConstraints gbc_methodNameTextField = new GridBagConstraints();
		gbc_methodNameTextField.gridwidth = 2;
		gbc_methodNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_methodNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodNameTextField.gridx = 1;
		gbc_methodNameTextField.gridy = 0;
		panel.add(methodNameTextField, gbc_methodNameTextField);
		methodNameTextField.setColumns(10);

		DefaultComboBoxModel<Polarity> model = 
				new DefaultComboBoxModel<Polarity>(Polarity.values());

		JLabel lblNewLabel = new JLabel("  ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.EAST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 1;
		gbc_cancelButton.gridy = 2;
		panel.add(cancelButton, gbc_cancelButton);

		saveButton = new JButton("Save assay");
		saveButton.setActionCommand(MainActionCommands.EDIT_ASSAY_METHOD_COMMAND.getName());
		saveButton.addActionListener((ActionListener) parent);
		GridBagConstraints gbc_saveButton = new GridBagConstraints();
		gbc_saveButton.gridx = 2;
		gbc_saveButton.gridy = 2;
		panel.add(saveButton, gbc_saveButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);

		activeMethod = null;
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}

	public void loadMethodData(Assay method, boolean createNewMethod) {

		activeMethod = method;
		this.createNewMethod = createNewMethod;
		methodNameTextField.setText(method.getName().trim());
		
		if (createNewMethod) {
			setTitle("Create new assay");
			saveButton.setActionCommand(MainActionCommands.ADD_ASSAY_METHOD_COMMAND.getName());
		} else {
			setTitle("Edit assay");
			saveButton.setActionCommand(MainActionCommands.EDIT_ASSAY_METHOD_COMMAND.getName());
		}
	}

	public Assay getActiveMethod() {
		return activeMethod;
	}

	public String getMethodName() {
		return methodNameTextField.getText().trim();
	}

	public boolean isCreateNewMethod() {
		return createNewMethod;
	}
}
