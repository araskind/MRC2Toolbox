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

import umich.ms.datatypes.scan.IScan;

public class IScanComparator  extends ObjectCompatrator<IScan> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5809222221449240449L;

	public IScanComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
	}

	public IScanComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(IScan p1, IScan p2) {

		int result = 0;

		switch (property) {

		case RT:
			result = Double.compare(p1.getRt(), p2.getRt());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case scanNumber:
			result = Double.compare(p1.getNum(), p2.getNum());

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