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

import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;

public class MSRTSearchUtils {

	public static Double calculateRetentionShift(MsFeature parentFeature, MsFeatureIdentity id) {

		if(id == null || id.getMsRtLibraryMatch() == null)
			return null;

		double expectedRt = id.getMsRtLibraryMatch().getExpectedRetention();
		if(expectedRt == 0.0d) {
			return null;
		}
		else {
			if(parentFeature.getStatsSummary() != null) {
				if(parentFeature.getStatsSummary().getMedianObservedRetention() > 0)
					return parentFeature.getStatsSummary().getMedianObservedRetention() - expectedRt;
				else
					return parentFeature.getRetentionTime() - expectedRt;
			}
			else
				return parentFeature.getRetentionTime() - expectedRt;
		}
	}
	
	public static MsFeatureIdentity getIDwithClosestRT(MsFeature parentFeature) {

		Set<MsFeatureIdentity> msRTidList = parentFeature.getMSRTIdentifications();
		if(msRTidList.isEmpty())
			return null;
		
		if(msRTidList.size() == 1)
			return msRTidList.iterator().next();
		
		if(msRTidList.size() > 1) {
			
			MsFeatureIdentity bestId = null;
			double diff = 1000000.0d;
			for(MsFeatureIdentity id : msRTidList) {
				
				Double signedShift = calculateRetentionShift(parentFeature, id);
				if(signedShift != null) {
					
					double shift = Math.abs(signedShift);
					if(shift < diff) {
						diff = shift;
						bestId = id;
					}
				}
			}
			return bestId;
		}		
		return null;
	}
}
