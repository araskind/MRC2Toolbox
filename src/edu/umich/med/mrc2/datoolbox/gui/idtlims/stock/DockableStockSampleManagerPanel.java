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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.stock;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class DockableStockSampleManagerPanel extends AbstractIDTrackerLimsPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("editStandardSample", 16);
	private static final Icon rsEditIcon = GuiUtils.getIcon("editStandardSample", 24);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addStandardSample", 24);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteStandardSample", 24);
	
	private StockSampleManagerToolbar toolbar;
	private StockSampleTable stockSampleTable;
	private StockSampleEditorDialog stockSampleEditorDialog;

	public DockableStockSampleManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableStockSampleManagerPanel", 
				componentIcon, "Stock samples", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new StockSampleManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		stockSampleTable =  new StockSampleTable();
		JScrollPane designScrollPane = new JScrollPane(stockSampleTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		stockSampleTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {
							
							StockSample sample = stockSampleTable.getSelectedSample();
							if(sample != null)
								showStockSampleEditor(sample);	
						}
					}
				});
		initActions();
	}

	@Override
	protected void initActions() {

		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(), 
				addSampleIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_REFERENCE_SAMPLE_DIALOG_COMMAND.getName(), 
				rsEditIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_REFERENCE_SAMPLE_COMMAND.getName(),
				MainActionCommands.DELETE_REFERENCE_SAMPLE_COMMAND.getName(), 
				deleteSampleIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(!isConnected())
			return;
		
		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_DIALOG_COMMAND.getName()))
			showStockSampleEditor(null);

		if(command.equals(MainActionCommands.ADD_REFERENCE_SAMPLE_COMMAND.getName()))
			addStockSample();

		if(command.equals(MainActionCommands.EDIT_REFERENCE_SAMPLE_DIALOG_COMMAND.getName())) {

			StockSample sample = stockSampleTable.getSelectedSample();
			if(sample != null)
				showStockSampleEditor(sample);	
		}
		if(command.equals(MainActionCommands.EDIT_REFERENCE_SAMPLE_COMMAND.getName()))
			editStockSample();

		if(command.equals(MainActionCommands.DELETE_REFERENCE_SAMPLE_COMMAND.getName()))
			deleteSelectedSample();
	}

	public void loadStockSamples() {
		stockSampleTable.setTableModelFromSamples(IDTDataCash.getStockSamples());
	}

	private void deleteSelectedSample() {

		StockSample toDelete = stockSampleTable.getSelectedSample();
		if(toDelete == null)
			return;		
		
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;

		String message = "Do you really want to delete stock sample "
		+ toDelete.getSampleName() + " (" + toDelete.getSampleId() + ")?\n"
				+ "All data derived from this samples will be also deleted from database!";

		if(MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane()) == JOptionPane.YES_OPTION) {

			try {
				IDTUtils.deleteStockSample(toDelete);
			} catch (Exception e) {
				e.printStackTrace();
			}
			IDTDataCash.refreshStockSampleList();
			stockSampleTable.setTableModelFromSamples(IDTDataCash.getStockSamples());
		}
	}

	private void editStockSample() {

		Collection<String> errors = stockSampleEditorDialog.validateStockSample();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), stockSampleEditorDialog);
			return;
		}
		StockSample stockSample = stockSampleEditorDialog.getStockSample();
		stockSample.setSampleName(stockSampleEditorDialog.getSampleName());
		stockSample.setSampleDescription(stockSampleEditorDialog.getSampleDescription());
		stockSample.setLimsSampleType(stockSampleEditorDialog.getSampleType());
		stockSample.setSpecies(stockSampleEditorDialog.getSpecies());
		stockSample.setExternalId(stockSampleEditorDialog.getExternalSourceId());
		stockSample.setExternalSource(stockSampleEditorDialog.getExternalSourceName());
		stockSample.setLimsExperiment(stockSampleEditorDialog.getLIMSExperiment());
		try {
			IDTUtils.updateStockSample(stockSample);
		} catch (Exception e) {
			e.printStackTrace();
		}
		IDTDataCash.refreshStockSampleList();
		stockSampleTable.setTableModelFromSamples(IDTDataCash.getStockSamples());
		stockSampleTable.selectSample(stockSample);
		stockSampleEditorDialog.dispose();
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
			IDTUtils.insertStockSample(stockSample);
		} catch (Exception e) {
			e.printStackTrace();
		}
		IDTDataCash.refreshStockSampleList();
		stockSampleTable.setTableModelFromSamples(IDTDataCash.getStockSamples());
		stockSampleTable.selectSample(stockSample);
		stockSampleEditorDialog.dispose();
	}

	private void showStockSampleEditor(StockSample sample) {

		stockSampleEditorDialog = new StockSampleEditorDialog(sample, this);
		stockSampleEditorDialog.setLocationRelativeTo(this.getContentPane());
		stockSampleEditorDialog.setVisible(true);
	}

	public synchronized void clearPanel() {
		stockSampleTable.clearTable();
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub
		
	}
}
