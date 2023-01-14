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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class DrugBankParserTask extends AbstractTask {

	private File inputFile;

	//	TODO update connection manager if needed to re-parse the data
	public DrugBankParserTask(File inputFile) {

		this.inputFile = inputFile;
		taskDescription = "Importing DrugBank data from " + inputFile.getPath();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = XmlUtils.countRecords(inputFile, "drug");
		processed = 0;
		System.out.println(Integer.toString(total) + " records found");
		parseRecords();
        setStatus(TaskStatus.FINISHED);
	}

	private void parseRecords() {

		taskDescription = "Parsing records...";
        try {
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(inputFile));
			xsr.nextTag(); // Advance to statements element

			//	TransformerFactory tf = TransformerFactory.newInstance( 
			//	"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null );
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DocumentBuilderFactory dbactory = DocumentBuilderFactory.newInstance();
			dbactory.setNamespaceAware(true);

			//	TODO Convert to jdom2
//			Connection conn = CompoundDbConnectionManager.getConnection();
//			while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
//
//			    DOMResult result = new DOMResult();
//			    t.transform(new StAXSource(xsr), result);
//			    Node domNode = result.getNode();
//
//			    if(domNode.getFirstChild().getNodeName().equals("drug")){
//
//			    	DrugBankRecord record = DrugBankParser.parseRecord(domNode.getFirstChild());
//			    	try {
//						DrugBankParser.insertRecord(record, conn);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			    	processed++;
//			    }
//			}
//			CompoundDbConnectionManager.releaseConnection(conn);
		}
        catch (FileNotFoundException e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
        catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
	}

	@Override
	public Task cloneTask() {
		return new DrugBankParserTask(inputFile);
	}
}
















