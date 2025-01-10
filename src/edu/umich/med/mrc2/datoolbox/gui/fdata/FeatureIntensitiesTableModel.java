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

package edu.umich.med.mrc2.datoolbox.gui.fdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FeatureIntensitiesTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 6061574090814938901L;

	public static final String ORDER_COLUMN = "##";
	public static final String MS_FEATURE_COLUMN = "Feature";
	public static final String SAMPLE_COLUMN = "Sample";
	public static final String AREA_COLUMN = "Area";
	public static final String IMPUTED_AREA_COLUMN = "Area with imputation";
	public static final String FEATURE_QUALITY_COLUMN = "Quality score";

	public FeatureIntensitiesTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ORDER_COLUMN, "Order number", Integer.class, true),
			new ColumnContext(MS_FEATURE_COLUMN, "Feature name", MsFeature.class, false),
			new ColumnContext(SAMPLE_COLUMN, "Sample name", DataFile.class, true),
			new ColumnContext(AREA_COLUMN, "Peak area", Double.class, false),
			//	new ColumnContext(IMPUTED_AREA_COLUMN, Double.class, false)
			//	new ColumnContext(FEATURE_QUALITY_COLUMN, "Peak area", Double.class, false),
		};
	}

	public void setTableModelFromFeatureAndPipeline(MsFeature cf, DataPipeline pipeline) {

		setRowCount(0);
		Map<DataPipeline, Collection<MsFeature>> featureMap = 
				new TreeMap<DataPipeline, Collection<MsFeature>>();
		featureMap.put(pipeline, Collections.singleton(cf));
		setTableModelFromFeatureMap(featureMap);
	}

	public void setTableModelFromFeatureMap(Map<DataPipeline, Collection<MsFeature>> featureMap) {

		setRowCount(0);
		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(project == null)
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		long[] coordinates = new long[2];
		long[] nameCoordinates = new long[2];
		int rowCount = 1;
		
		for (Entry<DataPipeline, Collection<MsFeature>> entry : featureMap.entrySet()) {
			
			DataPipeline dp = entry.getKey();
			Matrix dataMatrix = project.getDataMatrixForDataPipeline(dp);
			Matrix imputedDataMatrix = project.getImputedDataMatrixForDataPipeline(dp);
			Matrix sampleNameMatrix = dataMatrix.getMetaDataDimensionMatrix(1);
			for (MsFeature cf : entry.getValue()) {

				if(project.getActiveFeatureSetForDataPipeline(dp).getFeatures().contains(cf)) {

					coordinates[1] = dataMatrix.getColumnForLabel(cf);
					nameCoordinates[1] = 0;

					for (long i = 0; i < dataMatrix.getRowCount(); i++) {

						coordinates[0] = i;
						nameCoordinates[0] = i;
						double value  = dataMatrix.getAsDouble(coordinates);
						
//						double iputed = value;
//						if(imputedDataMatrix != null)
//							iputed = imputedDataMatrix.getAsDouble(coordinates);

//						Double qualityScore = null;
//						if(cf.getPrimaryIdentity() != null && cf.getPrimaryIdentity().getScoreCarryOver() > 0)
//							qualityScore = cf.getPrimaryIdentity().getScoreCarryOver();
							
						Object[] obj = {
								rowCount,
								cf,
								sampleNameMatrix.getAsObject(nameCoordinates),
								value,
								//	iputed
								//	qualityScore,
							};
						rowData.add(obj);
						rowCount++;
					}
				}
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}























