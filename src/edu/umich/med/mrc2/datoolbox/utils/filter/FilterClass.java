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

package edu.umich.med.mrc2.datoolbox.utils.filter;

import edu.umich.med.mrc2.datoolbox.utils.filter.sgjdsp.SavitzkyGolayJdsp;

public enum FilterClass {

	//	SAVITZKY_GOLAY("Savitzky-Golay", "SG", SGFilter.class),
	SAVITZKY_GOLAY_MZMINE("Savitzky-Golay | MZMine", "SGMZ", SavitzkyGolayFilter.class),
	SAVITZKY_GOLAY_JDSP("Savitzky-Golay | JDSP", "SGJDSP", SavitzkyGolayJdsp.class),
	MOVING_AVERAGE("Moving Average", "MA", MovingAverageFilter.class),
	WEIGHTED_MOVING_AVERAGE("Weighted Moving Average", "WMA", WeightedMovingAverageFilter.class),
	LOESS("Loess", "L", LoessFilter.class),
	SMOOTHING_CUBIC_SPLINE("Cubic Spline", "CS", SmoothingCubicSplineFilter.class),
	;

	private final String name;
	private final String code;
	private final Class<? extends Filter> filterClass;

	FilterClass(String type, String code, Class<? extends Filter> filterClass) {
		this.name = type;
		this.code = code;
		this.filterClass = filterClass;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}
	
	public String toString() {
		return name + " (" + code + ")";
	}
	
	public Class<? extends Filter> getFilterClass() {
		return filterClass;
	}
	
	public static FilterClass getFilterClassByName(String name) {
		
		for(FilterClass v : FilterClass.values()) {
			if(v.name().equals(name))
				return v;
		}	
		return null;
	}
	
	public static FilterClass getFilterClassByCode(String code) {
		
		for(FilterClass v : FilterClass.values()) {
			if(v.getCode().equals(code))
				return v;
		}	
		return null;
	}
}
