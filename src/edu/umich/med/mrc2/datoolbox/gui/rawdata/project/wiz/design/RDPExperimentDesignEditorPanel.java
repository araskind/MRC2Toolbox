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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.design;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.SamplePrepEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.SamplePrepListener;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.design.IDTrackerExperimentDesignEditorPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.prep.ExistingPrepSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.StockSampleEditorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RDPMetadataDefinitionStage;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class RDPExperimentDesignEditorPanel extends IDTrackerExperimentDesignEditorPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8971145226550968788L;
	private RDPExperimentDesignEditorToolbar toolbar;
	private StockSampleEditorDialog stockSampleEditorDialog;
	private ExistingPrepSelectorDialog existingPrepSelectorDialog;
	private Set<SamplePrepListener> eventListeners;
	
	public RDPExperimentDesignEditorPanel() {
		super();
		toolbar = new RDPExperimentDesignEditorToolbar(this);
		add(toolbar, BorderLayout.NORTH);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		
		if(experiment == null) {
			MessageDialog.showErrorMsg(
					"Please complete the experiment definition step first.", this);
			return;
		}
		super.actionPerformed(event);
		
		String command = event.getActionCommand();
		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName()))
			showStockSampleEditor(null);
		
		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_COMMAND.getName()))
			addStockSample();
		
		if(command.equals(MainActionCommands.LOAD_SAMPLES_WITH_PREP_FROM_DATABASE_COMMAND.getName()))
			selectExistingSamplePrep();
		
		if(command.equals(MainActionCommands.LOAD_SAMPLE_PREP_FROM_DATABASE_COMMAND.getName()))
			loadSelectedSamplePrep();
	}
	
	private void loadSelectedSamplePrep() {
		
		LIMSSamplePreparation selectedPrep = 
				existingPrepSelectorDialog.getSelectedPrep();
		if(selectedPrep == null)
			return;
		
		fireSamplePrepEvent(selectedPrep, ParameterSetStatus.ADDED);
		existingPrepSelectorDialog.dispose();
	}
	
	public void addSamplePrepListener(SamplePrepListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.add(listener);
	}

	public void removeSamplePrepListener(SamplePrepListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.remove(listener);
	}
	
	public void fireSamplePrepEvent(LIMSSamplePreparation prep, ParameterSetStatus status) {

		if(eventListeners == null){
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		SamplePrepEvent event = new SamplePrepEvent(
				prep, status, RDPMetadataDefinitionStage.ADD_SAMPLES);
		eventListeners.stream().forEach(l -> l.samplePrepStatusChanged(event));		
	}

	private void selectExistingSamplePrep() {
		
		if(experiment == null) {
			MessageDialog.showErrorMsg(
					"Please complete the experiment definition step first.", this);
			return;
		}
		if(experiment != null && experiment.getId() != null) {
			MessageDialog.showErrorMsg(
					"The parent experiment is already in the database\n "
					+ "and it's design can't be altered through this wizard.", this);
			return;
		}
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"This operation will automatically load experiment design "
				+ "associated with the selected sample preparation.\nProceed?",
				this);
		if(res != JOptionPane.YES_OPTION)
			return;
		
		existingPrepSelectorDialog = new ExistingPrepSelectorDialog(this);
		existingPrepSelectorDialog.setLocationRelativeTo(this);
		existingPrepSelectorDialog.setVisible(true);
	}
	
	private void addStockSample() {

		Collection<String> errors = stockSampleEditorDialog.validateStockSample();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), stockSampleEditorDialog);
			return;
		}
		StockSample stockSample = new StockSample(
				stockSampleEditorDialog.getSampleName(),
				stockSampleEditorDialog.getSampleDescription(),
				stockSampleEditorDialog.getSampleType(),
				stockSampleEditorDialog.getSpecies());
		stockSample.setExternalId(stockSampleEditorDialog.getExternalSourceId());
		stockSample.setExternalSource(stockSampleEditorDialog.getExternalSourceName());
		stockSample.setLimsExperiment(stockSampleEditorDialog.getLIMSExperiment());
		try {
			IDTUtils.addStockSample(stockSample);
		} catch (Exception e) {
			e.printStackTrace();
		}
		IDTDataCache.refreshStockSampleList();
		stockSampleEditorDialog.dispose();
	}

	private void showStockSampleEditor(StockSample sample) {

		stockSampleEditorDialog = new StockSampleEditorDialog(sample, this);
		stockSampleEditorDialog.setLocationRelativeTo(this);
		stockSampleEditorDialog.setVisible(true);
	}
	
	public void setDesignEditable(boolean editable) {
		toolbar.setDesignEditable(editable);
	}
}
