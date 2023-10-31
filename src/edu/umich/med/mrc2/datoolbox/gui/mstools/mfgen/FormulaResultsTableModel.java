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

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMolecularFormulaSet;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class FormulaResultsTableModel extends BasicTableModel {


	/**
	 *
	 */
	private static final long serialVersionUID = 5299963957599425248L;

	public static final String FORMULA_COLUMN = "Formula";
	public static final String ADDUCT_MASS_COLUMN = "Adduct M/Z";
	public static final String FORMULA_MASS_COLUMN = "Formula mass";
	public static final String EXPECTED_NEUTRAL_MASS_COLUMN = "Expected neutral mass";
	public static final String PPM_ERROR_COLUMN = "Error, ppm";
	public static final String ABS_PPM_ERROR_COLUMN = "Abs. error, ppm";

	public FormulaResultsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(FORMULA_COLUMN, FORMULA_COLUMN, String.class, false),
			new ColumnContext(ADDUCT_MASS_COLUMN, ADDUCT_MASS_COLUMN, String.class, false),
			new ColumnContext(FORMULA_MASS_COLUMN, FORMULA_MASS_COLUMN, Double.class, false),
			new ColumnContext(EXPECTED_NEUTRAL_MASS_COLUMN, EXPECTED_NEUTRAL_MASS_COLUMN, Double.class, false),
			new ColumnContext(PPM_ERROR_COLUMN, 
					"Difference between observed and expected neutral mass, ppm", Double.class, false),
			new ColumnContext(ABS_PPM_ERROR_COLUMN, 
					"Absolute difference between observed and expected neutral mass, ppm", Double.class, false)
		};
	}

	public void setFromFormulaGeneratorResults(
			IMolecularFormulaSet formulas, double mz, Adduct ad) {

		setRowCount(0);
		if(formulas == null)
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for(IMolecularFormula mf : formulas.molecularFormulas()){

			if(mf.getIsotopeCount() > 0) {

				String formula = MolecularFormulaManipulator.getString(mf);
				double formulaMass = MolecularFormulaManipulator.getMass(
						mf, MolecularFormulaManipulator.MonoIsotopic);
				double neutralMass = MsUtils.calculateNeutralMass(mz, ad);
				double error = (neutralMass - formulaMass) / neutralMass * 1000000;
				Object[] obj = {
						formula,
						mz,
						formulaMass,
						neutralMass,
						error,
						Math.abs(error)
					};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
















