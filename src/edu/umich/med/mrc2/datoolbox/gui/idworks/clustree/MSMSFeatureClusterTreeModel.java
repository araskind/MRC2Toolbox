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

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;

public class MSMSFeatureClusterTreeModel extends DefaultTreeModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 797594299057681776L;

	public static final String clusterNodeName = "Clusters";
	public final DefaultMutableTreeNode clustersNode = new DefaultMutableTreeNode(clusterNodeName);
	private Hashtable<Object, DefaultMutableTreeNode> treeObjects = new Hashtable<Object, DefaultMutableTreeNode>();
	private DefaultMutableTreeNode rootNode;

	public MSMSFeatureClusterTreeModel() {

		super(new DefaultMutableTreeNode("Analysis results"));
		rootNode = (DefaultMutableTreeNode) super.getRoot();
		insertNodeInto(clustersNode, rootNode, 0);
	}

	/**
	 * This method must be called from Swing thread
	 */
	public void addObject(final Object object) {

		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("This method must be called from Swing thread");
		}
		final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(object);
		treeObjects.put(object, newNode);

		if (object instanceof MsFeatureCluster) {

			int childCount = getChildCount(clustersNode);
			insertNodeInto(newNode, clustersNode, childCount);

			for (MsFeature cf : ((MsFeatureCluster) object).getFeatures()) {

				final DefaultMutableTreeNode newFeatureNode = new DefaultMutableTreeNode(cf);
				treeObjects.put(cf, newFeatureNode);
				insertNodeInto(newFeatureNode, newNode, getChildCount(newNode));
			}
		}
		if (object instanceof MsFeature) {

		}
	}

	public MsFeatureCluster getParentCluster(MsFeature feature) {

		DefaultMutableTreeNode featureNode = treeObjects.get(feature);
		if(featureNode!= null) {

			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) featureNode.getParent();
			return (MsFeatureCluster) parentNode.getUserObject();
		}
		return null;
	}

	public synchronized void clearClusters() {

		MsFeatureCluster[] clusters = getClusters();
		if (clusters.length > 0) {

			for (MsFeatureCluster cl : clusters)
				removeObject(cl);
		}
	}

	public synchronized MsFeatureCluster[] getClusters() {

		int childrenCount = getChildCount(clustersNode);
		MsFeatureCluster result[] = new MsFeatureCluster[childrenCount];
		for (int j = 0; j < childrenCount; j++) {

			DefaultMutableTreeNode child = (DefaultMutableTreeNode) getChild(clustersNode, j);
			result[j] = (MsFeatureCluster) child.getUserObject();
		}
		return result;
	}

	public DefaultMutableTreeNode getClustersNode() {
		return clustersNode;
	}

	public void clearModel() {

		clustersNode.removeAllChildren();
		treeObjects = new Hashtable<Object, DefaultMutableTreeNode>();
		reload();		
	}

	/**
	 * This method must be called from Swing thread
	 */
	public void removeObject(final Object object) {

		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("This method must be called from Swing thread");
		}

		final DefaultMutableTreeNode node = treeObjects.get(object);

		if (node != null) {

			// Remove all children from treeObjects
			Enumeration e = node.depthFirstEnumeration();
			while (e.hasMoreElements()) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) e.nextElement();
				Object nodeObject = childNode.getUserObject();
				treeObjects.remove(nodeObject);
			}
			// Remove the node from the tree, that also remove child nodes
			removeNodeFromParent(node);

			// Remove the node object from treeObjects
			treeObjects.remove(object);
		}
	}
}
