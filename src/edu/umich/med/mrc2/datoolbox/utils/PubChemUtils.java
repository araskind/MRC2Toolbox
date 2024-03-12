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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescription;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescriptionBundle;
import edu.umich.med.mrc2.datoolbox.misctest.IteratingSDFReaderFixed;

public class PubChemUtils {
	
	public static final String pubchemCidUrl = 
			"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";	
	public static final String pubchemInchiKeyUrl =
			"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/";
	public static final String pubchemByNameUrl =
			"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/";
	public static final String pubchemInchiUrl = 
			"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchi/";
	
	public static final String encoding = StandardCharsets.UTF_8.toString();

	public static PubChemCompoundDescriptionBundle getCompoundDescriptionById(String cid) {
		
		InputStream descStream = null;
		try {
			descStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/description/XML");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(descStream == null)
			return null;
		
		Document xmlDocument = XmlUtils.readXmlStream(descStream);
		return parsePubChemDescription(xmlDocument);
	}
	
	public static PubChemCompoundDescriptionBundle getCompoundDescriptionByInchiKey(String inchiKey) {
		
		InputStream descStream = null;
		try {
			descStream = WebUtils.getInputStreamFromURL(pubchemInchiKeyUrl + inchiKey + "/description/XML");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(descStream == null)
			return null;
		
		Document xmlDocument = XmlUtils.readXmlStream(descStream);
		return parsePubChemDescription(xmlDocument);
	}
	
	public static PubChemCompoundDescriptionBundle getCompoundDescriptionByName(
			String compoundName) throws Exception {
		
		String requestUrl = 
				pubchemByNameUrl + "description/XML?name=" + URLEncoder.encode(compoundName, encoding);
		InputStream descStream = null;
		try {
			descStream = WebUtils.getInputStreamFromURL(requestUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(descStream == null)
			return null;
		
		Document xmlDocument = XmlUtils.readXmlStream(descStream);
		return parsePubChemDescription(xmlDocument);
	}
	
	public static PubChemCompoundDescriptionBundle getCompoundDescriptionByInchi(
			String inchi) throws Exception {
		
		String requestUrl = 
				pubchemInchiUrl + "description/XML?inchi=" + URLEncoder.encode(inchi, encoding);
		InputStream descStream = null;
		try {
			descStream = WebUtils.getInputStreamFromURL(requestUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(descStream == null)
			return null;
		
		Document xmlDocument = XmlUtils.readXmlStream(descStream);
		return parsePubChemDescription(xmlDocument);
	}
	
	public static PubChemCompoundDescriptionBundle parsePubChemDescription(Document xmlDocument) {
		
		Element root = xmlDocument.getRootElement();
		Namespace ns = root.getNamespace();
		String cid = null;
		String title = null;
		String descriptionText = null;
		String sourceName = null;
		String url = null;
		List<PubChemCompoundDescription>descriptions = 
				new ArrayList<PubChemCompoundDescription>();
		List<Element>infoList = root.getChildren("Information", ns);
		for(Element info : infoList) {
			
			if(info.getChild("CID", ns) != null)
				cid = info.getChildText("CID", ns);
			
			if(info.getChild("Title", ns) != null)
				title = info.getChildText("Title", ns);
			
			if(info.getChild("Description", ns) != null)
				descriptionText = info.getChildText("Description", ns);
			
			if(info.getChild("DescriptionSourceName", ns) != null)
				sourceName = info.getChildText("DescriptionSourceName", ns);
			
			if(info.getChild("DescriptionURL", ns) != null)
				url = info.getChildText("DescriptionURL", ns);
			
			if(descriptionText != null) {
				PubChemCompoundDescription desc = 
						new PubChemCompoundDescription(descriptionText, sourceName, url);
				descriptions.add(desc);
			}
		}
		PubChemCompoundDescriptionBundle bundle = 
				new PubChemCompoundDescriptionBundle(cid, title);
		if(!descriptions.isEmpty())
			descriptions.stream().forEach(d -> bundle.addDescription(d));
		
		return bundle;
	}
	
	public static IAtomContainer getMoleculeFromPubChemByInChiKey(String inchiKey) {
		
		String requestUrl = pubchemInchiKeyUrl + inchiKey + "/record/SDF";
		InputStream pubchemDataStream = null;

		try {
			pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
		} catch (Exception e) {
			//	e.printStackTrace();
		}		
		if(pubchemDataStream == null)
			return null;
		
		IteratingSDFReaderFixed reader = 
				new IteratingSDFReaderFixed(
						pubchemDataStream, SilentChemObjectBuilder.getInstance());
		
		IAtomContainer molecule = null;
		while (reader.hasNext())
			molecule = (IAtomContainer)reader.next();
					
		return molecule;
	}
	
	
	public static IAtomContainer getMoleculeFromPubChemByName(String compoundName) throws Exception{

		String requestUrl = 
				pubchemByNameUrl + "record/SDF?name=" + URLEncoder.encode(compoundName, encoding);
		InputStream pubchemDataStream = null;
		try {
			pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
		} catch (Exception e) {
			//	e.printStackTrace();
		}		
		if(pubchemDataStream == null)
			return null;
		
		IteratingSDFReaderFixed reader = 
				new IteratingSDFReaderFixed(
						pubchemDataStream, SilentChemObjectBuilder.getInstance());	
		IAtomContainer molecule = null;
		while (reader.hasNext())
			molecule = (IAtomContainer)reader.next();
					
		return molecule;
	}
	
	public static boolean saveMoleculeFromPubChemByInChiKeyToSDFFile(String inchiKey, File sdfFile) {
		
		String requestUrl = pubchemInchiKeyUrl + inchiKey + "/record/SDF";
		InputStream pubchemDataStream = null;
		try {
			pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
		} catch (Exception e) {
			//	e.printStackTrace();
		}	
		if(pubchemDataStream == null) {
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			requestUrl = pubchemInchiKeyUrl + inchiKey.split("-")[0] + "/record/SDF";
			try {
				pubchemDataStream = WebUtils.getInputStreamFromURL(requestUrl);
			} catch (Exception e) {
				//	e.printStackTrace();
			}	
		}			
		if(pubchemDataStream != null) {
			
			try {
				Files.copy(
						pubchemDataStream,
						sdfFile.toPath(),
						  StandardCopyOption.REPLACE_EXISTING);
				pubchemDataStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}
		else 
			return false;
	}
	
	public static String[]getPubChemSynonymsByCid(String cid){
		
		String[] synonyms = new String[0];
		InputStream synonymStream = null;
		try {
			synonymStream = WebUtils.getInputStreamFromURL(pubchemCidUrl + cid + "/synonyms/TXT");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			//	e.printStackTrace();
		}
		if(synonymStream != null) {
			try {
				synonyms = IOUtils.toString(synonymStream, StandardCharsets.UTF_8).split("\\r?\\n");
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				//	e.printStackTrace();
			}
		}
		return synonyms;
	}
}




















