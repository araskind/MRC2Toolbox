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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class SubsetFeaturesTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 66715858498902337L;

	public static final String ORDER_COLUMN = "##";
	public static final String MS_FEATURE_COLUMN = "Name";
	public static final String RETENTION_COLUMN = "RT";
	public static final String BASE_PEAK_COLUMN = "Base peak";
	public static final String CHARGE_COLUMN = "Charge";

	public SubsetFeaturesTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ORDER_COLUMN, "Order number", Integer.class, false),
			new ColumnContext(MS_FEATURE_COLUMN, "Feature name", MsFeature.class, false),
			new ColumnContext(RETENTION_COLUMN, "Retention time", Double.class, false),
			new ColumnContext(BASE_PEAK_COLUMN, "Base peak M/Z", Double.class, false),
			new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false)
		};
	}

	public void setTableModelFromFeatureSet(MsFeatureSet featureSet) {
		setTableModelFromFeatures(featureSet.getFeatures());
	}
	
	public void setTableModelFromFeatures(Collection<MsFeature> features) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		int count = 1;
		for (MsFeature cf : features) {

			double bp = 0;
			int charge = 0;

			if (cf.getSpectrum() != null) {

				bp = cf.getMonoisotopicMz();
				charge = cf.getAbsoluteObservedCharge();
			}
			Object[] obj = {
				count,
				cf,
				cf.getRetentionTime(),
				bp,
				charge
			};
			rowData.add(obj);
			count++;
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
