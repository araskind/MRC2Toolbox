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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.mobph;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.idt.ChromatographyDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class DockableMobilePhaseManagerPanel extends AbstractIDTrackerLimsPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("editDataAcquisitionMethod", 16);
	private static final Icon addMobilePhaseIcon = GuiUtils.getIcon("newMobilePhase", 24);
	private static final Icon editMobilePhaseIcon = GuiUtils.getIcon("editMobilePhase", 24);
	private static final Icon deleteMobilePhaseIcon = GuiUtils.getIcon("deleteMobilePhase", 24);

	private MobilePhaseManagerToolbar toolbar;
	private MobilePhaseTable mobilePhaseTable;
	private MobilePhaseEditorDialog mobilePhaseEditorDialog;

	public DockableMobilePhaseManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableMobilePhaseManagerPanel", 
				componentIcon, "Mobile phases", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		this.idTrackerLimsManager = idTrackerLimsManager;

		toolbar = new MobilePhaseManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		mobilePhaseTable = new MobilePhaseTable();	
		mobilePhaseTable.addTablePopupMenu(
				new BasicTablePopupMenu(null, mobilePhaseTable, true));
		JScrollPane designScrollPane = new JScrollPane(mobilePhaseTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		
		mobilePhaseTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {
							MobilePhase mobilePhase = mobilePhaseTable.getSelectedMobilePhase();
							if(mobilePhase == null)
								return;
							
							showMobilePhaseEditor(mobilePhase);
						}											
					}
				});
		initActions();
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_MOBILE_PHASE_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_MOBILE_PHASE_DIALOG_COMMAND.getName(), 
				addMobilePhaseIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_MOBILE_PHASE_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_MOBILE_PHASE_DIALOG_COMMAND.getName(), 
				editMobilePhaseIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_MOBILE_PHASE_COMMAND.getName(),
				MainActionCommands.DELETE_MOBILE_PHASE_COMMAND.getName(), 
				deleteMobilePhaseIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
	
		if(!isConnected())
			return;
		
		super.actionPerformed(e);
		
		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.ADD_MOBILE_PHASE_DIALOG_COMMAND.getName()))
			showMobilePhaseEditor(null);
		
		if(command.equals(MainActionCommands.ADD_MOBILE_PHASE_COMMAND.getName()))
			addNewMobilePhase();
		
		if(command.equals(MainActionCommands.EDIT_MOBILE_PHASE_DIALOG_COMMAND.getName())) {
			
			MobilePhase mobilePhase = mobilePhaseTable.getSelectedMobilePhase();
			if(mobilePhase == null)
				return;
			
			showMobilePhaseEditor(mobilePhase);
		}
		if(command.equals(MainActionCommands.EDIT_MOBILE_PHASE_COMMAND.getName()))
			editMobilePhase();
		
		if(e.getActionCommand().equals(MainActionCommands.DELETE_MOBILE_PHASE_COMMAND.getName())) {

			if(mobilePhaseTable.getSelectedMobilePhase() == null)
				return;
			
			reauthenticateAdminCommand(MainActionCommands.DELETE_MOBILE_PHASE_COMMAND.getName());
		}
	}
	
	private void addNewMobilePhase() {

		String mpName = mobilePhaseEditorDialog.getmobilePhaseDescription();
		if(mpName.isEmpty()) {
			MessageDialog.showErrorMsg("Please provide a description!", mobilePhaseEditorDialog);
			return;
		}		
		MobilePhase newPhase = new MobilePhase(mpName);
		if(!mobilePhaseEditorDialog.getmobilePhaseSynonyms().isEmpty())
			newPhase.getSynonyms().addAll(mobilePhaseEditorDialog.getmobilePhaseSynonyms());

		MobilePhase existing = getExistingMobilePhaseByComparingNameAndSynonyms(newPhase);
		if(existing != null) {
			
			String message = "Mobile phase with the name \"" + existing.getName() + "\n";
			if(!existing.getSynonyms().isEmpty()) {
				
				message += " and the following synonyms:\n";
				for(String synonym : existing.getSynonyms())					
					message += "\"" + synonym + "\"\n";								
			}
			message += "already exists";
			MessageDialog.showErrorMsg(message, mobilePhaseEditorDialog);
			return;
		}
		try {
			ChromatographyDatabaseUtils.addNewMobilePhase(newPhase);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mobilePhaseEditorDialog.dispose();
		IDTDataCache.refreshMobilePhaseList();
		mobilePhaseTable.setTableModelFromMobilePhaseCollection(IDTDataCache.getMobilePhaseList());
	}
	
	private MobilePhase getExistingMobilePhaseByComparingNameAndSynonyms(MobilePhase newPhase) {
		
		MobilePhase existing = IDTDataCache.getMobilePhaseByNameOrSynonym(newPhase.getName());
		if(existing != null) {
			return existing;
		}
		else {
			if(!newPhase.getSynonyms().isEmpty()) {
				
				for(String synonym : newPhase.getSynonyms()) {
					
					existing = IDTDataCache.getMobilePhaseByNameOrSynonym(synonym);
					if(existing != null)
						return existing;
					
				}
			}
		}
		return null;
	}

	private void editMobilePhase() {

		MobilePhase mobPhase = mobilePhaseTable.getSelectedMobilePhase();
		if(mobPhase == null)
			return;
		
		String mpName = mobilePhaseEditorDialog.getmobilePhaseDescription();
		if(mpName.isEmpty()) {
			MessageDialog.showErrorMsg("Please provide a description!", mobilePhaseEditorDialog);
			return;
		}
		mobPhase.getSynonyms().clear();
		if(!mobilePhaseEditorDialog.getmobilePhaseSynonyms().isEmpty())
			mobPhase.getSynonyms().addAll(mobilePhaseEditorDialog.getmobilePhaseSynonyms());
		
		MobilePhase existing = getExistingMobilePhaseByComparingNameAndSynonyms(mobPhase);
		if(existing != null && !mobPhase.getId().equals(existing.getId())) {
			
			String message = "A different mobile phase with the name \"" + existing.getName() + "\n";
			if(!existing.getSynonyms().isEmpty()) {
				
				message += " and the following synonyms:\n";
				for(String synonym : existing.getSynonyms())					
					message += "\"" + synonym + "\"\n";							
			}
			message += "already exists";
			MessageDialog.showErrorMsg(message, mobilePhaseEditorDialog);
			return;
		}
		mobPhase.setName(mpName);
		try {
			ChromatographyDatabaseUtils.editMobilePhase(mobPhase);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mobilePhaseEditorDialog.dispose();
		IDTDataCache.refreshMobilePhaseList();
		mobilePhaseTable.setTableModelFromMobilePhaseCollection(IDTDataCache.getMobilePhaseList());		
	}

	private void deleteMobilePhase() {

		MobilePhase mobPhase = mobilePhaseTable.getSelectedMobilePhase();
		if(mobPhase == null)
			return;
		
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to delete selected mobile phase?", 
				this.getContentPane());
		if(res == JOptionPane.YES_OPTION) {
			
			try {
				ChromatographyDatabaseUtils.deleteMobilePhase(mobPhase);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCache.refreshMobilePhaseList();
			mobilePhaseTable.setTableModelFromMobilePhaseCollection(IDTDataCache.getMobilePhaseList());
		}	
	}

	private void showMobilePhaseEditor(MobilePhase mobilePhase) {
		
		mobilePhaseEditorDialog = new MobilePhaseEditorDialog(mobilePhase, this);
		mobilePhaseEditorDialog.setLocationRelativeTo(this.getContentPane());
		mobilePhaseEditorDialog.setVisible(true);
	}

	public void loadMobilePhaseCollection(Collection<MobilePhase>phases) {
		mobilePhaseTable.setTableModelFromMobilePhaseCollection(phases);
	}

	public void loadMobilePhases() {
		mobilePhaseTable.setTableModelFromMobilePhaseCollection(IDTDataCache.getMobilePhaseList());
	}

	public synchronized void clearPanel() {
		mobilePhaseTable.clearTable();
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

	@Override
	protected void executeAdminCommand(String command) {

		if(command.equals(MainActionCommands.DELETE_MOBILE_PHASE_COMMAND.getName()))
			deleteMobilePhase();
	}
}




















