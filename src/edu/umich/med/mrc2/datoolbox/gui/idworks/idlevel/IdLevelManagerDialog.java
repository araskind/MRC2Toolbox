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

package edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IdLevelUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.IdentificationLevelEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.IdentificationLevelEventListener;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class IdLevelManagerDialog extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7327164634118333171L;
	private static final Icon stIcon = GuiUtils.getIcon("idStatusManager", 32);
	private IdLevelTable idLevelTable;
	private IdLevelManagerToolbar toolbar;
	private IdLevelEditorDialog idLevelEditorDialog;
	private Set<IdentificationLevelEventListener> eventListeners;
	
	public IdLevelManagerDialog() {
		super();
		setTitle("Identification level manager");
		setIconImage(((ImageIcon) stIcon).getImage());
		setPreferredSize(new Dimension(400, 400));
		setSize(new Dimension(400, 400));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		toolbar = new IdLevelManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		idLevelTable = new IdLevelTable();
		getContentPane().add(new JScrollPane(idLevelTable), BorderLayout.CENTER);
		idLevelTable.setTableModelFromLevelList(
				IDTDataCache.getMsFeatureIdentificationLevelList());
		
		eventListeners = ConcurrentHashMap.newKeySet();
		pack();
	}
	
	@Override
	public void dispose() {
		eventListeners.clear();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.ADD_ID_LEVEL_DIALOG_COMMAND.getName()))
			showLevelEditor(null);
		
		if(command.equals(MainActionCommands.ADD_ID_LEVEL_COMMAND.getName()))
			addNewIdLevel();
	
		if(command.equals(MainActionCommands.EDIT_ID_LEVEL_DIALOG_COMMAND.getName())) {
			
			MSFeatureIdentificationLevel level = idLevelTable.getSelectedLevel();
			if(level == null)
				return;
			else if(level.isLocked()) {
				MessageDialog.showWarningMsg("ID level \"" + 
						level.getName() + "\" is locked and can not be modified.", this);
				return;
			}
			else
				showLevelEditor(level);
		}		
		if(command.equals(MainActionCommands.EDIT_ID_LEVEL_COMMAND.getName()))
			editIdLevel();
		
		if(command.equals(MainActionCommands.DELETE_ID_LEVEL_COMMAND.getName()))
			deleteIdLevel();
	}


	private void showLevelEditor(MSFeatureIdentificationLevel status) {

		idLevelEditorDialog = new IdLevelEditorDialog(status, this);
		idLevelEditorDialog.setLocationRelativeTo(this);
		idLevelEditorDialog.setVisible(true);	
	}

	private void addNewIdLevel() {

		Collection<String>errors = validateLevel();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), idLevelEditorDialog);
			return;
		}
		MSFeatureIdentificationLevel level = 
				new MSFeatureIdentificationLevel(
						idLevelEditorDialog.getLevelName(),
						idLevelEditorDialog.getLevelRank());
		level.setColorCode(idLevelEditorDialog.getLevelColor());
		try {
			IdLevelUtils.addNewMSFeatureIdentificationLevel(level);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IDTDataCache.refreshMsFeatureIdentificationLevelList();
		idLevelTable.setTableModelFromLevelList(
				IDTDataCache.getMsFeatureIdentificationLevelList());
		idLevelEditorDialog.dispose();
		fireIdentificationLevelEvent();
	}

	private void editIdLevel() {

		Collection<String>errors = validateLevel();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), idLevelEditorDialog);
			return;
		}
		MSFeatureIdentificationLevel level = idLevelEditorDialog.getLevel();
		level.setName(idLevelEditorDialog.getLevelName());
		level.setRank(idLevelEditorDialog.getLevelRank());
		level.setColorCode(idLevelEditorDialog.getLevelColor());
		level.setShorcut(idLevelEditorDialog.getLevelShorcut());
		if(level != null) {
			try {
				IdLevelUtils.editMSFeatureIdentificationLevel(level);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IDTDataCache.refreshMsFeatureIdentificationLevelList();
		idLevelTable.setTableModelFromLevelList(
				IDTDataCache.getMsFeatureIdentificationLevelList());
		idLevelEditorDialog.dispose();
		fireIdentificationLevelEvent();
	}
	
	private Collection<String>validateLevel(){
		
		Collection<String>errors = new ArrayList<String>();
		MSFeatureIdentificationLevel level = idLevelEditorDialog.getLevel();

		String name = idLevelEditorDialog.getLevelName();
		int rank = idLevelEditorDialog.getLevelRank();
		if(name.isEmpty()) {
			errors.add("Name can not be empty.");
			return errors;
		}	
		String shortcut = idLevelEditorDialog.getLevelShorcut();
		if(shortcut != null && shortcut.length() > 1)
			errors.add("Sortcut should be a single capital letter");
		
		//	New status
		if(level == null) {
			
			MSFeatureIdentificationLevel sameRank = 
				IDTDataCache.getMsFeatureIdentificationLevelList().stream().
				filter(s -> (s.getRank() == rank)).findFirst().orElse(null);
			
			if(sameRank != null)
				errors.add("Level with the same rank already exists.");
			
			MSFeatureIdentificationLevel sameName = 
					IDTDataCache.getMsFeatureIdentificationLevelList().stream().
					filter(s -> s.getName().equals(name)).findFirst().orElse(null);
			
			if(sameName != null)
				errors.add("Level with the same name already exists.");
			
			if(shortcut != null) {
				
				MSFeatureIdentificationLevel sameShortcut = 
						IDTDataCache.getMsFeatureIdentificationLevelList().stream().
						filter(s -> Objects.nonNull(s.getShorcut())).
						filter(s -> s.getShorcut().equals(shortcut)).
						findFirst().orElse(null);		
				if(sameShortcut != null)
					errors.add("Shortcut \""+shortcut+"\" already exists.");
			}
		}
		else {	//	Existing status
			String currentId = level.getId();
			MSFeatureIdentificationLevel sameRank = 
					IDTDataCache.getMsFeatureIdentificationLevelList().stream().
					filter(s -> !s.getId().equals(currentId)).
					filter(s -> (s.getRank() == rank)).findFirst().orElse(null);
				
			if(sameRank != null)
				errors.add("Different status with the same rank already exists.");
			
			MSFeatureIdentificationLevel sameName = 
					IDTDataCache.getMsFeatureIdentificationLevelList().stream().
					filter(s -> !s.getId().equals(currentId)).
					filter(s -> s.getName().equals(name)).findFirst().orElse(null);
			
			if(sameName != null)
				errors.add("Different level with the same name already exists.");
			
			if(shortcut != null) {
				
				MSFeatureIdentificationLevel sameShortcut = 
						IDTDataCache.getMsFeatureIdentificationLevelList().stream().
						filter(s -> Objects.nonNull(s.getShorcut())).
						filter(s -> !s.getId().equals(currentId)).
						filter(s -> s.getShorcut().equals(shortcut)).
						findFirst().orElse(null);		
				if(sameShortcut != null)
					errors.add("Shortcut \"" + shortcut + 
							"\" already assigned to different ID level.");
			}
		}		
		return errors;		
	}

	private void deleteIdLevel() {

		MSFeatureIdentificationLevel level = idLevelTable.getSelectedLevel();
		if(level == null)
			return;
		
		else if(level.isLocked()) {
			MessageDialog.showWarningMsg("ID level \"" + 
					level.getName() + "\" is locked and can not be deleted.", this);
			return;
		}
		
		String yesNoQuestion = 
				"Do you want to delete selected identification level?\n"
				+ "It will be cleared from all the features it was assigned to.";
		int res = MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this);
		if(res == JOptionPane.YES_OPTION) {
			
			try {
				IdLevelUtils.deleteMSFeatureIdentificationLevel(level);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IDTDataCache.refreshMsFeatureIdentificationLevelList();
		idLevelTable.setTableModelFromLevelList(
				IDTDataCache.getMsFeatureIdentificationLevelList());
		
		fireIdentificationLevelEvent();
	}
	
	public void addListener(IdentificationLevelEventListener listener) {
		eventListeners.add(listener);
	}
	
	public void removeListener(IdentificationLevelEventListener listener) {
		eventListeners.remove(listener);
	}
	
	public void fireIdentificationLevelEvent() {

		IdentificationLevelEvent event = new IdentificationLevelEvent(this, ParameterSetStatus.CHANGED);
			eventListeners.stream().forEach(l -> l.identificationLevelDefinitionChanged(event));		
	}
}


