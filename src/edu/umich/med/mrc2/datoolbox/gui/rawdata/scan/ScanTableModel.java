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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.scan;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scan.StorageStrategy;
import umich.ms.datatypes.scan.props.ScanType;
import umich.ms.datatypes.scancollection.IScanCollection;

public class ScanTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -707103850490099320L;
	public static final String SCAN_COLUMN = "Scan";
	public static final String RT_COLUMN = "RT";
	public static final String LEVEL_COLUMN = "Level";	
	public static final String POLARITY_COLUMN = "Pol.";
	public static final String SCAN_TYPE_COLUMN = "Type";
	public static final String PRECURSOR_MZ_COLUMN = "Parent M/Z";
	public static final String PRECURSOR_MZ_RANGE_COLUMN = "Range";
	
	public static final NumberFormat mzRangeFormat = 
			MRC2ToolBoxConfiguration.getMzFormat();
	
	public ScanTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(SCAN_COLUMN, IScan.class, false),
			new ColumnContext(RT_COLUMN, Double.class, false),
			new ColumnContext(LEVEL_COLUMN, Integer.class, false),
			new ColumnContext(POLARITY_COLUMN, String.class, false),
			//new ColumnContext(SCAN_TYPE_COLUMN, String.class, false),	
			new ColumnContext(PRECURSOR_MZ_COLUMN, Double.class, false),
			new ColumnContext(PRECURSOR_MZ_RANGE_COLUMN, String.class, false),
		};
	}

	public void setModelFromDataFile(DataFile dataFile) {

		setRowCount(0);		
		
		LCMSData data = RawDataManager.getRawData(dataFile);
		if(data == null)
			return;
		
		IScanCollection scans = data.getScans();
		scans.isAutoloadSpectra(true);
		scans.setDefaultStorageStrategy(StorageStrategy.SOFT);		
		Map<Integer, IScan> scanIndex = RawDataUtils.getCompleteScanMap(scans);

		for (Entry<Integer, IScan> entry : scanIndex.entrySet()) {

			IScan sc = entry.getValue();
			ScanType sType = sc.getScanType();
			String type = "";
			if(sType != null)
				type = sType.name();
			
			double precMz = 0.0d;
			String precRange = null;
			if(sc.getPrecursor() != null) {
				
				if(sc.getPrecursor().getMzTarget() != null) {
					precMz = sc.getPrecursor().getMzTarget();
				}
				else {
					if(sc.getPrecursor().getMzTargetMono() != null)
						precMz = sc.getPrecursor().getMzTargetMono();
				}
				if( sc.getPrecursor().getMzRangeStart() != null 
						&& sc.getPrecursor().getMzRangeEnd() != null) { 
					Range mzRange = new Range(
							sc.getPrecursor().getMzRangeStart(), 
							sc.getPrecursor().getMzRangeEnd());
					precRange = mzRange.getFormattedString(mzRangeFormat);
				}
			}			
			Object[] obj = new Object[] { 
					sc, 
					sc.getRt(), 
					sc.getMsLevel(),
					sc.getPolarity().toString(),
					precMz,
					precRange,
				};
			super.addRow(obj);
		}		
	}	
}












