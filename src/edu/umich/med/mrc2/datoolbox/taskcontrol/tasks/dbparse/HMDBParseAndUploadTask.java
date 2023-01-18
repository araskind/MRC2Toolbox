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
import java.util.Collection;
import java.util.Set;
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

import edu.umich.med.mrc2.datoolbox.dbparse.DbParserCore;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBParserJdom2;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBRecord;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class HMDBParseAndUploadTask extends AbstractTask {
	
	private File hmdbXmlFile;
	private Collection<HMDBRecord>records;
	private Set<String>idSet;
	
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
			    	System.out.println("metabolite - " + Integer.toString(processed));
			    	HMDBRecord record = null;
			    	try {
			    		record = HMDBParserJdom2.parseRecord(domElement);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	if(record != null) {
			    		records.add(record); // Put in cash and keep only ID list in memory?
			    		idSet.add(record.getPrimaryId());
			    		DbParserCore.dbUploadCache.put(record.getPrimaryId(), record);
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
		for(String id : idSet) {
			
			HMDBRecord record = (HMDBRecord)DbParserCore.dbUploadCache.get(id);
			System.out.println(record.getName());
			processed++;
		}
	}
	
	private void uploadRecordsToDatabase() {
		// TODO Auto-generated method stub
//		Connection conn = CompoundDbConnectionManager.getConnection();
//		
//		CompoundDbConnectionManager.releaseConnection(conn);	
	}

	@Override
	public Task cloneTask() {
		return new HMDBParseAndUploadTask(hmdbXmlFile);
	}
}
