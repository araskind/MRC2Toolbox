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

package edu.umich.med.mrc2.datoolbox.renjin;

import java.util.ArrayList;

import org.renjin.sexp.Null;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

public class RenjinUtils {

	public static Matrix convertVectorToDoubleMatrix(Vector vector) {

		Vector dim = (Vector) vector.getAttribute(Symbols.DIM);

		if (dim.length() != 2)
			throw new IllegalArgumentException("vector is not a matrix");

		//	Fill data matrix
		int nrows = dim.getElementAsInt(0);
		int ncols = dim.getElementAsInt(1);
		double[][] data = new double[nrows][ncols];

		for (int i = 0; i < vector.length(); i++) {

			int row = i % nrows;
			int col = ((i - row) / nrows) % ncols;
			data[row][col] = vector.getElementAsDouble(i);
		}
		Matrix dataMatrix = Matrix.Factory.linkToArray(data);

		//	Add row and column names
		String[] rowNames = getRowNames(vector);
		String[] colNames = getColNames(vector);

		if(rowNames.length > 0)
			dataMatrix.setMetaDataDimensionMatrix(1, Matrix.Factory.linkToArray(rowNames).transpose(Ret.NEW));

		if(colNames.length > 0)
			dataMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray(colNames));

		return dataMatrix;
	}

	public static Vector getDimNames(Vector vector, int dimensionIndex) {

		Vector dimNames = (Vector) vector.getAttribute(Symbols.DIMNAMES);
		if (dimNames.length() != 2) {
			return Null.INSTANCE;
		} else {
			return (Vector) dimNames.getElementAsSEXP(dimensionIndex);
		}
	}

	public static String[] getRowNames(Vector vector) {

		ArrayList<String>rowNames = new ArrayList<String>();
		Vector rowNamesVector = getDimNames(vector, 0);

		if(!rowNamesVector.equals(Null.INSTANCE)) {

			for(int i=0; i<rowNamesVector.length(); i++)
				rowNames.add(rowNamesVector.getElementAsString(i));
		}
		return rowNames.toArray(new String[rowNames.size()]);
	}

	public static String[] getColNames(Vector vector) {

		ArrayList<String>colNames = new ArrayList<String>();
		Vector colNamesVector = getDimNames(vector, 1);

		if(!colNamesVector.equals(Null.INSTANCE)) {

			for(int i=0; i<colNamesVector.length(); i++)
				colNames.add(colNamesVector.getElementAsString(i));
		}
		return colNames.toArray(new String[colNames.size()]);
	}
}
