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

package edu.umich.med.mrc2.datoolbox.misctest;

import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation.Ret;

public class CorrelationCalculator {

	private SparseMatrix dataMatrix;
	private Matrix corrMatrix, filteredCorrMatrix;

	public CorrelationCalculator(int[][] inputDataMatrix) {

		super();

		dataMatrix = Matrix.Factory.linkToArray(inputDataMatrix);

		filteredCorrMatrix = Matrix.Factory.zeros(dataMatrix.getColumnCount(), dataMatrix.getColumnCount());
	}

	public void calculatePearsonCorrelationMatrix() {

		corrMatrix = dataMatrix.corrcoef(Ret.LINK, false, false);
	}

	public void filterByCutoff(double cutoff) {

		long[] coordinates = new long[] { 0, 0 };
		double value;

		for (int i = 0; i < corrMatrix.getRowCount(); i++) {

			coordinates[0] = i;

			for (int j = i + 1; j < corrMatrix.getColumnCount(); j++) {

				coordinates[1] = j;

				value = corrMatrix.getAsDouble(coordinates);

				if (value > cutoff)
					filteredCorrMatrix.setAsDouble(value, coordinates);
			}
		}
	}
}
