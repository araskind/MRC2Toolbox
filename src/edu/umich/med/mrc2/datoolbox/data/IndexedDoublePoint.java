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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;

import org.apache.commons.math3.ml.clustering.DoublePoint;

public class IndexedDoublePoint extends DoublePoint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 564171434746528091L;
	private int[] coordinates;

	public IndexedDoublePoint(double[] point) {
		super(point);
	}

	public IndexedDoublePoint(double[] point, int[] coordinates) {

		super(point);
		this.coordinates = coordinates;
	}

	public int[] getCoordinates() {

		return coordinates;
	}
}
