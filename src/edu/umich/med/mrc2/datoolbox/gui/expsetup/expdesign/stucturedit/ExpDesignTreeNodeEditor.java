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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign.stucturedit;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.gui.utils.checkboxtree.CheckBoxNodeData;

public class ExpDesignTreeNodeEditor extends AbstractCellEditor implements TreeCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4254031167302868576L;
	private final ExpDesignTreeRenderer renderer = new ExpDesignTreeRenderer();
	private final ExpDesignTree theTree;

	public ExpDesignTreeNodeEditor(final ExpDesignTree tree) {
		theTree = tree;
	}

	@Override
	public Object getCellEditorValue() {

		final ExpDesignNodePanel panel = renderer.getPanel();
		
		final CheckBoxNodeData checkBoxNode = new CheckBoxNodeData(
				panel.getUserObject(), 
				panel.getLabel().getText(),
				panel.getCheck().isSelected());
		
		final Object userObject = panel.getUserObject();
		ExpDesignTreeModel model = (ExpDesignTreeModel) theTree.getModel();

		if (userObject instanceof ExperimentDesignFactor) {

			ExperimentDesignFactor f = (ExperimentDesignFactor) userObject;
			f.setEnabled(panel.getCheck().isSelected());

			for (ExperimentDesignLevel l : f.getLevels()) {

				l.setEnabled(f.isEnabled());
				model.nodeChanged(model.getNodeByUserObject(l));
			}
		}
		if (userObject instanceof ExperimentDesignLevel) {

			ExperimentDesignLevel l = ((ExperimentDesignLevel) userObject);
			l.setEnabled(panel.getCheck().isSelected());

			if (l.isEnabled() && !l.getParentFactor().isEnabled()) {

				l.getParentFactor().setEnabled(true);
				//model.nodeChanged(model.getNodeByUserObject(l.getParentFactor()));
			}
		}
		return checkBoxNode;
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row) {

		final ExpDesignNodePanel editor = (ExpDesignNodePanel) renderer.getTreeCellRendererComponent(tree, value, true,
				expanded, leaf, row, true);

		final ItemListener itemListener = new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent itemEvent) {

				if (stopCellEditing())
					fireEditingStopped();
			}
		};
		editor.getCheck().addItemListener(itemListener);
		return editor;
	}

	@Override
	public boolean isCellEditable(final EventObject event) {

		if (!(event instanceof MouseEvent))
			return false;
		final MouseEvent mouseEvent = (MouseEvent) event;

		final TreePath path = theTree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

		if (path == null)
			return false;

		final Object node = path.getLastPathComponent();

		if (!(node instanceof DefaultMutableTreeNode))
			return false;

		final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;

		final Object userObject = treeNode.getUserObject();
		return userObject instanceof CheckBoxNodeData;
	}

}
