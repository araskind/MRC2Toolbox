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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree;

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
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMSMSFeatureClusterTree extends DefaultSingleCDockable implements ActionListener {

	private MSMSFeatureClusterTree clusterTree;
	private static final Icon componentIcon = GuiUtils.getIcon("cluster", 16);
	private static final Icon expandTreeIcon = GuiUtils.getIcon("expand", 16);
	private static final Icon collapseTreeIcon = GuiUtils.getIcon("collapse", 16);
	
	private SimpleButtonAction
		expandCollapseTreeButton;
	
	public DockableMSMSFeatureClusterTree(
			String id, String title, ActionListener featurePopupListener, TreeSelectionListener tsl) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		clusterTree = new MSMSFeatureClusterTree(featurePopupListener);
		clusterTree.addTreeSelectionListener(tsl);
		add(new JScrollPane(clusterTree));
		initButtons(this);
	}
	
	private void initButtons(ActionListener l) {

		DefaultDockActionSource actions = new DefaultDockActionSource(
				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));

		expandCollapseTreeButton = GuiUtils.setupButtonAction(
				MainActionCommands.EXPAND_TREE.getName(),
				MainActionCommands.EXPAND_TREE.getName(), 
				expandTreeIcon, l);
		actions.add(expandCollapseTreeButton);		
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
			expandCollapseTreeButton.setTooltip("Collapse all cluster nodes");
		} else {
			expandCollapseTreeButton.setIcon(expandTreeIcon);
			expandCollapseTreeButton.setCommand(MainActionCommands.EXPAND_TREE.getName());
			expandCollapseTreeButton.setTooltip("Expand all cluster nodes");
		}
	}

	public MSMSFeatureClusterTree getTree() {
		return clusterTree;
	}

	public void expandCluster(MsFeatureInfoBundleCluster cluster) {
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

	public MSMSFeatureClusterTreeModel getModel() {
		return clusterTree.getModel();
	}

	public MsFeatureInfoBundleCluster[] getSelectedClusters() {
		return clusterTree.getSelectedClusters();
	}

	public Collection<MSFeatureInfoBundle> getSelectedFeatures() {
		return clusterTree.getSelectedFeatures();
	}

	public SortProperty getSortByProperty() {
		return clusterTree.getSortByProperty();
	}

	public SortDirection getSortDirection() {
		return clusterTree.getSortDirection();
	}

	public void loadFeatureClusters(Collection<MsFeatureInfoBundleCluster> clusterList) {
		clusterTree.loadFeatureClusters(clusterList);
	}

	public void removeClickedObject() {
		clusterTree.removeClickedObject();
	}

	public void removeFeature(MSFeatureInfoBundle feature) {
		clusterTree.removeFeature(feature);
	}

	public void removeFeatureCluster(MsFeatureInfoBundleCluster cluster) {
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

	public void selectFeatureCluster(MsFeatureInfoBundleCluster cluster) {
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

	public TreePath findfeaturePath(MSFeatureInfoBundle cf) {
		return clusterTree.findfeaturePath(cf);
	}

	public void highlightPath(TreePath clusterPath) {

		clusterTree.setSelectionPath(clusterPath);
		clusterTree.scrollPathToVisible(clusterPath);
	}

}





