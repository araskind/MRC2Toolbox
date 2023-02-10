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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign.stucturedit;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.gui.utils.checkboxtree.CheckBoxNodeData;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentDesignUtils;

public class ExpDesignTree extends JTree implements CellEditorListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4254421705570197976L;
	private final ExpDesignTreeRenderer renderer;
	private final ExpDesignTreeNodeEditor editor;
	//private ExpDesignTreeModelListener modelListener;

	public ExpDesignTree() {

		super(new ExpDesignTreeModel());
		this.setToggleClickCount(0);

		renderer = new ExpDesignTreeRenderer();
		setCellRenderer(renderer);

		editor = new ExpDesignTreeNodeEditor(this);
		setCellEditor(editor);
		editor.addCellEditorListener(this);
		setEditable(true);

		disableSelection();
		setRootVisible(true);
		setShowsRootHandles(false);

//		modelListener = new ExpDesignTreeModelListener();
//		this.getModel().addTreeModelListener(modelListener);
	}

	private void disableSelection() {

		DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3440237742268761110L;

			public void addSelectionPath(TreePath path) {
			}

			public void removeSelectionPath(TreePath path) {
			}

			public void setSelectionPath(TreePath path) {
			}

			public void setSelectionPaths(TreePath[] pPaths) {
			}
		};
		this.setSelectionModel(dtsm);
	}

	public void expandAllNodes() {

		for (int i = 0; i < this.getRowCount(); i++)
			this.expandRow(i);
	}

	public void expandFactorsNode() {

		int row = 0;
		TreePath treePath;

		while (row < this.getRowCount()) {

			treePath = this.getPathForRow(row);

			if (treePath.getLastPathComponent().toString() == ExpDesignTreeModel.factorNodeName) {

				this.expandRow(row);
				break;
			}
			row++;
		}
	}

	public void resetTree() {

		setModel(new ExpDesignTreeModel());
	}

	@Override
	public void editingStopped(ChangeEvent e) {

		ExpDesignTreeNodeEditor edtEditor = (ExpDesignTreeNodeEditor)e.getSource();
		CheckBoxNodeData value = (CheckBoxNodeData)edtEditor.getCellEditorValue();
		Object editedObject = value.getUserObject();

		if(editedObject instanceof ExperimentDesignLevel) {
			
			DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();		
			
			//	Disable or enable other levels to show only combinations 
			//	possible for existing samples			
			ExperimentDesignUtils.AdjustEnabledLevels(project.getExperimentDesign());
			ExpDesignTreeModel model = (ExpDesignTreeModel) getModel();
					
			//	Redraw the tree
			model.reload();
			expandAllNodes();
		}	
		//System.out.println(e.toString() + ": structure changed");
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}
}






























