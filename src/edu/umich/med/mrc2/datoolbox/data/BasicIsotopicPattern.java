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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class BasicIsotopicPattern implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6804885621035463705L;
	private Set<MsPoint>dataPoints;
	private int charge;

	public int getCharge() {
		return charge;
	}
	
	public BasicIsotopicPattern(MsPoint seedPoint) {

		dataPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
		dataPoints.add(seedPoint);
		charge = 1;
	}

	//	Rewrite using neutron mass and mass error in ppm adjusted for mass
	public boolean addDataPoint(MsPoint newPoint, int newCharge) {

		boolean added = false;
		Range diffRange;

		if(dataPoints.size() == 1) {

			MsPoint seed = dataPoints.iterator().next();

			diffRange = new Range(
					seed.getMz() + 1.0d/(double)newCharge - 0.1d,
					seed.getMz() + 1.0d/(double)newCharge + 0.1);

			if(!diffRange.contains(newPoint.getMz()))
				return false;

			dataPoints.add(newPoint);
			charge = newCharge;
			return true;
		}
		if(newCharge != charge)
			return false;

		for(MsPoint p : dataPoints) {

			diffRange = new Range(
					p.getMz() + 1.0d/(double)charge - 0.1d,
					p.getMz() + 1.0d/(double)charge + 0.1);

			if(diffRange.contains(newPoint.getMz())) {

				dataPoints.add(newPoint);
				return true;
			}
		}
		return added;
	}

	public Set<MsPoint> getDataPoints() {
		return dataPoints;
	}
}
