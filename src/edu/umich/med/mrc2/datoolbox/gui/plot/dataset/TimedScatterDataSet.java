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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.datexp.msone.LCMSPlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.tooltip.FileFeatureTooltipInputObject;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.ArrayUtils;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;

public class TimedScatterDataSet extends TimeSeriesCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9053487866402210888L;
	
	protected MsFeature[] featuresToPlot;
	protected DataAnalysisProject experiment;
	protected Calendar activeCalendar;
	protected PlotValuesStats dataSetStats;
	protected static final SimpleDateFormat injectionTimeFormat = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public TimedScatterDataSet() {
		super();
		activeCalendar = Calendar.getInstance(TimeZone.getDefault());
	}

	//	Plot feature areas
	public TimedScatterDataSet(
			Map<DataPipeline,Collection<MsFeature>> selectedFeaturesMap, 
			ExperimentDesignSubset activeDesign, 
			DataScale dataScale) {

		this();		
		experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		featuresToPlot = selectedFeaturesMap.values().stream().
				flatMap(c -> c.stream()).toArray(size -> new MsFeature[size]);
		Collection<ExperimentalSample> samples = 
				experiment.getExperimentDesign().getSamplesForDesignSubset(activeDesign, true);
		
		//	TODO handle multiple pipelines
		 Set<DataFile>files = DataSetUtils.getActiveFilesForPipelineAndDesignSubset(
						 experiment,
						 experiment.getActiveDataPipeline(),
						 experiment.getExperimentDesign().getActiveDesignSubset(),
						FileSortingOrder.TIMESTAMP);
		
		int seriesCount = 1;
		for (Entry<DataPipeline, Collection<MsFeature>> entry : selectedFeaturesMap.entrySet()) {
			
			for(MsFeature msf : entry.getValue()) {
				
				Map<DataFile, Double> dataMap = 
						PlotDataSetUtils.getScaledPeakAreasForFeature(
								experiment, msf, entry.getKey(), files, dataScale);
				String seriesKey = Integer.toString(seriesCount) + " - " + msf.getName();
				ObjectMappedTimeSeries series = new ObjectMappedTimeSeries(seriesKey);	
				for(DataFile df : files) {
					
					
					if (df.getInjectionTime() != null) {
						FileFeatureTooltipInputObject fftio = 
								new FileFeatureTooltipInputObject(
										df, null, msf, seriesKey, LCMSPlotType.PEAK_AREA);
						series.add(df.getInjectionTime(), dataMap.get(df), fftio);
					}
				}
				addSeries(series);
				seriesCount++;
			}	
		}
	}

	//	Plot single point feature values
	public TimedScatterDataSet(
						MsFeature feature,
						Map<DataFile, SimpleMsFeature> sortedFileFeatureMap,
						ChartColorOption colorOption,
						DataAnalysisProject currentExperiment,
						DataPipeline dataPipeline,
						LCMSPlotType dataType) {
		this();
		if (currentExperiment == null || currentExperiment.getExperimentDesign() == null 
				|| currentExperiment.getExperimentDesign().getSamples().isEmpty())
			return;
		
		Map<String, DataFile[]>seriesMap = PlotDataSetUtils.mapSeriesBySampleType(
				currentExperiment,
				dataPipeline,
				FileSortingOrder.TIMESTAMP,
				currentExperiment.getExperimentDesign().getActiveDesignSubset());	
		
		for( Entry<String, DataFile[]> smEntry : seriesMap.entrySet()){
			
			String seriesKey = smEntry.getKey();
			ObjectMappedTimeSeries series = 
					new ObjectMappedTimeSeries(seriesKey);
			
			for(DataFile df : smEntry.getValue()) {
				
				Double value = null;
				SimpleMsFeature msf = sortedFileFeatureMap.get(df);
				if(msf == null)
					continue;
				
				if(dataType.equals(LCMSPlotType.MZ))
					value = msf.getObservedSpectrum().getMonoisotopicMz();
				
				if(dataType.equals(LCMSPlotType.FEATURE_QUALITY))
					value = msf.getQualityScore();
				
				//	String label = generateLabelForSimpleMsFeature(df, msf, smEntry.getKey(), dataType);
				
				FileFeatureTooltipInputObject fftio = 
						new FileFeatureTooltipInputObject(
								df, msf, feature, seriesKey, dataType);
				series.add(df.getInjectionTime(), value, fftio);
			}	
			addSeries(series);
		}	
		combineSeriesStats();
	}
	
	public Object getLabelObjectForItem(int seriesIndex, int itemIndex) {
		
		TimeSeries series = getSeries(seriesIndex);
		if(series != null)
			return ((ObjectMappedTimeSeries)series).getLabelObject(itemIndex);
		else
			return null;
	}
	
//	protected String generateLabelForSimpleMsFeature(
//			DataFile df, SimpleMsFeature msf, String seriesKey, LCMSPlotType plotValueType) {
//		
//		
//        String label = "<HTML><B>Data file: </B>" + df.getName(); 
//    	label += "<BR><B>Injection time: </B>" + injectionTimeFormat.format(df.getInjectionTime()) + "<BR>";
//       
//        if(plotValueType.equals(LCMSPlotType.MZ))
//        	label += "<B>M/Z: </B>" + MRC2ToolBoxConfiguration.getMzFormat().format(
//        			msf.getObservedSpectrum().getMonoisotopicMz());
//        
//        if(plotValueType.equals(LCMSPlotType.RT_AND_PEAK_WIDTH)) {
//        	
//        	label += "<B>RT: </B>" + MRC2ToolBoxConfiguration.getRtFormat().format(
//        			msf.getRetentionTime()) + " min<BR>";
//        	label += "<B>RT range: </B>" + msf.getRtRange().getFormattedString(
//        			MRC2ToolBoxConfiguration.getRtFormat()) + " min<BR>";
//        	label += "<B>Peak width: </B>" + MRC2ToolBoxConfiguration.getRtFormat().format(
//        			msf.getRtRange().getSize()) + " min";
//        }  
//        if(plotValueType.equals(LCMSPlotType.FEATURE_QUALITY))
//        	label += "<B>Quality score: </B>" + MRC2ToolBoxConfiguration.getPpmFormat().format(
//        			msf.getQualityScore());
//        
//        if(plotValueType.equals(LCMSPlotType.PEAK_AREA))
//        	label += "<B>Quality score: </B>" + MRC2ToolBoxConfiguration.getIntensityFormat().format(
//        			msf.getArea());
//        
//    	TreeMap<ExperimentDesignFactor, ExperimentDesignLevel> desCell = null;
//    	if(df.getParentSample() != null) {
//    		desCell = df.getParentSample().getDesignCell();
//    		label += "<HR><B>Sample: </B>" + df.getParentSample().getName() 
//    				+ " (" + df.getParentSample().getId() + ")";
//    	}
//    	if(desCell != null && !desCell.isEmpty()) {
//    		
//    		for(Entry<ExperimentDesignFactor, ExperimentDesignLevel>e : desCell.entrySet())    			
//    			label += "<BR><B>" + e.getKey().getName() +": </B>" + e.getValue().getName();
//    		
//    		label += "<BR>";
//    	}      	
//        label += "<B>Series: </B>" + seriesKey + "<BR>";
//        
//        return label;
//	}

	public MsFeature[] getPlottedFeatures() {
		return featuresToPlot;
	}
	
	public PlotValuesStats getDataSetStats() {
		return dataSetStats;
	}
		
	protected void combineSeriesStats() {

		dataSetStats = new PlotValuesStats(); 
	    double[]valueArray = new double[0];	  	   
	    double[]lowerBorderArray = new double[0];
	    double[]upperBorderArray = new double[0];
	    for(int i=0; i<getSeriesCount(); i++) {	    	
        	
        	ObjectMappedTimeSeries series = (ObjectMappedTimeSeries) getSeries(i);
        	if(series.getSeriesStats() == null)
        		continue;
        		
        	if(series.getSeriesStats().getValueStats() != null) {
        		double[]result = ArrayUtils.concatDoubleArrays(
        				valueArray, series.getSeriesStats().getValueStats().getValues());
        		valueArray = result;
        	}
        	if(series.getSeriesStats().getLowerBorderStats() != null) {
        		double[]lbresult = ArrayUtils.concatDoubleArrays(
        				lowerBorderArray, series.getSeriesStats().getLowerBorderStats().getValues());
        		lowerBorderArray = lbresult;
        	}
        	if(series.getSeriesStats().getUpperBorderStats() != null)    {   		
        		double[]ubresult = ArrayUtils.concatDoubleArrays(
        				upperBorderArray, series.getSeriesStats().getUpperBorderStats().getValues());
        		upperBorderArray = ubresult;
        	}
        }
        dataSetStats.setValues(valueArray);
        dataSetStats.setLowperBorderValues(lowerBorderArray);
        dataSetStats.setUpperBorderValues(upperBorderArray);
	}
}




























