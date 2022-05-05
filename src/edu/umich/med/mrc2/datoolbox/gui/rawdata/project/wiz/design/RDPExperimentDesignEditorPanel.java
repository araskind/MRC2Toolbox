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

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.design.IDTrackerExperimentDesignEditorPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.StockSampleEditorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class RDPExperimentDesignEditorPanel extends IDTrackerExperimentDesignEditorPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8971145226550968788L;
	private RDPExperimentDesignEditorToolbar toolbar;
	private StockSampleEditorDialog stockSampleEditorDialog;
	
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
		try {
			IDTUtils.insertStockSample(stockSample);
		} catch (Exception e) {
			e.printStackTrace();
		}
		IDTDataCash.refreshStockSampleList();
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
