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

package edu.umich.med.mrc2.datoolbox.gui.plot.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.MSFeatureSetStatisticalParameters;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MsFeatureBubbleDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSFeatureStatsColorRenderer extends ColorCodedXYLineAndShapeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MSFeatureSetStatisticalParameters statsParameter;
	
	public MSFeatureStatsColorRenderer(
			MsFeatureBubbleDataSet datasetToRender, 
			MSFeatureSetStatisticalParameters statsParameter) {
		super(datasetToRender);
		this.statsParameter = statsParameter;
		createLookupPaintScale();
	}

	public MSFeatureStatsColorRenderer(
			MsFeatureBubbleDataSet datasetToRender, 
			ColorGradient colorGradient, 
			ColorScale colorScale,
			MSFeatureSetStatisticalParameters statsParameter) {
		super(datasetToRender, colorGradient, colorScale);
		this.statsParameter = statsParameter;
		createLookupPaintScale();
	}

	@Override
	protected Range createDataRange() {
					
		TreeSet<Double>data = new TreeSet<Double>();
		MsFeatureBubbleDataSet ds = 
				(MsFeatureBubbleDataSet)datasetToRender;
		Collection<MsFeature>allFeatures = ds.getAllFeatures();
		allFeatures.stream().filter(f -> Objects.nonNull(f.getStatsSummary())).
			forEach(f -> data.add(f.getStatsSummary().getValueOfType(statsParameter)));			
		Range dataRange = new Range(data.first(), data.last());
		return dataRange;
	}

	@Override
	public Paint getItemPaint(int row, int column) {

		MsFeature feature = 
				((MsFeatureBubbleDataSet)datasetToRender).getMsFeature(row, column);
		if(feature.getStatsSummary() == null 
				|| feature.getStatsSummary().getValueOfType(statsParameter) == null)
			return Color.LIGHT_GRAY;
		
		double value = feature.getStatsSummary().getValueOfType(statsParameter);
		
		return lookupPaintScale.getPaint(value);
	}

	public void setStatsParameter(MSFeatureSetStatisticalParameters statsParameter) {
		
		this.statsParameter = statsParameter;
		createLookupPaintScale();
	}
}










