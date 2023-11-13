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
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MSMSFeatureInfoBundleDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MSMSScoreColorRenderer extends ColorCodedXYLineAndShapeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MSMSScoringParameter scoringParameter;
	
	public MSMSScoreColorRenderer(
			MSMSFeatureInfoBundleDataSet datasetToRender, 
			MSMSScoringParameter scoringParameter) {
		super(datasetToRender);
		this.scoringParameter = scoringParameter;
	}

	public MSMSScoreColorRenderer(
			MSMSFeatureInfoBundleDataSet datasetToRender, 
			ColorGradient colorGradient, 
			ColorScale colorScale,
			MSMSScoringParameter scoringParameter) {
		super(datasetToRender, colorGradient, colorScale);
		this.scoringParameter = scoringParameter;
	}

	@Override
	protected Range createDataRange() {
					
		TreeSet<Double>data = new TreeSet<Double>();
		MSMSFeatureInfoBundleDataSet ds = 
				(MSMSFeatureInfoBundleDataSet)datasetToRender;
		List<ReferenceMsMsLibraryMatch> msmsMatches = ds.getAllFeatures().stream().
			filter(b -> b.getMsFeature().isIdentified()).
			filter(b ->Objects.nonNull(b.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
			map(b -> b.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch()).
			collect(Collectors.toList());
		if(msmsMatches.isEmpty())
			return new Range(0.0d);
		
		msmsMatches.stream().forEach(m -> data.add(m.getScoreOfType(scoringParameter)));			
		Range dataRange = new Range(data.first(), data.last());
		return dataRange;
	}

	@Override
	public Paint getItemPaint(int row, int column) {

//		if(lookupPaintScale == null)
//			createLookupPaintScale();
		
		MSFeatureInfoBundle bundle = 
				((MSMSFeatureInfoBundleDataSet)datasetToRender).getMsFeatureInfoBundle(row, column);
		if(bundle == null || !bundle.getMsFeature().isIdentified()
				|| bundle.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch() == null)
			return Color.LIGHT_GRAY;
		
		double score = bundle.getMsFeature().getPrimaryIdentity().
				getReferenceMsMsLibraryMatch().getScoreOfType(scoringParameter);
		
		return lookupPaintScale.getPaint(score);
	}
}










