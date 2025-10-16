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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.BinnerAdduct;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class SimpleBinnerAnnotationsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3265763908033362302L;
	
	public static final String ORDER_COLUMN = "##";
	public static final String BINNER_ADDUCT_COLUMN = "Binner name";
	public static final String STD_NAME_COLUMN = "Tracker name";
	public static final String POLARITY_COLUMN = "Polarity";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String TIER_COLUMN = "Tier";
	public static final String MASS_CORRECTION_COLUMN = '\u0394' + " mass";
	
	private boolean showTier;

	public SimpleBinnerAnnotationsTableModel(boolean showTier) {
		
		super();
		this.showTier = showTier;
		
		if(showTier) {
			columnArray = new ColumnContext[] {
					new ColumnContext(ORDER_COLUMN, ORDER_COLUMN, Integer.class, false),
					new ColumnContext(BINNER_ADDUCT_COLUMN, BINNER_ADDUCT_COLUMN, BinnerAdduct.class, false),
					new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),
					new ColumnContext(MASS_CORRECTION_COLUMN, "Mass difference", Double.class, false),
					new ColumnContext(TIER_COLUMN, TIER_COLUMN, Integer.class, true),			
				};
		}
		else {
			columnArray = new ColumnContext[] {
					new ColumnContext(ORDER_COLUMN, ORDER_COLUMN, Integer.class, false),
					new ColumnContext(BINNER_ADDUCT_COLUMN, BINNER_ADDUCT_COLUMN, BinnerAdduct.class, false),
					new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),
					new ColumnContext(MASS_CORRECTION_COLUMN, "Mass difference", Double.class, false),		
				};
		}
	}

	public void setTableModelFromBinnerAdductList(Collection<BinnerAdduct> collection) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		int counter = 1;
		if(showTier) {
			
			for (BinnerAdduct ad : collection) {

				Object[] obj = {
					counter,
					ad,
					ad.getCharge(),				
					ad.getMass(),
					ad.getTier(),
				};
				rowData.add(obj);
				counter++;
			}
		}
		else {
			for (BinnerAdduct ad : collection) {

				Object[] obj = {
					counter,
					ad,
					ad.getCharge(),
					ad.getMass(),
				};
				rowData.add(obj);
				counter++;
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void setTableModelFromBinnerAdductTierMap(Map<BinnerAdduct, Integer> tierMap) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		int counter = 1;
		if(showTier) {
			
			for (Entry<BinnerAdduct,Integer> entry : tierMap.entrySet()) {

				BinnerAdduct ad = entry.getKey();
				Object[] obj = {
					counter,
					ad,
					ad.getCharge(),					
					ad.getMass(),
					entry.getValue(),
				};				
				rowData.add(obj);
				counter++;
			}
		}
		else {
			for (Entry<BinnerAdduct,Integer> entry : tierMap.entrySet()) {

				BinnerAdduct ad = entry.getKey();
				Object[] obj = {
					counter,
					ad,
					ad.getCharge(),
					ad.getMass(),
				};
				rowData.add(obj);
				counter++;
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
