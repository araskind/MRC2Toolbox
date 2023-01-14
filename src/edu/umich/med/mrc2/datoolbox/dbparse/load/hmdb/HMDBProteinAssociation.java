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

package edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb;

public class HMDBProteinAssociation {

	private String proteinAccession;
	private String name;
	private String uniprot;
	private String geneName;
	private String proteinType;

	public HMDBProteinAssociation(String proteinAccession, String name, String uniprot, String geneName,
			String proteinType) {
		super();
		this.proteinAccession = proteinAccession;
		this.name = name;
		this.uniprot = uniprot;
		this.geneName = geneName;
		this.proteinType = proteinType;
	}

	/**
	 * @return the proteinAccession
	 */
	public String getProteinAccession() {
		return proteinAccession;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the uniprot
	 */
	public String getUniprot() {
		return uniprot;
	}

	/**
	 * @return the geneName
	 */
	public String getGeneName() {
		return geneName;
	}

	/**
	 * @return the proteinType
	 */
	public String getProteinType() {
		return proteinType;
	}
}
