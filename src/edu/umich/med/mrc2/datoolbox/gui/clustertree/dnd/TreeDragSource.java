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

package edu.umich.med.mrc2.datoolbox.gui.clustertree.dnd;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;

public class TreeDragSource implements DragSourceListener, DragGestureListener {

	DragSource source;
	DragGestureRecognizer recognizer;
	TransferableTreeNode transferable;

	TransferableFeatureCluster transferableCluster;
	TransferableFeature transferableFeature;

	DefaultMutableTreeNode oldNode;

	JTree sourceTree;

	public TreeDragSource(JTree tree, int actions) {

		sourceTree = tree;
		source = new DragSource();
		recognizer = source.createDefaultDragGestureRecognizer(sourceTree, actions, this);
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
		/*
		 * to support move or copy, we have to check which occurred:
		 */
		// System.out.println("Drop Action: " + dsde.getDropAction());
		//
		// if (dsde.getDropSuccess() && (dsde.getDropAction() ==
		// DnDConstants.ACTION_MOVE)) {
		//
		// ((DefaultTreeModel)
		// sourceTree.getModel()).removeNodeFromParent(oldNode);
		// }

		/*
		 * to support move only...
		 */
		// if (dsde.getDropSuccess()) {
		// ((DefaultTreeModel)sourceTree.getModel()).removeNodeFromParent(oldNode);
		// }

	}

	/*
	 * Drag Event Handlers
	 */
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	/*
	 * Drag Gesture Handler
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {

		TreePath path = sourceTree.getSelectionPath();

		if ((path == null) || (path.getPathCount() <= 1)) {
			// We can't move the root node or an empty selection
			return;
		}
		oldNode = (DefaultMutableTreeNode) path.getLastPathComponent();

		if (oldNode.getUserObject() instanceof MsFeatureCluster) {

			// transferable = new TransferableTreeNode(path);
			transferableCluster = new TransferableFeatureCluster((MsFeatureCluster) oldNode.getUserObject());
			source.startDrag(dge, DragSource.DefaultMoveDrop, transferableCluster, this);
			return;
		} else if (oldNode.getUserObject() instanceof MsFeature) {

			transferableFeature = new TransferableFeature((MsFeature) oldNode.getUserObject());
			source.startDrag(dge, DragSource.DefaultMoveDrop, transferableFeature, this);
			return;
		} else {
			return;
		}
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {

		// System.out.println("Action: " + dsde.getDropAction());
		// System.out.println("Target Action: " + dsde.getTargetActions());
		// System.out.println("User Action: " + dsde.getUserAction());
	}
}
