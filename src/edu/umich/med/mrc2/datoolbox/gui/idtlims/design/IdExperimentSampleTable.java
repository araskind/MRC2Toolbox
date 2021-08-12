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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.design;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.compare.StockSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.StockSampleFormat;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.StockSampleRenderer;

public class IdExperimentSampleTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3052614381449638727L;
	private IdExperimentSampleTableModel model;

	public IdExperimentSampleTable() {

		super();
		model = new IdExperimentSampleTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<IdExperimentSampleTableModel>(model);
		setRowSorter(rowSorter);

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getTableHeader().setReorderingAllowed(false);

		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRenderer(SortProperty.ID));
		setDefaultRenderer(StockSample.class, new StockSampleRenderer(SortProperty.ID));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(ExperimentalSample.class, new ExperimentalSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ExperimentalSample.class, new ExperimentalSampleComparator(SortProperty.ID));
		thf.getParserModel().setFormat(StockSample.class, new StockSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(StockSample.class, new StockSampleComparator(SortProperty.ID));
		finalizeLayout();
	}

	public void setTableModelFromSamples(Collection<IDTExperimentalSample>samples) {

		model.setTableModelFromSamples(samples);
		tca.adjustColumns();
	}

	public Collection<IDTExperimentalSample>getSelectedSamples(){

		Collection<IDTExperimentalSample>selected = new ArrayList<IDTExperimentalSample>();
		if(getSelectedRowCount() == 0)
			return selected;

		int sampleColumn = model.getColumnIndex(IdExperimentSampleTableModel.SAMPLE_ID_COLUMN);
		for(int i : getSelectedRows())
			selected.add((IDTExperimentalSample) model.getValueAt(convertRowIndexToModel(i), sampleColumn));

		return selected;
	}

	public IDTExperimentalSample getSelectedSample(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (IDTExperimentalSample) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(IdExperimentSampleTableModel.SAMPLE_ID_COLUMN));
	}
}











