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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.ctab;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityCluster;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.ChemInfoUtils;

public class CompoundIdentityClusterTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8274181230602392185L;
	
	public static final String PRIMARY_COLUMN = "Default";
	public static final String COMPOUND_NAME_COLUMN = "Name";
	public static final String DATABASE_COLUMN = "Database";
	public static final String ID_COLUMN = "DB ID";
	public static final String FORMULA_COLUMN = "Formula";
	public static final String MASS_COLUMN = "Monoisotopic mass";
	public static final String CHARGE_COLUMN = "Charge";


	
	public CompoundIdentityClusterTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(PRIMARY_COLUMN, Boolean.class, true),
			new ColumnContext(COMPOUND_NAME_COLUMN, String.class, false),
			new ColumnContext(DATABASE_COLUMN, String.class, false),
			new ColumnContext(ID_COLUMN, CompoundIdentity.class, false),			
			new ColumnContext(FORMULA_COLUMN, String.class, false),
			new ColumnContext(MASS_COLUMN, Double.class, false),
			new ColumnContext(CHARGE_COLUMN, String.class, false)
		};
	}

	public void setModelFromCompoundIdentityCluster(CompoundIdentityCluster cluster) {
		
		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(CompoundIdentity id : cluster.getIdList()) {
			
			Object[] obj = {
					id.equals(cluster.getPrimaryIdentity()),
					id.getCommonName(),
					id.getPrimaryDatabase().getName(),					
					id,
					id.getFormula(),
					id.getExactMass(),
					ChemInfoUtils.getChargeFromInChiKey(id.getInChiKey()),
				};
			rowData.add(obj);
		}
		addRows(rowData);
	}
}
