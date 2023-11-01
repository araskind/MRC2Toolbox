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

package edu.umich.med.mrc2.datoolbox.gui.idtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.MSRTSearchUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class UniversalIdentificationResultsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 8319459935748558385L;

	public static final String DEFAULT_ID_COLUMN = "Default";
	public static final String ID_LEVEL_COLUMN = "Level";
	public static final String IDENTIFICATION_COLUMN = "Compound name";
	public static final String COMPOUND_ID_COLUMN = "Compound ID";
	public static final String FORMULA_COLUMN = "Formula";
	public static final String NEUTRAL_MASS_COLUMN = "Monoisotopic mass";
	public static final String ID_SOURCE_COLUMN = "ID source";
	public static final String ID_CONFIDENCE_COLUMN = "Conf. level";
	public static final String ID_SCORE_COLUMN = "Score";
	
	//	MS1/RT library match
	public static final String MASS_ERROR_COLUMN = "Mass error";
	public static final String RETENTION_ERROR_COLUMN = "RT error";
	public static final String BEST_MATCH_ADDUCT_COLUMN = "Adduct";
	public static final String MSRT_LIB_COLUMN = "MS/RT library";
		
	//	MSMS library match
	public static final String ENTROPY_BASED_SCORE_COLUMN = "EScore";
	public static final String MSMS_MATCH_TYPE_COLUMN = "MType";
	public static final String PARENT_MZ_COLUMN = "Parent M/Z";
	public static final String MSMS_LIB_COLUMN = "MSMS library";
	public static final String COLLISION_ENERGY_COLUMN = "CE, V";
	public static final String FWD_SCORE_COLUMN = "Fwd";
	public static final String REV_SCORE_COLUMN = "Rvs";
	public static final String PROBABILITY_COLUMN = "Prob";
	public static final String DOT_PRODUCT_COLUMN = "DP";
	public static final String REVERSE_DOT_PRODUCT_COLUMN = "RDP";
	public static final String HYBRID_DOT_PRODUCT_COLUMN = "HRDP";
	public static final String HYBRID_SCORE_COLUMN = "HScore";
	public static final String HYBRID_DELTA_MZ_COLUMN = '\u0394' + " M/Z";
	public static final String SPECTRUM_ENTROPY_COLUMN = "PRE";
	
	//	FDR estimation
	public static final String Q_VALUE_COLUMN = "q-value";
	public static final String POSTERIOR_PROBABILITY_COLUMN = "Post.prob";
	public static final String PERCOLATOR_SCORE_COLUMN = "Perc.score";
	
	public static final String QC_COLUMN = "QC";
	
	public static final Collection<String> msRtColumnList = 
			Arrays.asList(DEFAULT_ID_COLUMN,
						ID_LEVEL_COLUMN,
						IDENTIFICATION_COLUMN,
						COMPOUND_ID_COLUMN,
						FORMULA_COLUMN,
						NEUTRAL_MASS_COLUMN,
						ID_SOURCE_COLUMN,
						ID_CONFIDENCE_COLUMN,
						ID_SCORE_COLUMN,
						ENTROPY_BASED_SCORE_COLUMN,
						MASS_ERROR_COLUMN,
						RETENTION_ERROR_COLUMN,
						BEST_MATCH_ADDUCT_COLUMN,
						MSRT_LIB_COLUMN,
						QC_COLUMN);
	
	public static final Collection<String> msmsColumnList = 
			Arrays.asList(DEFAULT_ID_COLUMN,
					ID_LEVEL_COLUMN,
					IDENTIFICATION_COLUMN,
					COMPOUND_ID_COLUMN,
					FORMULA_COLUMN,
					NEUTRAL_MASS_COLUMN,
					ID_SOURCE_COLUMN,
					ID_CONFIDENCE_COLUMN,
					ID_SCORE_COLUMN,
					ENTROPY_BASED_SCORE_COLUMN,
					MSMS_MATCH_TYPE_COLUMN,
					PARENT_MZ_COLUMN,
					MSMS_LIB_COLUMN,
					COLLISION_ENERGY_COLUMN,
					FWD_SCORE_COLUMN,
					REV_SCORE_COLUMN,
					PROBABILITY_COLUMN,
					DOT_PRODUCT_COLUMN,
					REVERSE_DOT_PRODUCT_COLUMN,
					HYBRID_DOT_PRODUCT_COLUMN,
					HYBRID_SCORE_COLUMN,
					HYBRID_DELTA_MZ_COLUMN,
					SPECTRUM_ENTROPY_COLUMN);

	private MsFeature parentFeature;

	public UniversalIdentificationResultsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DEFAULT_ID_COLUMN, "Default identification", Boolean.class, true),
			new ColumnContext(ID_LEVEL_COLUMN, "Identification level", MSFeatureIdentificationLevel.class, false),
			new ColumnContext(IDENTIFICATION_COLUMN, "Compound name", MsFeatureIdentity.class, false),
			new ColumnContext(COMPOUND_ID_COLUMN, 
					"Primary database accession and web link to the source database",CompoundIdentity.class, false),
			new ColumnContext(FORMULA_COLUMN, FORMULA_COLUMN, String.class, false),
			new ColumnContext(NEUTRAL_MASS_COLUMN, "Monoisotopic neutral mass", Double.class, false),
			new ColumnContext(ID_SOURCE_COLUMN, "Identification source", CompoundIdSource.class, false),
			new ColumnContext(ID_CONFIDENCE_COLUMN, "Identification confidence level", CompoundIdentificationConfidence.class, false),			
			new ColumnContext(ID_SCORE_COLUMN, "Score", Double.class, false),	//	Also LIB_SCORE_COLUMN
			new ColumnContext(ENTROPY_BASED_SCORE_COLUMN, "Entropy based MSMS match score", Double.class, false),
			//	MS1/RT library match
			new ColumnContext(MASS_ERROR_COLUMN, "Mass error relative to calculated value, ppm", Double.class, false),
			new ColumnContext(RETENTION_ERROR_COLUMN, "Retention time error relative to library RT", Double.class, false),
			new ColumnContext(BEST_MATCH_ADDUCT_COLUMN, "Best match adduct", Adduct.class, false),
			new ColumnContext(MSRT_LIB_COLUMN, "MS/RT compound library name", CompoundLibrary.class, false),	//	TODO replace by library class?
			//	MSMS library match						
			new ColumnContext(MSMS_MATCH_TYPE_COLUMN, "MSMS matching type", ReferenceMsMsLibraryMatch.class, false),
			new ColumnContext(PARENT_MZ_COLUMN, "MSMS precursor M/Z", Double.class, false),
			new ColumnContext(MSMS_LIB_COLUMN, "Reference MSMS library name", ReferenceMsMsLibrary.class, false),
			new ColumnContext(COLLISION_ENERGY_COLUMN, "MSMS collision energy", String.class, false),
			new ColumnContext(FWD_SCORE_COLUMN, "MSMS forward match score", Double.class, false),
			new ColumnContext(REV_SCORE_COLUMN, "MSMS reverse match score", Double.class, false),
			new ColumnContext(PROBABILITY_COLUMN, "MSMS match probability", Double.class, false),
			new ColumnContext(DOT_PRODUCT_COLUMN, "MSMS forward match dot-product", Double.class, false),			
			new ColumnContext(REVERSE_DOT_PRODUCT_COLUMN, "MSMS reverse match dot-product", Double.class, false),
			new ColumnContext(HYBRID_DOT_PRODUCT_COLUMN, "MSMS hybrid match dot-product", Double.class, false),
			new ColumnContext(HYBRID_SCORE_COLUMN, "MSMS hybrid match score", Double.class, false),
			new ColumnContext(HYBRID_DELTA_MZ_COLUMN, "MSMS hybrid match M/Z shift", Double.class, false),	
			new ColumnContext(SPECTRUM_ENTROPY_COLUMN, "MSMS Pattern recognition entropy", Double.class, false),
			//	FDR estimation
			new ColumnContext(Q_VALUE_COLUMN, "Q-value (false discovery rate estimation)", Double.class, false),
			new ColumnContext(POSTERIOR_PROBABILITY_COLUMN,  "Posterior probability (false discovery rate estimation)", Double.class, false),
			new ColumnContext(PERCOLATOR_SCORE_COLUMN,  "Percollator score (false discovery rate estimation)", Double.class, false),
			
			new ColumnContext(QC_COLUMN, "QC reference", Boolean.class, false),
		};
	}

	public void setModelFromFeature(MsFeature parentFeature) {

		this.parentFeature = parentFeature;
		setModelFromIdList(
				parentFeature.getIdentifications(), 
				parentFeature.getPrimaryIdentity());
	}

	public void setModelFromIdList(
			Collection<MsFeatureIdentity> idList, 
			MsFeatureIdentity defaultId) {

		setRowCount(0);
		if(idList == null || idList.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MsFeatureIdentity id : idList) {

			if(id.getCompoundIdentity() == null)
				continue;
			
			Double deltaMz = 0.0d;
			Double deltaRt = 
					MSRTSearchUtils.calculateRetentionShift(parentFeature, id);
			Adduct adductMatch = id.getPrimaryAdduct();
			CompoundLibrary msRtLibrary = null;		
			double parentMz = 0.0d;
			String collisionEnergyValue = null;
			double forwardScore = 0.0d;
			double reverseScore = 0.0d;
			double probability = 0.0d;
			double dotProduct = 0.0d;		
			double revDotProduct = 0.0d;
			double hybDotProduct = 0.0d;
			double hybScore = 0.0d;
			double hybDmz = 0.0d;
			double msmsEntropy = 0.0d;		
			double qValue = 0.0d;
			double posteriorProbability = 0.0d;
			double percolatorScore = 0.0d;

			if(id.getMsRtLibraryMatch() != null) {
				
				if(id.getMsRtLibraryMatch().getLibraryId() != null) {
					msRtLibrary = IDTDataCache.getMSRTLibraryById(
							id.getMsRtLibraryMatch().getLibraryId());	
				}
				deltaMz = MsUtils.getPpmMassErrorForTopAdductMatch(parentFeature);
			}			
			TandemMassSpectrum experimentalMsMs = 
					parentFeature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
			if(experimentalMsMs != null) {

				if(experimentalMsMs.getParent() != null)
					parentMz = experimentalMsMs.getParent().getMz();
			}
			ReferenceMsMsLibraryMatch msmslibMatch = id.getReferenceMsMsLibraryMatch();
			ReferenceMsMsLibrary lib = null;
			MsMsLibraryFeature matchFeature = null;
			if(msmslibMatch != null) {
				matchFeature = msmslibMatch.getMatchedLibraryFeature();
				lib = IDTDataCache.getReferenceMsMsLibraryById(matchFeature.getMsmsLibraryIdentifier());
				collisionEnergyValue = matchFeature.getCollisionEnergyValue();
				forwardScore = msmslibMatch.getForwardScore();
				reverseScore = msmslibMatch.getReverseScore();
				probability = msmslibMatch.getProbability();
				dotProduct = msmslibMatch.getDotProduct();
				revDotProduct = msmslibMatch.getReverseDotProduct();
				hybDotProduct = msmslibMatch.getHybridDotProduct();
				hybScore = msmslibMatch.getHybridScore();
				hybDmz = msmslibMatch.getHybridDeltaMz();
				msmsEntropy = matchFeature.getSpectrumEntropy();				
				if(msmslibMatch.getMatchType().equals(MSMSMatchType.Regular))
					deltaMz = MsUtils.getPpmMassErrorForIdentity(parentFeature, id);
				
				qValue = msmslibMatch.getqValue();
				posteriorProbability = msmslibMatch.getPosteriorErrorProbability();
				percolatorScore = msmslibMatch.getPercolatorScore();
			}
			Object[] obj = {
					id.equals(defaultId),
					id.getIdentificationLevel(),
					id,
					id.getCompoundIdentity(),
					id.getCompoundIdentity().getFormula(),
					id.getCompoundIdentity().getExactMass(),
					id.getIdSource(),
					id.getConfidenceLevel(),
					id.getScore(),	
					id.getEntropyBasedScore(),
					deltaMz,
					deltaRt,
					adductMatch,
					msRtLibrary,					
					msmslibMatch,
					parentMz,
					lib,
					collisionEnergyValue,
					forwardScore,
					reverseScore,
					probability,
					dotProduct,	
					revDotProduct,
					hybDotProduct,
					hybScore,
					hybDmz,
					msmsEntropy,
					qValue,
					posteriorProbability,
					percolatorScore,
					id.isQcStandard(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}	

	/**
	 * @param parentFeature the parentFeature to set
	 */
	public void setParentFeature(MsFeature parentFeature) {
		this.parentFeature = parentFeature;
	}
}























