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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.tree;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMotrpacProjectTree extends DefaultSingleCDockable {

	private MotrpacProjectTree projectTree;
	private static final Icon componentIcon = GuiUtils.getIcon("idExperiment", 16);

	public DockableMotrpacProjectTree(TreeSelectionListener tsl) {

		super("DockableMotrpacProjectTree", componentIcon, "MoTrPAC study tree", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		projectTree = new MotrpacProjectTree((ActionListener)tsl);
		projectTree.addTreeSelectionListener(tsl);
		add(new JScrollPane(projectTree));
	}

	public MotrpacProjectTree getTree() {
		return projectTree;
	}

	public void loadIdTrackerData() {
		projectTree.loadIdTrackerData();
	}

	public LIMSProject getSelectedProject() {
		return projectTree.getSelectedProject();
	}


	public LIMSExperiment getSelectedExperiment() {
		return projectTree.getSelectedExperiment();
	}

	public LIMSSamplePreparation getSelectedSamplePrep() {
		return projectTree.getSelectedSamplePrep();
	}

	public DataAcquisitionMethod getSelectedAcquisitionMethod() {
		return projectTree.getSelectedAcquisitionMethod();
	}

	public DataExtractionMethod getSelectedDataExtractionMethod() {
		return projectTree.getSelectedDataExtractionMethod();
	}

	public void updateNodeForObject(Object o) {
		projectTree.updateNodeForObject(o);
	}


	public LIMSProject getProjectForExperiment(LIMSExperiment experiment) {

		Object project = projectTree.getParentObject(experiment);
		if(project != null)
			return (LIMSProject) project;

		return null;
	}

	public LIMSExperiment getExperimentForSamplePrep(LIMSSamplePreparation samplePrep) {

		Object project = projectTree.getParentObject(samplePrep);
		if(project != null)
			return (LIMSExperiment) project;

		return null;
	}

	public LIMSSamplePreparation getSamplePrepForAcquisitionMethod( DataAcquisitionMethod acqMethod) {

		Object project = projectTree.getParentObject(acqMethod);
		if(project != null)
			return (LIMSSamplePreparation) project;

		return null;
	}

	public DataAcquisitionMethod getAcquisitionMethodForDataExtractionMethod( DataExtractionMethod dxMethod) {

		Object project = projectTree.getParentObject(dxMethod);
		if(project != null)
			return (DataAcquisitionMethod) project;

		return null;
	}
	
	public synchronized void clearPanel() {
		projectTree.getModel().clearModel();
	}
}













