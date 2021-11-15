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

import java.util.Map;
import java.util.TreeMap;


/**
 * Utilities for the Savitzky-Golay smoother.
 *
 * @author $Author: araskind $
 * @version $Revision: 3 $
 */
public class SavitzkyGolayFilter implements Filter {

	
	// constructor(s)
	/**
	 * Standard constructor, which accepts the number of data points to use in for
	 * the smoothing filter.
	 * 
	 * @param filterWidth		The number of data points to use.
	 */
	public SavitzkyGolayFilter(int width){
		
		filterWidth = width;
	}
	
	public double[] filter(Integer[] xvals, Double[] yvals){
		
		int n = xvals.length;
		
		double[] x = new double[n];
		double[] y = new double[n];
		
		for(int i = 0; i < n; i++){
			
			x[i] = xvals[i].doubleValue();
			y[i] = yvals[i];
		}
		
		double smooth[] = new double[n];
		
		smooth = filter(x,y);
		
		return smooth;
	}
	
	public double[] filter(Double[] xvals, Double[] yvals){
		
		int n = xvals.length;
		
		double[] x = new double[n];
		double[] y = new double[n];
		
		for(int i = 0; i < n; i++){
			
			x[i] = xvals[i];
			y[i] = yvals[i];
		}
		
		double smooth[] = new double[n];
		
		smooth = filter(x,y);
		
		return smooth;
	}
	
	// Filter overrides
	public double[] filter(double[] xvals, double[] yvals) throws IllegalArgumentException
	{
		if (xvals.length != yvals.length)
			throw new IllegalArgumentException("The arrays xvals and yvals need to be of equal length.");
		
		int n = xvals.length;
		double smooth[] = new double[n];
		
		// collect the constants applicable for this case
		int h = hvaluesMap.get(filterWidth);
		
		int avals[] = new int[avaluesMap.get(filterWidth).length];
		
		for(int i = 0; i < avaluesMap.get(filterWidth).length; i++)
			avals[i] = avaluesMap.get(filterWidth)[i];
		
		// start the process (5+points*2 actually makes the real points in the enum)
		int marginsize = (5+filterWidth*2+1) / 2 - 1;
		for (int index=marginsize; index<n-marginsize; ++index)
		{
			double value = avals[0] * yvals[index];
			for (int winindex=1; winindex<=marginsize; ++winindex)
				value += avals[winindex] * (yvals[index+winindex]+yvals[index-winindex]);
			value /= h;
			
			if (value < 0)
				value = 0;
			smooth[index] = value;
		}		
		return smooth;
	}
	
	
	// data
	protected int filterWidth;
	
	private static final Map<Integer, Integer[]> avaluesMap = new TreeMap<Integer, Integer[]>(){{
				
				  put(5, new Integer[] {  17,  12,  -3,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0});
				  put(7, new Integer[] {   7,   6,   3,  -2,   0,   0,   0,   0,   0,   0,   0,   0,   0});
				  put(9, new Integer[] {  59,  54,  39,  14, -21,   0,   0,   0,   0,   0,   0,   0,   0});
				  put(11, new Integer[] {  89,  84,  69,  44,   9, -36,   0,   0,   0,   0,   0,   0,   0});
				  put(13, new Integer[] {  25,  24,  21,  16,   9,   0, -11,   0,   0,   0,   0,   0,   0});
				  put(15, new Integer[] {  167, 162, 147, 122,  87,  42, -13, -78,   0,   0,   0,   0,   0});
				  put(17, new Integer[] {  43,  42,  39,  34,  27,  18,   7,  -6, -21,   0,   0,   0,   0});
				  put(19, new Integer[] {  269, 264, 249, 224, 189, 144,  89,  24, -51,-136,   0,   0,   0});
				  put(21, new Integer[] {  329, 324, 309, 284, 249, 204, 149,  84,   9, -76,-171,   0,   0});
				  put(23, new Integer[] {  79,  78,  75,  70,  63,  54,  43,  30,  15,  -2, -21, -42,   0});
				  put(25, new Integer[] {  467, 462, 447, 422, 387, 343, 287, 222, 147,  62, -33,-138,-253});
	}};
	
	private static final Map<Integer, Integer> hvaluesMap = new TreeMap<Integer, Integer>(){{
		
		  put(5, 35);
		  put(7, 21);
		  put(9, 231);
		  put(11, 429);
		  put(13, 143);
		  put(15, 1105);
		  put(17, 323);
		  put(19, 2261);
		  put(21, 3059);
		  put(23, 805);
		  put(25, 5175);
	}};
		
	// constants
	// see also: http://www.vias.org/tmdatanaleng/cc_savgol_coeff.html
	private static final int avalues[][] = new int[][] {
		{  17,  12,  -3,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0},
		{   7,   6,   3,  -2,   0,   0,   0,   0,   0,   0,   0,   0,   0},
		{  59,  54,  39,  14, -21,   0,   0,   0,   0,   0,   0,   0,   0},
		{  89,  84,  69,  44,   9, -36,   0,   0,   0,   0,   0,   0,   0},
		{  25,  24,  21,  16,   9,   0, -11,   0,   0,   0,   0,   0,   0},
		{ 167, 162, 147, 122,  87,  42, -13, -78,   0,   0,   0,   0,   0},
		{  43,  42,  39,  34,  27,  18,   7,  -6, -21,   0,   0,   0,   0},
		{ 269, 264, 249, 224, 189, 144,  89,  24, -51,-136,   0,   0,   0},
		{ 329, 324, 309, 284, 249, 204, 149,  84,   9, -76,-171,   0,   0},
		{  79,  78,  75,  70,  63,  54,  43,  30,  15,  -2, -21, -42,   0},
		{ 467, 462, 447, 422, 387, 343, 287, 222, 147,  62, -33,-138,-253},
	};
	private static final int hvalues[] = new int[] {
		35, 21, 231, 429, 143, 1105, 323, 2261, 3059, 805, 5175
	};
	
	@Override
	public String getCode() {
		return FilterClass.SAVITZKY_GOLAY_MZMINE.getCode();
	}
}
