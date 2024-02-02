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

package edu.umich.med.mrc2.datoolbox.data.enums;

import org.ujmp.core.doublematrix.calculation.general.missingvalues.Impute.ImputationMethod;

public enum DataImputationType {

	ColumnMean(ImputationMethod.ColumnMean,  "Mean value"),
	Regression(ImputationMethod.Regression,  "Regression"),
	KNN(ImputationMethod.KNN,  "K nearest neighbors (KNN)"),
	//	EM(ImputationMethod.EM,  "Expectation maximization (EM)"),
	BPCA(ImputationMethod.BPCA,  "Bayesian principal component analysis (BPCA)"),
	Zero(ImputationMethod.Zero, "Zero value");

	private final String uiName;
	private final ImputationMethod method;

	DataImputationType( ImputationMethod method, String uiName) {
		
		this.method = method;
		this.uiName = uiName;
	}

	public ImputationMethod getMethod() {
		return method;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}	
	
	public static DataImputationType getOptionByUIName(String name) {
		
		for(DataImputationType s : DataImputationType.values()) {
			
			if(s.getName().equals(name))
				return s;
		}		
		return null;
	}
	
	public static DataImputationType getOptionByName(String optionName) {

		for(DataImputationType o : DataImputationType.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
}
