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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.util.Collection;
import java.util.TreeSet;

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
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.StockSampleRenderer;

public class PrepSampleTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8797892159401292726L;

	public PrepSampleTable() {

		super();
		model = new PrepSampleTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<PrepSampleTableModel>((PrepSampleTableModel)model);
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

	public void setTableModelFromSamples(Collection<? extends ExperimentalSample>samples) {
		thf.setTable(null);
		((PrepSampleTableModel)model).setTableModelFromSamples(samples);
		thf.setTable(this);
		adjustColumns();
	}

	public Collection<IDTExperimentalSample>getSelectedSamples(){

		Collection<IDTExperimentalSample>selectedSamples = new TreeSet<IDTExperimentalSample>();
		int sCol = model.getColumnIndex(PrepSampleTableModel.SAMPLE_ID_COLUMN);
		int enabledCol = model.getColumnIndex(PrepSampleTableModel.ENABLED_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {

			if((Boolean)model.getValueAt(i,enabledCol))
				selectedSamples.add((IDTExperimentalSample)model.getValueAt(i,sCol));
		}
		return selectedSamples;
	}
}
