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

package edu.umich.med.mrc2.datoolbox.gui.clustertree.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.ClusterTree;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.ClusterTreeModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class TreeDropTarget implements DropTargetListener {

	private DropTarget target;
	private ClusterTree targetTree;

	public TreeDropTarget(ClusterTree tree) {

		targetTree = tree;
		target = new DropTarget(targetTree, this);
	}

	public void dragEnter(DropTargetDragEvent dtde) {

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) getNodeForEvent(dtde);

		if (node.getUserObject() instanceof MsFeatureCluster)
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		else
			dtde.rejectDrag();
	}

	public void dragExit(DropTargetEvent dte) {

	}

	public void dragOver(DropTargetDragEvent dtde) {

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) getNodeForEvent(dtde);

		if (node.getUserObject() instanceof MsFeatureCluster)
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		else
			dtde.rejectDrag();
	}

	public void drop(DropTargetDropEvent dtde) {

		Point pt = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		JTree tree = (JTree) dtc.getComponent();
		TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentpath.getLastPathComponent();
		DataAnalysisProject project = MRC2ToolBoxCore.getCurrentProject();
		Set<MsFeatureCluster> assayClusters = 
				project.getMsFeatureClustersForDataPipeline(project.getActiveDataPipeline());

		MsFeatureCluster parentCluster = null;
		MsFeatureCluster toMergeCluster = null;
		MsFeature featureToMove;
		ClusterTreeModel model = targetTree.getModel();

		if (parent.getUserObject() instanceof MsFeatureCluster) {

			parentCluster = (MsFeatureCluster) parent.getUserObject();

			try {
				Transferable tr = dtde.getTransferable();
				DataFlavor[] flavors = tr.getTransferDataFlavors();

				for (int i = 0; i < flavors.length; i++) {

					if (tr.isDataFlavorSupported(flavors[i])) {

						dtde.acceptDrop(dtde.getDropAction());

						// Move cluster to merge with another cluster
						if (tr.getTransferData(flavors[i]) instanceof MsFeatureCluster) {

							String toMergeClusterId = ((MsFeatureCluster) tr.getTransferData(flavors[i]))
									.getClusterId();

							for (MsFeatureCluster fc : assayClusters) {

								if (fc.getClusterId().equals(toMergeClusterId)) {

									toMergeCluster = fc;
									break;
								}
							}
							if (parentCluster != null && toMergeCluster != null) {

								dtde.dropComplete(true);

								for (MsFeature f : toMergeCluster.getFeatures()) {

									MsFeature realFeature = 
											project.getMsFeatureById(f.getId(), project.getActiveDataPipeline());
									parentCluster.addFeature(realFeature, project.getActiveDataPipeline());
								}
								parentCluster.setClusterCorrMatrix(parentCluster.createClusterCorrelationMatrix(false));
								assayClusters.remove(toMergeCluster);
								targetTree.removeFeatureCluster(toMergeCluster);
								targetTree.resortTree();
								targetTree.setSelectionPath(parentpath);
								targetTree.scrollPathToVisible(parentpath);
								targetTree.expandCluster(parentCluster);

								return;
							}
						}
						// Move feature to another cluster
						if (tr.getTransferData(flavors[i]) instanceof MsFeature) {

							String id = ((MsFeature) tr.getTransferData(flavors[i])).getId();
							featureToMove = project.getMsFeatureById(id, project.getActiveDataPipeline());
							MsFeatureCluster featureParent = model.getParentCluster(featureToMove);

							if(featureParent != null) {

								featureParent.removeFeature(featureToMove);
								if (featureParent.getFeatures().size() == 0) {
									assayClusters.remove(featureParent);
									targetTree.removeFeatureCluster(featureParent);
								}
							}
							parentCluster.addFeature(featureToMove, project.getActiveDataPipeline());
							parentCluster.setClusterCorrMatrix(parentCluster.createClusterCorrelationMatrix(false));
							targetTree.removeFeature(featureToMove);
							targetTree.resortTree();
							targetTree.expandCluster(parentCluster);
							targetTree.setSelectionPath(parentpath);
							targetTree.scrollPathToVisible(parentpath);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				dtde.rejectDrop();
			}
		} else {
			dtde.rejectDrop();
			return;
		}
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {

	}

	/*
	 * Drop Event Handlers
	 */
	private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {

		Point p = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		JTree tree = (JTree) dtc.getComponent();
		TreePath path = tree.getClosestPathForLocation(p.x, p.y);
		return (TreeNode) path.getLastPathComponent();
	}

}
