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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugBankCompoundProperties;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugBankDescriptiveFields;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugBankExternalLink;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugBankParserJdom2;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugBankRecord;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugCategory;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugPathway;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugTarget;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCitation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBPathway;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class DrugBankParseAndUploadTask extends AbstractTask {

	private File xmlInputFile;
	
	private Collection<DrugBankRecord>records;
	
	private Map<String,Collection<String>>externalLinkCollection;	
	private Map<Integer,DrugCategory>drugCategoryMap;
	private Map<Integer,DrugTarget>drugTargetMap;
	private Map<Integer,DrugPathway>drugPathwayMap;
	private Map<Integer,HMDBCitation>referencesMap;
	
	protected Map<Integer, String>drugCategoryIdMap;
	protected Map<Integer, String>drugTargetIdMap;
	protected Map<Integer, String>drugPathwayIdMap;
	protected Map<Integer, String>referencesIdMap;

	private Collection<CompoundProperty> hmdbCompoundProperties;
	private HashMap<Integer, String> compoundPropertiesIdMap;
	private Collection<HMDBPathway> hmdbPathways;
	private HashMap<Integer, String> pathwaysIdMap;

	private HashMap<Integer, CompoundProperty> missingCompoundProperties;

	public DrugBankParseAndUploadTask(File xmlInputFile) {
		super();
		this.xmlInputFile = xmlInputFile;		
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

		taskDescription = "Parsing DrugBank XML file " + xmlInputFile.getName() + " ...";		
		total = 15235;
		processed = 0;
		records = new ArrayList<DrugBankRecord>();
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
			errorMessage = e1.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
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
			    if(domNode.getFirstChild().getNodeName().equals("drug")){
			    	
				    org.jdom2.Element domElement = 
				    		domBuider.build((Element)domNode.getFirstChild());
				    DrugBankRecord record = null;
			    	try {
			    		record = DrugBankParserJdom2.parseRecord(domElement);
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
			errorMessage = e.getMessage();
			setStatus(TaskStatus.ERROR);
			return;
		}	
	}
	
	private void getHMDBMetadata() throws Exception{

		Connection conn = ConnectionManager.getConnection();
		
		hmdbCompoundProperties = 
				HMDBUtils.getCompoundProperties(conn);
		compoundPropertiesIdMap = new HashMap<Integer, String>();
		hmdbCompoundProperties.stream().
			forEach(l -> compoundPropertiesIdMap.put(l.hashCode(), l.getGlobalId()));
		
		hmdbPathways = 
				HMDBUtils.getHMDBPathways(conn);		
		pathwaysIdMap = new HashMap<Integer, String>();
		hmdbPathways.stream().
			forEach(l -> pathwaysIdMap.put(l.hashCode(), l.getGlobalId()));
		
		referencesIdMap = HMDBUtils.getHMDBReferencesMap();
		
		ConnectionManager.releaseConnection(conn);
	}
	
	private void extractRedundantData() {
		
		taskDescription = "Extracting redundant data ...";		
		total = 100;
		processed = 0;
		
		drugCategoryMap = new TreeMap<Integer,DrugCategory>();
		records.stream().
			flatMap(r -> r.getCategories().stream()).
			forEach(c -> drugCategoryMap.put(c.hashCode(), c));
		processed = 20;
		
		drugTargetMap = new TreeMap<Integer,DrugTarget>();
		drugTargetIdMap = new TreeMap<Integer, String>();
		drugTargetMap.values().stream().
			forEach(t -> drugTargetIdMap.put(t.hashCode(), t.getId()));
		records.stream().
			flatMap(r -> r.getDrugTargets().stream()).
			forEach(t -> drugTargetMap.put(t.hashCode(), t));
		processed = 40;
		
		drugPathwayMap = new TreeMap<Integer,DrugPathway>();
		records.stream().
			flatMap(r -> r.getPathways().stream()).
			forEach(p -> drugPathwayMap.put(p.hashCode(), p));
		processed = 60;
		
		referencesMap = new TreeMap<Integer,HMDBCitation>();
		records.stream().
			flatMap(r -> r.getReferences().stream()).
			forEach(r -> referencesMap.put(r.hashCode(), r));
		records.stream().
			flatMap(r -> r.getDrugTargets().stream()).
			flatMap(t -> t.getReferences().stream()).
			forEach(r -> referencesMap.put(r.hashCode(), r));
		processed = 80;
		
		missingCompoundProperties = 
				new HashMap<Integer, CompoundProperty>();
		records.stream().
			flatMap(r -> r.getCompoundProperties().stream()).
			filter(p -> compoundPropertiesIdMap.get(p.hashCode()) == null).
			forEach(p -> missingCompoundProperties.put(p.hashCode(),p));
		processed = 100;
	}

	private void insertRedundantData() throws Exception{

		Connection conn = ConnectionManager.getConnection();		
		try {
			uploadCategoriess(conn);
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
			uploadPathways(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			uploadTargets(conn);
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
		ConnectionManager.releaseConnection(conn);
	}
	
	protected void uploadCompoundProperties(Connection conn) throws Exception{
		
		taskDescription = "Uploading compound property types ...";
		total = missingCompoundProperties.size();
		processed = 0;

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
	}
	
	protected void uploadCategoriess(Connection conn) throws Exception{
		
		taskDescription = "Uploading drug category data ...";
		total = drugCategoryMap.size();
		processed = 0;
		drugCategoryIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_CATEGORIES "
				+ "(CATEGORY_ID, CATEGORY_NAME, MESH_TERM) "
				+ "VALUES (?, ?, ?)";
		
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, DrugCategory> categoryEntry : drugCategoryMap.entrySet()) {
			
			String categoryId = SQLUtils.getNextIdFromSequence(
					conn, "COMPOUNDDB.DRUG_CATEGORY_SEQ", 
					DataPrefix.DRUG_CATEGORY, "0", 7);
			ps.setString(1, categoryId);
			ps.setString(2, categoryEntry.getValue().getName());
			
			String meshId = categoryEntry.getValue().getMeshId();
			if(meshId != null)
				ps.setString(3, meshId);
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			ps.executeUpdate();
			drugCategoryIdMap.put(
					categoryEntry.getValue().hashCode(), 
					categoryId);
			processed++;
		}
		ps.close();
	}
	
	protected void uploadPathways(Connection conn) throws Exception{
		
		taskDescription = "Uploading pathway data ...";
		total = drugPathwayMap.size();
		processed = 0;
		drugPathwayIdMap = new TreeMap<Integer, String>();
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_PATHWAY "
				+ "(PATHWAY_ID, PATHWAY_NAME, SMPDB_ID, CATEGORY) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		for(Entry<Integer, DrugPathway> pathwayEntry : drugPathwayMap.entrySet()) {
			
			DrugPathway pw = pathwayEntry.getValue();
			if(!pathwaysIdMap.containsKey(pw.hashCode())) {
				
				String pathwayId = SQLUtils.getNextIdFromSequence(
						conn, "COMPOUNDDB.PATHWAY_SEQ", 
						DataPrefix.BIOCHEMICAL_PATHWAY, "0", 6);
				ps.setString(1, pathwayId);
				ps.setString(2, pathwayEntry.getValue().getName());
				
				String smpdbId = pathwayEntry.getValue().getSmpdbId();
				if(smpdbId != null)
					ps.setString(3, smpdbId);
				else
					ps.setNull(3, java.sql.Types.NULL);
				
				String category = pathwayEntry.getValue().getCategory();
				if(category != null)
					ps.setString(4, category);
				else
					ps.setNull(4, java.sql.Types.NULL);
				
				ps.executeUpdate();
				drugPathwayIdMap.put(
						pathwayEntry.getValue().hashCode(), 
						pathwayId);
			}
			processed++;
		}
		ps.close();
	}
	
	protected void uploadTargets(Connection conn) throws Exception{

		taskDescription = "Uploading pathway data ...";
		total = drugTargetMap.size();
		processed = 0;
		String query = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_TARGETS "
				+ "(TARGET_ID, NAME, ORGANISM) "
				+ "VALUES (?, ?, ?)";		
		PreparedStatement ps = conn.prepareStatement(query);			
		for(Entry<Integer, DrugTarget> pathwayEntry : drugTargetMap.entrySet()) {
			
			DrugTarget t = pathwayEntry.getValue();
			ps.setString(1, t.getId());
			ps.setString(2, t.getName());
			
			String organism = t.getOrganizm();
			if(organism != null)
				ps.setString(3, organism);
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			ps.executeUpdate();
			processed++;
		}
		ps.close();
	}
	
	protected void uploadReferences(Connection conn) throws Exception{
		
		taskDescription = "Uploading literature references ...";
		total = referencesMap.size();
		processed = 0;
		String query = 
				"INSERT INTO COMPOUNDDB.HMDB_LITERATURE_REFERENCES "
				+ "(LIT_REF_ID, PUBMED_ID, CITATION_TEXT, HASH_CODE) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		for(Entry<Integer, HMDBCitation> litRefEntry : referencesMap.entrySet()) {
			
			if(!referencesIdMap.containsKey(litRefEntry.getValue().hashCode())) {

				try {
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
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			processed++;
		}
		ps.close();
	}

	protected void uploadRecordsToDatabase() throws Exception{
		
		taskDescription = "Uploading HMDB records ...";
		total = records.size();
		processed = 0;
		
		Connection conn = ConnectionManager.getConnection();
		
		//	Compound data
		String compoundDataQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_COMPOUND_DATA " +
				"(ACCESSION, COMMON_NAME, MOL_FORMULA, EXACT_MASS, SMILES, INCHI, INCHI_KEY, AGGREGATE_STATE,  " +
				"DESCRIPTION, INDICATION, PHARMACODYNAMICS, MECHANISM_OF_ACTION, TOXICITY,  " +
				"METABOLISM, ABSORPTION, HALF_LIFE, PROTEIN_BINDING, ROUTE_OF_ELIMINATION,  " +
				"VOLUME_OF_DISTRIBUTION, CLEARANCE, DATE_CREATED, LAST_UPDATED) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement compoundDataPs = conn.prepareStatement(compoundDataQuery);
		
		
		//	Synonyms
		String synonymsQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_SYNONYMS "
				+ "(ACCESSION, NAME, NTYPE) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement synonymsPs = conn.prepareStatement(synonymsQuery);

		//	Crossref
		String dbCrossrefQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_CROSSREF "
				+ "(ACCESSION, SOURCE_DB, SOURCE_DB_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement dbCrossrefPs = conn.prepareStatement(dbCrossrefQuery);
		
		//	Properties
		String cpdPropertiesQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_COMPOUND_PROPERTY_MAP "
				+ "(ACCESSION, PROPERTY_ID, PROPERTY_VALUE, SOURCE) "
				+ "VALUES (?, ?, ?, ?)";
		PreparedStatement cpdPropertiesPs = conn.prepareStatement(cpdPropertiesQuery);
	
		//	Categories
		String categoriesQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_CATEGORY_MAP "
				+ "(ACCESSION, CATEGORY_ID) "
				+ "VALUES (?, ?)";					
		PreparedStatement categoriesPs = conn.prepareStatement(categoriesQuery);
		
		//  Pathways
		String pathwaysQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_PATHWAY_MAP "
				+ "(ACCESSION, PATHWAY_ID) "
				+ "VALUES (?, ?)";
		PreparedStatement pathwaysPs = conn.prepareStatement(pathwaysQuery);
		
		//	Pathway enzymes
		String pathwayEnzymesQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_PATHWAY_ENZYME_MAP "
				+ "(ACCESSION, PATHWAY_ID, ENZYME_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement pathwayEnzymesPs = conn.prepareStatement(pathwayEnzymesQuery);
		
		//	Pathway drugs
		String pathwayDrugsQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_PATHWAY_DRUG_MAP "
				+ "(ACCESSION, PATHWAY_ID, DRUG_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement pathwayDrugsPs = conn.prepareStatement(pathwayDrugsQuery);
		
		//	Drug targets
		String drugTargetsQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_TARGET_MAP "
				+ "(ACCESSION, TARGET_ID) VALUES (?, ?)";
		PreparedStatement drugTargetsPs = conn.prepareStatement(drugTargetsQuery);
		
		//	Drug target references
		String drugTargetsRefsQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_TARGET_REFERENCE_MAP "
				+ "(ACCESSION, TARGET_ID, LIT_REF_ID) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement dtRefsPs = conn.prepareStatement(drugTargetsRefsQuery);
		
		//	Target actions
		String drugTargetActionsQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_TARGET_ACTION_MAP "
				+ "(ACCESSION, TARGET_ID, ACTION) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement drugTargetActionsPs = conn.prepareStatement(drugTargetActionsQuery);		
		
		//	General references
		String genRefQuery = 
				"INSERT INTO COMPOUNDDB.DRUGBANK_GENERAL_LIT_REFERENCE_MAP "
				+ "(ACCESSION, LIT_REF_ID) "
				+ "VALUES (?, ?)";
		PreparedStatement genRefPs = conn.prepareStatement(genRefQuery);
		
		for(DrugBankRecord record : records) {
			
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
			//	Categories
			if(!record.getCategories().isEmpty()) {				
				try {
					insertCategories(record, conn, categoriesPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			//  Pathways
			if(!record.getPathways().isEmpty()) {				
				try {
					insertPathways(
							record, 
							conn, 
							pathwaysPs,
							pathwayEnzymesPs,
							pathwayDrugsPs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}			
			//	Targets with references
			if(!record.getDrugTargets().isEmpty()) {
				try {
					insertDrugTargetsWithReferences(
							record, 
							conn, 
							drugTargetsPs, 
							dtRefsPs, 
							drugTargetActionsPs);
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
		categoriesPs.close();		
		pathwaysPs.close();
		pathwayEnzymesPs.close();
		pathwayDrugsPs.close();
		drugTargetsPs.close();
		dtRefsPs.close();
		drugTargetActionsPs.close();
		genRefPs.close();
		
		ConnectionManager.releaseConnection(conn);	
	}

	protected void insertCompoundData(
			DrugBankRecord record, 
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
		
		String indication = 
				record.getDescriptiveField(DrugBankDescriptiveFields.INDICATION);
		if(indication != null && !indication.isEmpty())
			compoundDataPs.setString(10, indication);
		else
			compoundDataPs.setNull(10, java.sql.Types.NULL);
		
		String pharmacodynamics = 
				record.getDescriptiveField(DrugBankDescriptiveFields.PHARMACODYNAMICS);
		if(pharmacodynamics != null && !pharmacodynamics.isEmpty())
			compoundDataPs.setString(11, pharmacodynamics);
		else
			compoundDataPs.setNull(11, java.sql.Types.NULL);
		
		String mechanismOfAction = 
				record.getDescriptiveField(DrugBankDescriptiveFields.MECHANISMOFACTION);
		if(mechanismOfAction != null && !mechanismOfAction.isEmpty())
			compoundDataPs.setString(12, mechanismOfAction);
		else
			compoundDataPs.setNull(12, java.sql.Types.NULL);
		
		String toxicity = 
				record.getDescriptiveField(DrugBankDescriptiveFields.TOXICITY);
		if(toxicity != null && !toxicity.isEmpty())
			compoundDataPs.setString(13, toxicity);
		else
			compoundDataPs.setNull(13, java.sql.Types.NULL);
		
		String metabolism = 
				record.getDescriptiveField(DrugBankDescriptiveFields.METABOLISM);
		if(metabolism != null && !metabolism.isEmpty())
			compoundDataPs.setString(14, metabolism);
		else
			compoundDataPs.setNull(14, java.sql.Types.NULL);
		
		String absorption = 
				record.getDescriptiveField(DrugBankDescriptiveFields.ABSORPTION);
		if(absorption != null && !absorption.isEmpty())
			compoundDataPs.setString(15, absorption);
		else
			compoundDataPs.setNull(15, java.sql.Types.NULL);
		
		String halfLife = 
				record.getDescriptiveField(DrugBankDescriptiveFields.HALFLIFE);
		if(halfLife != null && !halfLife.isEmpty())
			compoundDataPs.setString(16, halfLife);
		else
			compoundDataPs.setNull(16, java.sql.Types.NULL);
		
		String proteinBinding = 
				record.getDescriptiveField(DrugBankDescriptiveFields.PROTEINBINDING);
		if(proteinBinding != null && !proteinBinding.isEmpty())
			compoundDataPs.setString(17, proteinBinding);
		else
			compoundDataPs.setNull(17, java.sql.Types.NULL);
		
		String routeOfElimination = 
				record.getDescriptiveField(DrugBankDescriptiveFields.ROUTEOFELIMINATION);
		if(routeOfElimination != null && !routeOfElimination.isEmpty())
			compoundDataPs.setString(18, routeOfElimination);
		else
			compoundDataPs.setNull(18, java.sql.Types.NULL);
		
		String volumeOfDistribution = 
				record.getDescriptiveField(DrugBankDescriptiveFields.VOLUMEOFDISTRIBUTION);
		if(volumeOfDistribution != null && !volumeOfDistribution.isEmpty())
			compoundDataPs.setString(19, volumeOfDistribution);
		else
			compoundDataPs.setNull(19, java.sql.Types.NULL);
		
		String clearance = 
				record.getDescriptiveField(DrugBankDescriptiveFields.CLEARANCE);
		if(clearance != null && !clearance.isEmpty())
			compoundDataPs.setString(20, clearance);
		else
			compoundDataPs.setNull(20, java.sql.Types.NULL);
		
		compoundDataPs.setDate(21, new java.sql.Date(record.getDateCreated().getTime()));
		compoundDataPs.setDate(22, new java.sql.Date(record.getLastUpdated().getTime()));
		compoundDataPs.executeUpdate();
	}
	
	protected void insertSynonyms(
			DrugBankRecord record, 
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
		String sysName = 
				record.getPropertyValue(DrugBankCompoundProperties.IUPAC_NAME);		
		if(sysName != null) {
			
			synonymsPs.setString(2, sysName);
			synonymsPs.setString(3, CompoundNameCategory.SYS.name());
			synonymsPs.addBatch();
		}
		String traditionalIupacName = 
				record.getPropertyValue(DrugBankCompoundProperties.TRADITIONAL_IUPAC_NAME);
		if(traditionalIupacName != null) {
			
			synonymsPs.setString(2, traditionalIupacName);
			synonymsPs.setString(3, CompoundNameCategory.IUP.name());
			synonymsPs.addBatch();
		}		
		synonymsPs.executeBatch();
	}

	protected void insertDatabaseCrossReferences(
			DrugBankRecord record, 
			Connection conn, 
			PreparedStatement dbCrossrefPs) throws Exception{

		dbCrossrefPs.setString(1, record.getPrimaryId());
		dbCrossrefPs.setString(2, CompoundDatabaseEnum.DRUGBANK.name() );
		dbCrossrefPs.setString(3, record.getPrimaryId());
		dbCrossrefPs.addBatch();
		for(Entry<CompoundDatabaseEnum, String> dbRef : record.getCompoundIdentity().getDbIdMap().entrySet()) {
			
			dbCrossrefPs.setString(2, dbRef.getKey().name());
			dbCrossrefPs.setString(3, dbRef.getValue());
			dbCrossrefPs.addBatch();
		}
		if(record.getSecondaryIds() != null 
				&& !record.getSecondaryIds().isEmpty()) {
			
			for(String secondaryHmdbAccesion : record.getSecondaryIds()) {
				
				dbCrossrefPs.setString(2, CompoundDatabaseEnum.DRUGBANK_SECONDARY.name());
				dbCrossrefPs.setString(3, secondaryHmdbAccesion);
				dbCrossrefPs.addBatch();
			}
		}
		dbCrossrefPs.executeBatch();
	}

	protected void insertCompoundProperties(
			DrugBankRecord record, 
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
	
	private void insertCategories(
			DrugBankRecord record, 
			Connection conn, 
			PreparedStatement categoriesPs) throws Exception{
		
		categoriesPs.setString(1, record.getPrimaryId());
		for(DrugCategory category : record.getCategories()) {
			
			String catId = drugCategoryIdMap.get(category.hashCode());
			if(catId != null) {
				
				categoriesPs.setString(2, catId);
				categoriesPs.addBatch();
			}
		}
		categoriesPs.executeBatch();
	}

	protected void insertPathways(
			DrugBankRecord record, 
			Connection conn, 
			PreparedStatement pathwaysPs, 
			PreparedStatement pathwayEnzymesPs, 
			PreparedStatement pathwayDrugsPs) throws Exception{
		
		pathwaysPs.setString(1, record.getPrimaryId());
		pathwayEnzymesPs.setString(1, record.getPrimaryId());
		pathwayDrugsPs.setString(1, record.getPrimaryId());
		for(DrugPathway pathway : record.getPathways()) {
			
			String pathwayId = pathwaysIdMap.get(pathway.hashCode());
			if(pathwayId != null) {
				
				pathwaysPs.setString(2, pathwayId);
				pathwaysPs.addBatch();
				if(!pathway.getEnzymes().isEmpty()) {
					
					pathwayEnzymesPs.setString(2, pathwayId);
					for(String enzyme : pathway.getEnzymes()) {
						
						pathwayEnzymesPs.setString(3, enzyme);
						pathwayEnzymesPs.addBatch();
					}
					pathwayEnzymesPs.executeBatch();
				}	
				if(!pathway.getDrugs().isEmpty()) {
					
					pathwayDrugsPs.setString(2, pathwayId);
					for(String drug : pathway.getDrugs()) {
						
						pathwayDrugsPs.setString(3, drug);
						pathwayDrugsPs.addBatch();
					}
					pathwayDrugsPs.executeBatch();
				}
			}
		}	
		pathwaysPs.executeBatch();
	}

	protected void insertDrugTargetsWithReferences(
			DrugBankRecord record, 
			Connection conn,
			PreparedStatement drugTargetsPs, 
			PreparedStatement dtRefsPs, 
			PreparedStatement drugTargetActionsPs) throws Exception{

		drugTargetsPs.setString(1, record.getPrimaryId());
		dtRefsPs.setString(1, record.getPrimaryId());
		drugTargetActionsPs.setString(1, record.getPrimaryId());
		
		for(DrugTarget target : record.getDrugTargets()) {
			
			drugTargetsPs.setString(2, target.getId());
			drugTargetsPs.addBatch();
			
			if(!target.getReferences().isEmpty()) {
				
				dtRefsPs.setString(2, target.getId());
				for(HMDBCitation ref : target.getReferences()) {
					
					String refId = referencesIdMap.get(ref.hashCode());
					if(refId != null) {
						dtRefsPs.setString(3, refId);
						dtRefsPs.addBatch();
					}					
				}
				dtRefsPs.executeBatch();
			}
			if(!target.getActions().isEmpty()) {
				
				drugTargetActionsPs.setString(2, target.getId());
				for(String action : target.getActions()) {
	
					drugTargetActionsPs.setString(3, action);
					drugTargetActionsPs.addBatch();										
				}
				drugTargetActionsPs.executeBatch();
			}
		}
		drugTargetsPs.executeBatch();
	}
	
	protected void insertGeneralReferences(
			DrugBankRecord record, 
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
	
//	private void parseRecords() {
//
//		taskDescription = "Parsing records...";
//        try {
//			XMLInputFactory xif = XMLInputFactory.newInstance();
//			XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(inputFile));
//			xsr.nextTag(); // Advance to statements element
//
//			//	TransformerFactory tf = TransformerFactory.newInstance( 
//			//	"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null );
//			TransformerFactory tf = TransformerFactory.newInstance();
//			Transformer t = tf.newTransformer();
//			t.setOutputProperty(OutputKeys.METHOD, "xml");
//			t.setOutputProperty(OutputKeys.INDENT, "yes");
//			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//
//			DocumentBuilderFactory dbactory = DocumentBuilderFactory.newInstance();
//			dbactory.setNamespaceAware(true);
//
//			//	TODO Convert to jdom2
////			Connection conn = CompoundDbConnectionManager.getConnection();
////			while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
////
////			    DOMResult result = new DOMResult();
////			    t.transform(new StAXSource(xsr), result);
////			    Node domNode = result.getNode();
////
////			    if(domNode.getFirstChild().getNodeName().equals("drug")){
////
////			    	DrugBankRecord record = DrugBankParser.parseRecord(domNode.getFirstChild());
////			    	try {
////						DrugBankParser.insertRecord(record, conn);
////					} catch (Exception e) {
////						// TODO Auto-generated catch block
////						e.printStackTrace();
////					}
////			    	processed++;
////			    }
////			}
////			CompoundDbConnectionManager.releaseConnection(conn);
//		}
//        catch (FileNotFoundException e) {
//			setStatus(TaskStatus.ERROR);
//			e.printStackTrace();
//		}
//        catch (Exception e) {
//			setStatus(TaskStatus.ERROR);
//			e.printStackTrace();
//		}
//	}

	@Override
	public Task cloneTask() {
		return new DrugBankParseAndUploadTask(xmlInputFile);
	}
	
	private void extractExtarnalLinks() {
		
		externalLinkCollection = new TreeMap<String,Collection<String>>();
		for(DrugBankRecord record : records) {
			
			Collection<DrugBankExternalLink> rel = record.getExternalLinks();
			if(!rel.isEmpty()) {
				
				for(DrugBankExternalLink el : rel) {
					
					if(!externalLinkCollection.containsKey(el.getResource()))
						externalLinkCollection.put(el.getResource(), new ArrayList<String>());
					
					externalLinkCollection.get(el.getResource()).add(el.getUrl());
				}
			}
			processed++;
		}
		String basePath = xmlInputFile.getParentFile().getAbsolutePath();
		for(Entry<String, Collection<String>> entry : externalLinkCollection.entrySet()) {
			
			Path outputPath = Paths.get(basePath, entry.getKey() + "URLs.TXT");
			try {
				Files.write(
						outputPath, 
						entry.getValue(), 
						StandardCharsets.UTF_8, 
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
















