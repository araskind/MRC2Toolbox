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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class FeatureDataTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 778642245738886689L;

	public static final String ORDER_COLUMN = "##";
	public static final String MS_FEATURE_COLUMN = "Feature name";
	public static final String COMPOUND_NAME_COLUMN = "Identification";
	public static final String DATABSE_LINK_COLUMN = "DBID";
	public static final String AMBIGUITY_COLUMN = "Ambig.";
	public static final String CHEM_MOD_OBSERVED_COLUMN = "Form (Obs)";
	public static final String CHEM_MOD_LIBRARY_COLUMN = "Form (Lib)";
	public static final String REJECT_COLUMN = "Reject";
	public static final String SCORE_COLUMN = "Score";
	public static final String RETENTION_COLUMN = "RT lib";
	public static final String RETENTION_OBSERVED_MEDIAN_COLUMN = "RT med";
	public static final String NEUTRAL_MASS_COLUMN = "Neutral mass";
	public static final String MONOISOTOPIC_PEAK_COLUMN = "Monoisotopic peak";
	public static final String MCMILLAN_PERCENT_DELTA_COLUMN = "McM %"+'\u0394';
	public static final String KMD_COLUMN = "KMD";
	public static final String KMD_MOD_COLUMN = "KMD mod";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String POOLED_MEAN_COLUMN = "Pool mean";
	public static final String POOLED_MEDIAN_COLUMN = "Pool median";
	public static final String POOLED_RSD_COLUMN = "Pool RSD";
	public static final String POOLED_FREQUENCY_COLUMN = "Pool freq.";
	public static final String SAMPLE_MEAN_COLUMN = "Sample mean";
	public static final String SAMPLE_MEDIAN_COLUMN = "Sample median";
	public static final String SAMPLE_RSD_COLUMN = "Sample RSD";
	public static final String SAMPLE_FREQUENCY_COLUMN = "Sample freq.";
	public static final String DATA_PIPELINE_COLUMN = "Data pipeline";

	public FeatureDataTableModel() {
		super();
		columnArray = new ColumnContext[] {

				new ColumnContext(ORDER_COLUMN, Integer.class, false),
				new ColumnContext(MS_FEATURE_COLUMN, MsFeature.class, false),
				new ColumnContext(COMPOUND_NAME_COLUMN, String.class, false),
				new ColumnContext(DATABSE_LINK_COLUMN, MsFeatureIdentity.class, false),
				new ColumnContext(AMBIGUITY_COLUMN, Boolean.class, false),
				new ColumnContext(CHEM_MOD_OBSERVED_COLUMN, Adduct.class, false),
				new ColumnContext(CHEM_MOD_LIBRARY_COLUMN, Adduct.class, false),
				new ColumnContext(RETENTION_COLUMN, Double.class, false),
				new ColumnContext(RETENTION_OBSERVED_MEDIAN_COLUMN, Double.class, false),
				new ColumnContext(NEUTRAL_MASS_COLUMN, Double.class, false),
				new ColumnContext(MONOISOTOPIC_PEAK_COLUMN, Double.class, false),
				new ColumnContext(MCMILLAN_PERCENT_DELTA_COLUMN, Double.class, false),			
				new ColumnContext(KMD_COLUMN, Double.class, false),
				new ColumnContext(KMD_MOD_COLUMN, Double.class, false),
				new ColumnContext(CHARGE_COLUMN, Integer.class, false),
				new ColumnContext(POOLED_MEAN_COLUMN, Double.class, false),
				new ColumnContext(POOLED_RSD_COLUMN, Double.class, false),
				new ColumnContext(POOLED_FREQUENCY_COLUMN, Double.class, false),
				new ColumnContext(SAMPLE_MEAN_COLUMN, Double.class, false),
				new ColumnContext(SAMPLE_RSD_COLUMN, Double.class, false),
				new ColumnContext(SAMPLE_FREQUENCY_COLUMN, Double.class, false),
				new ColumnContext(DATA_PIPELINE_COLUMN, DataPipeline.class, false)
			};
	}

	public void setTableModelFromFeatureMap(Map<DataPipeline, Collection<MsFeature>> featureMap) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		int count = 1;
		for (Entry<DataPipeline, Collection<MsFeature>> entry : featureMap.entrySet()) {
			
			List<MsFeature>sortedFeatures = 
					entry.getValue().stream().
					sorted(new MsFeatureComparator(SortProperty.Name)).
					collect(Collectors.toList());
			
			for (MsFeature cf : sortedFeatures) {

				Double bp = null;
				Integer charge = null;
				Adduct chmodObserved = null;
				Adduct chmodLibrary = null;
				String compoundName = "";
				Double mcMillanDelta = null;
				if (cf.getSpectrum() != null) {

					bp = cf.getMonoisotopicMz();
					charge = cf.getCharge();
					chmodObserved = cf.getSpectrum().getPrimaryAdduct();
					mcMillanDelta = cf.getSpectrum().getMcMillanCutoffPercentDelta();
				}
				if(cf.getPrimaryIdentity() != null) {
					compoundName = cf.getPrimaryIdentity().getIdentityName();
					if(cf.getPrimaryIdentity().getMsRtLibraryMatch() != null)
					chmodLibrary = cf.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch().getLibraryMatch();
				}
				boolean ambig = cf.getIdentifications().size() > 1;
				Object[] obj = {
						count,
						cf,
						compoundName,
						cf.getPrimaryIdentity(),
						ambig,
						chmodObserved,
						chmodLibrary,
						cf.getRetentionTime(),
						cf.getStatsSummary().getMedianObservedRetention(),
						cf.getNeutralMass(),
						bp,
						mcMillanDelta,
						cf.getKmd(),
						cf.getModifiedKmd(),
						charge,
						cf.getStatsSummary().getPooledMean(),
						cf.getStatsSummary().getPooledRsd(),
						cf.getStatsSummary().getPooledFrequency(),
						cf.getStatsSummary().getSampleMean(),
						cf.getStatsSummary().getSampleRsd(),
						cf.getStatsSummary().getSampleFrequency(),
						entry.getKey()
				};
				rowData.add(obj);
				count++;
			}
		}
		addRows(rowData);
	}

	public void updateFeatureData(MsFeature cf) {

		int row = getFeatureRow(cf);
		if(row == -1)
			return;

		int count = (int) getValueAt(row, getColumnIndex(ORDER_COLUMN));
		setValueAt(count, row, getColumnIndex(ORDER_COLUMN));
		Double bp = null;
		Integer charge = null;
		Adduct chmodObserved = null;
		Adduct chmodLibrary = null;
		String compoundName = "";

		if (cf.getSpectrum() != null) {

			bp = cf.getMonoisotopicMz();
			charge = cf.getCharge();
			chmodObserved = cf.getSpectrum().getPrimaryAdduct();
		}
		if(cf.getPrimaryIdentity() != null) {
			compoundName = cf.getPrimaryIdentity().getIdentityName();
			if(cf.getPrimaryIdentity().getMsRtLibraryMatch() != null)
			chmodLibrary = cf.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch().getLibraryMatch();
		}
		boolean ambig = cf.getIdentifications().size() > 1;

		setValueAt(cf, row, getColumnIndex(MS_FEATURE_COLUMN));
		setValueAt(compoundName, row, getColumnIndex(COMPOUND_NAME_COLUMN));
		setValueAt(cf.getPrimaryIdentity(), row, getColumnIndex(DATABSE_LINK_COLUMN));
		setValueAt(ambig, row, getColumnIndex(AMBIGUITY_COLUMN));
		setValueAt(chmodObserved, row, getColumnIndex(CHEM_MOD_OBSERVED_COLUMN));
		setValueAt(chmodLibrary, row, getColumnIndex(CHEM_MOD_LIBRARY_COLUMN));
		setValueAt(cf.getRetentionTime(), row, getColumnIndex(RETENTION_COLUMN));
		setValueAt(cf.getNeutralMass(), row, getColumnIndex(NEUTRAL_MASS_COLUMN));
		setValueAt(bp, row, getColumnIndex(MONOISOTOPIC_PEAK_COLUMN));
		setValueAt(cf.getKmd(), row, getColumnIndex(KMD_COLUMN));
		setValueAt(cf.getModifiedKmd(), row, getColumnIndex(KMD_MOD_COLUMN));
		setValueAt(charge, row, getColumnIndex(CHARGE_COLUMN));
		setValueAt(cf.getStatsSummary().getPooledMean(), row, getColumnIndex(POOLED_MEAN_COLUMN));
		setValueAt(cf.getStatsSummary().getPooledRsd(), row, getColumnIndex(POOLED_RSD_COLUMN));
		setValueAt(cf.getStatsSummary().getPooledFrequency(), row, getColumnIndex(POOLED_FREQUENCY_COLUMN));
		setValueAt(cf.getStatsSummary().getSampleMean(), row, getColumnIndex(SAMPLE_MEAN_COLUMN));
		setValueAt(cf.getStatsSummary().getSampleRsd(), row, getColumnIndex(SAMPLE_RSD_COLUMN));
		setValueAt(cf.getStatsSummary().getSampleFrequency(), row, getColumnIndex(SAMPLE_FREQUENCY_COLUMN));
	}

	public int getFeatureRow(MsFeature feature) {

		int col = getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (feature.equals((MsFeature)getValueAt(i, col)))
				return i;
		}
		return -1;
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster selectedCluster) {
		setTableModelFromFeatureMap(selectedCluster.getFeatureMap());
	}
}










