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

package edu.umich.med.mrc2.datoolbox.gui.tables;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class LineNumberTableRowHeader extends JComponent {

	/**
	 *
	 */
	private static final long serialVersionUID = 2264849435872212242L;
	private final JTable table;
	private final JScrollPane scrollPane;

	public LineNumberTableRowHeader(JScrollPane jScrollPane, JTable table) {
		this.scrollPane = jScrollPane;
		this.table = table;
		this.table.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent tme) {
				LineNumberTableRowHeader.this.repaint();
			}
		});

		this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent lse) {

				if(!lse.getValueIsAdjusting())
					LineNumberTableRowHeader.this.repaint();
			}
		});

		this.scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				LineNumberTableRowHeader.this.repaint();
			}
		});

		setPreferredSize(new Dimension(40, 90));
		// setMinimumSize (new Dimension(40, 90));
		// setMaximumSize (new Dimension(100, 90));
	}

	protected void paintComponent(Graphics g) {
		Point viewPosition = scrollPane.getViewport().getViewPosition();
		Dimension viewSize = scrollPane.getViewport().getViewSize();
		if (getHeight() < viewSize.height) {
			Dimension size = getPreferredSize();
			size.height = viewSize.height;
			setSize(size);
			setPreferredSize(size);
		}

		super.paintComponent(g);

		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		FontMetrics fm = g.getFontMetrics();

		for (int r = 0; r < table.getRowCount(); r++) {
			Rectangle cellRect = table.getCellRect(r, 0, false);

			boolean rowSelected = table.isRowSelected(r);

			if (rowSelected) {
				g.setColor(table.getSelectionBackground());
				g.fillRect(0, cellRect.y, getWidth(), cellRect.height);
			}

			if ((cellRect.y + cellRect.height) - viewPosition.y >= 0 && cellRect.y < viewPosition.y + viewSize.height) {
				g.setColor(table.getGridColor());
				g.drawLine(0, cellRect.y + cellRect.height, getWidth(), cellRect.y + cellRect.height);
				g.setColor(rowSelected ? table.getSelectionForeground() : getForeground());
				String s = Integer.toString(r + 1);
				g.drawString(s, getWidth() - fm.stringWidth(s) - 2, cellRect.y + cellRect.height - fm.getDescent());
			}
		}

		if (table.getShowVerticalLines()) {
			g.setColor(table.getGridColor());
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
	}
}
