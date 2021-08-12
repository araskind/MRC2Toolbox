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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.expdesign.stucturedit;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.gui.utils.checkboxtree.CheckBoxNodeData;

public class ExpDesignTreeModel extends DefaultTreeModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4939219422284100533L;
	public static final String factorNodeName = "Factors";
	private static DefaultMutableTreeNode createCheckedNode(final DefaultMutableTreeNode parent,
			final Object userObject, final String text, final boolean checked) {

		final CheckBoxNodeData data = new CheckBoxNodeData(userObject, text, checked);
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
		parent.add(node);
		return node;
	}
	public final DefaultMutableTreeNode factorsNode = new DefaultMutableTreeNode(factorNodeName);
	private Hashtable<Object, DefaultMutableTreeNode> treeObjects = new Hashtable<Object, DefaultMutableTreeNode>();

	private DefaultMutableTreeNode rootNode;

	public ExpDesignTreeModel() {

		super(new DefaultMutableTreeNode("Experiment design"));
		rootNode = (DefaultMutableTreeNode) super.getRoot();
		insertNodeInto(factorsNode, rootNode, 0);
	}

	/**
	 * This method must be called from Swing thread
	 */
	public void addObject(final Object object) {

		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("This method must be called from Swing thread");
		}
		String nodeText = "";
		boolean checked = false;

		if (object instanceof ExperimentDesignFactor) {

			nodeText = ((ExperimentDesignFactor) object).getName();
			checked = ((ExperimentDesignFactor) object).isEnabled();
		}
		if (object instanceof ExperimentDesignLevel) {

			nodeText = ((ExperimentDesignLevel) object).getName();
			checked = ((ExperimentDesignLevel) object).isEnabled();
		}
		final DefaultMutableTreeNode newNode = createCheckedNode(factorsNode, object, nodeText, checked);
		treeObjects.put(object, newNode);

		if (object instanceof ExperimentDesignFactor) {

			for (ExperimentDesignLevel dl : ((ExperimentDesignFactor) object).getLevels()) {

				final DefaultMutableTreeNode newLevelNode = createCheckedNode(newNode, dl, dl.getName(),
						dl.isEnabled());
				treeObjects.put(dl, newLevelNode);
			}
		}
	}

	public synchronized void clearDesign() {

		ExperimentDesignFactor[] factors = getFactors();

		if (factors.length > 0) {

			for (ExperimentDesignFactor f : factors)
				removeObject(f);
		}
	}

	@Override
	public void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {

		super.fireTreeNodesChanged(source, path, childIndices, children);
	}

	public synchronized ExperimentDesignFactor[] getFactors() {

		int childrenCount = getChildCount(factorsNode);
		ExperimentDesignFactor result[] = new ExperimentDesignFactor[childrenCount];

		for (int j = 0; j < childrenCount; j++) {

			DefaultMutableTreeNode child = (DefaultMutableTreeNode) getChild(factorsNode, j);
			result[j] = (ExperimentDesignFactor) child.getUserObject();
		}
		return result;
	}

	public DefaultMutableTreeNode getFactorsNode() {
		return factorsNode;
	}

	public DefaultMutableTreeNode getNodeByUserObject(Object userObject) {

		DefaultMutableTreeNode theNode = null;

		for (Enumeration e = rootNode.depthFirstEnumeration(); e.hasMoreElements() && theNode == null;) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();

			if (node.getUserObject() instanceof CheckBoxNodeData) {

				Object uo = ((CheckBoxNodeData) node.getUserObject()).getUserObject();

				if (userObject.equals(uo))
					theNode = node;
			}
		}
		return theNode;
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
			@SuppressWarnings("rawtypes")
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
