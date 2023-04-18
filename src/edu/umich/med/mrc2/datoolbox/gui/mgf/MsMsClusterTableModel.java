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

package edu.umich.med.mrc2.datoolbox.gui.mgf;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MsMsClusterTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 6263180722025274329L;

	public static final String PRIMARY_COLUMN = "Primary";
	public static final String INCLUDE_COLUMN = "Include";
	public static final String MSMS_COLUMN = "Name";
	public static final String PARENT_MASS_COLUMN = "Parent mass";
	public static final String ANNOTATION_COLUMN = "Retention";
	public static final String RETENTION_COLUMN = "Retention";
	public static final String TOP_INTENSITY_COLUMN = "Top intensity";
	public static final String TOTAL_INTENSITY_COLUMN = "Total intensity";
	public static final String NUM_PEAKS_COLUMN = "# of peaks";
	public static final String CHARGE_COLUMN = "Charge";

	private MsMsCluster currentCluster;

	public MsMsClusterTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(PRIMARY_COLUMN, Boolean.class, true),
			new ColumnContext(INCLUDE_COLUMN, Boolean.class, true),
			new ColumnContext(MSMS_COLUMN, SimpleMsMs.class, false),
			new ColumnContext(PARENT_MASS_COLUMN, Double.class, false),
			new ColumnContext(RETENTION_COLUMN, Double.class, false),
			new ColumnContext(TOP_INTENSITY_COLUMN, Double.class, false),
			new ColumnContext(TOTAL_INTENSITY_COLUMN, Double.class, false),
			new ColumnContext(NUM_PEAKS_COLUMN, Integer.class, false),
			new ColumnContext(CHARGE_COLUMN, Integer.class, false)
		};
	}

	public MsMsCluster getCurrentCluster() {
		return currentCluster;
	}

	public void reloadData() {
		setTableModelFromFeatureCluster(currentCluster);
	}

	public void setTableModelFromFeatureCluster(MsMsCluster featureCluster) {

		setRowCount(0);
		if(featureCluster == null 
				|| featureCluster.getClusterFeatures().isEmpty())
			return;

		currentCluster = featureCluster;
		SimpleMsMs topHit = currentCluster.getBestCandidate();
		boolean primary;
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (SimpleMsMs msms : featureCluster.getClusterFeatures()) {

			primary = false;
			if (msms.equals(topHit))
				primary = true;

			Object[] obj = {
				primary,
				true,
				msms,
				msms.getParentMass().getMz(),
				msms.getRetention(),
				msms.getCharge(),
				msms.getTopFragmentIntensity(),
				msms.getTotalFragmentIntensity(),
				msms.getDataPoints().length - 1
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}






















