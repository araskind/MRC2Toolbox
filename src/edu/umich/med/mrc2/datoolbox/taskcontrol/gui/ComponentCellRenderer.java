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

package edu.umich.med.mrc2.datoolbox.taskcontrol.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.OverlayLayout;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class ComponentCellRenderer implements TableCellRenderer, ListCellRenderer {

	private boolean createTooltips;
	private Font font;

	/**
	*/
	public ComponentCellRenderer() {
		this(false, null);
	}

	/**
	 * @param font
	 */
	public ComponentCellRenderer(boolean createTooltips) {
		this(createTooltips, null);
	}

	/**
	 * @param font
	 */
	public ComponentCellRenderer(boolean createTooltips, Font font) {
		this.createTooltips = createTooltips;
		this.font = font;
	}

	/**
	 * @param font
	 */
	public ComponentCellRenderer(Font font) {
		this(false, font);
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
	 *      java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean hasFocus) {

		JPanel newPanel = new JPanel();
		newPanel.setLayout(new OverlayLayout(newPanel));

		Color bgColor;

		if (isSelected)
			bgColor = list.getSelectionBackground();
		else
			bgColor = list.getBackground();

		newPanel.setBackground(bgColor);

		if (hasFocus) {
			Border border = null;
			if (isSelected)
				border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
			if (border == null)
				border = UIManager.getBorder("List.focusCellHighlightBorder");
			if (border != null)
				newPanel.setBorder(border);
		}

		if (value != null) {

			if (value instanceof JComponent) {

				newPanel.add((JComponent) value);

			} else {

				JLabel newLabel = new JLabel(value.toString());

				if (font != null)
					newLabel.setFont(font);
				else if (list.getFont() != null)
					newLabel.setFont(list.getFont());

				newPanel.add(newLabel);
			}
		}

		return newPanel;

	}

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		JPanel newPanel = new JPanel();
		newPanel.setLayout(new OverlayLayout(newPanel));

		Color bgColor;

		if (isSelected)
			bgColor = table.getSelectionBackground();
		else
			bgColor = table.getBackground();

		newPanel.setBackground(bgColor);

		if (hasFocus) {
			Border border = null;
			if (isSelected)
				border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
			if (border == null)
				border = UIManager.getBorder("Table.focusCellHighlightBorder");
			if (border != null)
				newPanel.setBorder(border);
		}

		if (value != null) {

			if (value instanceof JComponent) {

				newPanel.add((JComponent) value);

			} else {

				JLabel newLabel = new JLabel(value.toString());

				if (font != null)
					newLabel.setFont(font);
				else if (table.getFont() != null)
					newLabel.setFont(table.getFont());

				newPanel.add(newLabel);
			}

			if (createTooltips)
				newPanel.setToolTipText(value.toString());

		}

		return newPanel;

	}

}
