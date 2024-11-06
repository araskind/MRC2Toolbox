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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.jfree.data.time.TimeSeriesCollection;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileTimeStampComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class TimedScatterDataSet extends TimeSeriesCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9053487866402210888L;
	
	protected MsFeature[] featuresToPlot;
	protected DataAnalysisProject experiment;
	protected Calendar activeCalendar;

	public TimedScatterDataSet() {
		super();
		activeCalendar = Calendar.getInstance(TimeZone.getDefault());
	}

	public TimedScatterDataSet(
			Map<DataPipeline,Collection<MsFeature>> selectedFeaturesMap, 
			ExperimentDesignSubset activeDesign, 
			DataScale dataScale) {

		super();
		activeCalendar = Calendar.getInstance(TimeZone.getDefault());
		
		experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		featuresToPlot = selectedFeaturesMap.values().stream().
				flatMap(c -> c.stream()).toArray(size -> new MsFeature[size]);
		Collection<ExperimentalSample> samples = 
				experiment.getExperimentDesign().getSamplesForDesignSubset(activeDesign);
		
		int seriesCount = 1;
		for (Entry<DataPipeline, Collection<MsFeature>> entry : selectedFeaturesMap.entrySet()) {
			
			for(MsFeature msf : entry.getValue()) {
				
				Set<DataFile> files = samples.stream().
						flatMap(s -> s.getDataFilesForMethod(entry.getKey().getAcquisitionMethod()).stream()).
						filter(s -> s.isEnabled()).sorted(new DataFileTimeStampComparator()).
						collect(Collectors.toCollection(LinkedHashSet::new));
				
				Map<DataFile, Double> dataMap = 
						PlotDataSetUtils.getNormalizedDataForFeature(experiment, msf, entry.getKey(), files, dataScale);
				NamedTimeSeries series = new NamedTimeSeries(Integer.toString(seriesCount) + " - " + msf.getName());	
				for(DataFile df : files) {
					
					if (df.getInjectionTime() != null)
						series.add(df.getInjectionTime(), dataMap.get(df), df.getName());
				}
				addSeries(series);
				seriesCount++;
			}	
		}	
	}

	public MsFeature[] getPlottedFeatures() {
		return featuresToPlot;
	}
}




























