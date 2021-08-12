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

package edu.umich.med.mrc2.datoolbox.gui.clustertree;

import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableClusterTree extends DefaultSingleCDockable {

	private ClusterTree clusterTree;
	private static final Icon componentIcon = GuiUtils.getIcon("cluster", 16);

	public DockableClusterTree(String id, String title, ActionListener featurePopupListener, TreeSelectionListener tsl) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		clusterTree = new ClusterTree(featurePopupListener);
		clusterTree.addTreeSelectionListener(tsl);
		add(new JScrollPane(clusterTree));
	}

	public ClusterTree getTree() {
		return clusterTree;
	}

	public void expandCluster(MsFeatureCluster cluster) {
		clusterTree.expandCluster(cluster);
	}

	public void expandClusterBranch() {
		clusterTree.expandClusterBranch();
	}

	public void collapseTree() {
		clusterTree.collapseTree();
	}

	public void expandTree() {
		clusterTree.expandTree();
	}

	public Object getClickedObject() {
		return clusterTree.getClickedObject();
	}

	public Object getClickedParentCluster() {
		return clusterTree.getClickedParentCluster();
	}

	public Object getClusterForSelectedFeature() {
		return clusterTree.getClusterForSelectedFeature();
	}

	public ClusterTreeModel getModel() {
		return clusterTree.getModel();
	}

	public MsFeatureCluster[] getSelectedClusters() {
		return clusterTree.getSelectedClusters();
	}

	public Collection<MsFeature> getSelectedFeatures() {
		return clusterTree.getSelectedFeatures();
	}

	public SortProperty getSortByProperty() {
		return clusterTree.getSortByProperty();
	}

	public SortDirection getSortDirection() {
		return clusterTree.getSortDirection();
	}

	public void loadFeatureClusters(Collection<MsFeatureCluster> clusterList) {
		clusterTree.loadFeatureClusters(clusterList);
	}

	public void removeClickedObject() {
		clusterTree.removeClickedObject();
	}

	public void removeFeature(MsFeature feature) {
		clusterTree.removeFeature(feature);
	}

	public void removeFeatureCluster(MsFeatureCluster cluster) {
		clusterTree.removeFeatureCluster(cluster);
	}

	public void removeSelectededObjects() {
		clusterTree.removeSelectededObjects();
	}

	public void resetTree() {
		clusterTree.resetTree();
	}

	public void resortTree() {
		clusterTree.resortTree();
	}

	public void selectFeatureCluster(MsFeatureCluster cluster) {
		clusterTree.selectFeatureCluster(cluster);
	}

	public void setSortByProperty(SortProperty sortByProperty) {
		clusterTree.setSortByProperty(sortByProperty);
	}

	public void setSortDirection(SortDirection sortDirection) {
		clusterTree.setSortDirection(sortDirection);
	}

	public void sortTree(SortProperty property, SortDirection direction) {
		clusterTree.sortTree(property, direction);
	}

	public void updateElement(Object element){
		clusterTree.updateElement(element);
	}

	public TreePath findfeaturePath(MsFeature cf) {
		return clusterTree.findfeaturePath(cf);
	}

	public void highlightPath(TreePath clusterPath) {

		clusterTree.setSelectionPath(clusterPath);
		clusterTree.scrollPathToVisible(clusterPath);
	}
}





