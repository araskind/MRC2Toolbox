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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum CompoundAnnotationField {

	NAME("Compound"),
	SPECTRUM("CompositeSpectrum"),
	MS1_SPECTRUM("MS1 Composite Spectrum"),
	RETENTION("Retention Time"),
	RT("RT"),
	CAS("CAS Number"),
	CAS_ID("CAS ID"),
	FREQUENCY("Frequency"),
	FORMULA("Formula"),
	MASS("Mass"),
	ALIGNMENT_VALUE("Alignment Value"),
	ANNOTATIONS("Annotations"),
	CHEBI("ChEBI ID"),
	FEATURE_NAME("Feature Name"),
	COMPOUND_NAME("Compound Name"),
	COMPOUND_ID_NAME("ID name"),
	ID_SOURCE("ID source"),
	COMPOUNDALGO("CompoundAlgo"),
	HMP("HMP ID"),
	IONIZATION("Ionization mode"),
	KEGG("KEGG ID"),
	LIPIDMAPS("LMP ID"),
	METLIN("METLIN ID"),
	NCBI_GI("NCBI gi"),
	PUBCHEM("PubChem ID"),
	SWISS_PROT("Swiss-Prot ID"),
	ADDUCT_TYPE("Adduct type"),
	MONOISOTOPIC_NEUTRAL_MASS("Exact mass"),
	OBSERVED_MZ("Observed mz"),
	INCHI_KEY("InChI key"),
	SOURCE_DB("ID source"),
	DB_ID("Database ID"),
	ASSAY("Assay"),
	POOLED_MEAN("Pooled mean"),
	POOLED_MEDIAN("Pooled median"),
	POOLED_RSD("Pooled %RSD"),
	POOLED_FREQUENCY("Pooled frequency"),
	SAMPLE_MEAN("Sample mean"),
	SAMPLE_MEDIAN("Sample median"),
	SAMPLE_RSD("Sample %RSD"),
	SAMPLE_FREQUENCY("Sample frequency"),
	ID_CONFIDENCE("ID confidence"),
	RT_EXPECTED("RT expected"),
	RT_OBSERVED("RT observed"),
	RT_DELTA("Delta RT"),
	MZ_ERROR_PPM("M/Z error, ppm")
	;

	private final String uiName;

	CompoundAnnotationField(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
	
	public static CompoundAnnotationField getOptionByName(String name) {

		for(CompoundAnnotationField source : CompoundAnnotationField.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
	
	public static CompoundAnnotationField getOptionByUIName(String uiName) {

		for(CompoundAnnotationField source : CompoundAnnotationField.values()) {

			if(source.getName().equals(uiName))
				return source;
		}
		return null;
	}
}







