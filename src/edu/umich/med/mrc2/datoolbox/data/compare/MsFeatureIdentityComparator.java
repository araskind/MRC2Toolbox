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

import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;

public class MsFeatureIdentityComparator extends ObjectCompatrator<MsFeatureIdentity>{

	/**
	 *
	 */
	private static final long serialVersionUID = 3208234498036550811L;

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
			result = o1.getCompoundIdentity().getCommonName().compareToIgnoreCase(o2.getCompoundIdentity().getCommonName());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case ID:
			result = 0;
			if(o1.getPrimaryLinkLabel() != null && o2.getPrimaryLinkLabel() != null)
				result = o1.getPrimaryLinkLabel().compareTo(o2.getPrimaryLinkLabel());
			
			if(o1.getPrimaryLinkLabel() != null && o2.getPrimaryLinkLabel() == null)
				result = 1;
			
			if(o1.getPrimaryLinkLabel() == null && o2.getPrimaryLinkLabel() != null)
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

		case Quality:	//	Default descending
			result = 0;

			if(o1.getConfidenceLevel().getLevel() < o2.getConfidenceLevel().getLevel())
				result = -1;

			if(o1.getConfidenceLevel().getLevel() > o2.getConfidenceLevel().getLevel())
				result = 1;

			if(result == 0)
				result = Double.compare(o1.getScore(), o2.getScore());

			if (direction == SortDirection.ASC)
				return -result;
			else
				return result;
			
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


