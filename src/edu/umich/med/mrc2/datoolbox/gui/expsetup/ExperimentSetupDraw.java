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

package edu.umich.med.mrc2.datoolbox.gui.expsetup;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.event.ListSelectionEvent;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign.DesignSubsetPanel;
import edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist.FeatureSubsetPanel;
import edu.umich.med.mrc2.datoolbox.gui.expsetup.projinfo.DockableLIMSDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.expsetup.projinfo.ExperimentDetailsPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;

public class ExperimentSetupDraw extends DockableMRC2ToolboxPanel {

	private ExperimentDetailsPanel experimentDetailsPanel;
	private DockableLIMSDataPanel limsDataPanel;
	private DesignSubsetPanel designSubsetPanel;
	private FeatureSubsetPanel featureSubsetPanel;
//	private ProjectToolbar projectToolbar;

	private static final Icon componentIcon = GuiUtils.getIcon("MDAToolkit_icon", 16);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "ExperimentSetupDraw.layout");

	public ExperimentSetupDraw() {

		super("ExperimentSetupDraw", "Experiment setup", componentIcon);
		setLayout(new BorderLayout(0, 0));
		
//		projectToolbar = new ProjectToolbar(null);
//		add(projectToolbar, BorderLayout.NORTH);

		experimentDetailsPanel = new ExperimentDetailsPanel();
		limsDataPanel = new DockableLIMSDataPanel();
		designSubsetPanel = new DesignSubsetPanel();
		featureSubsetPanel = new FeatureSubsetPanel();

		grid.add( 0, 0, 100, 30, experimentDetailsPanel, limsDataPanel);
		grid.add( 0, 30, 100, 30, designSubsetPanel);
		grid.add( 0, 60, 100, 30, featureSubsetPanel);
		grid.select(0, 0, 100, 30, experimentDetailsPanel);
		control.getContentArea().deploy( grid );

		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
	}
	
//	public void setActionListener(ActionListener listener) {
//		projectToolbar.setActionListener(listener);
//	}

	public synchronized void clearPanel() {

		experimentDetailsPanel.clearPanel();
		limsDataPanel.clearPanel();
		designSubsetPanel.clearPanel();
		featureSubsetPanel.clearPanel();
//		projectToolbar.noProject();
	}

	public void closeExperiment() {

		experimentDetailsPanel.clearPanel();
		limsDataPanel.clearPanel();
		designSubsetPanel.closeExperiment();
		featureSubsetPanel.closeExperiment();
	}

	public void setActiveFeatureSubset(MsFeatureSet features) {

		if(currentExperiment == null || activeDataPipeline == null)
			return;
		
		features.setActive(true);
		featureSubsetPanel.switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		
		super.switchDataPipeline(project, newDataPipeline);
		clearPanel();
		if(currentExperiment == null) {
//			projectToolbar.updateGuiFromProjectAndDataPipeline(null, null);
			return;
		}
		experimentDetailsPanel.switchDataPipeline(currentExperiment, activeDataPipeline);
		featureSubsetPanel.switchDataPipeline(currentExperiment, activeDataPipeline);
		designSubsetPanel.switchDataPipeline(currentExperiment, activeDataPipeline);
		if(currentExperiment.getLimsProject() != null) {
			limsDataPanel.setDataFromLimsObjects(
					currentExperiment.getLimsProject(),
					currentExperiment.getLimsExperiment());
		}
//		projectToolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		super.actionPerformed(e);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {
		// TODO Auto-generated method stub
		reloadDesign();
	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {
		// TODO Auto-generated method stub
		reloadDesign();
	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if(!e.getValueIsAdjusting()) {

		}
	}

	public FeatureSubsetPanel getFeatureSubsetPanel() {
		return featureSubsetPanel;
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}
}




