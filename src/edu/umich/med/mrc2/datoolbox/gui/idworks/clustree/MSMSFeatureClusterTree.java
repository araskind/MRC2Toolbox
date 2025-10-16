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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.SuppressMouseEditor;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class MSMSFeatureClusterTree extends JTree implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 3920348848899169693L;
	private MSMSFeatureClusterTreeModel treeModel;
	private MSMSFeatureClusterTreeRenderer renderer;
	private MSMSFeatureClusterTreeMouseHandler mouseHandler;

	private SortProperty sortByProperty;
	private SortDirection sortDirection;
//	private TreeDropTarget dropTarget;
//	private TreeDragSource dragSource;

	private SuppressMouseEditor cellEditor;

	public MSMSFeatureClusterTree(ActionListener featurePopupListener) {

		treeModel = new MSMSFeatureClusterTreeModel();
		setModel(treeModel);
		setRowHeight(0);
		renderer = new MSMSFeatureClusterTreeRenderer();
		setCellRenderer(renderer);

		mouseHandler = new MSMSFeatureClusterTreeMouseHandler(this, featurePopupListener);
		addMouseListener(mouseHandler);

		setEditable(true);

		getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		cellEditor = new SuppressMouseEditor(
				this, (DefaultTreeCellRenderer) this.getCellRenderer());
		setCellEditor(cellEditor);

		setRootVisible(true);
		setShowsRootHandles(false);

		// Activate drag&drop
//		dragSource = new TreeDragSource(this, DnDConstants.ACTION_MOVE);
//		dropTarget = new TreeDropTarget(this);

		sortByProperty = SortProperty.RT;
		sortDirection = SortDirection.ASC;
		
		
	}

	public void setFeaturePopupMenu(JPopupMenu featurePopupMenu) {
		mouseHandler.setFeaturePopup(featurePopupMenu);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if (command.equals(MainActionCommands.REMOVE_SELECTED_FROM_CLUSTER_COMMAND.getName()))
			deleteFeature();

		if (command.equals(MainActionCommands.SORT_BY_AREA_COMMAND.getName()))
			sortTree(SortProperty.Area, SortDirection.DESC);

		if (command.equals(MainActionCommands.SORT_BY_MZ_COMMAND.getName()))
			sortTree(SortProperty.MZ, SortDirection.ASC);

		if (command.equals(MainActionCommands.SORT_BY_FNUM_COMMAND.getName()))
			sortTree(SortProperty.featureCount, SortDirection.DESC);

		if (command.equals(MainActionCommands.SORT_BY_CLUSTER_NAME_COMMAND.getName()))
			sortTree(SortProperty.Name, SortDirection.ASC);

		if (command.equals(MainActionCommands.SORT_BY_RT_COMMAND.getName()))
			sortTree(SortProperty.RT, SortDirection.ASC);
		
		if (command.equals(MainActionCommands.SORT_BY_RANK_COMMAND.getName()))
			sortTree(SortProperty.Rank, SortDirection.DESC);

//		if (command.equals(MainActionCommands.CREATE_XIC_METHOD_COMMAND.getName()))
//			createXicMethodForSelectedClusters();
//
//		if (command.equals(MainActionCommands.CREATE_XIC_METHOD_SET_COMMAND.getName()))
//			createXicMethodForAllClusters();

		if (command.equals(MainActionCommands.TOGGLE_CLUSTER_LOCK_COMMAND.getName()))
			toggleClusterLock();
	}

	public void expandCluster(IMsFeatureInfoBundleCluster cluster) {

		int row = 0;
		TreePath treePath;

		while (row < this.getRowCount()) {

			treePath = this.getPathForRow(row);

			if (treePath.getLastPathComponent() instanceof DefaultMutableTreeNode) {

				if (((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject().equals(cluster)) {

					this.expandRow(row);
					break;
				}
			}
			row++;
		}
	}

	public void expandClusterBranch() {

		int row = 0;
		TreePath treePath;

		while (row < this.getRowCount()) {

			treePath = this.getPathForRow(row);

			if (treePath.getLastPathComponent().toString().equals(MSMSFeatureClusterTreeModel.clusterNodeName)) {
				this.expandRow(row);
				break;
			}
			row++;
		}
	}

	public void collapseTree() {

		int row = getRowCount() - 1;

		while (row >= 2) {
			collapseRow(row);
			row--;
		}
		DefaultMutableTreeNode currentTreeNode = (DefaultMutableTreeNode) treeModel.getClustersNode();

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

	public Object getClickedObject() {

		int selectedRows[] = getSelectionRows();

		if ((selectedRows == null) || (selectedRows.length == 0))
			return (Object) Array.newInstance(Object.class, 0);

		Arrays.sort(selectedRows);

		TreePath path = getPathForRow(selectedRows[0]);
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

		Object selectedObject = selectedNode.getUserObject();

		return selectedObject;
	}

	public Object getClickedParentCluster() {

		int selectedRows[] = getSelectionRows();

		if ((selectedRows == null) || (selectedRows.length == 0))
			return (Object) Array.newInstance(Object.class, 0);

		Arrays.sort(selectedRows);

		TreePath path = getPathForRow(selectedRows[0]);
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) path
				.getLastPathComponent()).getParent();

		Object selectedObject = parentNode.getUserObject();

		return selectedObject;
	}

	public Object getClusterForSelectedFeature() {

		int selectedRows[] = getSelectionRows();
		Object parentObject = null;

		if ((selectedRows == null) || (selectedRows.length == 0))
			return null;

		Arrays.sort(selectedRows);

		TreePath path = getPathForRow(selectedRows[0]);
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

		Object selectedObject = selectedNode.getUserObject();

		if (selectedObject instanceof MSFeatureInfoBundle) {

			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();

			parentObject = parentNode.getUserObject();
		}
		return parentObject;
	}

	@Override
	public MSMSFeatureClusterTreeModel getModel() {
		return treeModel;
	}

	public IMsFeatureInfoBundleCluster[] getSelectedClusters() {
		Collection<IMsFeatureInfoBundleCluster>selected = 
				getSelectedClustersCollection();
		return selected.toArray(new IMsFeatureInfoBundleCluster[selected.size()]);
	}
	
	public Collection<IMsFeatureInfoBundleCluster> getSelectedClustersCollection() {

		ArrayList<IMsFeatureInfoBundleCluster> selectedObjects = 
				new ArrayList<IMsFeatureInfoBundleCluster>();
		int selectedRows[] = getSelectionRows();

		if ((selectedRows == null) || (selectedRows.length == 0))
			return selectedObjects;

		Arrays.sort(selectedRows);

		for (int row : selectedRows) {
			TreePath path = getPathForRow(row);
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

			Object selectedObject = selectedNode.getUserObject();

			if (selectedObject instanceof IMsFeatureInfoBundleCluster)
				selectedObjects.add((IMsFeatureInfoBundleCluster) selectedObject);
		}
		return selectedObjects;
	}

	public Collection<MSFeatureInfoBundle> getSelectedFeatures() {

		ArrayList<MSFeatureInfoBundle> selectedObjects = new ArrayList<MSFeatureInfoBundle>();
		int selectedRows[] = getSelectionRows();

		if ((selectedRows == null) || (selectedRows.length == 0))
			return selectedObjects;

		Arrays.sort(selectedRows);

		for (int row : selectedRows) {
			TreePath path = getPathForRow(row);
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

			Object selectedObject = selectedNode.getUserObject();

			if (selectedObject instanceof MSFeatureInfoBundle)
				selectedObjects.add((MSFeatureInfoBundle) selectedObject);
		}
		return selectedObjects;
	}

	public SortProperty getSortByProperty() {
		return sortByProperty;
	}

	public SortDirection getSortDirection() {
		return sortDirection;
	}

	public void loadFeatureClusters(Collection<IMsFeatureInfoBundleCluster> clusterList) {

		Runnable swingCode = new Runnable() {

			public void run() {

				treeModel.clearClusters();
				clusterList.stream().
					sorted(new MsFeatureInfoBundleClusterComparator(sortByProperty, sortDirection)).
					forEach(c -> treeModel.addObject(c));

				expandClusterBranch();
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

	public void removeClickedObject() {

		int selectedRows[] = getSelectionRows();

		if ((selectedRows == null) || (selectedRows.length == 0))
			return;

		Arrays.sort(selectedRows);

		TreePath path = getPathForRow(selectedRows[0]);
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

		treeModel.removeNodeFromParent(selectedNode);

		return;
	}

	public void removeFeature(MSFeatureInfoBundle feature) {

		TreePath clusterPath = findPath(feature);

		if (clusterPath != null) {

			DefaultMutableTreeNode featureNode = (DefaultMutableTreeNode) clusterPath.getLastPathComponent();
			treeModel.removeNodeFromParent(featureNode);
		}
	}

	public void removeFeatureCluster(IMsFeatureInfoBundleCluster cluster) {

		TreePath clusterPath = findPath(cluster);

		if (clusterPath != null) {

			DefaultMutableTreeNode clusterNode = (DefaultMutableTreeNode) clusterPath.getLastPathComponent();
			treeModel.removeNodeFromParent(clusterNode);
		}
	}


	public void removeSelectededObjects() {

		int selectedRows[] = getSelectionRows();

		if ((selectedRows == null) || (selectedRows.length == 0))
			return;

		Arrays.sort(selectedRows);

		TreePath path;
		DefaultMutableTreeNode selectedNode;

		for (int i = 0; i < selectedRows.length; i++) {

			path = getPathForRow(selectedRows[i]);
			selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			treeModel.removeNodeFromParent(selectedNode);
		}
		return;
	}

	public void resetTree() {
		treeModel.clearModel();
	}

	public void resortTree() {

		IMsFeatureInfoBundleCluster[] sortedClusters = treeModel.getClusters();

		Arrays.sort(sortedClusters, new MsFeatureInfoBundleClusterComparator(sortByProperty, sortDirection));

		treeModel.clearClusters();

		for (IMsFeatureInfoBundleCluster fc : sortedClusters)
			treeModel.addObject(fc);

		expandClusterBranch();
	}

	public void selectFeatureCluster(IMsFeatureInfoBundleCluster cluster) {

		TreePath clusterPath = findPath(cluster);

		if (clusterPath != null) {

			setSelectionPath(clusterPath);
			scrollPathToVisible(clusterPath);
		}
	}

	public void setSortByProperty(SortProperty sortByProperty) {
		this.sortByProperty = sortByProperty;
	}

	public void setSortDirection(SortDirection sortDirection) {
		this.sortDirection = sortDirection;
	}

	public void sortTree(SortProperty property, SortDirection direction) {

		sortByProperty = property;
		sortDirection = direction;

		IMsFeatureInfoBundleCluster[] sortedClusters = treeModel.getClusters();

		Arrays.sort(sortedClusters, new MsFeatureInfoBundleClusterComparator(property, direction));

		treeModel.clearClusters();

		for (IMsFeatureInfoBundleCluster fc : sortedClusters)
			treeModel.addObject(fc);

		expandClusterBranch();
	}

	/**
	 * @param element
	 * Redraw feature or cluster when their data are changed
	 */
	public void updateElement(Object element){

//		Enumeration<TreeNode> e = ((DefaultMutableTreeNode) treeModel.getRoot()).depthFirstEnumeration();
//
//		while (e.hasMoreElements()) {
//
//			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
//
//			if (node.getUserObject().equals(element))
//				((DefaultTreeModel)this.getModel()).nodeChanged(node);
//		}
		
		DefaultMutableTreeNode node = treeModel.getNodeForObject(element);
		if(node != null)
			treeModel.nodeChanged(node);		
	}

	public TreePath findfeaturePath(MSFeatureInfoBundle cf) {

		DefaultMutableTreeNode root = treeModel.clustersNode;

		@SuppressWarnings("unchecked")
		Enumeration<TreeNode> e = root.depthFirstEnumeration();

		while (e.hasMoreElements()) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();

			if (node.getUserObject().equals(cf)) {

				DefaultMutableTreeNode cluster = (DefaultMutableTreeNode) node.getParent();
				return new TreePath(cluster.getPath());
			}
		}
		return null;
	}


	private void deleteFeature() {
		// TODO Auto-generated method stub

	}

	private void toggleClusterLock() {

		IMsFeatureInfoBundleCluster[] selected = getSelectedClusters();

		for (IMsFeatureInfoBundleCluster c : selected)
			c.setLocked(!c.isLocked());
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

//	private void createXicMethodForAllClusters() {
//
//		for (IMsFeatureInfoBundleCluster fc : treeModel.getClusters())
//			generateXicForCluster(fc);
//	}
//
//	private void createXicMethodForSelectedClusters() {
//
//		for (IMsFeatureInfoBundleCluster fc : this.getSelectedClusters())
//			generateXicForCluster(fc);
//	}
//	
//	private void generateXicForCluster(IMsFeatureInfoBundleCluster fc) {
//
//		XicMethodGenerator xmg = new XicMethodGenerator(fc, true);
//		xmg.createXicMethod();
//	}
	
	
}
