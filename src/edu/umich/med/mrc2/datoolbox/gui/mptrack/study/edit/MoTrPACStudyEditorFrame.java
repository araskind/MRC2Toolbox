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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.study.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MotracSubjectType;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCache;
import edu.umich.med.mrc2.datoolbox.gui.lims.experiment.DockableExperimentListingTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.DockableMoTrPACStudyAssayListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.DockableMoTrPACTissueCodeListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class MoTrPACStudyEditorFrame  extends  JFrame 
		implements ActionListener, ListSelectionListener, PersistentLayout {

	/**
	 *
	 */
	private static final long serialVersionUID = 8046770417831235265L;
	private static final Icon studyIcon = GuiUtils.getIcon("newIdExperiment", 32);

	private CControl control;
	private CGrid grid;
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "MotrpacStudyEditorFrame.layout");
	
	private MotrpacStudyEditorToolbar toolbar;
	private JButton saveButton;
	
	private MoTrPACStudy study;
	private DockableMoTrPACTissueCodeListingPanel tissueCodeListingPanel;
	private DockableMoTrPACStudyAssayListingPanel studyAssayListingPanel;
	private DockableExperimentListingTable experimentListingTable;
	
	private MoTrPACStudyExperimentEditorDialog moTrPACStudyExperimentEditorDialog;
	private MoTrPACStudyAssayEditorDialog moTrPACStudyAssayEditorDialog;
	private MoTrPACStudyTissueCodeEditorDialog moTrPACStudyTissueCodeEditorDialog;
	private MoTrPACStudyDefinitionPanel moTrPACStudyDefinitionPanel;
	
	public MoTrPACStudyEditorFrame(MoTrPACStudy study, ActionListener actionListener) {

		super();
		setIconImage(((ImageIcon) studyIcon).getImage());
		setPreferredSize(new Dimension(1200, 800));
		setSize(new Dimension(1200, 800));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		toolbar = new MotrpacStudyEditorToolbar(this);	
		moTrPACStudyDefinitionPanel = new MoTrPACStudyDefinitionPanel();
		JPanel defPanel = new JPanel(new BorderLayout(0,0));
		defPanel.add(toolbar, BorderLayout.NORTH);
		defPanel.add(moTrPACStudyDefinitionPanel, BorderLayout.CENTER);
		getContentPane().add(defPanel, BorderLayout.NORTH);		
		control = new CControl(this);
		control.getController().setTheme(new EclipseTheme());
		getContentPane().add( control.getContentArea(), BorderLayout.CENTER);
		grid = new CGrid(control);

		tissueCodeListingPanel = new DockableMoTrPACTissueCodeListingPanel();
		studyAssayListingPanel = new DockableMoTrPACStudyAssayListingPanel(); 
		experimentListingTable = new DockableExperimentListingTable(this);
		grid.add(0, 0, 1, 1,
				tissueCodeListingPanel,
				studyAssayListingPanel,
				experimentListingTable);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);

		saveButton = new JButton(MainActionCommands.SAVE_MOTRPAC_STUDY_COMMAND.getName());
		saveButton.setActionCommand(MainActionCommands.SAVE_MOTRPAC_STUDY_COMMAND.getName());
		saveButton.addActionListener(actionListener);
		panel_1.add(saveButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		
		control.getContentArea().deploy(grid);		
		loadLayout(layoutConfigFile);
		loadStudyData(study);
		pack();
	}
	
	@Override
	public void dispose() {

		saveLayout(layoutConfigFile);		
		super.dispose();
	}
	
	private void loadStudyData(MoTrPACStudy study) {

		if(study == null) {
			this.study = new MoTrPACStudy(null, null, null, "New MoTrPAC study");
			setTitle("Create new MoTrPAC study");			
			saveButton.setActionCommand(MainActionCommands.ADD_MOTRPAC_STUDY_COMMAND.getName());
		}
		else {
			this.study = new MoTrPACStudy(study);
			setTitle("Edit MoTrPAC study \"" + study.getDescription() + "\"");
			saveButton.setActionCommand(MainActionCommands.EDIT_MOTRPAC_STUDY_COMMAND.getName());
			studyAssayListingPanel.loadAssays(study.getAssays());
			experimentListingTable.setModelFromExperimentCollection(study.getExperiments());
		}
		moTrPACStudyDefinitionPanel.loadStudy(this.study);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		if(!e.getValueIsAdjusting()) {

			LIMSExperiment selected = experimentListingTable.getSelectedExperiment();
			if (selected == null) {
				tissueCodeListingPanel.clearPanel();
			}
			else {
				Collection<MoTrPACTissueCode> experimentTissueCodes = study.getTissueCodesForExperiment(selected);
				if(experimentTissueCodes != null)
					tissueCodeListingPanel.loadTissueCodes(experimentTissueCodes);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.EDIT_MOTRPAC_STUDY_EXPERIMENTS_COMMAND.getName()))
			showStudyExperimentEditor();
		
		if(command.equals(MainActionCommands.SAVE_MOTRPAC_STUDY_EXPERIMENTS_COMMAND.getName()))
			saveStudyExperimentList();
		
		if(command.equals(MainActionCommands.EDIT_MOTRPAC_STUDY_ASSAYS_COMMAND.getName()))
			showStudyAssayEditor();
		
		if(command.equals(MainActionCommands.SAVE_MOTRPAC_STUDY_ASSAYS_COMMAND.getName()))
			saveStudyAssayList();
		
		if(command.equals(MainActionCommands.EDIT_MOTRPAC_STUDY_TISSUES_COMMAND.getName()))
			showExperimentTissueEditor();
		
		if(command.equals(MainActionCommands.SAVE_MOTRPAC_STUDY_TISSUES_COMMAND.getName()))
			saveExperimentTissueList();
	}
	
	private void showStudyExperimentEditor() {
		
		moTrPACStudyExperimentEditorDialog = new MoTrPACStudyExperimentEditorDialog(study, this);
		moTrPACStudyExperimentEditorDialog.setLocationRelativeTo(this.getContentPane());
		moTrPACStudyExperimentEditorDialog.setVisible(true);
	}
	
	private void saveStudyExperimentList() {

		Collection<LIMSExperiment> assignedExperiments = moTrPACStudyExperimentEditorDialog.getAssignedExperiments();
		Collection<LIMSExperiment> toRemove = new ArrayList<LIMSExperiment>(); 
		Collection<LIMSExperiment> toAdd = new ArrayList<LIMSExperiment>(); 
		Collection<LIMSExperiment>current = study.getExperiments();
		for(LIMSExperiment a : current) {
			
			if(!assignedExperiments.contains(a))
				toRemove.add(a);			
		}
		for(LIMSExperiment a : assignedExperiments) {
			
			if(!current.contains(a))
				toAdd.add(a);			
		}
		if(!toRemove.isEmpty() || !toAdd.isEmpty()) {
			
			if(!current.isEmpty()) {
				int res = MessageDialog.showChoiceWithWarningMsg(
						"Do you want to modify study experiment list?", moTrPACStudyAssayEditorDialog);
				if(res != JOptionPane.YES_NO_OPTION) 
					return;
			}
			for(LIMSExperiment a : toRemove)
				study.removeExperiment(a);
			
			for(LIMSExperiment a : toAdd)
				study.addExperiment(a);
			
			//	TODO save to database
		}			
		experimentListingTable.setModelFromExperimentCollection(study.getExperiments());
		moTrPACStudyExperimentEditorDialog.dispose();
	}
	
	private void showStudyAssayEditor() {
		
		moTrPACStudyAssayEditorDialog = new MoTrPACStudyAssayEditorDialog(study, this);
		moTrPACStudyAssayEditorDialog.loadAssays(study.getAssays());
		moTrPACStudyAssayEditorDialog.setLocationRelativeTo(this.getContentPane());
		moTrPACStudyAssayEditorDialog.setVisible(true);
	}
	
	private void saveStudyAssayList() {

		Collection<MoTrPACAssay> assays = moTrPACStudyAssayEditorDialog.getAssignedAssays();
		Collection<MoTrPACAssay> toRemove = new ArrayList<MoTrPACAssay>(); 
		Collection<MoTrPACAssay> toAdd = new ArrayList<MoTrPACAssay>(); 
		Collection<MoTrPACAssay>current = study.getAssays();
		for(MoTrPACAssay a : current) {
			
			if(!assays.contains(a))
				toRemove.add(a);			
		}
		for(MoTrPACAssay a : assays) {
			
			if(!current.contains(a))
				toAdd.add(a);			
		}
		if(!toRemove.isEmpty() || !toAdd.isEmpty()) {
			
			if(!current.isEmpty()) {
				int res = MessageDialog.showChoiceWithWarningMsg(
						"Do you want to modify study assay list?", moTrPACStudyAssayEditorDialog);
				if(res != JOptionPane.YES_NO_OPTION)
					return;
			}			 
			for(MoTrPACAssay a : toRemove)
				study.removeAssay(a);
			
			for(MoTrPACAssay a : toAdd)
				study.addAssay(a);
			
			//	TODO save to database
		}
		studyAssayListingPanel.loadAssays(study.getAssays());
		moTrPACStudyAssayEditorDialog.dispose();
	}
	
	private void showExperimentTissueEditor() {

		LIMSExperiment selected = experimentListingTable.getSelectedExperiment();
		if (selected == null) 
			return;
		
		moTrPACStudyTissueCodeEditorDialog = new MoTrPACStudyTissueCodeEditorDialog(selected, study, this);
		moTrPACStudyTissueCodeEditorDialog.loadTissueCodes(study.getTissueCodesForExperiment(selected));
		moTrPACStudyTissueCodeEditorDialog.setLocationRelativeTo(this.getContentPane());
		moTrPACStudyTissueCodeEditorDialog.setVisible(true);
	}
	
	private void saveExperimentTissueList() {
		
		Collection<MoTrPACTissueCode> tissueCodes = moTrPACStudyTissueCodeEditorDialog.getAssignedTissueCodes();
		Collection<MoTrPACTissueCode> toRemove = new ArrayList<MoTrPACTissueCode>(); 
		Collection<MoTrPACTissueCode> toAdd = new ArrayList<MoTrPACTissueCode>(); 
		LIMSExperiment experiment = moTrPACStudyTissueCodeEditorDialog.getExperiment();
		Collection<MoTrPACTissueCode>current = study.getTissueCodesForExperiment(experiment);
		for(MoTrPACTissueCode a : current) {
			
			if(!tissueCodes.contains(a))
				toRemove.add(a);			
		}
		for(MoTrPACTissueCode a : tissueCodes) {
			
			if(!current.contains(a))
				toAdd.add(a);			
		}
		if(!toRemove.isEmpty() || !toAdd.isEmpty()) {
			
			if(!current.isEmpty()) {
				int res = MessageDialog.showChoiceWithWarningMsg(
						"Do you want to modify experiment tissue list?", moTrPACStudyAssayEditorDialog);
				if(res != JOptionPane.YES_NO_OPTION)
					return;
			}			 
			for(MoTrPACTissueCode a : toRemove)
				study.removeTissueFromExperiment(experiment, a);
			
			for(MoTrPACTissueCode a : toAdd)
				study.addTissueForExperiment(experiment, a);
		}
		tissueCodeListingPanel.loadTissueCodes(study.getTissueCodesForExperiment(experiment));
		moTrPACStudyTissueCodeEditorDialog.dispose();
	}

	public Collection<String>validateStudyData(){
		
		Collection<String>errors = new ArrayList<String>();
		String studyCode = moTrPACStudyDefinitionPanel.getStudyCode();
		if(studyCode.isEmpty())
			errors.add("Study code not specified.");
		else {
			MoTrPACStudy existingStudyWithSameCode = MoTrPACDatabaseCache.getMotrpacStudyList().
					stream().filter(s -> s.getCode().equals(studyCode)).findFirst().orElse(null);
			if(existingStudyWithSameCode != null) {
				
				if(study.getId() == null || (study.getId() != null && !study.getId().equals(existingStudyWithSameCode.getId()))) {
					errors.add("A different study with the same code already exists.");
				}
			}
		}
		String studyDescription = moTrPACStudyDefinitionPanel.getStudyDescription();
		if(studyDescription.isEmpty())
			errors.add("Study description not specified.");

		MotracSubjectType subjectType = moTrPACStudyDefinitionPanel.getMotracSubjectType();
		if(subjectType == null)
			errors.add("Study subjectType not specified.");
		
		return errors;
	}	
	
	public String getStudyCode() {
		return moTrPACStudyDefinitionPanel.getStudyCode();
	}
	
	public String getStudyDescription() {
		return moTrPACStudyDefinitionPanel.getStudyDescription();
	}
	
	public MotracSubjectType getMotracSubjectType() {
		return moTrPACStudyDefinitionPanel.getMotracSubjectType();
	}
	
	@Override
	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					control.writeXML(layoutFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void saveLayout(File layoutFile) {

		if(control != null) {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	public MoTrPACStudy getStudy() {
		return study;
	}
}





















