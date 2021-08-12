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

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class BinnerAnnotationsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3265763908033362302L;

	public static final String BINNER_ADDUCT_COLUMN = "Binner name";
	public static final String STD_NAME_COLUMN = "Tracker name";
	public static final String POLARITY_COLUMN = "Polarity";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String TIER_COLUMN = "Tier";
	public static final String MASS_CORRECTION_COLUMN = '\u0394' + " mass";

	public BinnerAnnotationsTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(BINNER_ADDUCT_COLUMN, BinnerAdduct.class, false),
			new ColumnContext(STD_NAME_COLUMN, String.class, false),
			new ColumnContext(POLARITY_COLUMN, String.class, false),
			new ColumnContext(CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(TIER_COLUMN, Integer.class, false),
			new ColumnContext(MASS_CORRECTION_COLUMN, Double.class, false),
		};
	}

	public void setTableModelFromBinnerAdductList(Collection<BinnerAdduct> collection) {

		setRowCount(0);
		for (BinnerAdduct ad : collection) {

			Object[] obj = {
				ad,
				ad.getName(),
				ad.getPolarity().name(),
				ad.getCharge(),
				ad.getTier(),
				ad.getMass(),
			};
			super.addRow(obj);
		}
	}
}
