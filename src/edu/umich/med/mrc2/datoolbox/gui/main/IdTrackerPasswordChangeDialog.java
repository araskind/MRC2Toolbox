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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
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

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class IdTrackerPasswordChangeDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -1319197380515710262L;
	
	private static final Icon keyIcon = GuiUtils.getIcon("key", 32);
	private static final Icon capsLockIcon = GuiUtils.getIcon("capsLock", 16);
	
	private JTextField userNameTextField;
	private JPasswordField oldPasswordTextField;
	private JPasswordField newPasswordTextField;
	private JPasswordField newPasswordRetypeField;
	private JButton changePasswordButton;
	private JLabel capsLockLabel;

	public IdTrackerPasswordChangeDialog(ActionListener listener) {
		
		super();
		setPreferredSize(new Dimension(350, 250));
		setTitle("Change password");
		setIconImage(((ImageIcon) keyIcon).getImage());
		setSize(new Dimension(350, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 80, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblUserId_1 = new JLabel("User ID");
		GridBagConstraints gbc_lblUserId_1 = new GridBagConstraints();
		gbc_lblUserId_1.anchor = GridBagConstraints.EAST;
		gbc_lblUserId_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblUserId_1.gridx = 0;
		gbc_lblUserId_1.gridy = 0;
		panel.add(lblUserId_1, gbc_lblUserId_1);
		
		userNameTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(userNameTextField, gbc_textField);
		userNameTextField.setColumns(10);

		JLabel lblUserId = new JLabel("Old password");
		GridBagConstraints gbc_lblUserId = new GridBagConstraints();
		gbc_lblUserId.insets = new Insets(0, 0, 5, 5);
		gbc_lblUserId.anchor = GridBagConstraints.EAST;
		gbc_lblUserId.gridx = 0;
		gbc_lblUserId.gridy = 1;
		panel.add(lblUserId, gbc_lblUserId);

		oldPasswordTextField = new JPasswordField();
		GridBagConstraints gbc_userIdTextField = new GridBagConstraints();
		gbc_userIdTextField.gridwidth = 2;
		gbc_userIdTextField.insets = new Insets(0, 0, 5, 5);
		gbc_userIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_userIdTextField.gridx = 1;
		gbc_userIdTextField.gridy = 1;
		panel.add(oldPasswordTextField, gbc_userIdTextField);
		oldPasswordTextField.setColumns(10);
		
		capsLockLabel = new JLabel("");
		boolean isOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
		if (isOn == true) {
			capsLockLabel.setIcon(capsLockIcon);
		} else {
			capsLockLabel.setIcon(null);
		}
		GridBagConstraints gbc_capsLockLabel = new GridBagConstraints();
		gbc_capsLockLabel.insets = new Insets(0, 0, 5, 0);
		gbc_capsLockLabel.gridx = 3;
		gbc_capsLockLabel.gridy = 1;
		panel.add(capsLockLabel, gbc_capsLockLabel);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher()
	    {
	        @Override
	        public boolean dispatchKeyEvent(KeyEvent e)
	        {
				if (KeyEvent.VK_CAPS_LOCK == e.getKeyCode()) {
					boolean isOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
					if (isOn == true) {
						capsLockLabel.setIcon(capsLockIcon);
					} else {
						capsLockLabel.setIcon(null);
					}
				}
	            return false;
	        }
	    });
	    
		JLabel lblPassword = new JLabel("New password");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.anchor = GridBagConstraints.EAST;
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 2;
		panel.add(lblPassword, gbc_lblPassword);

		newPasswordTextField = new JPasswordField();
		GridBagConstraints gbc_newPasswordTextField = new GridBagConstraints();
		gbc_newPasswordTextField.insets = new Insets(0, 0, 5, 5);
		gbc_newPasswordTextField.gridwidth = 2;
		gbc_newPasswordTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_newPasswordTextField.gridx = 1;
		gbc_newPasswordTextField.gridy = 2;
		panel.add(newPasswordTextField, gbc_newPasswordTextField);
		newPasswordTextField.setColumns(10);
		
		JLabel lblPasswordRetype = new JLabel("Retype new password");
		GridBagConstraints gbc_lblPasswordRetype = new GridBagConstraints();
		gbc_lblPasswordRetype.anchor = GridBagConstraints.EAST;
		gbc_lblPasswordRetype.insets = new Insets(0, 0, 5, 5);
		gbc_lblPasswordRetype.gridx = 0;
		gbc_lblPasswordRetype.gridy = 3;
		panel.add(lblPasswordRetype, gbc_lblPasswordRetype);

		newPasswordRetypeField = new JPasswordField();
		GridBagConstraints gbc_newPasswordRetypeField = new GridBagConstraints();
		gbc_newPasswordRetypeField.insets = new Insets(0, 0, 5, 5);
		gbc_newPasswordRetypeField.gridwidth = 2;
		gbc_newPasswordRetypeField.fill = GridBagConstraints.HORIZONTAL;
		gbc_newPasswordRetypeField.gridx = 1;
		gbc_newPasswordRetypeField.gridy = 3;
		panel.add(newPasswordRetypeField, gbc_newPasswordRetypeField);
		newPasswordRetypeField.setColumns(10);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.WEST;
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 5;
		panel.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(al);

		changePasswordButton = new JButton("Change password");
		changePasswordButton.setIcon(GuiUtils.getIcon("key", 24));
		changePasswordButton.setActionCommand(MainActionCommands.CHANGE_IDTRACKER_PASSWORD_COMMAND.getName());
		changePasswordButton.addActionListener(listener);
				
		JLabel label = new JLabel("  ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 4;
		panel.add(label, gbc_label);
		
		GridBagConstraints gbc_changePasswordButton = new GridBagConstraints();
		gbc_changePasswordButton.insets = new Insets(0, 0, 0, 5);
		gbc_changePasswordButton.gridwidth = 2;
		gbc_changePasswordButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_changePasswordButton.gridx = 1;
		gbc_changePasswordButton.gridy = 5;
		panel.add(changePasswordButton, gbc_changePasswordButton);

		JRootPane rootPane = SwingUtilities.getRootPane(changePasswordButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(changePasswordButton);
		pack();
	}

	public String getUserName() {
		return userNameTextField.getText().trim();
	}
	
	public String getOldPassword() {
		return new String(oldPasswordTextField.getPassword());
	}

	public String getNewPassword() {
		return new String(newPasswordTextField.getPassword());
	}
	
	public String getNewPasswordRetyped() {
		return new String(newPasswordRetypeField.getPassword());
	}
}



















