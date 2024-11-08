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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.data.Range;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.datexp.msone.LCMSPlotType;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class TimedScatterDataSetWithCustomErrors extends TimedScatterDataSet implements DataSetWithCustomErrors{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//	Single series
	public TimedScatterDataSetWithCustomErrors(Map<DataFile,SimpleMsFeature>dataFileFeatureMap) {		
		super();		
	}
		
	//	Series by sample type
	public TimedScatterDataSetWithCustomErrors(
			MsFeature feature,
			Map<DataFile,SimpleMsFeature>dataFileFeatureMap,
			DataAnalysisProject currentExperiment,
			DataPipeline pipeline) {
		super();
		if (currentExperiment == null || currentExperiment.getExperimentDesign() == null 
				|| currentExperiment.getExperimentDesign().getSamples().isEmpty())
			return;
		
		Map<String, DataFile[]>seriesMap = PlotDataSetUtils.mapSeriesBySampleType(
				pipeline,
				FileSortingOrder.TIMESTAMP,
				currentExperiment.getExperimentDesign().getActiveDesignSubset());	
		
		for( Entry<String, DataFile[]> smEntry : seriesMap.entrySet()){
			
			NamedTimeSeriesWithCustomErrors series = 
					new NamedTimeSeriesWithCustomErrors(smEntry.getKey());
			
			for(DataFile df : smEntry.getValue()) {
				
				SimpleMsFeature msf = dataFileFeatureMap.get(df);
				if(msf == null)
					continue;
				
				String label = generateLabelForSimpleMsFeature(
						df, msf, smEntry.getKey(), LCMSPlotType.RT_AND_PEAK_WIDTH);
				double rtMin = msf.getRetentionTime();
				double rtMax = msf.getRetentionTime();
				if(msf.getRtRange() != null) {
					rtMin = msf.getRtRange().getMin();
					rtMax = msf.getRtRange().getMax();
				}
				series.add(df.getInjectionTime(), 
						msf.getRetentionTime(), 
						rtMin, rtMax, label);
			}	
			addSeries(series);
		}	
		combineSeriesStats();
	}

	@Override
    public Range getRangeBounds(boolean includeInterval) {
		
        Range result = null;
        while (getSeries().iterator().hasNext()) {
        	
        	NamedTimeSeriesWithCustomErrors series = (NamedTimeSeriesWithCustomErrors) getSeries().iterator().next();
            Range r = new Range(series.getFullDataRange().getMin(), series.getFullDataRange().getMax());
            result = Range.combineIgnoringNaN(result, r);
        }
        return result;
    }
	
    @Override
    public Range getRangeBounds(
    		List visibleSeriesKeys, 
    		Range xRange,
            boolean includeInterval) {
        Range result = null;
        for (Object visibleSeriesKey : visibleSeriesKeys) {
            Comparable seriesKey = (Comparable) visibleSeriesKey;
            NamedTimeSeriesWithCustomErrors series = (NamedTimeSeriesWithCustomErrors)getSeries(seriesKey);
            Range r = series.findValueRange(xRange, this.getXPosition(), activeCalendar);
            result = Range.combineIgnoringNaN(result, r);
        }
        return result;
    }
	
	private void createSeriesBySampleType() {
		
	}

	@Override
	public double getLowerYBorder(int series, int item) {

		Number[]borders = 
				((NamedTimeSeriesWithCustomErrors)getSeries(series)).getBorders(item);
		return (double) borders[0];
	}

	@Override
	public double getUpperYBorder(int series, int item) {

		Number[]borders = 
				((NamedTimeSeriesWithCustomErrors)getSeries(series)).getBorders(item);
		return (double) borders[1];
	}

	@Override
	public double getLowerXBorder(int series, int item) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getUpperXBorder(int series, int item) {
		// TODO Auto-generated method stub
		return 0;
	}
}




