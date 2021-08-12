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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.study;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDbUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.edit.MoTrPACStudyEditorFrame;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.edit.StudyTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableMoTrPACStudyManagerPanel extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("factor", 16);
	private StudyManagerToolbar toolbar;
	private StudyTable studiesTable;
	private MoTrPACStudyEditorFrame motrpacStudyEditorDialog;
	private boolean limsDataLoaded;

	public DockableMoTrPACStudyManagerPanel(ListSelectionListener lsListener) {

		super("DockableMoTrPACStudyManagerPanel", componentIcon, "MoTrPAC studies", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new StudyManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		studiesTable =  new StudyTable();
		studiesTable.getSelectionModel().addListSelectionListener(lsListener);
		JScrollPane designScrollPane = new JScrollPane(studiesTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		limsDataLoaded = false;
	}
	
	public StudyTable getTable() {
		return studiesTable;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in!", this.getContentPane());
			return;
		}
		if(!limsDataLoaded) {
			MessageDialog.showErrorMsg("Please refresh LIMS data first!", this.getContentPane());
			return;
		}
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_MOTRPAC_STUDY_DIALOG_COMMAND.getName()))
			showStudyEditor(null);

		if(command.equals(MainActionCommands.ADD_MOTRPAC_STUDY_COMMAND.getName()))
			addStudy();

		if(command.equals(MainActionCommands.EDIT_MOTRPAC_STUDY_DIALOG_COMMAND.getName())) {

			if(studiesTable.getSelectedStudy() != null)
				showStudyEditor(studiesTable.getSelectedStudy());
		}
		if(command.equals(MainActionCommands.EDIT_MOTRPAC_STUDY_COMMAND.getName()))
			editStudy();

		if(command.equals(MainActionCommands.DELETE_MOTRPAC_STUDY_COMMAND.getName()))
			deleteSelectedStudy();
	}

	public void loadStudies() {
		studiesTable.setTableModelFromStudies(
				MoTrPACDatabaseCash.getMotrpacStudyList());
	}

	private void deleteSelectedStudy() {

		MoTrPACStudy toDelete = studiesTable.getSelectedStudy();
		if(toDelete == null)
			return;		
		
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;

		String message = "Do you really want to delete MoTrPAC study "
				+ toDelete.toString() + "?";

		if(MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane()) == JOptionPane.YES_OPTION) {

			try {
				MoTrPACDbUtils.deleteMotrpacStudy(toDelete);
			} catch (Exception e) {
				e.printStackTrace();
			}
			MoTrPACDatabaseCash.refreshMotrpacStudyList();
			loadStudies();
		}
	}

	private void editStudy() {
		
		MoTrPACStudy studyToEdit = motrpacStudyEditorDialog.getStudy();
		Collection<String> errors = motrpacStudyEditorDialog.validateStudyData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), motrpacStudyEditorDialog);
			return;
		}
		studyToEdit.setCode(motrpacStudyEditorDialog.getStudyCode());
		studyToEdit.setDescription(motrpacStudyEditorDialog.getStudyDescription());
		studyToEdit.setSubjectType(motrpacStudyEditorDialog.getMotracSubjectType());
		try {
			MoTrPACDbUtils.editMotrpacStudy(studyToEdit);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		MoTrPACDatabaseCash.refreshMotrpacStudyList();
		loadStudies();
		motrpacStudyEditorDialog.dispose();	
	}

	private void addStudy() {
		
		MoTrPACStudy newStudy = motrpacStudyEditorDialog.getStudy();
		Collection<String> errors = motrpacStudyEditorDialog.validateStudyData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), motrpacStudyEditorDialog);
			return;
		}
		newStudy.setCode(motrpacStudyEditorDialog.getStudyCode());
		newStudy.setDescription(motrpacStudyEditorDialog.getStudyDescription());
		newStudy.setSubjectType(motrpacStudyEditorDialog.getMotracSubjectType());
		try {
			MoTrPACDbUtils.addNewMotrpacStudy(newStudy);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MoTrPACDatabaseCash.refreshMotrpacStudyList();
		loadStudies();
		motrpacStudyEditorDialog.dispose();	
	}

	private void showStudyEditor(MoTrPACStudy motrpacStudy) {
		
		motrpacStudyEditorDialog = new MoTrPACStudyEditorFrame(motrpacStudy, this);
		motrpacStudyEditorDialog.setLocationRelativeTo(this.getContentPane());
		motrpacStudyEditorDialog.setVisible(true);
	}

	public void clearPanel() {
		studiesTable.clearTable();
	}

	public void setLimsDataLoaded(boolean limsDataLoaded) {
		this.limsDataLoaded = limsDataLoaded;
	}
}
