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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.compare.RawMsPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class RawMsPointBucket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7869334229811164755L;
	private Collection<RawMsPoint>points;
	private Range calculatedWindow; 
	private static final RawMsPointComparator pSorter = 
			new RawMsPointComparator(SortProperty.Intensity, SortDirection.DESC);

	public RawMsPointBucket(RawMsPoint p, double massWindow, MassErrorType errorType) {
		super();
		points = new ArrayList<RawMsPoint>();
		points.add(p);
		calculatedWindow = MsUtils.createMassRange(p.getMz(), massWindow, errorType);
	}
	
	public boolean pointFits(RawMsPoint p) {
		return calculatedWindow.contains(p.getMz());
	}
	
	public void addPoint(RawMsPoint p) {
		points.add(p);
	}
	
	public RawMsPoint getAveragePoint() {
		
		if(points.isEmpty())
			return null;
		
		if(points.size() == 1)
			return points.iterator().next();
		
		double totalIntensity =  points.stream().mapToDouble(p -> p.getIntensity()).sum();
		double massIntensityProductSum = points.stream().mapToDouble(p -> p.getMz() * p.getIntensity()).sum();
		double avgMz = massIntensityProductSum / totalIntensity;
		return new RawMsPoint(avgMz, totalIntensity);
	}
	
	public RawMsPoint getMostIntensivePoint() {
		return points.stream().sorted(pSorter).findFirst().orElse(null);
	}
	
	public int getSize() {
		return points.size();
	}
}

