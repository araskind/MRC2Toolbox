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

package edu.umich.med.mrc2.datoolbox.gui.datexp;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.MSFeatureSetStatisticalParameters;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.gui.plot.HeatMapDataRange;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MZRTPlotParameterObject {

	private Range mzRange;
	private Range rtRange;
	private DataScale dataScale;
	private MSFeatureSetStatisticalParameters msFeatureSetStatisticalParameter;
	private FeatureIdentificationMeasure featureIdentificationMeasure;
	private ColorGradient colorGradient;
	private ColorScale colorScale;
	private TableRowSubset featureTableRowSubset;
	private HeatMapDataRange heatMapDataRange;
	private FileSortingOrder fileSortingOrder;
	private SortProperty featureSortingOrder;
	private Collection<ExperimentalSample>activeSamples;
	
	public MZRTPlotParameterObject() {
		super();
	}

	public MZRTPlotParameterObject(
			Range mzRange, 
			Range rtRange, 
			DataScale dataScale,
			MSFeatureSetStatisticalParameters msFeatureSetStatisticalParameter,
			FeatureIdentificationMeasure featureIdentificationMeasure, 
			ColorGradient colorGradient,
			ColorScale colorScale, 
			TableRowSubset featureTableRowSubset) {
		super();
		this.mzRange = mzRange;
		this.rtRange = rtRange;
		this.dataScale = dataScale;
		this.msFeatureSetStatisticalParameter = msFeatureSetStatisticalParameter;
		this.featureIdentificationMeasure = featureIdentificationMeasure;
		this.colorGradient = colorGradient;
		this.colorScale = colorScale;
		this.featureTableRowSubset = featureTableRowSubset;
	}

	public MZRTPlotParameterObject(
			Range mzRange, 
			Range rtRange, 
			DataScale dataScale,
			MSFeatureSetStatisticalParameters msFeatureSetStatisticalParameter,
			FeatureIdentificationMeasure featureIdentificationMeasure, 
			ColorGradient colorGradient,
			ColorScale colorScale, 
			TableRowSubset featureTableRowSubset,
			FileSortingOrder fileSortingOrder,
			SortProperty featureSortingOrder,
			Collection<ExperimentalSample>activeSamples) {
		super();
		this.mzRange = mzRange;
		this.rtRange = rtRange;
		this.dataScale = dataScale;
		this.msFeatureSetStatisticalParameter = msFeatureSetStatisticalParameter;
		this.featureIdentificationMeasure = featureIdentificationMeasure;
		this.colorGradient = colorGradient;
		this.colorScale = colorScale;
		this.featureTableRowSubset = featureTableRowSubset;
		this.fileSortingOrder = fileSortingOrder;
		this.featureSortingOrder = featureSortingOrder;
		this.activeSamples = activeSamples;
	}
	
	public Range getMzRange() {
		return mzRange;
	}

	public void setMzRange(Range mzRange) {
		this.mzRange = mzRange;
	}

	public Range getRtRange() {
		return rtRange;
	}

	public void setRtRange(Range rtRange) {
		this.rtRange = rtRange;
	}

	public DataScale getDataScale() {
		return dataScale;
	}

	public void setDataScale(DataScale dataScale) {
		this.dataScale = dataScale;
	}

	public MSFeatureSetStatisticalParameters getMsFeatureSetStatisticalParameter() {
		return msFeatureSetStatisticalParameter;
	}

	public void setMsFeatureSetStatisticalParameter(
			MSFeatureSetStatisticalParameters msFeatureSetStatisticalParameter) {
		this.msFeatureSetStatisticalParameter = msFeatureSetStatisticalParameter;
	}

	public FeatureIdentificationMeasure getFeatureIdentificationMeasure() {
		return featureIdentificationMeasure;
	}

	public void setFeatureIdentificationMeasure(
			FeatureIdentificationMeasure featureIdentificationMeasure) {
		this.featureIdentificationMeasure = featureIdentificationMeasure;
	}

	public ColorGradient getColorGradient() {
		return colorGradient;
	}

	public void setColorGradient(ColorGradient colorGradient) {
		this.colorGradient = colorGradient;
	}

	public ColorScale getColorScale() {
		return colorScale;
	}

	public void setColorScale(ColorScale colorScale) {
		this.colorScale = colorScale;
	}

	public TableRowSubset getFeatureTableRowSubset() {
		return featureTableRowSubset;
	}

	public void setFeatureTableRowSubset(TableRowSubset featureTableRowSubset) {
		this.featureTableRowSubset = featureTableRowSubset;
	}

	public HeatMapDataRange getHeatMapDataRange() {
		return heatMapDataRange;
	}

	public void setHeatMapDataRange(HeatMapDataRange heatMapDataRange) {
		this.heatMapDataRange = heatMapDataRange;
	}

	public FileSortingOrder getFileSortingOrder() {
		return fileSortingOrder;
	}

	public void setFileSortingOrder(FileSortingOrder fileSortingOrder) {
		this.fileSortingOrder = fileSortingOrder;
	}

	public SortProperty getFeatureSortingOrder() {
		return featureSortingOrder;
	}

	public void setFeatureSortingOrder(SortProperty featureSortingOrder) {
		this.featureSortingOrder = featureSortingOrder;
	}

	public Collection<ExperimentalSample> getActiveSamples() {
		return activeSamples;
	}

	public void setActiveSamples(Collection<ExperimentalSample> activeSamples) {
		this.activeSamples = activeSamples;
	}

	
	
}
