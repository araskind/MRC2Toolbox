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

public class IsotopicPatternReferenceBin {
	
	private String formula;
	private int numberOfRepeats;
	private int numberOfCarbons;
	private double exactMass;
	private boolean isClAdduct;
	private Double[]isotopeRelativeIntensities;
	
	public IsotopicPatternReferenceBin(
			String formula, 
			int numberOfRepeats, 
			int numberOfCarbons,
			double exactMass) {
		super();
		this.formula = formula;
		this.numberOfRepeats = numberOfRepeats;
		this.numberOfCarbons = numberOfCarbons;
		this.exactMass = exactMass;
		isotopeRelativeIntensities  = new Double[5];
	}

	public String getFormula() {
		return formula;
	}

	public int getNumberOfRepeats() {
		return numberOfRepeats;
	}

	public int getNumberOfCarbons() {
		return numberOfCarbons;
	}

	public boolean isClAdduct() {
		return isClAdduct;
	}

	public void setClAdduct(boolean isClAdduct) {
		this.isClAdduct = isClAdduct;
	}

	public Double[] getIsotopeRelativeIntensities() {
		return isotopeRelativeIntensities;
	}

	public double getExactMass() {
		return exactMass;
	}
}
