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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.design;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.design.IDTrackerExperimentDesignEditorPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.StockSampleEditorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class WizardExperimentDesignEditorPanel extends IDTrackerExperimentDesignEditorPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8971145226550968788L;
	private WizardExperimentDesignEditorToolbar toolbar;
	private StockSampleEditorDialog stockSampleEditorDialog;
	
	public WizardExperimentDesignEditorPanel() {
		super();
		toolbar = new WizardExperimentDesignEditorToolbar(this);
		add(toolbar, BorderLayout.NORTH);
	}
	
	@Override
	public void saveSampleData() {
		
		Collection<String>errors = vaidateSampleData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), expSampleEditorDialog);
			return;
		}
		IDTExperimentalSample sample = expSampleEditorDialog.getSample();
		StockSample stockSample = expSampleEditorDialog.getStockSample();
		String sampleName = expSampleEditorDialog.getSampleName();
		String sampleDescription = expSampleEditorDialog.getSampleDescription();
		
		//	Add new sample
		if(sample == null) {
			String count = Integer.toString(experiment.getExperimentDesign().getSamples().size() + 1);
			String tmpId = DataPrefix.ID_SAMPLE.getName() + StringUtils.leftPad(count, 4, '0');
			sample = new IDTExperimentalSample(tmpId, sampleName, sampleDescription, new Date(), stockSample);
			experiment.getExperimentDesign().addSample(sample,false);
		}
		else {
			sample.setName(sampleName);
			sample.setDescription(sampleDescription);
		}
		reloadDesign();
		expSampleEditorDialog.dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		
		super.actionPerformed(event);
		
		String command = event.getActionCommand();
		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName()))
			showStockSampleEditor(null);
		
		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_COMMAND.getName()))
			addStockSample();
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
	
	@Override
	public void deleteSample() {

		Collection<IDTExperimentalSample> selectedSamples = expDesignTable.getSelectedSamples();
		if(selectedSamples.isEmpty())
			return;

		String yesNoQuestion =
			"Do you want to delete selected sample(s) from the  experiment?";
		if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this) == JOptionPane.NO_OPTION)
			return;

		experiment.getExperimentDesign().removeSamples(selectedSamples,false);
		reloadDesign();
	}
}
