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

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;

public class ChromatographicColumnComparator extends ObjectCompatrator<LIMSChromatographicColumn> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5809222221449240449L;

	public ChromatographicColumnComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
	}

	public ChromatographicColumnComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(LIMSChromatographicColumn p1, LIMSChromatographicColumn p2) {

		int result = 0;

		switch (property) {

		case ID:
			result = p1.getColumnId().compareTo(p2.getColumnId());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Name:
			result = p1.getColumnName().compareTo(p2.getColumnName());

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