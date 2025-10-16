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

package edu.umich.med.mrc2.datoolbox.gui.mzfreq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.data.compare.MzFrequencyObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.RangeMidPoitComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.MzFrequencyObjectFormat;
import edu.umich.med.mrc2.datoolbox.data.format.RangeFormat;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedRangeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MzFrequencyObjectRTSpreadRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MzFrequencyObjectRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MzFrequencyDataTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1511489263877797538L;

	public MzFrequencyDataTable() {

		super();
				
		model = new MzFrequencyDataTableModel();
		setModel(model);
		
		rowSorter = new TableRowSorter<MzFrequencyDataTableModel>(
				(MzFrequencyDataTableModel)model);
		rowSorter.setComparator(model.getColumnIndex(MzFrequencyDataTableModel.AVG_MZ_COLUMN),
				new MzFrequencyObjectComparator(SortProperty.rangeMidpoint));
		rowSorter.setComparator(model.getColumnIndex(MzFrequencyDataTableModel.MZ_RANGE_COLUMN),
				new RangeMidPoitComparator());
		rowSorter.setComparator(model.getColumnIndex(MzFrequencyDataTableModel.RT_RANGE_COLUMN),
				new RangeMidPoitComparator());
		rowSorter.setComparator(model.getColumnIndex(MzFrequencyDataTableModel.RT_RSD_COLUMN),
				new MzFrequencyObjectComparator(SortProperty.RSD));
		setRowSorter(rowSorter);

		columnModel.getColumnById(MzFrequencyDataTableModel.AVG_MZ_COLUMN)
				.setCellRenderer(new MzFrequencyObjectRenderer(SortProperty.MZ));	
		columnModel.getColumnById(MzFrequencyDataTableModel.MZ_RANGE_COLUMN)
				.setCellRenderer(new FormattedRangeRenderer(MRC2ToolBoxConfiguration.getMzFormat()));
		columnModel.getColumnById(MzFrequencyDataTableModel.RT_RANGE_COLUMN)
				.setCellRenderer(new FormattedRangeRenderer(MRC2ToolBoxConfiguration.getRtFormat()));
		
		columnModel.getColumnById(MzFrequencyDataTableModel.RT_RSD_COLUMN)
			.setCellRenderer(new MzFrequencyObjectRTSpreadRenderer());
		FormattedDecimalRenderer ppmRenderer = new FormattedDecimalRenderer(
				MRC2ToolBoxConfiguration.getPpmFormat());
		columnModel.getColumnById(MzFrequencyDataTableModel.FREQUENCY_COLUMN)
				.setCellRenderer(ppmRenderer);
		columnModel.getColumnById(MzFrequencyDataTableModel.PERCENT_IDENTIFIED_COLUMN)
				.setCellRenderer(ppmRenderer);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(MzFrequencyObject.class, 
				new MzFrequencyObjectFormat(SortProperty.rangeMidpoint));
		thf.getParserModel().setComparator(MzFrequencyObject.class, 
				new MzFrequencyObjectComparator(SortProperty.rangeMidpoint));
		thf.getParserModel().setFormat(Range.class, 
				new RangeFormat(MRC2ToolBoxConfiguration.getRtFormat()));
		thf.getParserModel().setComparator(Range.class, 
				new RangeMidPoitComparator());
		finalizeLayout();
	}

	public void setTableModelFromMzFrequencyObjectCollection(
			Collection<MzFrequencyObject> collection) {
		thf.setTable(null);
		((MzFrequencyDataTableModel)model).setTableModelFromMzFrequencyObjectCollection(collection);
		thf.setTable(this);
		adjustColumns();
	}
	
	public MzFrequencyObject getSelectedMzFrequencyObject() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (MzFrequencyObject)model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(MzFrequencyDataTableModel.AVG_MZ_COLUMN));		
	}
	
	public Collection<MzFrequencyObject> getSelectedMzFrequencyObjects() {
		
		Collection<MzFrequencyObject>selected = new ArrayList<MzFrequencyObject>();
		int[] rows = getSelectedRows();
		if(rows.length == 0)
			return selected;
		
		int col = model.getColumnIndex(MzFrequencyDataTableModel.AVG_MZ_COLUMN);
		for(int row : rows) {
			
			MzFrequencyObject mzfo = 
					(MzFrequencyObject)model.getValueAt(convertRowIndexToModel(row), col);			
			selected.add(mzfo);
		}		
		return selected;	
	}
	
	public Collection<MsFeature>getMsFeaturesForSelectedLines(){
		
		Collection<MsFeature>selected = new ArrayList<MsFeature>();
		Collection<MzFrequencyObject>mzfoCollection = getSelectedMzFrequencyObjects();
		if(mzfoCollection.isEmpty())
			return selected;
		
		selected = mzfoCollection.stream().
				flatMap(o -> o.getFeatureCluster().getFeatures().stream()).
				collect(Collectors.toList());
		
		return selected;
	}
}















