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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.xic;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.data.compare.ExtractedChromatogramComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.ExtractedChromatogramFormat;
import edu.umich.med.mrc2.datoolbox.data.format.RangeFormat;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExtractedChromatogramRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PolarityRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RangeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class ChromatogramTable  extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7515659594072618562L;
	
	public ChromatogramTable() {

		super();

		model = new ChromatogramTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ChromatogramTableModel>(
				(ChromatogramTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(ChromatogramTableModel.DATA_FILE_COLUMN),
				new ExtractedChromatogramComparator(SortProperty.dataFile));

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		columnModel.getColumnById(ChromatogramTableModel.RT_RANGE_COLUMN)
			.setCellRenderer(new RangeRenderer(MRC2ToolBoxConfiguration.getRtFormat()));
		columnModel.getColumnById(ChromatogramTableModel.POLARIRTY_COLUMN)
			.setCellRenderer(new PolarityRenderer());
		columnModel.getColumnById(ChromatogramTableModel.DATA_FILE_COLUMN)
			.setCellRenderer(new ExtractedChromatogramRenderer());
		columnModel.getColumnById(ChromatogramTableModel.NOTE_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());
		
		columnModel.getColumnById(
				ChromatogramTableModel.CHROMATOGRAM_TYPE_COLUMN).setMaxWidth(80);
		columnModel.getColumnById(
				ChromatogramTableModel.POLARIRTY_COLUMN).setMaxWidth(80);
		columnModel.getColumnById(
				ChromatogramTableModel.MS_LEVEL_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(
				ChromatogramTableModel.SUM_ALL_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(
				ChromatogramTableModel.DATA_FILE_COLUMN).setMinWidth(200);
		
//		fixedWidthColumns.add(
//				getColumnIndex(ChromatogramTableModel.CHROMATOGRAM_TYPE_COLUMN));
//		fixedWidthColumns.add(
//				getColumnIndex(ChromatogramTableModel.POLARIRTY_COLUMN));
//		fixedWidthColumns.add(
//				getColumnIndex(ChromatogramTableModel.MS_LEVEL_COLUMN));
//		fixedWidthColumns.add(
//				getColumnIndex(ChromatogramTableModel.SUM_ALL_COLUMN));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(ExtractedChromatogram.class, 
				new ExtractedChromatogramFormat(SortProperty.dataFile));
		thf.getParserModel().setComparator(ExtractedChromatogram.class, 
				new ExtractedChromatogramComparator(SortProperty.dataFile));
		thf.getParserModel().setFormat(Range.class, 
				new RangeFormat(MRC2ToolBoxConfiguration.getRtFormat()));
		
		finalizeLayout();
	}

	public void setTableModelFromChromatograms(
			Collection<ExtractedChromatogram>chromatograms) {

		thf.setTable(null);
		((ChromatogramTableModel)model).setTableModelFromChromatograms(chromatograms);
		thf.setTable(this);
		adjustColumns();
	}

	public Collection<ExtractedChromatogram> getSelectedChromatograms() {
		
		Collection<ExtractedChromatogram>selected = 
				new ArrayList<ExtractedChromatogram>();
		int chromColumn = model.getColumnIndex(ChromatogramTableModel.DATA_FILE_COLUMN);
		for(int i : getSelectedRows()) 
			selected.add((ExtractedChromatogram)model.getValueAt(
							convertRowIndexToModel(i), chromColumn));		
			
		return selected;
	}
	
	public void addChromatogram(ExtractedChromatogram chromatogram) {
		thf.setTable(null);
		((ChromatogramTableModel)model).addChromatogram(chromatogram);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void removeChromatogram(ExtractedChromatogram chromatogram) {
		thf.setTable(null);
		((ChromatogramTableModel)model).removeChromatogram(chromatogram);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void addChromatograms(Collection<ExtractedChromatogram>chromatograms) {
		thf.setTable(null);
		((ChromatogramTableModel)model).addChromatograms(chromatograms);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void removeChromatograms(Collection<ExtractedChromatogram>chromatograms) {	
		thf.setTable(null);
		chromatograms.stream().forEach(c -> ((ChromatogramTableModel)model).removeChromatogram(c));	
		thf.setTable(this);
		adjustColumns();
	}
}






























