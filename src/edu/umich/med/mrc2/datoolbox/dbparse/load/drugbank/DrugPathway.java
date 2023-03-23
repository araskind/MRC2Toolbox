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

package edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank;

import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBPathway;

public class DrugPathway extends HMDBPathway {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9185843920546309795L;
	
	private String category;
	private Set<String> enzymes;
	private Set<String> drugs;
	
	public DrugPathway(
			String name, 
			String smpdbId) {
		super(name, smpdbId);
		enzymes = new TreeSet<String>();
		drugs = new TreeSet<String>();
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Set<String> getEnzymes() {
		return enzymes;
	}

	public Set<String> getDrugs() {
		return drugs;
	}
	
}
