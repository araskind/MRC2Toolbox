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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.Arrays;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;

public class NormalizationUtils {
	
    public static double[] paretoScale(final double[] sample) {
    	
    	double[]sampleCopy = new double[sample.length];
    	System.arraycopy( sample, 0, sampleCopy, 0, sample.length );
    	Arrays.setAll(sampleCopy, i -> Double.isNaN(sampleCopy[i]) ? 0 : sampleCopy[i]);
        DescriptiveStatistics stats = new DescriptiveStatistics(sampleCopy);

        // Compute mean and standard deviation
        double mean = stats.getMean();
        double standardDeviation = stats.getStandardDeviation();

        // initialize the standardizedSample, which has the same length as the sample
        double[] standardizedSample = new double[sample.length];

        for (int i = 0; i < sample.length; i++) {
            standardizedSample[i] = (sample[i] - mean) / Math.sqrt(standardDeviation);
        }
        return standardizedSample;
    }
    
    public static double[] zScore(final double[] sample) {
    	
    	double[]sampleCopy = new double[sample.length];
    	System.arraycopy( sample, 0, sampleCopy, 0, sample.length );
    	Arrays.setAll(sampleCopy, i -> Double.isNaN(sampleCopy[i]) ? 0 : sampleCopy[i]);
        DescriptiveStatistics stats = new DescriptiveStatistics(sampleCopy);

        // Compute mean and standard deviation
        double mean = stats.getMean();
        double standardDeviation = stats.getStandardDeviation();

        // initialize the standardizedSample, which has the same length as the sample
        double[] standardizedSample = new double[sample.length];

        for (int i = 0; i < sample.length; i++) {
            standardizedSample[i] = (sample[i] - mean) / standardDeviation;
        }
        return standardizedSample;
    }
    
    public static double[] rangeScale(final double[] sample, Range scaleRange) {
    	
    	double[]sampleCopy = new double[sample.length];
    	System.arraycopy( sample, 0, sampleCopy, 0, sample.length );
    	Arrays.setAll(sampleCopy, i -> Double.isNaN(sampleCopy[i]) ? 0 : sampleCopy[i]);
        DescriptiveStatistics stats = new DescriptiveStatistics(sampleCopy);
        
        if(scaleRange == null)
        	scaleRange = new Range(0.0d, 100.0d);

        double min = stats.getMin();       
        double size = stats.getMax() - min;
        
        double scaleMin = scaleRange.getMin();       
        double scaleSize = scaleRange.getMax() - scaleMin;

        // initialize the standardizedSample, which has the same length as the sample
        double[] standardizedSample = new double[sample.length];

        for (int i = 0; i < sample.length; i++) {
        	
        	double fraction = (sample[i] - min) / size;
            standardizedSample[i] = scaleMin + fraction * scaleSize;
        }
        return standardizedSample;
    }

	public static double[][] scaleData(
			double[][] data, 
			DataScale dataScale, 
			boolean byColumn) {
		
		if(dataScale.isDirectCalculation())
			return data;
		
		if(byColumn)
			data = MatrixUtils.createRealMatrix(data).transpose().getData();

		double[][] scaledData = new double[data.length][data[0].length];
		
		if(dataScale.equals(DataScale.PARETO)){
			
			for(int i=0; i<data.length; i++)				
				scaledData[i] = paretoScale(data[i]);			
		}
		if(dataScale.equals(DataScale.ZSCORE)){
			
			for(int i=0; i<data.length; i++) 
				scaledData[i] = zScore(data[i]);
		}
		if(dataScale.equals(DataScale.RANGE)){
			
			for(int i=0; i<data.length; i++)
				scaledData[i] = rangeScale(data[i], null);			
		}
		if(byColumn)
			scaledData = MatrixUtils.createRealMatrix(scaledData).transpose().getData();
		
		return scaledData;
	}
	
	public static Range getDataRangeFrom1Darray(double[]array) {
		
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (int i = 0; i < array.length; i++) {

            if (array[i] < min)
                min = array[i];
            
            if (array[i] > max)
                max = array[i];
        }
        return new Range(min, max);
	}
	
	public static Range getDataRangeFrom2Darray(double[][]array) {
		
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (int i = 0; i < array.length; i++) {
        	
            for (int j = 0; j < array[i].length; j++) {
            	
                if (array[i][j] < min) 
                    min = array[i][j];
                
                if (array[i][j] > max) 
                    max = array[i][j];               
            }
        }
        return new Range(min, max);
	}
}
