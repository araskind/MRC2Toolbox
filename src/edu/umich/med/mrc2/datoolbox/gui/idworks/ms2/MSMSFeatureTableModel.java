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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIdentificationState;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerAnnotationCache;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class MSMSFeatureTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -8721639551336713264L;
	public static final String MS_FEATURE_COLUMN = "Feature name";
	public static final String COMPOUND_NAME_COLUMN = "Identification";
	public static final String ID_LEVEL_COLUMN = "ID level";
	public static final String ADDUCT_COLUMN = "Adduct";
	public static final String BINNER_ANNOTATION_COLUMN = "BinAnnot";
	public static final String POLARITY_COLUMN = "Polarity";
	public static final String DATABASE_LINK_COLUMN = "DBID";
	public static final String AMBIGUITY_COLUMN = "ID status";
	public static final String RETENTION_COLUMN = "RT";
	public static final String PARENT_MZ_COLUMN = "Parent M/Z";
	public static final String COLLISION_ENERGY_COLUMN = "CE, V";
	public static final String ENTROPY_BASED_SCORE_COLUMN = "EScore";
	public static final String MSMS_MATCH_TYPE_COLUMN = "MType";
	public static final String EXPERIMENT_COLUMN = "Experiment";
	public static final String SAMPLE_COLUMN = "Sample";
	public static final String SAMPLE_TYPE_COLUMN = "Sample type";
	public static final String ACQ_METHOD_ID_COLUMN = "Acq. method";
	public static final String DEX_METHOD_ID_COLUMN = "DA method";
	public static final String ANNOTATIONS_COLUMN = "Annot.";
	public static final String FOLLOWUP_COLUMN = "Follow-up";
	public static final String PARENT_ION_PURITY_COLUMN = "PIpurity";
	public static final String PARENT_ION_IS_MINOR_ISOTOPE_COLUMN = "MinorP";
	public static final String SPECTRUM_ENTROPY_COLUMN = "PRE";
	public static final String SPECTRUM_TOTAL_INTENSITY_COLUMN = "Area Sum";	
	public static final String LIBRARY_PRECURSOR_DELTA_MZ_COLUMN = "Lib " + '\u0394' + " M/Z";
	public static final String NEUTRAL_MASS_PRECURSOR_DELTA_MZ_COLUMN = "MW " + '\u0394' + " M/Z";
	
	private IMsFeatureInfoBundleCluster featureCluster;
	private MSMSFeatureTable parentTable;

	public MSMSFeatureTableModel(MSMSFeatureTable msmsFeatureTable) {
		
		super();
		this.parentTable = msmsFeatureTable;
		columnArray = new ColumnContext[] {

			new ColumnContext(MS_FEATURE_COLUMN, MS_FEATURE_COLUMN, MSFeatureInfoBundle.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, "Compound name", String.class, false),
			new ColumnContext(DATABASE_LINK_COLUMN, 
					"Primary database accession and web link to the source database", MsFeatureIdentity.class, false),
			new ColumnContext(AMBIGUITY_COLUMN, 
					"Identification status (unknown, single / multiple IDs)", FeatureIdentificationState.class, false),
			new ColumnContext(ID_LEVEL_COLUMN, "Identification level", MSFeatureIdentificationLevel.class, false),
			new ColumnContext(ADDUCT_COLUMN, "Primary adduct", Adduct.class, false),
			new ColumnContext(BINNER_ANNOTATION_COLUMN, "Binner annotation", BinnerAnnotation.class, false),
			new ColumnContext(POLARITY_COLUMN, POLARITY_COLUMN, Polarity.class, false),
			new ColumnContext(ANNOTATIONS_COLUMN, "Has annotations (standard or free form)", Boolean.class, false),
			new ColumnContext(FOLLOWUP_COLUMN, "Identification followup steps assigned", Boolean.class, false),
			new ColumnContext(RETENTION_COLUMN, "Retention time", Double.class, false),
			new ColumnContext(PARENT_MZ_COLUMN, "Precursor ion M/Z", Double.class, false),			
			new ColumnContext(NEUTRAL_MASS_PRECURSOR_DELTA_MZ_COLUMN, 
					"Difference between calculated neutral mass and precursor ion M/Z", Double.class, false),
			new ColumnContext(LIBRARY_PRECURSOR_DELTA_MZ_COLUMN, 
					"Difference between precursor M/Z from reference MSMS library\n"
					+ "and observed precursor ion M/Z", Double.class, false),			
			new ColumnContext(COLLISION_ENERGY_COLUMN, "MSMS collision energy", Double.class, false),
			new ColumnContext(ENTROPY_BASED_SCORE_COLUMN, "Entropy based MSMS match score", Double.class, false),
			new ColumnContext(MSMS_MATCH_TYPE_COLUMN, "MSMS match type", ReferenceMsMsLibraryMatch.class, false),		
			new ColumnContext(SAMPLE_TYPE_COLUMN, SAMPLE_TYPE_COLUMN, LIMSSampleType.class, false),
			new ColumnContext(SAMPLE_COLUMN, "Sample ID", IDTExperimentalSample.class, false),
			new ColumnContext(EXPERIMENT_COLUMN, "Experiment ID", LIMSExperiment.class, false),
			new ColumnContext(ACQ_METHOD_ID_COLUMN, "Data acquisition method", DataAcquisitionMethod.class, false),
			new ColumnContext(DEX_METHOD_ID_COLUMN, "Data analysis method", DataExtractionMethod.class, false),			
			new ColumnContext(PARENT_ION_PURITY_COLUMN, "Precursor ion purity", Double.class, false),
			new ColumnContext(PARENT_ION_IS_MINOR_ISOTOPE_COLUMN, "Is precursor ion a minor isotope (not monoisotopic peak)", Boolean.class, false),			
			new ColumnContext(SPECTRUM_ENTROPY_COLUMN, "Pattern recognition entropy for observed MSMS", Double.class, false),
			new ColumnContext(SPECTRUM_TOTAL_INTENSITY_COLUMN, "Sum of intensities of all peaks in observed MSMS", Double.class, false),
		};
	}
	
	public void setTableModelFromFeatureList(Collection<MSFeatureInfoBundle> featureList) {

		setRowCount(0);
		this.featureCluster = null;
		if(featureList == null || featureList.isEmpty())
			return;
		
		List<Object[]>rowData = createModelData(featureList);
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public void setTableModelFromFeatureCluster(IMsFeatureInfoBundleCluster featureCluster) {

		setRowCount(0);
		if(featureCluster == null || featureCluster.getComponents() == null 
				|| featureCluster.getComponents().isEmpty())
			return;
		
		this.featureCluster = featureCluster;
		
		List<Object[]>rowData = createModelData(featureCluster.getComponents());
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public List<Object[]> createModelData(Collection<MSFeatureInfoBundle> featureList) {
		
		List<Object[]> modelData = new ArrayList<Object[]>();
		for (MSFeatureInfoBundle bundle : featureList) {

			MsFeature cf = bundle.getMsFeature();
			TandemMassSpectrum instrumentMsMs = 
					cf.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);

			if(instrumentMsMs == null)
				continue;

			String compoundName = "";
			LIMSSampleType limsSampleType = null;
			if(bundle.getStockSample() != null) 
				limsSampleType = bundle.getStockSample().getLimsSampleType();
				
			FeatureIdentificationState idState = cf.getIdentificationState();
			boolean hasAnnotations = (!cf.getAnnotations().isEmpty() 
					|| !bundle.getStandadAnnotations().isEmpty());
			boolean hasFollowup = !bundle.getIdFollowupSteps().isEmpty();
			MSFeatureIdentificationLevel idLevel = null;
			Double libraryPrecursorDeltaMz = null;
			Double neutralMassDeltaMz = null;
			MsFeatureIdentity primaryId = cf.getPrimaryIdentity();
			Adduct adduct = null;
			BinnerAnnotation ba = getBinnerAnnotation(bundle);
			
			Double entropyMsMsScore = null;
			ReferenceMsMsLibraryMatch refMatch = null;
			if(primaryId != null) {

				if(primaryId.getCompoundIdentity() == null) {
					compoundName = primaryId.getIdentityName();
				}
				else {
					compoundName = primaryId.getCompoundName();
					double neutralMass = primaryId.getCompoundIdentity().getExactMass();
					neutralMassDeltaMz = instrumentMsMs.getParent().getMz() - neutralMass;
				}
				refMatch = primaryId.getReferenceMsMsLibraryMatch();
				if(refMatch != null) {
					
					MsPoint libPrecursor = refMatch.getMatchedLibraryFeature().getParent();
					if(libPrecursor != null) 
						libraryPrecursorDeltaMz = instrumentMsMs.getParent().getMz() - libPrecursor.getMz();					
				
					entropyMsMsScore = refMatch.getEntropyBasedScore();
				}				
				idLevel = cf.getPrimaryIdentity().getIdentificationLevel();
				adduct = primaryId.getPrimaryAdduct();
			}
			if(adduct == null) {
				
				if(cf.getSpectrum() != null && cf.getSpectrum().getPrimaryAdduct() != null) 	
					adduct = cf.getSpectrum().getPrimaryAdduct();
				else if (cf.getCharge() != 0)
					adduct = AdductManager.getDefaultAdductForCharge(cf.getCharge());
				else
					adduct = AdductManager.getDefaultAdductForPolarity(cf.getPolarity());
			}
			Object[] obj = {
				bundle,			//	MS_FEATURE_COLUMN	MsFeature
				compoundName,	//	COMPOUND_NAME_COLUMN	String
				cf.getPrimaryIdentity(),	//	DATABSE_LINK_COLUMN	MsFeatureIdentity
				idState, //	AMBIGUITY_COLUMN, Boolean
				idLevel,
				adduct,
				ba,
				cf.getPolarity(),
				hasAnnotations,	//	ANNOTATIONS_COLUMN, Boolean
				hasFollowup,
				cf.getRetentionTime(),		//	RETENTION_COLUMN	Double
				instrumentMsMs.getParent().getMz(),	//	PARENT_MZ_COLUMN	Double
				neutralMassDeltaMz,		//	NEUTRAL_MASS_PRECURSOR_DELTA_MZ_COLUMN	Double
				libraryPrecursorDeltaMz,	//	LIBRARY_PRECURSOR_DELTA_MZ_COLUMN	Double				
				instrumentMsMs.getCidLevel(),	//	COLLISION_ENERGY_COLUMN	Double
				entropyMsMsScore,
				refMatch,
				
//				score,	//	LIB_SCORE_COLUMN	Double
//				lib,	//	MSMS_LIB_COLUMN	CompoundLibrary
				
				limsSampleType,	//	SAMPLE_TYPE_COLUMN	LIMSSampleType
				bundle.getSample(),		//	SAMPLE_COLUMN	IDTExperimentalSample
				bundle.getExperiment(),	//	EXPERIMENT_COLUMN	LIMSExperiment
				bundle.getAcquisitionMethod(),	//	ACQ_METHOD_ID_COLUMN	LIMSAcquisitionMethod
				bundle.getDataExtractionMethod(),	//	DEX_METHOD_ID_COLUMN	LIMSDataExtractionMethod
				instrumentMsMs.getParentIonPurity(),	//	PARENT_ION_PURITY_COLUMN	Double				
				instrumentMsMs.isParentIonMinorIsotope(),	//	PARENT_ION_IS_MINOR_ISOTOPE_COLUMN	Boolean
				instrumentMsMs.getEntropy(), //	SPECTRUM_ENTROPY_COLUMN		Double
				instrumentMsMs.getTotalIntensity(),	//	SPECTRUM_TOTAL_INTENSITY_COLUMN	Double
			};
			modelData.add(obj);
		}	
		return modelData;
	}

	private BinnerAnnotation getBinnerAnnotation(MSFeatureInfoBundle bundle) {
		
		if(featureCluster != null 
				&& featureCluster instanceof BinnerBasedMsFeatureInfoBundleCluster) {
			
			return ((BinnerBasedMsFeatureInfoBundleCluster)featureCluster).getAnnotationForFeature(bundle);		
		}
		else {
			Collection<BinnerAnnotation> cachedAnnotations = 
					BinnerAnnotationCache.getAnnotationsForMSFeature(bundle.getMSFeatureId());
			if(cachedAnnotations != null && !cachedAnnotations.isEmpty()) 
				return cachedAnnotations.iterator().next();			
			else
				return null;
		}
	}

	public void updateFeatureData(Collection<MSFeatureInfoBundle> bundlesToUpdate) {
		
		if(bundlesToUpdate == null || bundlesToUpdate.isEmpty())
			return;
		
		removeTableModelListener(parentTable);
		suppressEvents = true;
		Map<MSFeatureInfoBundle,Integer>featureRowMap = 
				getFeatureInfoBundleRowMap(bundlesToUpdate);
		if(featureRowMap.isEmpty())
			return;
		
		TreeSet<Integer>updatedRows = 
				new TreeSet<Integer>(featureRowMap.values());
		
		for(Entry<MSFeatureInfoBundle,Integer>e : featureRowMap.entrySet()) {
			
			MSFeatureInfoBundle bundle = e.getKey();
			int row = e.getValue();

			MsFeature cf = bundle.getMsFeature();
			TandemMassSpectrum instrumentMsMs = cf.getSpectrum().getTandemSpectra().
				stream().filter(s -> s.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL)).
				findFirst().orElse(null);

			if(instrumentMsMs == null)
				return;

			String compoundName = "";
			boolean hasAnnotations = (!cf.getAnnotations().isEmpty() 
					|| !bundle.getStandadAnnotations().isEmpty());
			boolean hasFollowup = !bundle.getIdFollowupSteps().isEmpty();
			MSFeatureIdentificationLevel idLevel = null;
			Double libraryPrecursorDeltaMz = null;
			Double neutralMassDeltaMz = null;
			Adduct adduct = null;
			BinnerAnnotation ba = getBinnerAnnotation(bundle);
			Double entropyMsMsScore = null;
			ReferenceMsMsLibraryMatch refMatch = null;
			MsFeatureIdentity primaryId = cf.getPrimaryIdentity();
		
			if(primaryId != null) {

				if(primaryId.getCompoundIdentity() == null) {
					compoundName = primaryId.getIdentityName();
				}
				else {
					compoundName = primaryId.getCompoundName();
					double neutralMass = primaryId.getCompoundIdentity().getExactMass();
					neutralMassDeltaMz = instrumentMsMs.getParent().getMz() - neutralMass;
				}	
				refMatch = primaryId.getReferenceMsMsLibraryMatch();
				if(refMatch != null) {
					
					MsPoint libPrecursor = refMatch.getMatchedLibraryFeature().getParent();
					if(libPrecursor != null) 
						libraryPrecursorDeltaMz = instrumentMsMs.getParent().getMz() - libPrecursor.getMz();					
				
					entropyMsMsScore = refMatch.getEntropyBasedScore();
				}
				
				idLevel = cf.getPrimaryIdentity().getIdentificationLevel();
				adduct = primaryId.getPrimaryAdduct();
			}
			if(adduct == null) {
				
				if(cf.getSpectrum() != null) 	
					adduct = cf.getSpectrum().getPrimaryAdduct();
				else
					adduct = AdductManager.getDefaultAdductForCharge(cf.getCharge());
			}
			LIMSSampleType sType = null;
			if(bundle.getStockSample() != null)
				sType = bundle.getStockSample().getLimsSampleType();
			
			setValueAt(compoundName, row, getColumnIndex(COMPOUND_NAME_COLUMN));
			setValueAt(cf.getPrimaryIdentity(), row, getColumnIndex(DATABASE_LINK_COLUMN));
			setValueAt(cf.getIdentificationState(), row, getColumnIndex(AMBIGUITY_COLUMN));
			setValueAt(idLevel, row, getColumnIndex(ID_LEVEL_COLUMN));
			setValueAt(adduct, row, getColumnIndex(ADDUCT_COLUMN));	
			setValueAt(ba, row, getColumnIndex(BINNER_ANNOTATION_COLUMN));
			setValueAt(cf.getPolarity(), row, getColumnIndex(POLARITY_COLUMN));	
			setValueAt(hasAnnotations, row, getColumnIndex(ANNOTATIONS_COLUMN));
			setValueAt(hasFollowup, row, getColumnIndex(FOLLOWUP_COLUMN));
			setValueAt(cf.getRetentionTime(), row, getColumnIndex(RETENTION_COLUMN));
			setValueAt(instrumentMsMs.getParent().getMz(), row, getColumnIndex(PARENT_MZ_COLUMN));
			setValueAt(neutralMassDeltaMz, row, getColumnIndex(NEUTRAL_MASS_PRECURSOR_DELTA_MZ_COLUMN));
			setValueAt(libraryPrecursorDeltaMz, row, getColumnIndex(LIBRARY_PRECURSOR_DELTA_MZ_COLUMN));	
			setValueAt(instrumentMsMs.getCidLevel(), row, getColumnIndex(COLLISION_ENERGY_COLUMN));		
			setValueAt(entropyMsMsScore, row, getColumnIndex(ENTROPY_BASED_SCORE_COLUMN));
			setValueAt(refMatch, row, getColumnIndex(MSMS_MATCH_TYPE_COLUMN));
			setValueAt(sType, row, getColumnIndex(SAMPLE_TYPE_COLUMN));
			setValueAt(bundle.getSample(), row, getColumnIndex(SAMPLE_COLUMN));
			setValueAt(bundle.getExperiment(), row, getColumnIndex(EXPERIMENT_COLUMN));
			setValueAt(bundle.getAcquisitionMethod(), row, getColumnIndex(ACQ_METHOD_ID_COLUMN));
			setValueAt(bundle.getDataExtractionMethod(), row, getColumnIndex(DEX_METHOD_ID_COLUMN));		
			setValueAt(instrumentMsMs.getParentIonPurity(), row, getColumnIndex(PARENT_ION_PURITY_COLUMN));		
			setValueAt(instrumentMsMs.isParentIonMinorIsotope(), row, getColumnIndex(PARENT_ION_IS_MINOR_ISOTOPE_COLUMN));
			setValueAt(instrumentMsMs.getEntropy(), row, getColumnIndex(SPECTRUM_ENTROPY_COLUMN));
			setValueAt(instrumentMsMs.getTotalIntensity(), row, getColumnIndex(SPECTRUM_TOTAL_INTENSITY_COLUMN));
			
			updatedRows.add(row);
		}
		suppressEvents = false;
		addTableModelListener(parentTable);
		fireTableRowsUpdated(updatedRows.first(), updatedRows.last());
	}
	
	public Map<MSFeatureInfoBundle,Integer> getFeatureInfoBundleRowMap(
			Collection<MSFeatureInfoBundle> bundles) {

		Map<MSFeatureInfoBundle,Integer>bundleRowMap = 
				new HashMap<MSFeatureInfoBundle,Integer>();
		int col = getColumnIndex(MS_FEATURE_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			MSFeatureInfoBundle b = (MSFeatureInfoBundle)getValueAt(i, col);
			if (bundles.contains(b))
				bundleRowMap.put(b, i);
		}
		return bundleRowMap;
	}

//	public int getFeatureInfoBundleRow(MSFeatureInfoBundle bundle) {
//
//		int col = getColumnIndex(MS_FEATURE_COLUMN);
//		for (int i = 0; i < getRowCount(); i++) {
//
//			if (bundle.equals(getValueAt(i, col)))
//				return i;
//		}
//		return -1;
//	}

//	public int getFeatureRow(MsFeature feature) {
//
//		int col = getColumnIndex(MS_FEATURE_COLUMN);
//		for (int i = 0; i < getRowCount(); i++) {
//
//			if (feature.equals(((MSFeatureInfoBundle)getValueAt(i, col)).getMsFeature()))
//				return i;
//		}
//		return -1;
//	}

	public void removeFeatureCluster() {
		this.featureCluster = null;
	}
}
