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

package edu.umich.med.mrc2.datoolbox.gui.users;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

import edu.umich.med.mrc2.datoolbox.data.enums.UserAffiliation;
import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.utils.CommonFormFieldVerifier;

public class UserEditorDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 11016722526002327L;
	private static final Icon addUserIcon = GuiUtils.getIcon("addUser", 32);
	private static final Icon editUserIcon = GuiUtils.getIcon("editUser", 32);
	
	private JButton cancelButton;
	private JButton saveButton;
	private LIMSUser user;
	private JTextField firstNameTextField;
	private JTextField lastNameTextField;
	private JTextField userNameTextField;
	private JComboBox affiliationComboBox;
	private JTextField emailTextField;
	private JTextField phoneTextField;
	private JComboBox organizationComboBox;
	private JCheckBox chckbxSuperuser;
	private JCheckBox chckbxActive;

	@SuppressWarnings("unchecked")
	public UserEditorDialog(LIMSUser user, ActionListener parent) {

		super();
		setPreferredSize(new Dimension(500, 300));
	
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(500, 300));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.user = user;

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);
		
		JLabel lblFirstName = new JLabel("First name");
		GridBagConstraints gbc_lblFirstName = new GridBagConstraints();
		gbc_lblFirstName.anchor = GridBagConstraints.EAST;
		gbc_lblFirstName.insets = new Insets(0, 0, 5, 5);
		gbc_lblFirstName.gridx = 0;
		gbc_lblFirstName.gridy = 0;
		panel.add(lblFirstName, gbc_lblFirstName);
		
		firstNameTextField = new JTextField();
		GridBagConstraints gbc_firstNameTextField = new GridBagConstraints();
		gbc_firstNameTextField.gridwidth = 3;
		gbc_firstNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_firstNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_firstNameTextField.gridx = 1;
		gbc_firstNameTextField.gridy = 0;
		panel.add(firstNameTextField, gbc_firstNameTextField);
		firstNameTextField.setColumns(10);
		
		JLabel lblLastName = new JLabel("Last name");
		GridBagConstraints gbc_lblLastName = new GridBagConstraints();
		gbc_lblLastName.anchor = GridBagConstraints.EAST;
		gbc_lblLastName.insets = new Insets(0, 0, 5, 5);
		gbc_lblLastName.gridx = 0;
		gbc_lblLastName.gridy = 1;
		panel.add(lblLastName, gbc_lblLastName);
		
		lastNameTextField = new JTextField();
		GridBagConstraints gbc_lastNameTextField = new GridBagConstraints();
		gbc_lastNameTextField.gridwidth = 3;
		gbc_lastNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_lastNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_lastNameTextField.gridx = 1;
		gbc_lastNameTextField.gridy = 1;
		panel.add(lastNameTextField, gbc_lastNameTextField);
		lastNameTextField.setColumns(10);
		
		JLabel lblUderid = new JLabel("UderID");
		GridBagConstraints gbc_lblUderid = new GridBagConstraints();
		gbc_lblUderid.anchor = GridBagConstraints.EAST;
		gbc_lblUderid.insets = new Insets(0, 0, 5, 5);
		gbc_lblUderid.gridx = 0;
		gbc_lblUderid.gridy = 2;
		panel.add(lblUderid, gbc_lblUderid);
		
		userNameTextField = new JTextField();
		GridBagConstraints gbc_userIdTextField = new GridBagConstraints();
		gbc_userIdTextField.insets = new Insets(0, 0, 5, 5);
		gbc_userIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_userIdTextField.gridx = 1;
		gbc_userIdTextField.gridy = 2;
		panel.add(userNameTextField, gbc_userIdTextField);
		userNameTextField.setColumns(10);
		
		JLabel lblAffiliation = new JLabel("Affiliation");
		GridBagConstraints gbc_lblAffiliation = new GridBagConstraints();
		gbc_lblAffiliation.anchor = GridBagConstraints.EAST;
		gbc_lblAffiliation.insets = new Insets(0, 0, 5, 5);
		gbc_lblAffiliation.gridx = 2;
		gbc_lblAffiliation.gridy = 2;
		panel.add(lblAffiliation, gbc_lblAffiliation);
		
		affiliationComboBox = new JComboBox<UserAffiliation>(
				new DefaultComboBoxModel<UserAffiliation>(UserAffiliation.values()));
		affiliationComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_affiliationComboBox = new GridBagConstraints();
		gbc_affiliationComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_affiliationComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_affiliationComboBox.gridx = 3;
		gbc_affiliationComboBox.gridy = 2;
		panel.add(affiliationComboBox, gbc_affiliationComboBox);

		JLabel lblNewLabel = new JLabel("Organization");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 3;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		Collection<IdTrackerOrganization> organizations = IDTDataCache.getOrganizations();
		organizationComboBox = new JComboBox<IdTrackerOrganization>(
				new DefaultComboBoxModel<IdTrackerOrganization>(
						organizations.toArray(new IdTrackerOrganization[organizations.size()])));
		organizationComboBox.setSelectedIndex(-1);	
		organizationComboBox.setRenderer(new OrganizationListCellRenderer());
		GridBagConstraints gbc_organizationComboBox = new GridBagConstraints();
		gbc_organizationComboBox.gridwidth = 3;
		gbc_organizationComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_organizationComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_organizationComboBox.gridx = 1;
		gbc_organizationComboBox.gridy = 3;
		panel.add(organizationComboBox, gbc_organizationComboBox);
		
		JLabel lblEmail = new JLabel("e-mail");
		GridBagConstraints gbc_lblEmail = new GridBagConstraints();
		gbc_lblEmail.anchor = GridBagConstraints.EAST;
		gbc_lblEmail.insets = new Insets(0, 0, 5, 5);
		gbc_lblEmail.gridx = 0;
		gbc_lblEmail.gridy = 4;
		panel.add(lblEmail, gbc_lblEmail);
		
		emailTextField = new JTextField();
		GridBagConstraints gbc_emailTextField = new GridBagConstraints();
		gbc_emailTextField.gridwidth = 3;
		gbc_emailTextField.insets = new Insets(0, 0, 5, 0);
		gbc_emailTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_emailTextField.gridx = 1;
		gbc_emailTextField.gridy = 4;
		panel.add(emailTextField, gbc_emailTextField);
		emailTextField.setColumns(10);
		
		JLabel lblPhone = new JLabel("Phone");
		GridBagConstraints gbc_lblPhone = new GridBagConstraints();
		gbc_lblPhone.anchor = GridBagConstraints.EAST;
		gbc_lblPhone.insets = new Insets(0, 0, 5, 5);
		gbc_lblPhone.gridx = 0;
		gbc_lblPhone.gridy = 5;
		panel.add(lblPhone, gbc_lblPhone);
		
		phoneTextField = new JTextField();
		GridBagConstraints gbc_phoneTextField = new GridBagConstraints();
		gbc_phoneTextField.insets = new Insets(0, 0, 5, 5);
		gbc_phoneTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_phoneTextField.gridx = 1;
		gbc_phoneTextField.gridy = 5;
		panel.add(phoneTextField, gbc_phoneTextField);
		phoneTextField.setColumns(10);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		
		chckbxSuperuser = new JCheckBox("Superuser");
		GridBagConstraints gbc_chckbxSuperuser = new GridBagConstraints();
		gbc_chckbxSuperuser.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxSuperuser.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSuperuser.gridx = 1;
		gbc_chckbxSuperuser.gridy = 6;
		panel.add(chckbxSuperuser, gbc_chckbxSuperuser);
		chckbxSuperuser.setSelected(false);
		
		chckbxActive = new JCheckBox("Active");
		GridBagConstraints gbc_chckbxActive = new GridBagConstraints();
		gbc_chckbxActive.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxActive.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxActive.gridx = 3;
		gbc_chckbxActive.gridy = 6;
		panel.add(chckbxActive, gbc_chckbxActive);
		chckbxActive.setSelected(true);
		
		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.EAST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 1;
		gbc_cancelButton.gridy = 7;
		panel.add(cancelButton, gbc_cancelButton);
		cancelButton.addActionListener(al);
		
		saveButton = new JButton("Save user data");
		saveButton.setActionCommand(MainActionCommands.EDIT_ASSAY_METHOD_COMMAND.getName());
		saveButton.addActionListener(parent);
		GridBagConstraints gbc_saveButton = new GridBagConstraints();
		gbc_saveButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_saveButton.gridwidth = 2;
		gbc_saveButton.gridx = 2;
		gbc_saveButton.gridy = 7;
		panel.add(saveButton, gbc_saveButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		
		loadUserData();
	}
	
	private void loadUserData() {
		
		if(user == null) {
			setTitle("Create new user");
			setIconImage(((ImageIcon)addUserIcon).getImage());
			saveButton.setActionCommand(MainActionCommands.ADD_USER_COMMAND.getName());
		}
		else {
			setTitle("Edit user data for " + user.getFirstName() + " " + 
					user.getLastName() + " (" + user.getUserName() + ")");
			setIconImage(((ImageIcon)editUserIcon).getImage());
			saveButton.setActionCommand(MainActionCommands.EDIT_USER_COMMAND.getName());
			firstNameTextField.setText(user.getFirstName());
			lastNameTextField.setText(user.getLastName());
			userNameTextField.setText(user.getUserName());
			
			UserAffiliation aff = UserAffiliation.getUserAffiliationByName(user.getAffiliation());
			affiliationComboBox.setSelectedItem(aff);
			
			IdTrackerOrganization organization = IDTDataCache.getOrganizations().stream().
					filter(o -> o.getId().equals(user.getOrganizationId())).
					findFirst().orElse(null);
			organizationComboBox.setSelectedItem(organization);
			emailTextField.setText(user.getEmail());
			phoneTextField.setText(CommonFormFieldVerifier.formatPhoneNumber(user.getPhone()));
			chckbxSuperuser.setSelected(user.isSuperUser());
			chckbxActive.setSelected(user.isActive());
		}
		pack();
	}
	
	public String getFirstName() {
		return firstNameTextField.getText().trim();
	}
	
	public String getLastName() {
		return lastNameTextField.getText().trim();
	}

	public String getUserName() {
		return userNameTextField.getText().trim();
	}
	
	public UserAffiliation getUserAffiliation() {
		return (UserAffiliation)affiliationComboBox.getSelectedItem();
	}
	
	public IdTrackerOrganization getOrganization() {
		return (IdTrackerOrganization)organizationComboBox.getSelectedItem();
	}
	
	public String getEmail() {
		return emailTextField.getText().trim();
	}
	
	public String getPhone() {
		return phoneTextField.getText().trim();
	}
	
	public boolean isSuperUser() {
		return chckbxSuperuser.isSelected();
	}
	
	public boolean isActive() {
		return chckbxActive.isSelected();
	}

	/**
	 * @return the user
	 */
	public LIMSUser getUser() {
		return user;
	}
}














