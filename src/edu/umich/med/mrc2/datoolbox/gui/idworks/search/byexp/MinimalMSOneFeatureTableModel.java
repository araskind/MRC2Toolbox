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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MinimalMSOneFeatureTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7387427636234648515L;

	public static final String FEATURE_COLUMN = "Name";
	public static final String MZ_COLUMN = "M/Z";
	public static final String RT_COLUMN = "RT";
	public static final String RANK_COLUMN = "Rank";
	public static final String FOLD_CHANGE_COLUMN = "Fold change";
	public static final String P_VALUE_COLUMN = "P-value";
	public static final String SMILES_COLUMN = "SMILES";
	public static final String INCHI_KEY_COLUMN = "InChi key";

	public MinimalMSOneFeatureTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(FEATURE_COLUMN, "Feature name", MinimalMSOneFeature.class, false),
			new ColumnContext(MZ_COLUMN, "Monoisotopic M/Z", Double.class, false),
			new ColumnContext(RT_COLUMN, "Retention time", Double.class, false),
			new ColumnContext(RANK_COLUMN, RANK_COLUMN, Double.class, false),
			new ColumnContext(FOLD_CHANGE_COLUMN, FOLD_CHANGE_COLUMN, Double.class, false),
			new ColumnContext(P_VALUE_COLUMN, P_VALUE_COLUMN, Double.class, false),
			new ColumnContext(SMILES_COLUMN, SMILES_COLUMN, String.class, false),
			new ColumnContext(INCHI_KEY_COLUMN, INCHI_KEY_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromFeatureCollection(
			Collection<MinimalMSOneFeature>features) {

		setRowCount(0);
		if(features == null || features.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MinimalMSOneFeature feature : features) {

			Object[] obj = {
					feature,
					feature.getMz(),
					feature.getRt(),
					feature.getRank(),
					feature.getFoldChange(),
					feature.getpValue(),
					feature.getSmiles(),
					feature.getInchiKey()
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}















