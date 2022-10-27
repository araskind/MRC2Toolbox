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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ClusterTreeRenderer extends DefaultTreeCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = -3568168754034663097L;

	static final Icon featureIcon = GuiUtils.getIcon("feature", 24);
	static final Icon clusterIcon = GuiUtils.getIcon("cluster", 24);
	static final Icon problemClusterIcon = GuiUtils.getIcon("problemCluster", 24);
	static final Icon namedClusterIcon = GuiUtils.getIcon("namedCluster", 24);
	static final Icon multiNamedClusterIcon = GuiUtils.getIcon("multiNamedCluster", 24);

	static final Font bigFont = new Font("SansSerif", Font.BOLD, 12);
	static final Font smallerFont = new Font("SansSerif", Font.PLAIN, 11);
	static final Font smallFont = new Font("SansSerif", Font.PLAIN, 10);

	static final Color defaultColor = Color.BLACK;
	static final Color lockedColor = Color.BLUE;

	private static final Border border = BorderFactory.createEmptyBorder(1, 1, 1, 1);

	ClusterTreeRenderer() {
		setOpenIcon(null);
		setClosedIcon(null);
		setLeafIcon(null);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object node, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {

		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, node, sel, expanded, leaf, row, hasFocus);
		label.setBorder(border);

		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		Object embeddedObject = treeNode.getUserObject();
		if (embeddedObject instanceof MsFeature) {

			label.setIcon(featureIcon);
			label.setFont(smallerFont);
		}
		if (embeddedObject instanceof MsFeatureCluster) {

			MsFeatureCluster cluster = (MsFeatureCluster) embeddedObject;

			if (cluster.getNumberOfNamed() > 1)
				label.setIcon(multiNamedClusterIcon);
			else if (cluster.getNumberOfNamed() == 1)
				label.setIcon(namedClusterIcon);
			else
				label.setIcon(clusterIcon);

			if (cluster.isLocked())
				label.setForeground(lockedColor);
			
			if(cluster.hasChargeMismatch())
				label.setIcon(problemClusterIcon);

			label.setFont(bigFont);
		}
		return label;
	}
}
