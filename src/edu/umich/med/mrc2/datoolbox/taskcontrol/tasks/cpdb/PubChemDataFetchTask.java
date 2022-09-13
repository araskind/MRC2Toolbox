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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdb;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescription;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescriptionBundle;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.load.pubchem.PubChemFields;
import edu.umich.med.mrc2.datoolbox.database.load.pubchem.PubChemParser;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.WebUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class PubChemDataFetchTask extends AbstractTask {

	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";

	private Collection<String> idList;
	private List<String> filteredIdList;
	private Collection<String> fetchLog;
	private Connection conn;
	private Collection<CompoundIdentity>importedIds;

	public PubChemDataFetchTask(Collection<String> idList) {
		super();
		this.idList = idList;
		fetchLog = new ArrayList<String>();
		importedIds = new ArrayList<CompoundIdentity>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			conn = ConnectionManager.getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			checkForPresentIds();
		} catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			fetchPubChemData();
		} catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			ConnectionManager.releaseConnection(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void fetchPubChemData() {

		if(filteredIdList.isEmpty())
			return;

		total = filteredIdList.size();
		processed = 0;
		taskDescription = "Fetching data from PubChem ...";
		IteratingSDFReader reader;
		IChemObjectBuilder coBuilder = DefaultChemObjectBuilder.getInstance();
		InputStream pubchemDataStream = null;
		InputStream synonymStream = null;
		List<List<String>> chunks =
				ListUtils.partition(
						filteredIdList.stream().distinct().collect(Collectors.toList()), 10);

		total = chunks.size();
		for(List<String>chunk : chunks) {

			String requestUrl = pubchemCidUrl + StringUtils.join(chunk, ",") + "/SDF";
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(pubchemDataStream != null) {
				reader = new IteratingSDFReader(pubchemDataStream, coBuilder);
				while (reader.hasNext()) {
					IAtomContainer molecule = (IAtomContainer)reader.next();
					String cid = molecule.getProperty(PubChemFields.PUBCHEM_ID.toString());
					try {
						synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/synonyms/TXT");
					} catch (Exception e) {
						e.printStackTrace();
					}
					String[] synonyms = new String[0];
					if(synonymStream != null) {
						try {
							synonyms = IOUtils.toString(synonymStream, StandardCharsets.UTF_8).split("\\r?\\n");
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(synonyms.length == 0)
						synonyms = new String[] {cid};
					
					PubChemCompoundDescriptionBundle descBundle =  getCompoundDescription(cid);					
					String inchiKey = molecule.getProperties().get(PubChemFields.INCHIKEY.toString()).toString();
					CompoundIdentity existingId = null;
					try {
						existingId = CompoundDatabaseUtils.getCompoundByInChiKey(inchiKey, conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(existingId == null) {
						
						try {
							CompoundIdentity inserted = PubChemParser.insertPubchemRecord(molecule, synonyms, descBundle, conn);
							importedIds.add(inserted);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						fetchLog.add("Imported " + synonyms[0] + " (ID " + cid +")");
					}
					else {
						fetchLog.add("Compound " + synonyms[0] + " (ID " + cid +") is already in database as " + 
								existingId.getPrimaryDatabaseId() + " (" + existingId.getName() + ")");
					}
				}
			}
			processed++;
		}
	}
	
	public static PubChemCompoundDescriptionBundle getCompoundDescription(String cid) {
		
		InputStream descStream = null;
		try {
			descStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/description/XML");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(descStream == null)
			return null;		
		
		PubChemCompoundDescriptionBundle bundle = null;
		String title = null;
		String descriptionText = null;
		String sourceName = null;
		String url = null;
		
		Document xmlDocument = XmlUtils.readXmlStream(descStream);	
		Namespace ns = Namespace.getNamespace("http://pubchem.ncbi.nlm.nih.gov/pug_rest");
		List<Element> infoElements = 
				xmlDocument.getRootElement().getChildren("Information", ns);
		
		for(Element infoElement : infoElements) {
						
			Element titleElement = infoElement.getChild("Title", ns);
			if(titleElement != null) {
				title = titleElement.getText();
				bundle = new PubChemCompoundDescriptionBundle(cid, title);
			}				
			Element descElement = infoElement.getChild("Description", ns);
			if(descElement != null)			
				descriptionText = descElement.getText();
				
			Element descSourceElement = infoElement.getChild("DescriptionSourceName", ns);
			if(descSourceElement != null)
				sourceName = descSourceElement.getText();
			
			Element descUrlElement = infoElement.getChild("DescriptionURL", ns);
			if(descUrlElement != null)
				url = descUrlElement.getText();
			
			if(descElement != null) {
				
				PubChemCompoundDescription desc = 
						new PubChemCompoundDescription(descriptionText, sourceName, url);
				bundle.addDescription(desc);
			}
		}
		return bundle;
	}

	private void checkForPresentIds() {

		total = idList.size();
		processed = 0;
		taskDescription = "Checking if PubChem IDs already in database ...";
		filteredIdList = new ArrayList<String>();
		for(String id : idList) {

			String name = PubChemParser.idInDatabase(id, conn);
			if(name == null)
				filteredIdList.add(id);
			else
				fetchLog.add("PUBCHEM ID " + id + " (" + name + ") already in database.");

			processed++;
		}
	}

	@Override
	public Task cloneTask() {
		return new PubChemDataFetchTask(idList);
	}

	/**
	 * @return the fetchLog
	 */
	public Collection<String> getFetchLog() {
		return fetchLog;
	}

	/**
	 * @return the importedIds
	 */
	public Collection<CompoundIdentity> getImportedIds() {
		return importedIds;
	}

}
