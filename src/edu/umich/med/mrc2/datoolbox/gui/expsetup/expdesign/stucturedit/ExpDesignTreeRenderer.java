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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.checkboxtree.CheckBoxNodeData;

public class ExpDesignTreeRenderer implements TreeCellRenderer {

	static final Icon factorIcon = GuiUtils.getIcon("factor", 32);
	static final Icon levelIcon = GuiUtils.getIcon("level", 24);
	static final Icon levelInactiveIcon = GuiUtils.getIcon("levelInactive", 24);

	static final Font bigFont = new Font("SansSerif", Font.BOLD, 12);
	static final Font smallerFont = new Font("SansSerif", Font.PLAIN, 11);
	static final Font smallFont = new Font("SansSerif", Font.PLAIN, 10);

	private final ExpDesignNodePanel panel = new ExpDesignNodePanel();
	private final DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

	private final Color selectionForeground, selectionBackground;
	private final Color textForeground, textBackground;

	public ExpDesignTreeRenderer() {

		final Font fontValue = UIManager.getFont("Tree.font");
		if (fontValue != null)
			panel.getLabel().setFont(fontValue);

		final Boolean focusPainted = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
		panel.getCheck().setFocusPainted(focusPainted != null && focusPainted);

		selectionForeground = UIManager.getColor("Tree.selectionForeground");
		selectionBackground = UIManager.getColor("Tree.selectionBackground");
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");
	}

	protected ExpDesignNodePanel getPanel() {
		return panel;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {

		CheckBoxNodeData data = null;

		if (value instanceof DefaultMutableTreeNode) {

			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

			final Object userObject = node.getUserObject();

			if (userObject instanceof CheckBoxNodeData)
				data = (CheckBoxNodeData) userObject;
		}
		if (data == null)
			return defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		panel.setUserObject(data.getUserObject());

		if (selected) {
			panel.setForeground(selectionForeground);
			panel.setBackground(selectionBackground);
			panel.getLabel().setForeground(selectionForeground);
			panel.getLabel().setBackground(selectionBackground);
		} else {
			panel.setForeground(textForeground);
			panel.setBackground(textBackground);
			panel.getLabel().setForeground(textForeground);
			panel.getLabel().setBackground(textBackground);
		}
		if (data.getUserObject() instanceof ExperimentDesignFactor) {

			panel.getLabel().setIcon(factorIcon);
			panel.getLabel().setFont(bigFont);
			panel.getLabel().setText(((ExperimentDesignFactor) data.getUserObject()).getName());
			panel.getCheck().setSelected(((ExperimentDesignFactor) data.getUserObject()).isEnabled());
		}
		if (data.getUserObject() instanceof ExperimentDesignLevel) {

			ExperimentDesignLevel level = (ExperimentDesignLevel) data.getUserObject();

			if(level.isEnabled())
				panel.getLabel().setIcon(levelIcon);
			else
				panel.getLabel().setIcon(levelInactiveIcon);

			panel.getLabel().setFont(smallerFont);
			panel.getLabel().setText(((ExperimentDesignLevel) data.getUserObject()).getName());
			panel.getCheck().setSelected(((ExperimentDesignLevel) data.getUserObject()).isEnabled());
		}
		return panel;
	}
}
