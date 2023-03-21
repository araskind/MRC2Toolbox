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

import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class DrugBankParserTask extends AbstractTask {

	private File xmlInputFile;

	//	TODO update connection manager if needed to re-parse the data
	public DrugBankParserTask(File xmlInputFile) {

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
	
	private void extractRedundantData() {
		// TODO Auto-generated method stub
		
	}

	private void insertRedundantData() {
		// TODO Auto-generated method stub
		
	}

	private void uploadRecordsToDatabase() {
		// TODO Auto-generated method stub
		
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
			    if(domNode.getFirstChild().getNodeName().equals("drug")){
			    	
				    org.jdom2.Element domElement = domBuider.build((Element)domNode.getFirstChild());
//			    	T3DBRecord record = null;
			    	try {
//			    		record = T3DBParserJdom2.parseRecord(domElement);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//			    	if(record != null) {
//			    		t3dbRecords.add(record);
//				    	System.out.println("Parsed - " + record.getName());
//			    	}
			    	processed++;
			    }
			}
		}
        catch (Exception e) {
			e.printStackTrace();
		}	
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
		return new DrugBankParserTask(xmlInputFile);
	}
}
















