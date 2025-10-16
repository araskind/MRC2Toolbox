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

package edu.umich.med.mrc2.datoolbox.gui.integration.dpalign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsRtLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AlignedDataSetFeatureTableModel extends BasicTableModel {

	private static final long serialVersionUID = -6269307946325720427L;

	public static final String DATA_PIPELINE_COLUMN = "Data pipeline";
	public static final String FEATURE_COLUMN = "Name";
	public static final String COMPOUND_NAME_COLUMN = "Identification";
	public static final String CHEM_MOD_LIBRARY_COLUMN = "Form (Lib)";
	public static final String SCORE_COLUMN = "Score";
	public static final String RETENTION_COLUMN = "RT";
	public static final String BASE_PEAK_COLUMN = "Base peak";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String POOLED_MEAN_COLUMN = "Pooled mean";
	public static final String POOLED_RSD_COLUMN = "Pooled RSD";
	public static final String POOLED_FREQUENCY_COLUMN = "Pooled frequency";
	public static final String SAMPLE_MEAN_COLUMN = "Sample mean";
	public static final String SAMPLE_RSD_COLUMN = "Sample RSD";
	public static final String SAMPLE_FREQUENCY_COLUMN = "Sample frequency";
	
	private Map<DataPipeline,Collection<MsFeature>> pipelineFeatureMap;

	public AlignedDataSetFeatureTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(DATA_PIPELINE_COLUMN, DATA_PIPELINE_COLUMN, DataPipeline.class, false),
			new ColumnContext(FEATURE_COLUMN, "Feature name", MsFeature.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, "Compound name", String.class, false),
			new ColumnContext(CHEM_MOD_LIBRARY_COLUMN, "Adduct (based on library /FbF assignment)", Adduct.class, false),			
			new ColumnContext(SCORE_COLUMN, SCORE_COLUMN, Double.class, false),
			new ColumnContext(RETENTION_COLUMN, "Retention time", Double.class, false),
			new ColumnContext(BASE_PEAK_COLUMN, "Base peak M/Z", Double.class, false),
			new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(POOLED_MEAN_COLUMN, "Mean area for pooled samples", Double.class, false),
			new ColumnContext(POOLED_RSD_COLUMN, "Relative standard deviation (%) for pooled samples", Double.class, false),
			new ColumnContext(POOLED_FREQUENCY_COLUMN, "Detection frequency in pooled samples", Double.class, false),
			new ColumnContext(SAMPLE_MEAN_COLUMN, "Mean area for regular samples", Double.class, false),
			new ColumnContext(SAMPLE_RSD_COLUMN, "Relative standard deviation (%) for regular samples", Double.class, false),
			new ColumnContext(SAMPLE_FREQUENCY_COLUMN, "Detection frequency in regular samples", Double.class, false)
		};
	}

	public void reloadData() {
		setTableModelFromFeatureMap(pipelineFeatureMap);
	}

	public void setTableModelFromFeatureMap(Map<DataPipeline,Collection<MsFeature>> featureMap) {

		this.pipelineFeatureMap = featureMap;
		setRowCount(0);
		if(pipelineFeatureMap == null || pipelineFeatureMap.isEmpty()) 
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Entry<DataPipeline, Collection<MsFeature>> entry : pipelineFeatureMap.entrySet()) {
			
			for(MsFeature cf : entry.getValue()) {
				
				Adduct chmodLibrary = null;
				if(cf.getSpectrum() != null)
					chmodLibrary = cf.getSpectrum().getPrimaryAdduct();

				String compoundName = "";
				if(cf.getPrimaryIdentity() != null) {
					compoundName = cf.getPrimaryIdentity().getCompoundName();
					MsRtLibraryMatch msRtMatch = cf.getPrimaryIdentity().getMsRtLibraryMatch();
					if(msRtMatch != null && msRtMatch.getTopAdductMatch() !=null)
						chmodLibrary = msRtMatch.getTopAdductMatch().getLibraryMatch();					
				}
				Object[] obj = {
						entry.getKey(), 							//DATA_PIPELINE_COLUMN
						cf, 										//FEATURE_COLUMN
						compoundName, 								//COMPOUND_NAME_COLUMN
						chmodLibrary, 								//CHEM_MOD_LIBRARY_COLUMN
						cf.getQualityScore() / 100.0d, 				//SCORE_COLUMN
						cf.getRetentionTime(), 						//RETENTION_COLUMN
						cf.getMonoisotopicMz(), 					//BASE_PEAK_COLUMN
						cf.getAbsoluteObservedCharge(), 			//CHARGE_COLUMN
						cf.getStatsSummary().getPooledMean(), 		//POOLED_MEAN_COLUMN
						cf.getStatsSummary().getPooledRsd(), 		//POOLED_RSD_COLUMN
						cf.getStatsSummary().getPooledFrequency(), 	//POOLED_FREQUENCY_COLUMN
						cf.getStatsSummary().getSampleMean(), 		//SAMPLE_MEAN_COLUMN
						cf.getStatsSummary().getSampleRsd(), 		//SAMPLE_RSD_COLUMN
						cf.getStatsSummary().getSampleFrequency() 	//SAMPLE_FREQUENCY_COLUMN
				};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}





















