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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AdductTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3265763908033362302L;

	public static final String TYPE_COLUMN = "Type";
	public static final String CHEM_MOD_COLUMN = "Name";
	public static final String CEF_NOTATION_COLUMN = "CEF notation";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String OLIGOMER_COLUMN = "Oligomer";
	public static final String ADDED_GROUP_COLUMN = "Added";
	public static final String REMOVED_GROUP_COLUMN = "Removed";
	public static final String MASS_CORRECTION_COLUMN = '\u0394' + " mass";
	public static final String MASS_CORRECTION_ABS_COLUMN = '\u0394' + " mass abs.";

	public AdductTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(TYPE_COLUMN, String.class, false),
			new ColumnContext(CHEM_MOD_COLUMN, Adduct.class, false),
			new ColumnContext(CEF_NOTATION_COLUMN, String.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(OLIGOMER_COLUMN, Integer.class, false),
			new ColumnContext(ADDED_GROUP_COLUMN, String.class, false),
			new ColumnContext(REMOVED_GROUP_COLUMN, String.class, false),
			new ColumnContext(MASS_CORRECTION_COLUMN, Double.class, false),
			new ColumnContext(MASS_CORRECTION_ABS_COLUMN, Double.class, false)
		};
	}

	public void setTableModelFromAdductList(Collection<Adduct> collection) {

		setRowCount(0);
		for (Adduct ad : collection) {

			Object[] obj = {

				ad.getModificationType().getName(),
				ad,
				ad.getCefNotation(),
				ad.getDescription(),
				ad.getCharge(),
				ad.getOligomericState(),
				ad.getAddedGroup(),
				ad.getRemovedGroup(),
				ad.getMassCorrection(),
				(double) Math.abs(ad.getMassCorrection())
			};
			super.addRow(obj);
		}
	}
}
