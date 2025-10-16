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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.projecttree;

import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;

public class IdProjectTree extends JTree {

	/**
	 *
	 */
	private static final long serialVersionUID = -5067942612276317764L;
	private IdProjectTreeModel treeModel;

	public IdProjectTree(ActionListener popupListener) {
		super();
		treeModel = new IdProjectTreeModel();
		setModel(treeModel);
		setRowHeight(0);
		setCellRenderer(new IdProjectTreeRenderer());
		addMouseListener(new IdProjectTreeMouseHandler(this, popupListener));
		setEditable(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setRootVisible(true);
		setShowsRootHandles(false);
	}

	@Override
	public IdProjectTreeModel getModel() {
		return treeModel;
	}

	public void loadIdTrackerData() {

		Runnable swingCode = new Runnable() {

			public void run() {

				getModel().clearModel();
				IDTDataCache.getProjects().stream().sorted().forEach(p -> getModel().addProject(p));
				expandTreeUpToLevel(1);				
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeLater(swingCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void collapseTree() {

		int row = getRowCount() - 1;

		while (row >= 2) {
			collapseRow(row);
			row--;
		}
		DefaultMutableTreeNode currentTreeNode = (DefaultMutableTreeNode) getModel().getProjectsNode();
		while (currentTreeNode != null) {

			if (currentTreeNode.getLevel() <= 1)
				expandPath(new TreePath(currentTreeNode.getPath()));

			currentTreeNode = currentTreeNode.getNextNode();
		}
	}

	public void expandTree() {

		int row = 0;
		while (row < getRowCount()) {
			expandRow(row);
			row++;
		}
	}

	public void expandTreeUpToLevel(int level) {

		DefaultMutableTreeNode currentNode = ((DefaultMutableTreeNode) treeModel.getRoot()).getNextNode();
	    do {
	       if (currentNode.getLevel() <= level)
	            expandPath(new TreePath(currentNode.getPath()));
	       currentNode = currentNode.getNextNode();
	       }
	    while (currentNode != null);
	}
	
	public void expandNodeForObject(Object o) {
		
		DefaultMutableTreeNode node = treeModel.getNodeForObject(o);
		if(node != null)
			expandPath(new TreePath(node.getPath()));
	}

	public LIMSProject getSelectedProject() {

		TreePath path = getSelectionPath();
		if(path == null)
			return null;

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object selectedObject = selectedNode.getUserObject();
		if (selectedObject instanceof LIMSProject)
			return (LIMSProject)selectedObject;
		else
			return null;
	}

	public LIMSExperiment getSelectedExperiment() {

		TreePath path = getSelectionPath();
		if(path == null)
			return null;

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object selectedObject = selectedNode.getUserObject();
		if (selectedObject instanceof LIMSExperiment)
			return (LIMSExperiment)selectedObject;
		else
			return null;
	}

	public LIMSSamplePreparation getSelectedSamplePrep() {

		TreePath path = getSelectionPath();
		if(path == null)
			return null;

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object selectedObject = selectedNode.getUserObject();
		if (selectedObject instanceof LIMSSamplePreparation)
			return (LIMSSamplePreparation)selectedObject;
		else
			return null;
	}

	public DataAcquisitionMethod getSelectedAcquisitionMethod() {

		TreePath path = getSelectionPath();
		if(path == null)
			return null;

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object selectedObject = selectedNode.getUserObject();
		if (selectedObject instanceof DataAcquisitionMethod)
			return (DataAcquisitionMethod)selectedObject;
		else
			return null;
	}

	public DataExtractionMethod getSelectedDataExtractionMethod() {

		TreePath path = getSelectionPath();
		if(path == null)
			return null;

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object selectedObject = selectedNode.getUserObject();
		if (selectedObject instanceof DataExtractionMethod)
			return (DataExtractionMethod)selectedObject;
		else
			return null;
	}

	public Object getSelectedObject(Class objectClass) {

		TreePath path = getSelectionPath();
		if(path == null)
			return null;

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object selectedObject = selectedNode.getUserObject();
		if (objectClass.isInstance(selectedObject))
			return selectedObject;
		else
			return null;
	}

	public void updateNodeForObject(Object o) {

		DefaultMutableTreeNode ojectNode = treeModel.getNodeForObject(o);
		if(ojectNode != null) {
			ojectNode.setUserObject(o);
			treeModel.nodeChanged(ojectNode);
		}
	}

	public Object getParentObject(Object o) {

		DefaultMutableTreeNode ojectNode = treeModel.getNodeForObject(o);
		if(ojectNode != null)
			return((DefaultMutableTreeNode)ojectNode.getParent()).getUserObject();

		return null;
	}
	
	public void selectNodeForObject(Object o) {
		
		DefaultMutableTreeNode objectNode = treeModel.getNodeForObject(o);
		if(objectNode == null)
			return;
		
		TreePath path = new TreePath(objectNode.getPath());
		expandPath(path);
		scrollPathToVisible(path);
		setSelectionPath(path);		
	}
	
	private TreePath findPath(Object o) {

		Enumeration<TreeNode> e = ((DefaultMutableTreeNode) treeModel.getRoot()).depthFirstEnumeration();

		while (e.hasMoreElements()) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();

			if (node.getUserObject().equals(o))
				return new TreePath(node.getPath());
		}
		return null;
	}
}





























