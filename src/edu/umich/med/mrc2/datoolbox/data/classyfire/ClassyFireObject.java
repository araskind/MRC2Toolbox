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

package edu.umich.med.mrc2.datoolbox.data.classyfire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class ClassyFireObject {

	private String name;
	private String description;
	private String version;
	private String smiles;
	private String inchiKey;
	private String molecularFramework;
	
	private Map<ClassyFireOntologyLevel,ClassyFireOntologyEntry>primaryClassification;
	private List<ClassyFireOntologyEntry>intermediateNodes;
	private List<ClassyFireOntologyEntry>alternativeParents;	
	private Collection<String>substituents;
	private Collection<ClassyFireExternalDescriptor>externalDescriptors;
	private Collection<String>ancestors;
	private Map<String,String>predictedChebiTerms;
	private Map<String,String>predictedLipidMapsTerms;
	
	public ClassyFireObject() {
		super();
		primaryClassification = 
				new TreeMap<ClassyFireOntologyLevel,ClassyFireOntologyEntry>();
		intermediateNodes = new ArrayList<ClassyFireOntologyEntry>();
		alternativeParents = new ArrayList<ClassyFireOntologyEntry>();
		substituents = new TreeSet<String>();
		ancestors = new TreeSet<String>();
		externalDescriptors = new ArrayList<ClassyFireExternalDescriptor>();
		predictedChebiTerms = new TreeMap<String,String>();
		predictedLipidMapsTerms = new TreeMap<String,String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public String getInchiKey() {
		return inchiKey;
	}

	public void setInchiKey(String inchiKey) {
		this.inchiKey = inchiKey;
	}

	public String getMolecularFramework() {
		return molecularFramework;
	}

	public void setMolecularFramework(String molecularFramework) {
		this.molecularFramework = molecularFramework;
	}

	public Map<ClassyFireOntologyLevel, ClassyFireOntologyEntry> getPrimaryClassification() {
		return primaryClassification;
	}

	public List<ClassyFireOntologyEntry> getIntermediateNodes() {
		return intermediateNodes;
	}

	public List<ClassyFireOntologyEntry> getAlternativeParents() {
		return alternativeParents;
	}

	public Collection<String> getSubstituents() {
		return substituents;
	}

	public Collection<ClassyFireExternalDescriptor> getExternalDescriptors() {
		return externalDescriptors;
	}

	public Collection<String> getAncestors() {
		return ancestors;
	}

	public Map<String, String> getPredictedChebiTerms() {
		return predictedChebiTerms;
	}

	public Map<String, String> getPredictedLipidMapsTerms() {
		return predictedLipidMapsTerms;
	}
}
