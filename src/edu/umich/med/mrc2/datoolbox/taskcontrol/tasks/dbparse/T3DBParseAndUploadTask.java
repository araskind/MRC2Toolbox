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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.dbparse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.jdom2.input.DOMBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundNameCategory;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.CompoundBioLocation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.CompoundConcentration;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCitation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBDesease;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBPathway;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBProteinAssociation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBUtils;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBParserJdom2;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBRecord;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBToxCategory;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBToxType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class T3DBParseAndUploadTask extends HMDBParseAndUploadTask {
	
	
	protected Collection<T3DBRecord>records;
	private Collection<CompoundProperty>hmdbCompoundProperties;
	private Collection<CompoundBioLocation>hmdbCompoundBioLocations;
	private Collection<HMDBPathway>hmdbPathways;
	private HashMap<Integer, T3DBToxType> toxTypes;
	private HashMap<Integer, T3DBToxCategory> toxCategories;
	private TreeMap<Integer, String> toxCategoryIdMap;
	private TreeMap<Integer, String> toxTypeIdMap;
	
	public T3DBParseAndUploadTask(File t3bXmlFile) {
		super(t3bXmlFile);
		records = new TreeSet<T3DBRecord>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			parseFileToRecords();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
		}
		try {
			getHMDBMetadata();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			extractRedundantData();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			insertRedundantData();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			uploadRecordsToDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);		
	}

	protected void parseFileToRecords() {

		taskDescription = "Parsing T3DB XML file " + xmlInputFile.getName() + " ...";		
		total = 200000;
		processed = 0;
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		
		XMLInputFactory xif = null;
		try {
			xif = XMLInputFactory.newInstance();
		} catch (FactoryConfigurationError e1) {

			e1.printStackTrace();
			errorMessage = e1.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
		TransformerFactory tf = null;
		try {
			tf = TransformerFactory.newInstance();
		} catch (TransformerFactoryConfigurationError e2) {

			e2.printStackTrace();
			errorMessage = e2.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}		
		Transformer t = null;
		try {
			t = tf.newTransformer();
		} catch (TransformerConfigurationException e2) {
			
			e2.printStackTrace();
			errorMessage = e2.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		
		DOMBuilder domBuider = new DOMBuilder();
		
		XMLStreamReader xsr = null;
		try {
			xsr = xif.createXMLStreamReader(new FileReader(xmlInputFile));
			xsr.nextTag();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (XMLStreamException e1) {

			e1.printStackTrace();
			errorMessage = e1.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
        try {						
			while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {

			    DOMResult result = new DOMResult();
			    t.transform(new StAXSource(xsr), result);
			    Node domNode = result.getNode();
			    if(domNode.getFirstChild().getNodeName().equals("compound")){
			    	
				    org.jdom2.Element domElement = domBuider.build((Element)domNode.getFirstChild());
			    	T3DBRecord record = null;
			    	try {
			    		record = T3DBParserJdom2.parseRecord(domElement);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	if(record != null) {
			    		records.add(record);
				    	System.out.println("Parsed - " + record.getName());
			    	}
			    	processed++;
			    }
			}
		}
        catch (Exception e) {
			e.printStackTrace();
		}	
	}

	private void getHMDBMetadata() throws Exception{

		Connection conn = ConnectionManager.getConnection();
		
		hmdbCompoundProperties = 
				HMDBUtils.getCompoundProperties(conn);
		compoundPropertiesIdMap = new TreeMap<Integer, String>();
		hmdbCompoundProperties.stream().
			forEach(l -> compoundPropertiesIdMap.put(l.hashCode(), l.getGlobalId()));
		
		hmdbCompoundBioLocations = 
				HMDBUtils.getCompoundBioLocations();
		bioLocationsIdMap = new TreeMap<Integer, String>();
		hmdbCompoundBioLocations.stream().
			forEach(l -> bioLocationsIdMap.put(l.hashCode(), l.getGlobalId()));
		
		hmdbPathways = 
				HMDBUtils.getHMDBPathways(conn);		
		pathwaysIdMap = new TreeMap<Integer, String>();
		hmdbPathways.stream().
			forEach(l -> pathwaysIdMap.put(l.hashCode(), l.getGlobalId()));
		
		referencesIdMap = HMDBUtils.getHMDBReferencesMap();
		
		ConnectionManager.releaseConnection(conn);
	}
	
	@Override
	protected void extractRedundantData() {
		
		taskDescription = "Extracting redundant data ...";		
		total = records.size();
		processed = 0;
		
		toxTypes = new HashMap<Integer, T3DBToxType>();
		toxCategories = new HashMap<Integer, T3DBToxCategory>();
		Map<Integer, CompoundBioLocation> missingBiolocations = 
				new HashMap<Integer, CompoundBioLocation>();
		Map<Integer, HMDBPathway>missingPathways = 
				new HashMap<Integer, HMDBPathway>();
		Map<Integer, CompoundProperty>missingCompoundProperties = 
				new HashMap<Integer, CompoundProperty>();
		Map<Integer, HMDBCitation>missingReferences = 
				new HashMap<Integer, HMDBCitation>();
				
		for(T3DBRecord record : records) {
			
			if(!record.getBiolocations().isEmpty()) {
				record.getBiolocations().stream().
						filter(b -> bioLocationsIdMap.get(b.hashCode()) == null).
						forEach(b -> missingBiolocations.put(b.hashCode(),b));
			}
			if(!record.getPathways().isEmpty()) {
				record.getPathways().stream().
					filter(p -> pathwaysIdMap.get(p.hashCode()) == null).
					forEach(p -> missingPathways.put(p.hashCode(),p));
			}			
			if(!record.getReferences().isEmpty()) {
				record.getReferences().stream().
					filter(r -> referencesIdMap.get(r.hashCode()) == null).	
					forEach(r -> missingReferences.put(r.hashCode(),r));
			}				
			if(!record.getCompoundProperties().isEmpty()) {
				record.getCompoundProperties().stream().
					filter(p -> compoundPropertiesIdMap.get(p.hashCode()) == null).
					forEach(p -> missingCompoundProperties.put(p.hashCode(),p));
			}
			if(!record.getTypes().isEmpty()) {
				record.getTypes().stream().
					forEach(r -> toxTypes.put(r.hashCode(), new T3DBToxType(r)));
				
			}
			if(!record.getCategories().isEmpty())
				record.getCategories().stream().
					forEach(a -> toxCategories.put(a.hashCode(), new T3DBToxCategory(a)));
			
			processed++;
		}		
		if(!missingBiolocations.isEmpty()) {
			try {
				uploadBiolocations(missingBiolocations);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!missingPathways.isEmpty()) {
			try {
				uploadPathways(missingPathways);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!missingCompoundProperties.isEmpty()) {
			try {
				uploadCompoundProperties(missingCompoundProperties);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!missingReferences.isEmpty()) {
			try {
				uploadReferences(missingReferences);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
		
	protected void insertRedundantData() throws Exception{

		Connection conn = ConnectionManager.getConnection();		
		try {
			uploadToxTypes(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			uploadToxCategories(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ConnectionManager.releaseConnection(conn);
	}

	private void uploadToxCategories(Connection conn) throws Exception{

		taskDescription = "Uploading TOX categories ...";
		total = compoundProperties.size();
		processed = 0;
		toxCategoryIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.T3DB_CATEGORY "
				+ "(CATEGORY_ID, CATEGORY_NAME) VALUES (?, ?)";			
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, T3DBToxType> typeEntry : toxTypes.entrySet()) {
			
			String categoryId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.T3DB_TOX_CATEGORY_SEQ", 
					DataPrefix.T3DB_TOX_CATEGORY, "0", 6);
			ps.setString(1, categoryId);
			ps.setString(2, typeEntry.getValue().getName());
			ps.executeUpdate();
			toxCategoryIdMap.put(typeEntry.getValue().hashCode(), categoryId);
			processed++;
		}
		ps.close();
	}

	private void uploadToxTypes(Connection conn) throws Exception{
		// 
		taskDescription = "Uploading TOX categories ...";
		total = compoundProperties.size();
		processed = 0;
		toxTypeIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.T3DB_CATEGORY "
				+ "(CATEGORY_ID, CATEGORY_NAME) VALUES (?, ?)";			
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, T3DBToxCategory> categoryEntry : toxCategories.entrySet()) {
			
			String typeId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.T3DB_TOX_TYPE_SEQ", 
					DataPrefix.T3DB_TOX_TYPE, "0", 6);
			ps.setString(1, typeId);
			ps.setString(2, categoryEntry.getValue().getName());
			ps.executeUpdate();
			toxTypeIdMap.put(categoryEntry.getValue().hashCode(), typeId);
			processed++;
		}
		ps.close();
	}

	protected void uploadBiolocations(
			Map<Integer, CompoundBioLocation> missingBiolocations) throws Exception{
		
		taskDescription = "Uploading bio-location data ...";
		total = missingBiolocations.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_BIOLOCATION "
				+ "(LOCATION_ID, LOCATION_TYPE, LOCATION) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, CompoundBioLocation> blEntry : missingBiolocations.entrySet()) {
			
			String blId = SQLUtils.getNextIdFromSequence(
					conn, 
					"COMPOUNDDB.BIOLOC_SEQ", 
					DataPrefix.BIO_LOCATION, "0", 6);
			ps.setString(1, blId);
			ps.setString(2, blEntry.getValue().getLocationType().name());
			ps.setString(3, blEntry.getValue().getLocationName());
			ps.executeUpdate();
			bioLocationsIdMap.put(blEntry.getValue().hashCode(), blId);
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void uploadPathways(Map<Integer, HMDBPathway>missingPathways) throws Exception{
		
		taskDescription = "Uploading pathway data ...";
		total = missingPathways.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_PATHWAY "
				+ "(PATHWAY_ID, PATHWAY_NAME, KEGG_MAP_ID, SMPDB_ID) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, HMDBPathway> pathwayEntry : missingPathways.entrySet()) {
			
			String pathwayId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.PATHWAY_SEQ", 
					DataPrefix.BIOCHEMICAL_PATHWAY, "0", 6);
			ps.setString(1, pathwayId);
			ps.setString(2, pathwayEntry.getValue().getName());
			
			String keggMapId = pathwayEntry.getValue().getKeggMapId();
			if(keggMapId != null)
				ps.setString(3, keggMapId);
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			String smpdbId = pathwayEntry.getValue().getSmpdbId();
			if(smpdbId != null)
				ps.setString(4, smpdbId);
			else
				ps.setNull(4, java.sql.Types.NULL);
			
			ps.executeUpdate();
			pathwaysIdMap.put(pathwayEntry.getValue().hashCode(), pathwayId);
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void uploadCompoundProperties(
			Map<Integer, CompoundProperty>missingCompoundProperties) throws Exception{
		
		taskDescription = "Uploading compound property types ...";
		total = missingCompoundProperties.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_COMPOUND_PROPERTY "
				+ "(PROPERTY_ID, PROPERTY_TYPE, PROPERTY_NAME) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, CompoundProperty> cpropEntry : missingCompoundProperties.entrySet()) {
			
			String propId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.COMPOUND_PROPERTY_SEQ", 
					DataPrefix.COMPOUND_PROPERTY, "0", 7);
			ps.setString(1, propId);
			ps.setString(2, cpropEntry.getValue().getType().name());
			ps.setString(3, cpropEntry.getValue().getPropertyName());
			ps.executeUpdate();
			compoundPropertiesIdMap.put(cpropEntry.getValue().hashCode(), propId);
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void uploadReferences(Map<Integer, HMDBCitation>missingReferences) throws Exception{
		
		taskDescription = "Uploading literature references ...";
		total = missingReferences.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_LITERATURE_REFERENCES "
				+ "(LIT_REF_ID, PUBMED_ID, CITATION_TEXT, HASH_CODE) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, HMDBCitation> litRefEntry : missingReferences.entrySet()) {
			
			String litRefId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.LIT_REF_SEQ", 
					DataPrefix.LITERATURE_REFERENCE, "0", 8);
			ps.setString(1, litRefId);
			
			String pubMedId = litRefEntry.getValue().getPubmedId();
			if(pubMedId != null)
				ps.setString(2, pubMedId);
			else
				ps.setNull(2, java.sql.Types.NULL);
			
			ps.setString(3, litRefEntry.getValue().getCitationText());
			ps.setInt(4, litRefEntry.getValue().hashCode());
			ps.executeUpdate();
			referencesIdMap.put(litRefEntry.getValue().hashCode(), litRefId);
			processed++;
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void uploadRecordsToDatabase() throws Exception{
			
		taskDescription = "Uploading HMDB records ...";
		total = idSet.size();
		processed = 0;
		
		Connection conn = ConnectionManager.getConnection();
		
		//	Compound data
		String compoundDataQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_COMPOUND_DATA " +
				"(ACCESSION, CREATION_DATE, UPDATE_DATE, COMMON_NAME, " +
				"IUPAC_NAME, CHEMICAL_FORMULA, MONISOTOPIC_MOLECULATE_WEIGHT, " +
				"CAS_REGISTRY_NUMBER, SMILES, INCHI, INCHIKEY, STATUS, ORIGIN, " +
				"AGGREGATE_STATE, APPEARANCE, ROUTE_OF_EXPOSURE, DESCRIPTION, " +
				"MECHANISM_OF_TOXICITY, METABOLISM, TOXICITY, LETHALDOSE, " +
				"CARCINOGENICITY, USE_SOURCE, MIN_RISK_LEVEL, " +
				"HEALTH_EFFECTS, SYMPTOMS, TREATMENT) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement compoundDataPs = conn.prepareStatement(compoundDataQuery);
		
		//	Synonyms
		String synonymsQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_SYNONYMS "
				+ "(ACCESSION, NAME, NTYPE) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement synonymsPs = conn.prepareStatement(synonymsQuery);

		//	Crossref
		String dbCrossrefQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement dbCrossrefPs = conn.prepareStatement(dbCrossrefQuery);
		
		//	Properties
		String cpdPropertiesQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_COMPOUND_PROPERTY_MAP "
				+ "(ACCESSION, PROPERTY_ID, PROPERTY_VALUE, SOURCE) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement cpdPropertiesPs = conn.prepareStatement(cpdPropertiesQuery);
	
		//	Biolocations
		String biolocationsQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_BIOLOCATION_MAP "
				+ "(ACCESSION, LOCATION_ID) "
				+ "VALUES (?, ?)";			
		PreparedStatement biolocationsPs = conn.prepareStatement(biolocationsQuery);
		
		//  Pathways
		String pathwaysQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_PATHWAY_MAP "
						+ "(ACCESSION, PATHWAY_ID) "
						+ "VALUES (?, ?)";
		PreparedStatement pathwaysPs = conn.prepareStatement(pathwaysQuery);
		
		//	Targets/accession
		String concentrationsQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_CONCENTRATIONS "
				+ "(ACCESSION, CONC_ID, TYPE, UNITS, VALUE, "
				+ "AGE, SEX, SUBJECT_CONDITION, COMMENTS) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement concentrationsPs = conn.prepareStatement(concentrationsQuery);
		
		//	Target references
		String concRefsQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_CONCENTRATIONS_LIT_REFERENCES "
				+ "(CONC_ID, LIT_REF_ID) "
				+ "VALUES (?, ?)";
		PreparedStatement concRefsPs = conn.prepareStatement(concRefsQuery);
		
		//	Diseases with references
		String diseasesQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_DESEASE_LIT_REFERENCES "
				+ "(ACCESSION, DESEASE_ID, LIT_REF_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement diseasesPs = conn.prepareStatement(diseasesQuery);		
		
		//	Protein associations
		String protAssocQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_PROTEIN_ASSOCIATION_MAP "
				+ "(ACCESSION, PROTEIN_ACCESSION) "
				+ "VALUES (?, ?)";
		PreparedStatement protAssocPs = conn.prepareStatement(protAssocQuery);
		
		//	General references
		String genRefQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_GENERAL_LIT_REFERENCES "
				+ "(ACCESSION, LIT_REF_ID) "
				+ "VALUES (?, ?)";
		PreparedStatement genRefPs = conn.prepareStatement(genRefQuery);
		
		for(T3DBRecord record : records) {
			
//			T3DBRecord record = (T3DBRecord)DbParserCore.dbUploadCache.get(id);
			
			//	Compound data
			try {
				insertCompoundData(record, conn, compoundDataPs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//	Synonyms		
			try {
				insertSynonyms(record, conn, synonymsPs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}					
			//	Crossref				
			try {
				insertDatabaseCrossReferences(record, conn, dbCrossrefPs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			//	Properties
			if(!record.getCompoundProperties().isEmpty()) {				
				try {
					insertCompoundProperties(record, conn, cpdPropertiesPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
			//	Biolocations
			if(!record.getBiolocations().isEmpty()) {				
				try {
					insertBiolocations(record, conn, biolocationsPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			//  Pathways
			if(!record.getPathways().isEmpty()) {				
				try {
					insertPathways(record, conn, pathwaysPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			//	Concentration with references
			if(!record.getConcentrations().isEmpty()) {
				try {
					insertConcentrationsWithReferences(
							record, conn, concentrationsPs, concRefsPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//	Diseases with references
			if(!record.getDeseases().isEmpty()) {
				try {
					insertDiseasesWithReferences(record, conn, diseasesPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			//	Protein associations
			if(!record.getProteinAssociations().isEmpty()) {				
				try {
					insertProteinAssociations(record, conn, protAssocPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			//	General references
			if(!record.getReferences().isEmpty()) {				
				try {
					insertGeneralReferences(record, conn, genRefPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			processed++;
		}
		compoundDataPs.close();
		synonymsPs.close();
		dbCrossrefPs.close();
		cpdPropertiesPs.close();
		biolocationsPs.close();
		pathwaysPs.close();
		concentrationsPs.close();
		concRefsPs.close();
		diseasesPs.close();
		protAssocPs.close();
		genRefPs.close();
		
		ConnectionManager.releaseConnection(conn);	
	}

	protected void insertCompoundData(
			T3DBRecord record, 
			Connection conn, 
			PreparedStatement compoundDataPs) throws Exception{
		CompoundIdentity cid = record.getCompoundIdentity();
		double exactMass = 0.0d;
		try {
			exactMass = cid.getExactMass();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		compoundDataPs.setString(1, record.getPrimaryId());
		compoundDataPs.setString(2, record.getName());
		compoundDataPs.setString(3, cid.getFormula());
		compoundDataPs.setDouble(4, exactMass);
		compoundDataPs.setString(5, cid.getSmiles());
		compoundDataPs.setString(6, cid.getInChi());
		compoundDataPs.setString(7, cid.getInChiKey());		
		if(record.getAggregateState() != null)
			compoundDataPs.setString(8, record.getAggregateState());
		else
			compoundDataPs.setNull(8, java.sql.Types.NULL);
		
		if(record.getDescription() != null)
			compoundDataPs.setString(9, record.getDescription());
		else
			compoundDataPs.setNull(9, java.sql.Types.NULL);
		
		if(record.getCsDescription() != null)
			compoundDataPs.setString(10, record.getCsDescription());
		else
			compoundDataPs.setNull(10, java.sql.Types.NULL);
		
		compoundDataPs.setDate(11, new java.sql.Date(record.getDateCreated().getTime()));
		compoundDataPs.setDate(12, new java.sql.Date(record.getLastUpdated().getTime()));
		compoundDataPs.executeUpdate();
	}
	
	protected void insertSynonyms(
			T3DBRecord record, 
			Connection conn, 
			PreparedStatement synonymsPs) throws Exception{
		synonymsPs.setString(1, record.getPrimaryId());
		synonymsPs.setString(2, record.getName());
		synonymsPs.setString(3, CompoundNameCategory.PRI.name());
		synonymsPs.addBatch();
		for(String synonym : record.getSynonyms()) {
			
			synonymsPs.setString(2, synonym);
			synonymsPs.setString(3, CompoundNameCategory.SYN.name());
			synonymsPs.addBatch();
		}
		if(record.getSysName() != null) {
			
			synonymsPs.setString(2, record.getSysName());
			synonymsPs.setString(3, CompoundNameCategory.SYS.name());
			synonymsPs.addBatch();
		}
		if(record.getTraditionalIupacName() != null) {
			
			synonymsPs.setString(2, record.getTraditionalIupacName());
			synonymsPs.setString(3, CompoundNameCategory.IUP.name());
			synonymsPs.addBatch();
		}		
		synonymsPs.executeBatch();
	}

	protected void insertDatabaseCrossReferences(
			T3DBRecord record, 
			Connection conn, 
			PreparedStatement dbCrossrefPs) throws Exception{

		dbCrossrefPs.setString(1, record.getPrimaryId());
		dbCrossrefPs.setString(2, CompoundDatabaseEnum.HMDB.name() );
		dbCrossrefPs.setString(3, record.getPrimaryId());
		dbCrossrefPs.addBatch();
		for(Entry<CompoundDatabaseEnum, String> dbRef : record.getCompoundIdentity().getDbIdMap().entrySet()) {
			
			dbCrossrefPs.setString(2, dbRef.getKey().name());
			dbCrossrefPs.setString(3, dbRef.getValue());
			dbCrossrefPs.addBatch();
		}
		if(record.getSecondaryHmdbAccesions() != null 
				&& !record.getSecondaryHmdbAccesions().isEmpty()) {
			
			for(String secondaryHmdbAccesion : record.getSecondaryHmdbAccesions()) {
				
				dbCrossrefPs.setString(2, CompoundDatabaseEnum.HMDB_SECONDARY.name());
				dbCrossrefPs.setString(3, secondaryHmdbAccesion);
				dbCrossrefPs.addBatch();
			}
		}
		dbCrossrefPs.executeBatch();
	}

	protected void insertCompoundProperties(
			T3DBRecord record, 
			Connection conn,
			PreparedStatement cpdPropertiesPs) throws Exception{
		
		Collection<CompoundProperty> compoundProperties2 = record.getCompoundProperties();
		cpdPropertiesPs.setString(1, record.getPrimaryId());
		
		for(CompoundProperty prop : compoundProperties2) {
			
			String propId = compoundPropertiesIdMap.get(prop.hashCode());
			if(propId != null) {
				
				cpdPropertiesPs.setString(2, propId);
				cpdPropertiesPs.setString(3, prop.getPropertyValue());
				cpdPropertiesPs.setString(4, prop.getSource());
				cpdPropertiesPs.addBatch();
			}
		}
		cpdPropertiesPs.executeBatch();
	}

	protected void insertBiolocations(
			T3DBRecord record, 
			Connection conn,
			PreparedStatement biolocationsPs) throws Exception{
		
		Collection<CompoundBioLocation> biolocations2 = record.getBiolocations();
		biolocationsPs.setString(1, record.getPrimaryId());
		for(CompoundBioLocation biolocation : biolocations2) {
			
			String locId = bioLocationsIdMap.get(biolocation.hashCode());
			if(locId != null) {
				biolocationsPs.setString(2, locId);
				biolocationsPs.addBatch();
			}			
		}		
		biolocationsPs.executeBatch();
	}

	protected void insertPathways(
			T3DBRecord record, 
			Connection conn, 
			PreparedStatement pathwaysPs) throws Exception{

		Set<Integer> pathwayHashCodes = record.getPathways().stream().
				map(p -> p.hashCode()).collect(Collectors.toSet());
		pathwaysPs.setString(1, record.getPrimaryId());
		for(Integer pwHash : pathwayHashCodes) {
			
			String pathwayId = pathwaysIdMap.get(pwHash);
			if(pathwayId != null) {
				pathwaysPs.setString(2, pathwayId);
				pathwaysPs.addBatch();
			}
		}		
		pathwaysPs.executeBatch();
	}

	protected void insertConcentrationsWithReferences(
			T3DBRecord record, 
			Connection conn,
			PreparedStatement concentrationsPs, 
			PreparedStatement concRefsPs) throws Exception{

		Collection<CompoundConcentration> concentrations = record.getConcentrations();
		concentrationsPs.setString(1, record.getPrimaryId());
		for(CompoundConcentration conc : concentrations) {
			
			String concId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.CONCENTRATION_SEQ", 
					DataPrefix.CONCENTRATION, "0", 16);
			
			concentrationsPs.setString(2, concId);
			concentrationsPs.setString(3, conc.getType().name());
			concentrationsPs.setString(4, conc.getUnits());
			concentrationsPs.setString(5, conc.getValue());			
			concentrationsPs.setString(6, conc.getAge());			
			concentrationsPs.setString(7, conc.getSex());			
			concentrationsPs.setString(8, conc.getCondition());
			concentrationsPs.setString(9, conc.getComment());			
			concentrationsPs.addBatch();
			if(conc.getReferences() != null && !conc.getReferences().isEmpty()) {
				
				concRefsPs.setString(1, concId);
				for(HMDBCitation ref : conc.getReferences()) {
					
					String refId = referencesIdMap.get(ref.hashCode());
					if(refId != null) {
						concRefsPs.setString(2, refId);
						concRefsPs.addBatch();
					}					
				}
			}
		}
		concentrationsPs.executeBatch();
		concRefsPs.executeBatch();
	}

	protected void insertDiseasesWithReferences(
			T3DBRecord record,
			Connection conn,
			PreparedStatement diseasesPs) throws Exception{
		
		Collection<HMDBDesease> deseases2 = record.getDeseases();
		diseasesPs.setString(1, record.getPrimaryId());
		for(HMDBDesease des : deseases2) {

			String diseaseId = deseasesIdMap.get(des.hashCode());
			if(diseaseId != null) {
				
				diseasesPs.setString(2, diseaseId);
				if(des.getReferences() == null || des.getReferences().isEmpty()) {
					diseasesPs.setNull(3, java.sql.Types.NULL);
					diseasesPs.addBatch();
				}
				else {
					for(HMDBCitation ref : des.getReferences()) {
						
						String refId = referencesIdMap.get(ref.hashCode());
						if(refId != null) {
							diseasesPs.setString(3, refId);
							diseasesPs.addBatch();
						}					
					}
				}
			}
		}				
		diseasesPs.executeBatch();
	}

	protected void insertProteinAssociations(
			T3DBRecord record, 
			Connection conn,
			PreparedStatement protAssocPs) throws Exception{
		
		Collection<HMDBProteinAssociation> proteinAssocs = record.getProteinAssociations();
		protAssocPs.setString(1, record.getPrimaryId());
		
		for(HMDBProteinAssociation prAssoc : proteinAssocs) {
			
			String prAssocId = proteinAssociationsIdMap.get(prAssoc.hashCode());
			if(prAssocId != null) {
				
				protAssocPs.setString(2, prAssocId);
				protAssocPs.addBatch();
			}
		}
		protAssocPs.executeBatch();
	}

	protected void insertGeneralReferences(
			T3DBRecord record, 
			Connection conn,
			PreparedStatement genRefPs) throws Exception{

		genRefPs.setString(1, record.getPrimaryId());
		for(HMDBCitation ref : record.getReferences()) {
			
			String refId = referencesIdMap.get(ref.hashCode());
			if(refId != null) {
				genRefPs.setString(2, refId);
				genRefPs.addBatch();
			}					
		}
		genRefPs.executeBatch();
	}
}
