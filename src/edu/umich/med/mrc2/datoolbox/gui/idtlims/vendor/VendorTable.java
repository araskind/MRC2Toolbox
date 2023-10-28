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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.vendor;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ManufacturerWebPageLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class VendorTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2069272556941448636L;

	public VendorTable() {
		super();
		model = new VendorTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<VendorTableModel>((VendorTableModel)model);
		setRowSorter(rowSorter);		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnModel.getColumnById(VendorTableModel.VENDOR_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
		
		columnModel.getColumnById(VendorTableModel.VENDOR_COLUMN).setMaxWidth(300);
		columnModel.getColumnById(VendorTableModel.VENDOR_COLUMN).setMinWidth(200);
		
		ManufacturerWebPageLinkRenderer vendorLinkRenderer = 
				new ManufacturerWebPageLinkRenderer();
		columnModel.getColumnById(VendorTableModel.WEB_ADDRESS_COLUMN).
			setCellRenderer(vendorLinkRenderer);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
		
		//	Database link adapter
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(VendorTableModel.WEB_ADDRESS_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(VendorTableModel.WEB_ADDRESS_COLUMN))
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					else
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(vendorLinkRenderer);
		addMouseMotionListener(vendorLinkRenderer);
	}

	public void setTableModelFromManufacturers(Collection<Manufacturer>manufacturerList) {

		thf.setTable(null);
		((VendorTableModel)model).setTableModelFromManufacturers(manufacturerList);
		thf.setTable(this);
		adjustColumns();		
	}

	public Manufacturer getSelectedVendor(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (Manufacturer) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(VendorTableModel.VENDOR_COLUMN));
	}

	public void selectSoftware(Manufacturer item) {

		int colIdx = model.getColumnIndex(VendorTableModel.VENDOR_COLUMN);
		if(colIdx == -1)
			return;
		
		for(int i=0; i<model.getRowCount(); i++) {

			if(model.getValueAt(i,colIdx).equals(item)){
				
				int row = convertRowIndexToView(i);			
				setRowSelectionInterval(row, row);
				this.scrollToSelected();
				return;
			}
		}
	}
}
