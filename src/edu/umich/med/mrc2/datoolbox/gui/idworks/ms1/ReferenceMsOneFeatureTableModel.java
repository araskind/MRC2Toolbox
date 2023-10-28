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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIdentificationState;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.gui.fdata.FeatureDataTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class ReferenceMsOneFeatureTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -8721639551336713264L;
	//	public static final String ORDER_COLUMN = "##";
	public static final String MS_FEATURE_COLUMN = "Feature name";
	public static final String COMPOUND_NAME_COLUMN = "Identification";
	public static final String DATABSE_LINK_COLUMN = "DBID";
	public static final String CHEM_MOD_COLUMN = "Adduct";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String RETENTION_COLUMN = "RT";
	public static final String NEUTRAL_MASS_COLUMN = "Neutral mass";
	public static final String BASE_PEAK_COLUMN = "Base peak";
	public static final String KMD_COLUMN = "KMD";
	public static final String KMD_MOD_COLUMN = "KMD mod";
	public static final String EXPERIMENT_COLUMN = "Experiment";
	public static final String SAMPLE_COLUMN = "Sample";
	public static final String SAMPLE_TYPE_COLUMN = "Sample type";
	public static final String ACQ_METHOD_ID_COLUMN = "Acq. method";
	public static final String DEX_METHOD_ID_COLUMN = "DA method";
	public static final String ANNOTATIONS_COLUMN = "Annot.";
	public static final String AMBIGUITY_COLUMN = "ID status";
	public static final String FOLLOWUP_COLUMN = "Follow-up";
	public static final String ID_LEVEL_COLUMN = "ID level";

	public ReferenceMsOneFeatureTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(MS_FEATURE_COLUMN, MS_FEATURE_COLUMN, MSFeatureInfoBundle.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, "Compound name", String.class, false),
			new ColumnContext(DATABSE_LINK_COLUMN, 
					"Primary database accession and web link to the source database", MsFeatureIdentity.class, false),
			new ColumnContext(AMBIGUITY_COLUMN, 
					"Identification status (unknown, single / multiple IDs)", FeatureIdentificationState.class, false),
			new ColumnContext(ID_LEVEL_COLUMN, "Identification level", MSFeatureIdentificationLevel.class, false),
			new ColumnContext(ANNOTATIONS_COLUMN, "Has annotations (standard or free form)", Boolean.class, false),
			new ColumnContext(FOLLOWUP_COLUMN, "Identification followup steps assigned", Boolean.class, false),
			new ColumnContext(CHEM_MOD_COLUMN, "Primary adduct", Adduct.class, false),
			new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(RETENTION_COLUMN, "Retention time", Double.class, false),
			new ColumnContext(NEUTRAL_MASS_COLUMN, "Monoisotopic neutral mass", Double.class, false),
			new ColumnContext(BASE_PEAK_COLUMN, "Base peak M/Z", Double.class, false),
			new ColumnContext(KMD_COLUMN, "Kendrick mass defect", Double.class, false),
			new ColumnContext(KMD_MOD_COLUMN, "Kendrick mass defect, modified", Double.class, false),
			new ColumnContext(EXPERIMENT_COLUMN, EXPERIMENT_COLUMN, LIMSExperiment.class, false),
			new ColumnContext(SAMPLE_COLUMN, SAMPLE_COLUMN, IDTExperimentalSample.class, false),
			new ColumnContext(SAMPLE_TYPE_COLUMN, SAMPLE_TYPE_COLUMN, LIMSSampleType.class, false),
			new ColumnContext(ACQ_METHOD_ID_COLUMN, "Data acquisition method", DataAcquisitionMethod.class, false),
			new ColumnContext(DEX_METHOD_ID_COLUMN, "Data analysis method", DataExtractionMethod.class, false),
		};
	}

	public void setTableModelFromFeatureList(Collection<MSFeatureInfoBundle> featureList) {

		setRowCount(0);
		if(featureList == null || featureList.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MSFeatureInfoBundle bundle : featureList) {

			MsFeature cf = bundle.getMsFeature();
			Double bp = null;
			Integer charge = null;
			boolean hasMsms = false;
			Adduct adduct = null;
			if (cf.getSpectrum() != null) {
				bp = cf.getMonoisotopicMz();
				charge = cf.getCharge();
				if(!cf.getSpectrum().getTandemSpectra().isEmpty())
					hasMsms = true;
			}
			String compoundName = "";
			LIMSSampleType limsSampleType = null;
			if(bundle.getStockSample() != null) 
				limsSampleType = bundle.getStockSample().getLimsSampleType();
			
			MSFeatureIdentificationLevel idLevel = null;
			if(cf.getPrimaryIdentity() != null) {
				compoundName = cf.getPrimaryIdentity().getCompoundName();
				idLevel = cf.getPrimaryIdentity().getIdentificationLevel();
				adduct = cf.getPrimaryIdentity().getPrimaryAdduct();
			}
			if(adduct == null) {
				
				if(cf.getSpectrum() != null) 	
					adduct = cf.getSpectrum().getPrimaryAdduct();
				else
					adduct = AdductManager.getDefaultAdductForCharge(cf.getCharge());
			}
			FeatureIdentificationState idState = cf.getIdentificationState();
			boolean hasAnnotations = (!cf.getAnnotations().isEmpty() 
					|| !bundle.getStandadAnnotations().isEmpty());
			boolean hasFollowup = !bundle.getIdFollowupSteps().isEmpty();

			Object[] obj = {
				bundle,
				compoundName,
				cf.getPrimaryIdentity(),
				idState,
				idLevel,
				hasAnnotations,
				hasFollowup,
				adduct,
				charge,
				cf.getRetentionTime(),
				cf.getNeutralMass(),
				bp,
				cf.getKmd(),
				cf.getModifiedKmd(),
				bundle.getExperiment(),
				bundle.getSample(),
				limsSampleType,
				bundle.getAcquisitionMethod(),
				bundle.getDataExtractionMethod(),
			};
			rowData.add(obj);
		}	
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void updateFeatureData(MSFeatureInfoBundle bundle) {
		
		int row = getFeatureInfoBundleRow(bundle);
		if(row == -1)
			return;
		
		MsFeature cf = bundle.getMsFeature();
		String compoundName = "";
		boolean hasAnnotations = (!cf.getAnnotations().isEmpty() 
				|| !bundle.getStandadAnnotations().isEmpty());
		boolean hasFollowup = !bundle.getIdFollowupSteps().isEmpty();
		Adduct adduct = null;
		Double bp = null;
		Integer charge = null;
		if (cf.getSpectrum() != null) {
			bp = cf.getMonoisotopicMz();
			charge = cf.getCharge();
		}
		MSFeatureIdentificationLevel idLevel = null;
		if(cf.getPrimaryIdentity() != null) {

			if(cf.getPrimaryIdentity().getCompoundIdentity() == null) {
				System.out.println(cf.getPrimaryIdentity().
						getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getUniqueId() + " has no compound ID");
			}
			else {
				compoundName = cf.getPrimaryIdentity().getCompoundName();
			}
			if(cf.getPrimaryIdentity().getMsRtLibraryMatch() != null) {
				adduct = cf.getPrimaryIdentity().getMsRtLibraryMatch().
						getTopAdductMatch().getLibraryMatch();
			}
			idLevel = cf.getPrimaryIdentity().getIdentificationLevel();
			adduct = cf.getPrimaryIdentity().getPrimaryAdduct();
		}	
		if(adduct == null) {
			
			if(cf.getSpectrum() != null) 	
				adduct = cf.getSpectrum().getPrimaryAdduct();
			else
				adduct = AdductManager.getDefaultAdductForCharge(cf.getCharge());
		}
		setValueAt(bundle, row, getColumnIndex(MS_FEATURE_COLUMN));
		setValueAt(compoundName, row, getColumnIndex(COMPOUND_NAME_COLUMN));
		setValueAt(cf.getPrimaryIdentity(), row, getColumnIndex(DATABSE_LINK_COLUMN));
		setValueAt(cf.getIdentificationState(), row, getColumnIndex(AMBIGUITY_COLUMN));		
		setValueAt(idLevel, row, getColumnIndex(ID_LEVEL_COLUMN));		
		setValueAt(hasAnnotations, row, getColumnIndex(ANNOTATIONS_COLUMN));
		setValueAt(hasFollowup, row, getColumnIndex(FOLLOWUP_COLUMN));
		setValueAt(adduct, row, getColumnIndex(CHEM_MOD_COLUMN));
		setValueAt(charge, row, getColumnIndex(CHARGE_COLUMN));
		setValueAt(cf.getRetentionTime(), row, getColumnIndex(RETENTION_COLUMN));
		setValueAt(cf.getNeutralMass(), row, getColumnIndex(NEUTRAL_MASS_COLUMN));
		setValueAt(bp, row, getColumnIndex(BASE_PEAK_COLUMN));
		setValueAt(cf.getKmd(), row, getColumnIndex(KMD_COLUMN));
		setValueAt(cf.getModifiedKmd(), row, getColumnIndex(KMD_MOD_COLUMN));		
		setValueAt(bundle.getExperiment(), row, getColumnIndex(EXPERIMENT_COLUMN));
		setValueAt(bundle.getSample(), row, getColumnIndex(SAMPLE_COLUMN));
		setValueAt(bundle.getStockSample().getLimsSampleType(), row, getColumnIndex(SAMPLE_TYPE_COLUMN));
		setValueAt(bundle.getAcquisitionMethod(), row, getColumnIndex(ACQ_METHOD_ID_COLUMN));
		setValueAt(bundle.getDataExtractionMethod(), row, getColumnIndex(DEX_METHOD_ID_COLUMN));
	}
	
	public int getFeatureInfoBundleRow(MSFeatureInfoBundle bundle) {

		int col = getColumnIndex(MS_FEATURE_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (bundle.equals(getValueAt(i, col)))
				return i;
		}
		return -1;
	}

	public int getFeatureRow(MsFeature feature) {

		int row = -1;
		int col = getColumnIndex(FeatureDataTableModel.MS_FEATURE_COLUMN);

		for (int i = 0; i < getRowCount(); i++) {

			if (feature.equals(((MSFeatureInfoBundle)getValueAt(i, col)).getMsFeature()))
				return i;
		}
		return row;
	}
}
