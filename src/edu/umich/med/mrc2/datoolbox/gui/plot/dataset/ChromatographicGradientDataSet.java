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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import java.util.TreeMap;
import java.util.TreeSet;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;

public class ChromatographicGradientDataSet extends XYSeriesCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private TreeMap<Integer,String>seriesNameMap;
	private ChromatographicGradient gradient;

	public ChromatographicGradientDataSet(ChromatographicGradient gradient) {
		super();
		this.gradient = gradient;
		seriesNameMap = new TreeMap<Integer,String>();
		seriesNameMap.put(0, "%A");
		seriesNameMap.put(1, "%B");
		seriesNameMap.put(2, "%C");
		seriesNameMap.put(3, "%D");
		analyzeGradient();		
	}

	private void analyzeGradient() {
		
		int[]channelUse = new int[4];
		for(ChromatographicGradientStep step : gradient.getGradientSteps()) {
			
			for(int i= 0; i<4; i++) {
				if(step.getMobilePhaseStartingPercent()[i] > 0.0d)
					channelUse[i]++;
			}
		}
		TreeSet<Integer>usedChannells = new TreeSet<Integer>();
		for(int i= 0; i<4; i++) {
			if(channelUse[i] > 0)
				usedChannells.add(i);
		}
		if(usedChannells.size() == 1) {
			
			int channel = usedChannells.first();
			XYSeries isoSeries = new XYSeries(seriesNameMap.get(channel));
			for(ChromatographicGradientStep step : gradient.getGradientSteps())
				isoSeries.add(step.getStartTime(), step.getMobilePhaseStartingPercent()[channel]);
			
			addSeries(isoSeries);
			return;
		}
		if(usedChannells.size() == 2) {
			
			int channel = usedChannells.last();
			XYSeries gradSeries = new XYSeries(seriesNameMap.get(channel));
			for(ChromatographicGradientStep step : gradient.getGradientSteps())
				gradSeries.add(step.getStartTime(), step.getMobilePhaseStartingPercent()[channel]);
			
			addSeries(gradSeries);
			return;
		}
		if(usedChannells.size() > 2) {
			
			for(int channel : usedChannells) {
				
				XYSeries gradSeries = new XYSeries(seriesNameMap.get(channel));
				for(ChromatographicGradientStep step : gradient.getGradientSteps())
					gradSeries.add(step.getStartTime(), step.getMobilePhaseStartingPercent()[channel]);
				
				addSeries(gradSeries);
			}
			return;
		}
	}	
}


