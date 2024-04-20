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

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MSMSFeatureClusterTreeRenderer extends DefaultTreeCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = -3568168754034663097L;

	private static final Icon featureIcon = GuiUtils.getIcon("feature", 24);
	private static final Icon primIdFeatureIcon = GuiUtils.getIcon("primIdFeature", 24);
	private static final Icon clusterIcon = GuiUtils.getIcon("cluster", 24);
	private static final Icon msmsClusterIcon = GuiUtils.getIcon("msmsCluster", 24);
	private static final Icon namedClusterIcon = GuiUtils.getIcon("namedCluster", 24);
	private static final Icon multiNamedClusterIcon = GuiUtils.getIcon("multiNamedCluster", 24);
	
	private static final Icon binIdentifiedMultipleAnnotIcon = GuiUtils.getIcon("binIdentifiedMultipleAnnot", 24);
	private static final Icon binIdentifiedSingleAnnotIcon = GuiUtils.getIcon("binIdentifiedSingleAnnot", 24);
	private static final Icon binUnknownMultipleAnnotIcon = GuiUtils.getIcon("binUnknownMultipleAnnot", 24);
	private static final Icon binUnknownSingleAnnotIcon = GuiUtils.getIcon("binUnknownSingleAnnot", 24);
	
	private static final Font bigFont = new Font("SansSerif", Font.BOLD, 12);
	private static final Font smallerFont = new Font("SansSerif", Font.PLAIN, 11);
	private static final Font smallFont = new Font("SansSerif", Font.PLAIN, 10);
	
	private static final Color defaultColor = Color.BLACK;
	private static final Color lockedColor = Color.BLUE;
	private static final String selectedColorString = "white";
	private static final String lockedColorString = "blue";
	private static final String lookupColorString = "green";

	private static final Border border = BorderFactory.createEmptyBorder(1, 1, 1, 1);

	MSMSFeatureClusterTreeRenderer() {
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
		if (embeddedObject instanceof MSFeatureInfoBundle) {

			if(isPrimaryIdFeatue(treeNode))
				label.setIcon(primIdFeatureIcon);
			else
				label.setIcon(featureIcon);
			
			label.setFont(smallerFont);
			label.setText(((MSFeatureInfoBundle)embeddedObject).getMsFeature().getName());
		}
		if (embeddedObject instanceof IMsFeatureInfoBundleCluster) {
			
			IMsFeatureInfoBundleCluster c = (IMsFeatureInfoBundleCluster)embeddedObject;

			if(embeddedObject instanceof MsFeatureInfoBundleCluster) {
				
				MsFeatureInfoBundleCluster cluster = (MsFeatureInfoBundleCluster) embeddedObject;
				
				if (cluster.getComponents().size() > 1)
					label.setIcon(multiNamedClusterIcon);
				else if (cluster.getComponents().size() == 1)
					label.setIcon(namedClusterIcon);
				else
					label.setIcon(clusterIcon);
			}
			if(embeddedObject instanceof BinnerBasedMsFeatureInfoBundleCluster) {
				
				BinnerBasedMsFeatureInfoBundleCluster cluster = 
						(BinnerBasedMsFeatureInfoBundleCluster) embeddedObject;
				int annotCount = cluster.getDetectedAnnotationsCount();
				boolean isIdentified = cluster.isIdentified();
				if(annotCount == 1) {
					
					if(isIdentified)
						label.setIcon(binIdentifiedSingleAnnotIcon);
					else
						label.setIcon(binUnknownSingleAnnotIcon);
				}
				if(annotCount > 1) {
					
					if(isIdentified)
						label.setIcon(binIdentifiedMultipleAnnotIcon);
					else
						label.setIcon(binUnknownMultipleAnnotIcon);
				}
			}
			String lockedClusterColorString = lockedColorString;
			String luColorString = lookupColorString;
			if(sel) {
				lockedClusterColorString = selectedColorString;
				luColorString = selectedColorString;
			}
			String labelText = "<html>";
			if (c.isLocked()) {
				//	label.setForeground(lockedColor);
				labelText+= "<font style=\"font-weight:bold; font-size: 1.2em; color:" 
						+ lockedClusterColorString + "\">" + c.toString() + "</font>";
			}
			else {
				labelText+= "<font style=\"font-weight:bold; font-size: 1.2em\">" + c.toString() + "</font>";
			}
			//label.setFont(bigFont);
			if(c.getLookupFeature() != null)
				labelText+= "<br><font style=\"color:" 
						+ luColorString + "; font-size: 1.1em\">" + c.getLookupFeature().getName();
				
			label.setText(labelText);
		}
		return label;
	}
	
	private boolean isPrimaryIdFeatue(DefaultMutableTreeNode treeNode) {

		Object embeddedObject = treeNode.getUserObject();		
		if (embeddedObject instanceof MSFeatureInfoBundle) {
			
//			MsFeatureIdentity pid = 
//					((MSFeatureInfoBundle)embeddedObject).getMsFeature().getPrimaryIdentity();
			Object parentObject = 
					((DefaultMutableTreeNode)treeNode.getParent()).getUserObject();
			if (parentObject instanceof IMsFeatureInfoBundleCluster) {
				
				MsFeatureIdentity cPid = ((IMsFeatureInfoBundleCluster)parentObject).getPrimaryIdentity();
				if(cPid != null 
						&& ((MSFeatureInfoBundle)embeddedObject).getMsFeature().getIdentifications().contains(cPid))
					return true;
				else
					return false;
			}
			else
				return false;
		}
		else
			return false;
	}
}













