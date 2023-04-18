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

package edu.umich.med.mrc2.datoolbox.gui.tables.ms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.scan.IScan;

public class MsMsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -4864924940070524356L;

	public static final String MZ_COLUMN = "M/Z";
	public static final String INTENSITY_COLUMN = "Intensity";
	public static final String ANNOTATION_COLUMN = "Annotation";

	public MsMsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(MZ_COLUMN, Double.class, false),
			new ColumnContext(INTENSITY_COLUMN, Double.class, false),
			new ColumnContext(ANNOTATION_COLUMN, String.class, false)
		};
	}

	//	For now deal only with MS2
	public void setTableModelFromTandemMs(TandemMassSpectrum msms) {

		setRowCount(0);
		if(msms == null || msms.getMassSortedSpectrum().length == 0)
			return;
		
		MsPoint trueParent = msms.getActualParentIon();
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(MsPoint p : msms.getMassSortedSpectrum()) {

			String annotation = "";
			if(p.equals(trueParent))
				annotation = "***";

			Object[] obj = new Object[] {
				p.getMz(),
				p.getIntensity(),
				annotation
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public void setTableModelFromScan(IScan scan) {
		
		setRowCount(0);
		if(scan == null)
			return;
		
		Collection<MsPoint> points = RawDataUtils.getScanPoints(scan);
		double precursorMz = RawDataUtils.getScanPrecursorMz(scan);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(MsPoint dp : points) {
			
			String annotation = "";
			if(dp.getMz() == precursorMz)
				annotation = "***";

			Object[] obj = {
					dp.getMz(),
					dp.getIntensity(),
					annotation,
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public void setTableModelFromDataPoints(
			Collection<MsPoint> points, MsPoint parent) {
		
		setRowCount(0);
		if(points == null || points.isEmpty())
			return;
		
		TreeSet<MsPoint>spectrum = 
				new TreeSet<MsPoint>(new MsDataPointComparator(SortProperty.MZ));
		spectrum.addAll(points);
		double parentMz = 0.0d;
		if(parent != null) {
			spectrum.add(parent);
			parentMz = parent.getMz();
		}
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(MsPoint dp : spectrum) {
			
			String annotation = "";
			if(dp.getMz() == parentMz)
				annotation = "***";

			Object[] obj = {
					dp.getMz(),
					dp.getIntensity(),
					annotation,
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}












