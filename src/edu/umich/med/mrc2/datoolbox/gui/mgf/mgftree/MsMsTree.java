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

package edu.umich.med.mrc2.datoolbox.gui.mgf.mgftree;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.data.compare.MsMsClusterComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.clustertree.ClusterTreeModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class MsMsTree extends JTree implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 8055804495421171312L;
	private MsMsTreeModel treeModel;
	private MsMsTreeRenderer renderer;
	private MsMsTreeMouseHandler mouseHandler;
	private Clipboard clipboard;
	private StringSelection msmsStringSelection;

	public MsMsTree() {

		treeModel = new MsMsTreeModel();
		setModel(treeModel);

		renderer = new MsMsTreeRenderer();
		setCellRenderer(renderer);
		setRowHeight(0);

		mouseHandler = new MsMsTreeMouseHandler(this);
		addMouseListener(mouseHandler);

		setEditable(false);

		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		setRootVisible(true);
		setShowsRootHandles(false);

		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MsMsTreeMouseHandler.SORT_BY_MZ_COMMAND))
			sortTreeByMz();

		if (command.equals(MsMsTreeMouseHandler.SORT_BY_RT_COMMAND))
			sortTreeByRt();

		if (command.equals(MsMsTreeMouseHandler.LOOKUP_AVERAGE_MSMS_COMMAND))
			lookupAverageMsMs();

		if (command.equals(MsMsTreeMouseHandler.LOOKUP_FEATURE_MSMS_COMMAND))
			lookupFeatureMsMs();

		if (command.equals(MsMsTreeMouseHandler.COPY_AVERAGE_MSMS_COMMAND))
			copyAverageMsMs();

		if (command.equals(MsMsTreeMouseHandler.COPY_FEATURE_MSMS_COMMAND))
			copyFeatureMsMs();
	}

	private void copyAverageMsMs() {

		if (getClickedObject() instanceof MsMsCluster) {

			MsMsCluster cluster = (MsMsCluster) getClickedObject();
			msmsStringSelection = new StringSelection(cluster.getClusterMsString());
			clipboard.setContents(msmsStringSelection, msmsStringSelection);
		}
	}

	private void copyFeatureMsMs() {

		if (getClickedObject() instanceof SimpleMsMs) {

			SimpleMsMs feature = (SimpleMsMs) getClickedObject();
			msmsStringSelection = new StringSelection(feature.getMsMsString());
			clipboard.setContents(msmsStringSelection, msmsStringSelection);
		}
	}

	public void expandClusterBranch() {

		int row = 0;
		TreePath treePath;

		while (row < this.getRowCount()) {

			treePath = this.getPathForRow(row);

			if (treePath.getLastPathComponent().toString() == ClusterTreeModel.clusterNodeName) {

				this.expandRow(row);
				break;
			}
			row++;
		}
	}

	public Object getClickedObject() {

		int selectedRows[] = getSelectionRows();

		if ((selectedRows == null) || (selectedRows.length == 0))
			return (Object) Array.newInstance(Object.class, 0);

		Arrays.sort(selectedRows);

		TreePath path = getPathForRow(selectedRows[0]);
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

		Object selectedObject = selectedNode.getUserObject();

		return selectedObject;
	}

	@Override
	public MsMsTreeModel getModel() {
		return treeModel;
	}

	private void lookupAverageMsMs() {

		MessageDialog.showInfoMsg("LOOKUP_AVERAGE_MSMS_COMMAND");
	}

	private void lookupFeatureMsMs() {

		MessageDialog.showInfoMsg("LOOKUP_FEATURE_MSMS_COMMAND");
	}

	public void resetTree() {

		treeModel = new MsMsTreeModel();
		setModel(treeModel);
	}

	private void sortTreeByMz() {

		List<MsMsCluster>sortedClusters = Arrays.asList(treeModel.getClusters()).stream().
				sorted(new MsMsClusterComparator(SortProperty.MZ)).collect(Collectors.toList());
		treeModel.clearClusters();
		sortedClusters.stream().forEach(fc -> treeModel.addObject(fc));
		expandClusterBranch();
	}

	private void sortTreeByRt() {

		List<MsMsCluster>sortedClusters = Arrays.asList(treeModel.getClusters()).stream().
				sorted(new MsMsClusterComparator(SortProperty.RT)).collect(Collectors.toList());
		treeModel.clearClusters();
		sortedClusters.stream().forEach(fc -> treeModel.addObject(fc));
		expandClusterBranch();
	}
}
