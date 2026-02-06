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

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MzRtFilter {
	
	private double mz;
	private double mzWindow;
	private MassErrorType massErrorType;
	private double rt;
	private double rtWindow;
	
	private Range mzRange;
	private Range rtRange;
	
	public MzRtFilter(
			double mz, 
			double mzWindow, 
			MassErrorType massErrorType, 
			double rt, 
			double rtWindow) {
		super();
		this.mz = mz;
		this.mzWindow = mzWindow;
		this.massErrorType = massErrorType;
		this.rt = rt;
		this.rtWindow = rtWindow;
		
		mzRange = MsUtils.createMassRange(mz, mzWindow, massErrorType);
		rtRange = new Range(rt - rtWindow, rt + rtWindow);
	}
	
	public boolean matches(double newMz, double newRt) {
		
		if(!mzRange.contains(newMz))
			return false;		
		else if(!rtRange.contains(newRt))
			return false;
		else
			return true;
	}
}
