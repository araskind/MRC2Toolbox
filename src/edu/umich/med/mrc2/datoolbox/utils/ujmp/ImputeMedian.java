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

package edu.umich.med.mrc2.datoolbox.utils.ujmp;

import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.calculation.AbstractDoubleCalculation;
import org.ujmp.core.util.MathUtil;

public class ImputeMedian extends AbstractDoubleCalculation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Matrix median = null;

	public ImputeMedian(int dimension, Matrix matrix) {
		super(dimension, matrix);
	}
	
	@Override
	public double getDouble(long... coordinates) {

		if (median == null) {
			median = new Median(getDimension(), true, getSource()).calcNew();
		}
		double v = getSource().getAsDouble(coordinates);
		if (MathUtil.isNaNOrInfinite(v)) {
			switch (getDimension()) {
				case ALL:
					return median.getAsDouble(0, 0);
				case ROW:
					return median.getAsDouble(0, coordinates[COLUMN]);
				case COLUMN:
					return median.getAsDouble(coordinates[ROW], 0);
				default:
					return Double.NaN;
			}
		} else {
			return v;
		}
	}
}
