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

package edu.umich.med.mrc2.datoolbox.gui.datexp.tooltip;

import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MSMSFeatureInfoBundleDataSet;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MSMSFeatureInfoBundleTooltipGenerator implements XYZToolTipGenerator {

	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {
		
		MSFeatureInfoBundle b = null;
		if(dataset instanceof MSMSFeatureInfoBundleDataSet) { 			
			b = ((MSMSFeatureInfoBundleDataSet)dataset).getMsFeatureInfoBundle(series, item);
			return getTooltipForFeatureBundle(b);
		}
		return null;
	}

	@Override
	public String generateToolTip(XYZDataset dataset, int series, int item) {
		return generateToolTip((XYDataset)dataset, series, item) ;
	}
	
	protected String getTooltipForFeatureBundle(MSFeatureInfoBundle b) {
		
		if(b == null)
			return null;
		
		MsFeature f = b.getMsFeature();
		String tooltip = 
			"<html><b>" + f.getName() + "</b><br>";
		if(f.getPrimaryIdentity() != null)
		tooltip += "<font style=\"font-weight:bold; color:blue\">" + f.getPrimaryIdentity().getCompoundName() + "</font><br>";
		int charge = f.getCharge();
		String chargeString = "<BR><B>Charge: </B>";
		if(charge > 0)
			chargeString += "<font style=\"font-weight:bold; color:red\">+" + Integer.toString(charge) + "</font>";
		if(charge < 0)
			chargeString += "<font style=\"font-weight:bold; color:blue\">" + Integer.toString(charge) + "</font>";
		
		tooltip +=  
			"<b>M/Z:</b> " +  MRC2ToolBoxConfiguration.getMzFormat().format(f.getMonoisotopicMz()) 
			+ chargeString
			+ "<br><b>RT:</b> " +  MRC2ToolBoxConfiguration.getRtFormat().format(f.getRetentionTime()) 
			+ "<br><b>Intensity:</b> ";
		if(f.getStatsSummary() != null)
			tooltip += 	MRC2ToolBoxConfiguration.getIntensityFormat().format(f.getStatsSummary().getTotalMedian());
		else if(f.getSpectrum() != null && f.getSpectrum().getExperimentalTandemSpectrum() != null)
			tooltip += 	MRC2ToolBoxConfiguration.getIntensityFormat().format(
					f.getSpectrum().getExperimentalTandemSpectrum().getTotalIntensity());
		else
			tooltip += "NA";
		
		return tooltip;
	}
}
