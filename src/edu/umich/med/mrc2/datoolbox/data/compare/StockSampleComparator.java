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

import edu.umich.med.mrc2.datoolbox.data.StockSample;

public class StockSampleComparator extends ObjectCompatrator<StockSample> {

	/**
	 *
	 */
	private static final long serialVersionUID = 7117634810131296950L;

	public StockSampleComparator(SortProperty property, SortDirection direction) {
		super(property,direction);
	}

	public StockSampleComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(StockSample o1, StockSample o2) {

		int result;

		switch (property) {

		case Name:
			result = o1.getSampleName().compareTo(o2.getSampleName());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case ID:
			result = o1.getSampleId().compareTo(o2.getSampleId());
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
