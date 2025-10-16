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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.enums.AdductNotationType;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AdductTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3265763908033362302L;

	public static final String TYPE_COLUMN = "Type";
	public static final String CHEM_MOD_COLUMN = "Name";	
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String OLIGOMER_COLUMN = "Oligomer";
	public static final String ADDED_GROUP_COLUMN = "Added";
	public static final String REMOVED_GROUP_COLUMN = "Removed";
	public static final String MASS_CORRECTION_COLUMN = '\u0394' + " mass";
	public static final String MASS_CORRECTION_ABS_COLUMN = '\u0394' + " mass abs.";
	public static final String CEF_NOTATION_COLUMN = "CEF notation";
	public static final String BINNER_NOTATION_COLUMN = "Binner notation";
	public static final String SIRIUS_NOTATION_COLUMN = "Sirius notation";

	public AdductTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(TYPE_COLUMN, "Modification type", String.class, false),
			new ColumnContext(CHEM_MOD_COLUMN, CHEM_MOD_COLUMN, Adduct.class, false),			
			new ColumnContext(DESCRIPTION_COLUMN, DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(OLIGOMER_COLUMN, "Oligomeric state", Integer.class, false),
			new ColumnContext(ADDED_GROUP_COLUMN, "Added group formula", String.class, false),
			new ColumnContext(REMOVED_GROUP_COLUMN, "Removed group formula", String.class, false),
			new ColumnContext(MASS_CORRECTION_COLUMN, "Mass correction", Double.class, false),
			new ColumnContext(MASS_CORRECTION_ABS_COLUMN, "Absolute mass correction", Double.class, false),			
			new ColumnContext(CEF_NOTATION_COLUMN, "Agilent (CEF file) notation", String.class, false),
			new ColumnContext(BINNER_NOTATION_COLUMN, BINNER_NOTATION_COLUMN, String.class, false),
			new ColumnContext(SIRIUS_NOTATION_COLUMN, SIRIUS_NOTATION_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromAdductList(Collection<Adduct> collection) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Adduct ad : collection) {

			Object[] obj = {

				ad.getModificationType().getName(),
				ad,				
				ad.getDescription(),
				ad.getCharge(),
				ad.getOligomericState(),
				ad.getAddedGroup(),
				ad.getRemovedGroup(),
				ad.getMassCorrection(),
				(double) Math.abs(ad.getMassCorrection()),
				ad.getNotationForType(AdductNotationType.CEF),
				ad.getNotationForType(AdductNotationType.BINNER),
				ad.getNotationForType(AdductNotationType.SIRIUS)
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
