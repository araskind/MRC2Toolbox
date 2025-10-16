/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityCluster;

public class CompoundIdentityClusterComparator extends ObjectCompatrator<CompoundIdentityCluster> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5623202807896292094L;
	private static final CompoundIdentityComparator nameIdComparator = 
			new CompoundIdentityComparator(SortProperty.NameAndId);

	public CompoundIdentityClusterComparator(SortProperty property, SortDirection direction) {
		super(property,direction);
	}

	public CompoundIdentityClusterComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(CompoundIdentityCluster o1, CompoundIdentityCluster o2) {

		int result;

		switch (property) {

		case Name:
			result = o1.getName().compareToIgnoreCase(o2.getName());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case ID:
			result = o1.getClusterId().compareTo(o2.getClusterId());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case NameAndId:
			
			result = nameIdComparator.compare(o1.getPrimaryIdentity(), o2.getPrimaryIdentity());
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
