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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;

public class LIMSAcquisitionDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 13350866688921077L;
	private String polarity;
	private String methodName;
	private String ionization_Type;
	private String massAnalyzer;
	private String msType;
	private String columnName;
	private String columnChemistry;
	private String instrument;

	public LIMSAcquisitionDetails(
			String polarity,
			String methodName,
			String ionization_Type,
			String massAnalyzer,
			String msType,
			String columnName,
			String columnChemistry,
			String instrument) {
		super();
		this.polarity = polarity;
		this.methodName = methodName;
		this.ionization_Type = ionization_Type;
		this.massAnalyzer = massAnalyzer;
		this.msType = msType;
		this.columnName = columnName;
		this.columnChemistry = columnChemistry;
		this.instrument = instrument;
	}

	/**
	 * @return the polarity
	 */
	public String getPolarity() {
		return polarity;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the ionization_Type
	 */
	public String getIonization_Type() {
		return ionization_Type;
	}

	/**
	 * @return the massAnalyzer
	 */
	public String getMassAnalyzer() {
		return massAnalyzer;
	}

	/**
	 * @return the msType
	 */
	public String getMsType() {
		return msType;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the columnChemistry
	 */
	public String getColumnChemistry() {
		return columnChemistry;
	}

	/**
	 * @return the instrument
	 */
	public String getInstrument() {
		return instrument;
	}
}
