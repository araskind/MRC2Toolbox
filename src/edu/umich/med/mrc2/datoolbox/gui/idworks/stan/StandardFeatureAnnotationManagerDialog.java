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

package edu.umich.med.mrc2.datoolbox.gui.idworks.stan;

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

import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.StandardAnnotationUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class StandardFeatureAnnotationManagerDialog extends JDialog implements ActionListener{


	/**
	 * 
	 */
	private static final long serialVersionUID = 6617911751201844630L;
	private static final Icon stIcon = GuiUtils.getIcon("editCollection", 32);
	private StandardFeatureAnnotationTable standardFeatureAnnotationTable;
	private StandardFeatureAnnotationToolbar toolbar;
	private StandardFeatureAnnotationEditorDialog standardFeatureAnnotationEditorDialog;
	
	public StandardFeatureAnnotationManagerDialog() {
		super();
		setTitle("Standard feature annotation manager");
		setIconImage(((ImageIcon) stIcon).getImage());
		setPreferredSize(new Dimension(400, 400));
		setSize(new Dimension(400, 400));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		toolbar = new StandardFeatureAnnotationToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		standardFeatureAnnotationTable = new StandardFeatureAnnotationTable();
		getContentPane().add(new JScrollPane(standardFeatureAnnotationTable), BorderLayout.CENTER);
		standardFeatureAnnotationTable.setTableModelFromStandardFeatureAnnotationList(
				IDTDataCache.getStandardFeatureAnnotationList());
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.ADD_STANDARD_FEATURE_ANNOTATION_DIALOG_COMMAND.getName()))
			showStandardFeatureAnnotationEditor(null);
		
		if(command.equals(MainActionCommands.ADD_STANDARD_FEATURE_ANNOTATION_COMMAND.getName()))
			addNewStandardFeatureAnnotation();
	
		if(command.equals(MainActionCommands.EDIT_STANDARD_FEATURE_ANNOTATION_DIALOG_COMMAND.getName())) {
			
			 StandardFeatureAnnotation annotation = standardFeatureAnnotationTable.getSelectedStandardFeatureAnnotation();
			if(annotation == null)
				return;
			else
				showStandardFeatureAnnotationEditor(annotation);
		}
		
		if(command.equals(MainActionCommands.EDIT_STANDARD_FEATURE_ANNOTATION_COMMAND.getName()))
			editStandardFeatureAnnotation();
		
		if(command.equals(MainActionCommands.DELETE_STANDARD_FEATURE_ANNOTATION_COMMAND.getName()))
			deleteStandardFeatureAnnotation();
	}
	
	
//	SHOW_STANDARD_FEATURE_ANNOTATION_MANAGER_DIALOG_COMMAND("Show standard feature annotation manager"),
//	ADD_STANDARD_FEATURE_ANNOTATION_DIALOG_COMMAND("Add standard feature annotation dialog"),
//	ADD_STANDARD_FEATURE_ANNOTATION_COMMAND("Add standard feature annotation"),
//	EDIT_STANDARD_FEATURE_ANNOTATION_DIALOG_COMMAND("Edit standard feature annotation dialog"),
//	EDIT_STANDARD_FEATURE_ANNOTATION_COMMAND("Edit standard feature annotation"),
//	DELETE_STANDARD_FEATURE_ANNOTATION_COMMAND("Delete standard feature annotation"),
//	ASSIGN_STANDARD_FEATURE_ANNOTATIONS_TO_FEATURE_DIALOG_COMMAND("Assign standard feature annotations to feature"),
//	SAVE_STANDARD_FEATURE_ANNOTATION_ASSIGNMENT_COMMAND("Save standard feature annotation assignment"),

	private void showStandardFeatureAnnotationEditor(StandardFeatureAnnotation annotation) {

		standardFeatureAnnotationEditorDialog = new StandardFeatureAnnotationEditorDialog(annotation, this);
		standardFeatureAnnotationEditorDialog.setLocationRelativeTo(this);
		standardFeatureAnnotationEditorDialog.setVisible(true);	
	}

	private void addNewStandardFeatureAnnotation() {

		Collection<String>errors = validateStandardFeatureAnnotation();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), standardFeatureAnnotationEditorDialog);
			return;
		}
		StandardFeatureAnnotation annotation = 
				new StandardFeatureAnnotation(null,
						standardFeatureAnnotationEditorDialog.getAnnotationCode(),
						standardFeatureAnnotationEditorDialog.getAnnotationText());
		try {
			StandardAnnotationUtils.addNewStandardFeatureAnnotation(annotation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		IDTDataCache.refreshStandardFeatureAnnotationList();
		standardFeatureAnnotationTable.setTableModelFromStandardFeatureAnnotationList(
				IDTDataCache.getStandardFeatureAnnotationList());
		standardFeatureAnnotationEditorDialog.dispose();
	}

	private void editStandardFeatureAnnotation() {

		Collection<String>errors = validateStandardFeatureAnnotation();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), standardFeatureAnnotationEditorDialog);
			return;
		}
		StandardFeatureAnnotation annotation = standardFeatureAnnotationEditorDialog.getStandardFeatureAnnotation();
		annotation.setCode(standardFeatureAnnotationEditorDialog.getAnnotationCode());
		annotation.setText(standardFeatureAnnotationEditorDialog.getAnnotationText());
		if(annotation != null) {
			try {
				StandardAnnotationUtils.editStandardFeatureAnnotation(annotation);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		IDTDataCache.refreshStandardFeatureAnnotationList();
		standardFeatureAnnotationTable.setTableModelFromStandardFeatureAnnotationList(
				IDTDataCache.getStandardFeatureAnnotationList());
		standardFeatureAnnotationEditorDialog.dispose();
	}
	
	private Collection<String>validateStandardFeatureAnnotation(){
		
		Collection<String>errors = new ArrayList<String>();
		StandardFeatureAnnotation annotation = 
				standardFeatureAnnotationEditorDialog.getStandardFeatureAnnotation();
		String code = standardFeatureAnnotationEditorDialog.getAnnotationCode();
		if(code.isEmpty())
			errors.add("Name can not be empty.");

		if(standardFeatureAnnotationEditorDialog.getAnnotationText().isEmpty())
			errors.add("Description can not be empty.");
		
		//	Code
		if(annotation != null) {
			
			String id = annotation.getId();
			StandardFeatureAnnotation sameCode = 
					IDTDataCache.getStandardFeatureAnnotationList().stream().
					filter(s -> !s.getId().equals(id)).
					filter(s -> s.getCode().equals(code)).
					findFirst().orElse(null);
			
			if(sameCode != null)
				errors.add("A different annotation with the same code \"" + code + "\" already exists.");
		}
		else {
			StandardFeatureAnnotation sameCode = 
					IDTDataCache.getStandardFeatureAnnotationList().stream().
					filter(s -> s.getCode().equals(code)).
					findFirst().orElse(null);
			
			if(sameCode != null)
				errors.add("A different annotation with the same code \"" + code + "\" already exists.");
		}
		return errors;		
	}

	private void deleteStandardFeatureAnnotation() {

		StandardFeatureAnnotation annotation 
			= standardFeatureAnnotationTable.getSelectedStandardFeatureAnnotation();
		if(annotation == null)
			return;
		
		if(!IDTUtils.isSuperUser(this))
			return;
		
		String yesNoQuestion = 
				"Do you want to delete selected standard annotation?\n"
				+ "It will be cleared from all the features it was assigned to.";
		int res = MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this);
		if(res == JOptionPane.YES_OPTION) {
			
			try {
				StandardAnnotationUtils.deleteStandardFeatureAnnotation(annotation);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IDTDataCache.refreshStandardFeatureAnnotationList();
		standardFeatureAnnotationTable.setTableModelFromStandardFeatureAnnotationList(
				IDTDataCache.getStandardFeatureAnnotationList());
	}
}


