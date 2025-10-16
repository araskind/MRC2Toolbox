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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class CuratedDatabaseCompoundTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3707538261469467980L;

	public static final String ORDER_COLUMN = "##";
	public static final String CURATED_COLUMN = "Curated";
	public static final String ID_COLUMN = "ID";
	public static final String COMPOUND_NAME_COLUMN = "Name";
	public static final String FORMULA_COLUMN = "Formula";
	public static final String MASS_COLUMN = "Monoisotopic mass";
	public static final String SPECTRA_COLUMN = "Spectra";
	public static final String BIOLOCATION_COLUMN = "Bio location";

	public CuratedDatabaseCompoundTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ORDER_COLUMN, "Order", Integer.class, false),
			new ColumnContext(CURATED_COLUMN, CURATED_COLUMN, Boolean.class, false),
			new ColumnContext(ID_COLUMN, 
					"Primary database accession and web link to the source database", MsFeatureIdentity.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN,  "Compound name", String.class, false),
			new ColumnContext(FORMULA_COLUMN, FORMULA_COLUMN, String.class, false),
			new ColumnContext(MASS_COLUMN, "Monoisotopic mass", Double.class, false),
			new ColumnContext(SPECTRA_COLUMN, "Number of spectra in the database",Integer.class, false),
			new ColumnContext(BIOLOCATION_COLUMN, BIOLOCATION_COLUMN, String.class, false)
		};
	}

	public void setTableModelFromCompoundCollection(
			Map<CompoundIdentity,Boolean> compoundCollection) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		int counter = 1;

		//	TODO spectra count and bio-location
		for(Entry<CompoundIdentity,Boolean> idEntry : compoundCollection.entrySet()){

			CompoundIdentity id = idEntry.getKey();
			MsFeatureIdentity msid = 
					new MsFeatureIdentity(id, 
							CompoundIdentificationConfidence.ACCURATE_MASS);
			Object[] obj = {
					counter,
					idEntry.getValue(),
					msid,
					id.getCommonName(),
					id.getFormula(),
					id.getExactMass(),
					null,	//	TODO supply actual number
					""
				};
			rowData.add(obj);
			counter++;
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void updateCidData(CompoundIdentity cid, boolean isCurated) {

		int row = getCompoundIdentityRow(cid);
		if(row == -1)
			return;

		setValueAt(cid.getCommonName(), 
				row, getColumnIndex(COMPOUND_NAME_COLUMN));
		setValueAt(cid.getFormula(), 
				row, getColumnIndex(FORMULA_COLUMN));
		setValueAt(cid.getExactMass(), 
				row, getColumnIndex(MASS_COLUMN));
		setValueAt(isCurated, 
				row, getColumnIndex(CURATED_COLUMN));
		//	TODO other stuff - spectra etc.
	}
	
	public void updateMSFidData(MsFeatureIdentity id, boolean isCurated) {

		int row = getIdentityRow(id);
		if(row == -1)
			return;

		CompoundIdentity cid = id.getCompoundIdentity();
		setValueAt(cid.getCommonName(), 
				row, getColumnIndex(COMPOUND_NAME_COLUMN));
		setValueAt(cid.getFormula(), 
				row, getColumnIndex(FORMULA_COLUMN));
		setValueAt(cid.getExactMass(), 
				row, getColumnIndex(MASS_COLUMN));
		setValueAt(isCurated, 
				row, getColumnIndex(CURATED_COLUMN));
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
	
	public int getCompoundIdentityRow(CompoundIdentity id) {

		int col = getColumnIndex(ID_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (id.equals(((MsFeatureIdentity)getValueAt(i, col)).getCompoundIdentity()))
				return i;
		}
		return -1;
	}
}



































