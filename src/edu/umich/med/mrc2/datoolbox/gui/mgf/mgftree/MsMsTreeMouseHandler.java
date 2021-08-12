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

package edu.umich.med.mrc2.datoolbox.gui.mgf.mgftree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MsMsTreeMouseHandler extends MouseAdapter {

	private static final Icon sortByMzIcon = GuiUtils.getIcon("sortByMz", 24);
	private static final Icon sortByRtIcon = GuiUtils.getIcon("sortByRt", 24);
	private static final Icon lookupMsMsIcon = GuiUtils.getIcon("searchMsMs", 24);
	private static final Icon copyMsMsIcon = GuiUtils.getIcon("copy", 24);

	public static final String SORT_BY_MZ_COMMAND = "SORT_BY_MZ";
	public static final String SORT_BY_RT_COMMAND = "SORT_BY_RT";
	public static final String LOOKUP_AVERAGE_MSMS_COMMAND = "LOOKUP_AVERAGE_MSMS";
	public static final String LOOKUP_FEATURE_MSMS_COMMAND = "LOOKUP_FEATURE_MSMS";
	public static final String COPY_AVERAGE_MSMS_COMMAND = "COPY_AVERAGE_MSMS";
	public static final String COPY_FEATURE_MSMS_COMMAND = "COPY_FEATURE_MSMS";

	private MsMsTree tree;

	private JPopupMenu
		clusterPopupMenu,
		featurePopupMenu,
		sortPopupMenu;

	private static JMenuItem
		lookupClusterMsMsMenuItem,
		lookupFeatureMsMsMenuItem,
		copyClusterMsMsMenuItem,
		copyFeatureMsMsMenuItem,
		sortByMzMenuItem,
		sortByRTMenuItem;

	/**
	 * Constructor
	 */
	public MsMsTreeMouseHandler(MsMsTree clusterTree) {

		this.tree = clusterTree;

		sortPopupMenu = new JPopupMenu();

		sortByMzMenuItem = GuiUtils.addMenuItem(sortPopupMenu, "Sort by mass", tree, SORT_BY_MZ_COMMAND);
		sortByMzMenuItem.setIcon(sortByMzIcon);

		sortByRTMenuItem = GuiUtils.addMenuItem(sortPopupMenu, "Sort by RT", tree, SORT_BY_RT_COMMAND);
		sortByRTMenuItem.setIcon(sortByRtIcon);

		clusterPopupMenu = new JPopupMenu();
		lookupClusterMsMsMenuItem = GuiUtils.addMenuItem(clusterPopupMenu, "Lookup average MSMS", tree,
				LOOKUP_AVERAGE_MSMS_COMMAND);
		lookupClusterMsMsMenuItem.setIcon(lookupMsMsIcon);
		copyClusterMsMsMenuItem = GuiUtils.addMenuItem(clusterPopupMenu, "Copy average MSMS", tree,
				COPY_AVERAGE_MSMS_COMMAND);
		copyClusterMsMsMenuItem.setIcon(copyMsMsIcon);

		featurePopupMenu = new JPopupMenu();
		lookupFeatureMsMsMenuItem = GuiUtils.addMenuItem(featurePopupMenu, "Lookup feature MSMS", tree,
				LOOKUP_FEATURE_MSMS_COMMAND);
		lookupFeatureMsMsMenuItem.setIcon(lookupMsMsIcon);
		copyFeatureMsMsMenuItem = GuiUtils.addMenuItem(featurePopupMenu, "Copy feature MSMS", tree,
				COPY_FEATURE_MSMS_COMMAND);
		copyFeatureMsMsMenuItem.setIcon(copyMsMsIcon);
	}

	private void handleDoubleClickEvent(MouseEvent e) {

		TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());

		if (clickedPath == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) clickedPath.getLastPathComponent();
		Object clickedObject = node.getUserObject();

		if (clickedObject instanceof MsFeatureCluster) {

		}

		if (clickedObject instanceof MsFeature) {

		}
	}

	private void handlePopupTriggerEvent(MouseEvent e) {

		TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());

		if (clickedPath == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) clickedPath.getLastPathComponent();

		Object clickedObject = node.getUserObject();

		if (clickedObject instanceof MsMsCluster)
			clusterPopupMenu.show(e.getComponent(), e.getX(), e.getY());

		if (clickedObject instanceof SimpleMsMs)
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
