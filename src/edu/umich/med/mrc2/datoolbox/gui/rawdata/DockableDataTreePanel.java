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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.tree.RawDataTree;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.tree.RawDataTreeModel;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.tree.TreeGrouping;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import umich.ms.datatypes.scan.IScan;

public class DockableDataTreePanel extends DefaultSingleCDockable implements ActionListener {
	
	private static final Icon treeIcon = GuiUtils.getIcon("doe-tree", 16);
	
	private static final Icon expandTreeIcon = GuiUtils.getIcon("expand", 16);
	private static final Icon collapseTreeIcon = GuiUtils.getIcon("collapse", 16);
	private static final Icon byFileTreeIcon = GuiUtils.getIcon("groupByFile", 16);
	private static final Icon byTypeTreeIcon = GuiUtils.getIcon("groupByType", 16);
	
	private SimpleButtonAction
		expandCollapseTreeButton, 
		toggleTreeGroupingButton;
	
	private JScrollPane panel;
	private RawDataTree rawDataTree;
	private RawDataTreeModel treeModel;	
	private boolean isTreeExpanded;
	private TreeGrouping grouping;

	public DockableDataTreePanel(RawDataExaminerPanel parentPanel) {
		
		super("DockableDataTreePanel", treeIcon, "Data tree", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		
		rawDataTree = new RawDataTree(parentPanel); 
		treeModel = (RawDataTreeModel) rawDataTree.getModel();
		panel = new JScrollPane(rawDataTree);
		isTreeExpanded = false;
		add( panel, BorderLayout.CENTER );
		grouping = TreeGrouping.BY_DATA_FILE;
		
		initButtons(this);
	}
	
	private void initButtons(ActionListener l) {

		DefaultDockActionSource actions = new DefaultDockActionSource(
				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));

		expandCollapseTreeButton = GuiUtils.setupButtonAction(
				MainActionCommands.EXPAND_TREE.getName(),
				MainActionCommands.EXPAND_TREE.getName(), 
				expandTreeIcon, l);
		actions.add(expandCollapseTreeButton);
		
		toggleTreeGroupingButton = GuiUtils.setupButtonAction(
				MainActionCommands.GROUP_TREE_BY_TYPE.getName(), 
				MainActionCommands.GROUP_TREE_BY_TYPE.getName(), 
				byFileTreeIcon, l);
		actions.add(toggleTreeGroupingButton);
		
		actions.addSeparator();
		intern().setActionOffers(actions);
	}
		
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.GROUP_TREE_BY_TYPE.getName()))
			groupTree(TreeGrouping.BY_OBJECT_TYPE);
		
		if(command.equals(MainActionCommands.GROUP_TREE_BY_FILE.getName()))
			groupTree(TreeGrouping.BY_DATA_FILE);
		
		if(command.equals(MainActionCommands.COLLAPSE_TREE.getName()))
			toggleTreeExpanded(false);
		
		if(command.equals(MainActionCommands.EXPAND_TREE.getName()))
			toggleTreeExpanded(true);
	}
	
	public void addTreeSelectionListener(TreeSelectionListener tsl) {
		rawDataTree.addTreeSelectionListener(tsl);
	}

	public void clearPanel() {
		rawDataTree.clearTree();
	}

	public void toggleTreeExpanded(boolean expand) {
		
		isTreeExpanded = expand;		
		if(treeModel.getTreeGrouping().equals(TreeGrouping.BY_OBJECT_TYPE)){
			
			TreeNode node = (TreeNode) treeModel.getDataFilesNode();
			toggleNode(node, true);
			
			node = (TreeNode) treeModel.getChromatogramNode();
			toggleNode(node, expand);
			
			node = (TreeNode) treeModel.getSpectraNode();
			toggleNode(node, expand);		
		}
		if(treeModel.getTreeGrouping().equals(TreeGrouping.BY_DATA_FILE)){

			TreeNode node = (TreeNode) treeModel.getDataFilesNode();
			toggleNode(node, true);
			
			int childCount = treeModel.getChildCount(treeModel.getDataFilesNode());		
			for (int i = 0; i < childCount; i++) {
				
				node = (TreeNode) treeModel.getChild(treeModel.getDataFilesNode(), i);
				for(int j=0; j<treeModel.getChildCount(node); j++) {
					
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)treeModel.getChild(node, j);
					if(child.getUserObject().equals(RawDataTreeModel.chromatogramNodeName) ||
							child.getUserObject().equals(RawDataTreeModel.spectraNodeName))
						toggleNode(child, expand);
					if(child.getUserObject().equals(RawDataTreeModel.scansNodeName))
						toggleNode(child, false);
				}
			}			
		}
		treeExpanded(isTreeExpanded);
	}
		
	public void groupTree(TreeGrouping grouping) {
		
		this.grouping = grouping;
		rawDataTree.loadData(getDataFiles(), grouping, true);
		
		if (grouping.equals(TreeGrouping.BY_DATA_FILE)) {

			toggleTreeGroupingButton.setIcon(byFileTreeIcon);
			toggleTreeGroupingButton.setCommand(MainActionCommands.GROUP_TREE_BY_TYPE.getName());
			toggleTreeGroupingButton.setTooltip(MainActionCommands.GROUP_TREE_BY_TYPE.getName());
		}
		if (grouping.equals(TreeGrouping.BY_OBJECT_TYPE)) {

			toggleTreeGroupingButton.setIcon(byTypeTreeIcon);
			toggleTreeGroupingButton.setCommand(MainActionCommands.GROUP_TREE_BY_FILE.getName());
			toggleTreeGroupingButton.setTooltip(MainActionCommands.GROUP_TREE_BY_FILE.getName());
		}
		//	Tree collapse tree to default state after resorting, so button is reset
		treeExpanded(false);
	}
	
	public void treeExpanded(boolean expanded) {

		if (expanded) {

			expandCollapseTreeButton.setIcon(collapseTreeIcon);
			expandCollapseTreeButton.setCommand(MainActionCommands.COLLAPSE_TREE.getName());
			expandCollapseTreeButton.setTooltip("Collapse all file nodes");
		} else {
			expandCollapseTreeButton.setIcon(expandTreeIcon);
			expandCollapseTreeButton.setCommand(MainActionCommands.EXPAND_TREE.getName());
			expandCollapseTreeButton.setTooltip("Expand all file nodes");
		}

	}
	
	public void loadData(Collection<DataFile>files, TreeGrouping grouping) {		
		this.grouping = grouping;
		rawDataTree.loadData(files, grouping, false);
	}
	
	public void loadData(Collection<DataFile>files) {		
		rawDataTree.loadData(files, grouping, false);
	}
	
	public void loadData(Collection<DataFile>files, boolean reset) {		
		rawDataTree.loadData(files, grouping, reset);
	}
	
	private void toggleNode(TreeNode node, boolean expand) {
		
		if(node == null)
			return;
		
		TreeNode pathToRoot[] = ((DefaultTreeModel) treeModel).getPathToRoot(node);
		TreePath path = new TreePath(pathToRoot);
		
		if(expand)
			rawDataTree.expandPath(path);
		else
			rawDataTree.collapsePath(path);
	}

	public void addChromatograms(Collection<ExtractedChromatogram>chromatograms) {
		
	    final Runnable r = new Runnable() {
	        public void run() {
	        	chromatograms.forEach(f -> treeModel.addObject(f));	
	        	return;
	        }
	    };
	    SwingUtilities.invokeLater(r);
	    return;
	}
	
	public void removeDataFiles(Collection<DataFile> selectedFiles) {
		treeModel.removeDataFiles(selectedFiles);		
	}
	
	public Collection<DataFile>getDataFiles(){
		
		return treeModel.getTreeObjects().stream().
				filter(DataFile.class::isInstance).
				map(DataFile.class::cast).
				collect(Collectors.toList());
	}
	
	public Collection<ExtractedChromatogram>getChromatograms(){
		
		return treeModel.getTreeObjects().stream().
				filter(ExtractedChromatogram.class::isInstance).
				map(ExtractedChromatogram.class::cast).
				collect(Collectors.toList());
	}
	
	public Collection<AverageMassSpectrum>getAverageMassSpectra(){
		
		return treeModel.getTreeObjects().stream().
				filter(AverageMassSpectrum.class::isInstance).
				map(AverageMassSpectrum.class::cast).
				collect(Collectors.toList());
	}
	
	public void addObject(Object object) {		
		rawDataTree.addObjects(Collections.singleton(object));			
	}
	
	public void removeObject(Object object) {		
		rawDataTree.removeObjects(Collections.singleton(object));	
	}
	
	public boolean isTreeExpanded() {
		return isTreeExpanded;
	}
	
	public void selectNodeForObject(Object o) {		
		rawDataTree.selectNodeForObject(o);
	}
	
	public DataFile getDataFileForScan(IScan s) {
		return rawDataTree.getDataFileForScan(s);
	}
	
	public DataFile getDataFileForMsFeature(MsFeature f) {
		return rawDataTree.getDataFileForMsFeature(f);
	}
}



































