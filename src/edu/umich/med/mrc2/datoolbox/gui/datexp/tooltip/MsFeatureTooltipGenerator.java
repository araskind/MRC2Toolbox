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

package edu.umich.med.mrc2.datoolbox.gui.datexp.tooltip;

import java.text.NumberFormat;

import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.MSFeatureSetStatisticalParameters;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MsFeatureBubbleDataSet;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MsFeatureMassDefectDataSet;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MsFeatureTooltipGenerator implements XYZToolTipGenerator {
	
	private final NumberFormat intensityFormat = MRC2ToolBoxConfiguration.getIntensityFormat();
	private final NumberFormat percentFormat = NumberFormat.getPercentInstance();
	private final NumberFormat mzFormat = MRC2ToolBoxConfiguration.getMzFormat();
	private final NumberFormat rtFormat = MRC2ToolBoxConfiguration.getRtFormat();

	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {
		
		MsFeature f = null;
		if(dataset instanceof MsFeatureBubbleDataSet) 			
			f = ((MsFeatureBubbleDataSet)dataset).getMsFeature(series, item);
		
		if(dataset instanceof MsFeatureMassDefectDataSet)
			f = ((MsFeatureMassDefectDataSet)dataset).getMsFeature(series, item);

		return getTooltipForFeature(f);
	}

	@Override
	public String generateToolTip(XYZDataset dataset, int series, int item) {
		return generateToolTip((XYDataset)dataset, series, item) ;
	}
	
	protected String getTooltipForFeature(MsFeature f) {
		
		if(f == null)
			return null;
		
		String tooltip = 
			"<html><b>" + f.getName() + "</b><br>";
		if(f.getPrimaryIdentity() != null 
				&& f.getPrimaryIdentity().getCompoundName() != null)
			tooltip += "<font style=\"font-weight:bold; color:blue\">" 
				+ f.getPrimaryIdentity().getCompoundName() + "</font><br>";
		
		int charge = f.getCharge();
		String chargeString = "<BR><B>Charge: </B>";
		if(charge > 0)
			chargeString += "<font style=\"font-weight:bold; color:red\">+" 
					+ Integer.toString(charge) + "</font>";
		if(charge < 0)
			chargeString += "<font style=\"font-weight:bold; color:blue\">" 
					+ Integer.toString(charge) + "</font>";
		
		tooltip +=  
			"<b>M/Z:</b> " +  mzFormat.format(f.getMonoisotopicMz()) + chargeString
			+ "<br><b>RT:</b> " +  rtFormat.format(f.getRetentionTime())
			+ "<hr>"
			+ "<br>Median intensity of experimental samples:<b> " + 
				intensityFormat.format(
					f.getStatsSummary().getValueOfType(MSFeatureSetStatisticalParameters.SAMPLE_MEDIAN)) + "</b>"
			+ "<br>Median intensity of pooled samples:<b> " + 
				intensityFormat.format(
					f.getStatsSummary().getValueOfType(MSFeatureSetStatisticalParameters.POOLED_MEDIAN)) + "</b>"
			+ "<br>% Missing in experimental samples:<b> " + 
				percentFormat.format(
					f.getStatsSummary().getValueOfType(MSFeatureSetStatisticalParameters.PERCENT_MISSING_IN_SAMPLES)/100.0d) + "</b>"
			+ "<br>% Missing in pooled samples:<b> " + 
				percentFormat.format(
					f.getStatsSummary().getValueOfType(MSFeatureSetStatisticalParameters.PERCENT_MISSING_IN_POOLS)/100.0d) + "</b>"
			+ "<br>Area %RSD of experimental samples:<b> " + 
				percentFormat.format( 
					f.getStatsSummary().getValueOfType(MSFeatureSetStatisticalParameters.AREA_RSD_SAMPLES)) + "</b>"
			+ "<br>Area %RSD of pooled samples:<b> " + 
				percentFormat.format(
					f.getStatsSummary().getValueOfType(MSFeatureSetStatisticalParameters.AREA_RSD_POOLS)) + "</b>";
		
		return tooltip;
	}
}
