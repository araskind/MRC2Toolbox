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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;

public class LIMSUserRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener  {

	/**
	 *
	 */
	private static final long serialVersionUID = -6997215377805652927L;
	private int row = -1;
	private int col = -1;
	private boolean isRollover = false;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

//		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
    	Component rendererComponent = table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());
		
		Color fg = rendererComponent.getForeground();
		String hex = String.format("#%02X%02X%02X", fg.getRed(), fg.getGreen(), fg.getBlue());  
		
		String link = null;
		if(value == null){
			setText("");
			return this;
		}
		if (value instanceof LIMSUser) {

			LIMSUser user = (LIMSUser)value;
			if(user.getEmail() == null) {
				setText(user.getFullName());
				return this;
			}
			if(user.getEmail().isEmpty()) {
				setText(user.getFullName());
				return this;
			}
			link = "<A HREF=\"mailto:" + user.getEmail() + "\">" + user.getFullName() + "</A>";

			if (!table.isEditing() && this.row == row && this.col == column && this.isRollover) {
				setText("<html><u><font color='blue'>" + link);
			} else if (hasFocus) {
				setText("<html><font color='blue'>" + link);
			} else {
				setText("<html><font color='" + hex + "'>" + link);
			}
		}
		return this;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

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
	public void mouseClicked(MouseEvent e) {

		JTable table = (JTable) e.getSource();
		Point pt = e.getPoint();

		if(table.rowAtPoint(pt) == -1)
			return;

		Object cellValue = table.getValueAt(table.rowAtPoint(pt), table.columnAtPoint(pt));

		if(cellValue == null)
			return;

		if(cellValue.getClass().equals(LIMSUser.class)) {

			LIMSUser user = (LIMSUser)cellValue;
			if(user.getEmail() == null)
				return;

			if(user.getEmail().isEmpty())
				return;

	        try {
	            Desktop.getDesktop().mail(new URI("mailto:" + user.getEmail()));
	        } catch (URISyntaxException | IOException ex) {
	            ex.printStackTrace();
	        }
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {

		JTable table = (JTable) e.getSource();
		table.repaint(table.getCellRect(row, col, false));
		row = -1;
		col = -1;
		isRollover = false;
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
}










