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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.vendor;

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

import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableVendorManagerPanel extends AbstractIDTrackerLimsPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("vendor", 16);
	private static final Icon addVendorIcon = GuiUtils.getIcon("addVendor", 24);
	private static final Icon editVendorIcon = GuiUtils.getIcon("editVendor", 24);
	private static final Icon deleteVendorIcon = GuiUtils.getIcon("deleteVendor", 24);
	private VendorManagerToolbar toolbar;
	private VendorTable vendorTable;
	private VendorEditorDialog vendorEditorDialog;
	
	public DockableVendorManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableVendorManagerPanel", 
				componentIcon, "Vendors", null, Permissions.MIN_MAX_STACK);

		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new VendorManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		vendorTable =  new VendorTable();
		vendorTable.addTablePopupMenu(
				new BasicTablePopupMenu(null, vendorTable, true));
		vendorTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							loadVendorInEditor();
						}
					}
				});
		JScrollPane designScrollPane = new JScrollPane(vendorTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		
		initActions();
	}

	@Override
	protected void initActions() {

		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_VENDOR_COMMAND.getName(),
				MainActionCommands.ADD_VENDOR_COMMAND.getName(), 
				addVendorIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_VENDOR_COMMAND.getName(),
				MainActionCommands.EDIT_VENDOR_COMMAND.getName(), 
				editVendorIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_VENDOR_COMMAND.getName(),
				MainActionCommands.DELETE_VENDOR_COMMAND.getName(), 
				deleteVendorIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(!isConnected())
			return;

		super.actionPerformed(e);
		
		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.ADD_VENDOR_COMMAND.getName())) {
			showVendorEditor(null);
		}
		if(command.equals(MainActionCommands.EDIT_VENDOR_COMMAND.getName()))
			loadVendorInEditor();
					
		if(command.equals(MainActionCommands.SAVE_VENDOR_DETAILS_COMMAND.getName()))
			saveVendorDetails();
		
		if(command.equals(MainActionCommands.DELETE_VENDOR_COMMAND.getName())) {	

			if(vendorTable.getSelectedVendor() == null)
				return;
			
			reauthenticateAdminCommand(
					MainActionCommands.DELETE_VENDOR_COMMAND.getName());
		}
	}
	
	private void loadVendorInEditor() {
		
		Manufacturer vendor = vendorTable.getSelectedVendor();
		if(vendor != null)
			showVendorEditor(vendor);
	}

	private void saveVendorDetails() {
		
		Collection<String> errors = 
				vendorEditorDialog.validateSoftware();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					vendorEditorDialog);
			return;
		}
		//	Add new vendor
		if(vendorEditorDialog.getVendor() == null) {
			
			Manufacturer newVendor = new Manufacturer(
					null, 
					vendorEditorDialog.getVendorName(), 
					vendorEditorDialog.getVendorWebAddress());
			try {
				IDTUtils.addNewManufacturer(newVendor);
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
		else {	//	Edit existing vendor
			Manufacturer vendorToEdit = vendorEditorDialog.getVendor();
			vendorToEdit.setName(vendorEditorDialog.getVendorName());	
			vendorToEdit.setCatalogWebAddress(vendorEditorDialog.getVendorWebAddress());
			try {
				IDTUtils.updateManufacturer(vendorToEdit);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		IDTDataCache.refreshManufacturers();
		loadVendorList();
		vendorEditorDialog.dispose();
	}

	private void deleteVendor() {
		
		Manufacturer vendor = vendorTable.getSelectedVendor();
		if(vendor == null)
			return;
		
		if(!MRC2ToolBoxCore.getIdTrackerUser().isSuperUser()) {
			MessageDialog.showErrorMsg(
					"You need to have administrative priviledge\n"
					+ "to delete manufacturer/vendor from the database.", 
					this.getContentPane());
			return;
		}
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to delete vendor/manufacturer \"" + vendor.getName() +"\"?", 
				this.getContentPane());
		if(res != JOptionPane.YES_OPTION)
			return;
		
		try {
			IDTUtils.deleteManufacturer(vendor);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		idTrackerLimsManager.refreshVendorList();		
	}

	private void showVendorEditor(Manufacturer manufacturer) {

		vendorEditorDialog = new VendorEditorDialog(this, manufacturer);
		vendorEditorDialog.setLocationRelativeTo(this.getContentPane());
		vendorEditorDialog.setVisible(true);
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

	public void loadVendorList() {
		vendorTable.setTableModelFromManufacturers(
				IDTDataCache.getManufacturers());
	}

	@Override
	protected void executeAdminCommand(String command) {
		
		if(command.equals(MainActionCommands.DELETE_VENDOR_COMMAND.getName()))
			deleteVendor();		
	}
}
