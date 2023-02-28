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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AdductSelectionTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7598398458767530808L;
	public static final String ENABLED_COLUMN = "Active";
	public static final String CHEM_MOD_COLUMN = "Name";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String NMER_COLUMN = "N-mer";

	public AdductSelectionTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ENABLED_COLUMN, Boolean.class, true),
			new ColumnContext(CHEM_MOD_COLUMN, Adduct.class, false),
			new ColumnContext(CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(NMER_COLUMN, Integer.class, false)
		};
	}

	public void setTableModelFromAdductListAndFeature(
			Collection<Adduct> adducts, LibraryMsFeature feature) {

		setRowCount(0);
		if(adducts == null || adducts.isEmpty() || feature == null)
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Adduct ad : adducts) {

			boolean enabled = false;
			if(feature.getSpectrum()!= null) {

				if(feature.getSpectrum().getAdducts().contains(ad))
					enabled = true;
			}
			Object[] obj = {
				enabled,
				ad,
				ad.getCharge(),
				ad.getOligomericState()
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}
}















