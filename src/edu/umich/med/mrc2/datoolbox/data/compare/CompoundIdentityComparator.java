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

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;

public class CompoundIdentityComparator extends ObjectCompatrator<CompoundIdentity> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5623202807896292094L;

	public CompoundIdentityComparator(SortProperty property, SortDirection direction) {
		super(property,direction);
	}

	public CompoundIdentityComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(CompoundIdentity o1, CompoundIdentity o2) {

		int result;

		switch (property) {

		case Name:
			result = o1.getCommonName().compareToIgnoreCase(o2.getCommonName());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case ID:
			result = 0;
			if(o1.getPrimaryDatabase() != null && o2.getPrimaryDatabase() != null) {
				
				result = o1.getPrimaryDatabase().getName().compareTo(o2.getPrimaryDatabase().getName());
				if(result == 0)
					result = o1.getPrimaryDatabaseId().compareTo(o2.getPrimaryDatabaseId());
			}
			else if(o1.getPrimaryDatabase() == null && o2.getPrimaryDatabase() == null) {
				result = 0;
			}
			else if(o1.getPrimaryDatabase() == null && o2.getPrimaryDatabase() != null) {
				result = -1;
			}
			else if(o1.getPrimaryDatabase() != null && o2.getPrimaryDatabase() == null) {
				result = 1;
			}
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case NameAndId:
			
			result = o1.getCommonName().compareToIgnoreCase(o2.getCommonName());
			if(result == 0)
				result = o1.getPrimaryDatabase().getName().compareTo(
						o2.getPrimaryDatabase().getName());
			
			if(result == 0)
				result = o1.getPrimaryDatabaseId().compareTo(
						o2.getPrimaryDatabaseId());

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
