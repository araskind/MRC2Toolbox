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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates;

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

public class DuplicateSelectionTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -82494899827528731L;
	public static final String ID_COLUMN = "ID";
//	public static final String DATA_COLUMN = "Data";
	public static final String FEATURE_COLUMN = "Name";
	public static final String COMPOUND_NAME_COLUMN = "Identification";
	public static final String CHEM_MOD_LIBRARY_COLUMN = "Form (Lib)";
	public static final String SCORE_COLUMN = "Score";
	public static final String RETENTION_COLUMN = "RT lib.";
	public static final String OBSERVED_RETENTION_COLUMN = "RT observed";
	public static final String BASE_PEAK_COLUMN = "Base peak";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String POOLED_MEAN_COLUMN = "Pooled mean";
	public static final String POOLED_RSD_COLUMN = "Pooled RSD";
	public static final String POOLED_FREQUENCY_COLUMN = "Pooled frequency";
	public static final String SAMPLE_MEAN_COLUMN = "Sample mean";
	public static final String SAMPLE_RSD_COLUMN = "Sample RSD";
	public static final String SAMPLE_FREQUENCY_COLUMN = "Sample frequency";
	public static final String DATA_PIPELINE_COLUMN = "Data pipeline";
	
	private MsFeatureCluster currentCluster;

	public DuplicateSelectionTableModel() {

		super();

		columnArray = new ColumnContext[] {
			new ColumnContext(ID_COLUMN, "Primary feature", Boolean.class, true),
//			new ColumnContext(DATA_COLUMN, Boolean.class, true),
			new ColumnContext(FEATURE_COLUMN, "Feature name", MsFeature.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, "Compound name", String.class, false),
			new ColumnContext(CHEM_MOD_LIBRARY_COLUMN, "Adduct (based on library / FbF match)", Adduct.class, false),
			new ColumnContext(SCORE_COLUMN, SCORE_COLUMN, Double.class, false),
			new ColumnContext(RETENTION_COLUMN, "Retention time from library / FbF match", Double.class, false),
			new ColumnContext(OBSERVED_RETENTION_COLUMN, "Retention time observed (median across all samples)", Double.class, false),
			new ColumnContext(BASE_PEAK_COLUMN, "Base peak M/Z", Double.class, false),
			new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(POOLED_MEAN_COLUMN, "Mean area for pooled samples", Double.class, false),
			new ColumnContext(POOLED_RSD_COLUMN, "Relative standard deviation (%) for pooled samples", Double.class, false),
			new ColumnContext(POOLED_FREQUENCY_COLUMN, "Detection frequency in pooled samples", Double.class, false),
			new ColumnContext(SAMPLE_MEAN_COLUMN, "Mean area for regular samples", Double.class, false),
			new ColumnContext(SAMPLE_RSD_COLUMN, "Relative standard deviation (%) for regular samples", Double.class, false),
			new ColumnContext(SAMPLE_FREQUENCY_COLUMN, "Detection frequency in regular samples", Double.class, false),
			new ColumnContext(DATA_PIPELINE_COLUMN, DATA_PIPELINE_COLUMN, DataPipeline.class, false)
		};
	}

	public MsFeatureCluster getCurrentCluster() {
		return currentCluster;
	}

	public void reloadData() {
		setTableModelFromFeatureCluster(currentCluster);
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster activeCluster) {

		currentCluster = activeCluster;
		setRowCount(0);
		if(currentCluster == null) 
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Entry<DataPipeline, Collection<MsFeature>> entry : currentCluster.getFeatureMap().entrySet()) {
			
			for(MsFeature cf: entry.getValue()) {

				String compoundName = "";
				if(cf.getPrimaryIdentity() != null)
					compoundName = cf.getPrimaryIdentity().getCompoundName();

				Adduct chmodLibrary = null;
				if(cf.getSpectrum() != null)
					chmodLibrary = cf.getSpectrum().getPrimaryAdduct();

				Object[] obj = {
					cf.equals(activeCluster.getPrimaryFeature()),
					//	currentCluster.isFeatureEnabled(cf),
					cf,
					compoundName,
					chmodLibrary,
					cf.getQualityScore() / 100.0d,
					cf.getRetentionTime(),
					cf.getStatsSummary().getMedianObservedRetention(),
					cf.getMonoisotopicMz(),
					cf.getAbsoluteObservedCharge(),
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
}


















