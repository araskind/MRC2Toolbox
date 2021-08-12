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
import java.io.FileReader;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XmlUtils {

	public static Document readXmlFile(File file) {

		Document xmlDocument = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			xmlDocument = dBuilder.parse(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlDocument;
	}
	
	public static Document readXmlStream(InputStream stream) {

		Document xmlDocument = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			xmlDocument = dBuilder.parse(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlDocument;
	}
	
	public static int countRecords(File inputFile, String rootNodeName) {

		int recordNumber = 0;

        try {
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(inputFile));
			xsr.nextTag();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();

			while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {

			    DOMResult result = new DOMResult();
			    t.transform(new StAXSource(xsr), result);
			    Node domNode = result.getNode();

			    if(domNode.getFirstChild().getNodeName().equals(rootNodeName))
			    	recordNumber++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return recordNumber;
	}
}
