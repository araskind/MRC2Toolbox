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

package edu.umich.med.mrc2.datoolbox.gui.main;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.DockAction;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleMenuAction;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignListener;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetListener;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetListener;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureListener;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;

public abstract class DockableMRC2ToolboxPanel extends DefaultSingleCDockable
	implements ActionListener, TaskListener, ExperimentDesignListener,
	ExperimentDesignSubsetListener, MsFeatureListener,
	FeatureSetListener, ListSelectionListener, PersistentLayout {

	protected DataAnalysisProject currentProject;
	protected DataPipeline activeDataPipeline;
	protected CControl control;
	protected CGrid grid;
	
	protected static final Icon actionIcon = GuiUtils.getIcon("cog", 16);
	protected DefaultDockActionSource menuActions;

	public DockableMRC2ToolboxPanel(String id, String title, Icon icon) {
		super(id, icon, title, null, Permissions.MIN_MAX_EXT_STACK);
	}

	// Clear panel contents
	public abstract void clearPanel();

	//	refresh if experiment design is updated
	public abstract void reloadDesign();
	
	protected void initActions() {
		
		DefaultDockActionSource actions = new DefaultDockActionSource(
				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));
		
        menuActions = new DefaultDockActionSource();		

		SimpleMenuAction actionMenu = new SimpleMenuAction(menuActions);
		actionMenu.setIcon(actionIcon);
		actionMenu.setText("Actions");       
		actions.add((DockAction)actionMenu);
		intern().setActionOffers(actions);
	}

	//	Load data for data pipeline
	public void switchDataPipeline(
			DataAnalysisProject project, DataPipeline newDataPipeline) {

		currentProject = project;
		activeDataPipeline = newDataPipeline;

		//	Implement specifics in other panels
	}

	public void closeProject() {
		currentProject = null;
		activeDataPipeline = null;
	}

	public abstract File getLayoutFile();

	@Override
	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					control.writeXML(layoutFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void saveLayout(File layoutFile) {

		if(control != null) {

			for(int i=0; i<control.getCDockableCount(); i++) {

				CDockable uiObject = control.getCDockable(i);
				if(uiObject instanceof PersistentLayout)
					((PersistentLayout)uiObject).saveLayout(((PersistentLayout)uiObject).getLayoutFile());
			}
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
