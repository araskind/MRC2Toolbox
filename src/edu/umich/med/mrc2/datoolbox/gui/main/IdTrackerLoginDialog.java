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
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class IdTrackerLoginDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -1319197380515710262L;
	private static final Icon idTrackerLoginIcon = GuiUtils.getIcon("idTrackerLogin", 32);
	private JTextField userNameTextField;
	private JPasswordField passwordTextField;
	private JButton btnLogIn;
	private JButton changePasswordButton;
	private IdTrackerPasswordChangeDialog passwordChangeDialog;

	public IdTrackerLoginDialog(ActionListener listener) {
		super();
		setPreferredSize(new Dimension(400, 200));
		setTitle("Log in to ID tracker database");
		setIconImage(((ImageIcon) idTrackerLoginIcon).getImage());
		setSize(new Dimension(400, 200));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblUserId = new JLabel("User ID");
		GridBagConstraints gbc_lblUserId = new GridBagConstraints();
		gbc_lblUserId.insets = new Insets(0, 0, 5, 5);
		gbc_lblUserId.anchor = GridBagConstraints.EAST;
		gbc_lblUserId.gridx = 0;
		gbc_lblUserId.gridy = 0;
		panel.add(lblUserId, gbc_lblUserId);

		userNameTextField = new JTextField();
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

//		JButton btnCancel = new JButton("Cancel");
//		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
//		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
//		gbc_btnCancel.gridx = 1;
//		gbc_btnCancel.gridy = 2;
//		panel.add(btnCancel, gbc_btnCancel);
//
//		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
//		ActionListener al = new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				dispose();
//			}
//		};
//		btnCancel.addActionListener(al);

		btnLogIn = new JButton("Log in");
		btnLogIn.setActionCommand(MainActionCommands.LOGIN_TO_ID_TRACKER_COMMAND.getName());
		btnLogIn.addActionListener(listener);
		GridBagConstraints gbc_btnLogIn = new GridBagConstraints();
		gbc_btnLogIn.insets = new Insets(0, 0, 5, 0);
		gbc_btnLogIn.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLogIn.gridx = 2;
		gbc_btnLogIn.gridy = 2;
		panel.add(btnLogIn, gbc_btnLogIn);

		JRootPane rootPane = SwingUtilities.getRootPane(btnLogIn);
//		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnLogIn);
		
		JLabel label = new JLabel("   ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 3;
		panel.add(label, gbc_label);
		
		changePasswordButton = new JButton("Change password");
		changePasswordButton.setIcon(GuiUtils.getIcon("key", 24));
		changePasswordButton.setActionCommand(MainActionCommands.CHANGE_ID_TRACKER_PASSWORD_DIALOG_COMMAND.getName());
		changePasswordButton.addActionListener(this);
		GridBagConstraints gbc_changePasswordButton = new GridBagConstraints();
		gbc_changePasswordButton.gridwidth = 2;
		gbc_changePasswordButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_changePasswordButton.gridx = 1;
		gbc_changePasswordButton.gridy = 4;
		panel.add(changePasswordButton, gbc_changePasswordButton);

		pack();
	}

	public String getUserName() {
		return userNameTextField.getText().trim();
	}

	public String getPassword() {
		return new String(passwordTextField.getPassword());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(MainActionCommands.CHANGE_ID_TRACKER_PASSWORD_DIALOG_COMMAND.getName())) {
			passwordChangeDialog = new IdTrackerPasswordChangeDialog(this);
			passwordChangeDialog.setLocationRelativeTo(this);
			passwordChangeDialog.setVisible(true);
		}
		if(e.getActionCommand().equals(MainActionCommands.CHANGE_ID_TRACKER_PASSWORD_COMMAND.getName())) {
			changePassword();
		}
	}

	private void changePassword() {

		Collection<String> errors = validateNewPassword();
		if(!errors.isEmpty()) {			
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), passwordChangeDialog);
			return;
		}
		String userName = passwordChangeDialog.getUserName();
		String oldPassword = passwordChangeDialog.getOldPassword();
		String newPassword = passwordChangeDialog.getNewPassword();
		LIMSUser user = null;
		try {
			user = UserUtils.getUserByUserId(userName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(user != null) {
			try {
				UserUtils.changePassword(user, oldPassword, newPassword);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		passwordChangeDialog.dispose();
	}
	
	private Collection<String> validateNewPassword() {
		
		Collection<String>errors = new ArrayList<String>();
		
		String userName = passwordChangeDialog.getUserName();
		String oldPassword = passwordChangeDialog.getOldPassword();
		String newPassword = passwordChangeDialog.getNewPassword();
		String newPasswordRetyped = passwordChangeDialog.getNewPasswordRetyped();
		
		if(userName.isEmpty() || oldPassword.isEmpty() ||  				
				newPassword.isEmpty() ||  newPasswordRetyped.isEmpty()) {
			errors.add("All fields in the form must be completed.");
			return errors;
		}
		//	Verify userName
		LIMSUser user = null;
		try {
			user = UserUtils.getUserByUserId(userName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(user == null) {
			errors.add("Incorrect user name.");
			return errors;
		}
		//	Verify old password
		user = null;
		try {
			user = UserUtils.getUserLogon(userName, oldPassword);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(user == null) {
			errors.add("Incorrect old password.");
			return errors;
		}
		if(!newPassword.equals(newPasswordRetyped)) {
			
			errors.add("New password repeat do not match original.");
			return errors;
		}
		return errors;
	}
}



















