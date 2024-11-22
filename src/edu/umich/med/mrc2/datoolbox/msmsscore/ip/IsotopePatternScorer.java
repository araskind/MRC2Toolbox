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

package edu.umich.med.mrc2.datoolbox.msmsscore.ip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class IsotopePatternScorer {

	private static final String[]elementSets = new String[] {
			"CH",
			"CHN",
			"CHNO",
			"CHNOP",
			"CHNOPS",
			"CHNOS",
			"CHNS",
			"CHO",
			"CHOP",
			"CHOPS",
			"CHOS",
			"CHS",
		}; 
	private static final double[]elementSetWeights = new double[] {
			189.0d,
			523.0d,
			4522.0d,
			1697.0d,
			494.0d,
			1511.0d,
			237.0d,
			4665.0d,
			1714.0d,
			10.0d,
			351.0d,
			65.0d,
		}; 
	
	private static final double[][]secondIsotopeLinearCoefficients = 
			new double[][] {
		{ 0.0029,0.00078},	//CH
		{-0.018,0.00075},	//CHN
		{-0.014,0.00062},	//CHNO
		{-0.042,0.00061},	//CHNOP
		{-0.076,0.00053},	//CHNOPS
		{-0.021,0.00055},	//CHNOS
		{-0.04,0.00073},	//CHNS
		{-0.0066,0.00063},	//CHO
		{-0.0095,0.00056},	//CHOP
		{-0.055,0.00054},	//CHOPS
		{-0.018,0.00054},	//CHOS
		{-0.022,0.00047},	//CHS
	};
	
	private static final double[][]thirdIsotopeLinearCoefficients = 
			new double[][] {
		{-0.024,0.00019},	//CH
		{-0.014,0.00012},	//CHN
		{-0.038,0.00018},	//CHNO
		{-0.055,0.0002},	//CHNOP
		{-0.021,0.00013},	//CHNOPS
		{ 0.029,0.00008},	//CHNOS
		{ 0.049,0.000018},	//CHNS
		{-0.058,0.00023},	//CHO
		{-0.044,0.00018},	//CHOP
		{ 0.043,0.000096},	//CHOPS
		{ 0.035,0.000079},	//CHOS
		{-0.0037,0.00062},	//CHS
	};
	
	private static final double[][]fourthIsotopeLinearCoefficients = 
			new double[][] {
		{-0.0092,0.000037},	//CH
		{-0.0049,0.000021},	//CHN
		{-0.013, 0.000042},	//CHNO
		{-0.016, 0.000042},	//CHNOP
		{-0.0042,0.000026},	//CHNOPS
		{-0.0042,0.000039},	//CHNOS
		{-0.0022,0.000038},	//CHNS
		{-0.021 ,0.000058},	//CHO
		{-0.016, 0.000040},	//CHOP
		{-0.0015,0.000029},	//CHOPS
		{-0.0021,0.000033},	//CHOS
		{-0.0049,0.000071},	//CHS
	};
	
	private Map<Integer, Collection<IsoPatternScoringObject>>ispScorersMap;

	public IsotopePatternScorer() {
		super();
		populateScorersMap();
	}

	private void populateScorersMap() {

		ispScorersMap = new TreeMap<Integer, Collection<IsoPatternScoringObject>>();
		ispScorersMap.put(2, new ArrayList<IsoPatternScoringObject>());
		ispScorersMap.put(3, new ArrayList<IsoPatternScoringObject>());
		ispScorersMap.put(4, new ArrayList<IsoPatternScoringObject>());
		for(int i=0; i<elementSets.length; i++) {
			
		}
	}
	
	
}




































