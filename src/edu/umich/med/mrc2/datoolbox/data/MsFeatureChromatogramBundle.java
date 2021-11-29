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

package edu.umich.med.mrc2.datoolbox.data;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.utils.ChromatogramUtils;

public class MsFeatureChromatogramBundle {
	
	private ChromatogramDefinition chromatogramDefinition;
	private Map<DataFile, Collection<ExtractedIonData>>chromatograms;
	
	public MsFeatureChromatogramBundle(ChromatogramDefinition chromatogramDefinition) {
		super();
		this.chromatogramDefinition = chromatogramDefinition;
		chromatograms = new TreeMap<DataFile, Collection<ExtractedIonData>>();
	}

	public void addChromatogramsForDataFile(
			DataFile df, Collection<ExtractedIonData>chromatograms2add) {
		
		if(chromatograms.get(df) == null)
			chromatograms.put(df, new TreeSet<ExtractedIonData>(ChromatogramUtils.eidComparator));
			
		chromatograms.get(df).addAll(chromatograms2add);
	}
	
	public void addChromatogramForDataFile(
			DataFile df, ExtractedIonData chromatogram2add) {
		
		if(chromatograms.get(df) == null)
			chromatograms.put(df, new TreeSet<ExtractedIonData>(ChromatogramUtils.eidComparator));
			
		chromatograms.get(df).add(chromatogram2add);
	}
	
	public Collection<ExtractedIonData>getChromatogramsForDataFile(DataFile df){
		return chromatograms.get(df);
	}
	
	public ChromatogramDefinition getChromatogramDefinition() {
		return chromatogramDefinition;
	}

	public Map<DataFile, Collection<ExtractedIonData>> getChromatograms() {
		return chromatograms;
	}
}
