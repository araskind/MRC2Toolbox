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

package edu.umich.med.mrc2.datoolbox.gui.integration.dpalign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsRtLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AlignedDataSetSummaryTableModel extends BasicTableModel {

	private static final long serialVersionUID = -6269307946325720427L;

	public static final String FOUND_COLUMN = "Found";
	public static final String EXPORT_FOR_REANALYSIS_COLUMN = "Export";
	public static final String REFERENCE_FEATURE_COLUMN = "Ref. feature";
	public static final String COMPOUND_NAME_COLUMN = "Identification";
	public static final String TOP_MATCH_COLUMN = "Top match";
	public static final String MERGED_COLUMN = "Merged";
	public static final String CORRELATION_COLUMN = "Corr";	
	public static final String POOLED_RSD_DIFFERENCE_COLUMN = '\u0394' + " RSD, pooled";
	public static final String SAMPLE_RSD_DIFFERENCE_COLUMN = '\u0394' + " RSD, samples";
	public static final String POOLED_AREA_DIFFERENCE_COLUMN = '\u0394' + " Area, pooled";
	public static final String SAMPLE_AREA_DIFFERENCE_COLUMN = '\u0394' + " Area, samples";
	public static final String POOLED_MISSING_DIFFERENCE_COLUMN = '\u0394' + " Missing, pooled";
	public static final String SAMPLE_MISSING_DIFFERENCE_COLUMN = '\u0394' + " Missing, samples";
	public static final String BASE_PEAK_COLUMN = "Base peak";
	public static final String DATA_PIPELINE_COLUMN = "Data pipeline";
	
	private Map<DataPipeline,Collection<MsFeature>> pipelineFeatureMap;

	public AlignedDataSetSummaryTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(FOUND_COLUMN, "Found in query data set", Boolean.class, false),
			new ColumnContext(EXPORT_FOR_REANALYSIS_COLUMN, "Export for reanalysis", Boolean.class, true),
			new ColumnContext(REFERENCE_FEATURE_COLUMN, "Reference feature", LibraryMsFeature.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, "Compound name", MsFeatureIdentity.class, false),
			new ColumnContext(TOP_MATCH_COLUMN, "Top match from query data set", LibraryMsFeature.class, false),
			new ColumnContext(MERGED_COLUMN, "Is top match a merged feature", Boolean.class, false),
			new ColumnContext(CORRELATION_COLUMN, "Top match area correlation", Double.class, false),			
			new ColumnContext(POOLED_RSD_DIFFERENCE_COLUMN, 
					"<HTML>RSD difference (%) between reference and query features"
					+ "<BR><B>Pooled</B><BR>Negative - lower RSD in query", 
					Double.class, false),
			new ColumnContext(SAMPLE_RSD_DIFFERENCE_COLUMN, 
				"<HTML>RSD difference (%) between reference and query features"
				+ "<BR><B>Samples</B><BR>Negative - lower RSD in query", 
				Double.class, false),
			new ColumnContext(POOLED_AREA_DIFFERENCE_COLUMN, 
					"<HTML>Median area difference (%) between reference and query features"
					+ "<BR><B>Pooled</B><BR>Negative - lower area in query", 
					Double.class, false),
			new ColumnContext(SAMPLE_AREA_DIFFERENCE_COLUMN, 
				"<HTML>Median area difference (%) between reference and query features"
				+ "<BR><B>Samples</B><BR>Negative - lower area in query", 
				Double.class, false),
			
			new ColumnContext(POOLED_MISSING_DIFFERENCE_COLUMN, 
					"<HTML>Percent missing difference (%) between reference and query features"
					+ "<BR><B>Pooled</B><BR>Negative - less missing in query", 
					Double.class, false),
			new ColumnContext(SAMPLE_MISSING_DIFFERENCE_COLUMN, 
					"<HTML>Percent missing difference (%) between reference and query features"
					+ "<BR><B>Samples</B><BR>Negative - less missing in query", 
					Double.class, false),
			
			new ColumnContext(BASE_PEAK_COLUMN, "Base peak M/Z", Double.class, false),
			new ColumnContext(DATA_PIPELINE_COLUMN, DATA_PIPELINE_COLUMN, DataPipeline.class, false),
		};
	}

	public void reloadData() {
		
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





















