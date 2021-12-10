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

package edu.umich.med.mrc2.datoolbox.gui.preferences;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterFactory;

public class SmoothingFilterManager {
	
	private static final String FILTER_LIST = "FilterList";
	private static final String FILTER_ID = "FilterId";

	private static final Map<String,Filter>filterMap = 
			new TreeMap<String,Filter>();
	private static final File filterMapFile = 
			Paths.get(MRC2ToolBoxCore.configDir + "SmoothingFilterMap.xml").toFile();
	
	public static void saveFilterMap() {
		
        Document document = new Document();
        Element filterListRoot = new Element(FILTER_LIST);
        filterListRoot.setAttribute("version", "1.0.0.0");
        for(Entry<String,Filter>e : filterMap.entrySet()) {
        	
        	Element filterElement = e.getValue().getXmlElement();
        	filterElement.setAttribute(FILTER_ID, e.getKey());
        	filterListRoot.addContent(filterElement);
        }
        document.addContent(filterListRoot);
        try {
            FileWriter writer = new FileWriter(filterMapFile, false);
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getCompactFormat());
            outputter.output(document, writer);
            outputter.output(document, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void loadFilterMap() {
		
		if(!filterMapFile.exists())
			return;
		
		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(filterMapFile);
			List<Element> list = doc.getRootElement().getChildren();
			for (Element filterElement : list) {
				Filter filter = FilterFactory.getFilter(filterElement);
				String filterId = filterElement.getAttributeValue(FILTER_ID);
				if(filter != null && filterId != null)
					filterMap.put(filterId, filter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static Filter getFilter(String filterId) {
		return filterMap.get(filterId);
	}
	
	public static void addFilter(String filterId, Filter filter) {
		filterMap.put(filterId, filter);
	}
}
