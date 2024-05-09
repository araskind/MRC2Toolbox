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

package edu.umich.med.mrc2.datoolbox.data.compare;

import java.util.Collection;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;

public class MsFeatureIdentityComparator extends ObjectCompatrator<MsFeatureIdentity>{

	/**
	 *
	 */
	private static final long serialVersionUID = 3208234498036550811L;

	private static final Collection<CompoundIdSource>libraryIdSources = 
			CompoundIdSource.getLibraryIdSources();
	private static final AdductRankComparator adductComparator = 
			new AdductRankComparator();
	
	public MsFeatureIdentityComparator(SortProperty property, SortDirection direction) {
		super(property,direction);
	}

	public MsFeatureIdentityComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(MsFeatureIdentity o1, MsFeatureIdentity o2) {

		int result;

		switch (property) {

		case Name:
			result = 0;
			if(o1.getCompoundIdentity() != null && o2.getCompoundIdentity() != null) {
				result = o1.getCompoundIdentity().getCommonName().
						compareToIgnoreCase(o2.getCompoundIdentity().getCommonName());
			}
			else if(o1.getCompoundIdentity() == null && o2.getCompoundIdentity() == null) {
				result = 0;
				if(o1.getIdentityName() != null && o2.getIdentityName() != null)
					result = o1.getIdentityName().compareToIgnoreCase(o2.getIdentityName());
			}
			else if(o1.getCompoundIdentity() != null && o2.getCompoundIdentity() == null) {
				result = 1;
				if(o2.getIdentityName() != null)
					result = o1.getCompoundIdentity().getCommonName().compareToIgnoreCase(o2.getIdentityName());
			}
			else if(o1.getCompoundIdentity() == null && o2.getCompoundIdentity() != null) {
				result = -1;
				if(o1.getIdentityName() != null)
					result = o1.getIdentityName().compareToIgnoreCase(o2.getCompoundIdentity().getCommonName());
			}

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case ID:
			result = 0;
			
			if(o1.getPrimaryLinkLabel() == null && o2.getPrimaryLinkLabel() == null)
				result = 0;
			
			else if(o1.getPrimaryLinkLabel() != null && o2.getPrimaryLinkLabel() != null)
				result = o1.getPrimaryLinkLabel().compareTo(o2.getPrimaryLinkLabel());
			
			else if(o1.getPrimaryLinkLabel() != null && o2.getPrimaryLinkLabel() == null)
				result = 1;
			
			else if(o1.getPrimaryLinkLabel() == null && o2.getPrimaryLinkLabel() != null)
				result = -1;

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case pimaryId:
			result = o1.getUniqueId().compareTo(o2.getUniqueId());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Quality:
			
			result = Integer.compare(
					o1.getConfidenceLevel().getLevel(), o2.getConfidenceLevel().getLevel());
			
			if(result == 0 && o1.getIdSource() != null && o2.getIdSource() != null)						
				result = Integer.compare(o1.getIdSource().getRank(), o2.getIdSource().getRank());	
			
			//	Rank RT matches by close RT
			if(result == 0 && o1.getMsRtLibraryMatch() != null 
					&& o2.getMsRtLibraryMatch() != null) {
				
				Double rtError1 = o1.getMsRtLibraryMatch().getRtError();
				Double rtError2 = o2.getMsRtLibraryMatch().getRtError();
				if(rtError1 != null && rtError2 != null)
					result = Double.compare(Math.abs(rtError1), Math.abs(rtError2));
				
				// Select more likely adduct
				if(result == 0) {									
					result = adductComparator.compare(
							o1.getMsRtLibraryMatch().getTopAdductMatch().getLibraryMatch(), 
							o2.getMsRtLibraryMatch().getTopAdductMatch().getLibraryMatch());
				}
			}
			if(result == 0) 				
				result = Double.compare(o2.getEntropyBasedScore(), o1.getEntropyBasedScore());	//	Higher score - better match
			
			if(result == 0) {
						
				Entry<CompoundDatabaseEnum, String>hr1 = 
						o1.getCompoundIdentity().getTopRankingDatabaseId();
				Entry<CompoundDatabaseEnum, String>hr2 = 
						o2.getCompoundIdentity().getTopRankingDatabaseId();
				
				if(hr1 != null && hr2 == null)	// Ranked database before unranked in ASC sort
					result = -1;
				
				if(hr1 == null && hr2 != null)	// Unranked database after ranked in ASC sort
					result = 1;
				
				if(hr1 != null && hr2 != null) {
					result = Integer.compare(hr1.getKey().getRank(), hr2.getKey().getRank());
					if(result == 0)
						result = hr1.getValue().compareToIgnoreCase(hr2.getValue());
				}
			}
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case msmsScore:
			result = Double.compare(o1.getScore(), o2.getScore());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case msmsEntropyScore:
			result = Double.compare(o1.getEntropyBasedScore(), o2.getEntropyBasedScore());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		default:
			break;
		}
		throw (new IllegalStateException());
	}
}


