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
import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class PrecursorLookupSpectrum {

	private String msmsFeatureId;
	private MsPoint precursor;
	private Set<MsPoint>spectrum;
	
	public PrecursorLookupSpectrum(String msmsFeatureId) {
		super();
		this.msmsFeatureId= msmsFeatureId;
		this.spectrum = new TreeSet<MsPoint>(MsUtils.mzSorter);
	}
	
	public boolean isMinorIsotope(double massAccuracyPpm) {
		
		if(precursor == null)
			return false;
		
		Range lookupRange = 
				MsUtils.createPpmMassRange(
						precursor.getMz() - MsUtils.NEUTRON_MASS, massAccuracyPpm);
		double precIntensity = precursor.getIntensity();
		MsPoint lighterIsotope = spectrum.stream().
				filter(p -> lookupRange.contains(p.getMz())).
				filter(p -> (p.getIntensity() > precIntensity)).
				findFirst().orElse(null);
		
		return (lighterIsotope) != null;
	}

	public MsPoint getPrecursor() {
		return precursor;
	}

	public void setPrecursor(MsPoint precursor) {
		this.precursor = precursor;
	}

	public Set<MsPoint> getSpectrum() {
		return spectrum;
	}

	public void addSpectrum(Collection<MsPoint> toAdd) {
		this.spectrum.addAll(toAdd);
	}

	public void addMsPoint(MsPoint p) {
		this.spectrum.add(p);
	}
	
	public String getMsmsFeatureId() {
		return msmsFeatureId;
	}	
}
