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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.design;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignDisplay;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.IDTExperimentDesignPullTask;

public class IDTrackerExperimentDesignEditorPanel extends JPanel
	implements ActionListener, ExperimentDesignDisplay, TaskListener {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -7834538383306416950L;

	protected IDTrackerExperimentDesignEditorToolbar designEditorToolbar;
	protected IdExperimentSampleTable expDesignTable;
	protected LIMSExperiment experiment;
	protected ExperimentDesign experimentDesign;

	protected ExpSampleEditorDialog expSampleEditorDialog;
	protected IndeterminateProgressDialog idp;

	public IDTrackerExperimentDesignEditorPanel() {

		setLayout(new BorderLayout(0, 0));
		designEditorToolbar = new IDTrackerExperimentDesignEditorToolbar(this);
		add(designEditorToolbar, BorderLayout.NORTH);

		expDesignTable = new IdExperimentSampleTable();
		JScrollPane designScrollPane = new JScrollPane(expDesignTable);
		add(designScrollPane, BorderLayout.CENTER);
		expDesignTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {

							IDTExperimentalSample sample = expDesignTable.getSelectedSample();
							if(sample != null)
								showSampleEditor(sample);
						}											
					}
				});
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName()))
			showSampleEditor(null);

		if (command.equals(MainActionCommands.EDIT_SAMPLE_DIALOG_COMMAND.getName())) {

			IDTExperimentalSample sample = expDesignTable.getSelectedSample();
			if(sample != null)
				showSampleEditor(sample);
		}
		if (command.equals(MainActionCommands.ADD_SAMPLE_COMMAND.getName()) ||
				command.equals(MainActionCommands.EDIT_SAMPLE_COMMAND.getName()))
			saveSampleData();

		if (command.equals(MainActionCommands.DELETE_SAMPLE_COMMAND.getName()))
			deleteSample();
	}

	protected void showSampleEditor(IDTExperimentalSample sample) {

		if(experiment == null)
			return;

		expSampleEditorDialog = new ExpSampleEditorDialog(sample, this);
		expSampleEditorDialog.setLocationRelativeTo(this);
		expSampleEditorDialog.setVisible(true);
	}

	protected void saveSampleData() {
		
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
			sample = new IDTExperimentalSample(null, sampleName, sampleDescription, new Date(), stockSample);
			try {
				String sampleId = IDTUtils.addNewIDTSample(sample, experiment);
				sample.setId(sampleId);
				if(experiment.getExperimentDesign() == null)
					experiment.setDesign(new ExperimentDesign());
				
				experiment.getExperimentDesign().addSample(sample);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			sample.setName(sampleName);
			sample.setDescription(sampleDescription);
			try {
				IDTUtils.updateIDTSample(sample);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		reloadDesign();
		expSampleEditorDialog.dispose();
	}
	
	protected Collection<String>vaidateSampleData(){
		
		Collection<String>errors = new ArrayList<String>();
		StockSample stockSample = expSampleEditorDialog.getStockSample();
		String sampleName = expSampleEditorDialog.getSampleName();
		if(experiment == null)
			errors.add("Parent experiment not selected.");

		if(stockSample == null)
			errors.add("Parent stock sample not selected.");

		if(sampleName.isEmpty())
			errors.add("Sample name can not be empty.");
		return errors;
	}

	protected void deleteSample() {

		Collection<IDTExperimentalSample> selectedSamples = expDesignTable.getSelectedSamples();
		if(selectedSamples.isEmpty())
			return;

		if(!IDTUtils.isSuperUser(this))
			return;

		String yesNoQuestion =
			"Do you want to delete selected sample(s) from the active experiment?\n" +
			"All associated information will be lost!";
		if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this) == JOptionPane.NO_OPTION)
			return;

		//	TODO
		try {
			IDTUtils.deleteIDTSamples(selectedSamples);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(experiment.getExperimentDesign() != null)
			experiment.getExperimentDesign().removeSamples(selectedSamples);

		reloadDesign();
	}

	public void showExperimentDesign(ExperimentDesign newDesign) {

		experimentDesign = newDesign;
		reloadDesign();
	}

	@Override
	public void reloadDesign() {

		expDesignTable.clearTable();
		if (experimentDesign != null)
			populateDesignTable(experimentDesign);
	}

	public void clearPanel() {

		experiment = null;
		experimentDesign = null;
		expDesignTable.clearTable();
	}

	/**
	 * @return the experimentDesign
	 */
	public ExperimentDesign getExperimentDesign() {
		return experimentDesign;
	}

	/**
	 * @param experimentDesign the experimentDesign to set
	 */
	public void setExperimentDesign(ExperimentDesign experimentDesign) {
		this.experimentDesign = experimentDesign;
	}

	public void loadExperiment(LIMSExperiment experiment) {

		this.experiment = experiment;
		experimentDesign = experiment.getExperimentDesign();
		if(experimentDesign == null) {

			LoadExperimentDesignTask task = new LoadExperimentDesignTask();
			idp = new IndeterminateProgressDialog("Loading experiment design ...", this, task);
			idp.setLocationRelativeTo(this);
			idp.setVisible(true);
		}
		populateDesignTable(experimentDesign);
	}

	class LoadExperimentDesignTask extends LongUpdateTask {

		@Override
		public Void doInBackground() {

			try {
				experimentDesign = IDTUtils.getDesignForIDTExperiment(experiment.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(experimentDesign != null)
				experiment.setDesign(experimentDesign);

			return null;
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(IDTExperimentDesignPullTask.class)) {

				expDesignTable.clearTable();
				experimentDesign = ((IDTExperimentDesignPullTask)e.getSource()).getDesign();
				if(experimentDesign != null)
					populateDesignTable(experimentDesign);

				MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
				MainWindow.hideProgressDialog();
			}
		}
	}

	protected void populateDesignTable(ExperimentDesign design) {

		experimentDesign = design;

		List<IDTExperimentalSample> sampleList = 
				experimentDesign.getSamples().stream().filter(IDTExperimentalSample.class::isInstance)
			    .map(IDTExperimentalSample.class::cast).collect(Collectors.toList());

		expDesignTable.setTableModelFromSamples(sampleList);
	}

	/**
	 * @return the experiment
	 */
	public LIMSExperiment getExperiment() {
		return experiment;
	}
}


























