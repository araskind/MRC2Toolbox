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

package edu.umich.med.mrc2.datoolbox.data;

import java.util.NavigableSet;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.utils.Range;

public class RangeBucket {

	private NavigableSet<Range>rangeSet;

	public RangeBucket() {
		super();
		rangeSet = new TreeSet<Range>();
	}
	
	public void addRange(Range newRange) {
		
		if(rangeSet.isEmpty()) {
			rangeSet.add(newRange);
			return;
		}
		boolean added = false;
		for(Range r : rangeSet) {
			
			 if(r.overlapsWith(newRange)) {
				 r.extendRange(newRange);
				 added = true;
			 }
		}
		if(!added)
			rangeSet.add(newRange);
	}

	public NavigableSet<Range> getRangeSet() {
		return rangeSet;
	}
	
	
}
