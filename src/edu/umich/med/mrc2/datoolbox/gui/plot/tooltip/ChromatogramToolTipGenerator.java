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

package edu.umich.med.mrc2.datoolbox.gui.plot.tooltip;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ChromatogramToolTipGenerator implements XYToolTipGenerator {

	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {

		final double rtValue = dataset.getXValue(series, item);
		final double intValue = dataset.getYValue(series, item);
			
		final String toolTip = 
			"<HTML><B>RT:</B> " + MRC2ToolBoxConfiguration.getRtFormat().format(rtValue)
			+ "<BR><B>Intensity:</B> " + MRC2ToolBoxConfiguration.getIntensityFormat().format(intValue)
			+ "<BR><B>Trace:</B> " + dataset.getSeriesKey(series);

		return toolTip;
	}

}
