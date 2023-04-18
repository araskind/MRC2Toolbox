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

package edu.umich.med.mrc2.datoolbox.gui.mstools.mfgen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.interfaces.IIsotope;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class ElementSelectionTableModel  extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5383587010503159608L;
	public static final String ENABLED_COLUMN = "Enabled";
	public static final String ELEMENT_COLUMN = "Element";
	public static final String MIN_COLUMN = "Min";
	public static final String MAX_COLUMN = "Max";

	public ElementSelectionTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ENABLED_COLUMN, Boolean.class, true),
			new ColumnContext(ELEMENT_COLUMN, IIsotope.class, false),
			new ColumnContext(MIN_COLUMN, Integer.class, true),
			new ColumnContext(MAX_COLUMN, Integer.class, true)
		};
		try {
			populateDefaultModel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void populateDefaultModel() throws IOException{

		IsotopeFactory ifac = Isotopes.getInstance();
		String[] elementSymbols = 
				new String[]{"H","C","N","S","P","O","Na","K","Ca", "Mg", "Cl","I"};

		List<Object[]>rowData = new ArrayList<Object[]>();
		for(String element : elementSymbols){

			 IIsotope elementObject = ifac.getMajorIsotope(element);
			 int min = 0, max = 0;
			 boolean enabled = false;

			 switch (element) {

	            case "H":  min = 0; max = 126; enabled = true;
	            	break;

	            case "C":  min = 0; max = 78; enabled = true;
            		break;

	            case "N":  min = 0; max = 20; enabled = true;
            		break;

	            case "S":  min = 0; max = 14; enabled = true;
            		break;

	            case "P":  min = 0; max = 9; enabled = true;
            		break;

	            case "O":  min = 0; max = 27; enabled = true;
            		break;

	            case "Na":  min = 0; max = 2; enabled = true;
            		break;

	            case "K":  min = 0; max = 2; enabled = true;
            		break;

	            case "Ca":  min = 0; max = 2; enabled = false;
            		break;

	            case "Mg":  min = 0; max = 2; enabled = false;
        			break;

	            case "Cl":  min = 0; max = 3; enabled = false;
            		break;

	            case "I":  min = 0; max = 3; enabled = false;
            		break;
			 }
			Object[] obj = {
					enabled,
					elementObject,
					min,
					max
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}



























