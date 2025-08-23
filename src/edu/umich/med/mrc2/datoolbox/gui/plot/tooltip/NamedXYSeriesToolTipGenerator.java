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

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.NamedXYSeries;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.ObjectMappedTimeSeries;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.TimedScatterDataSet;

public class NamedXYSeriesToolTipGenerator implements XYToolTipGenerator {

	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {
		
		String toolTip = "";
		if(dataset instanceof XYSeriesCollection) {
			
			XYSeries dataSeries = ((XYSeriesCollection)dataset).getSeries(series);
			if(dataSeries instanceof NamedXYSeries)
				toolTip = ((NamedXYSeries)dataSeries).getLabel(item);
		}
		if(dataset instanceof TimedScatterDataSet) {
						
			TimeSeries dataSeries = ((TimedScatterDataSet)dataset).getSeries(series);
			if(dataSeries instanceof ObjectMappedTimeSeries) {
				
				Object labelObject = ((ObjectMappedTimeSeries)dataSeries).getLabelObject(item);
				if(labelObject instanceof	FileFeatureTooltipInputObject) 						
					return TooltipUtils.generateLabelForFeature(
						(FileFeatureTooltipInputObject)labelObject);
			}			
		}		
		return toolTip;
	}
}
