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

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.PCDLFields;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.main.config.DefaultFormatStore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class PCDLUtils {
	
	private static final Logger logger = LogManager.getLogger(PCDLUtils.class);
	
	private PCDLUtils() {
		/* This utility class should not be instantiated */
	}

	public static CompoundLibrary parsePCDLTextLibrary(File libraryFile,Collection<Adduct> adductList ) {
		
		CompoundLibrary pcdlLibrary = 
				new CompoundLibrary(PathUtils.getBaseName(libraryFile.toPath()));
		String[][] compoundDataArray = DelimitedTextParser.parseTextFile(
				libraryFile, MRC2ToolBoxConfiguration.getTabDelimiter());
				
		Map<PCDLFields, Integer> dataFieldMap = new TreeMap<PCDLFields, Integer>();
		try {
			dataFieldMap = createAndValidatePCDLFieldMap(compoundDataArray[0]);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		}
		if(dataFieldMap.isEmpty())
			return pcdlLibrary;
				
		for(int i=1; i<compoundDataArray.length; i++) {
			
			double rt = 0.0d;
			String cpdName = compoundDataArray[i][dataFieldMap.get(PCDLFields.NAME)];
			String formula = compoundDataArray[i][dataFieldMap.get(PCDLFields.FORMULA)];
			if(cpdName == null || cpdName.isEmpty() || formula == null || formula.isEmpty())
				continue;
			
			CompoundIdentity identity = new CompoundIdentity(
								compoundDataArray[i][dataFieldMap.get(PCDLFields.NAME)], 
								compoundDataArray[i][dataFieldMap.get(PCDLFields.FORMULA)]);
							
			if(dataFieldMap.containsKey(PCDLFields.HMP)) {
				
				String hmdbId = compoundDataArray[i][dataFieldMap.get(PCDLFields.HMP)];
				if(hmdbId != null && !hmdbId.isEmpty())
					identity.addDbId(CompoundDatabaseEnum.HMDB, hmdbId);
			}
			if(dataFieldMap.containsKey(PCDLFields.PUBCHEM)) {
				
				String pubchemId = compoundDataArray[i][dataFieldMap.get(PCDLFields.PUBCHEM)];
				if(pubchemId != null && !pubchemId.isEmpty())
					identity.addDbId(CompoundDatabaseEnum.PUBCHEM, pubchemId);
			}
			if(dataFieldMap.containsKey(PCDLFields.LMP)) {
				
				String lipidMapsId = compoundDataArray[i][dataFieldMap.get(PCDLFields.LMP)];
				if(lipidMapsId != null && !lipidMapsId.isEmpty())
					identity.addDbId(CompoundDatabaseEnum.LIPIDMAPS, lipidMapsId);
			}
			if(dataFieldMap.containsKey(PCDLFields.INCHI_KEY)) {
				
				String inchiKey = compoundDataArray[i][dataFieldMap.get(PCDLFields.INCHI_KEY)];
				if(inchiKey != null && !inchiKey.isEmpty())
					identity.setInChiKey(inchiKey);
			}
			if(dataFieldMap.containsKey(PCDLFields.SMILES)) {
				
				String smiles = compoundDataArray[i][dataFieldMap.get(PCDLFields.SMILES)];
				if(smiles != null && !smiles.isEmpty())
					identity.setSmiles(smiles);
			}
			if(dataFieldMap.containsKey(PCDLFields.RETENTION_TIME)) {
				
				String rtString = compoundDataArray[i][dataFieldMap.get(PCDLFields.RETENTION_TIME)];
				if(rtString != null && !rtString.isEmpty()) {

					try {
						rt = Double.valueOf(rtString);
					} catch (NumberFormatException e) {
						logger.error(String.format("Failed to parse retention time %s", rtString), e);
					}
				}
			}
			LibraryMsFeature newTarget = 
					new LibraryMsFeature(identity.getName(), null, rt);
			MsFeatureIdentity mid = new MsFeatureIdentity(
					identity, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
			newTarget.setPrimaryIdentity(mid);
			MSRTLibraryUtils.generateMassSpectrumFromAdducts(newTarget, adductList);
			newTarget.setNeutralMass(identity.getExactMass());			
			newTarget.setName(identity.getName());
			if(newTarget.getName().toUpperCase().contains("[ISTD]"))
				mid.setQcStandard(true);
			
			pcdlLibrary.addFeature(newTarget);	
		}
		return pcdlLibrary;
	}
	
	private static Map<PCDLFields, Integer> createAndValidatePCDLFieldMap(String[]header) throws IllegalArgumentException {
		
		Map<PCDLFields, Integer>dataFieldMap = new TreeMap<PCDLFields, Integer>();
		ArrayList<String>missingFields = new ArrayList<String>();
		for(int i=0; i<header.length; i++) {
			
			PCDLFields f = PCDLFields.getOptionByUIName(header[i]);
			if(f != null)
				dataFieldMap.put(f, i);
		}		
		if(!dataFieldMap.containsKey(PCDLFields.NAME))
			missingFields.add(PCDLFields.NAME.getName());

		if(!dataFieldMap.containsKey(PCDLFields.FORMULA))
			missingFields.add(PCDLFields.FORMULA.getName());
		
		if(!missingFields.isEmpty()) {
			String message ="The following obligatory fields are missing form the input data:\n"
					+ StringUtils.join(missingFields, ", ");
			throw new IllegalArgumentException(message);
		}
		return dataFieldMap;			
	}
	
	public static void writePCDLLibrary(Collection<LibraryMsFeature>features, File outputFile) {
		
		NumberFormat mzFormat = DefaultFormatStore.getDefaultMZformat();
		NumberFormat rtFormat = DefaultFormatStore.getDefaultRTformat();
		
		StringBuilder sb = new StringBuilder();
		sb.append(PCDLFields.NAME.getName()).append("\t")
		  .append(PCDLFields.FORMULA.getName()).append("\t")
		  .append(PCDLFields.MASS.getName()).append("\t")
		  .append(PCDLFields.RETENTION_TIME.getName()).append("\t")
		  .append(PCDLFields.RETENTION_INDEX.getName()).append("\t")
		  .append(PCDLFields.CATION.getName()).append("\t")
		  .append(PCDLFields.ANION.getName()).append("\t")
		  .append(PCDLFields.CAS.getName()).append("\t")
		  .append(PCDLFields.CHEMSPIDER.getName()).append("\t")
		  .append(PCDLFields.PUBCHEM.getName()).append("\t")
		  .append(PCDLFields.SYNONYMS.getName()).append("\t")
		  .append(PCDLFields.IUPAC.getName()).append("\t")
		  .append(PCDLFields.NUMSPECTRA.getName()).append("\t")
		  .append(PCDLFields.CCS_COUNT.getName()).append("\n");
		
		for(LibraryMsFeature f : features) {
			
			CompoundIdentity identity = f.getPrimaryIdentity().getCompoundIdentity();
			sb.append(identity.getName()).append("\t")	//	NAME
			  .append(identity.getFormula()).append("\t")	//	FORMULA
			  .append(mzFormat.format(identity.getExactMass())).append("\t")	//	MASS
			  .append(rtFormat.format(f.getRetentionTime())).append("\t")	//	RETENTION_TIME
			  .append("\t")	//	RETENTION_INDEX
			  .append("\t")	//	CATION
			  .append("\t")	//	ANION
			  .append(identity.getDbId(CompoundDatabaseEnum.CAS) == null ? "" : identity.getDbId(CompoundDatabaseEnum.CAS)).append("\t") //	CAS
			  .append(identity.getDbId(CompoundDatabaseEnum.CHEMSPIDER) == null ? "" : identity.getDbId(CompoundDatabaseEnum.CHEMSPIDER)).append("\t")	//	CHEMSPIDER
			  .append(identity.getDbId(CompoundDatabaseEnum.PDBECHEM) == null ? "" : identity.getDbId(CompoundDatabaseEnum.PDBECHEM)).append("\t")	//	PUBCHEM	
			  .append("\t")	//	SYNONYMS
			  .append("\t")	//	IUPAC
			  .append("0").append("\t")	//	NUMSPECTRA
			  .append("0").append("\t")	//	CCS_COUNT
			  .append("\n");
		}
		try {
			Files.writeString(outputFile.toPath(), 
					sb.toString(), 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			logger.error("Failed to write PCDL library to file " + outputFile, e);
		}		
	}
}
