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

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MSMSFeatureClusterTreeMouseHandler extends MouseAdapter {

	private static final Icon deleteIcon = GuiUtils.getIcon("delete", 24);
	private static final Icon editIcon = GuiUtils.getIcon("edit", 24);
	private static final Icon sortByAreaIcon = GuiUtils.getIcon("sortByArea", 24);
	private static final Icon sortByMzIcon = GuiUtils.getIcon("sortByMz", 24);
	private static final Icon sortByFeatureNumIcon = GuiUtils.getIcon("sortByFeatureNum", 24);
	private static final Icon sortByClusterNameIcon = GuiUtils.getIcon("sortByClusterName", 24);
	private static final Icon sortByRtIcon = GuiUtils.getIcon("sortByRt", 24);
	private static final Icon sortByRankIcon = GuiUtils.getIcon("rank", 24);
	private static final Icon annotationIcon = GuiUtils.getIcon("msAnnotation", 24);
	private static final Icon qualIcon = GuiUtils.getIcon("AgtQual", 24);
	private static final Icon lockIcon = GuiUtils.getIcon("locked", 24);
	private static final Icon forkIcon = GuiUtils.getIcon("fork", 24);
	private static final Icon showAllSpectraIcon = GuiUtils.getIcon("showAllSpectra", 24);
	private static final Icon recalculateCorrelationsIcon = GuiUtils.getIcon("recalculateCorrelations", 24);
	private static final Icon copyAsMspIcon = GuiUtils.getIcon("exportToMSP", 24);
	private static final Icon copyAsSiriusIcon = GuiUtils.getIcon("sirius", 24);

	private MSMSFeatureClusterTree tree;
	private JPopupMenu 
		clusterPopupMenu, 
		sortPopupMenu, 
		featurePopupMenu;

	private static JMenuItem
		deleteClusterMenuItem,
		dissolveClusterMenuItem,
		editClusterMenuItem,
		copyAsMspMenuItem,
		copyAsSiriusMenuItem,
		createXicMenuItem,
		sortByAreaMenuItem,
		sortByMzMenuItem,
		sortByFnumMenuItem,
		sortByCnumMenuItem,
		sortByRTMenuItem,
		sortByRankMenuItem,
		createXicSetMenuItem,
		assingnAnnotationMenuItem,
		toggleLockMenuItem,
		showAllSpectraMenuItem,
		recalculateCorrelationsMenuItem;

	/**
	 * Constructor
	 */
	public MSMSFeatureClusterTreeMouseHandler(
			MSMSFeatureClusterTree clusterTree, 
			ActionListener featurePopupListener) {

		this.tree = clusterTree;

		sortPopupMenu = new JPopupMenu();

		sortByAreaMenuItem = GuiUtils.addMenuItem(sortPopupMenu, 
				MainActionCommands.SORT_BY_AREA_COMMAND.getName(), tree, 
				MainActionCommands.SORT_BY_AREA_COMMAND.getName());
		sortByAreaMenuItem.setIcon(sortByAreaIcon);

		sortByMzMenuItem = GuiUtils.addMenuItem(sortPopupMenu, 
				MainActionCommands.SORT_BY_MZ_COMMAND.getName(), tree,
				MainActionCommands.SORT_BY_MZ_COMMAND.getName());
		sortByMzMenuItem.setIcon(sortByMzIcon);

		sortByFnumMenuItem = GuiUtils.addMenuItem(sortPopupMenu, 
				MainActionCommands.SORT_BY_FNUM_COMMAND.getName(), tree,
				MainActionCommands.SORT_BY_FNUM_COMMAND.getName());
		sortByFnumMenuItem.setIcon(sortByFeatureNumIcon);

		sortByCnumMenuItem = GuiUtils.addMenuItem(sortPopupMenu,
				MainActionCommands.SORT_BY_CLUSTER_NAME_COMMAND.getName(), tree,
				MainActionCommands.SORT_BY_CLUSTER_NAME_COMMAND.getName());
		sortByCnumMenuItem.setIcon(sortByClusterNameIcon);

		sortByRTMenuItem = GuiUtils.addMenuItem(sortPopupMenu, 
				MainActionCommands.SORT_BY_RT_COMMAND.getName(), tree,
				MainActionCommands.SORT_BY_RT_COMMAND.getName());
		sortByRTMenuItem.setIcon(sortByRtIcon);
				
		sortByRankMenuItem = GuiUtils.addMenuItem(sortPopupMenu, 
				MainActionCommands.SORT_BY_RANK_COMMAND.getName(), tree,
				MainActionCommands.SORT_BY_RANK_COMMAND.getName());
		sortByRankMenuItem.setIcon(sortByRankIcon);

//		createXicSetMenuItem = GuiUtils.addMenuItem(sortPopupMenu,
//				MainActionCommands.CREATE_XIC_METHOD_SET_COMMAND.getName(), tree,
//				MainActionCommands.CREATE_XIC_METHOD_SET_COMMAND.getName());
//		createXicSetMenuItem.setIcon(qualIcon);

		clusterPopupMenu = new JPopupMenu();

//		dissolveClusterMenuItem = GuiUtils.addMenuItem(clusterPopupMenu,
//				MainActionCommands.DISSOLVE_CLUSTER_COMMAND.getName(), featurePopupListener,
//				MainActionCommands.DISSOLVE_CLUSTER_COMMAND.getName());
//		dissolveClusterMenuItem.setIcon(forkIcon);

		editClusterMenuItem = GuiUtils.addMenuItem(clusterPopupMenu, 
				MainActionCommands.EDIT_CLUSTER_COMMAND.getName(), featurePopupListener,
				MainActionCommands.EDIT_CLUSTER_COMMAND.getName());
		editClusterMenuItem.setIcon(editIcon);

		deleteClusterMenuItem = GuiUtils.addMenuItem(clusterPopupMenu,
				MainActionCommands.DELETE_CLUSTER_COMMAND.getName(), featurePopupListener,
				MainActionCommands.DELETE_CLUSTER_COMMAND.getName());
		deleteClusterMenuItem.setIcon(deleteIcon);

		assingnAnnotationMenuItem = GuiUtils.addMenuItem(clusterPopupMenu,
				MainActionCommands.ANNOTATE_CLUSTER_COMMAND.getName(), featurePopupListener,
				MainActionCommands.ANNOTATE_CLUSTER_COMMAND.getName());
		assingnAnnotationMenuItem.setIcon(annotationIcon);
		
		clusterPopupMenu.addSeparator();

		copyAsMspMenuItem = GuiUtils.addMenuItem(clusterPopupMenu,
				MainActionCommands.COPY_CLUSTER_FEATURES_AS_MSP.getName(), featurePopupListener,
				MainActionCommands.COPY_CLUSTER_FEATURES_AS_MSP.getName());
		copyAsMspMenuItem.setIcon(copyAsMspIcon);
		
		copyAsSiriusMenuItem = GuiUtils.addMenuItem(clusterPopupMenu,
				MainActionCommands.COPY_CLUSTER_FEATURES_AS_SIRIUS_MS.getName(), featurePopupListener,
				MainActionCommands.COPY_CLUSTER_FEATURES_AS_SIRIUS_MS.getName());
		copyAsSiriusMenuItem.setIcon(copyAsSiriusIcon);
		
//		showAllSpectraMenuItem = GuiUtils.addMenuItem(clusterPopupMenu,
//				MainActionCommands.SHOW_ALL_CLUSTER_SPECTRA_COMMAND.getName(), featurePopupListener,
//				MainActionCommands.SHOW_ALL_CLUSTER_SPECTRA_COMMAND.getName());
//		showAllSpectraMenuItem.setIcon(showAllSpectraIcon);
//
//		recalculateCorrelationsMenuItem = GuiUtils.addMenuItem(clusterPopupMenu,
//				MainActionCommands.RECALCULATE_CORRRELATIONS_4CLUSTER_COMMAND.getName(), featurePopupListener,
//				MainActionCommands.RECALCULATE_CORRRELATIONS_4CLUSTER_COMMAND.getName());
//		recalculateCorrelationsMenuItem.setIcon(recalculateCorrelationsIcon);

		clusterPopupMenu.addSeparator();
		
		toggleLockMenuItem = GuiUtils.addMenuItem(clusterPopupMenu,
				MainActionCommands.TOGGLE_CLUSTER_LOCK_COMMAND.getName(), tree,
				MainActionCommands.TOGGLE_CLUSTER_LOCK_COMMAND.getName());
		toggleLockMenuItem.setIcon(lockIcon);
	}

	public void setFeaturePopup(JPopupMenu featurePopupMenu) {
		this.featurePopupMenu = featurePopupMenu;
	}

	// TODO double-click in cluster tree. Is it necessary?
	private void handleDoubleClickEvent(MouseEvent e) {

		TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());

		if (clickedPath == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) clickedPath.getLastPathComponent();
		Object clickedObject = node.getUserObject();

		if (clickedObject instanceof IMsFeatureInfoBundleCluster) {

		}

		if (clickedObject instanceof MSFeatureInfoBundle) {

		}
	}

	private void handlePopupTriggerEvent(MouseEvent e) {

		TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());

		if (clickedPath == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) clickedPath.getLastPathComponent();

		Object clickedObject = node.getUserObject();

		if (clickedObject instanceof IMsFeatureInfoBundleCluster)
			clusterPopupMenu.show(e.getComponent(), e.getX(), e.getY());

		if (clickedObject instanceof MSFeatureInfoBundle && featurePopupMenu != null)
			featurePopupMenu.show(e.getComponent(), e.getX(), e.getY());

		if (node.equals(tree.getModel().getClustersNode()))
			sortPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	public void mousePressed(MouseEvent e) {

		if (e.isPopupTrigger())
			handlePopupTriggerEvent(e);

		if ((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1))
			handleDoubleClickEvent(e);
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger())
			handlePopupTriggerEvent(e);
	}
}
