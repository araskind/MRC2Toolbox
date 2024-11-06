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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeriesDataItem;

import edu.umich.med.mrc2.datoolbox.utils.Range;

public class NamedTimeSeriesWithCustomErrors extends NamedTimeSeries {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<RegularTimePeriod,Number[]>errors;
	private Range fullDataRange;

	public NamedTimeSeriesWithCustomErrors(Comparable name) {
		super(name);
		errors = new TreeMap<RegularTimePeriod,Number[]>();
	}
	
	public void add(Date x, Number y, Number min, Number max, String label) {

		super.add(x, y, label);
		errors.put(new Second(x), new Number[] {min,max});
		if(fullDataRange == null) {
			fullDataRange = new Range(min.doubleValue(), max.doubleValue());
		}
		else {
			fullDataRange.extendRange(new Range(min.doubleValue(), max.doubleValue()));
		}
	}
	
	public Number[]getBorders(int item){		
		return errors.get(getTimePeriod(item));
	}

	public Range getFullDataRange() {
		return fullDataRange;
	}
	
	@Override
    public org.jfree.data.Range findValueRange(org.jfree.data.Range xRange, TimePeriodAnchor xAnchor, Calendar calendar) {

        double lowY = Double.POSITIVE_INFINITY;
        double highY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < this.data.size(); i++) {
            TimeSeriesDataItem item = (TimeSeriesDataItem) this.data.get(i);
            long millis = item.getPeriod().getMillisecond(xAnchor, calendar);
            if (xRange.contains(millis)) {
            	
            	Number[]errorRange = getBorders(i);
            	Number errorMin= errorRange[0];
            	if(errorMin != null)
            		lowY = minIgnoreNaN(lowY, errorMin.doubleValue());
            	
            	Number errorMax= errorRange[1];
            	if(errorMax != null)
            		highY = maxIgnoreNaN(highY, errorMax.doubleValue());           	
            }
        }
        if (Double.isInfinite(lowY) && Double.isInfinite(highY)) {
            if (lowY < highY) {
                return new org.jfree.data.Range(lowY, highY);
            } else {
                return new org.jfree.data.Range(Double.NaN, Double.NaN);
            }
        }
        return new org.jfree.data.Range(lowY, highY);
    }
	
    private double minIgnoreNaN(double a, double b) {
        if (Double.isNaN(a)) {
            return b;
        }
        if (Double.isNaN(b)) {
            return a;
        }
        return Math.min(a, b);
    }
    
    private double maxIgnoreNaN(double a, double b) {
        if (Double.isNaN(a)) {
            return b;
        }
        if (Double.isNaN(b)) {
            return a;
        }
        else {
            return Math.max(a, b);
        }
    }
}
