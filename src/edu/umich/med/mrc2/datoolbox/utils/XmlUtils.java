/*******************************************************************************
 * 
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;

public class XmlUtils {
	
	private static final Logger logger = LogManager.getLogger(XmlUtils.class);

	private XmlUtils() {
		/* This utility class should not be instantiated */
	}
	
	private static SAXBuilder getSAXBuilder() {
		
		SAXBuilder sax = new SAXBuilder();
		sax.setXMLReaderFactory(XMLReaders.NONVALIDATING);
		sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		sax.setFeature("http://xml.org/sax/features/external-general-entities", false);
		sax.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		sax.setFeature("http://xml.org/sax/features/namespaces", true);
		sax.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		return sax;
	}

	public static Document readXmlFile(File file) {

		Document xmlDocument = null;
		SAXBuilder sax = getSAXBuilder();
		try {
			xmlDocument = sax.build(file);
		} catch (Exception e) {
			logger.error(String.format("Failed to parse XML file %s", file.getAbsolutePath()), e);
		}
		return xmlDocument;
	}
	
	public static Document readXmlString(String input) {
		
		Document xmlDocument = null;
		SAXBuilder sax = getSAXBuilder();
		InputSource source = new InputSource(new StringReader(input));
		try {
			xmlDocument = sax.build(source);
		} catch (Exception e) {
			logger.error("Failed to create XML document from string", e);
		}
		return xmlDocument;
	}
	
	public static Document readXmlStream(InputStream stream) {

		Document xmlDocument = null;
		SAXBuilder sax = getSAXBuilder();
		InputSource source = new InputSource(stream);
		try {
			xmlDocument = sax.build(source);
		} catch (Exception e) {
			logger.error("Failed to load read XML stream", e);
		}
		return xmlDocument;
	}
	
	public static Document readXmlFileWithEncoding(File file, Charset encoding) {

		Document xmlDocument = null;
		SAXBuilder sax = getSAXBuilder();
		try (InputStream inputStream = new FileInputStream(file)){
			
		    InputSource lSource = new InputSource(new FileInputStream(file));
	        lSource.setEncoding(encoding.displayName());
			xmlDocument = sax.build(lSource);
		} catch (JDOMException | IOException e) {
			logger.error(String.format("Failed to parse XML file %s", file.getAbsolutePath()), e);
		}		
	    return xmlDocument;
	}
	
	public static int countRecords(File inputFile, String rootNodeName) {

		int recordNumber = 0;
		XMLInputFactory xif = XMLInputFactory.newInstance();
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        try (FileReader fr = new FileReader(inputFile)){
        	
        	Transformer t = tf.newTransformer();
			XMLStreamReader xsr = xif.createXMLStreamReader(fr);
			xsr.nextTag();
			while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
			    DOMResult result = new DOMResult();
			    t.transform(new StAXSource(xsr), result);
			    if(result.getNode().getFirstChild().getNodeName().equals(rootNodeName))
			    	recordNumber++;
			}
		} catch (Exception e) {
			logger.error(String.format("Failed to count '%s' records in XML file %s", 
					rootNodeName, inputFile.getAbsolutePath()), e);
		}
        return recordNumber;
	}

	public static void writePrettyPrintXMLtoFile(
			Document xmlDocument, 
			File xmlFile) {
		writeXMLtoFile(xmlDocument, xmlFile, Format.getPrettyFormat(), false);
	}
	
	public static void writeCompactXMLtoFile(
			Document xmlDocument, 
			File xmlFile) {
		writeXMLtoFile(xmlDocument, xmlFile, Format.getCompactFormat(), false);
	}
	
	/**
	 * Format options Format.getCompactFormat(), Format.getPrettyFormat()
	 * @param xmlDocument
	 * @param xmlFile
	 * @param outputFormat
	 */
	public static void writeXMLtoFile(
			Document xmlDocument, 
			File xmlFile, 
			Format outputFormat,
			boolean append) {
		
		try (FileWriter writer = new FileWriter(xmlFile, StandardCharsets.UTF_8, append)) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(outputFormat);
			outputter.output(xmlDocument, writer);
		} catch (IOException e) {
			logger.error(String.format("Failed to output XML file %s", xmlFile.getAbsolutePath()), e);
		}
	}
}
