/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
import java.util.prefs.Preferences;

import javax.swing.Icon;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignDisplay;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableIDTrackerExperimentDesignEditorPanel 
		extends AbstractIDTrackerLimsPanel implements ExperimentDesignDisplay {

	private static final Icon componentIcon = GuiUtils.getIcon("editSample", 16);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 24);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteSample", 24);
	private static final Icon editSampleIcon = GuiUtils.getIcon("editSample", 24);
	
	IDTrackerExperimentDesignEditorPanel experimentDesignEditorPanel;

	public DockableIDTrackerExperimentDesignEditorPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableIDTrackerExperimentDesignEditorPanel", 
				componentIcon, "Experiment design", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		experimentDesignEditorPanel = 
				new IDTrackerExperimentDesignEditorPanel(true);
		getContentPane().add(experimentDesignEditorPanel, BorderLayout.CENTER);
		
		initActions();
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName(), 
				addSampleIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_SAMPLE_DIALOG_COMMAND.getName(), 
				editSampleIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_SAMPLE_COMMAND.getName(),
				MainActionCommands.DELETE_SAMPLE_COMMAND.getName(), 
				deleteSampleIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(!isConnected())
			return;
		
		super.actionPerformed(e);
		
		String command = e.getActionCommand();

		if (command.equals(MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName()))
			experimentDesignEditorPanel.showSampleEditor(null);

		if (command.equals(MainActionCommands.EDIT_SAMPLE_DIALOG_COMMAND.getName())) {

			IDTExperimentalSample sample = experimentDesignEditorPanel.getSelectedSample();
			if(sample != null)
				experimentDesignEditorPanel.showSampleEditor(sample);
		}
		if (command.equals(MainActionCommands.ADD_SAMPLE_COMMAND.getName()) ||
				command.equals(MainActionCommands.EDIT_SAMPLE_COMMAND.getName()))
			experimentDesignEditorPanel.saveSampleData();

		if (command.equals(MainActionCommands.DELETE_SAMPLE_COMMAND.getName()))
			experimentDesignEditorPanel.deleteSample();		
	}
	
	public void showExperimentDesign(ExperimentDesign newDesign) {
		experimentDesignEditorPanel.showExperimentDesign(newDesign);
	}

	@Override
	public void reloadDesign() {
		experimentDesignEditorPanel.reloadDesign();
	}

	public synchronized void clearPanel() {
		experimentDesignEditorPanel.clearPanel();
	}

	public ExperimentDesign getExperimentDesign() {
		return experimentDesignEditorPanel.getExperimentDesign();
	}
	
	public void loadExperiment(LIMSExperiment experiment) {
		experimentDesignEditorPanel.loadExperiment(experiment);
	}

	public LIMSExperiment getExperiment() {
		return experimentDesignEditorPanel.getExperiment();
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

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}
}


























