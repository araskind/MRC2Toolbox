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

import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;

public class MzFrequencyObjectComparator extends ObjectCompatrator<MzFrequencyObject> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5168254146932066483L;
	
	public MzFrequencyObjectComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
	}
	
	public MzFrequencyObjectComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(MzFrequencyObject o1, MzFrequencyObject o2) {

		int result = 0;
		switch (property) {
		
			case rangeMidpoint:
				result = Double.compare(
						o1.getMzRange().getAverage(), o2.getMzRange().getAverage());
	
				if (direction == SortDirection.ASC)
					return result;
				else
					return -result;
			
			case frequency:
				result = Double.compare(o1.getFrequency(), o2.getFrequency());
	
				if (direction == SortDirection.ASC)
					return result;
				else
					return -result;
				
			case RSD:
				result = Double.compare(o1.getRtRSD(), o2.getRtRSD());
	
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
