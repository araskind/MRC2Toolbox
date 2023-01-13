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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dextr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSUserRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class DataExtractionMethodTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4961051611998862332L;

	private DataExtractionMethodTableModel model;
	private LIMSUserRenderer userRenderer;
	private MouseMotionAdapter mma;

	public DataExtractionMethodTable() {
		super();
		model =  new DataExtractionMethodTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<DataExtractionMethodTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(DataExtractionMethodTableModel.USER_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(DataExtractionMethodTableModel.DEX_METHOD_COLUMN),
				new AnalysisMethodComparator(SortProperty.Name));

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(DataExtractionMethod.class, new AnalysisMethodRenderer());
		userRenderer = new LIMSUserRenderer();
		setDefaultRenderer(LIMSUser.class, userRenderer);
		mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(DataExtractionMethodTableModel.USER_COLUMN)) &&
					columnAtPoint(p) == columnModel.getColumnIndex(DataExtractionMethodTableModel.USER_COLUMN))
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(userRenderer);
		addMouseMotionListener(userRenderer);

		columnModel.getColumnById(DataExtractionMethodTableModel.DEX_METHOD_DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, new LIMSUserComparator(SortProperty.Name));
		thf.getParserModel().setComparator(DataExtractionMethod.class, new AnalysisMethodComparator(SortProperty.Name));
		thf.getParserModel().setFormat(DataExtractionMethod.class, new AnalysisMethodFormat(SortProperty.Name));
		finalizeLayout();
	}

	public void setTableModelFromMethods(Collection<DataExtractionMethod> dataExtractionMethods) {
		model.setTableModelFromMethods(dataExtractionMethods);
		tca.adjustColumns();
	}

	public DataExtractionMethod getSelectedMethod() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (DataExtractionMethod)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(DataExtractionMethodTableModel.DEX_METHOD_COLUMN));
	}

	public void setSelectedDataExtractionMethods(Collection<DataExtractionMethod> methodsToSelect) {

		int methodColumn = getColumnIndex(DataExtractionMethodTableModel.DEX_METHOD_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(methodsToSelect.contains((DataExtractionMethod)getValueAt(i, methodColumn)))
				addRowSelectionInterval(i, i);
		}		
	}

	public Collection<DataExtractionMethod> getSelectedDataExtractionMethods() {

		Collection<DataExtractionMethod>selected = new ArrayList<DataExtractionMethod>();
		if(getSelectedRows().length == 0)
			return selected;
		
		int methodColumn = getColumnIndex(DataExtractionMethodTableModel.DEX_METHOD_COLUMN);
		for(int i : getSelectedRows()) {
			selected.add((DataExtractionMethod)getValueAt(i, methodColumn));
		}
		return selected;
	}
}




















