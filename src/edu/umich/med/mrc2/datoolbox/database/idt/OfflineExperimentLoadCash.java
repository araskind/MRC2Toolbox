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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;

public class OfflineExperimentLoadCash {

	private static Map<String,ExperimentalSample> experimentalSamples = 
			new TreeMap<String,ExperimentalSample>();
	
	private static Map<String,CompoundIdentity> compoundIdentities = 
			new TreeMap<String,CompoundIdentity>();
	
	private static Map<String,MsMsLibraryFeature> tandemMsLibraryEntriess = 
			new TreeMap<String,MsMsLibraryFeature>();
	
	private static Map<String,LibraryMsFeatureDbBundle> msRtLibraryEntriess = 
			new TreeMap<String,LibraryMsFeatureDbBundle>();
	
	public static void addExperimentalSample(ExperimentalSample sample) {
		experimentalSamples.put(sample.getId(), sample);
	}
	
	public static ExperimentalSample getExperimentalSampleById(String sampleId) {
		return experimentalSamples.get(sampleId);
	}
	
	public static void addCompoundIdentity(CompoundIdentity cid) {
		compoundIdentities.put(cid.getPrimaryDatabaseId(), cid);
	}
	
	public static CompoundIdentity getCompoundIdentityByAccession(String accession) {
		return compoundIdentities.get(accession);
	}

	public static void addMsMsLibraryFeature(MsMsLibraryFeature libFeature) {
		tandemMsLibraryEntriess.put(libFeature.getUniqueId(), libFeature);
	}
	
	public static MsMsLibraryFeature getMsMsLibraryFeatureById(String libId) {
		return tandemMsLibraryEntriess.get(libId);
	}
	
	public static void addLibraryMsFeatureDbBundle(LibraryMsFeatureDbBundle bundle) {
		msRtLibraryEntriess.put(bundle.getFeature().getId(), bundle);
	}
	
	public static LibraryMsFeatureDbBundle getLibraryMsFeatureDbBundleById(String libId) {
		return msRtLibraryEntriess.get(libId);
	}
	
	public static void reset() {
		experimentalSamples.clear();
		compoundIdentities.clear();
		tandemMsLibraryEntriess.clear();
		msRtLibraryEntriess.clear();
	}
}
