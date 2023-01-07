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

import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;

public class MsFeatureInfoBundleClusterComparator extends ObjectCompatrator<MsFeatureInfoBundleCluster> {

	/**
	 *
	 */
	private static final long serialVersionUID = -1123946453611729990L;

	public MsFeatureInfoBundleClusterComparator(SortProperty property, SortDirection direction) {
		super(property,direction);
	}

	public MsFeatureInfoBundleClusterComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(MsFeatureInfoBundleCluster c1, MsFeatureInfoBundleCluster c2) {

		int result;

		switch (property) {

		case ID:
			result = c1.getId().compareTo(c2.getId());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case RT:
			result = Double.compare(c1.getRt(), c2.getRt());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case featureCount:
			result = Integer.compare(c1.getComponents().size(), c2.getComponents().size());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case Area:
			result = Double.compare(c1.getMedianArea(), c2.getMedianArea());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case MZ:
			result = Double.compare(c1.getMz(), c2.getMz());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Name:
			result = c1.toString().compareTo(c2.toString());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case pimaryId:
			result = c1.getPrimaryIdentity().getName().compareTo(c2.getPrimaryIdentity().getName());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case Rank:			
			double r1 = 0.0d;
			double r2 = 0.0d;
			if(c1.getLookupFeature() != null)
				r1 = c1.getLookupFeature().getRank();
			
			if(c2.getLookupFeature() != null)
				r2 = c2.getLookupFeature().getRank();
			
			result = Double.compare(r1, r2);
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
