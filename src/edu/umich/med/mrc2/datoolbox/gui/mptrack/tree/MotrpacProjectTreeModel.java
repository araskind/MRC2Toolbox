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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.tree;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;

public class MotrpacProjectTreeModel extends DefaultTreeModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5902763319823797973L;
	public static final String projectNodeName = "Projects";
	public final DefaultMutableTreeNode projectsNode = 
			new DefaultMutableTreeNode(projectNodeName);
	private Hashtable<Object, DefaultMutableTreeNode> treeObjects = 
			new Hashtable<Object, DefaultMutableTreeNode>();
	private DefaultMutableTreeNode rootNode;

	public MotrpacProjectTreeModel() {

		super(new DefaultMutableTreeNode("MoTrPAC studies"));
		rootNode = (DefaultMutableTreeNode) super.getRoot();
		insertNodeInto(projectsNode, rootNode, 0);
	}

	public DefaultMutableTreeNode getProjectsNode() {
		return projectsNode;
	}

	public void clearModel() {

		projectsNode.removeAllChildren();
		reload();
		treeObjects.clear();
	}

	public void addProject(MoTrPACStudy project) {

		if (!SwingUtilities.isEventDispatchThread())
			throw new IllegalStateException("This method must be called from Swing thread");

		final DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
		treeObjects.put(project, projectNode);
		insertNodeInto(projectNode, projectsNode, getChildCount(projectsNode));

		for(LIMSExperiment experiment : project.getExperiments()) {

			final DefaultMutableTreeNode experimentNode = new DefaultMutableTreeNode(experiment);
			insertNodeInto(experimentNode, projectNode, getChildCount(projectNode));

			for(LIMSSamplePreparation prep : experiment.getSamplePreps()) {

				final DefaultMutableTreeNode prepNode = new DefaultMutableTreeNode(prep);
				insertNodeInto(prepNode, experimentNode, getChildCount(experimentNode));

				for(DataAcquisitionMethod acqMethod : prep.getAssays()) {

					final DefaultMutableTreeNode assayNode = new DefaultMutableTreeNode(acqMethod);
					insertNodeInto(assayNode, prepNode, getChildCount(prepNode));

					//	TODO rewrite when new framework to retrieve the data is in place
//					for(DataExtractionMethod daMethod : acqMethod.getDaMethods()) {
//
//						final DefaultMutableTreeNode daNode = new DefaultMutableTreeNode(daMethod);
//						insertNodeInto(daNode, assayNode, getChildCount(assayNode));
//					}
				}
			}
		}
	}
	
	private DefaultMutableTreeNode findStudyNodeForExperiment(LIMSExperiment experiment) {
		
		MoTrPACStudy parent =  treeObjects.keySet().stream().
				filter(MoTrPACStudy.class::isInstance).
				map(MoTrPACStudy.class::cast).
				filter(o -> o.getExperiments().contains(experiment)).
				findFirst().orElse(null);
		
		if(parent == null)
			return null;
		else
			return treeObjects.get(parent);
	}

	/**
	 * This method must be called from Swing thread
	 */
	public void addObject(final Object object) {

		if (!SwingUtilities.isEventDispatchThread())
			throw new IllegalStateException("This method must be called from Swing thread");

		final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(object);
		if (object instanceof MoTrPACStudy) {

			treeObjects.put(object, newNode);
			int childCount = getChildCount(projectsNode);
			insertNodeInto(newNode, projectsNode, childCount);
		}
		if (object instanceof LIMSExperiment) {
			
			DefaultMutableTreeNode projectNode = findStudyNodeForExperiment((LIMSExperiment)object);
			if(projectNode != null) {
				treeObjects.put(object, newNode);
				int childCount = getChildCount(projectNode);
				insertNodeInto(newNode, projectNode, childCount);
			}
		}
		if (object instanceof LIMSSamplePreparation) {

			LIMSSamplePreparation prep = (LIMSSamplePreparation)object;
			for (Entry<LIMSExperiment, Collection<LIMSSamplePreparation>> entry :
					IDTDataCache.getExperimentSamplePrepMap().entrySet()) {

				DefaultMutableTreeNode experimentNode = treeObjects.get(entry.getKey());
				if(experimentNode != null && entry.getValue().contains(prep))
					insertNodeInto(newNode, experimentNode, getChildCount(experimentNode));
			}
		}
		if (object instanceof DataAcquisitionMethod) {

			DataAcquisitionMethod acqMethod = (DataAcquisitionMethod)object;
			//	TODO
//			for (Entry<LIMSSamplePreparation, Collection<DataAcquisitionMethod>> entry :
//					IDTDataCache.getSamplePrepAcquisitionMethodMap().entrySet()) {
//
//				DefaultMutableTreeNode prepNode = findNodeForObject(entry.getKey());
//				if(prepNode != null && entry.getValue().contains(acqMethod))
//					insertNodeInto(newNode, prepNode, getChildCount(prepNode));
//			}
		}
		if (object instanceof DataExtractionMethod) {

			DataExtractionMethod dexMethod = (DataExtractionMethod)object;
			//	TODO
//			for (Entry<DataAcquisitionMethod, Collection<DataExtractionMethod>> entry :
//					IDTDataCache.getAcquisitionDataExtractionMethodMap().entrySet()) {
//
//				DefaultMutableTreeNode acqNode = findNodeForObject(entry.getKey());
//				if(acqNode != null && entry.getValue().contains(dexMethod))
//					insertNodeInto(newNode, acqNode, getChildCount(acqNode));
//			}
		}
	}

	public DefaultMutableTreeNode findNodeForObject(Object nodeObject) {

		Enumeration e = rootNode.breadthFirstEnumeration();
		while (e.hasMoreElements()) {

			DefaultMutableTreeNode element = (DefaultMutableTreeNode) e.nextElement();
			if (element.getUserObject().equals(nodeObject))
				return element;
		}
		return null;
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

	public DefaultMutableTreeNode getNodeForObject(Object o) {
		return treeObjects.get(o);
	}


}














