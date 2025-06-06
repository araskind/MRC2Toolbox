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

package edu.umich.med.mrc2.datoolbox.utils.ujmp;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.calculation.AbstractDoubleCalculation;
import org.ujmp.core.mapmatrix.DefaultMapMatrix;
import org.ujmp.core.mapmatrix.MapMatrix;
import org.ujmp.core.util.MathUtil;

public class Median extends AbstractDoubleCalculation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean ignoreNaN = false;
	private DescriptiveStatistics descStats;

	public Median(int dimension, boolean ignoreNaN, Matrix matrix) {
		super(dimension, matrix);
		this.ignoreNaN = ignoreNaN;
		MapMatrix<String, Object> aold = matrix.getMetaData();
		if (aold != null) {
			MapMatrix<String, Object> a = new DefaultMapMatrix<String, Object>();
			a.put(Matrix.LABEL, aold.get(Matrix.LABEL));
			setMetaData(a);
		}
		descStats = new DescriptiveStatistics();
	}
	
	@Override
	public double getDouble(long... coordinates) {

		if(getDimension() == ALL)
			return getMatrixMedian(getSource());
		else
			return getMedianForDimension(getDimension(),coordinates);
	}
	
	public double getMedianForDimension(int dimension, long... coordinates) {
		
		descStats.clear();
		switch (dimension) {
		
			case ROW:
				for (long r = getSource().getSize()[ROW] - 1; r != -1; r--) {
					Double value = getSource().getAsDouble(r, coordinates[COLUMN]);
					if (ignoreNaN && MathUtil.isNaNOrInfinite(value))
						continue;
					
					descStats.addValue(value);
				}
				return descStats.getPercentile(50.0d);
				
			case COLUMN:
				for (long c = getSource().getSize()[COLUMN] - 1; c != -1; c--) {
					
					Double value = getSource().getAsDouble(coordinates[ROW], c);
					if (ignoreNaN && MathUtil.isNaNOrInfinite(value))
						continue;
					
					descStats.addValue(value);
				}
				return descStats.getPercentile(50.0d);
			default:
				return Double.NaN;
		}			
	}
	
	public double getMatrixMedian(Matrix m) {
		
		descStats.clear();
		for (long[] c : m.availableCoordinates()) {
			
			Double value = m.getAsDouble(c);
			if (ignoreNaN && MathUtil.isNaNOrInfinite(value))
				continue;
			
			descStats.addValue(value);
		}		
		return descStats.getPercentile(50.0d);
	}
	
	public static double calc(Matrix m) {
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (long[] c : m.availableCoordinates()) 
			stats.addValue(m.getAsDouble(c));
		
		return stats.getPercentile(50.0d);
	}
}
