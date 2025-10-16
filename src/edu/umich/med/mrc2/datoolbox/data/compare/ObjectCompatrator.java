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

import java.io.Serializable;
import java.util.Comparator;

public class ObjectCompatrator<T> implements Comparator<T> , Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5511197150298292092L;

	protected SortProperty property;
	protected SortDirection direction;

	@Override
	public int compare(T o1, T o2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public ObjectCompatrator(SortProperty property, SortDirection direction) {
		super();
		this.property = property;
		this.direction = direction;
	}

	public ObjectCompatrator(SortProperty property) {
		super();
		this.property = property;
		this.direction = SortDirection.ASC;
	}
}
