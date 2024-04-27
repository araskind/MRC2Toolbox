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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.PepSearchOutputObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.OfflineExperimentLoadCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTPepSearchOutputFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class NISTMsPepSearchDataUploadTask extends NISTMsPepSearchTask {
	
	private boolean validateOnly;
	private boolean addMissingParameters;
	private int maxHitsPerFeature;
	private boolean writeResultsFileWithSpectra;
	private Map<String,Collection<MsPoint>>spectraMap;
	
	private static final PepSearchOutputObjectComparator psoScoreComparator = 
			new PepSearchOutputObjectComparator(SortProperty.msmsScore, SortDirection.DESC);

	public NISTMsPepSearchDataUploadTask(
			File resultFile, 
			NISTPepSearchParameterObject pepSearchParameterObject,
			int maxHitsPerFeature) {
		this(resultFile, pepSearchParameterObject, false, false, maxHitsPerFeature, false);
		this.maxHitsPerFeature = maxHitsPerFeature;
	}
	
	public NISTMsPepSearchDataUploadTask(
			File resultFile, 
			NISTPepSearchParameterObject pepSearchParameterObject, 
			boolean validateOnly,
			boolean addMissingParameters,
			int maxHitsPerFeature, 
			boolean writeResultsFileWithSpectra) {
		super();
		this.resultFile = resultFile;
		this.validateOnly = validateOnly;
		this.pepSearchParameterObject = pepSearchParameterObject;
		this.addMissingParameters = addMissingParameters;
		this.maxHitsPerFeature = maxHitsPerFeature;
		this.writeResultsFileWithSpectra = writeResultsFileWithSpectra;
	}

	@Override
	public void run() {

		pooList = new ArrayList<PepSearchOutputObject>();
		pooListForUpdateOnly = new ArrayList<PepSearchOutputObject>();
		setStatus(TaskStatus.PROCESSING);
		initLogFile();
		try {
			parseSearchResults();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		if(writeResultsFileWithSpectra) {
			
			try {
				assignLibraryIds();
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				getMSMSLibraryEntries();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				getFeatureTandemSpectra();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				writeOutResultsFileWithSpectra();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setStatus(TaskStatus.FINISHED);
			return;
		}
		try {
			filterSearchResults(false);
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		if(validateOnly) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(!pooList.isEmpty()) {
			
			try {
				uploadSearchResults();
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
return;

			}
		}
		if(!pooListForUpdateOnly.isEmpty() && addMissingParameters) {
			try {
				updateExistingHits();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
return;

			}
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void writeOutResultsFileWithSpectra() throws Exception {

		taskDescription = "Writing out results with spectra ...";
		total = pooList.size();
		processed = 0;		
		ArrayList<String>contents = new ArrayList<String>();
		//	File header		
		String[]headerChunks = new String[] {
			NISTPepSearchOutputFields.UNKNOWN.getName(),
			NISTPepSearchOutputFields.LIBRARY.getName(),
			NISTPepSearchOutputFields.ID.getName(),
			NISTPepSearchOutputFields.NIST_RN.getName(),
			NISTPepSearchOutputFields.PEPTIDE.getName(),
			NISTPepSearchOutputFields.RANK.getName(),
			NISTPepSearchOutputFields.DELTA_MZ.getName(),
			NISTPepSearchOutputFields.SCORE.getName(),
			NISTPepSearchOutputFields.DOT_PRODUCT.getName(),
			NISTPepSearchOutputFields.PROB.getName(),
			NISTPepSearchOutputFields.REVERSE_DOT_PRODUCT.getName(),
			NISTPepSearchOutputFields.HYBRID_DOT_PRODUCT.getName(),
			NISTPepSearchOutputFields.HYBRID_SCORE.getName(),
			NISTPepSearchOutputFields.HYBRID_DELTA_MZ.getName(),
			NISTPepSearchOutputFields.UNKNOWN_INCHIKEY.getName(),
			"MRC2_LIB_ID",
			"FeatureSpectrum",
			"LibrarySpectrum",
			"SMILES",
			"InChiKey"
		};
		contents.add(StringUtils.join(headerChunks, "\t"));		
		
		for(PepSearchOutputObject poo : pooList) {
			
			ArrayList<String>line = new ArrayList<String>();
			
			String msmsFeatureId = ((poo.getMsmsFeatureId() == null) ? "" : poo.getMsmsFeatureId());
			line.add(msmsFeatureId);	//	NISTPepSearchOutputFields.UNKNOWN
			String libraryName = ((poo.getLibraryName() == null) ? "" : poo.getLibraryName());
			line.add(libraryName);		//	NISTPepSearchOutputFields.LIBRARY
			String dbNum = ((poo.getDatabaseNumber() == null) ? "" : poo.getDatabaseNumber());
			line.add(dbNum);	//	NISTPepSearchOutputFields.ID
			String nistReg = ((poo.getNistRegId() == null) ? "" : poo.getNistRegId());
			line.add(nistReg);		//	NISTPepSearchOutputFields.NIST_RN
			String peptide = ((poo.getPeptide() == null) ? "" : poo.getPeptide());
			line.add(peptide);			//	NISTPepSearchOutputFields.PEPTIDE
			String matchRank = ((poo.getMatchRank() == 0) ? "" : Integer.toString(poo.getMatchRank()));
			line.add(matchRank);	//	NISTPepSearchOutputFields.RANK
			String deltaMz = ((poo.getDeltaMz() == 0) ? "" : MsUtils.spectrumMzExportFormat.format(poo.getDeltaMz()));
			line.add(deltaMz); //	NISTPepSearchOutputFields.DELTA_MZ
			String score = ((poo.getScore() == 0) ? "" : MsUtils.spectrumIntensityFormat.format(poo.getScore()));
			line.add(score); //	NISTPepSearchOutputFields.SCORE
			String dotProd = ((poo.getDotProduct() == 0) ? "" : MsUtils.spectrumIntensityFormat.format(poo.getDotProduct()));
			line.add(dotProd); //	NISTPepSearchOutputFields.DOT_PRODUCT
			String prob = ((poo.getProbablility() == 0) ? "" : MsUtils.spectrumIntensityFormat.format(poo.getProbablility()));
			line.add(prob); //	NISTPepSearchOutputFields.PROB
			String revDot = ((poo.getReverseDotProduct() == 0) ? "" : MsUtils.spectrumIntensityFormat.format(poo.getReverseDotProduct()));
			line.add(revDot); //	NISTPepSearchOutputFields.REVERSE_DOT_PRODUCT
			String hybDot = ((poo.getHybridDotProduct() == 0) ? "" : MsUtils.spectrumIntensityFormat.format(poo.getHybridDotProduct()));
			line.add(hybDot); //	NISTPepSearchOutputFields.HYBRID_DOT_PRODUCT
			String hybScore = ((poo.getHybridScore() == 0) ? "" : MsUtils.spectrumIntensityFormat.format(poo.getHybridScore()));
			line.add(hybScore); //	NISTPepSearchOutputFields.HYBRID_SCORE
			String hybDeltaMz = ((poo.getHybridDeltaMz() == 0) ? "" : MsUtils.spectrumMzExportFormat.format(poo.getHybridDeltaMz()));
			line.add(hybDeltaMz); //	NISTPepSearchOutputFields.HYBRID_DELTA_MZ
			String unkInchiKey = ((poo.getUnknownInchiKey() == null) ? "" : poo.getUnknownInchiKey());
			line.add(unkInchiKey);	//	NISTPepSearchOutputFields.UNKNOWN_INCHIKEY			
			String mrcLibId = ((poo.getMrc2libid() == null) ? "" : poo.getMrc2libid());
			line.add(mrcLibId);	//	MRC2_LIB_ID			
			String libMsMsString = "";
			String smilesString = "";
			String libInChiKeyString = "";
			//	Library spectrum
			if(mrcLibId != null) {
				
				MsMsLibraryFeature libFeature = OfflineExperimentLoadCache.getMsMsLibraryFeatureById(mrcLibId);
				if(libFeature.getSpectrum() != null)
					libMsMsString = libFeature.getSpectrumAsPythonArray();
				
				if(libFeature.getCompoundIdentity() != null) {
					
					smilesString = ((libFeature.getCompoundIdentity().getSmiles() == null) ? "" : libFeature.getCompoundIdentity().getSmiles());
					libInChiKeyString = ((libFeature.getCompoundIdentity().getInChiKey() == null) ? "" : libFeature.getCompoundIdentity().getInChiKey());
				}
			}
			//	Feature spectrum
			Collection<MsPoint> featureMsMs = spectraMap.get(poo.getMsmsFeatureId());
			String featureMsMsString = "";
			if(featureMsMs != null)				
				featureMsMsString =  MsUtils.getSpectrumAsPythonArray(featureMsMs);	
			
			line.add(featureMsMsString);	//	FeatureSpectrum
			line.add(libMsMsString);	//	LibrarySpectrum		
			line.add(smilesString);	//	SMILES
			line.add(libInChiKeyString);	//	InChiKey		
			contents.add(StringUtils.join(line, "\t"));
			processed++;
		}	
		Path outFilePath = Paths.get(resultFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(resultFile.getName()) + "_WITH_SPECTRA.TXT");
	    try {
			Files.write(outFilePath, 
					contents, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getFeatureTandemSpectra() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Getting feature MSMS data ...";
		List<String> uniqueMSMSFeatureIds = pooList.stream().
				map(p -> p.getMsmsFeatureId()).distinct().
				sorted().collect(Collectors.toList());
		total = uniqueMSMSFeatureIds.size();
		processed = 0;	
		spectraMap = new HashMap<String,Collection<MsPoint>>();
		
		String msquery =
				"SELECT MZ, HEIGHT FROM MSMS_FEATURE_PEAK WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement msps = conn.prepareStatement(msquery);
		ResultSet msrs = null;		
		for(String featureId : uniqueMSMSFeatureIds) {

			Collection<MsPoint>msms = new TreeSet<MsPoint>(MsUtils.mzSorter);
			msps.setString(1, featureId);	
			msrs = msps.executeQuery();
			ArrayList<TandemMassSpectrum>msmsList = new ArrayList<TandemMassSpectrum>();
			while(msrs.next())
				msms.add(new MsPoint(msrs.getDouble("MZ"), msrs.getDouble("HEIGHT")));
				
			msrs.close();
			spectraMap.put(featureId, msms);
			processed++;
		}
		msps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private void getMSMSLibraryEntries() throws Exception {
		
		taskDescription = "Populating MSMS library data cache ...";
		List<String> uniqueMSMSLibraryIds = pooList.stream().
				map(p -> p.getMrc2libid()).distinct().
				sorted().collect(Collectors.toList());
		total = uniqueMSMSLibraryIds.size();
		processed = 0;	
		OfflineExperimentLoadCache.reset();
		Connection conn = ConnectionManager.getConnection();
		for(String libId : uniqueMSMSLibraryIds) {
			
			MsMsLibraryFeature libFeature = 
					MSMSLibraryUtils.getMsMsLibraryFeatureById(libId, conn);
			if(libFeature != null)
				OfflineExperimentLoadCache.addMsMsLibraryFeature(libFeature);
			
			processed++;
		}	
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {
		return new NISTMsPepSearchDataUploadTask(
				resultFile, 
				pepSearchParameterObject, 
				validateOnly, 
				addMissingParameters, 
				maxHitsPerFeature,
				writeResultsFileWithSpectra);
	}
}
