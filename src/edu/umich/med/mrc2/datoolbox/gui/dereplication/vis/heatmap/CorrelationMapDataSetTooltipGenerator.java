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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.heatmap;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.StandardXYZToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.CorrelationMapDataSet;

public class CorrelationMapDataSetTooltipGenerator extends StandardXYZToolTipGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1947101878972605184L;
	private static final NumberFormat corrFormat = new DecimalFormat("#.###");

	public CorrelationMapDataSetTooltipGenerator() {
		super();
	}

	@Override
	public String generateLabelString(XYDataset dataset, int series, int item) {
		return generateToolTip(dataset, series, item);
	}

	@Override
	public String generateToolTip(XYZDataset dataset, int series, int item) {

		CorrelationMapDataSet ds = (CorrelationMapDataSet) dataset;
		MsFeature[] labels = ds.getLabels()[item];
		String tooltip = "<HTML>" + corrFormat.format(ds.getZValue(series, item)) 
			+ "<BR>" + labels[0].getName() + "<BR>" + labels[1].getName();
		return tooltip;
	}
}
