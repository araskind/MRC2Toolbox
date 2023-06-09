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

package edu.umich.med.mrc2.datoolbox.dbparse.load.nist;

public enum NISTPeptideMSPField {
	
	NAME("Name"),
	MW("MW"),
	COMMENTS("Comment"),
	NUM_PEAKS("Num Peaks"),
	Charge("Charge"),
	DUScorr("DUScorr"),
	Dot_cons("Dot_cons"),
	Dotbest("Dotbest"),
	Dotfull("Dotfull"),
	Dottheory("Dottheory"),
	FTResolution("FTResolution"),
	Filter("Filter"),
	Flags("Flags"),
	Fullname("Fullname"),
	HCD("HCD"),
	Inst("Inst"),
	Max2med_orig("Max2med_orig"),
	Missing("Missing"),
	Mods("Mods"),
	Mz_av("Mz_av"),
	Mz_diff("Mz_diff"),
	Mz_exact("Mz_exact"),
	Naa("Naa"),
	Nrep("Nrep"),
	Nreps("Nreps"),
	Organism("Organism"),
	Origfile("Origfile"),
	Parent("Parent"),
	Parent_med("Parent_med"),
	Pep("Pep"),
	Pfin("Pfin"),
	Pfract("Pfract"),
	Precursor1MaxAb("Precursor1MaxAb"),
	PrecursorMonoisoMZ("PrecursorMonoisoMZ"),
	Probcorr("Probcorr"),
	Protein("Protein"),
	Pseq("Pseq"),
	Purity("Purity"),
	Sample("Sample"),
	Scan("Scan"),
	Se("Se"),
	Spec("Spec"),
	Tfratio("Tfratio"),
	Unassign_all("Unassign_all"),
	Unassigned("Unassigned"),
	ms1PrecursorAb("ms1PrecursorAb"),
	ms2IsolationWidth("ms2IsolationWidth"),
	;

	private final String name;

	NISTPeptideMSPField(String field) {
		this.name = field;
	}

	public String getName() {
		return name;
	}
}
