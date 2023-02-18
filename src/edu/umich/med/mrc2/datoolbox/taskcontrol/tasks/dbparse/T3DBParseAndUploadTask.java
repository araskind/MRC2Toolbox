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
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBRecord;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.t3db.T3DBRecord;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class T3DBParseAndUploadTask extends AbstractTask {
	
	private File hmdbXmlFile;
	
	private Collection<T3DBRecord>records;
	
	private Set<String>idSet;
	
	private Map<Integer, CompoundBioLocation>bioLocations;
	private Map<Integer, HMDBPathway>pathways;
	private Map<Integer, HMDBDesease>deseases;
	private Map<Integer, CompoundProperty>compoundProperties;
	private Map<Integer, HMDBCitation>references;
	private Map<Integer, HMDBProteinAssociation>proteinAssociations; 

	private Map<Integer, String>bioLocationsIdMap;
	private Map<Integer, String>pathwaysIdMap;
	private Map<Integer, String>deseasesIdMap;
	private Map<Integer, String>compoundPropertiesIdMap;
	private Map<Integer, String>referencesIdMap;
	private Map<Integer, String>proteinAssociationsIdMap;
	
	public T3DBParseAndUploadTask(File hmdbXmlFile) {
		super();
		this.hmdbXmlFile = hmdbXmlFile;
		records = new TreeSet<T3DBRecord>();
		idSet = new TreeSet<String>();
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
//		try {
//			extractRedundantData();
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		try {
//			insertRedundantData();
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		try {
//			uploadRecordsToDatabase();
//		} catch (Exception e) {
//			e.printStackTrace();
//			errorMessage = e.getMessage();
//			setStatus(TaskStatus.ERROR);
//		}
		setStatus(TaskStatus.FINISHED);		
	}

	private void parseFileToRecords() {

		taskDescription = "Parsing T3DB XML file " + hmdbXmlFile.getName() + " ...";		
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
			xsr = xif.createXMLStreamReader(new FileReader(hmdbXmlFile));
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
			    		record = T3DBParser.parseRecord(domElement);
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
	
	private void extractRedundantData() {
		
		taskDescription = "Extracting redundant data ...";		
		total = records.size();
		processed = 0;
		
		bioLocations = new HashMap<Integer, CompoundBioLocation>();
		pathways = new HashMap<Integer, HMDBPathway>();
		deseases = new HashMap<Integer, HMDBDesease>();
		compoundProperties = new HashMap<Integer, CompoundProperty>();
		references = new HashMap<Integer, HMDBCitation>();
		proteinAssociations = new HashMap<Integer, HMDBProteinAssociation>(); 
		
		for(HMDBRecord record : records) {
			
//			HMDBRecord record = (HMDBRecord)DbParserCore.dbUploadCache.get(id);
			
			if(!record.getBiolocations().isEmpty())
				record.getBiolocations().stream().forEach(b -> bioLocations.put(b.hashCode(), b));
			
			if(!record.getPathways().isEmpty())
				record.getPathways().stream().forEach(p -> pathways.put(p.hashCode(), p));
			
			if(!record.getReferences().isEmpty())
				record.getReferences().stream().forEach(r -> references.put(r.hashCode(), r));
							
			if(!record.getDeseases().isEmpty()) {
				
				record.getDeseases().stream().forEach(d -> deseases.put(d.hashCode(), d));
				for(HMDBDesease des : record.getDeseases()) {
					
					if(!des.getReferences().isEmpty())
						des.getReferences().stream().forEach(r -> references.put(r.hashCode(), r));
				}				
			}
			if(!record.getCompoundProperties().isEmpty())
				record.getCompoundProperties().stream().forEach(p -> compoundProperties.put(p.hashCode(), p));
			
			if(!record.getConcentrations().isEmpty()) {

				for(CompoundConcentration conc : record.getConcentrations()) {
					
					if(!conc.getReferences().isEmpty())
						conc.getReferences().stream().forEach(r -> references.put(r.hashCode(), r));
				}				
			}
			if(!record.getProteinAssociations().isEmpty())
				record.getProteinAssociations().stream().forEach(a -> proteinAssociations.put(a.hashCode(), a));
			
			System.out.println(record.getName());
			processed++;
		}
	}
		
	private void insertRedundantData() throws Exception{

		Connection conn = ConnectionManager.getConnection();		
		try {
			uploadBiolocations(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			uploadPathways(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			uploadCompoundProperties(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			uploadDeseases(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			uploadReferences(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			uploadProteinAssociations(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ConnectionManager.releaseConnection(conn);
	}

	private void uploadBiolocations(Connection conn) throws Exception{
		
		taskDescription = "Uploading bio-location data ...";
		total = bioLocations.size();
		processed = 0;
		bioLocationsIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_BIOLOCATION "
				+ "(LOCATION_ID, LOCATION_TYPE, LOCATION) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, CompoundBioLocation> blEntry : bioLocations.entrySet()) {
			
			String blId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.BIOLOC_SEQ", 
					DataPrefix.BIO_LOCATION, "0", 6);
			ps.setString(1, blId);
			ps.setString(2, blEntry.getValue().getLocationType().name());
			ps.setString(3, blEntry.getValue().getLocationName());
			ps.executeUpdate();
			bioLocationsIdMap.put(blEntry.getValue().hashCode(), blId);
			processed++;
		}
		ps.close();
	}
	
	private void uploadPathways(Connection conn) throws Exception{
		
		taskDescription = "Uploading pathway data ...";
		total = pathways.size();
		processed = 0;
		pathwaysIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_PATHWAY "
				+ "(PATHWAY_ID, PATHWAY_NAME, KEGG_MAP_ID, SMPDB_ID) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, HMDBPathway> pathwayEntry : pathways.entrySet()) {
			
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
	}
	
	private void uploadCompoundProperties(Connection conn) throws Exception{
		
		taskDescription = "Uploading compound property types ...";
		total = compoundProperties.size();
		processed = 0;
		compoundPropertiesIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_COMPOUND_PROPERTY "
				+ "(PROPERTY_ID, PROPERTY_TYPE, PROPERTY_NAME) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, CompoundProperty> cpropEntry : compoundProperties.entrySet()) {
			
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
	}
	
	private void uploadDeseases(Connection conn) throws Exception{
		
		taskDescription = "Uploading desease data ...";
		total = deseases.size();
		processed = 0;
		deseasesIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_DESEASE "
				+ "(DESEASE_ID, DESEASE_NAME, OMIM_ID) VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, HMDBDesease> deseaseEntry : deseases.entrySet()) {
			
			String deseaseId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.DESEASE_SEQ", 
					DataPrefix.DESEASE, "0", 7);
			ps.setString(1, deseaseId);
			ps.setString(2, deseaseEntry.getValue().getName());
			
			String omim = deseaseEntry.getValue().getOmimId();
			if(omim != null)
				ps.setString(3, omim);
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			ps.executeUpdate();
			deseasesIdMap.put(deseaseEntry.getValue().hashCode(), deseaseId);
			processed++;
		}
		ps.close();
	}
	
	private void uploadReferences(Connection conn) throws Exception{
		
		taskDescription = "Uploading literature references ...";
		total = references.size();
		processed = 0;
		referencesIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_LITERATURE_REFERENCES "
				+ "(LIT_REF_ID, PUBMED_ID, CITATION_TEXT, HASH_CODE) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, HMDBCitation> litRefEntry : references.entrySet()) {
			
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
	}
		
	private void uploadProteinAssociations(Connection conn) throws Exception{

		taskDescription = "Uploading protein associations ...";
		total = proteinAssociations.size();
		processed = 0;
		proteinAssociationsIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_PROTEIN "
				+ "(PROTEIN_ACCESSION, PROTEIN_NAME, UNIPROT_ID, GENE_NAME, PROTEIN_TYPE) "
				+ "VALUES (?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, HMDBProteinAssociation> litRefEntry : proteinAssociations.entrySet()) {

			ps.setString(1, litRefEntry.getValue().getProteinAccession());
			ps.setString(2, litRefEntry.getValue().getName());
			
			String uniprotId = litRefEntry.getValue().getUniprot();
			if(uniprotId != null)
				ps.setString(3, uniprotId);
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			String geneName = litRefEntry.getValue().getGeneName();
			if(geneName != null)
				ps.setString(4, uniprotId);
			else
				ps.setNull(4, java.sql.Types.NULL);
			
			String proteinType = litRefEntry.getValue().getProteinType();
			if(proteinType != null)
				ps.setString(5, proteinType);
			else
				ps.setNull(5, java.sql.Types.NULL);
			
			ps.executeUpdate();
			proteinAssociationsIdMap.put(
					litRefEntry.getValue().hashCode(), 
					litRefEntry.getValue().getProteinAccession());
			processed++;
		}
		ps.close();
	}
	
	private void uploadRecordsToDatabase() throws Exception{
			
		taskDescription = "Uploading HMDB records ...";
		total = idSet.size();
		processed = 0;
		
		Connection conn = ConnectionManager.getConnection();
		
		//	Compound data
		String compoundDataQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_COMPOUND_DATA "
				+ "(ACCESSION, NAME, FORMULA, EXACT_MASS, SMILES, INCHI, INCHI_KEY, "
				+ "AGGREGATE_STATE, DESCRIPTION, CS_DESCRIPTION, DATE_CREATED, LAST_UPDATED) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement compoundDataPs = conn.prepareStatement(compoundDataQuery);
		
		//	Synonyms
		String synonymsQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_SYNONYMS "
				+ "(ACCESSION, NAME, NTYPE) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement synonymsPs = conn.prepareStatement(synonymsQuery);

		//	Crossref
		String dbCrossrefQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement dbCrossrefPs = conn.prepareStatement(dbCrossrefQuery);
		
		//	Properties
		String cpdPropertiesQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_COMPOUND_PROPERTY_MAP "
				+ "(ACCESSION, PROPERTY_ID, PROPERTY_VALUE, SOURCE) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement cpdPropertiesPs = conn.prepareStatement(cpdPropertiesQuery);
	
		//	Biolocations
		String biolocationsQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_BIOLOCATION_MAP "
				+ "(ACCESSION, LOCATION_ID) "
				+ "VALUES (?, ?)";			
		PreparedStatement biolocationsPs = conn.prepareStatement(biolocationsQuery);
		
		//  Pathways
		String pathwaysQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_PATHWAY_MAP "
						+ "(ACCESSION, PATHWAY_ID) "
						+ "VALUES (?, ?)";
		PreparedStatement pathwaysPs = conn.prepareStatement(pathwaysQuery);
		
		//	Concentrations
		String concentrationsQuery = 
				"INSERT INTO COMPOUNDDB.HMDB_CONCENTRATIONS "
				+ "(ACCESSION, CONC_ID, TYPE, UNITS, VALUE, "
				+ "AGE, SEX, SUBJECT_CONDITION, COMMENTS) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement concentrationsPs = conn.prepareStatement(concentrationsQuery);
		
		//	Concentration references
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
		
		for(HMDBRecord record : records) {
			
//			HMDBRecord record = (HMDBRecord)DbParserCore.dbUploadCache.get(id);
			
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

	private void insertCompoundData(
			HMDBRecord record, 
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
	
	private void insertSynonyms(
			HMDBRecord record, 
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

	private void insertDatabaseCrossReferences(
			HMDBRecord record, 
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

	private void insertCompoundProperties(
			HMDBRecord record, 
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

	private void insertBiolocations(
			HMDBRecord record, 
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

	private void insertPathways(
			HMDBRecord record, 
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

	private void insertConcentrationsWithReferences(
			HMDBRecord record, 
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

	private void insertDiseasesWithReferences(
			HMDBRecord record,
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

	private void insertProteinAssociations(
			HMDBRecord record, 
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

	private void insertGeneralReferences(
			HMDBRecord record, 
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

	@Override
	public Task cloneTask() {
		return new T3DBParseAndUploadTask(hmdbXmlFile);
	}
}
