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
import java.util.List;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentParametersObject;
import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.NormalizationUtils;

public class AlignedDataSetSummaryTableModel extends BasicTableModel {

	private static final long serialVersionUID = -6269307946325720427L;

	public static final String ORDER_COLUMN = "##";
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

	public AlignedDataSetSummaryTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(ORDER_COLUMN, ORDER_COLUMN, Integer.class, false),
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

	public void setTableModelFromAlignmentResults(
			DataPipelineAlignmentResults alignmentResults, CompoundLibrary avgLib) {

		setRowCount(0);
		if(alignmentResults == null 
				|| alignmentResults.getAlignmentSettings() == null || avgLib == null) 
			return;
		
		List<Object[]>rowData = new ArrayList<>();
		Set<MsFeatureCluster> clusters = alignmentResults.getClusters();
		DataPipelineAlignmentParametersObject params = alignmentResults.getAlignmentSettings();
		for(LibraryMsFeature refLibFeature : avgLib.getFeatures()) {
				
			MsFeatureCluster cluster = clusters.stream().
					filter(c -> c.getPrimaryFeature().equals(refLibFeature)).
					findFirst().orElse(null);
			Object[] obj = generateRowData(refLibFeature, cluster, params);
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	private Object[]generateRowData(
			LibraryMsFeature refLibFeature, 
			MsFeatureCluster cluster, 
			DataPipelineAlignmentParametersObject params){
		
		CompoundIdentity cid = refLibFeature.isIdentified() ? refLibFeature.getPrimaryIdentity().getCompoundIdentity() : null;
		
		LibraryMsFeature topMatch = null;
		boolean merged = false;
		Double correlation = null;
		
		//	TODO calculate values
		Double rsdDifferencePooled = null;
		Double rsdDifferenceSample = null;
		Double areaDifferencePooled = null;
		Double areaDifferenceSample = null;
		Double missingDifferencePooled = null;
		Double missingDifferenceSample = null;
		
		DataPipeline pipeline = params.getReferencePipeline();
		
		if(cluster != null) {
			topMatch = (LibraryMsFeature)cluster.getTopMatchFeature();
			if(topMatch != null) {
				
				merged = topMatch.isMerged();				
				//	correlation = cluster.getCorrelation(refLibFeature, topMatch);
				pipeline = params.getQueryPipeline();
				
				rsdDifferencePooled = NormalizationUtils.calculateRelativeChange(
						topMatch.getStatsSummary().getPooledRsd(), refLibFeature.getStatsSummary().getPooledRsd());
				rsdDifferenceSample = NormalizationUtils.calculateRelativeChange(
						topMatch.getStatsSummary().getSampleRsd(), refLibFeature.getStatsSummary().getSampleRsd());				
				areaDifferencePooled = NormalizationUtils.calculateRelativeChange(
						topMatch.getStatsSummary().getPooledMedian(), refLibFeature.getStatsSummary().getPooledMedian());
				areaDifferenceSample = NormalizationUtils.calculateRelativeChange(
						topMatch.getStatsSummary().getSampleMedian(), refLibFeature.getStatsSummary().getSampleMedian());
				missingDifferencePooled = NormalizationUtils.calculateRelativeChange(
						topMatch.getStatsSummary().getPooledMissingness(), refLibFeature.getStatsSummary().getPooledMissingness());
				missingDifferenceSample = NormalizationUtils.calculateRelativeChange(
						topMatch.getStatsSummary().getSampleMissingness(), refLibFeature.getStatsSummary().getSampleMissingness());
			}
		}
		return new Object[]{
			1,							//	ORDER
			cluster != null,			//	FOUND
			false,						//	EXPORT_FOR_REANALYSIS
			refLibFeature,				//	REFERENCE_FEATURE
			cid,						//	COMPOUND_NAME
			topMatch,					//	TOP_MATCH
			merged,						//	MERGED
			correlation,				//	CORRELATION
			rsdDifferencePooled,		//	POOLED_RSD_DIFFERENCE_COLUMN
			rsdDifferenceSample,		//	SAMPLE_RSD_DIFFERENCE_COLUMN
			areaDifferencePooled,		//	POOLED_AREA_DIFFERENCE_COLUMN
			areaDifferenceSample,		//	SAMPLE_AREA_DIFFERENCE_COLUMN
			missingDifferencePooled,	//	POOLED_MISSING_DIFFERENCE_COLUMN
			missingDifferenceSample,	//	SAMPLE_MISSING_DIFFERENCE_COLUMN
			refLibFeature.getSpectrum().getBasePeakMz(),	//	BASE_PEAK
			pipeline,					//DATA_PIPELINE
		};		
	}
}





















