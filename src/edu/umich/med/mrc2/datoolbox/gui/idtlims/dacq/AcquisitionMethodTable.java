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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ChromatographicColumnComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ChromatographicColumnFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChromatographicColumnRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSUserRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class AcquisitionMethodTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;
	private AcquisitionMethodTableModel model;
	private MouseMotionAdapter mma;
	private LIMSUserRenderer userRenderer;

	public AcquisitionMethodTable() {
		super();
		model =  new AcquisitionMethodTableModel();
		setModel(model);
		
		rowSorter = new TableRowSorter<AcquisitionMethodTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(AcquisitionMethodTableModel.USER_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(AcquisitionMethodTableModel.ACQ_METHOD_COLUMN),
				new AnalysisMethodComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(AcquisitionMethodTableModel.COLUMN_NAME_COLUMN),
				new ChromatographicColumnComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(DataAcquisitionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(LIMSChromatographicColumn.class, new ChromatographicColumnRenderer());		
		userRenderer = new LIMSUserRenderer();
		setDefaultRenderer(LIMSUser.class, userRenderer);
		columnModel.getColumnById(AcquisitionMethodTableModel.ACQ_METHOD_DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
	
		mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(AcquisitionMethodTableModel.USER_COLUMN)) &&
					columnAtPoint(p) == columnModel.getColumnIndex(AcquisitionMethodTableModel.USER_COLUMN))
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
		thf.getParserModel().setComparator(DataAcquisitionMethod.class, new AnalysisMethodComparator(SortProperty.Name));
		thf.getParserModel().setFormat(DataAcquisitionMethod.class, new AnalysisMethodFormat(SortProperty.Name));		
		thf.getParserModel().setComparator(LIMSChromatographicColumn.class, new ChromatographicColumnComparator(SortProperty.Name));
		thf.getParserModel().setFormat(LIMSChromatographicColumn.class, new ChromatographicColumnFormat(SortProperty.Name));
		
		finalizeLayout();
	}

	public void setTableModelFromAcquisitionMethods(Collection<DataAcquisitionMethod> acquisitionMethods) {
		
		thf.setTable(null);
		model.setTableModelFromMethods(acquisitionMethods);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public void setSelectedAcquisitionMethods(Collection<DataAcquisitionMethod> acquisitionMethods) {
		
		int methodColumn = getColumnIndex(AcquisitionMethodTableModel.ACQ_METHOD_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(acquisitionMethods.contains((DataAcquisitionMethod)getValueAt(i, methodColumn)))
				addRowSelectionInterval(i, i);
		}
	}

	public DataAcquisitionMethod getSelectedMethod() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (DataAcquisitionMethod) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(AcquisitionMethodTableModel.ACQ_METHOD_COLUMN));
	}
	
	public Collection<DataAcquisitionMethod> getSelectedAcquisitionMethods() {
		
		Collection<DataAcquisitionMethod>selected = new ArrayList<DataAcquisitionMethod>();
		if(getSelectedRows().length == 0)
			return selected;
		
		int methodColumn = getColumnIndex(AcquisitionMethodTableModel.ACQ_METHOD_COLUMN);
		for(int i : getSelectedRows()) {
			selected.add((DataAcquisitionMethod)getValueAt(i, methodColumn));
		}
		return selected;
	}
}
















