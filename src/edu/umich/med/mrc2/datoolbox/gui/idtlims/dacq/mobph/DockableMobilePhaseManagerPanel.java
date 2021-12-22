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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.idt.ChromatographyUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class DockableMobilePhaseManagerPanel extends DefaultSingleCDockable implements ActionListener{

	private static final Icon componentIcon = GuiUtils.getIcon("editDataAcquisitionMethod", 16);

	private IDTrackerLimsManagerPanel idTrackerLimsManager;
	private MobilePhaseManagerToolbar toolbar;
	private MobilePhaseTable mobilePhaseTable;
	private MobilePhaseEditorDialog mobilePhaseEditorDialog;

	public DockableMobilePhaseManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super("DockableMobilePhaseManagerPanel", componentIcon, "Mobile phases", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		this.idTrackerLimsManager = idTrackerLimsManager;

		toolbar = new MobilePhaseManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		mobilePhaseTable = new MobilePhaseTable();	
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
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	
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
		
		if(command.equals(MainActionCommands.DELETE_MOBILE_PHASE_COMMAND.getName()))
			deleteMobilePhase();		
	}
	
	private void addNewMobilePhase() {

		String mpName = mobilePhaseEditorDialog.getmobilePhaseDescription();
		if(mpName.isEmpty()) {
			MessageDialog.showErrorMsg("Please provide a description!", mobilePhaseEditorDialog);
			return;
		}		
		MobilePhase newPhase = new MobilePhase(mpName);
		String mpid = null;
		try {
			mpid = ChromatographyUtils.getMobilePhaseId(newPhase);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		if(mpid != null) {
			MessageDialog.showErrorMsg("Mobile phase \"" + newPhase.getName() + 
					"\" already exists.", mobilePhaseEditorDialog);
			return;
		}
		try {
			ChromatographyUtils.addNewMobilePhase(newPhase);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mobilePhaseEditorDialog.dispose();
		IDTDataCash.refreshMobilePhaseList();
		mobilePhaseTable.setTableModelFromMobilePhaseCollection(IDTDataCash.getMobilePhaseList());
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
		//	No change in description
		if(mpName.equals(mobPhase.getName())) {
			mobilePhaseEditorDialog.dispose();
			return;
		}
		MobilePhase phaseToCheck = new MobilePhase(mobPhase.getId(), mpName);
		boolean hasNameConflict = false;
		try {
			hasNameConflict = ChromatographyUtils.hasNameConflict(phaseToCheck);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(hasNameConflict) {
			MessageDialog.showErrorMsg("Name \"" + mpName + 
					"\" is already assigned to a different mobile phase.", mobilePhaseEditorDialog);
			return;
		}
		mobPhase.setName(mpName);
		try {
			ChromatographyUtils.editMobilePhase(mobPhase);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mobilePhaseEditorDialog.dispose();
		IDTDataCash.refreshMobilePhaseList();
		mobilePhaseTable.setTableModelFromMobilePhaseCollection(IDTDataCash.getMobilePhaseList());
		
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
				ChromatographyUtils.deleteMobilePhase(mobPhase);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCash.refreshMobilePhaseList();
			mobilePhaseTable.setTableModelFromMobilePhaseCollection(IDTDataCash.getMobilePhaseList());
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
		mobilePhaseTable.setTableModelFromMobilePhaseCollection(IDTDataCash.getMobilePhaseList());
	}

	public synchronized void clearPanel() {
		mobilePhaseTable.clearTable();
	}
}




















