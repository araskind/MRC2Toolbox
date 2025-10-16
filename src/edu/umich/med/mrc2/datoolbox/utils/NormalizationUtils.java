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

import java.util.Arrays;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.stat.StatUtils;
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

	public static double[][] scale2Ddata(
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
	
	public static double[] scaleData(
			double[] data, 
			DataScale scale) {
		
		double[] scaledData = new double[data.length];
		
		if (scale.equals(DataScale.RAW))
			return data;
		
		if (scale.equals(DataScale.LN)) {

			for (int i = 0; i < data.length; i++) {
				double ln = Math.log(data[i]);
				scaledData[i] = Double.isFinite(ln) ?  ln : 0.001d;
			}
		}
		if (scale.equals(DataScale.LOG10)) {

			for (int i = 0; i < data.length; i++) {
				double ln = Math.log10(data[i]);
				scaledData[i] = Double.isFinite(ln) ?  ln : 0.001d;
			}
		}
		if (scale.equals(DataScale.SQRT)) {
			
			for (int i = 0; i < data.length; i++)
				scaledData[i] = Math.sqrt(data[i]);
		}
		if (scale.equals(DataScale.ZSCORE))
			scaledData = StatUtils.normalize(data);
		
		if (scale.equals(DataScale.PARETO))
			scaledData = paretoScale(data);
		
		if (scale.equals(DataScale.RANGE))
			scaledData = rangeScale(data, 0.0d, 100.0d);
		
		return scaledData;
	}	

	public static double[] rangeScale(
			double[] input, 
			double min, 
			double max) throws IllegalArgumentException {

		if (min >= max)
			throw new IllegalArgumentException("Max value should be larger than min value!");

		double[] output = new double[input.length];
		double range = max - min;

		DescriptiveStatistics stats = new DescriptiveStatistics(input);
		double minRaw = stats.getMin();
		double rangeRaw = stats.getMax() - minRaw;

		for (int i = 0; i < input.length; i++)
			output[i] = range * (input[i] - minRaw) / rangeRaw + min;

		return output;
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
	
	public static double calculateRelativeChange(double valueOne, double valueTwo) {
		
		double divisor = valueOne;
		if(valueOne == 0.0d)
			divisor = valueTwo;
		
		if(divisor == 0.0d)
			return 0.0d;
		else
			return (valueOne - valueTwo) / divisor;
	}
}
