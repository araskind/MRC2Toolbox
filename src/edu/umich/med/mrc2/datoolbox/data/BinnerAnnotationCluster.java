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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class BinnerAnnotationCluster {
	
	private int molIonNumber;
	private Set<BinnerAnnotation>annotations;
	private BinnerAnnotation primaryFeatureAnnotation;
		
	public BinnerAnnotationCluster(BinnerAnnotation firstAnnotation) {
		super();
		annotations = new TreeSet<BinnerAnnotation>();
		annotations.add(firstAnnotation);
		molIonNumber = firstAnnotation.getMolIonNumber();
		if(firstAnnotation.isPrimary())
			primaryFeatureAnnotation = firstAnnotation;
	}

	public boolean addAnnotation(BinnerAnnotation newAnnotation) {
		
		if(newAnnotation.getMolIonNumber() != molIonNumber)
			return false;
		
		annotations.add(newAnnotation);
		if(newAnnotation.isPrimary())
			primaryFeatureAnnotation = newAnnotation;
		
		return true;
	}

	public int getMolIonNumber() {
		return molIonNumber;
	}

	public Set<BinnerAnnotation> getAnnotations() {
		return annotations;
	}

	public BinnerAnnotation getPrimaryFeatureAnnotation() {
		return primaryFeatureAnnotation;
	}
	
	public Map<Double,Double>getMZvalues(){
		
		Map<Double,Double>mzrtMap = new TreeMap<Double,Double>();
		annotations.stream().forEach(
				a -> mzrtMap.put(a.getBinnerMz(), a.getBinnerMz()));
		return mzrtMap;
	}
}
