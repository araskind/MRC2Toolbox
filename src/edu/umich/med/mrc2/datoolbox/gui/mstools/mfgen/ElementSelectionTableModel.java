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
import java.util.Arrays;
import java.util.List;

import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.interfaces.IIsotope;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.isopat.IsotopicPatternUtils;

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
			new ColumnContext(ENABLED_COLUMN, ENABLED_COLUMN, Boolean.class, true),
			new ColumnContext(ELEMENT_COLUMN, ELEMENT_COLUMN, IIsotope.class, false),
			new ColumnContext(MIN_COLUMN, 
					"Minimal element atom count in generated formulas", Integer.class, true),
			new ColumnContext(MAX_COLUMN, 
					"Maximal element atom count in generated formulas", Integer.class, true)
		};
		try {
			populateDefaultModel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void populateDefaultModel() throws IOException{

		IsotopeFactory ifac = null;
		try {
			ifac = Isotopes.getInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(ifac == null)
			return;
			
		MolecularFormulaRange ranges =  
			 IsotopicPatternUtils.getDefaultElementRanges();
		List<String>elementSymbols = Arrays.asList(new String[]{
					"H","C","N","S","P","O","Na","K",
					"Ca", "Mg", "Cl","I"});				
		List<String>defaultDisabled = 
				Arrays.asList(new String[] {"Ca", "Mg", "Cl","I"});

		List<Object[]> rowData = new ArrayList<Object[]>();
		for (String element : elementSymbols) {

			IIsotope isotope = ifac.getMajorIsotope(element);
			int min = 0;
			int max = 0;
			if(ranges.contains(isotope)) {
				min = ranges.getIsotopeCountMin(isotope);
				max= ranges.getIsotopeCountMax(isotope);
				boolean enabled = !defaultDisabled.contains(element);
				Object[] obj = { enabled, isotope, min, max };
				rowData.add(obj);
			}			
		}
		//	Add defaults if not in the list
		for(IIsotope isotope : ranges.isotopes()) {
			
			if(!elementSymbols.contains(isotope.getSymbol())) {
				
				Object[] obj = { 
					true, 
					isotope, 
					ranges.getIsotopeCountMin(isotope), 
					ranges.getIsotopeCountMax(isotope),
				};
				rowData.add(obj);
			}
		}		
		if (!rowData.isEmpty())
			addRows(rowData);
	}
}



























