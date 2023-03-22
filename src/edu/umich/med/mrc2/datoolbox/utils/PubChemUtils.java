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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescription;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescriptionBundle;

public class PubChemUtils {
	
	public static final String pubchemCidUrl = 
			"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	
	public static final String pubchemInchiKeyUrl =
			"https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchikey/";

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
	
	
}
