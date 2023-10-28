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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.lookup;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.SampleTypeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.SampleTypeFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.SampleTypeRenderer;

public class SampleTypeTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1837501767078275796L;

	public SampleTypeTable() {
		super();
		model = new SampleTypeTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<SampleTypeTableModel>((SampleTypeTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(SampleTypeTableModel.SAMPLE_TYPE_ID_COLUMN),
				new SampleTypeComparator(SortProperty.ID));

		setDefaultRenderer(LIMSSampleType.class, new SampleTypeRenderer(SortProperty.ID));
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSSampleType.class, new SampleTypeFormat(SortProperty.ID));
		thf.getParserModel().setComparator(LIMSSampleType.class, new SampleTypeComparator(SortProperty.ID));
		finalizeLayout();
	}

	public void setModelFromSampleTypeList(Collection<LIMSSampleType>typeList) {

		thf.setTable(null);
		((SampleTypeTableModel)model).setModelFromSampleTypeList(typeList);
		thf.setTable(this);
		adjustColumns();
	}

	public LIMSSampleType getSelectedSampleType() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (LIMSSampleType)model.getValueAt(convertRowIndexToModel(row),
			model.getColumnIndex(SampleTypeTableModel.SAMPLE_TYPE_ID_COLUMN));
	}

	public Collection<LIMSSampleType> getSelectedSampleTypes() {
		
		Collection<LIMSSampleType>selectedTypes = new ArrayList<LIMSSampleType>();
		int col = model.getColumnIndex(SampleTypeTableModel.SAMPLE_TYPE_ID_COLUMN);
		for(int i : getSelectedRows())
			selectedTypes.add((LIMSSampleType)model.getValueAt(convertRowIndexToModel(i), col));
			
		return selectedTypes;
	}
	
	public void setSelectedSampleTypes(Collection<LIMSSampleType>typeList) {

		int col = getColumnIndex(SampleTypeTableModel.SAMPLE_TYPE_ID_COLUMN);
		if(col == -1)
			return;
		
		for(int i=0; i<getRowCount(); i++) {
			
			if(typeList.contains(getValueAt(i, col)))
				addRowSelectionInterval(i, i);
		}
	}	
}










