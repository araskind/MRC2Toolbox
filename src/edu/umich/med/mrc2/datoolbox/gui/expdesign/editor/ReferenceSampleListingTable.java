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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.util.Collection;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormatExtended;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRendererExtended;

public class ReferenceSampleListingTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ReferenceSampleListingTableModel model;

	public ReferenceSampleListingTable() {
		super();
		model = new ReferenceSampleListingTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ReferenceSampleListingTableModel>(model);
		setRowSorter(rowSorter);

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setDefaultRenderer(ExperimentalSample.class, 
				new ExperimentalSampleRendererExtended());

		rowSorter.setComparator(
				model.getColumnIndex(ReferenceSampleListingTableModel.REF_SAMPLE_COLUMN),
				new ExperimentalSampleComparator(SortProperty.ID));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(ExperimentalSample.class,
				new ExperimentalSampleFormatExtended());
		thf.getParserModel().setComparator(ExperimentalSample.class,
				new ExperimentalSampleComparator(SortProperty.ID));
		
		finalizeLayout();	
	}
	
	public void setTableModelFromReferenceSamples(Collection<ExperimentalSample>samples) {
		model.setTableModelFromReferenceSamples(samples);
	}
	
	Collection<ExperimentalSample>getSelectedSamples(){
		
		Collection<ExperimentalSample>selected = 
				new TreeSet<ExperimentalSample>(new ExperimentalSampleComparator(SortProperty.ID));
		if(getSelectedRowCount() == 0)
			return selected;
		
		int prColumn = model.getColumnIndex(ReferenceSampleListingTableModel.REF_SAMPLE_COLUMN);
		for(int i : getSelectedRows())
			selected.add((ExperimentalSample)model.getValueAt(convertRowIndexToModel(i), prColumn));

		return selected;
	}
	
	Collection<ExperimentalSample>getAllSamples(){
		
		Collection<ExperimentalSample>selected = 
				new TreeSet<ExperimentalSample>(new ExperimentalSampleComparator(SortProperty.ID));
		
		int prColumn = model.getColumnIndex(ReferenceSampleListingTableModel.REF_SAMPLE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			selected.add((ExperimentalSample)model.getValueAt(i, prColumn));

		return selected;
	}
}
