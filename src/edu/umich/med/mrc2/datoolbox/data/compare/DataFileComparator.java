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

import edu.umich.med.mrc2.datoolbox.data.DataFile;

public class DataFileComparator extends ObjectCompatrator<DataFile> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataFileComparator(SortProperty property) {
		super(property);
		// TODO Auto-generated constructor stub
	}
	
	public DataFileComparator(SortProperty property, SortDirection direction) {
		super(property, direction);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(DataFile df1, DataFile df2) {

		int result = 0;

		switch (property) {

		case Name:
			result = df1.getName().compareTo(df2.getName());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;
			
		case injectionTime:
			
			if(df1.getInjectionTime() != null && df2.getInjectionTime() != null)
				result = df1.getInjectionTime().compareTo(df2.getInjectionTime());			

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
