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


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.SmoothingFilterFields;

public class WeightedMovingAverageFilter extends Filter{

	private final Queue<Double> window = new LinkedList<Double>();
	private double[] weights;
    private final int period;
    private final int padding;
    private double sum;
    private Min minCalc;
 
    public WeightedMovingAverageFilter(int period) {   	
    	this(period, 0);
    }

	public WeightedMovingAverageFilter(int period, int padding) {
    	
		super(null);
        if(period <= 0)
        	throw new IllegalArgumentException("Period must be a positive integer");
        
        this.period = period;
        this.padding = padding;
        minCalc = new Min();
        
        generateWeights();
    }
	
    private void generateWeights() {
    	
    	weights = new double[period];
    	
    	int[] multipliers = new int[period];
    	multipliers[0] = 1;
    	multipliers[1] = 2;
    	   	
    	int middle  = (period - 1)/2;
    	
    	for(int i=2; i<=middle; i++)
    		multipliers[i] = multipliers[i-2] + multipliers[i-1];
    	
    	for(int i=middle+1; i<period; i++)
    		multipliers[i] = multipliers[period-i-1]; 

    	int multSum = 0;
    	
    	for(int i : multipliers)
    		multSum += i;
    	
    	for(int i=0; i<period; i++)
    		weights[i] = (double)multipliers[i] / multSum;
	}

 
    private void addPoint(double num) {
        
        window.add(num);
        
        if (window.size() > period)
            window.remove();       
    }
 
    private double getAvg() {
    	
    	double avg = 0.0d;
    	double sum = 0.0d;
    	int counter = 0;
    	
        if (!window.isEmpty()){
        	
        	for(double value : window){
        		sum += value * weights[counter];
        		counter++;
        	}
        	avg = sum;
        }        
        return avg;
    }

	@Override
	public double[] filter(double[] xvals, double[] yvals) throws IllegalArgumentException {
		
		double min = minCalc.evaluate(yvals);
		double[] paddedYValues = new double[yvals.length + padding * 2];
		int size, counter;
		
		for(int i=padding; i>0; i--)
			paddedYValues[padding - i] = min;
		
		for(int i=0; i<yvals.length; i++)
			paddedYValues[padding + i] = yvals[i];
		
		size = paddedYValues.length - 1;
		
		for(int i=0; i<padding; i++)
			paddedYValues[size - i] = min;
				
		window.clear();
		
		ArrayList<Double>filtered = new ArrayList<Double>();
		
		for(double point : paddedYValues){
			
			addPoint(point);			
			filtered.add(getAvg());
		}
		double[] newValues = new double[filtered.size() - padding * 2];

		counter = 0;
		
		for(int i=padding; i<filtered.size() - padding; i++){
			newValues[counter] = filtered.get(i);
			counter++;
		}
		return newValues;
	}

	@Override
	public FilterClass getFilterClass() {
		return FilterClass.WEIGHTED_MOVING_AVERAGE;
	}

	public int getPeriod() {
		return period;
	}

	public int getPadding() {
		return padding;
	}
	
	@Override
	public boolean equals(Filter obj) {
		
		if (obj == this)
			return true;

        if (obj == null)
            return false;
        
        if (!WeightedMovingAverageFilter.class.isAssignableFrom(obj.getClass()))
            return false;

        final WeightedMovingAverageFilter other = (WeightedMovingAverageFilter) obj;
        
       if(this.period != other.getPeriod())
    	   return false;
       
       if(this.padding != other.getPadding())
    	   return false;
       
		return true;
	}
	
	public WeightedMovingAverageFilter(Element filterElement) {
		
		super(filterElement);
		String periodWidth = 
				filterElement.getAttributeValue(SmoothingFilterFields.Width.name());		
		period = Integer.parseInt(periodWidth);
		String paddingWidth = 
				filterElement.getAttributeValue(SmoothingFilterFields.Padd.name());		
		padding = Integer.parseInt(paddingWidth);
	}

	@Override
	public Element getXmlElement() {

		Element filterElement = super.getXmlElement();
		filterElement.setAttribute(
				SmoothingFilterFields.Width.name(), Integer.toString(period));
		filterElement.setAttribute(
				SmoothingFilterFields.Padd.name(), Integer.toString(padding));
		return filterElement;
	}

	@Override
	protected void parseParameters(Element xmlElement) {

	}
}
