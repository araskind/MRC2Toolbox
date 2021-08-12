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

package edu.umich.med.mrc2.datoolbox.utils.filter;

public enum SavitzkyGolayWidth {

	/**
	 * Fixed definition of the number of data points to use for smoothing. The
	 * range is restricted because the algorithm utilizes pre-calculated coefficients.
	 */

	FIVE(5), 
	SEVEN(7), 
	NINE(9),
	ELEVEN(11), 
	THIRTEEN(13), 
	FIFTEEN(15), 
	SEVENTEEN(17), 
	NINETEEN(19),
	TWENTYONE(21), 
	TWENTYTHREE(23), 
	TWENTYFIVE(25);
	
	private final int width;
	
	SavitzkyGolayWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}
	
	public String toString() {
		return Integer.toString(width);
	}
}
