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

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;

public class MsFeatureInfoBundleComparator extends ObjectCompatrator<MsFeatureInfoBundle> {

	/**
	 *
	 */
	private static final long serialVersionUID = -6698896890232172943L;

	public MsFeatureInfoBundleComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
	}

	public MsFeatureInfoBundleComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(MsFeatureInfoBundle o1, MsFeatureInfoBundle o2) {

		int result = 0;

		switch (property) {
		
		case ID:
			result = o1.getMsFeature().getId().compareTo(o2.getMsFeature().getId());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Name:
			result = o1.getMsFeature().getName().compareTo(o2.getMsFeature().getName());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case MZ:
			result = Double.compare(o1.getMsFeature().getMonoisotopicMz(), o2.getMsFeature().getMonoisotopicMz());
			if (result == 0)
				result = Double.compare(o1.getMsFeature().getBasePeakMz(), o2.getMsFeature().getBasePeakMz());
			if (result == 0)
				result = o1.getMsFeature().toString().compareTo(o2.getMsFeature().toString());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Area:
			result = Double.compare(o1.getMsFeature().getAveragePeakArea(), o2.getMsFeature().getAveragePeakArea());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case RT:
			result = Double.compare(o1.getMsFeature().getRetentionTime(), o2.getMsFeature().getRetentionTime());
			if (result == 0)
				result = o1.getMsFeature().toString().compareTo(o2.getMsFeature().toString());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case pimaryId:
			result = 0;
			if (o1.getMsFeature().getPrimaryIdentity() != null && o2.getMsFeature().getPrimaryIdentity() != null)
				result = o1.getMsFeature().getPrimaryIdentity().getName().compareTo(o2.getMsFeature().getPrimaryIdentity().getName());

			if (o1.getMsFeature().getPrimaryIdentity() != null && o2.getMsFeature().getPrimaryIdentity() == null)
				result = o1.getMsFeature().getPrimaryIdentity().getName().compareTo(o2.getMsFeature().getName());

			if (o1.getMsFeature().getPrimaryIdentity() == null && o2.getMsFeature().getPrimaryIdentity() != null)
				result = o1.getMsFeature().getName().compareTo(o2.getMsFeature().getPrimaryIdentity().getName());

			if (o1.getMsFeature().getPrimaryIdentity() == null && o2.getMsFeature().getPrimaryIdentity() == null) {
				result = o1.getMsFeature().getName().compareTo(o2.getMsFeature().getName());

				if (result == 0)
					result = o1.getMsFeature().toString().compareTo(o2.getMsFeature().toString());
			}
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
