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

package edu.umich.med.mrc2.datoolbox.gui.tables;

import java.util.Collection;
import java.util.OptionalDouble;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PieChartFrequencyRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RelativeIntensityRenderer;

public abstract class FeatureSelectionTable extends BasicFeatureTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5240469608680267353L;

	protected TableRowSorter<?> featureSorter;
	protected PieChartFrequencyRenderer pieChartFrequencyRenderer;
	protected RelativeIntensityRenderer pooledMeanRenderer;
	protected RelativeIntensityRenderer sampleMeanRenderer;
	protected final int preferredWidth = 110;

	public FeatureSelectionTable() {

		super();
		pieChartFrequencyRenderer = new PieChartFrequencyRenderer(0,0);
		pooledMeanRenderer = new RelativeIntensityRenderer();
		sampleMeanRenderer = new RelativeIntensityRenderer();
	}

	protected void setrendererReferences(Collection<MsFeature>features) {

		pooledMeanRenderer.setMaxValue(0.0d);
		sampleMeanRenderer.setMaxValue(0.0d);;

		OptionalDouble maxPooledMean = 
				features.stream().mapToDouble(f -> f.getStatsSummary().getPooledMean()).max();
		OptionalDouble maxSampleMean = 
				features.stream().mapToDouble(f -> f.getStatsSummary().getSampleMean()).max();

		if(maxPooledMean.isPresent())
			pooledMeanRenderer.setMaxValue(maxPooledMean.getAsDouble());

		if(maxSampleMean.isPresent())
			sampleMeanRenderer.setMaxValue(maxSampleMean.getAsDouble());
	}

	public abstract void setTableModelFromFeatureCluster(MsFeatureCluster featureCluster);
}
