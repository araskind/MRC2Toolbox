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

package edu.umich.med.mrc2.datoolbox.msmsscore.ip;

public class IsoPatternScoringObject {

	private IsotopeIntensityApproximationType approxType;
	private double[]coefficients;
	private String elementSet;
	private double weight;
	
	public IsoPatternScoringObject(
			IsotopeIntensityApproximationType approxType, 
			String elementSet, 
			double weight) {
		super();
		this.approxType = approxType;
		this.elementSet = elementSet;
		this.weight = weight;
		if(approxType.equals(IsotopeIntensityApproximationType.Linear)
				|| approxType.equals(IsotopeIntensityApproximationType.Exponential)){
			coefficients = new double[2];
		}
		if(approxType.equals(IsotopeIntensityApproximationType.Polynomial_2))
			coefficients = new double[3];
		
		if(approxType.equals(IsotopeIntensityApproximationType.Polynomial_3))
			coefficients = new double[4];
	}
	
	public void setCoefficient(double coeff, int position) {
		
		if(coefficients == null || coefficients.length < position + 1)
			return;
		else
			coefficients[position] = coeff;
	}

	public IsotopeIntensityApproximationType getApproxType() {
		return approxType;
	}

	public double[] getCoefficients() {
		return coefficients;
	}

	public String getElementSet() {
		return elementSet;
	}

	public double getWeight() {
		return weight;
	}
	
	public double getExpectedRelativeIntensityForMass(double mass) {
		
		double expectedRi = 0.0d;
		if(approxType.equals(IsotopeIntensityApproximationType.Linear))			
			expectedRi = coefficients[0] + coefficients[1] * mass;
		
		if(approxType.equals(IsotopeIntensityApproximationType.Polynomial_2))			
			expectedRi = coefficients[0] + coefficients[1] * mass+ coefficients[2] * Math.pow(mass, 2.0);
		
		if(approxType.equals(IsotopeIntensityApproximationType.Polynomial_3))			
			expectedRi = coefficients[0] + coefficients[1] * mass 
				+ coefficients[2] * Math.pow(mass, 2.0) + coefficients[3] * Math.pow(mass, 3.0);
		
		if(approxType.equals(IsotopeIntensityApproximationType.Exponential))
			expectedRi = coefficients[0] * Math.exp(coefficients[1] * mass);
		
		return expectedRi;
	}
	
	
}













