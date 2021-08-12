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

import edu.umich.med.mrc2.datoolbox.data.MsFeature;

public class MsFeatureComparator extends ObjectCompatrator<MsFeature> {

	/**
	 *
	 */
	private static final long serialVersionUID = -6698896890232172943L;

	public MsFeatureComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
	}

	public MsFeatureComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(MsFeature o1, MsFeature o2) {

		int result = 0;

		switch (property) {
		
		case ID:
			result = o1.getId().compareTo(o2.getId());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Name:
			result = o1.getName().compareTo(o2.getName());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case MZ:
			result = Double.compare(o1.getMonoisotopicMz(), o2.getMonoisotopicMz());
			if (result == 0)
				result = Double.compare(o1.getBasePeakMz(), o2.getBasePeakMz());
			if (result == 0)
				result = o1.toString().compareTo(o2.toString());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Area:
			result = Double.compare(o1.getAveragePeakArea(), o2.getAveragePeakArea());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case RT:
			result = Double.compare(o1.getRetentionTime(), o2.getRetentionTime());
			if (result == 0)
				result = o1.toString().compareTo(o2.toString());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case pimaryId:
			result = 0;
			if (o1.getPrimaryIdentity() != null && o2.getPrimaryIdentity() != null)
				result = o1.getPrimaryIdentity().getName().compareTo(o2.getPrimaryIdentity().getName());

			if (o1.getPrimaryIdentity() != null && o2.getPrimaryIdentity() == null)
				result = o1.getPrimaryIdentity().getName().compareTo(o2.getName());

			if (o1.getPrimaryIdentity() == null && o2.getPrimaryIdentity() != null)
				result = o1.getName().compareTo(o2.getPrimaryIdentity().getName());

			if (o1.getPrimaryIdentity() == null && o2.getPrimaryIdentity() == null) {
				result = o1.getName().compareTo(o2.getName());

				if (result == 0)
					result = o1.toString().compareTo(o2.toString());
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
