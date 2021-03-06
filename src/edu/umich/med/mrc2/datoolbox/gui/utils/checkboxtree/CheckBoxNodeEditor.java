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

package edu.umich.med.mrc2.datoolbox.gui.utils.checkboxtree;

/*
 * #%L
 * Swing JTree check box nodes.
 * %%
 * Copyright (C) 2012 - 2017 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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

/**
 * A {@link TreeCellEditor} for check box tree nodes.
 * <p>
 * Thanks to John Zukowski for the <a href=
 * "http://www.java2s.com/Code/Java/Swing-JFC/CheckBoxNodeTreeSample.htm"
 * >sample code</a> upon which this is based.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3948100860205393063L;

	private final CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();

	private final JTree theTree;

	public CheckBoxNodeEditor(final JTree tree) {
		theTree = tree;
	}

	@Override
	public Object getCellEditorValue() {

		final CheckBoxNodePanel panel = renderer.getPanel();
		final CheckBoxNodeData checkBoxNode = new CheckBoxNodeData(panel.label.getText(), panel.label.getText(),
				panel.check.isSelected());
		return checkBoxNode;
	}

	@Override
	public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean selected,
			final boolean expanded, final boolean leaf, final int row) {

		final Component editor = renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);

		// editor always selected / focused
		final ItemListener itemListener = new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent itemEvent) {
				if (stopCellEditing()) {
					fireEditingStopped();
				}
			}
		};
		if (editor instanceof CheckBoxNodePanel) {
			final CheckBoxNodePanel panel = (CheckBoxNodePanel) editor;
			panel.check.addItemListener(itemListener);
		}

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
