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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.projecttree;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableIdProjectTree extends DefaultSingleCDockable {

	private IdProjectTree projectTree;
	private static final Icon componentIcon = GuiUtils.getIcon("idExperiment", 16);

	public DockableIdProjectTree(TreeSelectionListener tsl) {

		super("DockableIdProjectTree", componentIcon, "ID tracker project tree", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		projectTree = new IdProjectTree((ActionListener)tsl);
		projectTree.addTreeSelectionListener(tsl);
		add(new JScrollPane(projectTree));
	}

	public IdProjectTree getTree() {
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
		if(project != null && project instanceof LIMSProject)
			return (LIMSProject) project;

		return null;
	}

	public LIMSExperiment getExperimentForSamplePrep(LIMSSamplePreparation samplePrep) {

		Object experiment = projectTree.getParentObject(samplePrep);
		if(experiment != null && experiment instanceof LIMSExperiment )
			return (LIMSExperiment) experiment;

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
	
	public LIMSExperiment getExperimentForNode(DefaultMutableTreeNode objectNode) {

		Object nodeObject = objectNode.getUserObject();
		if(nodeObject instanceof LIMSExperiment)
			return (LIMSExperiment) nodeObject;
		
		if(nodeObject instanceof LIMSSamplePreparation)			
			return (LIMSExperiment)((DefaultMutableTreeNode)objectNode.getParent()).getUserObject();
				
		if(nodeObject instanceof DataAcquisitionMethod)			
			return (LIMSExperiment)((DefaultMutableTreeNode)objectNode.getParent().getParent()).getUserObject();
		
		if(nodeObject instanceof DataExtractionMethod)			
			return (LIMSExperiment)((DefaultMutableTreeNode)objectNode.getParent().getParent().getParent()).getUserObject();
		
//		Object experiment = projectTree.getParentObject(samplePrep);
//		if(experiment != null && experiment instanceof LIMSExperiment )
//			return (LIMSExperiment) experiment;

		return null;
	}
	
	public void selectNodeForObject(Object o) {
		projectTree.selectNodeForObject(o);
	}
	
	public void addObject(Object o) {
		
		Runnable swingCode = new Runnable() {

			public void run() {
				projectTree.getModel().addObject(o);
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeAndWait(swingCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void removeObject(Object o) {
		
		Runnable swingCode = new Runnable() {
			public void run() {
				projectTree.getModel().removeObject(o);
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeAndWait(swingCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void expandNodeForObject(Object o) {
		projectTree.expandNodeForObject(o);
	}
	
	public synchronized void clearPanel() {
		projectTree.getModel().clearModel();
	}
}













