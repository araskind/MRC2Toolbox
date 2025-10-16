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

import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisMethod;

public class AnalysisMethodComparator  extends ObjectCompatrator<AnalysisMethod> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5809222221449240449L;

	public AnalysisMethodComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
	}

	public AnalysisMethodComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(AnalysisMethod p1, AnalysisMethod p2) {

		int result = 0;

		switch (property) {

		case ID:
			result = p1.getId().compareTo(p2.getId());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Name:
			result = p1.getName().compareTo(p2.getName());

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