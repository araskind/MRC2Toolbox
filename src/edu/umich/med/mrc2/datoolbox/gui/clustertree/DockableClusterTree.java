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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableClusterTree extends DefaultSingleCDockable implements ActionListener {

	private ClusterTree clusterTree;
	private static final Icon componentIcon = GuiUtils.getIcon("cluster", 16);
	private static final Icon expandTreeIcon = GuiUtils.getIcon("expand", 16);
	private static final Icon collapseTreeIcon = GuiUtils.getIcon("collapse", 16);
	private static final Icon problemClustersIcon = GuiUtils.getIcon("levelInactive", 16);
	private static final Icon allClustersIcon = GuiUtils.getIcon("level", 16);
	
	private SimpleButtonAction
		expandCollapseTreeButton,
		toggleProblemClustersButton;
	
	public DockableClusterTree(String id, String title, 
			ActionListener featurePopupListener, TreeSelectionListener tsl) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		clusterTree = new ClusterTree(featurePopupListener);
		clusterTree.addTreeSelectionListener(tsl);
		add(new JScrollPane(clusterTree));
		initButtons(this, featurePopupListener);
	}
	
	private void initButtons(ActionListener l,
			ActionListener featurePopupListener) {

		DefaultDockActionSource actions = new DefaultDockActionSource(
				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));

		expandCollapseTreeButton = GuiUtils.setupButtonAction(
				MainActionCommands.EXPAND_TREE.getName(),
				MainActionCommands.EXPAND_TREE.getName(), 
				expandTreeIcon, l);
		actions.add(expandCollapseTreeButton);	
		
		toggleProblemClustersButton = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_ONLY_PROBLEM_CLUSTERS_COMMAND.getName(),
				MainActionCommands.SHOW_ONLY_PROBLEM_CLUSTERS_COMMAND.getName(), 
				allClustersIcon, featurePopupListener);
		actions.add(toggleProblemClustersButton);
		
		actions.addSeparator();
		intern().setActionOffers(actions);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();	
		if(command.equals(MainActionCommands.COLLAPSE_TREE.getName()))
			collapseTree();
		
		if(command.equals(MainActionCommands.EXPAND_TREE.getName()))
			expandTree();
	}
	
	public void treeExpanded(boolean expanded) {

		if (expanded) {

			expandCollapseTreeButton.setIcon(collapseTreeIcon);
			expandCollapseTreeButton.setCommand(MainActionCommands.COLLAPSE_TREE.getName());
			expandCollapseTreeButton.setText(MainActionCommands.COLLAPSE_TREE.getName());
			expandCollapseTreeButton.setTooltip(MainActionCommands.COLLAPSE_TREE.getName());
		} else {
			expandCollapseTreeButton.setIcon(expandTreeIcon);
			expandCollapseTreeButton.setCommand(MainActionCommands.EXPAND_TREE.getName());
			expandCollapseTreeButton.setText(MainActionCommands.EXPAND_TREE.getName());
			expandCollapseTreeButton.setTooltip(MainActionCommands.EXPAND_TREE.getName());
		}
	}

	public void showOnlyProblemClusters(boolean probOnly) {

		if (probOnly) {

			toggleProblemClustersButton.setIcon(problemClustersIcon);
			toggleProblemClustersButton.setCommand(MainActionCommands.SHOW_ALL_CLUSTERS_COMMAND.getName());		
			toggleProblemClustersButton.setText(MainActionCommands.SHOW_ALL_CLUSTERS_COMMAND.getName());
			toggleProblemClustersButton.setTooltip(MainActionCommands.SHOW_ALL_CLUSTERS_COMMAND.getName());
		} else {
			toggleProblemClustersButton.setIcon(allClustersIcon);
			toggleProblemClustersButton.setCommand(MainActionCommands.SHOW_ONLY_PROBLEM_CLUSTERS_COMMAND.getName());
			toggleProblemClustersButton.setText(MainActionCommands.SHOW_ONLY_PROBLEM_CLUSTERS_COMMAND.getName());
			toggleProblemClustersButton.setTooltip(MainActionCommands.SHOW_ONLY_PROBLEM_CLUSTERS_COMMAND.getName());
		}
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
		treeExpanded(false);
	}

	public void expandTree() {
		clusterTree.expandTree();
		treeExpanded(true);
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





