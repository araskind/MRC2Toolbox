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

package edu.umich.med.mrc2.datoolbox.data;

import jsat.classifiers.DataPoint;
import jsat.linear.DenseVector;

public class MsFeatureDataPoint extends DataPoint {

	private static final long serialVersionUID = -3062257632031978014L;
	
	private MsFeature msFeature;

	public MsFeatureDataPoint(double[] numericalValues, MsFeature msFeature) {
		super(new DenseVector(numericalValues));
		this.msFeature = msFeature;
	}

	public MsFeature getMsFeature() {
		return msFeature;
	}	
}
