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

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public class CompoundDatabaseCompatrator  extends ObjectCompatrator<CompoundDatabaseEnum> {

	private static final long serialVersionUID = 1L;

	public CompoundDatabaseCompatrator(SortProperty property) {
		super(property);
		this.direction = SortDirection.ASC;
	}
	
	public CompoundDatabaseCompatrator(
			SortProperty property, SortDirection direction) {
		super(property, direction);
	}

	@Override
	public int compare(CompoundDatabaseEnum o1, CompoundDatabaseEnum o2) {

		int result = 0;

		switch (property) {

			case Name:
				result = o1.name().compareToIgnoreCase(o2.name());
	
				if (direction == SortDirection.ASC)
					return result;
				else
					return -result;
				
			case Rank:	//	Lower rank number means more important, so the sorting is reverse
				
				if(o1.getRank() != null && o2.getRank() == null)					
					result = 1;
	
				if(o1.getRank() == null && o2.getRank() != null)					
					result = -1;
								
				if(o1.getRank() != null && o2.getRank() != null)					
					result = Integer.compare(o2.getRank(), o1.getRank());
				
				if(result == 0)
					result = o2.name().compareToIgnoreCase(o1.name());
				
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
