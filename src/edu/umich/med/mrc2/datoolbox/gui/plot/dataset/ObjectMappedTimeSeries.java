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

import java.util.Date;
import java.util.TreeMap;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

public class ObjectMappedTimeSeries extends TimeSeries {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4607022534107944158L;
	
	protected TreeMap<RegularTimePeriod,Object> labelObjects;
	protected PlotValuesStats stats;
	
	public ObjectMappedTimeSeries(Comparable name) {

		super(name);
		labelObjects = new TreeMap<RegularTimePeriod,Object>();
	}
	
	public void add(Date x, Number y, Object labelObject) {

		Second sec = new Second(x);
		add(sec, y, true);
		labelObjects.put(sec,labelObject);
	}

	@Override
	public void delete(RegularTimePeriod period) {

		super.delete(period);
		labelObjects.remove(period);
	}

	public Object getLabelObject(int index) {
		return labelObjects.get(getTimePeriod(index));
	}
	
	protected void calculateSeriesStats() {
		
		if(stats == null)
			stats = new PlotValuesStats();
		
		double[] values = new double[this.data.size()];
		for (int i = 0; i < this.data.size(); i++)		
			values[i] = ((TimeSeriesDataItem) this.data.get(i)).getValue().doubleValue();
		
		stats.setValues(values);
	}
	
	public PlotValuesStats getSeriesStats() {

		calculateSeriesStats();
		return stats;
	}
}
