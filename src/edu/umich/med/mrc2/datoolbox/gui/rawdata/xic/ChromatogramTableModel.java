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
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class ChromatogramTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -4864924940070524356L;

	public static final String CHROMATOGRAM_TYPE_COLUMN = "Type";
	public static final String POLARIRTY_COLUMN = "Polarity";
	public static final String MS_LEVEL_COLUMN = "Level";
	public static final String MASSES_COLUMN = "M/Z list";
	public static final String SUM_ALL_COLUMN = "Sum";
	public static final String MASS_WINDOW_COLUMN = "M/Z window";
	public static final String RT_RANGE_COLUMN = "RT range";
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String NOTE_COLUMN = "Notes";
	
	public ChromatogramTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(CHROMATOGRAM_TYPE_COLUMN, "Chromatogram type", String.class, false),
			new ColumnContext(POLARIRTY_COLUMN, POLARIRTY_COLUMN, Polarity.class, false),			
			new ColumnContext(MS_LEVEL_COLUMN, "MS level", Integer.class, false),
			new ColumnContext(MASSES_COLUMN, MASSES_COLUMN, String.class, false),
			new ColumnContext(SUM_ALL_COLUMN, "Is this sum of multiple extracted M/Z values", Boolean.class, false),
			new ColumnContext(MASS_WINDOW_COLUMN, "M/Z window for chromatogram extraction", String.class, false),
			new ColumnContext(RT_RANGE_COLUMN, "Chromatogram retention time range", Range.class, false),
			new ColumnContext(DATA_FILE_COLUMN,"Source data file",  ExtractedChromatogram.class, false),			
			new ColumnContext(NOTE_COLUMN, NOTE_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromChromatograms(
			Collection<ExtractedChromatogram>chromatograms) {

		setRowCount(0);
		addChromatograms(chromatograms);
	}
	
	private Object[] createRowData(ExtractedChromatogram chromatogram) {
		
		ChromatogramDefinition def = 
				chromatogram.getChromatogramDefinition();
		Object[] obj = {
				def.getMode().name(),
				def.getPolarity(),
				def.getMsLevel(),
				def.getMzListString(),
				def.getSumAllMassChromatograms(),
				def.getMZWindowString(),
				def.getRtRange(),
				chromatogram,
				chromatogram.getNote(),
			};
		return obj;
	}
	
	public void addChromatogram(ExtractedChromatogram chromatogram) {
		addRow(createRowData(chromatogram));
	}
	
	public void removeChromatogram(ExtractedChromatogram chromatogram) {
		
		int chromCol = getColumnIndex(DATA_FILE_COLUMN);
		for(int i = 0; i<getRowCount(); i++) {
			
			if(getValueAt(i, chromCol).equals(chromatogram)) {
				removeRow(i);
				return;
			}
		}
	}

	public void addChromatograms(Collection<ExtractedChromatogram> chromatograms) {

		if(chromatograms == null || chromatograms.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for(ExtractedChromatogram chr : chromatograms) 
			rowData.add(createRowData(chr));
		
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}























