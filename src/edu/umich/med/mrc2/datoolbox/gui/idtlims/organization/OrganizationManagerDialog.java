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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class OrganizationManagerDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 5685218262277207511L;
	private OrganizationManagerToolbar toolbar;
	private OrganizationTable userTable;
	private OrganizationEditorDialog organizationEditorDialog;

	private static final Icon organizationIcon = GuiUtils.getIcon("organization", 32);

	public OrganizationManagerDialog() {

		super();
		setTitle("Manage organizations");
		setIconImage(((ImageIcon) organizationIcon).getImage());
		setPreferredSize(new Dimension(800, 640));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 480));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		toolbar  = new OrganizationManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		IDTDataCash.refreshOrganizationList();
		userTable = new OrganizationTable();
		userTable.setTableModelFromOrganizations(IDTDataCash.getOrganizations());
		JScrollPane scrollPane = new JScrollPane(userTable);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(toolbar);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if(command.equals(MainActionCommands.ADD_ORGANIZATION_DIALOG_COMMAND.getName()))
			showAddOrganizationDialog();

		if(command.equals(MainActionCommands.EDIT_ORGANIZATION_DIALOG_COMMAND.getName()))
			showEditOrganizationDialog();

		if(command.equals(MainActionCommands.ADD_ORGANIZATION_COMMAND.getName()))
			addOrganization();

		if(command.equals(MainActionCommands.EDIT_ORGANIZATION_COMMAND.getName()))
			editOrganization();

		if(command.equals(MainActionCommands.DELETE_ORGANIZATION_COMMAND.getName()))
			deleteSelectedOrganization();
	}

	private void showAddOrganizationDialog() {

		organizationEditorDialog = new OrganizationEditorDialog(null, this);
		organizationEditorDialog.setLocationRelativeTo(this);
		organizationEditorDialog.setVisible(true);
	}

	private void addOrganization() {

		Collection<String>errors = organizationEditorDialog.validateOrganizationData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), organizationEditorDialog);
			return;
		}
		IdTrackerOrganization organization = new IdTrackerOrganization(
				organizationEditorDialog.getOrganizationName(), 
				organizationEditorDialog.getMailingAddress(), 
				organizationEditorDialog.getDepartment(), 
				organizationEditorDialog.getLaboratory(),
				organizationEditorDialog.getPrincipalInvestigator(), 
				organizationEditorDialog.getContactPerson());		
		try {
			LIMSUtils.addNewOrganization(organization);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		IDTDataCash.refreshOrganizationList();
		userTable.setTableModelFromOrganizations(IDTDataCash.getOrganizations());			
		organizationEditorDialog.dispose();
	}

	private void showEditOrganizationDialog() {

		IdTrackerOrganization organization = userTable.getSelectedOrganization();
		if(organization == null)
			return;
		
		organizationEditorDialog = new OrganizationEditorDialog(organization, this);
		organizationEditorDialog.setLocationRelativeTo(this);
		organizationEditorDialog.setVisible(true);
	}
	
	private void editOrganization() {
		
		Collection<String>errors = organizationEditorDialog.validateOrganizationData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), organizationEditorDialog);
			return;
		}	
		IdTrackerOrganization organization = organizationEditorDialog.getOrganization();
		organization.setName(organizationEditorDialog.getOrganizationName());
		organization.setDepartment(organizationEditorDialog.getDepartment());
		organization.setLaboratory(organizationEditorDialog.getLaboratory());
		organization.setPrincipalInvestigator(organizationEditorDialog.getPrincipalInvestigator());
		organization.setContactPerson(organizationEditorDialog.getContactPerson());
		organization.setAddress(organizationEditorDialog.getMailingAddress());
		organization.setMailingAddress(organizationEditorDialog.getMailingAddress());	
		try {
			LIMSUtils.editOrganization(organization);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IDTDataCash.refreshOrganizationList();
		userTable.setTableModelFromOrganizations(IDTDataCash.getOrganizations());			
		organizationEditorDialog.dispose();
	}

	private void deleteSelectedOrganization() {

		IdTrackerOrganization organization = userTable.getSelectedOrganization();
		if(organization == null)
			return;
		
		String yesNoQuestion = "<HTML>Do you want to delete organization <B>" + organization.getName() + "</B>?";
		int res = MessageDialog.showChoiceWithWarningMsg(yesNoQuestion , this);
		if(res == JOptionPane.YES_OPTION) {
			try {
				LIMSUtils.deleteOrganization(organization);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCash.refreshOrganizationList();
			try {			
				userTable.setTableModelFromOrganizations(IDTDataCash.getOrganizations());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
























