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

package edu.umich.med.mrc2.datoolbox.utils.filter;

import java.lang.reflect.Constructor;

import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.SmoothingFilterFields;

public class FilterFactory {

	public static Filter getFilter(Element filterElement) throws Exception {

		String filterCode = 
				filterElement.getAttributeValue(SmoothingFilterFields.FilterCode.name());
		if (filterCode == null)
			throw new InvalidArgumentException("Filter type not specified");

		FilterClass filterClass = FilterClass.getFilterClassByCode(filterCode);
		if (filterClass == null)
			throw new InvalidArgumentException("Unknown filter class");

		Constructor<?> constructor = 
				filterClass.getFilterClass().getConstructor(Element.class);
		Filter filterInstance = 
				(Filter) constructor.newInstance(filterElement);

		return filterInstance;
	}
}
