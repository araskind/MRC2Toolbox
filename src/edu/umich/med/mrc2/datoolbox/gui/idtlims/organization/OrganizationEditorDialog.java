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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.organization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.user.UserSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class OrganizationEditorDialog extends JDialog implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 7684989595475342241L;

	private static final Icon editOrganizationIcon = GuiUtils.getIcon("editOrganization", 32);
	private static final Icon addOrganizationIcon = GuiUtils.getIcon("addOrganizationIcon", 32);

	private IdTrackerOrganization organization;
	private LIMSUser principalInvestigator;
	private LIMSUser contactPerson;
	
	private JButton btnSave;
	private JTextField organizationNameTextField;
	private JTextField departmentTextField;
	private JLabel idValueLabel;
	private JLabel piNameLabel;
	private JTextField laboratoryTextField;
	private JLabel contactPersonNameLabel;
	private JTextArea mailingAddressTextArea;
	private String selectUserLevel;
	private UserSelectorDialog userSelectorDialog;

	public OrganizationEditorDialog(IdTrackerOrganization organization, ActionListener actionListener) {
		super();
		setPreferredSize(new Dimension(600, 400));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.organization = organization;
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 114, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		dataPanel.add(lblId, gbc_lblId);

		idValueLabel = new JLabel("");
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.anchor = GridBagConstraints.WEST;
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel lblCreatedBy = new JLabel("Name");
		GridBagConstraints gbc_lblCreatedBy = new GridBagConstraints();
		gbc_lblCreatedBy.anchor = GridBagConstraints.EAST;
		gbc_lblCreatedBy.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatedBy.gridx = 0;
		gbc_lblCreatedBy.gridy = 1;
		dataPanel.add(lblCreatedBy, gbc_lblCreatedBy);
		
		organizationNameTextField = new JTextField();
		GridBagConstraints gbc_organizationNameTextField = new GridBagConstraints();
		gbc_organizationNameTextField.gridwidth = 2;
		gbc_organizationNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_organizationNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_organizationNameTextField.gridx = 1;
		gbc_organizationNameTextField.gridy = 1;
		dataPanel.add(organizationNameTextField, gbc_organizationNameTextField);
		organizationNameTextField.setColumns(10);

		JLabel lblName = new JLabel("Department");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 2;
		dataPanel.add(lblName, gbc_lblName);

		departmentTextField = new JTextField();
		GridBagConstraints gbc_departmentTextField = new GridBagConstraints();
		gbc_departmentTextField.gridwidth = 2;
		gbc_departmentTextField.insets = new Insets(0, 0, 5, 0);
		gbc_departmentTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_departmentTextField.gridx = 1;
		gbc_departmentTextField.gridy = 2;
		dataPanel.add(departmentTextField, gbc_departmentTextField);
		departmentTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Laboratory");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		laboratoryTextField = new JTextField();
		GridBagConstraints gbc_laboratoryTextField = new GridBagConstraints();
		gbc_laboratoryTextField.gridwidth = 2;
		gbc_laboratoryTextField.insets = new Insets(0, 0, 5, 0);
		gbc_laboratoryTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_laboratoryTextField.gridx = 1;
		gbc_laboratoryTextField.gridy = 3;
		dataPanel.add(laboratoryTextField, gbc_laboratoryTextField);
		laboratoryTextField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Principal investigator");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 4;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		piNameLabel = new JLabel("");
		GridBagConstraints gbc_piNameLabel = new GridBagConstraints();
		gbc_piNameLabel.anchor = GridBagConstraints.NORTH;
		gbc_piNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_piNameLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_piNameLabel.gridx = 1;
		gbc_piNameLabel.gridy = 4;
		dataPanel.add(piNameLabel, gbc_piNameLabel);
		
		JButton selectPIButton = new JButton("Select");
		selectPIButton.setActionCommand(MainActionCommands.SELECT_PI_COMMAND.getName());
		selectPIButton.addActionListener(this);	
		GridBagConstraints gbc_selectPIButton = new GridBagConstraints();
		gbc_selectPIButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectPIButton.gridx = 2;
		gbc_selectPIButton.gridy = 4;
		dataPanel.add(selectPIButton, gbc_selectPIButton);
		
		JLabel lblNewLabel_2 = new JLabel("Contact person");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 5;
		dataPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		contactPersonNameLabel = new JLabel("");
		GridBagConstraints gbc_contactPersonNameLabel = new GridBagConstraints();
		gbc_contactPersonNameLabel.anchor = GridBagConstraints.NORTH;
		gbc_contactPersonNameLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_contactPersonNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_contactPersonNameLabel.gridx = 1;
		gbc_contactPersonNameLabel.gridy = 5;
		dataPanel.add(contactPersonNameLabel, gbc_contactPersonNameLabel);
		
		JButton selectContactButton = new JButton("Select");
		selectContactButton.setActionCommand(MainActionCommands.SELECT_CONTACT_PERSON_COMMAND.getName());
		selectContactButton.addActionListener(this);
		GridBagConstraints gbc_selectContactButton = new GridBagConstraints();
		gbc_selectContactButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectContactButton.gridx = 2;
		gbc_selectContactButton.gridy = 5;
		dataPanel.add(selectContactButton, gbc_selectContactButton);
		
		JLabel lblNewLabel_3 = new JLabel("Mailing address");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 6;
		dataPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		mailingAddressTextArea = new JTextArea();
		GridBagConstraints gbc_mailingAddressTextArea = new GridBagConstraints();
		gbc_mailingAddressTextArea.insets = new Insets(0, 0, 0, 5);
		gbc_mailingAddressTextArea.fill = GridBagConstraints.BOTH;
		gbc_mailingAddressTextArea.gridx = 1;
		gbc_mailingAddressTextArea.gridy = 6;
		dataPanel.add(mailingAddressTextArea, gbc_mailingAddressTextArea);

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

		loadOrganizationData();
		pack();
	}

	private void loadOrganizationData() {

		if(organization == null) {
			setTitle("Add New Organization");
			setIconImage(((ImageIcon) addOrganizationIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_ORGANIZATION_COMMAND.getName());
		}
		else {
			setTitle("Edit information for " + organization.getName());
			setIconImage(((ImageIcon) editOrganizationIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_ORGANIZATION_COMMAND.getName());		
			idValueLabel.setText(organization.getId());
			organizationNameTextField.setText(organization.getName());
			departmentTextField.setText(organization.getDepartment());
			laboratoryTextField.setText(organization.getLaboratory());
			principalInvestigator = organization.getPrincipalInvestigator();
			piNameLabel.setText(organization.getPrincipalInvestigator().getInfo());
			contactPerson = organization.getContactPerson();
			contactPersonNameLabel.setText(organization.getContactPerson().getInfo());
			mailingAddressTextArea.setText(organization.getMailingAddress());
		}		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SELECT_PI_COMMAND.getName()) ||
				command.equals(MainActionCommands.SELECT_CONTACT_PERSON_COMMAND.getName())) {
			
			selectUserLevel = command;
			userSelectorDialog = new UserSelectorDialog(this);
			userSelectorDialog.setLocationRelativeTo(this);
			userSelectorDialog.setVisible(true);
		}
		if(command.equals(MainActionCommands.SELECT_USER_COMMAND.getName())) {
			
			LIMSUser user = userSelectorDialog.getSelectedUser();
			if(user == null)
				return;
			
			if(selectUserLevel.equals(MainActionCommands.SELECT_PI_COMMAND.getName())){
				principalInvestigator = user;
				piNameLabel.setText(principalInvestigator.getInfo());
			}
			if(selectUserLevel.equals(MainActionCommands.SELECT_CONTACT_PERSON_COMMAND.getName())){
				contactPerson = user;
				contactPersonNameLabel.setText(contactPerson.getInfo());
			}
			userSelectorDialog.dispose();
		}
	}	

	public IdTrackerOrganization getOrganization() {
		return organization;
	}

	public String getOrganizationName() {
		return organizationNameTextField.getText().trim();
	}
	
	public String getDepartment() {
		return departmentTextField.getText().trim();
	}
	
	public String getLaboratory() {
		return laboratoryTextField.getText().trim();
	}
	
	public LIMSUser getPrincipalInvestigator() {
		return principalInvestigator;
	}
	
	public LIMSUser getContactPerson() {
		return contactPerson;
	}

	public String getMailingAddress() {
		return mailingAddressTextArea.getText().trim();
	}
	
	public Collection<String>validateOrganizationData(){
				
		Collection<String>errors = new ArrayList<String>();

		if(principalInvestigator == null)
			errors.add("Principal investigator not specified.");

		if(contactPerson == null)
			errors.add("Contact person not specified.");
		
		String name = getOrganizationName();
		if(name.isEmpty())
			errors.add("Organization name not specified.");

		if(getDepartment().isEmpty())
			errors.add("Department name not specified.");
		
		if(getLaboratory().isEmpty())
			errors.add("Laboratory name not specified.");
		
		if(getMailingAddress().isEmpty())
			errors.add("Mailing address not specified.");
		
		//	Check for name conflicts
		
		if(!name.isEmpty()) {
			
			if(this.organization == null) {
				IdTrackerOrganization existing = 
						IDTDataCash.getOrganizations().stream().
							filter(o -> o.getName().equals(name)).
							findFirst().orElse(null);
				if(existing != null) 
					errors.add("Another organization with this name already exists.");
			}
			else {
				String id = organization.getId();
				IdTrackerOrganization existing = 
						IDTDataCash.getOrganizations().stream().
							filter(o -> !o.getId().equals(id)).
							filter(o -> o.getName().equals(name)).
							findFirst().orElse(null);
				if(existing != null) 
					errors.add("Another organization with this name already exists.");
			}
		}
		return errors;
	}
}











