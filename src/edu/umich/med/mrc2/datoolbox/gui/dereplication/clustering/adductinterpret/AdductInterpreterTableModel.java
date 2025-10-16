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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.adductinterpret;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class AdductInterpreterTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 2552136017748898903L;

	public static final String FEATURE_COLUMN = "Name";
	public static final String RETENTION_COLUMN = "Retention";
	public static final String BASE_PEAK_COLUMN = "Base peak";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String POOLED_MEAN_COLUMN = "Pooled mean";
	public static final String SAMPLE_MEAN_COLUMN = "Sample mean";
	public static final String KMD_COLUMN = "KMD";
	public static final String ANNOTATION_COLUMN = "Binner annotation";
	public static final String CHEM_MOD_COLUMN = "Form";
	public static final String CHEM_MOD_ERROR = "Error, ppm";
	public static final String SUGGESTED_CHEM_MOD_COLUMN = "Suggested";
	public static final String SUGGESTED_CHEM_MOD_AMBIGUOUS_COLUMN = "Ambiguous";
	public static final String SUGGESTED_CHEM_MOD_ERROR = "Error 2, ppm";
	public static final String ACCEPT_CHEM_MOD_COLUMN = "Accept";
	public static final String DATA_PIPELINE_COLUMN = "Data pipeline";

	private MsFeatureCluster currentCluster;
	private double neutralMass;

	public AdductInterpreterTableModel() {
		super();
		columnArray = new ColumnContext[] {
				new ColumnContext(FEATURE_COLUMN, "Feature name", MsFeature.class, false),
				new ColumnContext(RETENTION_COLUMN, "Retention time", Double.class, false),
				new ColumnContext(BASE_PEAK_COLUMN, "Base peak M/Z", Double.class, false),
				new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),
				new ColumnContext(POOLED_MEAN_COLUMN, "Pooled samples mean area", Double.class, false),
				new ColumnContext(SAMPLE_MEAN_COLUMN, "Regular samples mean area", Double.class, false),
				new ColumnContext(KMD_COLUMN, "Kendrick mass defect", Double.class, false),
				new ColumnContext(ANNOTATION_COLUMN, ANNOTATION_COLUMN, String.class, false),
				new ColumnContext(CHEM_MOD_COLUMN, "Assigned adduct", Adduct.class, false),
				new ColumnContext(CHEM_MOD_ERROR, "Mass error relative to calculated value, ppm", Double.class, false),
				new ColumnContext(SUGGESTED_CHEM_MOD_COLUMN, "Suggested adduct", Adduct.class, true),
				new ColumnContext(SUGGESTED_CHEM_MOD_AMBIGUOUS_COLUMN, "Is suggested adduct ambiguous (more than one option)?", Boolean.class, false),
				new ColumnContext(SUGGESTED_CHEM_MOD_ERROR, "Mass error relative to calculated value for suggested adduct, ppm", Double.class, false),
				new ColumnContext(ACCEPT_CHEM_MOD_COLUMN, "Accept suggested adduct", Boolean.class, true),
				new ColumnContext(DATA_PIPELINE_COLUMN, DATA_PIPELINE_COLUMN, DataPipeline.class, false)
		};
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster featureCluster) {

		setRowCount(0);
		currentCluster = featureCluster;
		if (currentCluster != null)
			setTableModelFromFeatureList(
					featureCluster.getFeatureMap(), 
					featureCluster.getAnnotationMap());
	}

	public void setTableModelFromFeatureCluster(
			MsFeatureCluster featureCluster, 
			MsFeature referenceFeature,
			Adduct referenceModification) {

		currentCluster = featureCluster;
		if (currentCluster != null)
			setTableModelFromFeatureList(
					featureCluster.getFeatureMap(), 
					featureCluster.getAnnotationMap(),
					referenceFeature, 
					referenceModification);
	}

	public void setTableModelFromFeatureList(
			Map<DataPipeline, Collection<MsFeature>>featureMap, 
			Map<MsFeature, Set<Adduct>> chemModMap) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
				
		//	Calculate neutral mass using primary modification
		MsFeature primary = currentCluster.getPrimaryFeature();
		for (MsFeature cf : currentCluster.getFeatures()) {

			if (cf.equals(currentCluster.getPrimaryFeature()) && cf.getDefaultChemicalModification() != null)
				neutralMass = MsUtils.calculateNeutralMass(cf.getMonoisotopicMz(), cf.getDefaultChemicalModification());
		}
		for (Entry<DataPipeline, Collection<MsFeature>> entry : featureMap.entrySet()) {
			
			for (MsFeature cf : entry.getValue()) {

				double currentError = 0.0d;
				double suggestedError = 0.0d;
				boolean ambiguous = false;

				if (cf.getDefaultChemicalModification() != null && neutralMass > 0) {

					double modNeutral = MsUtils.calculateNeutralMass(cf.getMonoisotopicMz(),
							cf.getDefaultChemicalModification());
					currentError = (modNeutral - neutralMass) / neutralMass * 1000000.0d;
				}

				Adduct selected = null;

				if (chemModMap != null) {

					if (!chemModMap.isEmpty()) {

						selected = cf.getSuggestedModification();

						if (selected != null && neutralMass > 0) {

							double modNeutralSuggested = MsUtils.calculateNeutralMass(cf.getMonoisotopicMz(),
									selected);
							suggestedError = (modNeutralSuggested - neutralMass) / neutralMass * 1000000.0d;
						}
						if(chemModMap.get(cf).size() > 1)
							ambiguous = true;
					}
				}
				Object[] obj = {
					cf,
					cf.getRetentionTime(),
					cf.getMonoisotopicMz(),
					cf.getAbsoluteObservedCharge(),
					cf.getStatsSummary().getPooledMean(),
					cf.getStatsSummary().getSampleMean(),
					cf.getKmd(),
					cf.getBinnerAnnotation(),
					cf.getDefaultChemicalModification(),
					currentError,
					cf,	// will display suggested modification and list of possible modifications
					ambiguous,
					suggestedError,
					true,
					entry.getKey()
				};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	private void setTableModelFromFeatureList(
			Map<DataPipeline, Collection<MsFeature>>featureMap,
			Map<MsFeature, 
			Set<Adduct>> modificationMap, 
			MsFeature referenceFeature,
			Adduct referenceModification) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		neutralMass = MsUtils.calculateNeutralMass(
				referenceFeature.getMonoisotopicMz(),
				referenceModification);
		
		for (Entry<DataPipeline, Collection<MsFeature>> entry : featureMap.entrySet()) {
			
			for (MsFeature cf : entry.getValue()) {

				double currentError = 0.0d;
				double suggestedError = 0.0d;
				boolean ambiguous = false;

				if (cf.getDefaultChemicalModification() != null) {

					double expected = MsUtils.calculateModifiedMz(neutralMass, cf.getDefaultChemicalModification());
					currentError = (expected - cf.getMonoisotopicMz()) / expected * 1000000.0d;
				}

				Adduct selected = null;

				if (modificationMap != null) {

					selected = cf.getSuggestedModification();

					if (selected != null) {

						double suggested = MsUtils.calculateModifiedMz(neutralMass, selected);
						suggestedError = (suggested - cf.getMonoisotopicMz()) / suggested * 1000000.0d;
					}
					if(modificationMap.get(cf).size() > 1)
						ambiguous = true;
				}

				Object[] obj = {
					cf,
					cf.getRetentionTime(),
					cf.getMonoisotopicMz(),
					cf.getAbsoluteObservedCharge(),
					cf.getStatsSummary().getPooledMean(),
					cf.getStatsSummary().getSampleMean(),
					cf.getKmd(),
					cf.getBinnerAnnotation(),
					cf.getDefaultChemicalModification(),
					currentError,
					cf,	// will display suggested modification and list of possible modifications
					ambiguous,
					suggestedError,
					true,
					entry.getKey()
				};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {

		if(col == getColumnIndex(SUGGESTED_CHEM_MOD_COLUMN)){

			Adduct selected = (Adduct)value;
			if(selected != null){

				MsFeature cf = (MsFeature) this.getValueAt(row, col);
				double expected = MsUtils.calculateModifiedMz(neutralMass, selected);
				double currentError = (expected - cf.getMonoisotopicMz()) / expected * 1000000.0d;
				cf.setSuggestedModification(selected);
				super.setValueAt(currentError, row, getColumnIndex(SUGGESTED_CHEM_MOD_ERROR));
			}
		}
		else
			super.setValueAt(value, row, col);
	}
}
