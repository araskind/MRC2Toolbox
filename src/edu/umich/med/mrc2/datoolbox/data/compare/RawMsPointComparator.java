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

import edu.umich.med.mrc2.datoolbox.data.RawMsPoint;

public class RawMsPointComparator extends ObjectCompatrator<RawMsPoint> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7104217523094908054L;

	public RawMsPointComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
	}

	public RawMsPointComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(RawMsPoint p1, RawMsPoint p2) {

		int result = 0;

		switch (property) {

		case MZ:
			result = Double.compare(p1.getMz(), p2.getMz());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Intensity:
			result = Double.compare(p1.getIntensity(), p2.getIntensity());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case scanNumber:
			result = Integer.compare(p1.getScan(), p2.getScan());

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
