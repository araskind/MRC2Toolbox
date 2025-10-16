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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import umich.ms.datatypes.scan.IScan;

public class RawDataTree extends JTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1227419030212784290L;
	
	private RawDataTreeModel treeModel;

	public RawDataTree(RawDataExaminerPanel parentPanel) {
		super();
		
		treeModel = new RawDataTreeModel();
		setModel(treeModel);	
		setCellRenderer(new RawDataTreeRenderer());
		setEditable(false);
		setRootVisible(true);
		setShowsRootHandles(false);		
		addMouseListener(new RawDataTreeMouseHandler(this, parentPanel));
		setRowHeight(25);
	}

	public void loadData(Collection<DataFile> dataFilesForMethod, boolean replace) {	
		loadData(dataFilesForMethod, TreeGrouping.BY_DATA_FILE, replace);
	}
	
	public void loadData(Collection<DataFile> dataFilesForMethod, TreeGrouping grouping, boolean replace) {
			
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				loadDataFunction(dataFilesForMethod, grouping, replace);
			}
		});	
	}
	
	private void loadDataFunction(Collection<DataFile> dataFilesForMethod, TreeGrouping grouping, boolean replace) {
		
		if(replace)
			treeModel.clear();
		
		treeModel.setTreeGrouping(grouping);
		treeModel.insertNodeInto(treeModel.getDataFilesNode(), treeModel.getRootNode(), 0);
		
		if(grouping.equals(TreeGrouping.BY_OBJECT_TYPE)) {			
			treeModel.insertNodeInto(treeModel.getChromatogramNode(), treeModel.getRootNode(), 1);
			treeModel.insertNodeInto(treeModel.getSpectraNode(), treeModel.getRootNode(), 2);
		}
		if(dataFilesForMethod != null)
			dataFilesForMethod.stream().sorted().forEach(b -> treeModel.addObject(b));
		
		if(treeModel.getTreeGrouping().equals(TreeGrouping.BY_OBJECT_TYPE)){
			
			TreeNode node = (TreeNode) treeModel.getDataFilesNode();
			toggleNode(node, true);
			
			node = (TreeNode) treeModel.getChromatogramNode();
			toggleNode(node, true);
			
			node = (TreeNode) treeModel.getSpectraNode();
			toggleNode(node, true);		
		}
		if(treeModel.getTreeGrouping().equals(TreeGrouping.BY_DATA_FILE)){

			TreeNode node = (TreeNode) treeModel.getDataFilesNode();
			toggleNode(node, true);
			
			int childCount = treeModel.getChildCount(treeModel.getDataFilesNode());		
			for (int i = 0; i < childCount; i++) {			
				node = (TreeNode) treeModel.getChild(treeModel.getDataFilesNode(), i);
				toggleNode(node, true);
			}			
		}
	}
	
	private void toggleNode(TreeNode node, boolean expand) {
		
		if(node == null)
			return;
		
		TreeNode pathToRoot[] = ((DefaultTreeModel) treeModel).getPathToRoot(node);
		TreePath path = new TreePath(pathToRoot);
		
		if(expand)
			expandPath(path);
		else
			collapsePath(path);
	}
	
	public void clearTree() {		
		treeModel.clear();
	}
	
	public void removeObjects(Collection<? extends Object>toRemove) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				
				for(Object o : toRemove)
					((RawDataTreeModel) treeModel).removeObject(o);
			}
		});	
	}
	
	public void addObjects(Collection<? extends Object>toAdd) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				
				for(Object o : toAdd)
					((RawDataTreeModel) treeModel).addObject(o);
			}
		});	
	}
	
	public Collection<Object>getSelectedObjects(){
		
		Collection<Object>selectedObjects = new ArrayList<Object>();
		int selectedRows[] = getSelectionRows();
		if(selectedRows == null || selectedRows.length == 0)
			return selectedObjects;
		
		if(selectedRows.length > 1)
			Arrays.sort(selectedRows);
		
		for (int row : selectedRows)
			selectedObjects.add(((DefaultMutableTreeNode) getPathForRow(row).getLastPathComponent()).getUserObject());		
		
		return selectedObjects;
	}
	
	public TreePath findPath(Object o) {
		
		DefaultMutableTreeNode node = treeModel.getNodeForObject(o);
		if(node != null)
			return new TreePath(node.getPath());

		return null;
	}
	
	public DefaultMutableTreeNode getParentNodeForObject(Object o) {
		
        TreePath tp = findPath(o);
        if(tp == null)
        	return null;
        
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)tp.getLastPathComponent();		
		return (DefaultMutableTreeNode) childNode.getParent();
	}
	
	public DataFile getDataFileForScan(IScan s) {
		
		DefaultMutableTreeNode scanNode = treeModel.getNodeForObject(s);
		if(scanNode == null)
			return null;
		
		return (DataFile)((DefaultMutableTreeNode)scanNode.getParent().getParent()).getUserObject();
	}
	
	public DataFile getDataFileForMsFeature(MsFeature f) {
		
		DefaultMutableTreeNode scanNode = treeModel.getNodeForObject(f);
		if(scanNode == null)
			return null;
		
		return (DataFile)((DefaultMutableTreeNode)scanNode.getParent().getParent()).getUserObject();
	}
	
	public void selectNodeForObject(Object o) {
		
		TreePath treePath = findPath(o);
		setSelectionPath(treePath);
		scrollPathToVisible(treePath);
	}
}






