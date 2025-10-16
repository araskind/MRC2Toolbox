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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;

public class ManufacturerWebPageLinkRenderer  extends DefaultTableCellRenderer 
			implements MouseListener, MouseMotionListener  {

	/**
	 *
	 */
	private static final long serialVersionUID = -7560387119374843056L;
	private int row = -1;
	private int col = -1;
	private boolean isRollover = false;

	public ManufacturerWebPageLinkRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Component rendererComponent = 
				table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());

		if(value == null){
			setText("");
			return this;
		}
		String link = null;
		Manufacturer vendor = null;
		if(value instanceof Manufacturer)
			vendor = (Manufacturer)value;
		
		if(vendor == null) {
			setText("");
			return this;
		}
		String linkLabel = vendor.getCatalogWebAddress();
		if(linkLabel != null && !linkLabel.isEmpty()) {
			
			String urlString = linkLabel;
			if(!urlString.startsWith("http"))
				urlString = "https://" + urlString;
			
			link =
				"<A HREF=\"" + urlString + "\">" + linkLabel + "</A>";
		}
		if (!table.isEditing() && this.row == row && this.col == column && this.isRollover) {
			setText("<html><u><font color='blue'>" + link);
		} else if (hasFocus) {
			setText("<html><font color='blue'>" + link);
		} else {
			setText("<html><font color='black'>" + link);
		}		
		return this;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		JTable table = (JTable) e.getSource();
		Point pt = e.getPoint();
		int ccol = table.columnAtPoint(pt);
		int crow = table.rowAtPoint(pt);		
		if(table.getCellRenderer(crow, ccol) instanceof ManufacturerWebPageLinkRenderer) {
			
			URL url;
			String urlString = "";
			try {
				Object cellValue = table.getValueAt(crow, ccol);
				Manufacturer vendor = null;
				if(cellValue instanceof Manufacturer)
					vendor = (Manufacturer)cellValue;

				if(vendor != null)
					urlString = vendor.getCatalogWebAddress();
				
				if(!urlString.startsWith("http"))
					urlString = "https://" + urlString;
			} 
			catch (Exception e2) {
				//	e2.printStackTrace();
			}
			try {
				url = new URL(urlString);
				try {
					if (Desktop.isDesktopSupported())
						Desktop.getDesktop().browse(url.toURI());

				} catch (Exception ex) {
					// ex.printStackTrace();
				}
			} catch (MalformedURLException e1) {

			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//	setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent e) {

		JTable table = (JTable) e.getSource();
		table.repaint(table.getCellRect(row, col, false));
		row = -1;
		col = -1;
		isRollover = false;
		//	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mouseMoved(MouseEvent e) {

		JTable table = (JTable) e.getSource();
		Point pt = e.getPoint();
		int prev_row = row;
		int prev_col = col;
		boolean prev_ro = isRollover;
		row = table.rowAtPoint(pt);
		col = table.columnAtPoint(pt);
		//isRollover = isURLColumn(table, col);

		isRollover = true;

		if (row == prev_row && col == prev_col && isRollover == prev_ro || !isRollover && !prev_ro)
			return;

		Rectangle repaintRect;
		if (isRollover) {
			Rectangle r = table.getCellRect(row, col, false);
			repaintRect = prev_ro ? r.union(table.getCellRect(prev_row, prev_col, false)) : r;
		} else {
			repaintRect = table.getCellRect(prev_row, prev_col, false);
		}
		table.repaint(repaintRect);
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}
}
