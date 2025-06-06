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

import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.calculation.AbstractDoubleCalculation;
import org.ujmp.core.util.MathUtil;

public class Log1pTransform extends AbstractDoubleCalculation {

	private static final long serialVersionUID = 3879555534699629510L;
	
	private final boolean ignoreNaN;
	
	public Log1pTransform(boolean ignoreNaN, Matrix m1) {
		super(m1);
		this.ignoreNaN = ignoreNaN;
	}

	@Override
	public double getDouble(long... coordinates) {

		Double value = getSources()[0].getAsDouble(coordinates);
		if (MathUtil.isNaNOrInfinite(value)) {
			
			if(ignoreNaN)
				return Double.NaN;
			else
				return Math.log1p(0.0d);				
		}
		else	
			return Math.log1p(value);
	}

}
