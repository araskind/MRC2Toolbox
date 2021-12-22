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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.event.ListSelectionEvent;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.expdesign.DesignSubsetPanel;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.featurelist.FeatureSubsetPanel;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.projinfo.DockableLIMSDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.projinfo.ProjectDetailsPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;

public class ProjectSetupDraw extends DockableMRC2ToolboxPanel {

	private ProjectDetailsPanel projectDetailsPanel;
	private DockableLIMSDataPanel limsDataPanel;
	private DesignSubsetPanel designSubsetPanel;
	private FeatureSubsetPanel featureSubsetPanel;
	private ProjectToolbar projectToolbar;

	private static final Icon componentIcon = GuiUtils.getIcon("MDAToolkit_icon", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "ProjectSetupDraw.layout");

	public ProjectSetupDraw() {

		super("ProjectSetupDraw", "Settings", componentIcon);
		setLayout(new BorderLayout(0, 0));
		
		projectToolbar = new ProjectToolbar(null);
		add(projectToolbar, BorderLayout.NORTH);

		projectDetailsPanel = new ProjectDetailsPanel();
		limsDataPanel = new DockableLIMSDataPanel();
		designSubsetPanel = new DesignSubsetPanel();
		featureSubsetPanel = new FeatureSubsetPanel();

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid( control );
		grid.add( 0, 0, 100, 30, projectDetailsPanel, limsDataPanel);
		grid.add( 0, 30, 100, 30, designSubsetPanel);
		grid.add( 0, 60, 100, 30, featureSubsetPanel);
		grid.select(0, 0, 100, 30, projectDetailsPanel);
		control.getContentArea().deploy( grid );

		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
	}
	
	public void setActionListener(ActionListener listener) {
		projectToolbar.setActionListener(listener);
	}

	public synchronized void clearPanel() {

		projectDetailsPanel.clearPanel();
		limsDataPanel.clearPanel();
		designSubsetPanel.clearPanel();
		featureSubsetPanel.clearPanel();
		projectToolbar.noProject();
	}

	public void closeProject() {

		projectDetailsPanel.clearPanel();
		limsDataPanel.clearPanel();
		designSubsetPanel.closeProject();
		featureSubsetPanel.closeProject();
	}

	public void setActiveFeatureSubset(MsFeatureSet features) {

		if(currentProject == null || activeDataPipeline == null)
			return;
		
		features.setActive(true);
		featureSubsetPanel.switchDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		
		super.switchDataPipeline(project, newDataPipeline);
		clearPanel();
		if(currentProject == null) {
			projectToolbar.updateGuiFromProjectAndDataPipeline(null, null);
			return;
		}
		projectDetailsPanel.switchDataPipeline(currentProject, activeDataPipeline);
		featureSubsetPanel.switchDataPipeline(currentProject, activeDataPipeline);
		designSubsetPanel.switchDataPipeline(currentProject, activeDataPipeline);
		if(currentProject.getLimsProject() != null) {
			limsDataPanel.setDataFromLimsObjects(
					currentProject.getLimsProject(),
					currentProject.getLimsExperiment());
		}
		projectToolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

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
}




