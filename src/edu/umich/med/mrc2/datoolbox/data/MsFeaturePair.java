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

public class MsFeaturePair implements Comparable<MsFeaturePair> {

	private MsFeature referenceFeature;
	private MsFeature unknownFeature;

	public MsFeaturePair(
			MsFeature referenceFeature, 
			MsFeature unknownFeature) {
		super();
		this.referenceFeature = referenceFeature;
		this.unknownFeature = unknownFeature;
	}

	@Override
	public int compareTo(MsFeaturePair o) {
		return getName().compareTo(o.getName());
	}

	public MsFeature getReferenceFeature() {
		return referenceFeature;
	}

	public MsFeature getUnknownFeature() {
		return unknownFeature;
	}
	
	public String getName() {
		return "REF: " + referenceFeature.getName() + 
				" | UNK: " + unknownFeature.getName();
	}
	
	public MsPoint getUnknownFeatureParentIon() {
		
		if(unknownFeature.getSpectrum() == null 
				|| unknownFeature.getSpectrum().getExperimentalTandemSpectrum() == null)
			return null;
		
		return unknownFeature.getSpectrum().getExperimentalTandemSpectrum().getParent();
	}
	
	public MsPoint getReferenceFeatureParentIon() {
		
		if(referenceFeature.getSpectrum() == null 
				|| referenceFeature.getSpectrum().getExperimentalTandemSpectrum() == null)
			return null;
		
		return referenceFeature.getSpectrum().getExperimentalTandemSpectrum().getParent();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!MsFeaturePair.class.isAssignableFrom(obj.getClass()))
			return false;

		final MsFeaturePair other = (MsFeaturePair) obj;

		if (this.hashCode() != other.hashCode())
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return referenceFeature.hashCode() + unknownFeature.hashCode();
	}
}
