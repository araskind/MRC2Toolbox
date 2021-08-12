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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering;

import java.util.Collection;
import java.util.HashSet;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MassDifferenceTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -8939583259646221105L;

	public static final String FEATURE_ONE_COLUMN = "Feature 1";
	public static final String DATA_PIPELINE_ONE_COLUMN = "Data pipeline 1";
	public static final String BINNER_ANNOTATION_ONE_COLUMN = "Binner 1";
	public static final String FEATURE_TWO_COLUMN = "Feature 2";
	public static final String DATA_PIPELINE_TWO_COLUMN = "Data pipeline 2";
	public static final String BINNER_ANNOTATION_TWO_COLUMN = "Binner 2";
	public static final String DELTA_COLUMN = "Delta";
	public static final String DELTA_ABS_COLUMN = "Delta abs.";
	public static final String CORRELATION_COLUMN = "Correlation";
	public static final String ANNOTATION_COLUMN = "Annotation";
	public static final String ANNOTATION_DESCRIPTION_COLUMN = "Annotation description";

	public MassDifferenceTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(FEATURE_ONE_COLUMN, MsFeature.class, false),
			new ColumnContext(DATA_PIPELINE_ONE_COLUMN, DataPipeline.class, false),
			new ColumnContext(BINNER_ANNOTATION_ONE_COLUMN, String.class, false),
			new ColumnContext(FEATURE_TWO_COLUMN, MsFeature.class, false),
			new ColumnContext(DATA_PIPELINE_TWO_COLUMN, DataPipeline.class, false),
			new ColumnContext(BINNER_ANNOTATION_TWO_COLUMN, String.class, false),
			new ColumnContext(DELTA_COLUMN, Double.class, false),
			new ColumnContext(DELTA_ABS_COLUMN, Double.class, false),
			new ColumnContext(CORRELATION_COLUMN, Double.class, false),
			new ColumnContext(ANNOTATION_COLUMN, Adduct.class, false),
			new ColumnContext(ANNOTATION_DESCRIPTION_COLUMN, String.class, false)
		};
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster selectedCluster) {

		setRowCount(0);
		setTableModelFromFeatures(selectedCluster.getFeatures(), selectedCluster);
	}

	public void setTableModelFromFeatures(Collection<MsFeature> clusterFeatures, MsFeatureCluster activeCluster) {

		setRowCount(0);
		MsFeature[] features = clusterFeatures.toArray(new MsFeature[clusterFeatures.size()]);

		for (int i = 0; i < features.length; i++) {

			for (int j = 0; j < features.length; j++) {

				if (i > j) {

					double delta = 0.0d;
					double corr = 0.0d;
					HashSet<Adduct> mods = new HashSet<Adduct>();
					delta = features[j].getMonoisotopicMz() - features[i].getMonoisotopicMz();
					corr = activeCluster.getCorrelation(features[i], features[j]);
					mods = activeCluster.getModificationsForFeaturePair(features[j], features[i]);
					if (mods.isEmpty()) {

						Object[] obj = {
							features[i],
							activeCluster.getDataPipelineForFeature(features[i]),
							features[i].getBinnerAnnotation(),
							features[j],
							activeCluster.getDataPipelineForFeature(features[j]),
							features[j].getBinnerAnnotation(),
							delta,
							Math.abs(delta),
							corr,
							null,
							null
						};
						super.addRow(obj);
					} else {
						for (Adduct m : mods) {

							Object[] obj = { 
									features[i], 
									activeCluster.getDataPipelineForFeature(features[i]),
									features[i].getBinnerAnnotation(), 
									features[j],
									activeCluster.getDataPipelineForFeature(features[j]),
									features[j].getBinnerAnnotation(), 
									delta, 
									Math.abs(delta), 
									corr, 
									m,
									m.getDescription() };
							super.addRow(obj);
						}
					}
				}
			}
		}
	}
}













