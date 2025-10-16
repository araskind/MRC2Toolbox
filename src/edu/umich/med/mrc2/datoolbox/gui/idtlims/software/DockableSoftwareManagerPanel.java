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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.software;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableSoftwareManagerPanel extends AbstractIDTrackerLimsPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("software", 16);
	private static final Icon addSoftwareIcon = GuiUtils.getIcon("addSoftware", 24);
	private static final Icon editSoftwareIcon = GuiUtils.getIcon("editSoftware", 24);
	private static final Icon deleteSoftwareIcon = GuiUtils.getIcon("deleteSoftware", 24);
	private SoftwareManagerToolbar toolbar;
	private SoftwareTable softwareTable;
	private SoftwareEditorDialog softwareEditorDialog;
	
	public DockableSoftwareManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableSoftwareManagerPanel", 
				componentIcon, "Software", null, Permissions.MIN_MAX_STACK);

		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new SoftwareManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		softwareTable =  new SoftwareTable();
		softwareTable.addTablePopupMenu(
				new BasicTablePopupMenu(null, softwareTable, true));
		softwareTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							loadSoftwareIntoEditor();
						}
					}
				});
		JScrollPane designScrollPane = new JScrollPane(softwareTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		
		initActions();
	}

	@Override
	protected void initActions() {

		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_SOFTWARE_COMMAND.getName(),
				MainActionCommands.ADD_SOFTWARE_COMMAND.getName(), 
				addSoftwareIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_SOFTWARE_COMMAND.getName(),
				MainActionCommands.EDIT_SOFTWARE_COMMAND.getName(), 
				editSoftwareIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_SOFTWARE_COMMAND.getName(),
				MainActionCommands.DELETE_SOFTWARE_COMMAND.getName(), 
				deleteSoftwareIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(!isConnected())
			return;

		super.actionPerformed(e);
		
		String command = e.getActionCommand();	
		
		if(command.equals(MainActionCommands.ADD_SOFTWARE_COMMAND.getName()))
			showSoftwareEditor(null);
		
		if(command.equals(MainActionCommands.EDIT_SOFTWARE_COMMAND.getName())) 
			loadSoftwareIntoEditor();
		
		if(command.equals(MainActionCommands.SAVE_SOFTWARE_DETAILS_COMMAND.getName()))
			saveSoftwareDetails();
		
		if(command.equals(MainActionCommands.DELETE_SOFTWARE_COMMAND.getName())) {

			if(softwareTable.getSelectedSoftware() == null)
				return;
			
			reauthenticateAdminCommand(
					MainActionCommands.DELETE_SOFTWARE_COMMAND.getName());
		}		
	}
	
	private void loadSoftwareIntoEditor() {
		
		DataProcessingSoftware softwareItem = softwareTable.getSelectedSoftware();
		if(softwareItem != null)
			showSoftwareEditor(softwareItem);	
	}

	private void saveSoftwareDetails() {

		Collection<String> errors = 
				softwareEditorDialog.validateSoftware();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), softwareEditorDialog);
			return;
		}
		DataProcessingSoftware software = softwareEditorDialog.getSoftwareItem();
		//	New software
		if(software == null) {
			
			software = new DataProcessingSoftware(null, 
					softwareEditorDialog.getSoftwareType(),
					softwareEditorDialog.getSoftwareName(), 
					softwareEditorDialog.getSoftwareDescription(), 
					softwareEditorDialog.getSoftwareVendor());			
			try {
				IDTUtils.addNewSoftware(software);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {	//	Edit existing software
			software.setSoftwareType(softwareEditorDialog.getSoftwareType());
			software.setName(softwareEditorDialog.getSoftwareName());
			software.setDescription(softwareEditorDialog.getSoftwareDescription());
			software.setVendor(softwareEditorDialog.getSoftwareVendor());
			try {
				IDTUtils.updateSoftware(software);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		IDTDataCache.refreshSoftwareList();
		softwareTable.setTableModelFromSoftwareList(IDTDataCache.getSoftwareList());
		softwareEditorDialog.dispose();
	}

	private void deleteSoftware() {

		DataProcessingSoftware toDelete = softwareTable.getSelectedSoftware();
		if(toDelete == null)
			return;
		
		if(!MRC2ToolBoxCore.getIdTrackerUser().isSuperUser()) {
			MessageDialog.showErrorMsg(
					"You need to have administrative priviledge\n"
					+ "to delete the software from the database.", 
					this.getContentPane());
			return;
		}	
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to delete software \"" + toDelete.getName() +"\"?", 
				this.getContentPane());
		if(res != JOptionPane.YES_OPTION)
			return;
		
		try {
			IDTUtils.deleteSoftware(toDelete);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IDTDataCache.refreshSoftwareList();
		softwareTable.setTableModelFromSoftwareList(IDTDataCache.getSoftwareList());
	}

	private void showSoftwareEditor(DataProcessingSoftware softwareItem) {

		softwareEditorDialog = 
				new SoftwareEditorDialog(this, softwareItem);
		softwareEditorDialog.setLocationRelativeTo(this.getContentPane());
		softwareEditorDialog.setVisible(true);
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub

	}

	public void loadSoftwareList() {
		softwareTable.setTableModelFromSoftwareList(
				IDTDataCache.getSoftwareList());
	}

	@Override
	protected void executeAdminCommand(String command) {

		if(command.equals(MainActionCommands.DELETE_SOFTWARE_COMMAND.getName()))
			deleteSoftware();		
	}
}
