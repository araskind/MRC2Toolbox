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

package edu.umich.med.mrc2.datoolbox.gui.io.mwtab;

public enum MWTabCompoundAnnotationFields {

	metabolite_name("metabolite_name"),
	moverz_quant("moverz_quant"),
	RI("ri"),
	ri_type("ri_type"),
	pubchem_id("pubchem_id"),
	inchi_key("inchi_key"),
	kegg_id("kegg_id"),
	other_id("other_id"),
	other_id_type("other_id_type"),
	id_confidence("id_confidence"),
	;

	private final String name;

	MWTabCompoundAnnotationFields(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}
}
