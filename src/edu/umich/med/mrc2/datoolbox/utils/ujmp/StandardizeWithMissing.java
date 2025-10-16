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
import org.ujmp.core.doublematrix.calculation.general.misc.Center;
import org.ujmp.core.doublematrix.calculation.general.statistical.Std;

public class StandardizeWithMissing  extends AbstractDoubleCalculation {

	private static final long serialVersionUID = -1592533411876491290L;

	private Matrix center = null;
	private Matrix sigma = null;
	private boolean ignoreNaN;

	public StandardizeWithMissing(int dimension, Matrix matrix, boolean ignoreNaN) {
		super(dimension, matrix);
		this.ignoreNaN = ignoreNaN;
	}

	public double getDouble(long... coordinates) {
		if (center == null) {
			center = new Center(ignoreNaN, getDimension(), getSource()).calcNew();
		}
		if (sigma == null) {
			sigma = new Std(getDimension(), ignoreNaN, center, true).calcNew();
		}
		switch (getDimension()) {
		case ALL:
			return center.getAsDouble(coordinates) / sigma.getAsDouble(0, 0);
		case ROW:
			return center.getAsDouble(coordinates) / sigma.getAsDouble(0, coordinates[COLUMN]);
		case COLUMN:
			return center.getAsDouble(coordinates) / sigma.getAsDouble(coordinates[ROW], 0);
		}
		return Double.NaN;
	}
}