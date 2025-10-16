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
import java.sql.ResultSet;
import java.sql.SQLException;

public class LipidBlastClass  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1852697541496858990L;
	private String molFormula;
	private String classCode;
	private String lipidMapsClass;
	private boolean isPrimary;

	public LipidBlastClass(ResultSet rs) throws SQLException {

		this.classCode = rs.getString(1);
		this.molFormula = rs.getString(2);
		this.lipidMapsClass = rs.getString(3);

		isPrimary = false;
	}

	public LipidBlastClass(String molFormula, String classCode, String lipidMapsClass, boolean isPrimary) {
		super();
		this.molFormula = molFormula;
		this.classCode = classCode;
		this.lipidMapsClass = lipidMapsClass;
		this.isPrimary = isPrimary;
	}

	public String getClassCode() {
		return classCode;
	}

	public String getLipidMapsClass() {
		return lipidMapsClass;
	}

	public String getMolFormula() {
		return molFormula;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setClassCode(String classCode) {
		this.classCode = classCode;
	}

	public void setLipidMapsClass(String lipidMapsClass) {
		this.lipidMapsClass = lipidMapsClass;
	}

	public void setMolFormula(String molFormula) {
		this.molFormula = molFormula;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
}
