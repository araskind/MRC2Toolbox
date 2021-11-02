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

import edu.umich.med.mrc2.datoolbox.data.MsPoint;

public class MsDataPointComparator extends ObjectCompatrator<MsPoint> {

	/**
	 *
	 */
	private static final long serialVersionUID = 6792554994895024895L;

	public MsDataPointComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
	}

	public MsDataPointComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(MsPoint p1, MsPoint p2) {

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
				result = Integer.compare(p1.getScanNum(), p2.getScanNum());
	
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
