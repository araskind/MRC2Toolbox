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

package edu.umich.med.mrc2.datoolbox.gui.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class IdTrackerPasswordActionUnlockDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -1319197380515710262L;
	private static final Icon idTrackerLoginIcon = GuiUtils.getIcon("idTrackerLogin", 32);
	private JTextField userNameTextField;
	private JPasswordField passwordTextField;
	private JButton btnLogIn;
	private String actionCommand2confirm;

	public IdTrackerPasswordActionUnlockDialog(ActionListener listener, String actionCommand2confirm) {
		super();
		setPreferredSize(new Dimension(400, 200));
		setTitle("Verify password");
		setIconImage(((ImageIcon) idTrackerLoginIcon).getImage());
		setSize(new Dimension(400, 200));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.actionCommand2confirm = actionCommand2confirm;

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblUserId = new JLabel("User ID");
		GridBagConstraints gbc_lblUserId = new GridBagConstraints();
		gbc_lblUserId.insets = new Insets(0, 0, 5, 5);
		gbc_lblUserId.anchor = GridBagConstraints.EAST;
		gbc_lblUserId.gridx = 0;
		gbc_lblUserId.gridy = 0;
		panel.add(lblUserId, gbc_lblUserId);

		userNameTextField = new JTextField();
		userNameTextField.setEditable(false);
		GridBagConstraints gbc_userIdTextField = new GridBagConstraints();
		gbc_userIdTextField.gridwidth = 2;
		gbc_userIdTextField.insets = new Insets(0, 0, 5, 0);
		gbc_userIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_userIdTextField.gridx = 1;
		gbc_userIdTextField.gridy = 0;
		panel.add(userNameTextField, gbc_userIdTextField);
		userNameTextField.setColumns(10);

		JLabel lblPassword = new JLabel("Password");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.anchor = GridBagConstraints.EAST;
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 1;
		panel.add(lblPassword, gbc_lblPassword);

		passwordTextField = new JPasswordField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.gridwidth = 2;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 1;
		panel.add(passwordTextField, gbc_textField);
		passwordTextField.setColumns(10);

		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 2;
		panel.add(btnCancel, gbc_btnCancel);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		btnLogIn = new JButton(MainActionCommands.VERIFY_TRACKER_PASSWORD_COMMAND.getName());
		btnLogIn.setActionCommand(MainActionCommands.VERIFY_TRACKER_PASSWORD_COMMAND.getName());
		btnLogIn.addActionListener(listener);
		GridBagConstraints gbc_btnLogIn = new GridBagConstraints();
		gbc_btnLogIn.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLogIn.gridx = 2;
		gbc_btnLogIn.gridy = 2;
		panel.add(btnLogIn, gbc_btnLogIn);

		JRootPane rootPane = SwingUtilities.getRootPane(btnLogIn);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnLogIn);

		pack();
	}

	public String getPassword() {
		return new String(passwordTextField.getPassword());
	}
	
	public void setUser(LIMSUser user) {
		userNameTextField.setText(user.getUserName());
	}

	public String getActionCommand2confirm() {
		return actionCommand2confirm;
	}
}



















