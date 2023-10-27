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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.CompoundConcentration;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class ConcentrationsTableModel extends BasicTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1721115518921883088L;
	public static final String LOCATION_COLUMN = "Location";
	public static final String CONCENTRATION_COLUMN = "Concentration";
	public static final String UNITS_COLUMN = "Units";
	public static final String HEALTH_STATUS_COLUMN = "Health status";
	public static final String AGE_COLUMN = "Age";
	public static final String SEX_COLUMN = "Sex";
	public static final String FLAG_COLUMN = "Flag";
	public static final String TYPE_COLUMN = "Type";
	public static final String COMMENTS_COLUMN = "Comments";
	public static final String REFERENCE_COLUMN = "References";	
	
	public ConcentrationsTableModel() {
		super();
		columnArray = new ColumnContext[] {

				new ColumnContext(LOCATION_COLUMN, "Biolocation", String.class, false), 
				new ColumnContext(CONCENTRATION_COLUMN, CONCENTRATION_COLUMN, String.class, false), 
				new ColumnContext(UNITS_COLUMN, "Measurement units", String.class, false), 
				new ColumnContext(HEALTH_STATUS_COLUMN, HEALTH_STATUS_COLUMN, String.class, false), 	
				new ColumnContext(COMMENTS_COLUMN, COMMENTS_COLUMN, String.class, false), 
				new ColumnContext(AGE_COLUMN, AGE_COLUMN, String.class, false), 
				new ColumnContext(SEX_COLUMN, SEX_COLUMN, String.class, false), 
				new ColumnContext(FLAG_COLUMN, FLAG_COLUMN, String.class, false), 
				new ColumnContext(TYPE_COLUMN, TYPE_COLUMN, String.class, false), 			
		};
	}

	public void setModelFromConcentrations(Collection<CompoundConcentration>concentrations) {
		
		setRowCount(0);
		if(concentrations == null || concentrations.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(CompoundConcentration conc : concentrations) {
			
			Object[] obj = {
					conc.getBiofluid(),
					conc.getValue(),
					conc.getUnits(),
					conc.getSubjectCondition(),
					conc.getComments(),
					conc.getAge(),
					conc.getSex(),
					conc.getFlag(),
					conc.getType(),					
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}













