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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class ClusterFeatureSelectionTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 128352519706043995L;

	public static final String PRIMARY_COLUMN = "Primary";
	public static final String INCLUDE_COLUMN = "Include";
	public static final String SCORE_COLUMN = "Score";
	public static final String FEATURE_COLUMN = "Name";
	public static final String COMPOUND_NAME_COLUMN = "Identification";
	public static final String ANNOTATION_COLUMN = "Annotation";
	public static final String CHEM_MOD_COLUMN = "Form";
	public static final String RETENTION_COLUMN = "RT";
	public static final String NEUTRAL_MASS_COLUMN = "Neutral mass";
	public static final String BASE_PEAK_COLUMN = "Base peak";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String KMD_COLUMN = "KMD";
	public static final String KMD_MOD_COLUMN = "KMD mod";
	public static final String POOLED_MEAN_COLUMN = "Pooled mean";
	public static final String POOLED_RSD_COLUMN = "Pooled RSD";
	public static final String POOLED_FREQUENCY_COLUMN = "Pooled frequency";
	public static final String SAMPLE_MEAN_COLUMN = "Sample median";
	public static final String SAMPLE_RSD_COLUMN = "Sample RSD";
	public static final String SAMPLE_FREQUENCY_COLUMN = "Sample frequency";
	public static final String DATA_PIPELINE_COLUMN = "Data pipeline";

	private MsFeatureCluster currentCluster;

	//	TODO modify to deal with multiple data pipelines?
	public ClusterFeatureSelectionTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(PRIMARY_COLUMN, PRIMARY_COLUMN, Boolean.class, true),
			new ColumnContext(INCLUDE_COLUMN, INCLUDE_COLUMN, Boolean.class, true),
			new ColumnContext(SCORE_COLUMN, SCORE_COLUMN, Double.class, false),
			new ColumnContext(FEATURE_COLUMN, "Feature name", MsFeature.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, "Compound name", String.class, false),
			new ColumnContext(ANNOTATION_COLUMN, ANNOTATION_COLUMN, String.class, false),
			new ColumnContext(CHEM_MOD_COLUMN, "Adduct", Adduct.class, false),
			new ColumnContext(RETENTION_COLUMN, "Retention time", Double.class, false),
			new ColumnContext(NEUTRAL_MASS_COLUMN, NEUTRAL_MASS_COLUMN, Double.class, false),
			new ColumnContext(BASE_PEAK_COLUMN, "Base peak M/Z", Double.class, false),
			new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(KMD_COLUMN, "Kendrick mass defect", Double.class, false),
			new ColumnContext(KMD_MOD_COLUMN, "Kendrick mass defect, modified", Double.class, false),
			new ColumnContext(POOLED_MEAN_COLUMN, "Mean area for pooled samples", Double.class, false),
			new ColumnContext(POOLED_RSD_COLUMN, "Relative standard deviation (%) for pooled samples", Double.class, false),
			new ColumnContext(POOLED_FREQUENCY_COLUMN, "Detection frequency in pooled samples", Double.class, false),
			new ColumnContext(SAMPLE_MEAN_COLUMN, "Mean area for regular samples", Double.class, false),
			new ColumnContext(SAMPLE_RSD_COLUMN, "Relative standard deviation (%) for regular samples", Double.class, false),
			new ColumnContext(SAMPLE_FREQUENCY_COLUMN, "Detection frequency in regular samples", Double.class, false),
			new ColumnContext(DATA_PIPELINE_COLUMN, DATA_PIPELINE_COLUMN, DataPipeline.class, false)
		};
	}

	public void reloadData() {
		setTableModelFromFeatureCluster(currentCluster);
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster featureCluster) {

		currentCluster = featureCluster;
		setRowCount(0);
		if(currentCluster == null) 
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Entry<DataPipeline, Collection<MsFeature>> entry : currentCluster.getFeatureMap().entrySet()) {
			
			for(MsFeature cf : entry.getValue()) {
				
				String compoundName = "";
				if(cf.getPrimaryIdentity() != null)
					compoundName = cf.getPrimaryIdentity().getCompoundName();
				
				String binAnnot = "";
				if( cf.getBinnerAnnotation() != null)
					binAnnot = cf.getBinnerAnnotation().getAnnotation();

				Object[] obj = {
					cf.equals(currentCluster.getPrimaryFeature()),
					entry.getValue(),
					cf.getQualityScore(),
					cf,
					compoundName,
					binAnnot,
					cf.getDefaultChemicalModification(),
					cf.getRetentionTime(),
					cf.getNeutralMass(),
					cf.getMonoisotopicMz(),
					cf.getAbsoluteObservedCharge(),
					cf.getKmd(),
					cf.getModifiedKmd(),
					cf.getStatsSummary().getPooledMean(),
					cf.getStatsSummary().getPooledRsd(),
					cf.getStatsSummary().getPooledFrequency(),
					cf.getStatsSummary().getSampleMean(),
					cf.getStatsSummary().getSampleRsd(),
					cf.getStatsSummary().getSampleFrequency(),
					entry.getKey()
				};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);	
	}

	public MsFeatureCluster getCurrentCluster() {
		return currentCluster;
	}
}

















