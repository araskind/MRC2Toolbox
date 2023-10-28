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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.user;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSUserRenderer;

public class UserTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2959278423863597193L;
	private LIMSUserRenderer userRenderer;

	public UserTable() {
		super();
		model = new UserTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<UserTableModel>((UserTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(UserTableModel.USER_COLUMN),
				new LIMSExperimentComparator(SortProperty.Name, SortDirection.DESC));
		
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowHeight(80);
		userRenderer = new LIMSUserRenderer();
		setDefaultRenderer(LIMSUser.class, userRenderer);

		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(UserTableModel.USER_COLUMN)) &&
					columnAtPoint(p) == columnModel.getColumnIndex(UserTableModel.USER_COLUMN))
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(userRenderer);
		addMouseMotionListener(userRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, new LIMSUserComparator(SortProperty.Name));

		finalizeLayout();
	}

	public void setTableModelFromUserList(Collection<LIMSUser> users) {
		thf.setTable(null);
		((UserTableModel)model).setTableModelFromUserList(users);
		thf.setTable(this);
		adjustColumns();
	}

	public LIMSUser getSelectedUser() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (LIMSUser) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(UserTableModel.USER_COLUMN));
	}
	
	public void selectUser(LIMSUser userToSelect) {
		
		int userColumn = getColumnIndex(UserTableModel.USER_COLUMN);
		//	TODO handle through model
		if(userColumn == -1)
			return;
		
		for(int i=0; i<getRowCount(); i++) {
			LIMSUser user = (LIMSUser) getValueAt(i, userColumn);
			if(userToSelect.equals(user)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				return;
			}
		}
	}
}










