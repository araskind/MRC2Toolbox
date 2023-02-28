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

package edu.umich.med.mrc2.datoolbox.gui.adducts.exchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AdductExchangeTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 4704928135582839516L;

	public static final String ADDUCT_ONE_COLUMN = "Leaving adduct";
	public static final String ADDUCT_TWO_COLUMN = "Coming adduct";
	public static final String MASS_DIFFERENCE_COLUMN = "Mass difference";

	public AdductExchangeTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(ADDUCT_ONE_COLUMN, Adduct.class, false),
			new ColumnContext(ADDUCT_TWO_COLUMN, Adduct.class, false),
			new ColumnContext(MASS_DIFFERENCE_COLUMN, AdductExchange.class, false)
		};
	}

	public void setTableModelFromAdductExchangeList(Collection<AdductExchange> list) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();				
		for (AdductExchange ex : list) {

			Object[] obj = { 
					ex.getLeavingAdduct(), 
					ex.getComingAdduct(), 
					ex };
			rowData.add(obj);
		}
		addRows(rowData);
	}
}
