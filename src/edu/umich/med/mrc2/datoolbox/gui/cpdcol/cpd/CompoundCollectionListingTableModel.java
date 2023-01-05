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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.cpd;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class CompoundCollectionListingTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3707538261469467980L;

	public static final String ORDER_COLUMN = "##";
	public static final String ID_COLUMN = "ID";
	public static final String COMPOUND_NAME_COLUMN = "Name";
	public static final String FORMULA_COLUMN = "Formula";
	public static final String MASS_COLUMN = "Monoisotopic mass";
	public static final String SPECTRA_COLUMN = "Spectra";
	public static final String BIOLOCATION_COLUMN = "Bio location";

	public CompoundCollectionListingTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ORDER_COLUMN, Integer.class, false),
			new ColumnContext(ID_COLUMN, MsFeatureIdentity.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, String.class, false),
			new ColumnContext(FORMULA_COLUMN, String.class, false),
			new ColumnContext(MASS_COLUMN, Double.class, false),
			new ColumnContext(SPECTRA_COLUMN, Integer.class, false),
			new ColumnContext(BIOLOCATION_COLUMN, String.class, false)
		};
	}

	public void setTableModelFromCompoundCollection(Collection<CompoundIdentity> compoundCollection) {

		setRowCount(0);
		int counter = 1;

		//	TODO spectra count and bio-location
		for(CompoundIdentity id : compoundCollection){

			MsFeatureIdentity msid = new MsFeatureIdentity(id, CompoundIdentificationConfidence.ACCURATE_MASS);
			Object[] obj = {
					counter,
					msid,
					id.getCommonName(),
					id.getFormula(),
					id.getExactMass(),
					null,	//	TODO supply actual number
					""
				};
			super.addRow(obj);
			counter++;
		}
	}

	public void updateCidData(MsFeatureIdentity id) {

		int row = getIdentityRow(id);
		if(row == -1)
			return;

		setValueAt(id.getCompoundIdentity().getCommonName(), row, getColumnIndex(COMPOUND_NAME_COLUMN));
		setValueAt(id.getCompoundIdentity().getFormula(), row, getColumnIndex(FORMULA_COLUMN));
		setValueAt(id.getCompoundIdentity().getExactMass(), row, getColumnIndex(MASS_COLUMN));

		//	TODO other stuff - spectra etc.
	}

	public int getIdentityRow(MsFeatureIdentity id) {

		int col = getColumnIndex(ID_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (id.equals((MsFeatureIdentity)getValueAt(i, col)))
				return i;
		}
		return -1;
	}
}



































