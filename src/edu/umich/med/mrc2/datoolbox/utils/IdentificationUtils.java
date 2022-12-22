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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;

public class IdentificationUtils {
	
	private static final MsFeatureIdentityComparator idQualitySorter = 
			new MsFeatureIdentityComparator(SortProperty.Quality);

	public static Collection<MsFeatureIdentity>getBestMatchIds(MsFeature feature){
		
		Collection<MsFeatureIdentity> idList = feature.getIdentifications();			
		Map<CompoundIdentity, List<MsFeatureIdentity>> idsByCompound = 
				idList.stream().collect(Collectors.groupingBy(MsFeatureIdentity::getCompoundIdentity));
		
		Collection<MsFeatureIdentity> bestMatchList = new HashSet<MsFeatureIdentity>();
		for(Entry<CompoundIdentity, List<MsFeatureIdentity>> matchList : idsByCompound.entrySet()) {
			
			MsFeatureIdentity bestMatch = matchList.getValue().
					stream().sorted(idQualitySorter).findFirst().orElse(null);
			if(bestMatch != null)
				bestMatchList.add(bestMatch);
		}
		if(feature.getPrimaryIdentity() != null && !bestMatchList.contains(feature.getPrimaryIdentity()))
			bestMatchList.add(feature.getPrimaryIdentity());
		
		return bestMatchList;
	}
	
	public static Collection<MsFeatureIdentity>filterIdsOnMatchType(
			Collection<MsFeatureIdentity>toFilter,
			Collection<MSMSMatchType>msmsSearchTypes){

		return toFilter.stream().
				filter(i -> Objects.nonNull(i.getReferenceMsMsLibraryMatch())).
				filter(i -> msmsSearchTypes.contains(i.getReferenceMsMsLibraryMatch().getMatchType())).
				collect(Collectors.toSet());
	}
	
	public static MsFeatureIdentity getTopScoringIdForMatchTypes(
			Collection<MsFeatureIdentity>toFilter,
			Collection<MSMSMatchType>msmsSearchTypes,
			MSMSScoringParameter msmsScoringParameter){
		
		Collection<MsFeatureIdentity>toScore = filterIdsOnMatchType(toFilter, msmsSearchTypes);
		if(toScore.isEmpty())
			return null;
		
		Map<MsFeatureIdentity,Double>scoreMap = new HashMap<MsFeatureIdentity,Double>();
		toScore.stream().
			forEach(i -> scoreMap.put(i, i.getReferenceMsMsLibraryMatch().getScoreOfType(msmsScoringParameter)));
		Entry<MsFeatureIdentity, Double> topEntry = 
				MapUtils.getTopEntryByValue(scoreMap, SortDirection.DESC);
		if(topEntry == null)
			return null;
		else
			return topEntry.getKey();
	}
	
	public static Collection<MsFeatureIdentity>filterIdsOnScore(
			Collection<MsFeatureIdentity>toFilter,
			double minScore,
			MSMSScoringParameter msmsScoringParameter){

		return toFilter.stream().
				filter(i -> Objects.nonNull(i.getReferenceMsMsLibraryMatch())).
				filter(i -> i.getReferenceMsMsLibraryMatch().getScoreOfType(msmsScoringParameter) > minScore).
				collect(Collectors.toSet());
	}
}
