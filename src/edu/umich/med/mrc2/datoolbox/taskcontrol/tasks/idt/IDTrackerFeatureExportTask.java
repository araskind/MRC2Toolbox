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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.CompoundNameSet;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.ClassyFireClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIDSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMsFeatureProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.RefMetClassificationLevels;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTRawDataUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public abstract class IDTrackerFeatureExportTask extends AbstractTask {

	protected Collection<IDTrackerMsFeatureProperties> msmsFeaturePropertyList; 
	protected Collection<IDTrackerFeatureIdentificationProperties>identificationDetailsList;

	protected MSMSScoringParameter scoringParam; 
	protected double minimalMSMSScore; 
	protected FeatureIDSubset idSubset; 
	protected Collection<MSMSMatchType> msmsSearchTypes; 
	protected boolean excludeIfNoIdsLeft;
	protected File outputFile;
	
	protected static final String lineSeparator = System.getProperty("line.separator");
	protected static final char columnSeparator = MRC2ToolBoxConfiguration.getTabDelimiter();
	protected static final String trueMarker = "X";
	
	protected static final NumberFormat rtFormat = MRC2ToolBoxConfiguration.getRtFormat();
	protected static final NumberFormat mzFormat = MRC2ToolBoxConfiguration.getMzFormat();
	protected static final NumberFormat ppmFormat = MRC2ToolBoxConfiguration.getPpmFormat();
	protected static final NumberFormat intensityFormat = MRC2ToolBoxConfiguration.getIntensityFormat();
	protected static final NumberFormat entropyFormat = new DecimalFormat("##.###");
	protected static final DecimalFormat sciFormatter = new DecimalFormat("0.###E0");
	
	protected Collection<String>accessions;
	protected Collection<Injection>injections;
	protected Map<String,Map<RefMetClassificationLevels,String>>refMetClassifications;
	protected Map<String,Map<ClassyFireClassificationLevels,String>>classyFireClassifications;
	protected Map<String,String>systematicNames;
	protected Map<String,String>refMetNames;

	
	protected void getInjections(Collection<MSFeatureInfoBundle>features) throws Exception {
		
		Collection<String> injectionIds = features.stream().
				filter(f -> Objects.nonNull(f.getInjectionId())).
				map(f -> f.getInjectionId()).
				collect(Collectors.toCollection(TreeSet::new));
		
		total = injectionIds.size();
		processed = 0;
		injections = new TreeSet<Injection>();
		Connection conn = ConnectionManager.getConnection();		
		for(String id : injectionIds) {
			
			Injection inj = IDTRawDataUtils.getInjectionForId(id, conn);
			if(inj != null)
				injections.add(inj);
		}
		ConnectionManager.releaseConnection(conn);		
	}
	
	protected void getSystematicNames() throws Exception {
		
		systematicNames = new TreeMap<String,String>();
		if(accessions == null || accessions.isEmpty())
			return;
		
		taskDescription = "Getting systematic names ...";		
		total = accessions.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT S.NAME, S.NTYPE FROM COMPOUND_SYNONYMS S WHERE S.ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		for(String accession : accessions) {
			
			systematicNames.put(accession, "");
			CompoundNameSet nameSet = new CompoundNameSet(accession);
			ps.setString(1, accession);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				nameSet.addName(rs.getString("NAME"), rs.getString("NTYPE"));

			rs.close();
			if(nameSet.getIupacName() != null && !nameSet.getIupacName().isEmpty())
				systematicNames.put(accession, nameSet.getIupacName());
			
			if(systematicNames.get(accession).isEmpty() 
					&& nameSet.getSystematicName() != null 
					&& !nameSet.getSystematicName().isEmpty())
				systematicNames.put(accession, nameSet.getSystematicName());
			
			processed++;
		}
		ps.close();	
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void getRefMetClassifications() throws Exception {
		
		refMetClassifications = 
				new TreeMap<String,Map<RefMetClassificationLevels,String>>();
		
		if(accessions == null || accessions.isEmpty())
			return;
		
		taskDescription = "Getting RefMet classification ...";
		total = accessions.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		
		String sql =
				"SELECT R.SUPER_CLASS, R.MAIN_CLASS, R.SUB_CLASS  " +
				"FROM REFMET_DATA R, COMPOUND_CROSSREF C " +
				"WHERE C.SOURCE_DB = ? " +
				"AND C.SOURCE_DB_ID = R.NAME " +
				"AND C.ACCESSION = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, CompoundDatabaseEnum.REFMET.name());
		for(String accession : accessions) {
			
			Map<RefMetClassificationLevels,String>classifiers = 
					new TreeMap<RefMetClassificationLevels,String>();
			classifiers.put(RefMetClassificationLevels.SUPER_CLASS, "");
			classifiers.put(RefMetClassificationLevels.MAIN_CLASS, "");
			classifiers.put(RefMetClassificationLevels.SUB_CLASS, "");
			
			ps.setString(2, accession);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				classifiers.put(RefMetClassificationLevels.SUPER_CLASS, rs.getString("SUPER_CLASS"));
				classifiers.put(RefMetClassificationLevels.MAIN_CLASS, rs.getString("MAIN_CLASS"));
				classifiers.put(RefMetClassificationLevels.SUB_CLASS, rs.getString("SUB_CLASS"));
			}
			rs.close();
			refMetClassifications.put(accession, classifiers);			
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void getClassyFireClassifications() throws Exception {
				
		classyFireClassifications = 
				new TreeMap<String,Map<ClassyFireClassificationLevels,String>>();
		if(accessions == null || accessions.isEmpty())
			return;
		
		taskDescription = "Getting ClassyFire classification ...";
		total = accessions.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();		
		String sql =
			"SELECT KINGDOM, SUPERCLASS, CLASS, SUBCLASS, DIRECT_PARENT "
			+ "FROM CLASSYFIRE_CLASSIFICATION WHERE ACCESSION = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		String cfSql = "SELECT NAME FROM CLASSYFIRE_TAX_NODES WHERE CHEMONT_ID = ?";
		PreparedStatement cfPs = conn.prepareStatement(cfSql);
		
		for(String accession : accessions) {
			
			Map<ClassyFireClassificationLevels,String>classifiers = 
					new TreeMap<ClassyFireClassificationLevels,String>();
			classifiers.put(ClassyFireClassificationLevels.KINGDOM, "");
			classifiers.put(ClassyFireClassificationLevels.SUPERCLASS, "");
			classifiers.put(ClassyFireClassificationLevels.CLASS, "");
			classifiers.put(ClassyFireClassificationLevels.SUBCLASS, "");
			classifiers.put(ClassyFireClassificationLevels.DIRECT_PARENT, "");
			
			Map<ClassyFireClassificationLevels,String>classifierNames = 
					new TreeMap<ClassyFireClassificationLevels,String>();
			classifierNames.put(ClassyFireClassificationLevels.KINGDOM, "");
			classifierNames.put(ClassyFireClassificationLevels.SUPERCLASS, "");
			classifierNames.put(ClassyFireClassificationLevels.CLASS, "");
			classifierNames.put(ClassyFireClassificationLevels.SUBCLASS, "");
			classifierNames.put(ClassyFireClassificationLevels.DIRECT_PARENT, "");
						
			ps.setString(1, accession);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				classifierNames.put(ClassyFireClassificationLevels.KINGDOM, rs.getString("KINGDOM"));
				classifierNames.put(ClassyFireClassificationLevels.SUPERCLASS, rs.getString("SUPERCLASS"));
				classifierNames.put(ClassyFireClassificationLevels.CLASS, rs.getString("CLASS"));
				classifierNames.put(ClassyFireClassificationLevels.SUBCLASS, rs.getString("SUBCLASS"));
				classifierNames.put(ClassyFireClassificationLevels.DIRECT_PARENT, rs.getString("DIRECT_PARENT"));				
			}
			rs.close();
			for(Entry<ClassyFireClassificationLevels,String>field : classifierNames.entrySet()) {
				
				if(field.getValue() != null && !field.getValue().isEmpty()) {
					cfPs.setString(1, field.getValue());
					ResultSet cfRs = cfPs.executeQuery();
					while(cfRs.next()) 
						classifiers.put(field.getKey(), cfRs.getString("NAME"));
					
					cfRs.close();
				}
			}
			classyFireClassifications.put(accession, classifiers);			
			processed++;
		}
		ps.close();
		cfPs.close();
		ConnectionManager.releaseConnection(conn);
	}
		
	protected void getRefMetNames() throws Exception {
		
		refMetNames = new TreeMap<String,String>();
		if(accessions == null || accessions.isEmpty())
			return;
		
		taskDescription = "Getting RefMet names ...";
		total = accessions.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT S.SOURCE_DB_ID FROM COMPOUND_CROSSREF S "
				+ "WHERE SOURCE_DB = ? AND S.ACCESSION = ?";		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, CompoundDatabaseEnum.REFMET.name());
		for(String accession : accessions) {
			
			refMetNames.put(accession, "");
			ps.setString(2, accession);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				refMetNames.put(accession, rs.getString("SOURCE_DB_ID"));

			rs.close();
			processed++;
		}
		ps.close();		
		ConnectionManager.releaseConnection(conn);
	}
	
	protected String getFeatureIdentificationProperty(
			MsFeature feature,
			MsFeatureIdentity fid,
			IDTrackerFeatureIdentificationProperties property) {

		if(fid == null)
			return "";
		
		String compoundName = "";
		String idLevelName = "";
		MSFeatureIdentificationLevel idLevel = null;
		if(fid != null) {
			compoundName = fid.getIdentityName();
			if(fid.getCompoundIdentity() != null)
				compoundName = fid.getName();
			
			if(compoundName == null)
				compoundName = "";
			
			idLevel = fid.getIdentificationLevel();
			if(idLevel != null)
				idLevelName = idLevel.getName();
		}		
		double deltaMz = MsUtils.getMassErrorForIdentity(
				feature.getMonoisotopicMz(), fid, MassErrorType.ppm);

		if(property.equals(IDTrackerFeatureIdentificationProperties.COMPOUND_NAME))
			return compoundName;
				 
		if(fid.getCompoundIdentity() != null) {
			
			if(fid.getCompoundIdentity().getPrimaryDatabase() != null) {
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.DATABASE_ID))
					return fid.getCompoundIdentity().getPrimaryDatabaseId();
					
				if(property.equals(IDTrackerFeatureIdentificationProperties.SOURCE_DATABASE))
					return fid.getCompoundIdentity().getPrimaryDatabase().getName();	
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.FORMULA))
					return fid.getCompoundIdentity().getFormula();	
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.INCHI_KEY))
					return fid.getCompoundIdentity().getInChiKey();	
				
				if(property.equals(IDTrackerFeatureIdentificationProperties.SMILES))
					return fid.getCompoundIdentity().getSmiles();
								
				if(property.equals(IDTrackerFeatureIdentificationProperties.REFMET_NAME))
					return refMetNames.get(fid.getCompoundIdentity().getPrimaryDatabaseId());
								
				if(property.equals(IDTrackerFeatureIdentificationProperties.SYSTEMATIC_NAME))
					return systematicNames.get(fid.getCompoundIdentity().getPrimaryDatabaseId());			
			}
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.ID_SOURCE))
			return fid.getIdSource().getName();

		//	Move to feature properties, these are not identity properties
		
//		if(property.equals(IDTrackerFeatureIdentificationProperties.ANNOTATIONS) && hasAnnotations)
//			return trueMarker;
//		
//		if(property.equals(IDTrackerFeatureIdentificationProperties.FOLLOWUPS) && hasFollowup)
//			return trueMarker;
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.MRC2_ID_LEVEL))
			return idLevelName;
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.ID_SCORE)) {
			
			if(fid.getScore() > 0.0d)
				return entropyFormat.format(fid.getScore());
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.MSMS_ENTROPY_SCORE)) {
			
			if(fid.getEntropyBasedScore() > 0.0d)
				return entropyFormat.format(fid.getEntropyBasedScore());
		}		
		if(property.equals(IDTrackerFeatureIdentificationProperties.MASS_ERROR) && deltaMz != 0.0d)
			return ppmFormat.format(deltaMz);

		if(property.equals(IDTrackerFeatureIdentificationProperties.RETENTION_ERROR)) {
			Double deltaRt = calculateRetentionShift(fid, feature.getRetentionTime());
			if(deltaRt == null)
				return "";
			else
				return rtFormat.format(deltaRt);
		}
		double parentMz = 0.0d;
		String collisionEnergyValue = null;
		boolean isHybrid = false;
		boolean isInSource = false;
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
		String featureSpectrumArray = "";
		String libraryHitSpectrumArray = "";		
		double libraryPrecursorDeltaMz = 0.0d;
		double neutralMassDeltaMz = 0.0d;
		double precursorPurity = 1.0d;
		
		TandemMassSpectrum instrumentMsMs = feature.getSpectrum().getExperimentalTandemSpectrum();
		
		if(instrumentMsMs != null) {

			if(instrumentMsMs.getParent() != null)
				parentMz = instrumentMsMs.getParent().getMz();
			
			featureSpectrumArray = instrumentMsMs.getSpectrumAsPythonArray();				
			precursorPurity = instrumentMsMs.getParentIonPurity();
		}
		ReferenceMsMsLibraryMatch msmslibMatch = fid.getReferenceMsMsLibraryMatch();
		ReferenceMsMsLibrary lib = null;
		MsMsLibraryFeature matchFeature = null;
		if(msmslibMatch != null) {
			
			matchFeature = msmslibMatch.getMatchedLibraryFeature();
			lib = IDTDataCash.getReferenceMsMsLibraryById(matchFeature.getMsmsLibraryIdentifier());
			collisionEnergyValue = matchFeature.getCollisionEnergyValue();
			forwardScore = msmslibMatch.getForwardScore();
			reverseScore = msmslibMatch.getReverseScore();
			probability = msmslibMatch.getProbability();
			dotProduct = msmslibMatch.getDotProduct();
			isHybrid = msmslibMatch.isHybridMatch();
			isInSource = msmslibMatch.isInSourceMatch();
			revDotProduct = msmslibMatch.getReverseDotProduct();
			hybDotProduct = msmslibMatch.getHybridDotProduct();
			hybScore = msmslibMatch.getHybridScore();
			hybDmz = msmslibMatch.getHybridDeltaMz();
			msmsEntropy = matchFeature.getSpectrumEntropy();				
			if(!isHybrid)
				deltaMz = MsUtils.getMassErrorForIdentity(
						feature.getMonoisotopicMz(), fid, MassErrorType.ppm);
			
			qValue = msmslibMatch.getqValue();
			posteriorProbability = msmslibMatch.getPosteriorErrorProbability();
			percolatorScore = msmslibMatch.getPercolatorScore();				
			libraryHitSpectrumArray = matchFeature.getSpectrumAsPythonArray();

			if(fid.getCompoundIdentity() != null) {
				double neutralMass = fid.getCompoundIdentity().getExactMass();
				neutralMassDeltaMz = instrumentMsMs.getParent().getMz() - neutralMass;
			}
			MsPoint libPrecursor = msmslibMatch.getMatchedLibraryFeature().getParent();
			if(libPrecursor != null) 
				libraryPrecursorDeltaMz = instrumentMsMs.getParent().getMz() - libPrecursor.getMz();				
		}	
		if(property.equals(IDTrackerFeatureIdentificationProperties.MSMS_LIBRARY) && lib != null)
			return lib.getName();
		
//		if(property.equals(IDTrackerFeatureIdentificationProperties.MSMS_LIBRARY_ENTRY_ID))
//			msmslibMatch.getMatchedLibraryFeature().getMsmsLibraryIdentifier();
			
		if(property.equals(IDTrackerFeatureIdentificationProperties.COLLISION_ENERGY) && collisionEnergyValue != null)
			return collisionEnergyValue;
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.REVERSE_SCORE) && reverseScore != 0.0d)
			return entropyFormat.format(reverseScore);

		if(property.equals(IDTrackerFeatureIdentificationProperties.PROBABILITY) && probability != 0.0d) 
			return entropyFormat.format(probability);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.DOT_PRODUCT_COLUMN) && dotProduct != 0.0d)
			return entropyFormat.format(dotProduct);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.REVERSE_DOT_PRODUCT) && revDotProduct != 0.0d)
			return entropyFormat.format(revDotProduct);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.MATCH_TYPE)) {
			if(isHybrid)
				return MSMSMatchType.Hybrid.name();
			else if(isInSource)
				return MSMSMatchType.InSource.name();
			else
				return MSMSMatchType.Regular.name();
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.HYBRID_DOT_PRODUCT) && hybDotProduct != 0.0d)
			return entropyFormat.format(hybDotProduct);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.HYBRID_SCORE) && hybScore != 0.0d)
			return entropyFormat.format(hybScore);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.HYBRID_DELTA_MZ) && hybDmz != 0.0d)
			return mzFormat.format(hybDmz);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.SPECTRUM_ENTROPY) && msmsEntropy != 0.0d)
			return entropyFormat.format(msmsEntropy);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.LIBRARY_PRECURSOR_DELTA_MZ) && libraryPrecursorDeltaMz != 0.0d)
			return mzFormat.format(libraryPrecursorDeltaMz);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.NEUTRAL_MASS_PRECURSOR_DELTA_MZ) && neutralMassDeltaMz != 0.0d)
			return mzFormat.format(neutralMassDeltaMz);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.PRECURSOR_PURITY) && precursorPurity != 0.0d)
			return entropyFormat.format(precursorPurity);
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.FDR_Q_VALUE) && qValue != 0.0d) {
			if(qValue < 0.001d || qValue > 1000.d)
				return sciFormatter.format(qValue);
			else
				return entropyFormat.format(qValue);
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.POSTERIOR_PROBABILITY) && posteriorProbability != 0.0d) {
			if(posteriorProbability < 0.001d || posteriorProbability > 1000.d)
				return sciFormatter.format(posteriorProbability);
			else
				return entropyFormat.format(posteriorProbability);	
		}
		if(property.equals(IDTrackerFeatureIdentificationProperties.PERCOLATOR_SCORE) && percolatorScore != 0.0d)
			return entropyFormat.format(percolatorScore);	
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.FEATURE_MSMS))
			return featureSpectrumArray;	
		
		if(property.equals(IDTrackerFeatureIdentificationProperties.LIBRARY_MATCH_MSMS))
			return libraryHitSpectrumArray;
		
		return "";
	}	
	
	protected Double calculateRetentionShift(MsFeatureIdentity id, double observedRt) {

		if(id.getMsRtLibraryMatch() == null)
			return null;

		double expectedRt = id.getMsRtLibraryMatch().getExpectedRetention();
		if(expectedRt == 0.0d) 
			return null;

		return observedRt - expectedRt;		
	}
	
}
