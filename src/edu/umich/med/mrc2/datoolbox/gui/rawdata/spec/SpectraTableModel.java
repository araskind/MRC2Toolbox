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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class SpectraTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -4864924940070524356L;

	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String MS_LEVEL_COLUMN = "Level";
	public static final String RT_RANGE_COLUMN = "RT range";
	
	public SpectraTableModel() {
		super();
		columnArray = new ColumnContext[] {					
			new ColumnContext(MS_LEVEL_COLUMN, Integer.class, false),
			new ColumnContext(RT_RANGE_COLUMN, Range.class, false),
			new ColumnContext(DATA_FILE_COLUMN, AverageMassSpectrum.class, false),
		};
	}

	public void setTableModelFromSpectra(
			Collection<AverageMassSpectrum>spectra) {

		setRowCount(0);
		addSpectra(spectra);
	}
	
	private Object[] createRowData(AverageMassSpectrum spectrum) {
		
		Object[] obj = {				
				spectrum.getMsLlevel(),
				spectrum.getRtRange(),
				spectrum,
			};
		return obj;
	}
	
	public void addSpectrum(AverageMassSpectrum spectrum) {
		addRow(createRowData(spectrum));
	}
	
	public void removeSpectrum(AverageMassSpectrum spectrum) {
		
		int specCol = getColumnIndex(DATA_FILE_COLUMN);
		for(int i = 0; i<getRowCount(); i++) {
			
			if(getValueAt(i, specCol).equals(spectrum)) {
				removeRow(i);
				return;
			}
		}
	}

	public void addSpectra(Collection<AverageMassSpectrum>spectra) {

		if(spectra == null || spectra.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for(AverageMassSpectrum spec : spectra) 
			rowData.add(createRowData(spec));
		
		addRows(rowData);
	}
}























