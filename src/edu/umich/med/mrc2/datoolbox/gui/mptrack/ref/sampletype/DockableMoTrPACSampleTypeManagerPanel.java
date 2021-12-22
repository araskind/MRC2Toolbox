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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.sampletype;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MotrpacSampleType;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDbUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableMoTrPACSampleTypeManagerPanel extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("editStandardSample", 16);
	private SampleTypeManagerToolbar toolbar;
	private SampleTypeTable sampleTypeTable;

	public DockableMoTrPACSampleTypeManagerPanel() {

		super("DockableMoTrPACSampleTypeManagerPanel", componentIcon, "MoTrPAC sample types", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new SampleTypeManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		sampleTypeTable =  new SampleTypeTable();
		JScrollPane designScrollPane = new JScrollPane(sampleTypeTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in!", this.getContentPane());
			return;
		}
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName()))
			showSampleTypeEditor(null);

		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_COMMAND.getName()))
			addSampleType();

		if(command.equals(MainActionCommands.EDIT_REFERENCE_SAMPLE_DIALOG_COMMAND.getName())) {

			if(sampleTypeTable.getSelectedSample() != null)
				showSampleTypeEditor(sampleTypeTable.getSelectedSample());
		}
		if(command.equals(MainActionCommands.EDIT_REFERENCE_SAMPLE_COMMAND.getName()))
			editSampleType();

		if(command.equals(MainActionCommands.DELETE_REFERENCE_SAMPLE_COMMAND.getName()))
			deleteSelectedSampleType();
	}

	public void loadSampleTypes() {
		sampleTypeTable.setTableModelFromSamples(
				MoTrPACDatabaseCash.getMotrpacSampleTypeList());
	}

	private void deleteSelectedSampleType() {

		MotrpacSampleType toDelete = sampleTypeTable.getSelectedSample();
		if(toDelete == null)
			return;		
		
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;

		String message = "Do you really want to delete MoTrPAC sample type "
				+ toDelete.getSampleType() + "?";

		if(MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane()) == JOptionPane.YES_OPTION) {

			try {
				MoTrPACDbUtils.deleteMotrpacSampleType(toDelete);
			} catch (Exception e) {
				e.printStackTrace();
			}
			MoTrPACDatabaseCash.refreshMotrpacSampleTypeList();
			loadSampleTypes();
		}
	}

	private void editSampleType() {

//		Collection<String> errors = stockSampleEditorDialog.validateSampleType();
//		if(!errors.isEmpty()) {
//			MessageDialogue.showErrorMsg(StringUtils.join(errors, "\n"), stockSampleEditorDialog);
//			return;
//		}
//		SampleType stockSample = stockSampleEditorDialog.getSampleType();
//		stockSample.setSampleName(stockSampleEditorDialog.getSampleName());
//		stockSample.setSampleDescription(stockSampleEditorDialog.getSampleDescription());
//		stockSample.setLimsSampleType(stockSampleEditorDialog.getSampleType());
//		stockSample.setSpecies(stockSampleEditorDialog.getSpecies());
//		stockSample.setExternalId(stockSampleEditorDialog.getExternalSourceId());
//		stockSample.setExternalSource(stockSampleEditorDialog.getExternalSourceName());
//		try {
//			IDTUtils.updateSampleType(stockSample);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		IDTDataCash.refreshSampleTypeList();
//		sampleTypeTable.setTableModelFromSamples(IDTDataCash.getSampleTypes());
//		sampleTypeTable.selectSample(stockSample);
//		stockSampleEditorDialog.dispose();
		
		MoTrPACDatabaseCash.refreshMotrpacSampleTypeList();
		loadSampleTypes();
	}

	private void addSampleType() {

//		Collection<String> errors = stockSampleEditorDialog.validateSampleType();
//		if(!errors.isEmpty()) {
//			MessageDialogue.showErrorMsg(StringUtils.join(errors, "\n"), stockSampleEditorDialog);
//			return;
//		}
//		SampleType stockSample = new SampleType(
//				stockSampleEditorDialog.getSampleName(),
//				stockSampleEditorDialog.getSampleDescription(),
//				stockSampleEditorDialog.getSampleType(),
//				stockSampleEditorDialog.getSpecies());
//		stockSample.setExternalId(stockSampleEditorDialog.getExternalSourceId());
//		stockSample.setExternalSource(stockSampleEditorDialog.getExternalSourceName());
//		try {
//			IDTUtils.insertSampleType(stockSample);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		IDTDataCash.refreshSampleTypeList();
//		sampleTypeTable.setTableModelFromSamples(IDTDataCash.getSampleTypes());
//		sampleTypeTable.selectSample(stockSample);
//		stockSampleEditorDialog.dispose();
		
		MoTrPACDatabaseCash.refreshMotrpacSampleTypeList();
		loadSampleTypes();
	}

	private void showSampleTypeEditor(MotrpacSampleType sample) {

//		stockSampleEditorDialog = new SampleTypeEditorDialog(sample, this);
//		stockSampleEditorDialog.setLocationRelativeTo(this.getContentPane());
//		stockSampleEditorDialog.setVisible(true);
	}

	public synchronized void clearPanel() {
		sampleTypeTable.clearTable();
	}
}
