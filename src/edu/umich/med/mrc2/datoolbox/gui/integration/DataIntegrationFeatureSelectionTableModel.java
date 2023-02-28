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

package edu.umich.med.mrc2.datoolbox.gui.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsRtLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class DataIntegrationFeatureSelectionTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -6269307946325720427L;

	public static final String ID_COLUMN = "ID";
	public static final String MERGE_COLUMN = "Sum";
	public static final String FEATURE_COLUMN = "Name";
	public static final String COMPOUND_NAME_COLUMN = "Identification";
	public static final String CHEM_MOD_LIBRARY_COLUMN = "Form (Lib)";
	public static final String ASSAY_COLUMN = "Assay";
	public static final String SCORE_COLUMN = "Score";
	public static final String RETENTION_COLUMN = "Retention";
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

	public DataIntegrationFeatureSelectionTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ID_COLUMN, Boolean.class, true),
			new ColumnContext(MERGE_COLUMN, Boolean.class, true),
			new ColumnContext(FEATURE_COLUMN, MsFeature.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, String.class, false),
			new ColumnContext(CHEM_MOD_LIBRARY_COLUMN, Adduct.class, false),
			new ColumnContext(DATA_PIPELINE_COLUMN, DataPipeline.class, false),
			new ColumnContext(SCORE_COLUMN, Double.class, false),
			new ColumnContext(RETENTION_COLUMN, Double.class, false),
			new ColumnContext(BASE_PEAK_COLUMN, Double.class, false),
			new ColumnContext(CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(POOLED_MEAN_COLUMN, Double.class, false),
			new ColumnContext(POOLED_RSD_COLUMN, Double.class, false),
			new ColumnContext(POOLED_FREQUENCY_COLUMN, Double.class, false),
			new ColumnContext(SAMPLE_MEAN_COLUMN, Double.class, false),
			new ColumnContext(SAMPLE_RSD_COLUMN, Double.class, false),
			new ColumnContext(SAMPLE_FREQUENCY_COLUMN, Double.class, false)
		};
	}

	public MsFeatureCluster getCurrentCluster() {
		return currentCluster;
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
				
				Adduct chmodLibrary = null;
				if(cf.getSpectrum() != null)
					chmodLibrary = cf.getSpectrum().getPrimaryAdduct();

				String compoundName = "";
				if(cf.getPrimaryIdentity() != null) {
					compoundName = cf.getPrimaryIdentity().getName();
					MsRtLibraryMatch msRtMatch = cf.getPrimaryIdentity().getMsRtLibraryMatch();
					if(msRtMatch != null) {

						if(msRtMatch.getTopAdductMatch() !=null)
							chmodLibrary = msRtMatch.getTopAdductMatch().getLibraryMatch();
					}
				}
				Object[] obj = {
					cf.equals(currentCluster.getPrimaryFeature()),
					entry.getValue(),
					cf,
					compoundName,
					chmodLibrary,
					entry.getKey(),
					cf.getQualityScore() / 100.0d,
					cf.getRetentionTime(),
					cf.getMonoisotopicMz(),
					cf.getAbsoluteObservedCharge(),
					cf.getStatsSummary().getPooledMean(),
					cf.getStatsSummary().getPooledRsd(),
					cf.getStatsSummary().getPooledFrequency(),
					cf.getStatsSummary().getSampleMean(),
					cf.getStatsSummary().getSampleRsd(),
					cf.getStatsSummary().getSampleFrequency()
				};
				rowData.add(obj);
			}
		}
		addRows(rowData);
	}

	/**
	 * @param currentCluster the currentCluster to set
	 */
	public void setCurrentCluster(MsFeatureCluster currentCluster) {
		this.currentCluster = currentCluster;
	}
}





















