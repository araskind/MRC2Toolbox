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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.tcode;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCache;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDbUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableMoTrPACTissueCodeManagerPanel extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("tissueCode", 16);
	private TissueCodeManagerToolbar toolbar;
	private TissueCodeTable tissueCodesTable;

	public DockableMoTrPACTissueCodeManagerPanel() {

		super("DockableMoTrPACTissueCodeManagerPanel", componentIcon, "MoTrPAC tissue codes", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new TissueCodeManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		tissueCodesTable =  new TissueCodeTable();
		JScrollPane designScrollPane = new JScrollPane(tissueCodesTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in!", this.getContentPane());
			return;
		}

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_TISSUE_CODE_DIALOG_COMMAND.getName()))
			showTissueCodeEditor(null);

		if(command.equals(MainActionCommands.ADD_TISSUE_CODE_COMMAND.getName()))
			addTissueCode();

		if(command.equals(MainActionCommands.EDIT_TISSUE_CODE_DIALOG_COMMAND.getName())) {

			if(tissueCodesTable.getSelectedCode() != null)
				showTissueCodeEditor(tissueCodesTable.getSelectedCode());
		}
		if(command.equals(MainActionCommands.EDIT_TISSUE_CODE_COMMAND.getName()))
			editTissueCode();

		if(command.equals(MainActionCommands.DELETE_TISSUE_CODE_COMMAND.getName()))
			deleteSelectedTissueCode();
	}

	public void loadTissueCodes() {
		tissueCodesTable.setTableModelFromTissueCodes(
				MoTrPACDatabaseCache.getMotrpacTissueCodeList());
	}

	private void deleteSelectedTissueCode() {

		MoTrPACTissueCode toDelete = tissueCodesTable.getSelectedCode();
		if(toDelete == null)
			return;		
		
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;

		String message = "Do you really want to delete MoTrPAC tissue code "
				+ toDelete.toString() + "?";

		if(MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane()) == JOptionPane.YES_OPTION) {

			try {
				MoTrPACDbUtils.deleteMotrpacTissueCode(toDelete);
			} catch (Exception e) {
				e.printStackTrace();
			}
			MoTrPACDatabaseCache.refreshMotrpacTissueCodeList();
			loadTissueCodes();
		}
	}

	private void editTissueCode() {

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
//		IDTDataCache.refreshSampleTypeList();
//		sampleTypeTable.setTableModelFromSamples(IDTDataCache.getSampleTypes());
//		sampleTypeTable.selectSample(stockSample);
//		stockSampleEditorDialog.dispose();
		
		MoTrPACDatabaseCache.refreshMotrpacTissueCodeList();
		loadTissueCodes();
	}

	private void addTissueCode() {

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
//		IDTDataCache.refreshSampleTypeList();
//		sampleTypeTable.setTableModelFromSamples(IDTDataCache.getSampleTypes());
//		sampleTypeTable.selectSample(stockSample);
//		stockSampleEditorDialog.dispose();
		
		MoTrPACDatabaseCache.refreshMotrpacTissueCodeList();
		loadTissueCodes();
	}

	private void showTissueCodeEditor(MoTrPACTissueCode code) {

//		stockSampleEditorDialog = new SampleTypeEditorDialog(sample, this);
//		stockSampleEditorDialog.setLocationRelativeTo(this.getContentPane());
//		stockSampleEditorDialog.setVisible(true);
	}

	public synchronized void clearPanel() {
		tissueCodesTable.clearTable();
	}
}
