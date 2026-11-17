/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.umich.med.mrc2.datoolbox.data.enums.ChromatographicGradientParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.SolventChannel;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad.GradientPlotParameters;

public class ChromatographicGradientCompositionDataSet extends XYSeriesCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private TreeMap<Integer,String>seriesNameMap;
	private ChromatographicGradient gradientOne;
	private ChromatographicGradient gradientTwo;

	public ChromatographicGradientCompositionDataSet(ChromatographicGradient gradient) {
		super();
		this.gradientOne = gradient;
		seriesNameMap = new TreeMap<Integer,String>();
		seriesNameMap.put(0, "%A");
		seriesNameMap.put(1, "%B");
		seriesNameMap.put(2, "%C");
		seriesNameMap.put(3, "%D");
		createDataSetFromGradient(gradientOne);		
	}

	public ChromatographicGradientCompositionDataSet(
			ChromatographicGradient activeGradientOne,
			ChromatographicGradient activeGradientTwo, 
			GradientPlotParameters plotParams,
			ChromatographicGradientParameter valueToPlot) {
		
		seriesNameMap = new TreeMap<Integer,String>();
		SortedSet<Integer>channelsToPlot = plotParams.getChannelsToPlot();
		if(valueToPlot.equals(ChromatographicGradientParameter.SolventComposition))
			populateSolventCompositionsDataSet(activeGradientOne, activeGradientTwo, channelsToPlot);
		
		if(valueToPlot.equals(ChromatographicGradientParameter.FlowRate))
			populateFlowRatesDataSet(activeGradientOne, activeGradientTwo);			
	}
	
	private void populateFlowRatesDataSet(ChromatographicGradient activeGradientOne,
			ChromatographicGradient activeGradientTwo) {

		if(activeGradientOne != null) {
			
			String seriesName = "[" + activeGradientOne.getId() + "] flow rate";
			seriesNameMap.put(0, seriesName);
			XYSeries flowSeries = new XYSeries(seriesName);		
			for(ChromatographicGradientStep step : activeGradientOne.getGradientSteps())
				flowSeries.add(step.getStartTime(), step.getFlowRate());
			
			addSeries(flowSeries);	
		}
		if(activeGradientTwo != null) {
			
			if(activeGradientOne != null && activeGradientTwo.equals(activeGradientOne)) {
				return;
			}
			else {
				String seriesName = "[" + activeGradientTwo.getId() + "] flow rate";
				seriesNameMap.put(1, seriesName);
				XYSeries flowSeries = new XYSeries(seriesName);	
				for(ChromatographicGradientStep step : activeGradientTwo.getGradientSteps())
					flowSeries.add(step.getStartTime(), step.getFlowRate());
				
				addSeries(flowSeries);	
			}
		}
	}

	private void populateSolventCompositionsDataSet(
			ChromatographicGradient activeGradientOne,
			ChromatographicGradient activeGradientTwo,
			Set<Integer>channelsToPlot) {
		
		if(activeGradientOne != null) {
			
			SortedSet<Integer>usedChannelsGradOne = 
					getChannelsToPlotForGradient(activeGradientOne, channelsToPlot);
			for(int i : usedChannelsGradOne) {
				String seriesName = "[" + activeGradientOne.getId() + "] %" + SolventChannel.getChannelByPosition(i);
				seriesNameMap.put(i, seriesName);
				XYSeries gradSeries = new XYSeries(seriesName);
				for(ChromatographicGradientStep step : activeGradientOne.getGradientSteps()) 
					gradSeries.add(step.getStartTime(), step.getMobilePhaseStartingPercent()[i]);
				
				addSeries(gradSeries);			
			}
		}
		if(activeGradientTwo != null) {
			
			if(activeGradientOne != null && activeGradientTwo.equals(activeGradientOne)) {
				return;
			}
			else {
				int next = seriesNameMap.size();
				
				SortedSet<Integer>usedChannelsGradTwo = 
						getChannelsToPlotForGradient(activeGradientTwo, channelsToPlot);
				for(int i : usedChannelsGradTwo) {	
					String seriesName = "[" + activeGradientTwo.getId() + "] %" + SolventChannel.getChannelByPosition(i);
					seriesNameMap.put(next + i, seriesName);
					XYSeries gradSeries = new XYSeries(seriesName);
					for(ChromatographicGradientStep step : activeGradientTwo.getGradientSteps())
						gradSeries.add(step.getStartTime(), step.getMobilePhaseStartingPercent()[i]);
					
					addSeries(gradSeries);	
				}
			}
		}
	}
	
	private SortedSet<Integer>getChannelsToPlotForGradient(
			ChromatographicGradient gradient, Set<Integer>channelsFromParameters){
		
		SortedSet<Integer>channelsToPlotForGradient = new TreeSet<>();
		SortedSet<Integer>usedChannels = getUsedChannells(gradient);
		for(Integer i : usedChannels) {
			if(channelsFromParameters.contains(i))
				channelsToPlotForGradient.add(i);

		}
		return channelsToPlotForGradient;
	}

	private void createDataSetFromGradient(ChromatographicGradient gradient) {		

		SortedSet <Integer>usedChannells = getUsedChannells(gradient);
		if(usedChannells.size() == 1) {
			
			int channel = usedChannells.first();
			XYSeries isoSeries = new XYSeries(seriesNameMap.get(channel));
			for(ChromatographicGradientStep step : gradientOne.getGradientSteps())
				isoSeries.add(step.getStartTime(), step.getMobilePhaseStartingPercent()[channel]);
			
			addSeries(isoSeries);
			return;
		}
		if(usedChannells.size() == 2) {
			
			int channel = usedChannells.last();
			XYSeries gradSeries = new XYSeries(seriesNameMap.get(channel));
			for(ChromatographicGradientStep step : gradientOne.getGradientSteps())
				gradSeries.add(step.getStartTime(), step.getMobilePhaseStartingPercent()[channel]);
			
			addSeries(gradSeries);
			return;
		}
		if(usedChannells.size() > 2) {
			
			for(int channel : usedChannells) {
				
				XYSeries gradSeries = new XYSeries(seriesNameMap.get(channel));
				for(ChromatographicGradientStep step : gradientOne.getGradientSteps())
					gradSeries.add(step.getStartTime(), step.getMobilePhaseStartingPercent()[channel]);
				
				addSeries(gradSeries);
			}
		}
	}	
	
	private SortedSet <Integer>getUsedChannells(ChromatographicGradient gradient){
		
		TreeSet<Integer>usedChannells = new TreeSet<Integer>();
		int[]channelUse = new int[4];
		for(ChromatographicGradientStep step : gradient.getGradientSteps()) {
			
			for(int i= 0; i<4; i++) {
				if(step.getMobilePhaseStartingPercent()[i] > 0.0d)
					channelUse[i]++;
			}
		}
		for(int i= 0; i<4; i++) {
			if(channelUse[i] > 0)
				usedChannells.add(i);
		}
		return usedChannells;
	}
}


