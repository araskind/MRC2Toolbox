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

package edu.umich.med.mrc2.datoolbox.utils;

public class StatDistributionUtils {

	public static double[][]getMAtrixForCosineDistribution(Range dataRange){
		
		double[]y = new double[] {
				3e-04, 0.001, 0.0023, 0.0041, 0.0064, 0.0091, 0.0123, 
		        0.016, 0.02, 0.0244, 0.0291, 0.0341, 0.0393, 0.0448, 
		        0.0504, 0.0562, 0.062, 0.0679, 0.0738, 0.0796, 0.0853, 
		        0.0909, 0.0962, 0.1014, 0.1062, 0.1108, 0.115, 0.1188, 
		        0.1222, 0.1252, 0.1277, 0.1297, 0.1313, 0.1323, 0.1328, 
		        0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 
		        0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 
		        0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 
		        0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 0.1329, 
		        0.1329, 0.1329, 0.1328, 0.1323, 0.1313, 0.1297, 0.1277, 
		        0.1252, 0.1222, 0.1188, 0.115, 0.1108, 0.1062, 0.1014, 
		        0.0962, 0.0909, 0.0853, 0.0796, 0.0738, 0.0679, 0.062, 
		        0.0562, 0.0504, 0.0448, 0.0393, 0.0341, 0.0291, 0.0244, 
		        0.02, 0.016, 0.0123, 0.0091, 0.0064, 0.0041, 0.0023, 
		        0.001, 3e-04};
		double[]x = new double[y.length];
		double step = (dataRange.getSize())/(y.length - 1);
		double min = dataRange.getMin();
		for(int i=0; i<y.length; i++)
			x[i] = min + step * (double)i;

		double[][]out = new double[][] {x,y};
		return out;
	}
	
	
}
