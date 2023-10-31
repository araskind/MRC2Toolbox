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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.sop;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSSopComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSSopFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSUserFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSUserRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class ProtocolTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5111265849253755884L;
	private LIMSUserRenderer userRenderer;

	public ProtocolTable() {
		super();
		model = new ProtocolTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ProtocolTableModel>((ProtocolTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(ProtocolTableModel.SOP_COLUMN),
				new LIMSSopComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(ProtocolTableModel.CRERATED_BY_COLUMN),
				new LIMSUserComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		createInteractiveUserRenderer(Arrays.asList(ProtocolTableModel.CRERATED_BY_COLUMN));

		columnModel.getColumnById(ProtocolTableModel.SOP_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());		
		columnModel.getColumnById(ProtocolTableModel.DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSProtocol.class,
				new LIMSSopFormat(SortProperty.Name));
		thf.getParserModel().setComparator(LIMSProtocol.class,
				new LIMSSopComparator(SortProperty.Name));
		thf.getParserModel().setFormat(LIMSUser.class, new LIMSUserFormat());
		thf.getParserModel().setComparator(LIMSUser.class, new LIMSUserComparator(SortProperty.Name));

		finalizeLayout();
	}

	public void setTableModelFromProtocols(Collection<LIMSProtocol>protocols) {
		thf.setTable(null);
		((ProtocolTableModel)model).setTableModelFromProtocols(protocols);
		thf.setTable(this);
		adjustColumns();
	}

	public LIMSProtocol getSelectedProtocol() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (LIMSProtocol)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(ProtocolTableModel.SOP_COLUMN));
	}

	public Collection<LIMSProtocol> getSelectedProtocols() {

		Collection<LIMSProtocol>selected = new TreeSet<LIMSProtocol>();
		if(getSelectedRowCount() == 0)
			return selected;
		
		int prColumn = model.getColumnIndex(ProtocolTableModel.SOP_COLUMN);
		for(int i : getSelectedRows())
			selected.add((LIMSProtocol)model.getValueAt(convertRowIndexToModel(i), prColumn));

		return selected;
	}

	public Collection<LIMSProtocol> getAllProtocols(){

		Collection<LIMSProtocol>protocols = new TreeSet<LIMSProtocol>();
		int prColumn = model.getColumnIndex(ProtocolTableModel.SOP_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			protocols.add((LIMSProtocol)model.getValueAt(i, prColumn));

		return protocols;
	}
}

















