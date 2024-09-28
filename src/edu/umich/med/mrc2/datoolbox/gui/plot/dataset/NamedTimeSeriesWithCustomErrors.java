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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;

public class NamedTimeSeriesWithCustomErrors extends NamedTimeSeries {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<RegularTimePeriod,Number[]>errors;

	public NamedTimeSeriesWithCustomErrors(Comparable name) {
		super(name);
		errors = new TreeMap<RegularTimePeriod,Number[]>();
	}
	
	public void add(Date x, Number y, Number min, Number max, String label) {

		super.add(x, y, label);
		errors.put(new Second(x), new Number[] {min,max});
	}

}
