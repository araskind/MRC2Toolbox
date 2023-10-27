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

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.scan.IScan;

public class MsOneTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -4864924940070524356L;

	public static final String MZ_COLUMN = "M/Z";
	public static final String INTENSITY_COLUMN = "Intensity";
	public static final String ADDUCT_COLUMN = "Adduct";
	public static final String NUM_ION_COLUMN = "# in series";

	public MsOneTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(MZ_COLUMN, MZ_COLUMN, Double.class, false),
			new ColumnContext(INTENSITY_COLUMN, INTENSITY_COLUMN, Double.class, false),
			new ColumnContext(ADDUCT_COLUMN, ADDUCT_COLUMN, Adduct.class, false),
			new ColumnContext(NUM_ION_COLUMN, "Ion number in series for the adduct", Integer.class, false)
		};
	}

	public void setTableModelFromMsFeature(MsFeature feature) {
		setTableModelFromSpectrum(feature.getSpectrum());
	}
	
	public void setTableModelFromSpectrum(MassSpectrum spectrum) {

		setRowCount(0);
		if(spectrum == null)
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		if(spectrum.getAdducts() != null && !spectrum.getAdducts().isEmpty()) {
			
			for(Adduct adduct : spectrum.getAdducts()) {

				int count = 1;
				for(MsPoint dp : spectrum.getMsForAdduct(adduct)) {

					Object[] obj = {
							dp.getMz(),
							dp.getIntensity(),
							adduct,
							count
					};
					rowData.add(obj);
					count++;
				}
			}
		}
		else {						
			int count = 1;
			for(MsPoint dp : spectrum.getCompletePattern()) {

				Object[] obj = {
						dp.getMz(),
						dp.getIntensity(),
						"",
						count
				};
				rowData.add(obj);
				count++;
			}			
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public void setTableModelFromScan(IScan scan) {
		
		setRowCount(0);
		int count = 1;
		Collection<MsPoint> points = RawDataUtils.getScanPoints(scan);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(MsPoint dp : points) {

			Object[] obj = {
					dp.getMz(),
					dp.getIntensity(),
					null,
					count
			};
			rowData.add(obj);
			count++;
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}























