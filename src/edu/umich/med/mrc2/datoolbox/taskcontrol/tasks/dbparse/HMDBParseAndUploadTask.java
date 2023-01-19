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

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.dbparse.DbParserCore;
import edu.umich.med.mrc2.datoolbox.dbparse.load.CompoundProperty;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.CompoundBioLocation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.CompoundConcentration;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCitation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBDesease;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBParserJdom2;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBPathway;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBProteinAssociation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBRecord;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class HMDBParseAndUploadTask extends AbstractTask {
	
	private File hmdbXmlFile;
	
	private Collection<HMDBRecord>records;
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
	
//	BIOLOC_SEQ
//	PATHWAY_SEQ
//	DESEASE_SEQ
//	LIT_REF_SEQ
//	COMPOUND_PROPERTY_SEQ
//	CONCENTRATION_SEQ
	
	public HMDBParseAndUploadTask(File hmdbXmlFile) {
		super();
		this.hmdbXmlFile = hmdbXmlFile;
		records = new TreeSet<HMDBRecord>();
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

	private void parseFileToRecords() {

		taskDescription = "Parsing HMDB XML file " + hmdbXmlFile.getName() + " ...";		
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
			    if(domNode.getFirstChild().getNodeName().equals("metabolite")){
			    	
				    org.jdom2.Element domElement = domBuider.build((Element)domNode.getFirstChild());
			    	HMDBRecord record = null;
			    	try {
			    		record = HMDBParserJdom2.parseRecord(domElement);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	if(record != null) {
//			    		records.add(record); // Put in cash and keep only ID list in memory?
			    		idSet.add(record.getPrimaryId());
			    		DbParserCore.dbUploadCache.put(record.getPrimaryId(), record);
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
		total = idSet.size();
		processed = 0;
		
		bioLocations = new HashMap<Integer, CompoundBioLocation>();
		pathways = new HashMap<Integer, HMDBPathway>();
		deseases = new HashMap<Integer, HMDBDesease>();
		compoundProperties = new HashMap<Integer, CompoundProperty>();
		references = new HashMap<Integer, HMDBCitation>();
		proteinAssociations = new HashMap<Integer, HMDBProteinAssociation>(); 
		
		for(String id : idSet) {
			
			HMDBRecord record = (HMDBRecord)DbParserCore.dbUploadCache.get(id);
			
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
				+ "AGGREGATE_STATE, DESCRIPTION, CS_DESCRIPTION, DATE_CREATED, LAST_UPDATED, CHARGE) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
				+ "(ACCESSION, PROPERTY_ID, PROPERTY_VALUE) "
				+ "VALUES (?, ?, ?)";
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
				+ "(ACCESSION, CONC_ID, TYPE, UNITS, VALUE, AGE, SEX, SUBJECT_CONDITION, COMMENTS) "
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
		
		for(String id : idSet) {
			
			HMDBRecord record = (HMDBRecord)DbParserCore.dbUploadCache.get(id);
			
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
					insertCompoundProperties(record.getCompoundProperties(), conn, cpdPropertiesPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
			//	Biolocations
			if(!record.getBiolocations().isEmpty()) {				
				try {
					insertBiolocations(record.getBiolocations(), conn, biolocationsPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			//  Pathways
			if(!record.getPathways().isEmpty()) {				
				try {
					insertPathways(record.getPathways(), conn, pathwaysPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			//	Concentration with references
			if(!record.getConcentrations().isEmpty()) {
				try {
					insertConcentrationsWithReferences(
							record.getConcentrations(), conn, concentrationsPs, concRefsPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//	Diseases with references
			if(!record.getDeseases().isEmpty()) {
				try {
					insertDiseasesWithReferences(record.getDeseases(), conn, diseasesPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			//	Protein associations
			if(!record.getProteinAssociations().isEmpty()) {				
				try {
					insertProteinAssociations(record.getProteinAssociations(), conn, protAssocPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			//	General references
			if(!record.getReferences().isEmpty()) {				
				try {
					insertGeneralReferences(record.getReferences(), conn, genRefPs);
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
		// TODO Auto-generated method stub
		
	}
	
	private void insertSynonyms(
			HMDBRecord record, 
			Connection conn, 
			PreparedStatement synonymsPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertDatabaseCrossReferences(
			HMDBRecord record, 
			Connection conn, 
			PreparedStatement dbCrossrefPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertCompoundProperties(
			Collection<CompoundProperty> compoundProperties2, 
			Connection conn,
			PreparedStatement cpdPropertiesPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertBiolocations(
			Collection<CompoundBioLocation> biolocations2, 
			Connection conn,
			PreparedStatement biolocationsPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertPathways(
			Collection<HMDBPathway> pathways2, 
			Connection conn, 
			PreparedStatement pathwaysPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertConcentrationsWithReferences(
			Collection<CompoundConcentration> concentrations, 
			Connection conn,
			PreparedStatement concentrationsPs, 
			PreparedStatement concRefsPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertDiseasesWithReferences(
			Collection<HMDBDesease> deseases2,
			Connection conn,
			PreparedStatement diseasesPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	private void insertProteinAssociations(
			Collection<HMDBProteinAssociation> proteinAssocs, 
			Connection conn,
			PreparedStatement protAssocPs) throws Exception{
		// TODO Auto-generated method stub
		
		
	}

	private void insertGeneralReferences(
			Collection<HMDBCitation> genRefs, 
			Connection conn,
			PreparedStatement genRefPs) throws Exception{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task cloneTask() {
		return new HMDBParseAndUploadTask(hmdbXmlFile);
	}
}
