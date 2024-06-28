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

package edu.umich.med.mrc2.datoolbox.utils.acqmethod;

import org.apache.commons.math3.util.Precision;

import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;

public class ChromatographicGradientUtils {
	
	public static final double eps = 0.00001d;

	public static boolean gradientsEquivalent(
			ChromatographicGradient gradOne, 
			ChromatographicGradient gradTwo) {
		
		if(gradOne.getGradientSteps().size() != gradTwo.getGradientSteps().size())		
			return false;
		
		if(!mobilePhaseSetsEquivalent(
				gradOne.getMobilePhases(), gradTwo.getMobilePhases()))
			return false;
		
		ChromatographicGradientStep[]gradOneSteps = 
				gradOne.getGradientStepsArray();
		ChromatographicGradientStep[]gradTwoSteps = 
				gradTwo.getGradientStepsArray();
		
		for(int i=0; i<gradOneSteps.length; i++) {
			
			if(!gradientStepsEquivalent(gradOneSteps[i], gradTwoSteps[i]))
				return false;
		}		
		return true;
	}
	
	public static boolean timeTableEquivalent(
			ChromatographicGradient gradOne, 
			ChromatographicGradient gradTwo) {
		
		if(gradOne.getGradientSteps().size() != gradTwo.getGradientSteps().size())		
			return false;
		
		ChromatographicGradientStep[]gradOneSteps = 
				gradOne.getGradientStepsArray();
		ChromatographicGradientStep[]gradTwoSteps = 
				gradTwo.getGradientStepsArray();
		
		for(int i=0; i<gradOneSteps.length; i++) {
			
			if(!gradientStepsEquivalent(gradOneSteps[i], gradTwoSteps[i]))
				return false;
		}		
		return true;
	}
	
	public static boolean mobilePhaseSetsEquivalent(
			MobilePhase[] setOne, 
			MobilePhase[] setTwo) {
		
		if(setOne.length != setTwo.length)
			return false;
		
		for(int i=0; i<setOne.length; i++) {
			
			if((setOne[i] == null && setTwo[i] != null) 
					|| (setOne[i] != null && setTwo[i] == null))
				return false;
			
			if(setOne[i] != null && setTwo[i] != null 
					&& !setOne[i].equals(setTwo[i]))
				return false;
		}
		return true;
	}
	
	public static boolean gradientStepsEquivalent(
			ChromatographicGradientStep stepOne, 
			ChromatographicGradientStep stepTwo) {
		
		if(!Precision.equalsIncludingNaN(
				stepOne.getStartTime(), stepTwo.getStartTime(), eps))
				return false;
		
		if(!Precision.equalsIncludingNaN(
				stepOne.getFlowRate(), stepTwo.getFlowRate(), eps))
				return false;
		
		for(int i=0; i<4; i++) {
			
			if(!Precision.equalsIncludingNaN(
					stepOne.getMobilePhaseStartingPercent()[i], 
					stepTwo.getMobilePhaseStartingPercent()[i], eps))
					return false;
		}		
		return true;
	}
}
