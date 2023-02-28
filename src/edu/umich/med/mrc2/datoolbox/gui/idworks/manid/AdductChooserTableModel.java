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

package edu.umich.med.mrc2.datoolbox.gui.idworks.manid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AdductChooserTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5236650300446754423L;
	public static final String CHEM_MOD_TYPE_COLUMN = "Type";
	public static final String CHEM_MOD_COLUMN = "Name";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String NMER_COLUMN = "N-mer";
	public static final String MASS_CORRECTION_COLUMN = '\u0394' + " M/Z";

	public AdductChooserTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(CHEM_MOD_TYPE_COLUMN, ModificationType.class, false),	
			new ColumnContext(CHEM_MOD_COLUMN, Adduct.class, false),
			new ColumnContext(CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(NMER_COLUMN, Integer.class, false),
			new ColumnContext(MASS_CORRECTION_COLUMN, Double.class, false)
		};
	}

	public void setTableModelFromAdductList(Collection<Adduct> adducts) {

		setRowCount(0);
		if(adducts == null || adducts.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Adduct ad : adducts) {

			Object[] obj = {
				ad.getModificationType(),
				ad,
				ad.getCharge(),
				ad.getOligomericState(),
				ad.getMassCorrection()
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}
}















