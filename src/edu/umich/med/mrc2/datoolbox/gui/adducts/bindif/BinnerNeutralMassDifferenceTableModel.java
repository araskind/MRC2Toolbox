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

package edu.umich.med.mrc2.datoolbox.gui.adducts.bindif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class BinnerNeutralMassDifferenceTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 4704928135582839516L;

	public static final String NAME_COLUMN = "Name";
	public static final String NORMALIZED_NAME_COLUMN = "Normalized name";	
	public static final String MASS_DIFFERENCE_COLUMN = '\u0394' + " mass";

	public BinnerNeutralMassDifferenceTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(NAME_COLUMN, NAME_COLUMN, BinnerNeutralMassDifference.class, false),
			new ColumnContext(NORMALIZED_NAME_COLUMN, NORMALIZED_NAME_COLUMN, String.class, false),
			new ColumnContext(MASS_DIFFERENCE_COLUMN, "Mass difference", Double.class, false)
		};
	}

	public void setTableModelFromBinnerNeutralMassDifferenceList(
			Collection<BinnerNeutralMassDifference> list) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
			
		for (BinnerNeutralMassDifference mDif : list) {

			Object[] obj = { 
				mDif, 
				mDif.getName(),
				mDif.getMassCorrection(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
