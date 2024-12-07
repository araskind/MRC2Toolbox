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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;

public class XmlUtils {

	public static Document readXmlFile(File file) {

		Document xmlDocument = null;
		try {
			SAXBuilder sax = new SAXBuilder();
			xmlDocument = sax.build(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlDocument;
	}
	
	public static Document readXmlString(String input) {
		
		Document xmlDocument = null;
		try {
			SAXBuilder sax = new SAXBuilder();
			xmlDocument = sax.build(new InputSource(input));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlDocument;
	}
	
	public static Document readXmlStream(InputStream stream) {

		Document xmlDocument = null;
		try {
			SAXBuilder sax = new SAXBuilder();
			xmlDocument = sax.build(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlDocument;
	}
	
	public static Document readXmlFromString(String input) {
		
		Document xmlDocument = null;
		SAXBuilder sax = new SAXBuilder();
		sax.setXMLReaderFactory(XMLReaders.NONVALIDATING);
		sax.setFeature("http://xml.org/sax/features/namespaces", true);
		sax.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		
	    InputSource lSource = new InputSource(new java.io.StringReader(input));
        lSource.setEncoding("UTF-8");
        try {
			xmlDocument = sax.build(lSource);
		} catch (JDOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return xmlDocument;
	}
	
	public static Document readXmlFileWithEncoding(File file, Charset encoding) {

		Document xmlDocument = null;
		SAXBuilder sax = new SAXBuilder();
		sax.setXMLReaderFactory(XMLReaders.NONVALIDATING);
		sax.setFeature("http://xml.org/sax/features/namespaces", true);
		sax.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	    InputSource lSource = new InputSource(inputStream);
        lSource.setEncoding(encoding.displayName());
        try {
			xmlDocument = sax.build(lSource);
		} catch (JDOMException | IOException e) {
			// TODO Auto-generated catch block
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
			    if(result.getNode().getFirstChild().getNodeName().equals(rootNodeName))
			    	recordNumber++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return recordNumber;
	}
	
	public static void writeXMLDocumentToFile(Document xmlDocument, File destinationFile) {
		
		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(destinationFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    try {
			xmlOutputter.output(xmlDocument, fileWriter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    if(fileWriter != null) {
			try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
}
