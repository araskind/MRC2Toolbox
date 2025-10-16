/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.pref.TableColumnState;
import edu.umich.med.mrc2.datoolbox.gui.tables.pref.TableLayoutFields;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class TableLayoutManager {

	private static final Map<String,TableColumnState[]>tableLayouts = 
			new TreeMap<String,TableColumnState[]>();
	private static final File tableLayoutFile = 
			Paths.get(MRC2ToolBoxCore.configDir + "tableLayouts.xml").toFile();
	
	public static void setTableLayout(BasicTable table) {
		tableLayouts.put(table.getClass().getName(), table.getTableLayout());
	}
	
	public static TableColumnState[] getTableLayout(BasicTable table) {
		return tableLayouts.get(table.getClass().getName());
	}
	
	public static void saveLayouts() {
		
		Document document = new Document();
		Element tableLayoutsList = 
				new Element(TableLayoutFields.TableLayoutList.name());
		tableLayoutsList.setAttribute("version", "1.0.0.0");
		
		for(Entry<String, TableColumnState[]> layoutEntry : tableLayouts.entrySet()) {
			
			Element tableLayoutElement = 
					new Element(TableLayoutFields.TableLayout.name());
			tableLayoutElement.setAttribute(
					TableLayoutFields.TableId.name(), layoutEntry.getKey());
			for(TableColumnState state : layoutEntry.getValue()) {
				
				if(state != null)
					tableLayoutElement.addContent(state.getXmlElement());	
				else {
					System.err.println(layoutEntry.getKey() + " NO LAYOUT");
				}
			}			
			tableLayoutsList.addContent(tableLayoutElement);
		}
		document.setContent(tableLayoutsList);	 
		
		//	Save XML document
        try {
            FileWriter writer = new FileWriter(tableLayoutFile, false);
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getCompactFormat());
            outputter.output(document, writer);
         } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void loadLayouts() throws Exception {
		
		if(tableLayoutFile.exists() && tableLayoutFile.canRead()) {
			
			tableLayouts.clear();
			
			SAXBuilder sax = new SAXBuilder();
			Document doc = null;
			try {
				doc = sax.build(tableLayoutFile);
			} catch (JDOMException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();					
			}
			
//			Reader reader = new InputStreamReader(new FileInputStream(tableLayoutFile),"UTF-8");
//			InputSource is = new InputSource(reader);
//			is.setEncoding("UTF-8");
//
//			DocumentBuilder docBuilder = 
//					DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			org.w3c.dom.Document document = docBuilder.parse(is);
						
//			org.jdom2.Element domElement = domBuider.build((org.w3c.dom.Element)document.getElementById("TableLayoutList"));			
			Element tableLayoutsElement = doc.getRootElement();
			
//			DOMBuilder domBuider = new DOMBuilder();
//			Element tableLayoutsElement = 
//					domBuider.build((org.w3c.dom.Element)document.getDocumentElement());
			List<Element> tableLayoutsList = 
					tableLayoutsElement.getChildren(TableLayoutFields.TableLayout.name());
			for(Element tableLayout : tableLayoutsList) {
				
				String tableId = 
						tableLayout.getAttributeValue(TableLayoutFields.TableId.name());
				Collection<TableColumnState>columnStates = 
						new ArrayList<TableColumnState>();
				List<Element> columnStateList = 
						tableLayout.getChildren(TableLayoutFields.TableColumnState.name());
				for(Element stateElement : columnStateList) {
					
					TableColumnState state =null;					
					try {
						state = new TableColumnState(stateElement);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(state != null)
						columnStates.add(state);
				}
				if(tableId != null && !columnStates.isEmpty()) {
					tableLayouts.put(tableId, 
							columnStates.toArray(new TableColumnState[columnStates.size()]));
				}
			}		
		}
	}
}
