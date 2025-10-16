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
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCitation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBPathway;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBUtils;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBParserJdom2;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBProteinTarget;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBRecord;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBToxCategory;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBToxProperties;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBToxType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class T3DBParseAndUploadTask extends HMDBParseAndUploadTask {
		
	private Collection<T3DBRecord>t3dbRecords;
	private Collection<CompoundProperty>hmdbCompoundProperties;
	private Collection<CompoundBioLocation>hmdbCompoundBioLocations;
	private Collection<HMDBPathway>hmdbPathways;
	private Map<Integer, T3DBToxType> toxTypes;
	private Map<Integer, T3DBToxCategory> toxCategories;
	private Map<Integer, T3DBProteinTarget>proteinTargets;
	private Map<Integer, String> toxCategoryIdMap;
	private Map<Integer, String> toxTypeIdMap;
	private Map<Integer, String>targetIdMap;
	
	public T3DBParseAndUploadTask(File t3bXmlFile) {
		super(t3bXmlFile);
		t3dbRecords = new TreeSet<T3DBRecord>();
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
			return;
		}
		try {
			getHMDBMetadata();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			errorMessage = e1.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			extractRedundantData();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			errorMessage = e1.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			insertRedundantData();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			errorMessage = e1.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			uploadRecordsToDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);		
	}

	protected void parseFileToRecords() {

		taskDescription = "Parsing T3DB XML file " + xmlInputFile.getName() + " ...";		
		total = 4000;
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
			    		t3dbRecords.add(record);
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
		compoundPropertiesIdMap = new HashMap<Integer, String>();
		hmdbCompoundProperties.stream().
			forEach(l -> compoundPropertiesIdMap.put(l.hashCode(), l.getGlobalId()));
		
		hmdbCompoundBioLocations = 
				HMDBUtils.getCompoundBioLocations();
		bioLocationsIdMap = new HashMap<Integer, String>();
		hmdbCompoundBioLocations.stream().
			forEach(l -> bioLocationsIdMap.put(l.hashCode(), l.getGlobalId()));
		
		hmdbPathways = 
				HMDBUtils.getHMDBPathways(conn);		
		pathwaysIdMap = new HashMap<Integer, String>();
		hmdbPathways.stream().
			forEach(l -> pathwaysIdMap.put(l.hashCode(), l.getGlobalId()));
		
		referencesIdMap = HMDBUtils.getHMDBReferencesMap();
		
		ConnectionManager.releaseConnection(conn);
	}
	
	@Override
	protected void extractRedundantData() {
		
		taskDescription = "Extracting redundant data ...";		
		total = t3dbRecords.size();
		processed = 0;
		
		toxTypes = new HashMap<Integer, T3DBToxType>();
		toxCategories = new HashMap<Integer, T3DBToxCategory>();
		proteinTargets = new HashMap<Integer, T3DBProteinTarget>();
		
		Map<Integer, CompoundBioLocation> missingBiolocations = 
				new HashMap<Integer, CompoundBioLocation>();
		Map<Integer, HMDBPathway>missingPathways = 
				new HashMap<Integer, HMDBPathway>();
		Map<Integer, CompoundProperty>missingCompoundProperties = 
				new HashMap<Integer, CompoundProperty>();
		Map<Integer, HMDBCitation>missingReferences = 
				new HashMap<Integer, HMDBCitation>();
				
		for(T3DBRecord record : t3dbRecords) {
			
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
			
			if(!record.getProteinTargets().isEmpty()){
				
				record.getProteinTargets().stream().
					forEach(t -> proteinTargets.put(t.hashCode(), t));
				
				record.getTagetReferences().values().stream().
					flatMap(r -> r.stream()).
					filter(r -> referencesIdMap.get(r.hashCode()) == null).	
					forEach(r -> missingReferences.put(r.hashCode(),r));
			}
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
		try {
			uploadProteinTargets(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		ConnectionManager.releaseConnection(conn);
	}

	private void uploadProteinTargets(Connection conn) throws Exception{
		
		taskDescription = "Uploading TOX categories ...";
		total = proteinTargets.size();
		processed = 0;
		targetIdMap = new HashMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.T3DB_TARGETS (TARGET_ID, NAME, "
				+ "UNIPROT_ID, MECHANISM) VALUES(?,?,?,?)";			
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, T3DBProteinTarget> typeEntry : proteinTargets.entrySet()) {
			
			T3DBProteinTarget tgt = typeEntry.getValue();
			
			ps.setString(1, tgt.getTargetId());
			ps.setString(2, tgt.getName());
			ps.setString(3, tgt.getUniprotId());
			ps.setString(4, tgt.getMechanismOfAction());
			ps.executeUpdate();
			targetIdMap.put(typeEntry.getValue().hashCode(), tgt.getTargetId());
			processed++;
		}
		ps.close();
	}

	private void uploadToxCategories(Connection conn) throws Exception{

		taskDescription = "Uploading TOX categories ...";
		total = toxCategories.size();
		processed = 0;
		toxCategoryIdMap = new HashMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.T3DB_CATEGORY "
				+ "(CATEGORY_ID, CATEGORY_NAME) VALUES (?, ?)";			
		PreparedStatement ps = conn.prepareStatement(query);	
		for(Entry<Integer, T3DBToxCategory> categoryEntry : toxCategories.entrySet()) {
			
			String categoryId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.T3DB_TOX_CATEGORY_SEQ", 
					DataPrefix.T3DB_TOX_CATEGORY, "0", 6);
			ps.setString(1, categoryId);
			ps.setString(2, categoryEntry.getValue().getName());
			ps.executeUpdate();
			toxCategoryIdMap.put(categoryEntry.getValue().hashCode(), categoryId);
			processed++;
		}
		ps.close();
	}

	private void uploadToxTypes(Connection conn) throws Exception{
		 
		taskDescription = "Uploading TOX categories ...";
		total = toxTypes.size();
		processed = 0;
		toxTypeIdMap = new HashMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.T3DB_TOX_TYPE "
				+ "(TYPE_ID, TYPE_NAME) VALUES (?, ?)";							
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, T3DBToxType> typeEntry : toxTypes.entrySet()) {
			
			String typeId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.T3DB_TOX_TYPE_SEQ", 
					DataPrefix.T3DB_TOX_TYPE, "0", 6);
			ps.setString(1, typeId);
			ps.setString(2, typeEntry.getValue().getName());
			ps.executeUpdate();
			toxTypeIdMap.put(typeEntry.getValue().hashCode(), typeId);
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
				"INSERT INTO COMPOUNDDB.T3DB_COMPOUND_DATA "
				+"(ACCESSION, "                      	// 1
				+ "CREATION_DATE, "                  	// 2
				+ "UPDATE_DATE, "                    	// 3
				+ "COMMON_NAME, "                    	// 4
				+ "CHEMICAL_FORMULA, "               	// 5
				+ "MONISOTOPIC_MOLECULATE_WEIGHT, "  	// 6
				+ "SMILES, "                         	// 7
				+ "INCHI, "                          	// 8
				+ "INCHIKEY, "                       	// 9
				+ "STATUS, "                        	// 10
				+ "ORIGIN, "                         	// 11
				+ "AGGREGATE_STATE, "                	// 12
				+ "APPEARANCE, "                     	// 13
				+ "ROUTE_OF_EXPOSURE, "              	// 14
				+ "DESCRIPTION, "                    	// 15
				+ "MECHANISM_OF_TOXICITY, "          	// 16
				+ "METABOLISM, "                    	// 17
				+ "TOXICITY, "                       	// 18
				+ "LETHALDOSE, "                     	// 19
				+ "CARCINOGENICITY, "                	// 20
				+ "USE_SOURCE, "                     	// 21
				+ "MIN_RISK_LEVEL, "                 	// 22
				+ "HEALTH_EFFECTS, "                 	// 23
				+ "SYMPTOMS, "                       // 24
				+ "TREATMENT) "                      // 25
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
		
		//	Targets map
		String proteinTargetsQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_TARGET_MAP "
				+ "(TARGET_ID, ACCESSION) VALUES (?, ?)";
		PreparedStatement proteinTargetsPs = conn.prepareStatement(proteinTargetsQuery);
		
		//	Target references
		String targetsRefsQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_TARGET_LIT_REFERENCE_MAP "
				+ "(TARGET_ID, ACCESSION, LIT_REF_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement targetRefsPs = conn.prepareStatement(targetsRefsQuery);
		
		//	General references
		String genRefQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_GENERAL_LIT_REFERENCE_MAP "
				+ "(ACCESSION, LIT_REF_ID) "
				+ "VALUES (?, ?)";
		PreparedStatement genRefPs = conn.prepareStatement(genRefQuery);
		
		String categoriesQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_CATEGORY_MAP "
				+ "(ACCESSION, CATEGORY_ID) "
				+ "VALUES (?, ?)";
		PreparedStatement categoriesPs = conn.prepareStatement(categoriesQuery);

		String typesQuery = 
				"INSERT INTO COMPOUNDDB.T3DB_TOX_TYPE_MAP "
				+ "(ACCESSION, TYPE_ID) "
				+ "VALUES (?, ?)";
		PreparedStatement typesPs = conn.prepareStatement(typesQuery);
		
		for(T3DBRecord record : t3dbRecords) {
			
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
			if(!record.getProteinTargets().isEmpty()) {
				try {
					insertProteinTargetsWithReferences(
							record, conn, proteinTargetsPs, targetRefsPs);
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
			//	Categories
			if(!record.getCategories().isEmpty()) {				
				try {
					insertCategories(record, conn, categoriesPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			//	Types
			if(!record.getTypes().isEmpty()) {				
				try {
					insertTypes(record, conn, typesPs);
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
		proteinTargetsPs.close();
		targetRefsPs.close();
		genRefPs.close();		
		categoriesPs.close();
		typesPs.close();
		
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
		compoundDataPs.setDate(2, 
				new java.sql.Date(record.getDateCreated().getTime()));
		compoundDataPs.setDate(3, 
				new java.sql.Date(record.getLastUpdated().getTime()));
		compoundDataPs.setString(4, record.getName());
		compoundDataPs.setString(5, cid.getFormula());
		compoundDataPs.setDouble(6, exactMass);
		compoundDataPs.setString(7, cid.getSmiles());
		compoundDataPs.setString(8, cid.getInChi());
		compoundDataPs.setString(9, cid.getInChiKey());	
		
		String status = record.getToxicityProperties().get(T3DBToxProperties.status);
		if(status != null)
			compoundDataPs.setString(10, status);
		else
			compoundDataPs.setNull(10, java.sql.Types.NULL);
		
		String origin = record.getToxicityProperties().get(T3DBToxProperties.origin);
		if(origin != null)
			compoundDataPs.setString(11, origin);
		else
			compoundDataPs.setNull(11, java.sql.Types.NULL);
		
		if(record.getAggregateState() != null)
			compoundDataPs.setString(12, record.getAggregateState());
		else
			compoundDataPs.setNull(12, java.sql.Types.NULL);
		
		String appearance = record.getToxicityProperties().get(T3DBToxProperties.appearance);
		if(appearance != null)
			compoundDataPs.setString(13, appearance);
		else
			compoundDataPs.setNull(13, java.sql.Types.NULL);
		
		String route_of_exposure = record.getToxicityProperties().get(T3DBToxProperties.route_of_exposure);
		if(route_of_exposure != null)
			compoundDataPs.setString(14, route_of_exposure);
		else
			compoundDataPs.setNull(14, java.sql.Types.NULL);
		
		if(record.getDescription() != null)
			compoundDataPs.setString(15, record.getDescription());
		else
			compoundDataPs.setNull(15, java.sql.Types.NULL);
		
		String mechanismOfToxicity = record.getToxicityProperties().get(T3DBToxProperties.mechanism_of_toxicity);
		if(mechanismOfToxicity != null)
			compoundDataPs.setString(16, mechanismOfToxicity);
		else
			compoundDataPs.setNull(16, java.sql.Types.NULL);
		
		String metabolism = record.getToxicityProperties().get(T3DBToxProperties.metabolism);
		if(metabolism != null)
			compoundDataPs.setString(17, metabolism);
		else
			compoundDataPs.setNull(17, java.sql.Types.NULL);
		
		String toxicity = record.getToxicityProperties().get(T3DBToxProperties.toxicity);
		if(toxicity != null)
			compoundDataPs.setString(18, toxicity);
		else
			compoundDataPs.setNull(18, java.sql.Types.NULL);
		
		String lethaldose = record.getToxicityProperties().get(T3DBToxProperties.lethaldose);
		if(lethaldose != null)
			compoundDataPs.setString(19, lethaldose);
		else
			compoundDataPs.setNull(19, java.sql.Types.NULL);
		
		String carcinogenicity = record.getToxicityProperties().get(T3DBToxProperties.carcinogenicity);
		if(carcinogenicity != null)
			compoundDataPs.setString(20, carcinogenicity);
		else
			compoundDataPs.setNull(20, java.sql.Types.NULL);
		
		String use_source = record.getToxicityProperties().get(T3DBToxProperties.use_source);
		if(use_source != null)
			compoundDataPs.setString(21, use_source);
		else
			compoundDataPs.setNull(21, java.sql.Types.NULL);
		
		String min_risk_level = record.getToxicityProperties().get(T3DBToxProperties.min_risk_level);
		if(min_risk_level != null)
			compoundDataPs.setString(22, min_risk_level);
		else
			compoundDataPs.setNull(22, java.sql.Types.NULL);
		
		String health_effects = record.getToxicityProperties().get(T3DBToxProperties.health_effects);
		if(health_effects != null)
			compoundDataPs.setString(23, health_effects);
		else
			compoundDataPs.setNull(23, java.sql.Types.NULL);
		
		String symptoms = record.getToxicityProperties().get(T3DBToxProperties.symptoms);
		if(symptoms != null)
			compoundDataPs.setString(24, symptoms);
		else
			compoundDataPs.setNull(24, java.sql.Types.NULL);
		
		String treatment = record.getToxicityProperties().get(T3DBToxProperties.treatment);
		if(treatment != null)
			compoundDataPs.setString(25, treatment);
		else
			compoundDataPs.setNull(25, java.sql.Types.NULL);
		
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
		dbCrossrefPs.setString(2, CompoundDatabaseEnum.T3DB.name() );
		dbCrossrefPs.setString(3, record.getPrimaryId());
		dbCrossrefPs.addBatch();
		for(Entry<CompoundDatabaseEnum, String> dbRef : record.getCompoundIdentity().getDbIdMap().entrySet()) {
			
			dbCrossrefPs.setString(2, dbRef.getKey().name());
			dbCrossrefPs.setString(3, dbRef.getValue());
			dbCrossrefPs.addBatch();
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

	protected void insertProteinTargetsWithReferences(
			T3DBRecord record, 
			Connection conn,
			PreparedStatement proteinTargetsPs, 
			PreparedStatement targetRefsPs) throws Exception{

		Collection<T3DBProteinTarget> targets = record.getProteinTargets();
		proteinTargetsPs.setString(2, record.getPrimaryId());
		targetRefsPs.setString(2, record.getPrimaryId());
		
		for(T3DBProteinTarget target : targets) {
			
			proteinTargetsPs.setString(1, target.getTargetId());			
			proteinTargetsPs.addBatch();
			Collection<HMDBCitation>tgtRefs = 
					record.getTagetReferences().get(target);
			
			if(tgtRefs != null && !tgtRefs.isEmpty()) {
				
				targetRefsPs.setString(1, target.getTargetId());
				for(HMDBCitation ref : tgtRefs) {
					
					String refId = referencesIdMap.get(ref.hashCode());
					if(refId != null) {
						targetRefsPs.setString(3, refId);
						targetRefsPs.addBatch();
					}					
				}
			}
		}
		proteinTargetsPs.executeBatch();
		targetRefsPs.executeBatch();
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
	
	private void insertTypes(
			T3DBRecord record, 			
			Connection conn, 
			PreparedStatement typesPs) throws Exception{

		typesPs.setString(1, record.getPrimaryId());
		for(String type : record.getTypes()) {
			
			String typeId = toxTypeIdMap.get(type.hashCode());
			if(typeId != null) {
				typesPs.setString(2, typeId);
				typesPs.addBatch();
			}
		}
		typesPs.executeBatch();		
	}

	private void insertCategories(
			T3DBRecord record, 
			Connection conn, 
			PreparedStatement categoriesPs) throws Exception{

		categoriesPs.setString(1, record.getPrimaryId());
		for(String category : record.getCategories()) {
			
			String catId = toxCategoryIdMap.get(category.hashCode());
			if(catId != null) {
				categoriesPs.setString(2, catId);
				categoriesPs.addBatch();
			}
		}
		categoriesPs.executeBatch();
	}
}






