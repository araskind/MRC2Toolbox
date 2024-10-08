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

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsPointBucket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 527228795168227893L;
	private ArrayList<MsPoint> basketPoints;
	private Range mzRange;

	public MsPointBucket(MsPoint p, double massWindow, MassErrorType errorType) {
		super();
		basketPoints = new ArrayList<MsPoint>();
		basketPoints.add(p);
		mzRange = MsUtils.createMassRange(p.getMz(), massWindow, errorType);
	}

	public boolean addPoint(MsPoint newPoint) {

		if(mzRange.contains(newPoint.getMz())) {
			basketPoints.add(newPoint);
			return true;
		}
		else {
			return false;
		}
	}
	
	public MsPoint getAveragePoint() {
		return MsUtils.getAveragePoint(basketPoints);
	}
	
	public MsPoint getMostIntensivePoint() {
		
		if(basketPoints.isEmpty())
			return null;
		
		return basketPoints.stream().
				sorted(MsUtils.reverseIntensitySorter).findFirst().
				orElse(null);
	}
	
	public int getSize() {
		return basketPoints.size();
	}

	public double getAverageIntensity() {

		double intensity = 0.0d;

		for (MsPoint p : basketPoints)
			intensity += p.getIntensity();

		return intensity / basketPoints.size();
	}

	public double getMz() {
		return getAveragePoint().getMz();
	}
	
	public boolean pointBelongs(MsPoint p) {
		
		if(mzRange.contains(p.getMz())) 
			return true;
		else
			return false;
	}
}


