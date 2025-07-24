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

package edu.umich.med.mrc2.datoolbox.gui.plot.tooltip;

import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.gui.datexp.msone.LCMSPlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.ObjectMappedTimeSeries;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ObjectMappedTimeSeriesToolTipGenerator implements XYToolTipGenerator {

	private static final SimpleDateFormat injectionTimeFormat = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {
		
		if(dataset instanceof TimeSeriesCollection) {
			
			TimeSeries tsc = ((TimeSeriesCollection)dataset).getSeries(series);
			if(tsc instanceof ObjectMappedTimeSeries) {
				
				Object labelObject = ((ObjectMappedTimeSeries)tsc).getLabelObject(item);
				if(labelObject instanceof	FileFeatureTooltipInputObject) {
					final String toolTip =  
							generateLabelForFeature((FileFeatureTooltipInputObject)labelObject);
					return toolTip;
				}
			}
		}
		return null;	
	}
	
	protected String generateLabelForFeature(FileFeatureTooltipInputObject fftio) {
		
		DataFile df = fftio.getDataFile();
		SimpleMsFeature msf = fftio.getSimpleMsFeature();
		String seriesKey = fftio.getSeriesKey();
		LCMSPlotType plotValueType = fftio.getPlotValueType();
		
        String label = "<HTML><B>Data file: </B>" + df.getName(); 
    	label += "<BR><B>Injection time: </B>" 
    			+ injectionTimeFormat.format(df.getInjectionTime()) + "<BR>";
       
        if(plotValueType.equals(LCMSPlotType.MZ))
        	label += "<B>M/Z: </B>" + MRC2ToolBoxConfiguration.getMzFormat().format(
        			msf.getObservedSpectrum().getMonoisotopicMz());
        
        if(plotValueType.equals(LCMSPlotType.RT_AND_PEAK_WIDTH)) {
        	
        	label += "<B>RT: </B>" + MRC2ToolBoxConfiguration.getRtFormat().format(
        			msf.getRetentionTime()) + " min<BR>";
        	label += "<B>RT range: </B>" + msf.getRtRange().getFormattedString(
        			MRC2ToolBoxConfiguration.getRtFormat()) + " min<BR>";
        	label += "<B>Peak width: </B>" + MRC2ToolBoxConfiguration.getRtFormat().format(
        			msf.getRtRange().getSize()) + " min";
        }  
        if(plotValueType.equals(LCMSPlotType.FEATURE_QUALITY))
        	label += "<B>Quality score: </B>" 
        			+ MRC2ToolBoxConfiguration.getPpmFormat().format(msf.getQualityScore());
        
        if(plotValueType.equals(LCMSPlotType.PEAK_AREA))
        	label += "<B>Quality score: </B>" 
        			+ MRC2ToolBoxConfiguration.getIntensityFormat().format(msf.getArea());
        
    	TreeMap<ExperimentDesignFactor, ExperimentDesignLevel> desCell = null;
    	if(df.getParentSample() != null) {
    		desCell = df.getParentSample().getDesignCell();
    		label += "<HR><B>Sample: </B>" + df.getParentSample().getName() 
    				+ " (" + df.getParentSample().getId() + ")";
    	}
    	if(desCell != null && !desCell.isEmpty()) {
    		
    		for(Entry<ExperimentDesignFactor, ExperimentDesignLevel>e : desCell.entrySet())    			
    			label += "<BR><B>" + e.getKey().getName() +": </B>" + e.getValue().getName();
    		
    		label += "<BR>";
    	}      	
        label += "<B>Series: </B>" + seriesKey + "<BR>";
        
        return label;
	}
}
