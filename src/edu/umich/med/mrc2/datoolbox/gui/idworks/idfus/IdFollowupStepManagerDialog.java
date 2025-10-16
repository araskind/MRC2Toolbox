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

package edu.umich.med.mrc2.datoolbox.gui.idworks.idfus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdFollowupUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class IdFollowupStepManagerDialog extends JDialog implements ActionListener{


	/**
	 * 
	 */
	private static final long serialVersionUID = 6617911751201844630L;
	private static final Icon stIcon = GuiUtils.getIcon("followUp", 32);
	private IdFollowupStepTable idFollowupStepTable;
	private IdFollowupStepManagerToolbar toolbar;
	private IdFollowupStepEditorDialog idFollowupStepEditorDialog;
	
	public IdFollowupStepManagerDialog() {
		super();
		setTitle("Identification follow-up step manager");
		setIconImage(((ImageIcon) stIcon).getImage());
		setPreferredSize(new Dimension(400, 400));
		setSize(new Dimension(400, 400));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		toolbar = new IdFollowupStepManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		idFollowupStepTable = new IdFollowupStepTable();
		getContentPane().add(new JScrollPane(idFollowupStepTable), BorderLayout.CENTER);
		idFollowupStepTable.setTableModelFromFollowupStepList(
				IDTDataCache.getMsFeatureIdentificationFollowupStepList());
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.ADD_ID_FOLLOWUP_STEP_DIALOG_COMMAND.getName()))
			showFollowupStepEditor(null);
		
		if(command.equals(MainActionCommands.ADD_ID_FOLLOWUP_STEP_COMMAND.getName()))
			addNewIdFollowupStep();
	
		if(command.equals(MainActionCommands.EDIT_ID_FOLLOWUP_STEP_DIALOG_COMMAND.getName())) {
			
			MSFeatureIdentificationFollowupStep step = idFollowupStepTable.getSelectedFollowupStep();
			if(step == null)
				return;
			else
				showFollowupStepEditor(step);
		}
		
		if(command.equals(MainActionCommands.EDIT_ID_FOLLOWUP_STEP_COMMAND.getName()))
			editIdFollowupStep();
		
		if(command.equals(MainActionCommands.DELETE_ID_FOLLOWUP_STEP_COMMAND.getName()))
			deleteIdFollowupStep();
	}

	private void showFollowupStepEditor(MSFeatureIdentificationFollowupStep step) {

		idFollowupStepEditorDialog = new IdFollowupStepEditorDialog(step, this);
		idFollowupStepEditorDialog.setLocationRelativeTo(this);
		idFollowupStepEditorDialog.setVisible(true);	
	}

	private void addNewIdFollowupStep() {

		Collection<String>errors = validateFollowupStep();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), idFollowupStepEditorDialog);
			return;
		}
		MSFeatureIdentificationFollowupStep step = 
				new MSFeatureIdentificationFollowupStep(idFollowupStepEditorDialog.getStepName());
		try {
			IdFollowupUtils.addNewMSFeatureIdentificationFollowupStep(step);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IDTDataCache.refreshMsFeatureIdentificationFollowupStepList();
		idFollowupStepTable.setTableModelFromFollowupStepList(
				IDTDataCache.getMsFeatureIdentificationFollowupStepList());
		idFollowupStepEditorDialog.dispose();
	}

	private void editIdFollowupStep() {

		Collection<String>errors = validateFollowupStep();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), idFollowupStepEditorDialog);
			return;
		}
		MSFeatureIdentificationFollowupStep step = idFollowupStepEditorDialog.getStep();
		step.setName(idFollowupStepEditorDialog.getStepName());
		if(step != null) {
			try {
				IdFollowupUtils.editMSFeatureIdentificationFollowupStep(step);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IDTDataCache.refreshMsFeatureIdentificationFollowupStepList();
		idFollowupStepTable.setTableModelFromFollowupStepList(
				IDTDataCache.getMsFeatureIdentificationFollowupStepList());
		idFollowupStepEditorDialog.dispose();
	}
	
	private Collection<String>validateFollowupStep(){
		
		Collection<String>errors = new ArrayList<String>();
		MSFeatureIdentificationFollowupStep step = idFollowupStepEditorDialog.getStep();
		String name = idFollowupStepEditorDialog.getStepName();
		if(name.isEmpty()) {
			errors.add("Name can not be empty.");
			return errors;
		}		
		//	New status
		if(step == null) {
			
			MSFeatureIdentificationFollowupStep sameName = 
					IDTDataCache.getMsFeatureIdentificationFollowupStepList().stream().
					filter(s -> s.getName().equals(name)).findFirst().orElse(null);
			
			if(sameName != null)
				errors.add("Follow-up step with the same name already exists.");
		}
		else {	//	Existing status
			String currentId = step.getId();
			MSFeatureIdentificationFollowupStep sameName = 
					IDTDataCache.getMsFeatureIdentificationFollowupStepList().stream().
					filter(s -> !s.getId().equals(currentId)).
					filter(s -> s.getName().equals(name)).findFirst().orElse(null);
			
			if(sameName != null)
				errors.add("Different follow-up step with the same name already exists.");
		}		
		return errors;		
	}

	private void deleteIdFollowupStep() {

		MSFeatureIdentificationFollowupStep step = idFollowupStepTable.getSelectedFollowupStep();
		if(step == null)
			return;
		
		if(!IDTUtils.isSuperUser(this))
			return;
		
		String yesNoQuestion = 
				"Do you want to delete selected identification follow-up step?\n"
				+ "It will be cleared from all the features it was assigned to.";
		int res = MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this);
		if(res == JOptionPane.YES_OPTION) {
			
			try {
				IdFollowupUtils.deleteMSFeatureIdentificationFollowupStep(step);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IDTDataCache.refreshMsFeatureIdentificationFollowupStepList();
		idFollowupStepTable.setTableModelFromFollowupStepList(
				IDTDataCache.getMsFeatureIdentificationFollowupStepList());
	}
}


